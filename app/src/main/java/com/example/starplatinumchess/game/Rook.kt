package com.example.starplatinumchess.game

import android.widget.ImageView
import com.example.starplatinumchess.Array2D
import com.example.starplatinumchess.R
import com.example.starplatinumchess.game.ChessPiece.Color

fun rookSimpleMove(board : Array2D<ChessPiece?>, viewGrid : Array2D<ImageView>, selected : ChessPiece, i : Int, j : Int) {
    simpleMove(board, viewGrid, selected, i, j)
    (selected as Rook).moved = true
}

class Rook(override val color: Color) : ChessPiece {

    override val res = if (color == Color.WHITE) R.drawable.w_rook else R.drawable.b_rook
    override val validMoves = HashMap<Pair<Int,Int>, MoveFunction>()
    override var alive = true
    override var i = 0
    override var j = 0
    var moved = false


    override fun updateValidMoves(board: Array2D<ChessPiece?>,danger: HashSet<Pair<Int, Int>>) {
        validMoves.clear()
        for (k in 1..7 - i) {
            val pos = Pair(i + k, j)
            if (board[pos.first][pos.second]?.color != color)
                validMoves[pos] = ::rookSimpleMove
            if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..i) {
            val pos = Pair(i - k, j)
            if (board[pos.first][pos.second]?.color != color)
                validMoves[pos] = ::rookSimpleMove
            if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..7 - j) {
            val pos = Pair(i, j + k)
            if (board[pos.first][pos.second]?.color != color)
                validMoves[pos] = ::rookSimpleMove
            if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..j) {
            val pos = Pair(i, j - k)
            if (board[pos.first][pos.second]?.color != color)
                validMoves[pos] = ::rookSimpleMove
            if (board[pos.first][pos.second] != null)
                break
        }
    }

    override fun addControl(board: Array2D<ChessPiece?>, control: HashSet<Pair<Int, Int>>, checkList : ArrayList<ChessPiece>) {
        for (k in 1..7 - i) {
            val pos = Pair(i + k, j)
            control.add(pos)
            if (board[pos.first][pos.second] != null && board[pos.first][pos.second] is King &&
                board[pos.first][pos.second]!!.color != color) {
                checkList.add(this)
            } else if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..i) {
            val pos = Pair(i - k, j)
            control.add(pos)
            if (board[pos.first][pos.second] != null && board[pos.first][pos.second] is King &&
                board[pos.first][pos.second]!!.color != color) {
                checkList.add(this)
            } else if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..7 - j) {
            val pos = Pair(i, j + k)
            control.add(pos)
            if (board[pos.first][pos.second] != null && board[pos.first][pos.second] is King &&
                board[pos.first][pos.second]!!.color != color) {
                checkList.add(this)
            } else if (board[pos.first][pos.second] != null)
                break
        }
        for (k in 1..j) {
            val pos = Pair(i, j - k)
            control.add(pos)
            if (board[pos.first][pos.second] != null && board[pos.first][pos.second] is King &&
                board[pos.first][pos.second]!!.color != color) {
                checkList.add(this)
            } else if (board[pos.first][pos.second] != null)
                break
        }
    }
}