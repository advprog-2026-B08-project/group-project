package id.ac.ui.cs.advprog.groupproject.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;
import java.time.LocalDate;

@Entity 
@Getter @Setter
@Table(name = "Catalog")
public class Catalog {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String name;
    private String description;
    private Double price;
    private Integer stock;
    
    @Column(name = "origin_location")
    private String originLocation;

    @Column(name = "travel_date")
    private LocalDate travelDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User jastiper;
}