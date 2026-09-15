package com.example.ecommerce.order_service.clients;

import com.example.ecommerce.order_service.dto.ShipmentRecordDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "shipping-service", path = "/shipping")
public interface ShippingFeignClient {

    @GetMapping("/core/{orderId}")
    ResponseEntity<ShipmentRecordDto> getShipment(@PathVariable Long orderId);
}
