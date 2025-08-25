package com.dwhqa.framework.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.testng.Assert;

import java.io.InputStream;

public class SchemaUtils {
    private static final ObjectMapper M = new ObjectMapper();

    public static void validate(String json, String schemaResource) {
        if (schemaResource == null || schemaResource.isEmpty()) return;
        try (InputStream schemaStream =
                     Thread.currentThread().getContextClassLoader()
                             .getResourceAsStream("schemas/" + schemaResource)) {

            if (schemaStream == null) {
                throw new RuntimeException("Schema not found: " + schemaResource);
            }

            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            JsonSchema schema = factory.getSchema(schemaStream);
            JsonNode node = M.readTree(json);

            var errors = schema.validate(node);
            Assert.assertTrue(errors.isEmpty(), "Schema errors: " + errors);

        } catch (Exception e) {
            throw new RuntimeException("Schema validation failed: " + e.getMessage(), e);
        }
    }
}
