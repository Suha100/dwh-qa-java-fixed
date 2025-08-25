package com.dwhqa.framework.api;

import com.dwhqa.framework.reporting.TestListener;
import com.dwhqa.framework.util.JsonAssert;
import com.dwhqa.framework.util.SchemaUtils;
import com.dwhqa.framework.util.WireMockSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

@Listeners({ TestListener.class })
public class ApiTests {

    @BeforeSuite(alwaysRun = true)
    public void startWireMock() {
        WireMockSupport.start();
    }

    @AfterSuite(alwaysRun = true)
    public void stopWireMock() {
        WireMockSupport.stop();
    }

    private final ObjectMapper M = new ObjectMapper();

    @Test(dataProvider = "apiData", dataProviderClass = ApiExcelDataProvider.class, groups = {"api","smoke","product","inventory"})
    public void runApi(TestCase tc) throws Exception {
        String baseUri = tc.baseUri();
        if (baseUri == null || baseUri.isEmpty() || baseUri.equalsIgnoreCase("wiremock")) baseUri = WireMockSupport.baseUri();
        RestAssured.baseURI = baseUri;
        var req = given().relaxedHTTPSValidation();

        Map<String, Object> pathParams = M.readValue(tc.pathParams(), Map.class);
        if (!pathParams.isEmpty()) req.pathParams(pathParams);

        Map<String, Object> queryParams = M.readValue(tc.queryParams(), Map.class);
        if (!queryParams.isEmpty()) req.queryParams(queryParams);

        Map<String, String> headers = M.readValue(tc.headers(), Map.class);
        if (!headers.isEmpty()) req.headers(headers);

        if (tc.bodyFile() != null && !tc.bodyFile().isEmpty()) {
            String res = "bodies/" + tc.bodyFile();
            var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(res);
            if (is == null) throw new RuntimeException("Payload resource not found on classpath: " + res);
            String body = new String(is.readAllBytes());
            req.body(body).header("Content-Type","application/json; charset=UTF-8");
        }

        TestListener.current().info("Request: " + tc.method() + " " + baseUri + tc.endpoint());
        Response res;
        switch (tc.method().toUpperCase()) {
            case "POST": res = req.post(tc.endpoint()); break;
            case "PUT": res = req.put(tc.endpoint()); break;
            case "PATCH": res = req.patch(tc.endpoint()); break;
            case "DELETE": res = req.delete(tc.endpoint()); break;
            default: res = req.get(tc.endpoint());
        }
        TestListener.current().info("Status: " + res.statusCode());
        TestListener.current().info("Body: " + res.asString());

        Assert.assertEquals(res.statusCode(), tc.expStatus(), "Status code mismatch");
        Map<String,String> asserts = M.readValue(tc.expJsonPathAsserts(), Map.class);
        JsonAssert.assertPaths(res.asString(), asserts);
        SchemaUtils.validate(res.asString(), tc.expSchema());
    }
}
