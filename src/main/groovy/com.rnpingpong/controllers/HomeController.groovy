package com.rnpingpong.controllers

import com.rnpingpong.models.Game
import com.rnpingpong.models.NewGame
import com.rnpingpong.models.Player
import com.rnpingpong.repositories.GameRepository
import com.rnpingpong.repositories.PlayerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
class HomeController {

    @Autowired GameRepository gameRepository
    @Autowired PlayerRepository playerRepository

    @RequestMapping(value = '/', method = RequestMethod.GET)
    String index(final ModelMap model) {
        model.addAttribute('newPlayer', new Player())
        model.addAttribute('newGame', new NewGame())
        return 'index'
    }

    @RequestMapping(value = '/dashboard', method = RequestMethod.GET)
    String dashboard(final ModelMap model) {
        model.addAttribute('players', playerRepository.findAllByOrderByRatingDesc())
        model.addAttribute('games', gameRepository.findAllByOrderByCreatedDesc())
        return 'dashboard'
    }

    @RequestMapping(value='/player/new', method=RequestMethod.POST)
    public String playerSubmit(@ModelAttribute Player newPlayer, ModelMap model) {
        newPlayer.rating = 100;
        if (playerRepository.findByName(newPlayer.name) == null) {
            playerRepository.save(newPlayer)
            model.addAttribute('result', 'success')
        }
        return 'redirect:/'
    }

    @RequestMapping(value='/game/new', method=RequestMethod.POST)
    public String gameSubmit(@ModelAttribute NewGame newGame, ModelMap model) {

        Player player1 = playerRepository.findByName(newGame.playerOne)
        Player player2 = playerRepository.findByName(newGame.playerTwo)

        Game game = new Game()
        game.playerOne = player1.id
        game.playerTwo = player2.id
        game.playerOneScore = newGame.playerOneScore
        game.playerTwoScore = newGame.playerTwoScore
        game.playerOneRatingBefore = player1.rating
        game.playerTwoRatingBefore = player2.rating


        int adj = 0;
        if (game.playerOneScore > game.playerTwoScore) {
            adj = getAdjustment(player1.rating, player2.rating, newGame.playerOneScore, newGame.playerTwoScore)
            player1.rating += adj
            player2.rating = (player2.rating - adj >= 0) ? player2.rating - adj : 0
            game.playerOneAdjustment = adj
            game.playerTwoAdjustment = -adj
        } else {
            adj = getAdjustment(player2.rating, player1.rating, newGame.playerTwoScore, newGame.playerOneScore)
            player1.rating = (player1.rating - adj >= 0) ? player1.rating - adj : 0
            player2.rating += adj
            game.playerOneAdjustment = -adj
            game.playerTwoAdjustment = adj
        }

        playerRepository.save([player1, player2])
        gameRepository.save(game)

        return 'redirect:/'
    }



    private int getAdjustment(int winnerRating, int loserRating, int winnerScore, int loserScore) {
        int ratingDiff = winnerRating - loserRating;
        int scoreDiff = winnerScore - loserScore;
        int adj = 0;

        if (ratingDiff <= 0) { // upset
            if (ratingDiff >= -1) { adj = 2 }
            else if (ratingDiff >= -4) { adj = 3 }
            else if (ratingDiff >= -7) { adj = 4 }
            else if (ratingDiff >= -10) { adj = 6 }
            else if (ratingDiff >= -13) { adj = 8 }
            else if (ratingDiff >= -16) { adj = 10 }
            else if (ratingDiff >= -20) { adj = 13 }
            else if (ratingDiff >= -25) { adj = 16 }
            else { adj = 20 }
        } else { // expected
            if (ratingDiff <= 1) { adj = 2 }
            else if (ratingDiff <= 4) { adj = 2 }
            else if (ratingDiff <= 7) { adj = 2 }
            else if (ratingDiff <= 10) { adj = 2 }
            else if (ratingDiff <= 13) { adj = 1 }
            else if (ratingDiff <= 16) { adj = 1 }
            else if (ratingDiff <= 20) { adj = 1 }
            else if (ratingDiff <= 25) { adj = 0 }
            else { adj = 0 }
        }

        // k factor
        if (scoreDiff >= 14) { adj += 2 }
        else if (scoreDiff >= 7) { adj += 1 }

        return adj;
    }
}