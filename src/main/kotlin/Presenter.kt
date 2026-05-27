package boxGame
import javax.swing.JOptionPane

// Слушатель View
interface GameViewListener {
    fun onNominalQuestionSubmitted(nominal: Nominal)
    fun onQuantityQuestionSubmitted(quantity: Int)
    fun onSuitsQuestionSubmitted(suits: List<Suit>)
    fun onAnswerSubmitted(answer: Answer)
    fun onGameInitialized(playerNames: List<String>, cards: MutableList<Card>)
    fun onMenuRequested()
    fun onHistoryRequested()
}

class GamePresenter(
    private val view: GameView,
    private val repository: GameRepository
) : GameViewListener {

    private val game: Game = Game(1)
    private var currentDialog: Dialog? = null
    private var currentAsker: Player? = null
    private var currentTarget: Player? = null

    private var currentAskerIndex = 0
    private var currentTargetIndex = 1

    private var isFinalRound = false
    private var finalRoundTurns = mutableListOf<Pair<Player, Player>>()

    // текущий номер хода в финальном раунде
    private var currentFinalTurnIndex = 0

    fun startTurn(asker: Player, target: Player) {
        currentAsker = asker
        currentTarget = target
        currentDialog = Dialog(asker, target)
        view.updateGameState(game.players, asker, target)
        showCurrentQuestion()
    }

    // после того как был сделан ход
    private fun afterTurnComplete() {
        view.updateGameState(game.players, currentAsker, currentTarget)
        if (isFinalRound) {
            currentFinalTurnIndex++
            executeNextFinalTurn()
        } else {
            nextTurn()
        }
    }

    private fun nextTurn() {
        // Проверяем, не закончилась ли колода
        val deckIsEmpty = game.isEmptyDeck()
        val allHandsEmpty = game.players.all { it.hand.isEmpty() }

        // ФИНАЛЬНЫЙ РАУНД: колода пуста, но у кого-то ещё есть карты
        if (deckIsEmpty && (!allHandsEmpty) && (!isFinalRound)) {
            runFinalRound()
            return
        }
        if (deckIsEmpty && allHandsEmpty) {
            finishGame()
            return
        }
        // Туть обычный ход
        currentAskerIndex = currentTargetIndex
        currentTargetIndex = (currentTargetIndex + 1) % game.players.size

        val asker = game.players[currentAskerIndex]
        var target = game.players[currentTargetIndex]

        // Добираем карты спрашивающему, если нужно
        if (asker.hand.isEmpty() && !game.isEmptyDeck()) {
            val drawn = game.mainDeck.getCards(4)
            updateDeckDisplay()
            asker.addCardsInHand(drawn)
        }

        if (asker.hand.isEmpty()) {
            nextTurn()
            return
        }

        // Ищем цель с картами
        var foundTarget = false
        for (i in 0 until game.players.size) {
            val idx = (currentTargetIndex + i) % game.players.size
            if (idx != currentAskerIndex && game.players[idx].hand.isNotEmpty()) {
                currentTargetIndex = idx
                target = game.players[idx]
                foundTarget = true
                break
            }
        }

        if (!foundTarget) {
            // не у кого спрашивать - игра окончена
            finishGame()
            return
        }
        startTurn(asker, target)
    }

    // исполняет в финальном раунде 1 ход
    private fun executeNextFinalTurn() {
        // Если очередь ходов закончилась => игра окончена
        if (currentFinalTurnIndex >= finalRoundTurns.size) {
            finishGame()
            return
        }

        val (asker, target) = finalRoundTurns[currentFinalTurnIndex]

        // Защита: если к моменту хода у кого-то закончились карты, пропускаем ход
        if (asker.hand.isEmpty() || target.hand.isEmpty()) {
            currentFinalTurnIndex++
            executeNextFinalTurn()
            return
        }

        currentAsker = asker
        currentTarget = target
        currentDialog = Dialog(asker, target)
        view.updateGameState(game.players, asker, target)
        showCurrentQuestion()
    }

    // для View, когда то пишет на экране что надо сделать игроку
    private fun showCurrentQuestion() {
        val dialog = currentDialog ?: return
        val state = dialog.getCurrentState()

        when (state) {
            DialogState.ASK_NOMINAL -> view.showNominalQuestionInput()
            DialogState.ASK_QUANTITY -> view.showQuantityQuestionInput(dialog.getMaxQuantity())
            DialogState.ASK_SUITS -> view.showSuitsQuestionInput(dialog.getAvailableSuits())
            DialogState.ANSWER_NOMINAL,
            DialogState.ANSWER_QUANTITY,
            DialogState.ANSWER_SUITS -> view.showAnswerDialog(dialog.getCurrentQuestion())
            else -> view.showMessage("Ошибка")
        }
    }

    // они вызываются во View, когда там что-то происходит
    override fun onNominalQuestionSubmitted(nominal: Nominal) {
        val result = currentDialog?.submitInput(listOf(nominal.ordinal), isAsker = true)
        handleDialogResult(result)
    }

    override fun onQuantityQuestionSubmitted(quantity: Int) {
        val result = currentDialog?.submitInput(listOf(quantity))
        handleDialogResult(result)
    }

    override fun onSuitsQuestionSubmitted(suits: List<Suit>) {
        val suitIndices = suits.map { it.ordinal + 1 }
        val result = currentDialog?.submitInput(suitIndices)
        handleDialogResult(result)
    }

    override fun onAnswerSubmitted(answer: Answer) {
        val answerValue = if (answer == Answer.YES) 0 else 1
        val result = currentDialog?.submitInput(listOf(answerValue), isAsker = false)
        handleDialogResult(result)
    }

    private fun updateDeckDisplay() {
        view.updateDeckSize(game.mainDeck.size)
    }

    // то место где происходит взаимодействие Presenter с View по результату Dialog
    // + логика игры (передать карты, забрать, взять из колоды) мб ее ваще в Game надо... ну пусть тут будет
    private fun handleDialogResult(state: DialogState?) {
        val dialog = currentDialog ?: run {
            return
        }
        game.historyOfTurns.add(dialog.getCurrentLog())

        when (state) {
            DialogState.GUESSED -> {
                val cards = dialog.getGuessedCards()
                currentTarget?.removeCardsFromHand(cards)
                currentAsker?.addCardsInHand(cards)
                view.showMessage("Угадал! +${cards.size} карт")
                afterTurnComplete()
            }

            DialogState.NOT_GUESSED -> {
                if (!game.mainDeck.deckIsEmpty()) {
                    val card = game.mainDeck.getCards(1)
                    currentAsker?.addCardsInHand(card)
                    view.showMessage("Не угадал! Взял карту из колоды")
                    updateDeckDisplay()
                } else {
                    view.showMessage("Не угадал! Колода пуста")
                }
                afterTurnComplete()
            }

            DialogState.ASK_AGAIN, DialogState.ANSWER_AGAIN -> {
                view.showMessage("Блеф! Попробуй ещё раз")
                showCurrentQuestion()
            }

            DialogState.ERROR -> {
                view.showMessage("Ошибка! Ход заново")
                startTurn(currentAsker!!, currentTarget!!)
            }
            else -> {
                println("Другое состояние: $state, показываем следующий вопрос")
                showCurrentQuestion()
            }
        }
    }


    private fun finishGame() {
        game.status = GameStatus.FINISHED
        val maxBoxes = game.players.maxOfOrNull { it.quantityOfBoxes } ?: 0
        val winners = game.players.filter { it.quantityOfBoxes == maxBoxes }
        game.winner = winners.firstOrNull()

        // записываем игроков в базу данных
        game.players.forEach { player ->
            val isWinner = winners.any { it.name == player.name }
            repository.recordPlayerGame(player.name, isWinner)
        }
        // запись игры в базу данных
        val playersStr = game.players.joinToString(", ") { it.name }
        val historyStr = game.historyOfTurns.joinToString(" | ")
        repository.saveGame(game.id, game.winner?.name, playersStr, historyStr)

        //---------------------------------------------------------------

        val resultMessage = buildString {
            append("🏆 ИГРА ОКОНЧЕНА 🏆\n\n")
            append("═".repeat(30))
            append("\n\n")
            for (player in game.players) {
                append("👤 ${player.name}\n")
                append("   🧰 Сундуков: ${player.quantityOfBoxes}\n")
                append("   🃏 Карт в руке: ${player.hand.size}\n\n")
            }
            append("═".repeat(30))
            append("\n\n")
            if (winners.size > 1) {
                append("🤝 НИЧЬЯ!\n")
                append("Победители: ${winners.joinToString { it.name }}\n")
                append("Сундуков: $maxBoxes")
            } else {
                append("👑 ПОБЕДИТЕЛЬ: ${game.winner?.name}\n")
                append("🏆 Сундуков: $maxBoxes")
            }
        }

        JOptionPane.showMessageDialog(
            null,
            resultMessage,
            "Результаты игры",
            JOptionPane.INFORMATION_MESSAGE
        )

        isFinalRound = false
        finalRoundTurns.clear()
        currentFinalTurnIndex = 0
        view.updateGameState(emptyList(), null, null)
        view.clearGameState()
        view.updateDeckSize(0)
        view.showMenu()
    }

    private fun runFinalRound() {
        view.showMessage("ФИНАЛЬНЫЙ РАУНД! Колода пуста, последние ходы...")
        isFinalRound = true
        // Запоминаем стартовую позицию
        val startIndex = currentAskerIndex
        var finalTurnCount = 0

        for (i in 0 until game.players.size) {
            if (game.status != GameStatus.IN_PROGRESS) break

            val askerIdx = (startIndex + i) % game.players.size
            val asker = game.players[askerIdx]

            if (asker.hand.isEmpty()) {
                continue
            }

            // Ищем цель с картами
            var targetIdx: Int? = null
            for (j in 1 until game.players.size) {
                val candidate = (askerIdx + j) % game.players.size
                if (game.players[candidate].hand.isNotEmpty()) {
                    targetIdx = candidate
                    break
                }
            }

            if (targetIdx != null) {
                finalTurnCount++
                val target = game.players[targetIdx]

                // Сохраняем текущие индексы
                val savedAskerIndex = currentAskerIndex
                val savedTargetIndex = currentTargetIndex

                currentAskerIndex = askerIdx
                currentTargetIndex = targetIdx

                startTurn(asker, target)

                currentAskerIndex = savedAskerIndex
                currentTargetIndex = savedTargetIndex
            } else {
                view.showMessage("${asker.name} пропускает (не у кого спрашивать)")
            }
        }

        if (finalTurnCount == 0) {
            finishGame()
        }
    }

    override fun onGameInitialized(playerNames: List<String>, cards: MutableList<Card>) {
        game.id = repository.getNextGameId()
        game.historyOfTurns.clear()
        game.players.clear()
        game.mainDeck = Deck()
        game.status = GameStatus.WAITING
        game.winner = null
        game.historyOfTurns.clear()

        isFinalRound = false
        game.initialiseNewDeck(cards)

        for (player in playerNames){
            game.addPlayer(Player(player))
        }
        game.initialisation(4)
        updateDeckDisplay()

        currentAskerIndex = 0
        currentTargetIndex = 1

        view.updateGameState(game.players, game.players.getOrNull(0), game.players.getOrNull(1))
        startTurn(game.players[0], game.players[1])

    }

    override fun onMenuRequested() {
    }

    override fun onHistoryRequested() {
        val games = repository.getGameHistory()
        val formatted = games.map { g ->
            "🎲 Игра #${g.id} | 📅 ${g.date.split("T")[0]} | 🏆 ${g.winner ?: "Ничья"} | 👥 ${g.players}"
        }
        view.showGameHistory(formatted)
    }

}
