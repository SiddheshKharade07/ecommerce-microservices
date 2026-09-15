package com.example.ecommerce.order_service.controller;

import com.example.ecommerce.order_service.dto.OrderRequestDto;
import com.example.ecommerce.order_service.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/core")
public class OrdersController {

    private final OrdersService ordersService;

    @GetMapping("/helloOrders")
    public String helloOrders() {
        return "Hello From Order Service";
    }

    @PostMapping("/create-order")
    public ResponseEntity<OrderRequestDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        OrderRequestDto orderRequestDtoResponse = ordersService.createOrder(orderRequestDto);
        return ResponseEntity.ok(orderRequestDtoResponse);
    }

    @PutMapping("/cancel-order/{id}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {
        String response = ordersService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderRequestDto>> getAllOrders() {
        log.info("Fetching all orders via controller");
        List<OrderRequestDto> orders = ordersService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderRequestDto> getOrderById(@PathVariable Long id) {
        log.info("Fetching order with ID: {} via controller", id);
        OrderRequestDto order = ordersService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
}
