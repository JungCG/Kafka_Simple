package com.tacademy;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class SimpleProducer {
    // TOPIC_NAME
    private static String TOPIC_NAME = "test";
    // BOOTSTRAP_SERVERS : Kafka Cluster IP = AWS EC2 Public IP
    private static String BOOTSTRAP_SERVERS = "{AWS EC2 Public IP}}:9092";

    public static void main(String[] args) {
        Properties configs = new Properties();
        // BOOTSTRAP_SERVERS
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // KEY_SERIALIZER
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // VALUE_SERIALIZER
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

        for (int index = 0; index < 10; index++) {
            String data = "This is record " + index;

            // ProducerRecord<>(Topic, Value)
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, data);
            try {
                producer.send(record);
                System.out.println("Send to " + TOPIC_NAME + " | data : " + data);
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}