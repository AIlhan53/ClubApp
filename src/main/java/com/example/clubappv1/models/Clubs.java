package com.example.clubappv1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a club.
 *
 * <p>A club has exactly one responsible manager ({@code responsable}, a
 * specific {@link Users}, independent of that user's global role), can
 * have several members, and organizes several {@link Tournaments}.</p>
 */
@Entity
@Getter
@Setter
@Table(name = "clubs")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"responsable", "users", "tournaments"})
public class Clubs {

    @Id
    @GeneratedValue
    private int Id;

    private String name;

    private boolean active;

    /**
     * The users who are members of this club.
     */
    @ManyToMany(mappedBy = "clubs")
    @JsonManagedReference
    @JsonIgnore
    private List<Users> users =  new ArrayList<>();

    /**
     * The user responsible for managing this specific club.
     * <p>
     * This is independent from the user's global role: a club's responsible
     * may hold the {@code RESPONSABLE} role or be an {@code ADMIN}, but is
     * always the one specific user allowed to manage this particular club.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private Users responsable;

    /**
     * The tournaments organized by this club.
     */
    @OneToMany(mappedBy = "club")
    @JsonManagedReference
    @JsonIgnore
    private List<Tournaments> tournaments = new ArrayList<>();

}
