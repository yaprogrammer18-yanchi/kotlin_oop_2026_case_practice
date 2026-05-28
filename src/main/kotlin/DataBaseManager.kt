package boxGame
import java.sql.Connection
import java.sql.DriverManager

object DatabaseManager {
    var dbUrl = "jdbc:sqlite:boxgame.db"

    fun getConnection(): Connection {
        val conn = DriverManager.getConnection(dbUrl)
        createTables(conn)
        return conn
    }


    private fun createTables(conn: Connection) {
        // таблица со статистикой игроков
        conn.createStatement().use { stmt ->
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS PlayerStats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    games_played INTEGER DEFAULT 0,
                    wins INTEGER DEFAULT 0
                )
            """)
            // таблица с историей игр
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS GameHistory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    winner TEXT,
                    players TEXT NOT NULL,
                    moves_history TEXT NOT NULL
                )
            """)
        }
    }
}