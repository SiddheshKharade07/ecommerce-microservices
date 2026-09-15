package com.example.ecommerce.shipping_service.dto;

import com.example.ecommerce.shipping_service.entity.ShipmentStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class ShipmentRecordDto {

    private Long id;
    private Long orderId;
    private String trackingNumber;
    private ShipmentStatus shipmentStatus;
    private Instant shippedAt;
    private LocalDate estimatedDeliveryDate;
}
