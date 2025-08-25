package com.dwhqa.framework.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.util.*;

public class ExcelUtils {
    public static List<Map<String, String>> readSheetFromClasspath(String resourcePath, String sheetName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);
            Iterator<Row> it = sheet.iterator();
            if (!it.hasNext()) return Collections.emptyList();
            Row header = it.next();
            List<String> headers = new ArrayList<>();
            for (Cell c : header) headers.add(c.getStringCellValue().trim());
            DataFormatter fmt = new DataFormatter();
            List<Map<String, String>> rows = new ArrayList<>();
            while (it.hasNext()) {
                Row r = it.next();
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell c = r.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = fmt.formatCellValue(c).trim();
                    map.put(headers.get(i), value);
                }
                boolean allBlank = map.values().stream().allMatch(v -> v == null || v.isEmpty());
                if (!allBlank) rows.add(map);
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel " + resourcePath + ": " + e.getMessage(), e);
        }
    }
}
