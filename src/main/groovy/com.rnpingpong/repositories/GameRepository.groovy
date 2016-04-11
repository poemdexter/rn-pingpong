package com.rnpingpong.repositories

import com.rnpingpong.models.Game
import org.springframework.data.repository.CrudRepository


interface GameRepository extends CrudRepository<Game, Integer> {

    List<Game> findAllByOrderByCreatedDesc()
}