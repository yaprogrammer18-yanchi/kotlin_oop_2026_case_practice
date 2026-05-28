import boxGame.DatabaseManager
import boxGame.GameRepository
import boxGame.SqliteGameRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataBaseTest {

    private lateinit var repository: GameRepository

    @BeforeEach
    fun setup() {
        DatabaseManager.dbUrl = "jdbc:sqlite:test_boxgame_temp.db"
        repository = SqliteGameRepository()
    }

    @AfterEach
    fun cleanup() {
        java.io.File("test_boxgame_temp.db").delete()
    }

    @Test fun `getNextGameId increments correctly after saves`() {
        assertEquals(1, repository.getNextGameId())
        repository.saveGame(1, "Виолетта", "А,Б", "Лог1")
        repository.saveGame(2, "Борис", "А,Б", "Лог2")
        assertEquals(3, repository.getNextGameId())
    }

    @Test fun `saveGame persists and retrieves full game entry`() {
        val id = repository.getNextGameId()
        repository.saveGame(id, "Победитель", "Игрок1,Игрок2", "Ход1 | Ход2")

        val history = repository.getGameHistory()
        assertEquals(1, history.size)
        with(history[0]) {
            assertEquals(id, this.id)
            assertEquals("Победитель", winner)
            assertEquals("Игрок1,Игрок2", players)
            assertEquals("Ход1 | Ход2", movesHistory)
            assertNotNull(date)
        }
    }

    @Test fun `recordPlayerGame creates new player and updates on repeat`() {
        repository.recordPlayerGame("Виолетта", true)
        repository.recordPlayerGame("Виолетта", false)
        repository.recordPlayerGame("Борис", true)

        val stats = repository.getPlayerStatsSorted()
        assertEquals(2, stats.size)

        with(stats[0]) {
            assertEquals("Виолетта", name)
            assertEquals(2, gamesPlayed)
            assertEquals(1, wins)
        }
        with(stats[1]) {
            assertEquals("Борис", name)
            assertEquals(1, gamesPlayed)
            assertEquals(1, wins)
        }
    }

    @Test fun `getPlayerStatsSorted orders by wins then games`() {
        repository.recordPlayerGame("Чарли", true)
        repository.recordPlayerGame("Чарли", true)
        repository.recordPlayerGame("Виолетта", true)
        repository.recordPlayerGame("Виолетта", true)
        repository.recordPlayerGame("Виолетта", false)
        repository.recordPlayerGame("Борис", false)

        val stats = repository.getPlayerStatsSorted()
        assertEquals(3, stats.size)
        assertEquals("Чарли", stats[0].name)
        assertEquals("Виолетта", stats[1].name)
        assertEquals("Борис", stats[2].name)
    }

    @Test fun `getGameHistory returns games in descending id order`() {
        repository.saveGame(1, "А", "Х,У", "Л1")
        repository.saveGame(2, "Б", "Х,У", "Л2")
        repository.saveGame(3, "В", "Х,У", "Л3")

        val history = repository.getGameHistory()
        assertEquals(listOf(3, 2, 1), history.map { it.id })
    }

    @Test fun `getGameHistory returns empty list when database is empty`() {
        assertTrue(repository.getGameHistory().isEmpty())
    }

    @Test fun `completeGameWorkflow persists stats and history correctly`() {
        repository.recordPlayerGame("Виолетта", true)
        repository.recordPlayerGame("Борис", false)
        val gameId = repository.getNextGameId()
        repository.saveGame(gameId, "Виолетта", "Виолетта,Борис,Чарли", "Полный лог игры")

        repository.recordPlayerGame("Чарли", true)
        repository.recordPlayerGame("Виолетта", true)
        repository.recordPlayerGame("Борис", false)

        val stats = repository.getPlayerStatsSorted()
        assertEquals(3, stats.size)

        with(stats.find { it.name == "Виолетта" }!!) {
            assertEquals(2, gamesPlayed); assertEquals(2, wins)
        }
        with(stats.find { it.name == "Чарли" }!!) {
            assertEquals(1, gamesPlayed); assertEquals(1, wins)
        }
        with(stats.find { it.name == "Борис" }!!) {
            assertEquals(2, gamesPlayed); assertEquals(0, wins)
        }

        val history = repository.getGameHistory()
        assertEquals(1, history.size)
        with(history[0]) {
            assertEquals(gameId, id)
            assertEquals("Виолетта", winner)
            assertEquals("Полный лог игры", movesHistory)
        }
    }
}