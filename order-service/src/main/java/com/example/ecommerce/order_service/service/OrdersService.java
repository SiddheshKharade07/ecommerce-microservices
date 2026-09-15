package com.example.ecommerce.order_service.service;

import com.example.ecommerce.order_service.dto.OrderRequestDto;
import com.example.ecommerce.order_service.dto.ShipmentRecordDto;

import java.util.List;

public interface OrdersService {

    List<OrderRequestDto> getAllOrders();

    OrderRequestDto getOrderById(Long id);

    OrderRequestDto createOrder(OrderRequestDto orderRequestDto);

    String cancelOrder(Long id);

    ShipmentRecordDto getShipmentStatus(Long orderId);
}
