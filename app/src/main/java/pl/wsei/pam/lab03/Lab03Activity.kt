package pl.wsei.pam.lab03

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import pl.wsei.pam.lab01.R
import java.util.Timer
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoardModel: MemoryBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab03)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val size = intent.getIntArrayExtra("size") ?: intArrayOf(4, 4)
        val rows = size[0]
        val cols = size[1]

        val mBoard: GridLayout = findViewById(R.id.main)
        mBoard.rowCount = rows
        mBoard.columnCount = cols

        mBoardModel = MemoryBoardView(mBoard, cols, rows)

        if (savedInstanceState != null) {
            val state = savedInstanceState.getIntArray("state")
            if (state != null) {
                mBoardModel.setState(state)
                mBoardModel.getTiles().forEach { tile ->
                    if (tile.revealed) {
                        tile.button.backgroundTintList = ColorStateList.valueOf(Color.rgb(0, 102, 0))
                        tile.button.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        tile.removeOnClickListener()
                    }
                }
            }
        }

        mBoardModel.setOnGameChangeListener { e ->
            when (e.state) {
                GameStates.Matching -> {
                    e.tiles.forEach { tile -> 
                        tile.revealed = true 
                        tile.button.imageTintList = ColorStateList.valueOf(Color.BLACK)
                    }
                }
                GameStates.Match -> {
                    e.tiles.forEach { tile ->
                        tile.revealed = true
                        tile.button.backgroundTintList = ColorStateList.valueOf(Color.rgb(0, 102, 0))
                        tile.button.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        tile.removeOnClickListener()
                    }
                }
                GameStates.NoMatch -> {
                    mBoardModel.lock()
                    e.tiles.forEach { tile -> 
                        tile.revealed = true 
                    }
                    Timer().schedule(1000) {
                        runOnUiThread {
                            e.tiles.forEach { tile -> 
                                tile.revealed = false 
                            }
                            mBoardModel.unlock()
                        }
                    }
                }
                GameStates.Finished -> {
                    e.tiles.forEach { tile ->
                        tile.revealed = true
                        tile.button.backgroundTintList = ColorStateList.valueOf(Color.rgb(0, 102, 0))
                        tile.button.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        tile.removeOnClickListener()
                    }
                    Toast.makeText(this, "Gratulacje! Gra ukończona. \uD83C\uDFC6", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("state", mBoardModel.getState())
    }
}
