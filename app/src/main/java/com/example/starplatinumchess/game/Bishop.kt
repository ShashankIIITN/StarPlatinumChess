package com.example.starplatinumchess.game

import com.example.starplatinumchess.Array2D
import com.example.starplatinumchess.R
import com.example.starplatinumchess.game.ChessPiece.Color
import kotlin.math.min

class Bishop(override val color: Color) : ChessPiece {

    override val res = if (color == Color.WHITE) R.drawable.w_bishop else R.drawable.b_bishop

    override val validMoves = HashMap<Pair<Int,Int>, MoveFunction>()
    override var alive = true
    override var i = 0
    override var j = 0

    override fun updateValidMoves(board: Array2D<ChessPiece?>,danger: HashSet<Pair<Int, Int>>) {
        validMoves.clear()
        for (k in 1..min(7 - i, 7 - j)) {
            val pos = Pair(i + k, j + k)
            if (board[pos.first][pos.second]?.color != color)
                validMoves[pos] = ::simpleMove
            if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..min(i, j)) {
            val pos = Pair(i - k, j - k)
            if (board[pos.first][pos.second]?.color != color)
                validMoves[pos] = ::simpleMove
            if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..min(7 - i, j)) {
            val pos = Pair(i + k, j - k)
            if (board[pos.first][pos.second]?.color != color)
                validMoves[pos] = ::simpleMove
            if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..min(i, 7 - j)) {
            val pos = Pair(i - k, j + k)
            if (board[pos.first][pos.second]?.color != color)
                validMoves[pos] = ::simpleMove
            if (board[pos.first][pos.second] != null)
                break
        }
    }

    override fun addControl(board: Array2D<ChessPiece?>, control: HashSet<Pair<Int, Int>>, checkList : ArrayList<ChessPiece>) {
        for (k in 1..min(7 - i, 7 - j)) {
            val pos = Pair(i + k, j + k)
            control.add(pos)
            if (board[pos.first][pos.second] != null && board[pos.first][pos.second] is King &&
                board[pos.first][pos.second]!!.color != color) {
                checkList.add(this)
            } else if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..min(i, j)) {
            val pos = Pair(i - k, j - k)
            control.add(pos)
            if (board[pos.first][pos.second] != null && board[pos.first][pos.second] is King &&
                board[pos.first][pos.second]!!.color != color) {
                checkList.add(this)
            } else if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..min(7 - i, j)) {
            val pos = Pair(i + k, j - k)
            control.add(pos)
            if (board[pos.first][pos.second] != null && board[pos.first][pos.second] is King &&
                board[pos.first][pos.second]!!.color != color) {
                checkList.add(this)
            } else if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..min(i, 7 - j)) {
            val pos = Pair(i - k, j + k)
            control.add(pos)
            if (board[pos.first][pos.second] != null && board[pos.first][pos.second] is King &&
                board[pos.first][pos.second]!!.color != color) {
                checkList.add(this)
            } else if (board[pos.first][pos.second] != null)
                break
        }
    }
}