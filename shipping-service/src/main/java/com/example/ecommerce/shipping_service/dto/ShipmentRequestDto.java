package com.example.ecommerce.shipping_service.dto;

import com.example.ecommerce.shipping_service.entity.ShipmentStatus;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

@Data
public class ShipmentRequestDto {
    @NotNull
    private Long orderId;

    @NotNull
    private ShipmentStatus shipmentStatus;
}
