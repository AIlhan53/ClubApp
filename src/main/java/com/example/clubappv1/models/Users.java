package com.example.clubappv1.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a registered user of the application.
 *
 * <p>A user has a global {@link Roles}
 * (member, responsible, admin), can be a member of several {@link Clubs},
 * can be the responsible manager of several clubs ({@code clubsResponsable}),
 * and can participate in several {@link Tournaments}.</p>
 */
@Entity
@Getter
@Setter
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"role", "clubsResponsable", "tournaments", "clubs"})
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int Id;

    private String firstName;
    private String lastName;

    @Email(message = "ne correspond pas au format email")
    @EqualsAndHashCode.Include
    private String email;

    private boolean active;

    @JsonIgnore
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role;

    @ManyToMany
    @JoinTable(
            name = "members_clubs",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "club_id")
    )
    private List<Clubs> clubs = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "tournaments_players",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tournament_id")
    )
    private Set<Tournaments> tournaments = new HashSet<>();

    @OneToMany(mappedBy = "responsable")
    @JsonBackReference
    @JsonIgnore
    private List<Clubs> clubsResponsable = new ArrayList<>();
}