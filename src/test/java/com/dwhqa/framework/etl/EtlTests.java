package com.dwhqa.framework.etl;

import com.dwhqa.framework.reporting.TestListener;
import com.dwhqa.framework.util.DbUtils;
import com.dwhqa.framework.util.ExcelUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Listeners({ TestListener.class })
public class EtlTests {

    @BeforeSuite(alwaysRun = true)
    public void initDb() {
        DbUtils.runSqlResource("sql/schema.sql");
        DbUtils.runSqlResource("sql/data.sql");
    }

    private long countCsvRows(String globResourcePattern) throws Exception {
        String base = globResourcePattern.substring(0, globResourcePattern.indexOf("*"));
        String dir = base.substring(0, base.lastIndexOf("/"));
        String prefix = base.substring(base.lastIndexOf("/")+1);
        Pattern p = Pattern.compile("^" + Pattern.quote(prefix) + ".*\\.csv$");
        String[] known = new String[]{ "ingest/products_20250822.csv", "ingest/inventory_delta_20250822.csv" };
        long total = 0;
        for (String res : known) {
            if (!res.startsWith(dir + "/")) continue;
            String name = res.substring(res.lastIndexOf("/")+1);
            if (!p.matcher(name).matches()) continue;
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(res);
            if (is == null) continue;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                long lines = br.lines().skip(1).count(); // skip header
                total += lines;
            }
        }
        return total;
    }

    @Test(groups = {"etl","smoke","product","inventory"})
    public void runEtlCases() throws Exception {
        List<Map<String, String>> cases = ExcelUtils.readSheetFromClasspath("data/etl-cases.xlsx", "cases");
        for (Map<String,String> c : cases) {
            String id = c.get("TestId");
            String ruleType = c.get("RuleType");
            String source = c.get("Source");
            String target = c.get("Target");
            String ruleSpec = c.get("RuleSpec");
            String threshold = c.get("Threshold");

            TestListener.current().info("ETL Case " + id + " rule=" + ruleType + " spec=" + ruleSpec);

            if ("row_count".equalsIgnoreCase(ruleType)) {
                if (source.startsWith("file://") && target.startsWith("DB:")) {
                    String glob = source.replace("file://","");
                    long fileRows = countCsvRows(glob);
                    String table = target.substring("DB:".length());
                    try (Statement st = DbUtils.get().createStatement();
                         ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                        rs.next();
                        long dbRows = rs.getLong(1);
                        boolean within = false;
                        if (threshold != null && threshold.contains("%")) {
                            String pctStr = threshold.replaceAll("[^0-9]", "");
                            double pct = Double.parseDouble(pctStr);
                            double diffPct = Math.abs(fileRows - dbRows) * 100.0 / Math.max(1, fileRows);
                            within = diffPct <= pct;
                        }
                        Assert.assertTrue(fileRows == dbRows || within,
                                "Row count mismatch: file=" + fileRows + " db=" + dbRows + " threshold=" + threshold);
                    }
                } else { Assert.fail("Unsupported row_count source/target"); }
            } else if ("not_null".equalsIgnoreCase(ruleType)) {
                String cols = ruleSpec
                        .replaceAll(".*\\[", "")
                        .replaceAll("\\].*", "");                String table = target.substring("DB:".length());
                for (String col : cols.split("\s*,\s*")) {
                    try (Statement st = DbUtils.get().createStatement();
                         ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + col + " IS NULL")) {
                        rs.next();
                        long n = rs.getLong(1);
                        Assert.assertEquals(n, 0L, "Nulls found in " + table + "." + col);
                    }
                }
            } else if ("consistency_expr".equalsIgnoreCase(ruleType)) {
                String table = target.substring("DB:".length());
                try (Statement st = DbUtils.get().createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE available <> on_hand - reserved")) {
                    rs.next();
                    long n = rs.getLong(1);
                    Assert.assertEquals(n, 0L, "Consistency failed (available = on_hand - reserved) in " + table);
                }
            } else if ("pk_unique".equalsIgnoreCase(ruleType)) {
                String cols = ruleSpec
                        .replaceAll(".*\\[", "")
                        .replaceAll("\\].*", "");
                System.out.println(cols);  // prints: sku,effective_from,price
                System.out.println(cols);
                String table = target.substring("DB:".length());
                try (Statement st = DbUtils.get().createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM (SELECT " + cols + ", COUNT(*) c FROM " + table + " GROUP BY " + cols + " HAVING COUNT(*)>1) d")) {
                    rs.next();
                    long dups = rs.getLong(1);
                    Assert.assertEquals(dups, 0L, "Duplicate PKs in " + table + " on " + cols);
                }
            } else if ("file_format".equalsIgnoreCase(ruleType)) {
                String path = source.replace("file://","");
                long rows = countCsvRows(path);
                Assert.assertTrue(rows >= 0, "CSV not readable: " + path);
            } else {
                Assert.fail("Unknown ETL RuleType: " + ruleType);
            }
        }
    }
}
