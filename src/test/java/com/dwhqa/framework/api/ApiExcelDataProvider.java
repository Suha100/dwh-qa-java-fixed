package com.dwhqa.framework.api;

import com.dwhqa.framework.util.ExcelUtils;
import org.testng.annotations.DataProvider;
import java.util.List;
import java.util.Map;

public class ApiExcelDataProvider {
    @DataProvider(name = "apiData")
    public static Object[][] apiData() {
        List<Map<String, String>> rows = ExcelUtils.readSheetFromClasspath("data/api-cases.xlsx", "cases");
        Object[][] out = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            Map<String,String> r = rows.get(i);
            out[i][0] = new TestCase(
                r.getOrDefault("BaseURI",""),
                r.getOrDefault("TestId",""),
                r.getOrDefault("Group",""),
                r.getOrDefault("Method","GET"),
                r.getOrDefault("Endpoint","/"),
                r.getOrDefault("PathParams","{}"),
                r.getOrDefault("QueryParams","{}"),
                r.getOrDefault("Headers","{}"),
                r.getOrDefault("BodyFile",""),
                r.getOrDefault("ExpStatus","200"),
                r.getOrDefault("ExpJsonPathAsserts","{}"),
                r.getOrDefault("ExpSchema","")
            );
        }
        return out;
    }
}
