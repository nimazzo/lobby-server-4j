package com.example.lobbyserver.lobby.db;


import com.example.lobbyserver.user.db.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lobby")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer numberOfPlayers;

    @Column(nullable = false)
    private Integer maxPlayers;

    @ManyToOne
    @JoinColumn(name = "owner", nullable = false, referencedColumnName = "username", foreignKey = @ForeignKey(name = "fk_lobby_users"))
    private User owner;

    @Column(nullable = false)
    private String gameServerHost;

    @Column(nullable = false)
    private Integer gameServerPort;

}
