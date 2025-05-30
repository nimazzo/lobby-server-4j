package com.example.lobbyserver.user.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("unused")
public class User {
    @Id
    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 500)
    private String password;

    @Column(unique = true, length = 320)
    private String email;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Authority> authorities = new HashSet<>();

    public User(String username, String password, String email, boolean enabled, Set<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.enabled = enabled;
        this.authorities = Set.copyOf(authorities);
    }

    public User(User user) {
        this.username = user.username;
        this.password = user.password;
        this.email = user.email;
        this.enabled = user.enabled;
        this.authorities = Set.copyOf(user.authorities);
    }

    public Set<Authority> getAuthorities() {
        return Set.copyOf(authorities);
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = Set.copyOf(authorities);
    }
}
