package com.flashsale.kafka;


import com.flashsale.entity.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(Order order){
        try{
            kafkaTemplate.send("order-created", order.getId(), objectMapper.writeValueAsString(order));
        }catch (Exception e){
            throw new RuntimeException("Failed to publish order event", e);
        }
    }
}
