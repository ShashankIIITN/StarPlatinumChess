package com.example.starplatinumchess.game

import com.example.starplatinumchess.Array2D
import com.example.starplatinumchess.R
import com.example.starplatinumchess.game.ChessPiece.Color

class Knight(override val color: Color) : ChessPiece {

    override val res = if (color == Color.WHITE) R.drawable.w_knight else R.drawable.b_knight
    override val validMoves = HashMap<Pair<Int,Int>, MoveFunction>()
    override var alive = true
    override var i = 0
    override var j = 0

    override fun updateValidMoves(board: Array2D<ChessPiece?>,danger: HashSet<Pair<Int, Int>>) {
        validMoves.clear()
        val moves = arrayOf(
            Pair(i + 2, j + 1), Pair(i + 2, j - 1), Pair(i - 2, j + 1), Pair(i - 2, j - 1),
            Pair(i + 1, j + 2), Pair(i + 1, j - 2), Pair(i - 1, j + 2), Pair(i - 1, j - 2))
        for (m in moves) {
            if (m.first in 0..7 && m.second in 0..7 && board[m.first][m.second]?.color != color) {
                validMoves[m] = ::simpleMove
            }
        }
    }
    override fun addControl(board: Array2D<ChessPiece?>, control: HashSet<Pair<Int, Int>>, checkList : ArrayList<ChessPiece>) {
        val moves = arrayOf(
            Pair(i + 2, j + 1), Pair(i + 2, j - 1), Pair(i - 2, j + 1), Pair(i - 2, j - 1),
            Pair(i + 1, j + 2), Pair(i + 1, j - 2), Pair(i - 1, j + 2), Pair(i - 1, j - 2))
        for (m in moves) {
            if (m.first in 0..7 && m.second in 0..7) {
                if (board[m.first][m.second]?.color != color && board[m.first][m.second] is King)
                    checkList.add(this)
                control.add(m)
            }
        }
    }
}