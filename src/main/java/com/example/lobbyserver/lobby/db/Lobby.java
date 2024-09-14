package com.example.lobbyserver.lobby.db;


import com.example.lobbyserver.user.db.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lobby")
@Getter
@Setter
@NoArgsConstructor
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

    @OneToMany
    @JoinTable(
            name = "lobby_players",
            joinColumns = @JoinColumn(name = "lobby_id"),
            inverseJoinColumns = @JoinColumn(name = "username")
    )
    @Column(nullable = false)
    private Set<User> players = new HashSet<>();

    private String gameServerHost;

    private Integer gameServerPort;

    @Column(nullable = false)
    private Boolean gameStarted;

    @Version
    private Long version;

    public Lobby(Long id, String name, Integer numberOfPlayers, Integer maxPlayers, User owner, Set<User> players, String gameServerHost, Integer gameServerPort, Boolean gameStarted, Long version) {
        this.id = id;
        this.name = name;
        this.numberOfPlayers = numberOfPlayers;
        this.maxPlayers = maxPlayers;
        this.owner = new User(owner.getUsername(), owner.getPassword(), owner.getEmail(), owner.isEnabled(), owner.getAuthorities());
        this.players = new HashSet<>(players);
        this.gameServerHost = gameServerHost;
        this.gameServerPort = gameServerPort;
        this.gameStarted = gameStarted;
        this.version = version;
    }

    public Lobby(String name, Integer maxPlayers, User owner) {
        this(null, name, 0, maxPlayers, owner, Set.of(), null, null, false, null);
    }

    public User getOwner() {
        return new User(owner.getUsername(), owner.getPassword(), owner.getEmail(), owner.isEnabled(), owner.getAuthorities());
    }

    public void setOwner(User owner) {
        this.owner = new User(owner.getUsername(), owner.getPassword(), owner.getEmail(), owner.isEnabled(), owner.getAuthorities());
    }

    public Set<User> getPlayers() {
        return Set.copyOf(players);
    }

    public void setPlayers(Set<User> players) {
        this.players = new HashSet<>(players);
    }

    public boolean isGameActive() {
        return getGameServerHost() != null && getGameServerPort() != null;
    }

    public boolean isNotFull() {
        return !isFull();
    }

    public void addPlayer(User player) {
        players.add(new User(player.getUsername(), player.getPassword(), player.getEmail(), player.isEnabled(), player.getAuthorities()));
    }

    public void removePlayer(User player) {
        players.removeIf(p -> p.getUsername().equals(player.getUsername()));
    }

    public boolean isFull() {
        return getNumberOfPlayers() >= getMaxPlayers();
    }

    public boolean notStarted() {
        return !getGameStarted();
    }

    public boolean containsUser(String username) {
        return players.stream().anyMatch(user -> user.getUsername().equals(username));
    }
}
