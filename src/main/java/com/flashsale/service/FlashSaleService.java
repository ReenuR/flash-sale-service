package com.flashsale.service;

import com.flashsale.entity.Order;
import com.flashsale.entity.OrderStatus;
import com.flashsale.exception.RateLimitExceededException;
import com.flashsale.exception.SoldOutException;
import com.flashsale.kafka.OrderEventProducer;
import com.flashsale.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FlashSaleService {

    private final RateLimiterService rateLimiterService;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;


    public FlashSaleService(RateLimiterService rateLimiterService, InventoryService inventoryService, OrderRepository orderRepository, OrderEventProducer producer) {
        this.rateLimiterService = rateLimiterService;
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
        this.orderEventProducer = producer;
    }

    public void buyItem(String userId, String saleId, String productId){
        // Check rate limiter
        if(!rateLimiterService.isAllowed(userId))
            throw new RateLimitExceededException("Rate limit exceeded");

        //Check inventory
        if(!inventoryService.reserveItem(saleId))
            throw new SoldOutException("Sold out!");

        Order order = Order.builder()
                .userId(userId)
                .saleId(saleId)
                .productId(productId)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        //Save order
        orderRepository.save(order);

        //Publish Kafka event
        orderEventProducer.publishOrderCreated(order);
    }

    public void initializeStock(String saleId, int quantity) {
        try{
            inventoryService.initializeStock(saleId, quantity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public int getStock(String saleId) {
        return inventoryService.getStock(saleId);
    }


}
