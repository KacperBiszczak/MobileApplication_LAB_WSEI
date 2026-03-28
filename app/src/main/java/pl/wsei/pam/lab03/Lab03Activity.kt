package pl.wsei.pam.lab03

import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import pl.wsei.pam.lab01.R

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
        for(row in 0 until rows) {
            for (col in 0 until cols) {
                val btn = ImageButton(this).also {
                    it.tag = "${row}x${col}"
                    val layoutParams = GridLayout.LayoutParams()

                    // Ustawienie ikony (upewnij się, że plik istnieje w res/drawable)
                    it.setImageResource(R.drawable.baseline_audiotrack_24)

                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.CENTER)

                    // Definicja miejsca w siatce: spec(indeks, rozpiętość, waga)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)

                    it.layoutParams = layoutParams
                    mBoard.addView(it)
                }
            }
        }

    }
}