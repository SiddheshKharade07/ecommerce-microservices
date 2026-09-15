package com.example.ecommerce.inventory_service.service.impl;

import com.example.ecommerce.inventory_service.dto.OrderRequestDto;
import com.example.ecommerce.inventory_service.dto.OrderRequestItemDto;
import com.example.ecommerce.inventory_service.dto.ProductDto;
import com.example.ecommerce.inventory_service.entity.Product;
import com.example.ecommerce.inventory_service.repository.ProductRepository;
import com.example.ecommerce.inventory_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ProductDto> getALlInventory() {
        log.info("Fetching all inventory items");
        List<Product> inventories = productRepository.findAll();
        return inventories.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .toList();
    }

    @Override
    public ProductDto getProductById(Long id) {
        log.info("Fetching Product with ID: {}", id);
        Optional<Product> inventory = productRepository.findById(id);
        return inventory.map(item -> modelMapper.map(item, ProductDto.class))
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
    }

    @Override
    @Transactional
    public Double reduceStocks(OrderRequestDto orderRequestDto) {
        log.info("Reducing the stocks");
        Double totalPrice = 0.0;
        for (OrderRequestItemDto orderRequestItemDtoDto : orderRequestDto.getItems()) {
            Long productId = orderRequestItemDtoDto.getProductId();
            Integer quantity = orderRequestItemDtoDto.getQuantity();

            Product product = productRepository.findById(productId).orElseThrow(() ->
                    new RuntimeException("product not found with ID: " + productId));

            if (product.getStock() < quantity) {
                throw new RuntimeException("Product cannot be fulfilled for given quantity");
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            totalPrice += quantity * product.getPrice();
        }

        return totalPrice;
    }

    @Override
    @Transactional
    public void addStocks(OrderRequestDto orderRequestDto) {
        log.info("Adding the stocks");
        for (OrderRequestItemDto orderRequestItemDto : orderRequestDto.getItems()) {
            Product product = productRepository.findById(orderRequestItemDto.getProductId()).orElseThrow(() ->
                    new RuntimeException("Product not found with ID: " + orderRequestItemDto.getProductId()));
            product.setStock(product.getStock() + orderRequestItemDto.getQuantity());

            productRepository.save(product);
        }

        log.info("Stocks added successfully");
    }
}
