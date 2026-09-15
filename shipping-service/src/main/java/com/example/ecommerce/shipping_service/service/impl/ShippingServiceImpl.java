package com.example.ecommerce.shipping_service.service.impl;

import com.example.ecommerce.shipping_service.dto.ShipmentRecordDto;
import com.example.ecommerce.shipping_service.dto.ShipmentRequestDto;
import com.example.ecommerce.shipping_service.entity.ShipmentRecord;
import com.example.ecommerce.shipping_service.entity.ShipmentStatus;
import com.example.ecommerce.shipping_service.repository.ShipmentRecordRepository;
import com.example.ecommerce.shipping_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShipmentRecordRepository shipmentRecordRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ShipmentRecordDto createShipment(Long orderId) {
        log.info("Creating shipment for order ID: {}", orderId);
        shipmentRecordRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new RuntimeException("Shipment already exists for order ID: " + orderId);
        });

        ShipmentRecord shipmentRecord = ShipmentRecord.builder()
                .orderId(orderId)
                .trackingNumber(UUID.randomUUID().toString())
                .shipmentStatus(ShipmentStatus.PENDING)
                .estimatedDeliveryDate(LocalDate.now().plusDays(5))
                .build();

        ShipmentRecord savedShipment = shipmentRecordRepository.save(shipmentRecord);
        return modelMapper.map(savedShipment, ShipmentRecordDto.class);
    }

    @Override
    @Transactional
    public ShipmentRecordDto updateShipmentStatus(ShipmentRequestDto shipmentRequestDto) {
        log.info("Updating shipment status for order ID: {}", shipmentRequestDto.getOrderId());
        ShipmentStatus newStatus = shipmentRequestDto.getShipmentStatus();
        if (newStatus == null) {
            throw new RuntimeException("Shipment status must not be null");
        }

        ShipmentRecord shipmentRecord = shipmentRecordRepository.findByOrderId(shipmentRequestDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Shipment record not found for order ID: "
                        + shipmentRequestDto.getOrderId()));

        if (!isValidTransition(shipmentRecord.getShipmentStatus(), newStatus)) {
            throw new RuntimeException("Invalid shipment status transition from "
                    + shipmentRecord.getShipmentStatus() + " to " + newStatus);
        }

        shipmentRecord.setShipmentStatus(newStatus);
        if (newStatus == ShipmentStatus.SHIPPED) {
            shipmentRecord.setShippedAt(Instant.now());
        }

        ShipmentRecord savedShipmentRecord = shipmentRecordRepository.save(shipmentRecord);
        return modelMapper.map(savedShipmentRecord, ShipmentRecordDto.class);
    }

    @Override
    public ShipmentRecordDto getShipment(Long orderId) {
        log.info("Getting shipment details for order ID: {}", orderId);
        ShipmentRecord shipmentRecord = shipmentRecordRepository.findByOrderId(orderId).orElseThrow(() ->
                new RuntimeException("Shipment record not found with order ID: " + orderId));

        return modelMapper.map(shipmentRecord, ShipmentRecordDto.class);
    }

    //    utility method
    private boolean isValidTransition(ShipmentStatus from, ShipmentStatus to) {
        if (from == to) {
            return true;
        }
        return switch (from) {
            case PENDING    -> to == ShipmentStatus.IN_TRANSIT || to == ShipmentStatus.SHIPPED || to == ShipmentStatus.CANCELLED;
            case IN_TRANSIT -> to == ShipmentStatus.SHIPPED || to == ShipmentStatus.DELIVERED;
            case SHIPPED    -> to == ShipmentStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
