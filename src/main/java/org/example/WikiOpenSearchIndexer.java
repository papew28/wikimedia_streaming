package org.example;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;

import java.io.IOException;

public class WikiOpenSearchIndexer {

    private final OpenSearchClient client;

    public WikiOpenSearchIndexer(OpenSearchClient client) {
        this.client = client;
    }

    public void indexData(String indexName, String id, WikiData jsonData) throws IOException {
        IndexRequest<WikiData> indexRequest = new IndexRequest.Builder<WikiData>().index(indexName).id(id).document(jsonData).build();
        client.index(indexRequest);
  }
}