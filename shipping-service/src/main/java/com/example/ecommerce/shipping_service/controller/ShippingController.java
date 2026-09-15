package com.example.ecommerce.shipping_service.controller;

import com.example.ecommerce.shipping_service.dto.ShipmentRecordDto;
import com.example.ecommerce.shipping_service.dto.ShipmentRequestDto;
import com.example.ecommerce.shipping_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/core")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ShipmentRecordDto> getShipment(@PathVariable Long orderId) {
        ShipmentRecordDto shipmentRecordDto = shippingService.getShipment(orderId);
        return ResponseEntity.ok(shipmentRecordDto);
    }

    @PostMapping("/create-shipment/{orderId}")
    public ResponseEntity<ShipmentRecordDto> createShipment(@PathVariable Long orderId) {
        ShipmentRecordDto shipmentRecordDto = shippingService.createShipment(orderId);
        return ResponseEntity.ok(shipmentRecordDto);
    }

    @PutMapping("/update-shipment")
    public ResponseEntity<ShipmentRecordDto> updateShipment(@RequestBody ShipmentRequestDto shipmentRequestDto) {
        ShipmentRecordDto shipmentRecordDto = shippingService.updateShipmentStatus(shipmentRequestDto);
        return ResponseEntity.ok(shipmentRecordDto);
    }
}
