package com.example.ecommerce.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Integer quantity;

    @ManyToOne()
    @JoinColumn(name = "order_id")
    private Orders order;
}
