package io.github.drawat123.geo_logistics_orchestrator.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data // Use Lombok for getters/setters
@Table(name = "orders") // Good practice to name tables plural
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal orderValue;

    @ManyToOne(fetch = FetchType.LAZY) // Many orders belong to one driver
    @JoinColumn(name = "driver_id", nullable = true) // Defines the foreign key column
    private Driver driver;

    // Add status so we know if it's PENDING or DELIVERED
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private Double destinationLat;

    private Double destinationLon;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false) // Ensures it's never changed after creation
    private LocalDateTime createdAt;
}
