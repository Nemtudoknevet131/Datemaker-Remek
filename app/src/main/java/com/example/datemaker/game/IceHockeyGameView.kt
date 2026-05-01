package com.example.datemaker.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.example.datemaker.utils.AdPopupManager
import kotlin.math.*

class IceHockeyGameView(context: Context) : View(context) {

    private val paddlePaint1 = Paint().apply { color = Color.BLUE }
    private val paddlePaint2 = Paint().apply { color = Color.RED }
    private val puckPaint = Paint().apply { color = Color.BLACK }
    private val midLinePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 12f
    }

    private val goalPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 20f
    }

    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 90f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    // Sizes
    private var paddleRadius = 110f
    private var puckRadius = 45f
    private var goalWidth = 300f

    // Player positions
    private var p1X = 0f
    private var p1Y = 0f
    private var p2X = 0f
    private var p2Y = 0f

    // Puck
    private var puckX = 0f
    private var puckY = 0f
    private var puckVX = 0f
    private var puckVY = 0f

    // Scores
    private var score1 = 0
    private var score2 = 0
    private var winningScore = 5
    private var winner: Int? = null
    private var adShown = false

    // Screen
    private var screenW = 0
    private var screenH = 0

    // Touch pointers
    private var activePointer1 = -1
    private var activePointer2 = -1

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        screenW = w
        screenH = h
        goalWidth = w / 3f

        p1X = w / 2f
        p1Y = h * 0.25f

        p2X = w / 2f
        p2Y = h * 0.75f

        puckX = w / 2f
        puckY = h / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (winner == null) {
            updatePuckPhysics()
        }

        // Draw middle line
        canvas.drawLine(0f, screenH / 2f, screenW.toFloat(), screenH / 2f, midLinePaint)

        // Draw goals
        val goalLeft = (screenW - goalWidth) / 2
        val goalRight = goalLeft + goalWidth
        canvas.drawLine(goalLeft, 0f, goalRight, 0f, goalPaint)
        canvas.drawLine(goalLeft, screenH.toFloat(), goalRight, screenH.toFloat(), goalPaint)

        // Draw paddles
        canvas.drawCircle(p1X, p1Y, paddleRadius, paddlePaint1)
        canvas.drawCircle(p2X, p2Y, paddleRadius, paddlePaint2)

        // Draw puck
        canvas.drawCircle(puckX, puckY, puckRadius, puckPaint)

        // Draw score
        canvas.drawText("$score1", 50f, screenH / 2f - 60f, scorePaint)
        canvas.drawText("$score2", 50f, screenH / 2f + 120f, scorePaint)

        // Draw winner screen
        if (winner != null) {
            drawWinnerScreen(canvas)
        }

        invalidate()
    }

    private fun updatePuckPhysics() {
        puckX += puckVX
        puckY += puckVY

        // Friction
        puckVX *= 0.99f
        puckVY *= 0.99f

        // Wall bounce
        if (puckX - puckRadius < 0 || puckX + puckRadius > screenW) {
            puckVX *= -1
        }

        if (puckY - puckRadius < 0) {
            if (isGoal()) {
                score2++
                checkWinner()
                resetPuck()
            } else {
                puckVY *= -1
                puckY = puckRadius // unstuck
            }
        }

        if (puckY + puckRadius > screenH) {
            if (isGoal()) {
                score1++
                checkWinner()
                resetPuck()
            } else {
                puckVY *= -1
                puckY = screenH - puckRadius // unstuck
            }
        }

        // Collisions
        collideWithPaddle(p1X, p1Y)
        collideWithPaddle(p2X, p2Y)
    }

    private fun isGoal(): Boolean {
        val goalLeft = (screenW - goalWidth) / 2
        val goalRight = goalLeft + goalWidth
        return puckX > goalLeft && puckX < goalRight
    }

    private fun collideWithPaddle(px: Float, py: Float) {
        val dx = puckX - px
        val dy = puckY - py
        val dist = sqrt(dx*dx + dy*dy)

        if (dist < paddleRadius + puckRadius) {
            val angle = atan2(dy, dx)
            puckVX = cos(angle) * 35f
            puckVY = sin(angle) * 35f
        }
    }

    private fun checkWinner() {
        if (score1 >= winningScore) winner = 1
        if (score2 >= winningScore) winner = 2
        
        if (winner != null && !adShown) {
            adShown = true
            AdPopupManager.onGameEnd(context) {
                // Double score? Maybe not for hockey as it's a match.
                // But we can just show a toast or something.
                // Or maybe double the winning score to 10?
                if (winner == 1) score1 *= 2 else score2 *= 2
                invalidate()
            }
        }
    }

    private fun drawWinnerScreen(canvas: Canvas) {
        val overlay = Paint().apply { color = Color.argb(180, 0, 0, 0) }
        canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(), overlay)

        val winPaint = Paint().apply {
            color = Color.WHITE
            textSize = 140f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val winnerText = if (winner == 1) "Player 1 Wins!" else "Player 2 Wins!"
        canvas.drawText(winnerText, screenW / 2f, screenH / 2f - 60f, winPaint)

        winPaint.textSize = 80f
        canvas.drawText("Tap to Restart", screenW / 2f, screenH / 2f + 100f, winPaint)
    }

    private fun resetPuck() {
        puckX = screenW / 2f
        puckY = screenH / 2f
        puckVX = 0f
        puckVY = 0f
    }

    private fun restartGame() {
        score1 = 0
        score2 = 0
        winner = null
        adShown = false
        resetPuck()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // Restart on win
        if (winner != null && event.action == MotionEvent.ACTION_DOWN) {
            restartGame()
            return true
        }

        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.y < screenH / 2) {
                    activePointer1 = pointerId
                    p1X = event.x
                    p1Y = event.y
                } else {
                    activePointer2 = pointerId
                    p2X = event.x
                    p2Y = event.y
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)

                    if (id == activePointer1 && event.getY(i) < screenH / 2) {
                        p1X = event.getX(i)
                        p1Y = event.getY(i)
                    }

                    if (id == activePointer2 && event.getY(i) > screenH / 2) {
                        p2X = event.getX(i)
                        p2Y = event.getY(i)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (pointerId == activePointer1) activePointer1 = -1
                if (pointerId == activePointer2) activePointer2 = -1
            }
        }

        return true
    }
}
