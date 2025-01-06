package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.PutIndicesSettingsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Properties;
import java.io.IOException;

public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private static boolean doesIndexExist(OpenSearchClient client, String indexName) throws IOException {
        try {
            client.indices().get(b -> b.index(indexName));
            return true;
        } catch (Exception e) {
            //log.error("Error while searching index", e);
            return false;
        }
    }

    private static void createIndex(OpenSearchClient client, String indexName) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(indexName).build();
        client.indices().create(createIndexRequest);

        IndexSettings indexSettings = new IndexSettings.Builder().autoExpandReplicas("0-all").build();
        PutIndicesSettingsRequest putIndicesSettingsRequest = new PutIndicesSettingsRequest.Builder().index(indexName).settings(indexSettings).build();
        client.indices().putSettings(putIndicesSettingsRequest);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OpenSearchClients clients = WikiOpenSearchClient.createClient("admin","Pwc22129.@");
        RestClient restClient = clients.getRestClient();
        OpenSearchClient client= clients.getOpenSearchClient();
        WikiOpenSearchIndexer indexer = new WikiOpenSearchIndexer(client);

        String bootstrapServers = "localhost:9092";
        String groupId = "wiki.data";
        String topic = "wikimedia_topic";
        String data_index="wiki-data-index";

        boolean exists = doesIndexExist(client, data_index);
        if (!exists) {
            createIndex(client, data_index);
        }

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);

                for (ConsumerRecord<String, String> record : records) {
                    try {
                        logger.info(record.value());
                        WikiData wikiData = objectMapper.readValue(record.value(), WikiData.class);

                        indexer.indexData("wiki-data-index", String.valueOf(wikiData.getId()), wikiData);
                        String jsonData = objectMapper.writeValueAsString(wikiData);
                        logger.info("Data indexed: {}", jsonData);
                    } catch (Exception e) {
                        logger.error("Erreur lors de la désérialisation ou l'indexation : ", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur rencontrée : ", e);
        } finally {
            consumer.close();
            try {
                restClient.close();
            } catch (IOException e) {
                logger.error("Erreur lors de la fermeture du client OpenSearch : ", e);
            }
        }
    }


}
