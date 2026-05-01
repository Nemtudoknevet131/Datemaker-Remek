package com.example.datemaker.game

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import com.example.datemaker.utils.AdPopupManager
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import kotlin.random.Random
import androidx.core.graphics.scale

class FlappyGameView(context: Context) : View(context) {

    private var birdBitmap: Bitmap? = null

    fun setBirdAvatar(url: String?) {
        if (url.isNullOrEmpty()) return;

        Glide.with(this)
            .asBitmap()
            .load(url)
            .circleCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    birdBitmap = resource.scale((birdRadius * 2).toInt(), (birdRadius * 2).toInt())
                    invalidate()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    birdBitmap = null
                }
            })
    }

    private val birdPaint = Paint().apply {
        color = Color.YELLOW
        isAntiAlias = true
    }

    private val pipePaint = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true
    }

    private val scorePaint = Paint().apply {
        color = Color.BLACK
        textSize = 110f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    // Bird
    private var birdX = 200f
    private var birdY = 500f
    private val birdRadius = 50f

    // Physics
    private var velocity = 0f
    private val gravity = 2f
    private val flapPower = -30f

    // Pipes
    private var pipeX = 1000f
    private val pipeWidth = 200f
    private var pipeGap = 500f
    private var pipeTopHeight = 300f
    private val pipeSpeed = 10f
    private var pipePassed = false
    private var score = 0
    private var isGameOver = false
    private var adShown = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isGameOver) {
            drawGameOver(canvas)
            return
        }

        updatePhysics()
        drawBird(canvas)
        drawPipes(canvas)
        drawScore(canvas)

        invalidate() // redraw continuously
    }

    private fun updatePhysics() {
        velocity += gravity
        birdY += velocity

        // Move pipes
        pipeX -= pipeSpeed

        if (!pipePassed && pipeX + pipeWidth < birdX - birdRadius) {
            score++
            pipePassed = true
        }

        if (pipeX + pipeWidth < 0) {
            resetPipes()
        }

        checkCollision()
    }

    private fun drawBird(canvas: Canvas) {
        val bmp = birdBitmap

        if (bmp != null) {
            val left = birdX - bmp.width / 2f
            val top = birdY - bmp.height / 2f
            canvas.drawBitmap(bmp, left, top, null)
        } else {
            canvas.drawCircle(birdX, birdY, birdRadius, birdPaint)
        }
    }

    private fun drawPipes(canvas: Canvas) {
        // Top pipe
        canvas.drawRect(pipeX, 0f, pipeX + pipeWidth, pipeTopHeight, pipePaint)

        // Bottom pipe
        val bottomTop = pipeTopHeight + pipeGap
        canvas.drawRect(pipeX, bottomTop, pipeX + pipeWidth, height.toFloat(), pipePaint)
    }

    private fun drawScore(canvas: Canvas) {
        canvas.drawText("$score", width / 2f - 50f, 150f, scorePaint)
    }

    private fun resetPipes() {
        pipeX = width.toFloat()
        pipeGap = 450f
        pipeTopHeight = Random.nextInt(200, height - 800).toFloat()
        pipePassed = false
    }

    private fun checkCollision() {
        // Ground & ceiling
        if (birdY - birdRadius < 0 || birdY + birdRadius > height) {
            isGameOver = true
        }

        // Pipe collision
        val hitsPipeX = birdX + birdRadius > pipeX && birdX - birdRadius < pipeX + pipeWidth

        if (hitsPipeX) {
            val pipeBottomTop = pipeTopHeight + pipeGap

            val hitsTopPipe = birdY - birdRadius < pipeTopHeight
            val hitsBottomPipe = birdY + birdRadius > pipeBottomTop

            if (hitsTopPipe || hitsBottomPipe) {
                isGameOver = true
            }
        }

        if (isGameOver && !adShown) {
            adShown = true
            AdPopupManager.onGameEnd(context) {
                score *= 2
                invalidate()
            }
        }
    }

    private fun drawGameOver(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.RED
            textSize = 120f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.drawText("GAME OVER", width / 2f, height / 2f - 100, paint)

        paint.textSize = 80f
        canvas.drawText("Tap to restart", width / 2f, height / 2f + 50, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {

            // Restart if dead
            if (isGameOver) {
                restartGame()
            } else {
                velocity = flapPower
            }
        }
        return true
    }

    private fun restartGame() {
        adShown = false
        birdY = 500f
        velocity = 0f
        pipeX = width.toFloat()
        score = 0
        pipePassed = false
        isGameOver = false
    }
}
