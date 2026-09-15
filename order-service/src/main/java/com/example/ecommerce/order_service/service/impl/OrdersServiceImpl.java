package com.example.ecommerce.order_service.service.impl;

import com.example.ecommerce.order_service.clients.InventoryFeignClient;
import com.example.ecommerce.order_service.dto.OrderRequestDto;
import com.example.ecommerce.order_service.entity.OrderItem;
import com.example.ecommerce.order_service.entity.OrderStatus;
import com.example.ecommerce.order_service.entity.Orders;
import com.example.ecommerce.order_service.repository.OrdersRepository;
import com.example.ecommerce.order_service.service.OrdersService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepository;
    private final ModelMapper modelMapper;
    private final InventoryFeignClient inventoryFeignClient;

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

    @Override
    @CircuitBreaker(name = "inventoryCircuitBreaker", fallbackMethod = "createOrderFallBack")
    public OrderRequestDto createOrder(OrderRequestDto orderRequestDto) {
        log.info("Calling the createOrder method");
        Double totalPrice = inventoryFeignClient.reduceStocks(orderRequestDto).getBody();

        Orders orders = modelMapper.map(orderRequestDto, Orders.class);
        for (OrderItem orderItem : orders.getItems()) {
            orderItem.setOrder(orders);
        }

        orders.setTotalPrice(totalPrice);
        orders.setOrderStatus(OrderStatus.CONFIRMED);

        Orders savedOrder = ordersRepository.save(orders);

        return modelMapper.map(orders, OrderRequestDto.class);
    }

    @Override
    @Transactional
    public String cancelOrder(Long id) {
        log.info("Cancelling order");
        Orders order = ordersRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Order not found with ID: " + id));
        OrderRequestDto orderRequestDto = modelMapper.map(order, OrderRequestDto.class);

        inventoryFeignClient.addStocks(orderRequestDto);

        order.setOrderStatus(OrderStatus.CANCELLED);
        ordersRepository.save(order);

        return "Order Cancelled Successfully";
    }


    public OrderRequestDto createOrderFallBack(OrderRequestDto orderRequestDto, Throwable throwable) {
        log.error("fallback occurred due to : {}", throwable.getMessage());

        return new OrderRequestDto();
    }
}
