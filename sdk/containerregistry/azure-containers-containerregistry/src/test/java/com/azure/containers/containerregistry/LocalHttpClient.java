// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.
package com.azure.containers.containerregistry;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.util.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;

public class LocalHttpClient implements HttpClient {

    private final String playbackRecordFile;

    /**
     * Creates an instance of the local HttpClient.
     * @param playbackFile the playbackfile name that has the response.
     */
    public LocalHttpClient(String playbackFile) {
        this.playbackRecordFile = playbackFile;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest) {
        return Mono.empty();
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // convert JSON array to list of books
            RecordedData recordedData = mapper.readValue(Paths.get("src", "test", "resources", "session-records", playbackRecordFile).toFile(), RecordedData.class);
            NetworkCallRecord callRecord = recordedData.findFirstAndRemoveNetworkCall(record ->
                record.getUri().contains(request.getUrl().getPath())
                    && (request.getUrl().getQuery() == null || record.getUri().contains(request.getUrl().getQuery()))
                    && request.getHttpMethod().toString().equalsIgnoreCase(record.getMethod()));

            Map<String, String> response = callRecord.getResponse();
            int statusCode = Integer.parseInt(response.get("StatusCode"));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Link", response.get("Link"));
            byte[] body = response.get("Body") != null ? response.get("Body").getBytes(StandardCharsets.UTF_8) : new byte[0];
            return Mono.just(new MockHttpResponse(request, statusCode, headers, body));
        }
        catch (IOException exception)
        {
            return Mono.error(new HttpResponseException(new MockHttpResponse(request, 404)));
        }
    }


}
