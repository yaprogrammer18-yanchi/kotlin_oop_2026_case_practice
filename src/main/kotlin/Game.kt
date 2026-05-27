package boxGame

enum class GameStatus {
    WAITING,
    IN_PROGRESS,
    FINISHED
}

class Game(var id: Int) {
    var status = GameStatus.WAITING
    val players = mutableListOf<Player>()
    var mainDeck: Deck = Deck()
    var winner: Player? = null
    var historyOfTurns = mutableListOf<String>()
        private set

    fun addPlayer(player: Player) {
        players.add(player)
    }

    fun isEmptyDeck(): Boolean {
        return mainDeck.deckIsEmpty()
    }

    // извне формируется список карт и передается в колоду
    // тут все карты, которые получает игрок будут автоматически добавляться к нему из этой колоды
    fun initialiseNewDeck(cards: MutableList<Card>) {
        mainDeck.initialiseDeck(cards)
    }

    // карт всегда хватает, так как это отлавливается еще во View, если карт банально недостаточно для раздачи
    fun initialisation(cardsPerPlayer: Int = 4) {
        if (players.isEmpty()) {
            return
        }
        // Раздаём карты игрокам
        for (player in players) {
            val cards = mainDeck.getCards(cardsPerPlayer)
            player.addCardsInHand(cards)
        }
        status = GameStatus.IN_PROGRESS
    }
}