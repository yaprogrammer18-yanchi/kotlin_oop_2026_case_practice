package boxGame


abstract class Phase(){
    abstract fun communicate(arr: List<Int>, pl: Player): Phase?
    abstract fun checkValidation(player: Player, instance: Int): Boolean // переход к след фазе
    abstract fun toLogString(): String
}

class NominalQuestion(val asker: Player, val target: Player): Phase(){
    private var nominal: Nominal? = null

    override fun communicate(arr: List<Int>, pl: Player): Phase?{
        nominal = Nominal.values().getOrNull(arr[0])
        val isCorrect = checkValidation(asker, arr[0])
        return if (isCorrect) {
            NominalAnswer(askedNominal = nominal!!, asker, target)
        } else {
            NominalQuestion(asker, target)
        }
    }
    override fun checkValidation(player: Player, instance: Int): Boolean {
        val assumeNominal = Nominal.values().getOrNull(instance)
        val arr: List<Card> = player.getCardsByNominal(assumeNominal!!)
        return arr.isNotEmpty()
    }
    override fun toLogString(): String {
        return "${asker.name} -> ${target.name}. Есть ${nominal}?"
    }
}

class NominalAnswer(val askedNominal: Nominal, val asker: Player, val target: Player): Phase(){
    private var answerForNominal: Answer? = null
    override fun communicate(arr: List<Int>, pl: Player): Phase? {
        answerForNominal = Answer.values().getOrNull(arr[0])
        val isTruth = checkValidation(pl, arr[0])
        return if (isTruth) {
            if (answerForNominal == Answer.YES) {
                QuantityQuestion(askedNominal, asker, target)
            } else {
                null
            }
        } else {
            NominalAnswer(askedNominal, asker, target)
        }
    }
    override fun checkValidation(player: Player, instance: Int): Boolean {
        // instance: 0 = YES, 1 = NO
        val hasCard = player.getCardsByNominal(askedNominal).isNotEmpty()
        if ((instance == 0) && hasCard ){
            return true
        }
        else if ((instance == 1) && (!hasCard)) {
            return true
        }
        return false
    }
    override fun toLogString(): String = "${if (answerForNominal == Answer.YES) "Да. " else "Нет. "}"
}

class QuantityQuestion(private val askedNominal: Nominal, val asker: Player, val target: Player) : Phase() {
    private var assumeQuantity: Int = 0
    override fun communicate(arr: List<Int>, pl: Player): Phase {
        assumeQuantity = arr[0]

        if (checkValidation(asker, assumeQuantity)){
            return QuantityAnswer(askedNominal, assumeQuantity, asker, target)
        }
        else{
            return QuantityQuestion(askedNominal, asker, target)
        }

    }
    override fun checkValidation(player: Player, instance: Int): Boolean {
        return instance in 1..4
    }
    override fun toLogString(): String {
        return "${assumeQuantity}? "
    }
}

class QuantityAnswer(
    private val askedNominal: Nominal,
    private val assumeQuantity: Int,
    val asker: Player, val target: Player
) : Phase() {
    private var realQuantity: Int = 0
    override fun communicate(arr: List<Int>, pl: Player): Phase? {
        val answer = if (arr[0] == 0) Answer.YES else Answer.NO
        val isValid = checkValidation(pl, arr[0])

        if (isValid && answer == Answer.YES) {

            return SuitsQuestion(askedNominal, assumeQuantity, asker, target)
        }
        if (isValid && answer == Answer.NO) {
            return null
        }
        return QuantityAnswer(askedNominal, assumeQuantity, asker, target)
    }
    override fun checkValidation(player: Player, instance: Int): Boolean {
        realQuantity = player.getCardsByNominal(askedNominal).size

        if (instance == 0 && realQuantity == assumeQuantity) return true  // Answer.YES
        if (instance == 1 && realQuantity != assumeQuantity) return true  // Answer.NO
        return false
    }
    override fun toLogString(): String {
        val guessed = realQuantity == assumeQuantity
        return "${if (guessed) "Да. " else "Нет. "}" }

}

class SuitsQuestion(
    private val askedNominal: Nominal,
    private val askedQuantity: Int,
    val asker: Player,
    val target: Player
) : Phase() {
    private var askedSuits: List<Suit> = emptyList()
    override fun communicate(arr: List<Int>, pl: Player): Phase {
        val askedSuits = arr.mapNotNull { index ->
            when(index) {
                1 -> Suit.HEARTS
                2 -> Suit.DIAMONDS
                3 -> Suit.CLUBS
                4 -> Suit.SPADES
                else -> null
            }
        }
        if (askedSuits.size != askedQuantity) {
            return SuitsQuestion(askedNominal, askedQuantity, asker, target)  // повтор
        }
        return SuitsAnswer(askedNominal, askedQuantity, askedSuits, asker, target)
    }
    override fun checkValidation(player: Player, instance: Int): Boolean {
        return askedSuits.size == askedQuantity
    }
    override fun toLogString(): String {
        return "${askedSuits.joinToString { it.displayName }}? "
    }
}

class SuitsAnswer(
    private val askedNominal: Nominal,
    private val askedQuantity: Int,
    val askedSuits: List<Suit>,
    val asker: Player,
    val target: Player

) : Phase() {
    private var guessedCards: List<Card> = emptyList()
    override fun communicate(arr: List<Int>, pl: Player): Phase? {
        val answer = if (arr[0] == 0) Answer.YES else Answer.NO
        val isValid = checkValidation(target, arr[0])  // проверяем у target

        if (isValid && answer == Answer.YES) {
            val realSuits = target.getCardsByNominal(askedNominal).map { it.suit }
            val matchedSuits = askedSuits.intersect(realSuits).toList()
            guessedCards = target.getCardsByNominal(askedNominal).filter { it.suit in matchedSuits }
            target.removeCardsFromHand(guessedCards)
            asker.addCardsInHand(guessedCards)
            return null
        }

        if (isValid && answer == Answer.NO){
            return null
        }

        return SuitsAnswer(askedNominal, askedQuantity, askedSuits, asker, target)  // повтор
    }
    fun getGuessedCards(): List<Card> = guessedCards
    override fun checkValidation(player: Player, instance: Int): Boolean {
        val realSuits = player.getCardsByNominal(askedNominal).map { it.suit }
        if (instance == 0) {  // Answer.YES
            return realSuits.any { it in askedSuits }
        } else {  // Answer.NO
            return realSuits.none { it in askedSuits }
        }
    }
    override fun toLogString(): String {
        return if (guessedCards.isNotEmpty()) {
            "Да, ${guessedCards.map { it.suit.displayName }.joinToString()}."
        } else {
            "Нет."
        }
    }
}