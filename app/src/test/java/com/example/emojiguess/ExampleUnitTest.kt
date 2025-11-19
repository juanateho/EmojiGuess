package com.example.emojiguess

import com.example.emojiguess.model.GameLogic
import com.example.emojiguess.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLogicTest {

    @Test
    fun `determineWinner returns winner when only one player is alive`() {
        val players = listOf(
            Player(id = "1", name = "Alive", isAlive = true),
            Player(id = "2", name = "Dead", isAlive = false)
        )
        val winner = GameLogic.determineWinner(players)
        assertEquals("Alive", winner?.name)
    }

    @Test
    fun `determineWinner returns null when multiple players are alive`() {
        val players = listOf(
            Player(id = "1", name = "Alive1", isAlive = true),
            Player(id = "2", name = "Alive2", isAlive = true)
        )
        val winner = GameLogic.determineWinner(players)
        assertNull(winner)
    }
    
    @Test
    fun `isGuessCorrect returns true for matching emoji`() {
        assertTrue(GameLogic.isGuessCorrect("😀", "😀"))
    }

    @Test
    fun `isGuessCorrect returns false for different emoji`() {
        assertFalse(GameLogic.isGuessCorrect("😀", "👽"))
    }
}
