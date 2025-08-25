package com.dwhqa.framework.db;

import com.dwhqa.framework.reporting.TestListener;
import com.dwhqa.framework.util.DbUtils;
import com.dwhqa.framework.util.ExcelUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Map;

@Listeners({ TestListener.class })
public class DatabaseTests {

    @BeforeSuite(alwaysRun = true)
    public void initDb() {
        DbUtils.runSqlResource("sql/schema.sql");
        DbUtils.runSqlResource("sql/data.sql");
    }

    @Test(groups = {"db","smoke","inventory","product"})
    public void runDbCases() throws Exception {
        List<Map<String, String>> cases = ExcelUtils.readSheetFromClasspath("data/db-cases.xlsx", "cases");
        for (Map<String,String> c : cases) {
            String id = c.get("TestId");
            String testSql = c.get("TestSQL");
            String expType = c.get("ExpResultType");
            String expResult = c.get("ExpResult");

            TestListener.current().info("DB Case " + id + " executing: " + testSql);

            try (Statement st = DbUtils.get().createStatement()) {
                boolean hasResult = st.execute(testSql);
                if ("scalar".equalsIgnoreCase(expType)) {
                    Assert.assertTrue(hasResult, "Expected a scalar result for " + id);
                    try (ResultSet rs = st.getResultSet()) {
                        rs.next();
                        long value = rs.getLong(1);

                        String exp = expResult.replaceAll("\"", "").trim();

                        if (exp.startsWith("=")) {
                            // format "= 0"
                            String[] parts = exp.split("\\s+");
                            long expected = Long.parseLong(parts[1]);
                            Assert.assertEquals(value, expected, "Scalar mismatch for " + id);
                        } else {
                            // plain number format "0"
                            long expected = Long.parseLong(exp);
                            Assert.assertEquals(value, expected, "Scalar mismatch for " + id);
                        }
                    }
                } else if ("rows".equalsIgnoreCase(expType)) {
                    Assert.assertTrue(hasResult, "Expected a rowset for " + id);
                    try (ResultSet rs = st.getResultSet()) {
                        boolean any = rs.next();
                        if (any) {
                            Files.createDirectories(Paths.get("artifacts/db"));
                            String out = "artifacts/db/" + id + "-diff.csv";
                            try (FileWriter fw = new FileWriter(out)) {
                                int cols = rs.getMetaData().getColumnCount();
                                for (int i=1;i<=cols;i++){ fw.write(rs.getMetaData().getColumnName(i)); if (i<cols) fw.write(","); }
                                fw.write("\n");
                                do {
                                    for (int i=1;i<=cols;i++) { Object v = rs.getObject(i); fw.write(v==null?"":String.valueOf(v)); if (i<cols) fw.write(","); }
                                    fw.write("\n");
                                } while (rs.next());
                            }
                            Assert.fail("Expected no rows, but found mismatches. See " + out);
                        }
                    }
                } else {
                    Assert.fail("Unknown ExpResultType: " + expType);
                }
            }
        }
    }
}
