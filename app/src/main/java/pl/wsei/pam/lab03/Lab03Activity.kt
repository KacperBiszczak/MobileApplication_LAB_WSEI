package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import pl.wsei.pam.lab01.R
import java.util.Random
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
                        animatePairedButton(
                            button = tile.button,
                            action = {}
                        )
                    }
                }
                GameStates.NoMatch -> {
                    mBoardModel.lock()

                    val tilesToHide = e.tiles
                    var finishedAnimations = 0

                    tilesToHide.forEach { tile ->
                        tile.revealed = true

                        animateUnpairedButton(
                            button = tile.button,
                            action = {
                                tile.revealed = false

                                finishedAnimations++
                                if (finishedAnimations == tilesToHide.size) {
                                    mBoardModel.unlock()
                                }
                            }
                        )
                    }
                }


                GameStates.Finished -> {
                    e.tiles.forEach { tile ->
                        tile.revealed = true
                        tile.button.backgroundTintList = ColorStateList.valueOf(Color.rgb(0, 102, 0))
                        tile.button.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        tile.removeOnClickListener()
                        animatePairedButton(
                            button = tile.button,
                            action = {}
                        )
                    }
                    Toast.makeText(this, "Gratulacje! Gra ukończona. \uD83C\uDFC6", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun animatePairedButton(button: ImageButton, action: Runnable ) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)
        set.startDelay = 500
        set.duration = 2000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)
        set.addListener(object: Animator.AnimatorListener {

            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run();
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }

    private fun animateUnpairedButton(button: ImageButton, action: Runnable ) {
        val set = AnimatorSet()

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 3f,-3f,0f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 1f)
        set.startDelay = 200
        set.duration = 1000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, fade)
        set.addListener(object: Animator.AnimatorListener {

            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                button.rotation = 0f;
//                button.alpha = 0.0f

                action.run();
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("state", mBoardModel.getState())
    }
}
