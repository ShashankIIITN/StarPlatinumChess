package com.example.starplatinumchess.game

import android.util.Log
import android.widget.ImageView
import com.example.starplatinumchess.Array2D
import com.example.starplatinumchess.game.ChessPiece.Color
import com.example.starplatinumchess.R
import com.example.starplatinumchess.empty

fun twoSquareMove(board : Array2D<ChessPiece?>, viewGrid : Array2D<ImageView>, selected : ChessPiece, i : Int, j : Int) {
    (selected as Pawn).enPassantAble = true
    simpleMove(board, viewGrid, selected, i, j)
    Log.i("Main", "Pawn (${selected.i}, ${selected.j}) is en passant-able")
}

fun enPassantMove(board : Array2D<ChessPiece?>, viewGrid : Array2D<ImageView>, selected : ChessPiece, i : Int, j : Int) {
    Log.i("Main", "Killing pos (${selected.i}, $j)")
    board[selected.i][j]!!.alive = false
    board[selected.i][j] = null
    viewGrid[selected.i][j].setImageResource(empty)
    simpleMove(board, viewGrid, selected, i, j)
}

class Pawn(override val color: Color) : ChessPiece {

    override val res = if (color == Color.WHITE) R.drawable.w_pawn else R.drawable.b_pawn
    override val validMoves = HashMap<Pair<Int,Int>, MoveFunction>()
    override var alive = true
    override var i = 0
    override var j = 0

    var enPassantAble = false

    override fun updateValidMoves(board: Array2D<ChessPiece?>, danger: HashSet<Pair<Int, Int>>) {
        if (enPassantAble) {
            enPassantAble = false
            Log.i("Main", "Pawn ($i, $j) is not en passant-able")
        }
        validMoves.clear()
        val row =  if (color == Color.WHITE) i - 1 else i + 1
        if (row in 0 until 8) {
            if (board[row][j] == null)
                validMoves[Pair(row, j)] = ::simpleMove
            if (j + 1 < 8 && board[row][j+1] != null && board[row][j+1]!!.color != color)
                validMoves[Pair(row, j+1)] = ::simpleMove
            if (0 <= j - 1 && board[row][j-1] != null && board[row][j-1]!!.color != color)
                validMoves[Pair(row, j-1)] = ::simpleMove
            if (j + 1 < 8 && board[i][j+1] != null && board[i][j+1] is Pawn &&
                (board[i][j+1] as Pawn).enPassantAble && board[i][j+1]!!.color != color) {
                validMoves[Pair(row, j+1)] = ::enPassantMove
            }
            if (0 <= j - 1 && board[i][j-1] != null && board[i][j-1] is Pawn &&
                (board[i][j-1] as Pawn).enPassantAble && board[i][j-1]!!.color != color) {
                validMoves[Pair(row, j-1)] = ::enPassantMove
            }
        }
        if (color == Color.WHITE && i == 6 && board[5][j] == null && board[4][j] == null) {
            validMoves[Pair(4, j)] = ::twoSquareMove
        }
        if (color == Color.BLACK && i == 1 && board[2][j] == null && board[3][j] == null) {
            validMoves[Pair(3, j)] = ::twoSquareMove
        }
        //TODO: Add promotion
    }

    override fun addControl(board: Array2D<ChessPiece?>, control: HashSet<Pair<Int, Int>>, checkList : ArrayList<ChessPiece>) {
        val row =  if (color == Color.WHITE) i - 1 else i + 1
        if (row in 0 until 8) {
            if (j + 1 < 8) {
                if (board[row][j+1]?.color != color && board[row][j+1] is King)
                    checkList.add(this)
                control.add(Pair(row, j + 1))
            }
            if (0 <= j - 1) {
                if (board[row][j-1]?.color != color && board[row][j-1] is King)
                    checkList.add(this)
                control.add(Pair(row, j - 1))
            }
        }
    }
}
