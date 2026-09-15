package com.example.ecommerce.shipping_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long orderId;

    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus shipmentStatus;

    private Instant shippedAt;

    private LocalDate estimatedDeliveryDate;
}
