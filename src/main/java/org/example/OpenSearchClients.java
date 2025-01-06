package org.example;

import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;

public class OpenSearchClients {
    private final OpenSearchClient openSearchClient;
    private final RestClient restClient;

    public OpenSearchClients(OpenSearchClient openSearchClient, RestClient restClient) {
        this.openSearchClient = openSearchClient;
        this.restClient = restClient;
    }

    public OpenSearchClient getOpenSearchClient() {
        return openSearchClient;
    }

    public RestClient getRestClient() {
        return restClient;
    }
}
