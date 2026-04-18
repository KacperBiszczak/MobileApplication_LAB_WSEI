package pl.wsei.pam.lab03

import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.gridlayout.widget.GridLayout
import pl.wsei.pam.lab01.R
import java.util.Stack

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_24,
        R.drawable.baseline_roller_skating_24,
        R.drawable.outline_add_card_24,
        R.drawable.baseline_sports_baseball_24,
        R.drawable.baseline_supervisor_account_24,
        R.drawable.baseline_air_24,
        R.drawable.outline_agriculture_24,
        R.drawable.outline_air_freshener_24,
        R.drawable.outline_comedy_mask_24,
        R.drawable.outline_adb_24,
        R.drawable.outline_celebration_24,
        R.drawable.outline_church_24,
        R.drawable.outline_chess_knight_24,
        R.drawable.outline_chess_bishop_2_24,
        R.drawable.outline_battery_android_frame_bolt_24,
        R.drawable.outline_allergy_24,
        R.drawable.outline_fingerprint_24,
        R.drawable.outline_filter_vintage_24,
        R.drawable.outline_30fps_24
    )

    init {
        val totalTiles = cols * rows
        val numPairs = totalTiles / 2
        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            for (i in 0 until numPairs) {
                val icon = icons[i % icons.size]
                it.add(icon)
                it.add(icon)
            }
            if (totalTiles % 2 != 0) {
                it.add(icons[numPairs % icons.size])
            }
            it.shuffle()
        }

        for (row in 0 until rows) {
            for (col in 0 until cols) {

                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "${row}x${col}"

                    it.scaleType = ImageView.ScaleType.FIT_CENTER
                    it.setPadding(10, 10, 10, 10)

                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.FILL)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                    
                    // Dodajemy marginesy, aby zachować odstępy między przyciskami
                    layoutParams.setMargins(10, 10, 10, 10)

                    it.layoutParams = layoutParams
                    gridLayout.addView(it)
                }

                if (shuffledIcons.isNotEmpty()) {
                    val resource = shuffledIcons.removeAt(0)
                    addTile(btn, resource)
                }
            }
        }
    }

    private val deckResource: Int = R.drawable.deck
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = {}
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)
    private var isLocked: Boolean = false

    fun lock() {
        isLocked = true
    }

    fun unlock() {
        isLocked = false
    }

    fun getState(): IntArray {
        val state = IntArray(rows * cols)
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val tile = tiles["${row}x${col}"]
                state[index] = if (tile?.revealed == true) tile.tileResource else -1
            }
        }
        return state
    }

    fun setState(state: IntArray) {
        val revealedResources = state.filter { it != -1 }
        logic.matches = revealedResources.size / 2

        val remainingTilesCount = state.count { it == -1 }
        val remainingPairs = remainingTilesCount / 2

        val availableIcons = icons.filter { it !in revealedResources }.toMutableList()
        val shuffledRemaining: MutableList<Int> = mutableListOf()
        for (i in 0 until remainingPairs) {
            val icon = availableIcons.getOrElse(i % availableIcons.size) { icons[i % icons.size] }
            shuffledRemaining.add(icon)
            shuffledRemaining.add(icon)
        }
        if (remainingTilesCount % 2 != 0) {
            shuffledRemaining.add(availableIcons.getOrElse(remainingPairs % availableIcons.size) { icons[0] })
        }
        shuffledRemaining.shuffle()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val tile = tiles["${row}x${col}"] ?: continue
                if (state[index] != -1) {
                    tile.tileResource = state[index]
                    tile.revealed = true
                } else {
                    if (shuffledRemaining.isNotEmpty()) {
                        tile.tileResource = shuffledRemaining.removeAt(0)
                        tile.revealed = false
                    }
                }
            }
        }
    }

    fun getTiles(): List<Tile> {
        val list = mutableListOf<Tile>()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                tiles["${row}x${col}"]?.let { list.add(it) }
            }
        }
        return list
    }

    private fun onClickTile(v: View) {
        if (isLocked) return
        val tile = tiles[v.tag] ?: return
        
        if (tile.revealed || matchedPair.contains(tile)) return
        
        if (matchedPair.size >= 2) return

        matchedPair.push(tile)
        val matchResult = logic.process {
            tile.tileResource
        }
        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }
}
