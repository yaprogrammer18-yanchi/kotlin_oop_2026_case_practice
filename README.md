## Администрирование карточной игры "Сундучок" на Kotlin
Десктоп приложение, позволяющее администрировать процесс игры в рамках одной партии

Студент: Кальсина Яна

Правила игры:

Каждому игроку раздается по 4-7 карт
Игра идет по кругу
Есть закрытая (общая) колода

Ход игрока:
- у него есть карты на руках
- он задает вопрос следующему игроку, есть ли у него карты номинала (который есть в колоде текущего игрока)

- игрок, к которому обращаются, отвечает да или нет
  если нет , то текущий игрок берет карту из общей колоды

- если карты есть, то игрок пытается угадать количество этих карт
- если не угадывает, то берет карту из колоды, игра продолжается
- если угадывает, пытается угадать масти

- пытается угадать масти
- сколько угадал, столько и забрал (например если он назвал даму черви и пики, но у игрока на руках только дама пики, то текущий игрок забирает только даму пики)
- если не угадал, то опять же берется карта из колоды

когда заканчивается общая колода, игроки доигрывают последний круг
и подсчитывают кол-во сундучков у каждого игрока
подсчет идет в конце игры




### Архитектура приложения


#### Диаграммы классов 
```mermaid
classDiagram
%% ===== ENUMS =====
class GameStatus {
<<enumeration>>
WAITING
IN_PROGRESS
PAUSED
FINISHED
}
    class DialogState {
        <<enumeration>>
        ASK_NOMINAL
        ANSWER_NOMINAL
        ASK_QUANTITY
        ANSWER_QUANTITY
        ASK_SUITS
        ANSWER_SUITS
        GUESSED
        NOT_GUESSED
        ASK_AGAIN
        ANSWER_AGAIN
        ERROR_QUESTION
        ERROR
    }

    class Answer {
        <<enumeration>>
        +val displayName: String
        YES
        NO
        +fun fromDisplayName(displayName: String?): Answer?
    }
    
    class Suit {
        <<enumeration>>
        +val displayName: String
        HEARTS
        DIAMONDS
        CLUBS
        SPADES
        +fun fromDisplayName(displayName: String?): Suit?
    }
    
    class Nominal {
        <<enumeration>>
        +val displayName: String
        SIX
        SEVEN
        EIGHT
        NINE
        TEN
        JACK
        QUEEN
        KING
        ACE
        +fun fromDisplayName(displayName: String?): Nominal?
    }
    
    %% ===== DOMAIN CLASSES =====
    class Card {
        +val nominal: Nominal
        +val suit: Suit
    }
    
    class Player {
        +val name: String
        +val hand: MutableList~Card~
        +var quantityOfBoxes: Int «private set»
        +fun addCardsInHand(cards: List~Card~): Unit
        -fun countBoxesInHand(): Int
        -fun removeBoxesFromHand(): Unit
        -fun manageBoxes(): Unit
        +fun removeCardsFromHand(cards: List~Card~): Unit
        +fun getCardsByNominal(nominal: Nominal): List~Card~
        +fun getCardsBySuit(suit: Suit): List~Card~
    }
    
    class Deck {
        +var cards: MutableList~Card~
        +val size: Int
        +fun deckIsEmpty(): Boolean
        +fun getCards(quantity: Int): List~Card~
        +fun initialiseDeck(cardsInRealDeck: MutableList~Card~): Unit
    }
    
    %% ===== GAME LOGIC CLASSES =====
    class Dialog {
        +var asker: Player
        +var target: Player
        +var assumeNominal: Nominal?
        +var answerForNominal: Answer?
        +var assumeQuantity: Int
        +var realQuantity: Int
        +var answerForQuantity: Answer?
        +var assumeSuits: List~Suit~
        +var realSuits: List~Suit~
        +var answerForSuits: Answer?
        +var guessedCards: List~Card~
        +var state: DialogState
        +var guessedNominal: Boolean
        +var guessedQuantity: Boolean
        +var guessedSuits: Boolean
        +fun questionNominal(nominal: Nominal?): DialogState
        +fun answerNominal(answer: Answer?): DialogState
        +fun checkNominalValidation(player: Player): Boolean
        +fun questionQuantity(quantity: Int): DialogState
        +fun answerQuantity(answer: Answer?): DialogState
        +fun checkQuantityValidation(player: Player, assumquantity: Int): Boolean
        +fun questionSuits(suits: List~Suit~): DialogState
        +fun answerSuits(answer: Answer?): DialogState
        +fun checkSuitsValidationForTarget(): Boolean
        +fun checkSuitsValidationForAsker(): Boolean
        +fun suitsToGive(): Unit
    }
    
    class Turn {
        +val dialog: Dialog?
        +fun toLogString(): String
    }
    
    class Game {
        +val id: Int
        -var status: GameStatus
        +val players: MutableList~Player~
        +var mainDeck: Deck
        +var currentAskerPlayerIndex: Int «private set»
        -var currentTargetPlayerIndex: Int
        -var currentTurn: Turn?
        +var winner: Player? «private set»
        +val historyOfTurns: MutableList~Turn~
        -var currentDialog: Dialog?
        +fun addPlayer(player: Player): Unit
        +fun isEmptyDeck(): Boolean
        +fun forceEndGame(): Unit
        +fun initialiseNewDeck(): Unit
        +fun initialisation(cardsPerPlayer: Int = 4): Unit
        +fun runTurn(): Unit
        +fun runGame(): Unit
    }
    
    %% ===== RELATIONSHIPS =====
    Game "1" *-- "2..*" Player : contains
    Game "1" *-- "1" Deck : owns
    Game "1" *-- "0..*" Turn : historyOfTurns
    Game "1" o-- "0..1" Dialog : currentDialog
    Game ..> GameStatus : status
    
    Player "1" *-- "0..*" Card : hand
    Player ..> Nominal : filter by
    Player ..> Suit : filter by
    
    Deck "1" *-- "0..*" Card : cards
    
    Dialog "1" --> "2" Player : asker & target
    Dialog ..> DialogState : state machine
    Dialog ..> Answer : validation
    Dialog ..> Nominal : guess nominal
    Dialog ..> Suit : guess suits
    Dialog "1" *-- "0..*" Card : guessedCards
    
    Turn "1" o-- "0..1" Dialog : logs dialog
    
    Card --> Nominal : has nominal
    Card --> Suit : has suit
```

## Описание архитектуры

### 1. Game — главный управляющий класс
Управляет всей партией: от начала до завершения.

#### Поля
- `id` — идентификатор игры
- `status` — состояние (ожидание, идёт, пауза, завершена)
- `mainDeck: Deck` — основная колода
- `players: List<Player>` — список игроков (меняется редко)
- `currentPlayer: Int` — индекс текущего игрока
- `cardsQuantity` — сколько карт в начале игры
- `currentTurn: Turn` — текущий ход
- `winner` — победитель
- `historyOfTurns: MutableList<Turn>` — история всех ходов

#### Методы
- `isEmptyDeck()` — проверка, не пуста ли колода
- `checkWinCondition()` — проверка победы
- `forceEndGame()` — принудительное завершение (для админа)
- `nextTurn()` — переключение на следующий ход
- `initialisation()` — подготовка игры перед стартом

**Что делает Game:**
- вызывает Turn
- отвечает за смену ходов
- хранит победителя
- в завершении игры от объекта Game собирается статистика

---

### 2. Player — игрок

#### Зачем нужен
Хранит состояние одного игрока.

#### Поля
- `name`
- `hand: MutableList<Card>` — карты на руке
- `quantityOfBoxes` — сколько сундуков заработал

#### Методы
- `countBoxes()` — пересчёт сундуков
- `addCardsInHand(cards, quantity)`
- `removeCardsFromHand(cards, quantity)`

---

### 3. Deck — колода

#### Зачем нужна
Управляет картами в игре.

#### Поля
- `cards: List<Card>` — карты в колоде
- `allCards` — общее количество
- `currentQuantity` — сколько осталось

#### Метод
- `getCard(quantity: Int): List<Card>` — получить N карт (для раздачи или перемещения)
---

## 4. Card — карта

### Поля
- `nominal` — 6,7,8…Ace
- `suit` — масть

---

### 5. Turn — ход (самый важный класс)

- знает, кто спросил
- кто ответил
- какой был вопрос / ответ
- успешен ли ход
- какие карты перемещаются

#### Поля
- `askingPlayer` — кто спросил
- `answeringPlayer` — кого спросили
- `question: Question?`
- `answer: Answer?`
- `isCompleted` — завершён ли ход
- `successful` — успешен (получил сундук или нет)
- `moveCards: MutableList<Card>` — карты, которые перемещаются в ходе

#### Методы (логика хода)
- `checkNominal(q, a)` — совпадает ли номинал
- `checkQuantity(q, a)` — совпадает ли количество
- `checkSuits(q, a)` — проверка мастей
- `isEnoughCards()` — достаточно ли карт у отвечающего
- `getResultFromQuestionAndAnswer()` — итоговый результат хода
- `moveCards(askingPlayer, answeringPlayer, cards)` — перемещение карт

(как только какой-то из методов проверок возвращает false (спрашивающий игрок не
угадал, то successful становится fasle, далее вызывается askingPlayer.getCardFromDeck), если игрок все же 
угадал, то successful становится true, далее вызывается moveCards, внутри которого вызываются методы
обмена картами из класса Player)
---

### 6. Dialog
Поля:
- `nominal`
- `quantity`
- `suits: List<String>`



#### Логика работы Game-Turn-Dialog
Game вызывает метод nextTurn(), 
который в свою очередь создает объект класса Turn

Game создает объект диалога, который заполняется от внешнего взаимодействия (пользователь тыкает на кнопочки)

Что делает Turn:
он хранит историю в письменном виде (кто что спросил, угадал, не угадал, какие карты перешли или же 
была взята какая то (какая?) карта из колоды), game просто передает туда dialog и turn формирует строчку в истории


Сам Dialog только проверяет валидность заданного вопроса и полученного ответа (фильтр на блеф)
и также определяет дальнейшие действия игроков (формирует список карт, которые надо передать) или же
информация о том, что надо взять карту из колоды
