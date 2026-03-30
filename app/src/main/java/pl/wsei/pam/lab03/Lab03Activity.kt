package pl.wsei.pam.lab03

import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab03)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val size = intent.getIntArrayExtra("size") ?: intArrayOf(3,3)
        val rows = size[0];
        val cols = size[1];

        val mBoard: GridLayout = findViewById<GridLayout>(R.id.main)
        mBoard.rowCount = rows;
        mBoard.columnCount = cols;

        val mBoardModel = MemoryBoardView(mBoard, cols, rows)

        mBoardModel.setOnGameChangeListener { e ->
            run {
                when (e.state) {
                    GameStates.Matching -> {
                        e.tiles.forEach {
                            tile -> tile.revealed = true
                        }
                    }
                    GameStates.Match -> {
                        e.tiles.forEach {
                                tile -> tile.revealed = true
                        }
                    }
                    GameStates.NoMatch -> {
                        e.tiles.forEach {
                                tile -> tile.revealed = true
                            Timer().schedule(2000) {
                                tile.revealed = false
                            }
                        }
                    }
                    GameStates.Finished -> {
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }
}