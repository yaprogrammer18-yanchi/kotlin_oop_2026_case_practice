package boxGame

fun main() {
    val view = GameViewImpl()
    val repository = SqliteGameRepository()
    val presenter = GamePresenter(view, repository)
    view.setListener(presenter)
    view.showMenu()
    view.isVisible = true
}