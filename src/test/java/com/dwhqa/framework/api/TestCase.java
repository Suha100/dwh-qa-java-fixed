package com.dwhqa.framework.api;
public class TestCase {
    private final String testId, group, method, endpoint, pathParams, queryParams, headers, bodyFile, expStatus, expJsonPathAsserts, expSchema, baseUri;
    public TestCase(String baseUri, String testId, String group, String method, String endpoint, String pathParams, String queryParams, String headers, String bodyFile, String expStatus, String expJsonPathAsserts, String expSchema) {
        this.baseUri=baseUri; this.testId=testId; this.group=group; this.method=method; this.endpoint=endpoint; this.pathParams=pathParams; this.queryParams=queryParams; this.headers=headers; this.bodyFile=bodyFile; this.expStatus=expStatus; this.expJsonPathAsserts=expJsonPathAsserts; this.expSchema=expSchema;
    }
    public String baseUri(){return baseUri;} public String testId(){return testId;} public String group(){return group;}
    public String method(){return method;} public String endpoint(){return endpoint;} public String pathParams(){return pathParams;}
    public String queryParams(){return queryParams;} public String headers(){return headers;} public String bodyFile(){return bodyFile;}
    public int expStatus(){return Integer.parseInt(expStatus);} public String expJsonPathAsserts(){return expJsonPathAsserts;} public String expSchema(){return expSchema;}
}
