package org.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private static final String TOPIC_NAME = "wikimedia_topic";
    private static final String STREAM_URL = "https://stream.wikimedia.org/v2/stream/recentchange";

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    public Producer() {
        this.producer = createKafkaProducer();
        this.objectMapper = new ObjectMapper();

        // Register shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down producer...");
            shutdown();
        }));
    }

    private KafkaProducer<String, String> createKafkaProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
        properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768); // 32 KB
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 100);    // 100 ms
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        return new KafkaProducer<>(properties);
    }

    public void start() {
        while (true) { // Infinite loop for reconnecting
            try {
                logger.info("Connecting to Wikimedia stream...");
                URL url = new URL(STREAM_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(60000);   // 60 seconds

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6); // Skip "data: " prefix
                            processData(data);
                        }
                    }
                }

                logger.warn("Stream ended, reconnecting...");
            } catch (Exception e) {
                logger.error("Error in stream processing: {}", e.getMessage());
                try {
                    Thread.sleep(5000); // Wait 5 seconds before retrying
                } catch (InterruptedException interruptedException) {
                    logger.error("Interrupted during retry sleep: {}", interruptedException.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processData(String eventData) {
        try {
            ObjectNode jsonNode = objectMapper.readValue(eventData, ObjectNode.class);

            String data = objectMapper.writeValueAsString(jsonNode);

            logger.info(data);

            producer.send(new ProducerRecord<>(TOPIC_NAME, data),
                    (metadata, exception) -> {
                        if (exception != null) {
                            logger.error("Message failed delivery: {}", exception.getMessage());
                        } else {
                            String truncatedData = data.length() > 100 ? data.substring(0, 100) + "..." : data;
                            logger.info("Produced event to topic {}: value = {}", TOPIC_NAME, truncatedData);
                        }
                    });
        } catch (Exception e) {
            logger.error("Error processing data: {}", e.getMessage());
        }
    }

    public void shutdown() {
        if (producer != null) {
            producer.close();
            logger.info("Kafka producer closed.");
        }
    }

    public static void main(String[] args) {
        Producer producer = new Producer();
        try {
            producer.start();
        } finally {
            producer.shutdown();
        }
    }
}
