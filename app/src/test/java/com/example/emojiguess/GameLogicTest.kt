package com.example.emojiguess

import com.example.emojiguess.model.Emojis
import com.example.emojiguess.model.Player
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Timer
import kotlin.concurrent.schedule

class GameLogicTest {
    @Test
    fun testAssignEmojisToPlayers() {
        val players = listOf(
            Player(id = "1", name = "Player 1"),
            Player(id = "2", name = "Player 2"),
            Player(id = "3", name = "Player 3")
        )

        val updatedPlayers = Emojis.assignEmojisToPlayers(players)

        // Verificar que cada jugador tiene un emoji
        updatedPlayers.values.forEach { player ->
            assertTrue("El jugador ${player.name} debe tener un emoji", player.assignedEmoji.isNotEmpty())
        }

        // Verificar que al menos dos jugadores tienen emojis diferentes
        val uniqueEmojis = updatedPlayers.values.map { it.assignedEmoji }.toSet()
        assertTrue("Debe haber más de un emoji único asignado", uniqueEmojis.size > 1)
    }

    @Test
    fun testTimerFunctionality() {
        var timerFinished = false
        val timer = Timer()

        timer.schedule(1000) {
            timerFinished = true
        }

        Thread.sleep(1500) // Esperar más que el tiempo del temporizador

        assertTrue("El temporizador debería haber terminado", timerFinished)
    }
}
