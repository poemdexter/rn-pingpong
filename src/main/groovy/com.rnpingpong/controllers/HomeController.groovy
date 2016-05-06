package com.rnpingpong.controllers

import com.rnpingpong.models.Game
import com.rnpingpong.models.NewGame
import com.rnpingpong.models.Player
import com.rnpingpong.repositories.GameRepository
import com.rnpingpong.repositories.PlayerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
class HomeController {

    @Autowired GameRepository gameRepository
    @Autowired PlayerRepository playerRepository

    @Value('${hipchatToken}')
    String authToken

    @RequestMapping(value = '/', method = RequestMethod.GET)
    String index(final ModelMap model) {
        model.addAttribute('newPlayer', new Player())
        model.addAttribute('newGame', new NewGame())
        return 'index'
    }

    @RequestMapping(value = '/dashboard', method = RequestMethod.GET)
    String dashboard(final ModelMap model) {
        model.addAttribute('players', playerRepository.findAllByOrderByRatingDesc())
        model.addAttribute('games', gameRepository.findTop36ByOrderByCreatedDesc())
        return 'dashboard'
    }

    @RequestMapping(value='/player/new', method=RequestMethod.POST)
    public String playerSubmit(@ModelAttribute Player newPlayer, ModelMap model) {
        newPlayer.rating = 100;
        newPlayer.wins = 0;
        newPlayer.losses = 0;
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
            player1.wins += 1
            player2.losses += 1
            game.playerOneAdjustment = adj
            game.playerTwoAdjustment = -adj
        } else {
            adj = getAdjustment(player2.rating, player1.rating, newGame.playerTwoScore, newGame.playerOneScore)
            player1.rating = (player1.rating - adj >= 0) ? player1.rating - adj : 0
            player2.rating += adj
            player1.losses += 1
            player2.wins += 1
            game.playerOneAdjustment = -adj
            game.playerTwoAdjustment = adj
        }

        playerRepository.save([player1, player2])
        gameRepository.save(game)

        notifyHipchat(game, player1.name, player2.name)

        return 'redirect:/'
    }

    private void notifyHipchat(game, p1Name, p2Name) {
        def message = "$p1Name $game.playerOneRatingBefore($game.playerOneAdjustment) vs. $p2Name $game.playerTwoRatingBefore($game.playerTwoAdjustment) : $game.playerOneScore - $game.playerTwoScore"
        def hipchatMessage = message.replace(' ', '+')
        new URL("https://api.hipchat.com/v1/rooms/message?room_id=2636455&from=PingPongBot&message=$hipchatMessage&notify=1&auth_token=$authToken").getText()
    }

    private int getAdjustment(int winnerRating, int loserRating, int winnerScore, int loserScore) {
        int ratingDiff = winnerRating - loserRating;
        int scoreDiff = winnerScore - loserScore;
        int adj = 0;

        if (ratingDiff <= 0) { // upset
                 if (ratingDiff >=  -4) { adj = 2 }
            else if (ratingDiff >=  -8) { adj = 4 }
            else if (ratingDiff >= -12) { adj = 6 }
            else if (ratingDiff >= -16) { adj = 8 }
            else if (ratingDiff >= -20) { adj = 10 }
            else if (ratingDiff >= -24) { adj = 12 }
            else { adj = 15 }
        } else { // expected
                 if (ratingDiff <= 12) { adj = 2 }
            else if (ratingDiff <= 24) { adj = 1 }
            else { adj = 0 }
        }

        // k factor
        if (scoreDiff >= 14) { adj += 2 }
        else if (scoreDiff >= 7) { adj += 1 }

        return adj;
    }
}