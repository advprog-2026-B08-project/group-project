package id.ac.ui.cs.advprog.groupproject.model;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String email;

    @Column
    private String status;

    //TODO: buat user bisa ambil banyak role
    @Column(nullable = false)
    private String role;

    @OneToMany(mappedBy = "jastiper", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Catalog> catalog = new ArrayList<>();

    public boolean isJastiper() {
        return "JASTIPER".equalsIgnoreCase(this.role);
    }
}
