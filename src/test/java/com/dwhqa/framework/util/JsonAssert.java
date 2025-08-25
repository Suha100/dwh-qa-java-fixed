package com.dwhqa.framework.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.testng.Assert;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.*;

public class JsonAssert {
    private static final Pattern IN_PATTERN =
            Pattern.compile("^IN\\[(.*)]$");
    private static final Pattern GE_PATTERN = Pattern.compile("^>=\s*(.*)$");
    private static final Pattern LE_PATTERN = Pattern.compile("^<=\s*(.*)$");
    private static final Pattern EQ_PATTERN = Pattern.compile("^=\s*(.*)$");
    private static final Pattern EXPR_SUB_PATTERN =
            Pattern.compile("^=\\(\\s*(\\$\\.[^\\+\\-\\s]+)\\s*([\\+\\-])\\s*(\\$\\.[^\\)\\s]+)\\s*\\)$");

    public static void assertPaths(String json, Map<String, String> expectations) {
        DocumentContext ctx = JsonPath.parse(json);
        for (Map.Entry<String, String> e : expectations.entrySet()) {
            String path = e.getKey();
            String expect = e.getValue();
            Object val = ctx.read(path);
            String sval = String.valueOf(val);
            if (expect.equalsIgnoreCase("NOT_NULL")) { Assert.assertNotNull(val, "NOT_NULL " + path); continue; }
            Matcher inM = IN_PATTERN.matcher(expect);
            Matcher geM = GE_PATTERN.matcher(expect);
            Matcher leM = LE_PATTERN.matcher(expect);
            Matcher eqM = EQ_PATTERN.matcher(expect);
            Matcher exprM = EXPR_SUB_PATTERN.matcher(expect);
            if (inM.matches()) {
                String[] options = inM.group(1).split("\s*,\s*");
                boolean ok = Arrays.stream(options).anyMatch(opt -> opt.equalsIgnoreCase(sval));
                Assert.assertTrue(ok, "IN failed " + path + " -> " + sval);
            } else if (geM.matches()) {
                double thr = Double.parseDouble(geM.group(1));
                double actual = Double.parseDouble(sval);
                Assert.assertTrue(actual >= thr, ">= failed " + path);
            } else if (leM.matches()) {
                double thr = Double.parseDouble(leM.group(1));
                double actual = Double.parseDouble(sval);
                Assert.assertTrue(actual <= thr, "<= failed " + path);
            } else if (expect.equalsIgnoreCase("ISODATETIME")) {
                try { OffsetDateTime.parse(sval); } catch (Exception ex) { Assert.fail("ISODATETIME failed " + path + " got " + sval); }
            } else if (exprM.matches()) {
                String p1 = exprM.group(1), op = exprM.group(2), p2 = exprM.group(3);
                double v1 = toNum(ctx.read(p1)), v2 = toNum(ctx.read(p2)), actual = toNum(val);
                double calc = op.equals("+") ? v1 + v2 : v1 - v2;
                Assert.assertEquals(actual, calc, "Expr failed at " + path);
            } else if (eqM.matches()) {
                String expVal = eqM.group(1);
                try { Assert.assertEquals(Double.parseDouble(sval), Double.parseDouble(expVal), "Numeric = failed " + path); }
                catch (NumberFormatException nfe) { Assert.assertEquals(sval, expVal, "String = failed " + path); }
            } else {
                Assert.assertEquals(sval, expect, "Equality failed " + path);
            }
        }
    }
    private static double toNum(Object o){ if (o instanceof Number) return ((Number)o).doubleValue(); return Double.parseDouble(String.valueOf(o)); }
}
