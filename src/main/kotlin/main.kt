package boxGame

fun main() {
    val view = GameViewImpl()
    val registry = PlayerRegistry()
    val presenter = GamePresenter(view, registry)
    view.setListener(presenter)
    view.showMenu()
    view.isVisible = true
}