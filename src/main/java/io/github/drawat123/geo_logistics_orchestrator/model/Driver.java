package io.github.drawat123.geo_logistics_orchestrator.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus; // Uses the DriverStatus enum defined above

    // Stores the vertical position North (+) or South (-) of the Equator.
    // Example: New York City is approximately 40.71280000
    private double latitude;

    // Stores the horizontal position East (+) or West (-) of the Prime Meridian.
    // Example: New York City is approximately -74.00600000 (Negative because it's West)
    private double longitude;

    @Version
    private Integer version; // Managed automatically by JPA

    // Warning: Be careful with this list. If you return 'Driver' in a REST API,
    // it will try to print all Orders, which prints the Driver, which prints Orders...
    // Infinite Recursion. Use @JsonIgnore here or use DTOs.
    @OneToMany(mappedBy = "driver")
    private List<Order> orders;
}