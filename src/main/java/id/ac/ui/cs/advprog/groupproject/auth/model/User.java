package id.ac.ui.cs.advprog.groupproject.auth.model;

import id.ac.ui.cs.advprog.groupproject.catalog.model.Catalog;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.List;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String status;

    //TODO: buat user bisa ambil banyak role
    @Column(nullable = false)
    private String role;

    @Column
    private String profilePictureURL;

    @Column
    private String fullName;

    @Column
    private String socials;

    @OneToMany(mappedBy = "jastiper", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Catalog> catalog = new ArrayList<>();

    public boolean isJastiper() {
        return "JASTIPER".equalsIgnoreCase(this.role);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString()));
    }
}
