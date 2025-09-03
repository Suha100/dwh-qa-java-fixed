package com.dwhqa.framework.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockSupport {
    private static WireMockServer server;

    // Start WireMock with stubs
    public static void start() {
        if (server != null && server.isRunning()) {
            return; // already started
        }

        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();

        WireMock.configureFor("localhost", server.port());
        server.stubFor(get(urlEqualTo("/v1/products/SKU-1001")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"sku\":\"SKU-1001\",\"name\":\"Widget\",\"brand\":\"Acme\",\"category\":\"Gadgets\",\"price\":19.99,\"currency\":\"USD\",\"status\":\"ACTIVE\",\"updatedAt\":\"2025-08-20T12:00:00Z\"}")));

        server.stubFor(patch(urlEqualTo("/v1/products/SKU-1002")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"sku\":\"SKU-1002\",\"price\":19.99,\"currency\":\"USD\",\"status\":\"ACTIVE\",\"updatedAt\":\"2025-08-22T10:00:00Z\"}")));

        server.stubFor(get(urlPathEqualTo("/v1/inventory/SKU-1001"))
                .withQueryParam("locationId", equalTo("SEA-01"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"sku\":\"SKU-1001\",\"locationId\":\"SEA-01\",\"onHand\":20,\"reserved\":5,\"available\":15,\"safetyStock\":10,\"updatedAt\":\"2025-08-22T09:00:00Z\"}")));

        server.stubFor(post(urlEqualTo("/v1/inventory/adjust")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"preAdjust\":{\"available\":15},\"postAdjust\":{\"available\":10}}")));
    }

    // Stop WireMock
    public static void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    // Get base URI to point your API tests to WireMock
    public static String baseUri() {
        if (server == null || !server.isRunning()) {
            throw new IllegalStateException("WireMock not started");
        }
        return "http://localhost:" + server.port();
    }
}
