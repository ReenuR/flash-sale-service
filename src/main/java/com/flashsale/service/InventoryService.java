package com.flashsale.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final RedisTemplate<String, String> redisTemplate;

    public InventoryService( RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    // — quantity comes from the caller
    public void initializeStock(String saleId, int quantity){
        redisTemplate.opsForValue().set("inventory:"+saleId, String.valueOf(quantity));
    }
    public boolean reserveItem(String saleId) {
        String key = "inventory:" + saleId;
        Long stock = redisTemplate.opsForValue().decrement(key);

        if (stock == null || stock < 0) {
            redisTemplate.opsForValue().increment(key); // undo
            return false;
        }
        return true;
    }

    public int getStock(String saleId){
        String value = redisTemplate.opsForValue().get("inventory:" + saleId);
        return value != null ? Integer.parseInt(value) : 0;
    }

}
