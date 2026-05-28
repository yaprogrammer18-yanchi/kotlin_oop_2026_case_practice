## Администрирование карточной игры "Сундучок" на Kotlin
Десктоп приложение, позволяющее администрировать процесс игры в рамках одной партии

Студент: Кальсина Яна

Правила игры:

Каждому игроку раздается по 4 карты
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
    direction TB

    %% ==================== STYLES ====================
    classDef domain fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,color:#000
    classDef dialog fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px,color:#000
    classDef ui fill:#fff3e0,stroke:#ef6c00,stroke-width:2px,color:#000
    classDef enum fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px,color:#000,font-style:italic

    %% ==================== DOMAIN MODEL ====================
    subgraph Domain Model
        class Card {
            +nominal: Nominal
            +suit: Suit
        }
        class Player {
            -name: String
            -hand: MutableList~Card~
            -quantityOfBoxes: Int
            +addCardsInHand(cards: List~Card~) void
            +removeCardsFromHand(cards: List~Card~) void
            -manageBoxes() void
        }
        class Deck {
            -cards: MutableList~Card~
            +size: Int
            +deckIsEmpty() Boolean
            +getCards(quantity: Int) List~Card~
            +initialiseDeck(cardsInRealDeck: MutableList~Card~) void
        }
        class Game {
            +id: Int
            -status: GameStatus
            -players: MutableList~Player~
            -mainDeck: Deck
            +winner: Player
            +historyOfTurns: MutableList~String~
            +addPlayer(player: Player) void
            +initialiseNewDeck(cards: MutableList~Card~) void
            +initialisation(cardsPerPlayer: Int) void
        }
        class GameStatus {
            <<enumeration>>
            WAITING
            IN_PROGRESS
            FINISHED
        }
        class Suit {
            <<enumeration>>
            HEARTS
            DIAMONDS
            CLUBS
            SPADES
        }
        class Nominal {
            <<enumeration>>
            SIX
            SEVEN
            EIGHT
            NINE
            TEN
            JACK
            QUEEN
            KING
            ACE
        }
    end

    %% ==================== DIALOG & PHASE SYSTEM ====================
    subgraph Dialog Phase System
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
            YES
            NO
        }
        class Dialog {
            +asker: Player
            +target: Player
            -currentPhase: Phase
            -resultCards: List~Card~
            +submitInput(input: List~Int~, isAsker: Boolean) DialogState
    
        }
        class Phase {
            <<abstract>>
            +communicate(arr: List~Int~, pl: Player) Phase
            +checkValidation(player: Player, instance: Int) Boolean
            +toLogString() String
        }
        class NominalQuestion {
            +asker: Player
            +target: Player
            -nominal: Nominal
        }
        class NominalAnswer {
            +askedNominal: Nominal
            +asker: Player
            +target: Player
            -answerForNominal: Answer
        }
        class QuantityQuestion {
            +asker: Player
            +target: Player
            -askedNominal: Nominal
            -assumeQuantity: Int
        }
        class QuantityAnswer {
            +asker: Player
            +target: Player
            -askedNominal: Nominal
            -assumeQuantity: Int
            -realQuantity: Int
        }
        class SuitsQuestion {
            +asker: Player
            +target: Player
            -askedNominal: Nominal
            -askedQuantity: Int
            -askedSuits: List~Suit~
        }
        class SuitsAnswer {
            +asker: Player
            +target: Player
            -askedNominal: Nominal
            -askedQuantity: Int
            -askedSuits: List~Suit~
            -guessedCards: List~Card~
            +getGuessedCards() List~Card~
        }
    end

    %% ==================== MVP LAYER ====================
    subgraph MVP UI Layer
        class GameView {
            <<interface>>
            +showNominalQuestionInput() void
            +showQuantityQuestionInput(maxQuantity: Int) void
            +showSuitsQuestionInput(availableSuits: List~String~) void
            +showAnswerDialog(message: String) void
            +showMessage(message: String) void
            +showGameSetup() void
            +showMenu() void
            +showPlayerStats(stats: Map~String,Int~) void
            +showGameHistory(history: List~String~) void
            +setListener(listener: GameViewListener) void
            +close() void
        }

        class GameViewImpl {
            -listener: GameViewListener
            -cardLayout: CardLayout
            -mainPanel: JPanel
            -quantitySpinner: JSpinner
            -suitsCheckboxes: List~JCheckBox~
            -answerLabel: JLabel
            -messageLabel: JLabel
            -statsTextArea: JTextArea
            -historyListModel: DefaultListModel~String~
            -statusLabel: JLabel
            -playersPanel: JPanel
            -deckSizeLabel: JLabel
        }
        class GameViewListener {
            <<interface>>
            +onNominalQuestionSubmitted(nominal: Nominal) void
            +onQuantityQuestionSubmitted(quantity: Int) void
            +onSuitsQuestionSubmitted(suits: List~Suit~) void
            +onAnswerSubmitted(answer: Answer) void
            +onGameInitialized(playerNames: List~String~, cards: MutableList~Card~) void
            +onMenuRequested() void
        }
        class Presenter {
            -game: Game
            -view: GameView
            -currentDialog: Dialog
            -currentAsker: Player
            -currentTarget: Player
            -currentAskerIndex: Int
            -currentTargetIndex: Int
            -isFinalRound: Boolean
            -finalRoundTurns: MutableList~Pair~
            -currentFinalTurnIndex: Int
            +startTurn(asker: Player, target: Player) void
            +handleDialogResult(state: DialogState) void
            +nextTurn() void
            +finishGame() void
            +runFinalRound() void
        }
    end

    subgraph Data Persistence Layer
        class GameRepository {
            <<interface>>
            +getNextGameId() Int
            +recordPlayerGame(name: String, isWinner: Boolean) void
            +getPlayerStatsSorted() List~PlayerStats~
            +saveGame(id: Int, winner: String?, players: String, movesHistory: String) void
            +getGameHistory() List~GameEntry~
        }
        class SqliteGameRepository {
            +getNextGameId() Int
            +recordPlayerGame(...) void
            +saveGame(...) void
            +getGameHistory() List~GameEntry~
        }
        class DatabaseManager {
            <<object>>
            +dbUrl: String
            +getConnection() Connection
            -createTables(conn: Connection) void
        }
        class PlayerStats {
            +name: String
            +gamesPlayed: Int
            +wins: Int
        }
        class GameEntry {
            +id: Int
            +date: String
            +winner: String?
            +players: String
            +movesHistory: String
        }
    end

    %% ==================== RELATIONSHIPS ====================
    %% Domain
    Card --> Nominal
    Card --> Suit
    Deck "1" *-- "*" Card : contains
    Player "1" *-- "*" Card : hand
    Game "1" *-- "*" Player : manages
    Game "1" *-- "1" Deck : main
    Game --> GameStatus : tracks
    Game "1" o-- "0..1" Dialog : active turn

    %% Phase Hierarchy
    Phase <|-- NominalQuestion
    Phase <|-- NominalAnswer
    Phase <|-- QuantityQuestion
    Phase <|-- QuantityAnswer
    Phase <|-- SuitsQuestion
    Phase <|-- SuitsAnswer

    %% Dialog Internal
    Dialog "1" *-- "0..1" Phase : current
    Dialog --> DialogState : returns
    Dialog --> Answer : parses
    Dialog ..> Card : yields result

    NominalQuestion --> Nominal
    NominalAnswer --> Nominal
    NominalAnswer --> Answer
    QuantityQuestion --> Nominal
    QuantityAnswer --> Nominal
    QuantityAnswer --> Answer
    SuitsQuestion --> Nominal
    SuitsQuestion --> Suit
    SuitsAnswer --> Nominal
    SuitsAnswer --> Suit
    SuitsAnswer --> Card : guessed

    %% MVP Wiring
    GameView <|.. GameViewImpl : implements
    GameViewListener <|..Presenter : implements
    GameViewImpl ..> GameViewListener : delegates
    Presenter --> Game : controls
    Presenter --> GameView : updates
    Presenter "1" o-- "0..1" Dialog : orchestrates
    Presenter --> Player : observes

 GameRepository <|.. SqliteGameRepository : implements
    SqliteGameRepository --> DatabaseManager : uses
    DatabaseManager --> boxgame.db : reads/writes
    Presenter --> GameRepository : persists data & stats
    GameRepository ..> PlayerStats : returns
    GameRepository ..> GameEntry : returns
```

## Описание архитектуры

Приложение построено по паттерну **MVP (Model-View-Presenter)** 

### Ключевые компоненты

#### Domain Model (Доменная модель)
Чистый слой, не зависящий от UI. Содержит сущности и правила игры.
- `Card`, `Suit`, `Nominal` – данные карты, масти и номиналы. Чистые data-классы и enum.
- `Player` – управляет рукой игрока. Автоматически подсчитывает собранные сундуки (`quantityOfBoxes`) и удаляет полные наборы из руки.
- `Deck` – управляет колодой: инициализация, раздача, взятие карт (`getCards`).
- `Game` – контейнер состояния игры: список игроков, колода, статус (`GameStatus`), история ходов.

#### Система ходов
Реализована через паттерн **State/Phase**. Каждый шаг хода инкапсулирован в отдельный класс.
- `Phase` – абстрактный базовый класс. Определяет контракт: `communicate()` (обработка ввода), 
- `checkValidation()` (проверка правил), 
- `toLogString()` (лог хода).

- `Dialog` – менеджер фаз. Хранит текущую `Phase`, делегирует ей ввод игрока и возвращает `DialogState` для Presenter.
- Наследники `Phase` (`NominalQuestion`, `QuantityAnswer`, `SuitsAnswer` и др.) – содержат логику конкретного шага, правила валидации и переход к следующей фазе или `null` при завершении хода.
Если игрок не угадывает, то возвращается null, а если null возвращается после фазы AnswerSuits, то проверяется список передаваемых карт и если он не пустой, то это значит, что игрок выиграл


#### Presenter (`Presenter` + `GameViewListener`)
Координатор приложения. Связывает UI и модель.
- `GameViewListener` – интерфейс колбэков от UI.
- `Presenter` – реализует слушатель, управляет жизненным циклом `Game` и `Dialog`, принимает решения (`handleDialogResult`), маршрутизирует ходы (`nextTurn`, `runFinalRound`), обрабатывает условия победы, также добирает карты игрокам, когда возникают какие-то проблемы + определяет кто ходит следующим (тоже зависит от состояния руки игрока)
- При получении нового состояния из Dialog, запрашивает изменения у View (точнее буквально говорит ей что делать)

#### View (`GameView` + `GameViewImpl`)
Swing-интерфейс. Отвечает исключительно за отображение и сбор ввода.
- `GameView` – контракт для UI.
- `GameViewImpl` – реализация на Swing (`CardLayout`, панели, кнопки, спиннеры). Делегирует события в `GameViewListener`. **Не знает про правила игры и модель.**
- немного "тупой" класс, просто знает что и как отрисовывать и на что реагировать. Все action(ы) которые делает пользователь (нажимает на кнопочки) анализирует и в нужном виде отправляет в Presenter

#### DataBase и взаимодействие с ней

Все общение с базой данных происходит через Presenter.

Структура: 

GameRepository (интерфейс) описывает контракт операций с данными.

SqliteGameRepository (реализация интерфейса GameRepository) содержит SQL-запросы и возвращает соответствующие результаты. С его помощью можно:
- получить id след игры
- получить историю игр для отображения во view
- сохранить игру
- получить отсортированную статистику игроков
- обновить игрока в таблице статистики (выиграл он, не выиграл, обновить после того как еще одну игру сыграл)

DatabaseManager он отвечает за подключение к бд и создает таблицы (история игр, статистика игроков).
