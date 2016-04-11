package com.rnpingpong.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.PrePersist
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.validation.constraints.NotNull

@Entity
@Table(name = "games")
class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id

    @NotNull Integer playerOne

    @NotNull Integer playerTwo

    @NotNull Integer playerOneScore

    @NotNull Integer playerTwoScore

    @NotNull Integer playerOneAdjustment

    @NotNull Integer playerTwoAdjustment

    @Temporal(TemporalType.TIMESTAMP)
    Date created

    @PrePersist
    protected void onCreate() {
        created = new Date()
    }
}
