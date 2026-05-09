package boxGame

enum class GameStatus {
    WAITING,
    IN_PROGRESS,
    PAUSED,
    FINISHED
}


class Game(val id: Int) {
    private var status = GameStatus.WAITING
    val players = mutableListOf<Player>()
    var mainDeck: Deck = Deck()
    private set
    private var currentAskerPlayerIndex: Int = 0
    private var currentTargetPlayerIndex: Int = 0
    private var currentTurn: Turn? = null
    var winner: Player? = null
    private set
    var historyOfTurns = mutableListOf<Turn>()
    private set
    private var currentDialog: Dialog? = null

    fun addPlayer(player: Player) {
        players.add(player)
    }
    fun isEmptyDeck(): Boolean{
        return mainDeck.deckIsEmpty()
    }
    fun forceEndGame(){
        status = GameStatus.FINISHED
    }

    // извне формируется список карт и передается в колоду
    // тут все карты, которые получает игрок будут автоматически добавляться к нему из этой колоды
    fun initialiseNewDeck(cards: MutableList<Card>){
        mainDeck.initialiseDeck(cards)
    }

    fun initialisation(cardsPerPlayer: Int = 4) {
        if (players.isEmpty()) {
            println("Нет игроков!")
            return
        }

        // Проверяем, хватает ли карт в колоде
        val neededCards = players.size * cardsPerPlayer
        if (mainDeck.size < neededCards) {
            println("Ошибка: в колоде недостаточно карт! Нужно ${neededCards}, есть ${mainDeck.size}")
            return
        }

        // Раздаём карты игрокам
        for (player in players) {
            val cards = mainDeck.getCards(cardsPerPlayer)
            player.addCardsInHand(cards)
        }
        status = GameStatus.IN_PROGRESS
    }

    fun runTurn() {
        if (status != GameStatus.IN_PROGRESS) {
            println("Игра не активна")
            return
        }
        if (winner != null) {
            println("Игра уже закончена! Победитель: ${winner?.name}")
            return
        }

        // ИНИЦИАЛИЗАЦИЯ ХОДА

        currentAskerPlayerIndex = currentTargetPlayerIndex
        currentTargetPlayerIndex = (currentTargetPlayerIndex + 1) % players.size

        val currentAsker = players[currentAskerPlayerIndex]
        val currentTarget = players[currentTargetPlayerIndex]
        println("\n Следующий (текущий) ход: ${currentAsker.name} → ${currentTarget.name}")
        currentDialog = Dialog(currentAsker, currentTarget)


        // ЕСЛИ у спрашивающего нет карт, добрать 4 или оставшиеся из колоды
        if (currentAsker.hand.isEmpty() && !mainDeck.deckIsEmpty()) {
            val drawnCards = mainDeck.getCards(4)
            currentAsker.addCardsInHand(drawnCards)
        }

        // Если у спрашивающего нет карт и колода пуста - пропуск хода
        if (currentAsker.hand.isEmpty()) {
            return
        }

        // Если у отвечающего нет карт



        // НОМИНАЛ

        fun enterQuestionNominal(): DialogState {
            println("\n--- ВОПРОС (номинал) ---")
            val input = readln().uppercase() ?: return DialogState.ERROR// дописать что произойдет, если ничего не ввела
            val nominal = Nominal.fromDisplayName(input)
            val state = currentDialog?.questionNominal(nominal) ?: return DialogState.ERROR
            return state
        }

        fun enterAnswerNominal(): DialogState {
            println("\n--- ОТВЕТ (номинал) ---")
            val input = readLine()?.uppercase() ?: return DialogState.ERROR // дописать что произойдет, если ничего не ввела
            val answer = Answer.fromDisplayName(input)
            val state = currentDialog?.answerNominal(answer) ?: return DialogState.ERROR
            return state
        }


        // КОЛИЧЕСТВО

        fun enterQuestionQuantity(): DialogState {
            val dialog = currentDialog ?: return DialogState.ERROR_QUESTION
            println("\n--- ВОПРОС (количество) ---")
            val input = readln().toInt()
            // проверить, что что-то действительно было введено, просить ввод, пока не будут введены корректные данные
            val state = dialog.questionQuantity(input)
            return state
        }

        fun enterAnswerQuantity(): DialogState {
            println("\n--- ОТВЕТ (количество) ---")
            val input = readLine()?.uppercase() ?: return DialogState.ERROR // дописать что произойдет, если ничего не ввела
            val answer = Answer.fromDisplayName(input)
            val state = currentDialog?.answerQuantity(answer) ?: return DialogState.ERROR
            return state
        }

        // МАСТИ

        // проверку на некорректные масти делать не буду - возможно сделаю просто кнопочки с мастями, которые просто
        // можно будет нажимать
        fun enterQuestionSuits(): DialogState{
            println("\n--- ВОПРОС (масти) ---")
            var input = readLine()
            // Проверка, что ввод не пустой
            while (input.isNullOrBlank()) {
                println("Ошибка: Вы ничего не ввели!")
                input = readLine()
            }
            val suitsStr = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            val arr = mutableListOf<Suit?>()
            for (suit in suitsStr){
                arr.add(Suit.fromDisplayName(suit))
            }
            val suits = arr.filterNotNull()
            return currentDialog?.questionSuits(suits) ?: return DialogState.ERROR
        }

        fun enterAnswerSuits(): DialogState{
            println("\n--- ОТВЕТ (масти) ---")
            val input = readLine()?.uppercase() ?: return DialogState.ERROR // дописать что произойдет, если ничего не ввела
            val answer = Answer.fromDisplayName(input)
            val state = currentDialog?.answerSuits(answer) ?: return DialogState.ERROR
            return state
        }


        // МЕХАНИЗМ ХОДА:

        var state = enterQuestionNominal()
        while (state == DialogState.ASK_AGAIN){
            println("Блеф или ошибка!")
            state = enterQuestionNominal()

        }
        state = enterAnswerNominal()
        while (state == DialogState.ANSWER_AGAIN){
            println("Блеф или ошибка!")
            state = enterAnswerNominal()
        }

        if (state == DialogState.ASK_QUANTITY){
            // игрок может сказать любое кол-во (хоть 7 карт одного номинала)
            state = enterQuestionQuantity()
            // тут в любом случает будет ANSWER_QUANTITY
            state = enterAnswerQuantity()
            while (state == DialogState.ANSWER_AGAIN){
                println("Блеф или ошибка!")
                state = enterAnswerQuantity()
            }
        }

        if (state == DialogState.ASK_SUITS){
            state = enterQuestionSuits()
            while (state == DialogState.ASK_AGAIN){
                println("Блеф или ошибка!")
                state = enterQuestionSuits()
            }

            state = enterAnswerSuits()
            while (state == DialogState.ANSWER_AGAIN){
                println("Блеф или ошибка!")
                state = enterAnswerSuits()
            }

            // ПЕРЕДАЧА УГАДАННЫХ КАРТ

            if (state == DialogState.GUESSED){
                val cards =  currentDialog?.guessedCards ?: return
                currentTarget.removeCardsFromHand(cards)
                currentAsker.addCardsInHand(cards)
            }
        }

        // ЕСЛИ НЕ УГАДАЛ - ВЗЯТЬ КАРТУ ИЗ КОЛОДЫ
        if (state == DialogState.NOT_GUESSED){
            if (!mainDeck.deckIsEmpty()){
                val CardFromDeck = mainDeck.getCards(1)
                currentAsker.addCardsInHand(CardFromDeck)
            }
        }

        // ФОРМИРОВАНИЕ ИСТОРИИ ХОДА
        currentTurn = Turn(dialog = currentDialog)
        println(currentTurn?.toLogString()) ?: "Ошибка"
    }


    fun runGame() {
        while (status == GameStatus.IN_PROGRESS) {
            // отладочный вывод
            for (player in players) {
                println("${player.name}: ${player.hand.joinToString { "${it.nominal.displayName} ${it.suit.displayName}" }}")
            }
            // отладочный вывод
            runTurn()
            currentTurn?.let { historyOfTurns.add(it) }

            // Если колода пустая — доигрываем ровно один финальный круг
            if (isEmptyDeck()) {
                val startIndex = (currentAskerPlayerIndex + 1) % players.size

                for (i in 0 until players.size) {
                    val askerIdx = (startIndex + i) % players.size
                    val asker = players[askerIdx]

                    if (asker.hand.isEmpty()) {
                        continue
                    }

                    currentAskerPlayerIndex = askerIdx

                    var targetIdx = (askerIdx + 1) % players.size
                    var checked = 0
                    while (players[targetIdx].hand.isEmpty() && checked < players.size) {
                        targetIdx = (targetIdx + 1) % players.size
                        checked++
                    }
                    if (checked < players.size) {
                        currentTargetPlayerIndex = targetIdx
                        runTurn()
                        currentTurn?.let { historyOfTurns.add(it) }
                    }
                }
            }
        }
        // победитель
        val maxBoxes = players.maxOfOrNull { it.quantityOfBoxes } ?: 0
        winner = players.firstOrNull { it.quantityOfBoxes == maxBoxes }
        println("Победил ${winner}")
        status = GameStatus.FINISHED
        return
    }
}

// небольшой примерчик, можно прям поиграть
fun main() {
    val players = mutableListOf(
        Player("Анна"),
        Player("Борис")
    )

    // Создаём колоду с картами
    val allCards = mutableListOf<Card>()

    // Все карты для колоды (16 карт для двух игроков по 4 карты + запас)
    allCards.add(Card(Nominal.QUEEN, Suit.HEARTS))
    allCards.add(Card(Nominal.QUEEN, Suit.SPADES))
    allCards.add(Card(Nominal.KING, Suit.CLUBS))
    allCards.add(Card(Nominal.SIX, Suit.DIAMONDS))
    allCards.add(Card(Nominal.QUEEN, Suit.DIAMONDS))
    allCards.add(Card(Nominal.KING, Suit.HEARTS))
    allCards.add(Card(Nominal.KING, Suit.SPADES))
    allCards.add(Card(Nominal.SEVEN, Suit.CLUBS))

    // Показываем карты в колоде
    println("\n--- КОЛОДА ---")
    println("----------------")

    val game = Game(1)
    for (player in players) {
        game.addPlayer(player)
    }

    game.initialiseNewDeck(allCards)
    game.initialisation(4)  // раздаём по 4 карты

    println("\n--- КАРТЫ ИГРОКОВ ПОСЛЕ РАЗДАЧИ ---")
    for (player in players) {
        println("${player.name}: ${player.hand.joinToString { "${it.nominal.displayName} ${it.suit.displayName}" }}")
    }
    game.runGame()
}
