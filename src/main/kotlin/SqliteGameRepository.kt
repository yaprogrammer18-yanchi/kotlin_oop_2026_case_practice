package boxGame

interface GameRepository {
    fun getNextGameId(): Int
    fun recordPlayerGame(name: String, isWinner: Boolean)
    fun getPlayerStatsSorted(): List<PlayerStats>
    fun saveGame(id: Int, winner: String?, players: String, movesHistory: String)
    fun getGameHistory(): List<GameEntry>
}

data class GameEntry(
    val id: Int,
    val date: String,
    val winner: String?,
    val players: String,
    val movesHistory: String
)

class SqliteGameRepository : GameRepository {

    override fun getNextGameId(): Int {
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement("SELECT COALESCE(MAX(id), 0) + 1 FROM GameHistory").use { stmt ->
                stmt.executeQuery().use { rs -> if (rs.next()) return rs.getInt(1) }
            }
        }
        return 1
    }

    override fun recordPlayerGame(name: String, isWinner: Boolean) {
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO PlayerStats (name, games_played, wins) 
                VALUES (?, 1, ?) 
                ON CONFLICT(name) DO UPDATE SET 
                games_played = games_played + 1, 
                wins = wins + ?
            """).use { stmt ->
                stmt.setString(1, name)
                stmt.setInt(2, if (isWinner) 1 else 0)
                stmt.setInt(3, if (isWinner) 1 else 0)
                stmt.executeUpdate()
            }
        }
    }

    override fun getPlayerStatsSorted(): List<PlayerStats> {
        val list = mutableListOf<PlayerStats>()
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(
                "SELECT name, games_played, wins FROM PlayerStats ORDER BY wins DESC"
            ).use { stmt ->
                stmt.executeQuery().use { rs ->
                    while (rs.next()) list.add(PlayerStats(rs.getString(1), rs.getInt(2), rs.getInt(3)))
                }
            }
        }
        return list
    }

    override fun saveGame(id: Int, winner: String?, players: String, movesHistory: String) {
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(
                "INSERT INTO GameHistory (id, date, winner, players, moves_history) VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?)"
            ).use { stmt ->
                stmt.setInt(1, id)
                stmt.setString(2, winner)
                stmt.setString(3, players)
                stmt.setString(4, movesHistory)
                stmt.executeUpdate()
            }
        }
    }

    override fun getGameHistory(): List<GameEntry> {
        val list = mutableListOf<GameEntry>()
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(
                "SELECT id, date, winner, players, moves_history FROM GameHistory ORDER BY id DESC"
            ).use { stmt ->
                stmt.executeQuery().use { rs ->
                    while (rs.next()) list.add(GameEntry(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)))
                }
            }
        }
        return list
    }
}