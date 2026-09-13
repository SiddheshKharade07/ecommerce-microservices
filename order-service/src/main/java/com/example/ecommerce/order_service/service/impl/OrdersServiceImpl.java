package com.example.ecommerce.order_service.service.impl;

import com.example.ecommerce.order_service.dto.OrderRequestDto;
import com.example.ecommerce.order_service.entity.Orders;
import com.example.ecommerce.order_service.repository.OrdersRepository;
import com.example.ecommerce.order_service.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<OrderRequestDto> getAllOrders() {
        log.info("Fetching all orders");
        List<Orders> orders = ordersRepository.findAll();
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderRequestDto.class))
                .toList();
    }

    @Override
    public OrderRequestDto getOrderById(Long id) {
        log.info("Fetching order with ID: {}", id);
        Orders order = ordersRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        return modelMapper.map(order, OrderRequestDto.class);
    }
}
