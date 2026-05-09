package boxGame
class Card(
    val nominal: Nominal,
    val suit: Suit
)

enum class Suit(val displayName: String){
    HEARTS("HEARTS"),
    DIAMONDS("DIAMONDS"),
    CLUBS("CLUBS"),
    SPADES("SPADES");

    companion object {
        fun fromDisplayName(displayName: String?): Suit? {
            return Suit.values().find { it.displayName.equals(displayName, ignoreCase = true) }
        }
    }
}


enum class Nominal(val displayName: String) {
    SIX("SIX"),
    SEVEN("SEVEN"),
    EIGHT("EIGHT"),
    NINE("NINE"),
    TEN("TEN"),
    JACK("JACK"),
    QUEEN("QUEEN"),
    KING("KING"),
    ACE("ACE");

companion object {
    fun fromDisplayName(displayName: String?): Nominal? {
        return values().find { it.displayName.equals(displayName, ignoreCase = true) }
    }
}
}