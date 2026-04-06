package com.flashsale.controller;

import com.flashsale.dto.BuyRequest;
import com.flashsale.service.FlashSaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flash-sale")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    public FlashSaleController(FlashSaleService flashSaleService) {
        this.flashSaleService = flashSaleService;
    }

    @PostMapping("/{saleId}/buy")
    public ResponseEntity<String> buyItem(@PathVariable String saleId, @RequestBody BuyRequest buyRequestDTO){
        System.out.println("=== Buy controller hit: " + saleId);
        flashSaleService.buyItem(buyRequestDTO.getUserId(), saleId, buyRequestDTO.getProductId());
        return ResponseEntity.ok("Order placed successfully");
    }

    @PostMapping("/{saleId}/init")
    public ResponseEntity<String> initializeStock(@PathVariable String saleId, @RequestParam int quantity){
        flashSaleService.initializeStock(saleId, quantity);
        return ResponseEntity.ok("Stock initialized");
    }

    @GetMapping("/{saleId}/stock")
    public ResponseEntity<Integer> checkStock(@PathVariable String saleId){
        int stock  = flashSaleService.getStock(saleId);
        return ResponseEntity.ok(stock);
    }
}

/*
POST/api/flash-sale/{saleId}/buy   ---> Buy item
POST/api/flash-sale/{saleId}/init   --->Initialize stock
GET/api/flash-sale/{saleId}/stock --> check Stock
* */