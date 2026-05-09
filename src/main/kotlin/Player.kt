package boxGame

class Player(val name: String) {
    val hand = mutableListOf<Card>()
    var quantityOfBoxes = 0
        private set

    fun addCardsInHand(cards: List<Card>) {
        hand.addAll(cards)
        manageBoxes()
    }
    // функция, которая считает кол-во сундуков в руке
    private fun countBoxesInHand(): Int {
        val cardsByNominal = hand.groupBy { it.nominal }
        var boxes = 0
        for ((nominal, cards) in cardsByNominal) {
            boxes += cards.size / 4
        }
        return boxes
    }
        // функция, которая убирает все сундучки из руки (они больше никак не используются)
        private fun removeBoxesFromHand(){
            val cardsByNominal = hand.groupBy { it.nominal }
            for ((nominal, cards) in cardsByNominal) {
                if (cards.size == 4){
                    val removeNominal = cards[0].nominal
                    hand.removeAll { it.nominal == removeNominal }
                }
            }
}
    // функция, которая менеджерит кол-во сундуков в руке игрока
    private fun manageBoxes(){
        quantityOfBoxes += countBoxesInHand()
        removeBoxesFromHand()
    }
    fun removeCardsFromHand(cards: List<Card>) {
        hand.removeAll(cards)
    }
    // функции, которые будут использоваться для проверки валидности вопроса (
    // то есть может ли игрок спросить этот номинал)
    fun getCardsByNominal(nominal: Nominal): List<Card> {
        return hand.filter { it.nominal == nominal }
    }
    // эта нужна чтобы проверять отвечающего игрока (все ли правильные масти он ответил)
    fun getCardsBySuit(suit: Suit): List<Card> {
       return hand.filter { it.suit == suit }
    }
}