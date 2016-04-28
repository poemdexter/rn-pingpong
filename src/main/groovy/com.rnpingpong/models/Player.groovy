package com.rnpingpong.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "players")
class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @NotNull String name;
    @NotNull Integer rating;
    Integer wins;
    Integer losses;

    def getWinPercentage() {
        int w = wins.intValue()
        int l = losses.intValue()

        if (w + l > 0) {
            return ((w / (w + l)) * 100).intValue()
        } else {
            return 0
        }
    }
}
