package boxGame

import java.awt.*
import javax.swing.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

interface GameView {
    fun showNominalQuestionInput()
    fun showQuantityQuestionInput(maxQuantity: Int = 4)
    fun showSuitsQuestionInput(availableSuits: List<String>)
    fun showAnswerDialog(message: String)
    fun showMessage(message: String)
    fun showGameSetup()
    fun showMenu()
    fun showPlayerRegistry(stats: List<PlayerStats>)
    fun showGameHistory(history: List<String>)
    fun setListener(listener: GameViewListener)
    fun close()
    fun updateGameState(players: List<Player>, currentAsker: Player?, currentTarget: Player?)
    fun updateDeckSize(deckSize: Int)
    fun clearGameState()
}

class GameViewImpl : JFrame(), GameView {
    private var listener: GameViewListener? = null
    private val cardLayout = CardLayout()
    private val mainPanel = JPanel(cardLayout)
    private lateinit var quantitySpinner: JSpinner
    private lateinit var suitsCheckboxes: List<JCheckBox>
    private lateinit var answerLabel: JLabel
    private lateinit var messageLabel: JLabel
    private lateinit var statsTextArea: JTextArea
    private lateinit var historyListModel: DefaultListModel<String>
    private lateinit var cardsListModel: DefaultListModel<String>
    private lateinit var statusLabel: JLabel
    private lateinit var playersPanel: JPanel
    private lateinit var deckSizeLabel: JLabel

    init {
        initFrame()
        createPanels()
        showGameSetup()
    }

    private fun initFrame() {
        title = " Сундучок - игра в карты"
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(800, 800)
        setLocationRelativeTo(null)
        layout = BorderLayout()

        val topPanel = JPanel(BorderLayout())

        val topLeftPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        deckSizeLabel = JLabel("📚 Карт в колоде: 0")
        deckSizeLabel.font = Font("Arial", Font.PLAIN, 12)
        deckSizeLabel.foreground = Color(100, 100, 150)
        topLeftPanel.add(deckSizeLabel)

        // Центр - кто ходит, от кого кому ход
        statusLabel = JLabel(" ", SwingConstants.CENTER)
        statusLabel.font = Font("Arial", Font.BOLD, 14)
        statusLabel.foreground = Color(70, 70, 150)

        topPanel.add(topLeftPanel, BorderLayout.WEST)
        topPanel.add(statusLabel, BorderLayout.CENTER)

        playersPanel = JPanel(FlowLayout(FlowLayout.CENTER, 20, 10))
        playersPanel.background = Color(200, 220, 240)
        topPanel.add(playersPanel, BorderLayout.SOUTH)

        add(topPanel, BorderLayout.NORTH)
        add(mainPanel, BorderLayout.CENTER)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                dispose()
            }
        })
    }

    private fun createPanels() {
        createSetupPanel()
        createNominalPanel()
        createQuantityPanel()
        createSuitsPanel()
        createAnswerPanel()
        createMessagePanel()
        createMenuPanel()
        createStatsPanel()
        createHistoryPanel()
    }


    override fun updateGameState(players: List<Player>, currentAsker: Player?, currentTarget: Player?) {
        if (currentAsker != null && currentTarget != null) {
            statusLabel.text = "🎲 Ход: ${currentAsker.name} → ${currentTarget.name} 🎲"
        } else {
            statusLabel.text = "🃏 Игра в процессе 🃏"
        }

        // меняем цвета игроков
        playersPanel.removeAll()
        for (player in players) {
            val playerCard = createPlayerCard(player, player == currentAsker, player == currentTarget)
            playersPanel.add(playerCard)
        }
        playersPanel.revalidate()
        playersPanel.repaint()
    }

    override fun updateDeckSize(deckSize: Int) {
        deckSizeLabel.text = "📚 Карт в колоде: $deckSize"
        if (deckSize == 0) {
            deckSizeLabel.foreground = Color(200, 50, 50)
            deckSizeLabel.font = Font("Arial", Font.BOLD, 12)
        } else {
            deckSizeLabel.foreground = Color(100, 100, 150)
            deckSizeLabel.font = Font("Arial", Font.PLAIN, 12)
        }
    }

    private fun createPlayerCard(player: Player, isAsker: Boolean, isTarget: Boolean): JPanel {
        val card = JPanel()
        card.layout = BoxLayout(card, BoxLayout.Y_AXIS)
        card.preferredSize = Dimension(140, 90)
        card.background = when {
            isAsker && isTarget -> Color(200, 200, 255)
            isAsker -> Color(200, 255, 200)
            isTarget -> Color(255, 200, 200)
            else -> Color(240, 240, 240)
        }
        card.border = BorderFactory.createLineBorder(Color.BLACK, 1)

        val nameLabel = JLabel(player.name, SwingConstants.CENTER)
        nameLabel.font = Font("Arial", Font.BOLD, 13)
        nameLabel.alignmentX = Component.CENTER_ALIGNMENT

        val cardsLabel = JLabel("🃏 ${player.hand.size} карт", SwingConstants.CENTER)
        cardsLabel.font = Font("Arial", Font.PLAIN, 11)
        cardsLabel.alignmentX = Component.CENTER_ALIGNMENT

        val boxesLabel = JLabel("🧰 ${player.quantityOfBoxes} сундуков", SwingConstants.CENTER)
        boxesLabel.font = Font("Arial", Font.PLAIN, 11)
        boxesLabel.alignmentX = Component.CENTER_ALIGNMENT

        card.add(nameLabel)
        card.add(Box.createVerticalStrut(5))
        card.add(cardsLabel)
        card.add(boxesLabel)

        return card
    }

    private fun createSetupPanel() {
        val panel = JPanel(GridBagLayout())
        panel.background = Color(240, 248, 255)
        val gbc = GridBagConstraints()
        gbc.insets = Insets(10, 10, 10, 10)
        gbc.fill = GridBagConstraints.HORIZONTAL


        val titleLabel = JLabel("⚙️ НАСТРОЙКИ ИГРЫ ⚙️")
        titleLabel.font = Font("Arial", Font.BOLD, 20)
        titleLabel.foreground = Color(70, 70, 200)
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        panel.add(titleLabel, gbc)


        gbc.gridy = 1
        gbc.gridwidth = 2
        val playersContainer = JPanel()
        playersContainer.layout = BoxLayout(playersContainer, BoxLayout.Y_AXIS)
        val playerFields = mutableListOf<JTextField>()


        val p1Field = JTextField("Игрунчик 1", 15)
        playerFields.add(p1Field)
        playersContainer.add(JLabel("☺Игрок 1:"))
        playersContainer.add(p1Field)
        playersContainer.add(Box.createVerticalStrut(5))


        val p2Field = JTextField("Игрунчик 2", 15)
        playerFields.add(p2Field)
        playersContainer.add(JLabel("☺Игрок 2:"))
        playersContainer.add(p2Field)
        playersContainer.add(Box.createVerticalStrut(5))
        panel.add(playersContainer, gbc)


        gbc.gridy = 2
        val addButton = JButton("✚ Добавить игрока")
        addButton.background = Color(180, 180, 250)
        addButton.addActionListener {
            val newField = JTextField("Игрок ${playerFields.size + 1}", 15)
            playerFields.add(newField)
            playersContainer.add(JLabel("Игрок ${playerFields.size}:"))
            playersContainer.add(newField)
            playersContainer.add(Box.createVerticalStrut(5))
            playersContainer.revalidate()
            playersContainer.repaint()
            panel.revalidate()
            panel.repaint()
            // надо добавить кнопку, чтобы убрать игрока
        }
        panel.add(addButton, gbc)

        gbc.gridy = 3
        panel.add(JSeparator(), gbc)


        val deckLabel = JLabel("🎴 СОЗДАНИЕ КОЛОДЫ 🎴")
        deckLabel.font = Font("Arial", Font.BOLD, 14)
        gbc.gridy = 4
        panel.add(deckLabel, gbc)


        gbc.gridy = 5
        val selectorPanel = JPanel(FlowLayout())
        val nominalCombo = JComboBox(Nominal.values().map { it.displayName }.toTypedArray())
        val suitCombo = JComboBox(Suit.values().map { it.displayName }.toTypedArray())
        val addCardButton = JButton("✚ Добавить карту")
        addCardButton.background = Color(100, 200, 100)
        selectorPanel.add(JLabel("Номинал:"))
        selectorPanel.add(nominalCombo)
        selectorPanel.add(JLabel("Масть:"))
        selectorPanel.add(suitCombo)
        selectorPanel.add(addCardButton)
        panel.add(selectorPanel, gbc)


        gbc.gridy = 6
        gbc.fill = GridBagConstraints.BOTH
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        cardsListModel = DefaultListModel()
        val cardsList = JList(cardsListModel)
        val scrollPane = JScrollPane(cardsList)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.border = BorderFactory.createTitledBorder("📦 Карты в колоде")
        scrollPane.preferredSize = Dimension(350, 150)

        panel.add(scrollPane, gbc)

        gbc.gridy = 7
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weighty = 0.0
        val deckButtonPanel = JPanel(FlowLayout())

        val testDeckButton = JButton("🎲 Тестовая колода")
        testDeckButton.background = Color(200, 200, 100)
        testDeckButton.addActionListener {
            cardsListModel.clear()
            val testDeck = listOf(
                "QUEEN HEARTS", "QUEEN SPADES", "QUEEN CLUBS", "SIX DIAMONDS",
                "QUEEN DIAMONDS", "KING HEARTS", "KING SPADES", "SEVEN CLUBS", "EIGHT CLUBS"
            )
            testDeck.forEach { cardsListModel.addElement(it) }
        }
        deckButtonPanel.add(testDeckButton)

        val clearButton = JButton("🗑️ Очистить")
        clearButton.background = Color(255, 150, 150)
        clearButton.addActionListener { cardsListModel.clear() }

        deckButtonPanel.add(clearButton)
        panel.add(deckButtonPanel, gbc)

        addCardButton.addActionListener {
            val nominal = nominalCombo.selectedItem as String
            val suit = suitCombo.selectedItem as String
            val cardString = "$nominal $suit"

            // тут проверочка на то, что в колоде нет повторяющихся карт
            var isDuplicate = false
            for (i in 0 until cardsListModel.size()) {
                if (cardsListModel.getElementAt(i) == cardString) {
                    isDuplicate = true
                    break
                }
            }

            if (isDuplicate) {
                JOptionPane.showMessageDialog(panel, "Эта карта уже добавлена в колоду!", "Дубликат", JOptionPane.WARNING_MESSAGE)
            } else {
                cardsListModel.addElement(cardString)
            }
        }


        gbc.gridy = 8
        val startButton = JButton("🚀 НАЧАТЬ ИГРУ 🚀")
        startButton.font = Font("Arial", Font.BOLD, 16)
        startButton.background = Color(100, 200, 100)
        startButton.foreground = Color.WHITE
        startButton.addActionListener {
            val playerNames = playerFields.map { it.text.trim().ifEmpty { "Игрок" } }

            val cards = mutableListOf<Card>()
            for (i in 0 until cardsListModel.size()) {
                val parts = cardsListModel.getElementAt(i).split(" ")
                if (parts.size >= 2) {
                    val nominal = Nominal.fromDisplayName(parts[0])
                    val suit = Suit.fromDisplayName(parts[1])
                    if (nominal != null && suit != null) {
                        cards.add(Card(nominal, suit))
                    }
                }
            }

            // Проверочки на то, что все как надо
            if (cards.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Добавьте карты в колоду или используйте тестовую колоду!")
                return@addActionListener
            }

            if (playerNames.size < 2) {
                JOptionPane.showMessageDialog(panel, "Добавьте минимум 2 игрока!")
                return@addActionListener
            }

            if (cards.size < (playerNames.size * 4)) {
                JOptionPane.showMessageDialog(
                    panel,
                    "Количество карт в колоде должно быть не менее чем ${4 * playerNames.size}!\n",
                    "Ошибка колоды",
                    JOptionPane.WARNING_MESSAGE
                )
                return@addActionListener
            }
            listener?.onGameInitialized(playerNames, cards)
        }
        panel.add(startButton, gbc)

        mainPanel.add(panel, "setup")
    }

    private fun createNominalPanel() {
        val panel = JPanel(GridBagLayout())
        panel.background = Color(255, 250, 240)
        val gbc = GridBagConstraints()
        gbc.insets = Insets(10, 10, 10, 10)
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0

        val titleLabel = JLabel("❓ Какой номинал вы угадываете?")
        titleLabel.font = Font("Arial", Font.BOLD, 18)
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 4
        panel.add(titleLabel, gbc)

        val nominals = Nominal.values()
        gbc.gridwidth = 1

        for (i in nominals.indices) {
            val nominal = nominals[i]
            val button = JButton(nominal.displayName)
            button.font = Font("Arial", Font.BOLD, 14)
            button.preferredSize = Dimension(120, 60)
            button.background = Color(200, 220, 255)
            button.addActionListener {
                listener?.onNominalQuestionSubmitted(nominal)
            }
            gbc.gridx = i % 4
            gbc.gridy = 1 + i / 4
            panel.add(button, gbc)
        }

        mainPanel.add(panel, "nominal")
    }

    private fun createQuantityPanel() {
        val panel = JPanel(GridBagLayout())
        panel.background = Color(255, 250, 240)
        val gbc = GridBagConstraints()
        gbc.insets = Insets(10, 10, 10, 10)

        val titleLabel = JLabel("🔢 Сколько карт вы угадываете?")
        titleLabel.font = Font("Arial", Font.BOLD, 18)
        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(titleLabel, gbc)

        quantitySpinner = JSpinner(SpinnerNumberModel(1, 1, 4, 1))
        quantitySpinner.font = Font("Arial", Font.PLAIN, 24)
        quantitySpinner.preferredSize = Dimension(100, 50)
        gbc.gridy = 1
        panel.add(quantitySpinner, gbc)

        val submitButton = JButton("✅ Подтвердить")
        submitButton.font = Font("Arial", Font.BOLD, 16)
        submitButton.background = Color(100, 200, 100)
        submitButton.addActionListener {
            listener?.onQuantityQuestionSubmitted(quantitySpinner.value as Int)
        }
        gbc.gridy = 2
        panel.add(submitButton, gbc)

        mainPanel.add(panel, "quantity")
    }

    private fun createSuitsPanel() {
        val panel = JPanel(GridBagLayout())
        panel.background = Color(255, 250, 240)
        val gbc = GridBagConstraints()
        gbc.insets = Insets(10, 10, 10, 10)

        val titleLabel = JLabel("🎴 Какие масти вы угадываете?")
        titleLabel.font = Font("Arial", Font.BOLD, 18)
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        panel.add(titleLabel, gbc)

        val suits = Suit.values()
        val checkboxes = mutableListOf<JCheckBox>()
        gbc.gridwidth = 1

        for (i in suits.indices) {
            val checkbox = JCheckBox(suits[i].displayName)
            checkbox.font = Font("Arial", Font.PLAIN, 14)
            checkboxes.add(checkbox)
            gbc.gridx = i % 2
            gbc.gridy = 1 + i / 2
            panel.add(checkbox, gbc)
        }

        suitsCheckboxes = checkboxes

        val submitButton = JButton("✅ Подтвердить")
        submitButton.font = Font("Arial", Font.BOLD, 16)
        submitButton.background = Color(100, 200, 100)
        submitButton.addActionListener {
            val selectedSuits = suitsCheckboxes.filter { it.isSelected }.map { checkbox ->
                Suit.values().first { it.displayName == checkbox.text }
            }
            listener?.onSuitsQuestionSubmitted(selectedSuits)
        }
        gbc.gridy = 1 + (suits.size + 1) / 2
        panel.add(submitButton, gbc)

        mainPanel.add(panel, "suits")
    }

    private fun createAnswerPanel() {
        val panel = JPanel(GridBagLayout())
        panel.background = Color(255, 250, 240)
        val gbc = GridBagConstraints()
        gbc.insets = Insets(10, 10, 10, 10)

        answerLabel = JLabel("", SwingConstants.CENTER)
        answerLabel.font = Font("Arial", Font.PLAIN, 18)
        answerLabel.preferredSize = Dimension(400, 80)
        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(answerLabel, gbc)

        val buttonPanel = JPanel(FlowLayout())
        val yesButton = JButton("✅ ДА")
        yesButton.font = Font("Arial", Font.BOLD, 20)
        yesButton.background = Color(100, 200, 100)
        yesButton.preferredSize = Dimension(120, 60)
        yesButton.addActionListener { listener?.onAnswerSubmitted(Answer.YES) }

        val noButton = JButton("❌ НЕТ")
        noButton.font = Font("Arial", Font.BOLD, 20)
        noButton.background = Color(200, 100, 100)
        noButton.preferredSize = Dimension(120, 60)
        noButton.addActionListener { listener?.onAnswerSubmitted(Answer.NO) }

        buttonPanel.add(yesButton)
        buttonPanel.add(noButton)

        gbc.gridy = 1
        panel.add(buttonPanel, gbc)

        mainPanel.add(panel, "answer")
    }

    private fun createMessagePanel() {
        val panel = JPanel(GridBagLayout())
        panel.background = Color(255, 250, 240)
        val gbc = GridBagConstraints()
        gbc.insets = Insets(10, 10, 10, 10)

        messageLabel = JLabel("", SwingConstants.CENTER)
        messageLabel.font = Font("Arial", Font.PLAIN, 16)
        messageLabel.preferredSize = Dimension(400, 100)
        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(messageLabel, gbc)
        gbc.gridy = 1
        mainPanel.add(panel, "message")
    }

    private fun createMenuPanel() {
        val panel = JPanel(GridBagLayout())
        panel.background = Color(200, 220, 240)
        val gbc = GridBagConstraints()
        gbc.insets = Insets(15, 15, 15, 15)

        val titleLabel = JLabel("🃏 СУНДУЧОК 🃏")
        titleLabel.font = Font("Arial", Font.BOLD, 28)
        titleLabel.foreground = Color(70, 70, 150)
        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(titleLabel, gbc)

        val newGameButton = createMenuButton("🎮 Новая игра", Color(100, 200, 100))
        newGameButton.addActionListener { showGameSetup() }

        val statsButton = createMenuButton("📊 Статистика", Color(200, 200, 100))
        statusLabel.text = " "
        statsButton.addActionListener { listener?.onMenuRequested() }

        val registryButton = createMenuButton("📖 Реестр игроков", Color(100, 150, 200))
        statusLabel.text = " "
        registryButton.addActionListener { listener?.onMenuRequested() }

        val exitButton = createMenuButton("🚪 Выход", Color(200, 100, 100))
        exitButton.addActionListener { close() }

        gbc.gridy = 1
        panel.add(newGameButton, gbc)
        gbc.gridy = 2
        panel.add(statsButton, gbc)
        gbc.gridy = 3
        panel.add(exitButton, gbc)

        mainPanel.add(panel, "menu")
    }

    private fun createMenuButton(text: String, color: Color): JButton {
        val button = JButton(text)
        button.font = Font("Arial", Font.BOLD, 16)
        button.preferredSize = Dimension(200, 50)
        button.background = color
        button.foreground = Color.WHITE
        button.isFocusPainted = false
        return button
    }

    private fun createStatsPanel() {
        val panel = JPanel(BorderLayout())

        statsTextArea = JTextArea()
        statsTextArea.font = Font("Arial", Font.PLAIN, 14)
        statsTextArea.isEditable = false
        statsTextArea.background = Color(255, 255, 240)
        panel.add(JScrollPane(statsTextArea), BorderLayout.CENTER)

        val closeButton = JButton("🔙 Назад в меню")
        closeButton.font = Font("Arial", Font.BOLD, 14)
        closeButton.addActionListener { showMenu() }
        panel.add(closeButton, BorderLayout.SOUTH)

        mainPanel.add(panel, "stats")
    }

    override fun showPlayerRegistry(stats: List<PlayerStats>) {
        val text = buildString {
            append("🏆 РЕЕСТР ИГРОКОВ 🏆\n\n")
            append("═".repeat(30)).append("\n\n")
            if (stats.isEmpty()) {
                append("Пока нет сохранённых игроков.\n")
            } else {
                stats.forEachIndexed { index, p ->
                    append("🥇 #${index + 1} ${p.name}\n")
                    append("   🎮 Игр: ${p.gamesPlayed} | 🏆 Побед: ${p.wins}\n\n")
                }
            }
        }
        statsTextArea.text = text
        switchCard("stats")
    }

    private fun createHistoryPanel() {
        val panel = JPanel(BorderLayout())

        historyListModel = DefaultListModel()
        val historyList = JList(historyListModel)
        historyList.font = Font("Monospaced", Font.PLAIN, 12)
        panel.add(JScrollPane(historyList), BorderLayout.CENTER)

        val closeButton = JButton("🔙 Назад в меню")
        closeButton.font = Font("Arial", Font.BOLD, 14)
        closeButton.addActionListener { showMenu() }
        panel.add(closeButton, BorderLayout.SOUTH)

        mainPanel.add(panel, "history")
    }

    override fun setListener(listener: GameViewListener) {
        this.listener = listener
    }

    override fun showNominalQuestionInput() {
        switchCard("nominal")
    }

    override fun showQuantityQuestionInput(maxQuantity: Int) {
        quantitySpinner.model = SpinnerNumberModel(1, 1, maxQuantity, 1)
        switchCard("quantity")
    }

    override fun showSuitsQuestionInput(availableSuits: List<String>) {
        suitsCheckboxes.forEach { it.isSelected = false }
        switchCard("suits")
    }

    override fun showAnswerDialog(message: String) {
        answerLabel.text = message
        switchCard("answer")
    }

    override fun showMessage(message: String) {
        messageLabel.text = message
        switchCard("message")
    }

    override fun showGameSetup() {
        switchCard("setup")
    }

    override fun showMenu() {
        switchCard("menu")
    }

    override fun showGameHistory(history: List<String>) {
        historyListModel.clear()
        history.forEach { historyListModel.addElement(it) }
        switchCard("history")
    }

    override fun clearGameState() {
        playersPanel.removeAll()
        playersPanel.revalidate()
        playersPanel.repaint()
        deckSizeLabel.text = "📚 Карт в колоде: 0"
    }

    override fun close() {
        dispose()
    }

    private fun switchCard(name: String) {
        cardLayout.show(mainPanel, name)
    }
}