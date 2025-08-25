package com.dwhqa.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import org.testng.*;

public class TestListener implements ITestListener {
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();
    private static final ExtentReports EXTENT = ExtentManager.getReporter();
    public static ExtentTest current() { return TEST.get(); }

    @Override public void onStart(ITestContext context) { System.out.println("[Extent] start " + context.getName()); }
    @Override public void onFinish(ITestContext context) { EXTENT.flush(); }
    @Override public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        ExtentTest t = EXTENT.createTest(name).assignCategory(String.join(",", result.getMethod().getGroups()));
        TEST.set(t);
        t.info("Starting " + name);
    }
    @Override public void onTestSuccess(ITestResult result) { if (TEST.get()!=null) TEST.get().pass("Passed"); }
    @Override public void onTestFailure(ITestResult result) { if (TEST.get()!=null) TEST.get().fail(result.getThrowable()); }
    @Override public void onTestSkipped(ITestResult result) { if (TEST.get()!=null) TEST.get().skip("Skipped"); }
}
