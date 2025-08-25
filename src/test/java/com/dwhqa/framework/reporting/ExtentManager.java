package com.dwhqa.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.io.File;

public class ExtentManager {
    private static ExtentReports extent;
    private static final String REPORT_DIR  = "reports";
    private static final String REPORT_PATH = REPORT_DIR + "/extent-report.html";

    public synchronized static ExtentReports getReporter() {
        if (extent == null) {
            new File(REPORT_DIR).mkdirs();
            ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
            spark.config().setDocumentTitle("DWH QA Suite");
            spark.config().setReportName("API / DB / ETL Automation");
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }
}
