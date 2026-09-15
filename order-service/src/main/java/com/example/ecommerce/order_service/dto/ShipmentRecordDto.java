package com.example.ecommerce.order_service.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class ShipmentRecordDto {

    private Long id;
    private Long orderId;
    private String trackingNumber;
    private String shipmentStatus;
    private Instant shippedAt;
    private LocalDate estimatedDeliveryDate;
}
