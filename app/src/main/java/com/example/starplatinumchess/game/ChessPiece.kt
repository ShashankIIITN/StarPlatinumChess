package com.example.starplatinumchess.game

import android.widget.ImageView
import com.example.starplatinumchess.Array2D
import com.example.starplatinumchess.empty

typealias MoveFunction = (Array2D<ChessPiece?>, Array2D<ImageView>, ChessPiece, Int, Int) -> Unit

fun simpleMove(board : Array2D<ChessPiece?>, viewGrid : Array2D<ImageView>, selected : ChessPiece, i : Int, j : Int) {
    board[i][j]?.alive = false
    board[i][j] = selected
    board[selected.i][selected.j] = null
    viewGrid[i][j].setImageResource(selected.res)
    viewGrid[selected.i][selected.j].setImageResource(empty)
    selected.i = i
    selected.j = j
}

interface ChessPiece {

    enum class Color {
        WHITE, BLACK
    }

    val color: Color
    val res: Int
    val validMoves : HashMap<Pair<Int, Int>, MoveFunction>
    var alive : Boolean
    var i : Int
    var j : Int

    fun updateValidMoves(board: Array2D<ChessPiece?>, danger : HashSet<Pair<Int, Int>>)

    fun addControl(board: Array2D<ChessPiece?>, control : HashSet<Pair<Int, Int>>, checkList : ArrayList<ChessPiece>)
}

