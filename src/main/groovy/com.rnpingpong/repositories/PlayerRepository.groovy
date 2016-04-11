package com.rnpingpong.repositories

import com.rnpingpong.models.Player
import org.springframework.data.repository.CrudRepository


interface PlayerRepository extends CrudRepository<Player, Integer> {

    Player findByName(String name)

    List<Player> findAllByOrderByRatingDesc()
}