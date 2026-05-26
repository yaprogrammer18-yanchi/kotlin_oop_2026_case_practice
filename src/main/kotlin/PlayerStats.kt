package boxGame

data class PlayerStats(
    val name: String,
    var gamesPlayed: Int = 0,
    var wins: Int = 0,
)