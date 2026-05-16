package boxGame

data class Turn(
    val dialog: Dialog? = null
) {
    fun toLogString(): String {
        val d = dialog ?: return "Ошибка: диалог отсутствует"

        return buildString {
            append("${d.asker.name} → ${d.target.name}: ")
            append("Есть ${d.assumeNominal?.displayName ?: "?"}? ")

            if (d.guessedNominal) {
                append("Да. ")

                append("Их ${d.assumeQuantity}? ")

                if (d.guessedQuantity) {
                    append("Да. ")

                    if (d.assumeSuits.isNotEmpty()) {
                        append("Масти ${d.assumeSuits.joinToString { it.displayName }}? ")

                        if (d.guessedSuits) {
                            val guessedSuitsList = d.guessedCards.map {it.suit}
                            if (guessedSuitsList.isNotEmpty()) {
                                append("Да, ${guessedSuitsList.joinToString { it.displayName }}")
                                    // стоит ли выводить (раскрывать возможно неугаданные масти?)
                            } else {
                            append("Нет")
                        }
                    }

                    val guessedSuitsList = d.guessedCards.map {it.suit} ?: emptyList()
                    append(" → ${if (guessedSuitsList.isNotEmpty()) "УГАДАЛ" else "❌ НЕ УГАДАЛ"}")

                    if (guessedSuitsList.isNotEmpty()) {
                        append(" (переходят ${guessedSuitsList.size} карт)")
                    }
                } else {
                    append("Нет (сказал ${d.realQuantity}) → ❌ НЕ УГАДАЛ")
                }
            } else {
                append("Нет → ❌ НЕ УГАДАЛ")
            }
        }else {
                append("Нет → ❌ НЕ УГАДАЛ")
            }
    }
}
}


// gard closes типо от противного идти
// state.tostring проходиться for и отрисовывать состояния
// переместить в диалог
// сделать так, чтобы отрисовывались состояния каждого стат, чтобы у них была такая функция и можно было пройтись for по нему