package com.example.ecommerce.inventory_service.service;

import com.example.ecommerce.inventory_service.dto.OrderRequestDto;
import com.example.ecommerce.inventory_service.dto.ProductDto;

import java.util.List;

public interface ProductService {

    List<ProductDto> getALlInventory();

    ProductDto getProductById(Long id);

    Double reduceStocks(OrderRequestDto orderRequestDto);

    void addStocks(OrderRequestDto orderRequestDto);
}
