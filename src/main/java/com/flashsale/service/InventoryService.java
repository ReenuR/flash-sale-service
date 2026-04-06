package com.flashsale.service;

import com.flashsale.config.RedisKeys;
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
        System.out.println("=== initializeStock called: " + saleId + " quantity: " + quantity);
        try{
            redisTemplate.opsForValue().set("inventory:"+saleId, String.valueOf(quantity));
            System.out.println("=== Redis set successful");
        } catch (Exception e) {
            System.out.println("=== Redis error: " + e.getMessage());
            throw e;
        }

    }
    public boolean reserveItem(String saleId) {
        String key = RedisKeys.inventoryKey(saleId);
        System.out.println("=== reserveItem key: " + key);
        Long stock = redisTemplate.opsForValue().decrement(key);
        System.out.println("=== stock after decrement: " + stock);

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
