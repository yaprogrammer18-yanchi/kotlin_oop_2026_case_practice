package boxGame

class Deck {
    var cards = mutableListOf<Card>()
    val size: Int get() = cards.size

    fun deckIsEmpty(): Boolean{
        return cards.isEmpty()
    }

    // функция, которая берет карты из колоды и возвращает их во вне
    fun getCards(quantity: Int): List<Card>{
        val cardsToGive = mutableListOf<Card>()

        if (quantity >= size){
            return cards.toList()
        }
        for (i in 1 .. quantity){
            cardsToGive.add(cards.removeAt(0))
        }
        return cardsToGive
    }
    // каким-то внешним классом вводить карты из реальной колоды, формировать список карт, а сюда отправлять уже готовый список
    fun initialiseDeck(cardsInRealDeck: MutableList<Card>){
        cards.addAll(cardsInRealDeck)
    }
}
