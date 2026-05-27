package boxGame
import java.io.File

class PlayerRegistry(private val filePath: String = "src/main/kotlin/playersStats.dat") {
    private val stats = mutableMapOf<String, PlayerStats>()

    init { load() }

    private fun load() {
        val file = File(filePath)
        if (!file.exists()) return
        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            val parts = line.split("|").map { it.trim() }
            if (parts.size == 3) {
                stats[parts[0].lowercase()] = PlayerStats(
                    name = parts[0],
                    gamesPlayed = parts[1].toIntOrNull() ?: 0,
                    wins = parts[2].toIntOrNull() ?: 0
                )
            }
        }
    }

    private fun save() {
        File(filePath).writeText(
            stats.values.joinToString("\n") { "${it.name}|${it.gamesPlayed}|${it.wins}" }
        )
    }

    fun getOrCreate(displayName: String): PlayerStats {
        val key = displayName.trim().lowercase()
        return stats.getOrPut(key) { PlayerStats(displayName.trim()) }
    }

    fun recordGame(displayName: String, isWinner: Boolean) {
        val p = getOrCreate(displayName)
        p.gamesPlayed++
        if (isWinner) p.wins++
        save()
    }


    fun getAllSorted(): List<PlayerStats> =
        stats.values.sortedWith(
            compareByDescending<PlayerStats> { it.wins }
                .thenByDescending { it.gamesPlayed }
        )}