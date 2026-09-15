package com.example.ecommerce.shipping_service.service;

import com.example.ecommerce.shipping_service.dto.ShipmentRecordDto;
import com.example.ecommerce.shipping_service.dto.ShipmentRequestDto;
import org.springframework.http.ResponseEntity;

public interface ShippingService {
    ShipmentRecordDto createShipment(Long orderId);

    ShipmentRecordDto updateShipmentStatus(ShipmentRequestDto shipmentRequestDto);

    ShipmentRecordDto getShipment(Long orderId);
}
