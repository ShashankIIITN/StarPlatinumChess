package com.example.starplatinumchess.game

import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.starplatinumchess.Array2D
import com.example.starplatinumchess.Black
import com.example.starplatinumchess.cntxt
import com.example.starplatinumchess.empty
import com.example.starplatinumchess.game.ChessPiece.Color
import com.example.starplatinumchess.sendData.SendGameData
import java.util.LinkedList

class Chessboard(viewGrid : Array2D<ImageView?>) {

    private var viewGrid : Array2D<ImageView> = Array(8) {i->
        Array(8) {j->viewGrid[i][j]!!}
    }
    private var board : Array2D<ChessPiece?> = Array(8){ arrayOfNulls(8)}
    private var selected : ChessPiece? = null

    private val blackPieces = LinkedList<ChessPiece>()
    private val whitePieces = LinkedList<ChessPiece>()
    private  val blackKing : King
    private  val whiteKing : King
    private val sendData = SendGameData()


    private val control = HashSet<Pair<Int, Int>>()
    private val checkList = ArrayList<ChessPiece>()

    private var turnColor = Color.WHITE

    init {
        for (i in 0 until 8) {
            board[1][i] = Pawn(Color.BLACK)
            blackPieces.add(board[1][i]!!)
            board[6][i] = Pawn(Color.WHITE)
            whitePieces.add(board[6][i]!!)
        }
        board[0][0] = Rook(Color.BLACK)
        board[0][1] = Knight(Color.BLACK)
        board[0][2] = Bishop(Color.BLACK)
        board[0][3] = King(Color.BLACK)
        board[0][4] = Queen(Color.BLACK)
        board[0][5] = Bishop(Color.BLACK)
        board[0][6] = Knight(Color.BLACK)
        board[0][7] = Rook(Color.BLACK)
        blackPieces.addAll(listOf(board[0][0]!!, board[0][1]!!, board[0][2]!!, board[0][3]!!,
            board[0][4]!!, board[0][5]!!, board[0][6]!!, board[0][7]!!,))

        board[7][0] = Rook(Color.WHITE)
        board[7][1] = Knight(Color.WHITE)
        board[7][2] = Bishop(Color.WHITE)
        board[7][3] = King(Color.WHITE)
        board[7][4] = Queen(Color.WHITE)
        board[7][5] = Bishop(Color.WHITE)
        board[7][6] = Knight(Color.WHITE)
        board[7][7] = Rook(Color.WHITE)

        blackKing = board[0][3] as King
        whiteKing = board[7][3] as King

        whitePieces.addAll(listOf(board[7][0]!!, board[7][1]!!, board[7][2]!!, board[7][3]!!,
            board[7][4]!!, board[7][5]!!, board[7][6]!!, board[7][7]!!,))
//        val onClickListener = fun (view : View) {
//            val pos = view.tag as Pair<Int, Int>
//            onCellSelected(pos)
//        }
        val onTouchListener = fun(view : View,  motionEvent : MotionEvent) : Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    onCellSelected(view.tag as Pair<Int, Int>)
                    if (selected != null) {
                        view.startDragAndDrop(null, View.DragShadowBuilder(view), view, 0)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }
            return true
        }
        val onDragListener = fun(view : View, dragEvent : DragEvent) : Boolean {
            if (dragEvent.localState == view) {
                Log.i("MainActivity", "Rejected")
                return false
            }
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {

                }
                DragEvent.ACTION_DRAG_EXITED -> {

                }
                DragEvent.ACTION_DROP -> {
                    Log.i("MainActivity", "Dropped")
                    onCellSelected(view.tag as Pair<Int, Int>)
                }
            }
            return true
        }
        for ((i, row) in this.viewGrid.withIndex()) {
            for ((j, cell) in row.withIndex()) {
                cell.tag = Pair(i, j)
//                cell.setOnClickListener(onClickListener)
                cell.setOnTouchListener(onTouchListener)
                cell.setOnDragListener(onDragListener)
                if (board[i][j] == null) {
                    this.viewGrid[i][j].setImageResource(empty)
                } else {
                    board[i][j]!!.i = i
                    board[i][j]!!.j = j
                    this.viewGrid[i][j].setImageResource(board[i][j]!!.res)
                }
            }
        }
        update()
    }

    fun onCellSelected(cell : Pair<Int, Int>) {
        if (selected == null) {
            if (board[cell.first][cell.second] != null &&
                board[cell.first][cell.second]!!.color == turnColor &&
                board[cell.first][cell.second]!!.validMoves.isNotEmpty()) {
                selected = board[cell.first][cell.second]
                for ((pos, _) in selected!!.validMoves) {
                    viewGrid[pos.first][pos.second].setBackgroundResource(android.R.color.holo_green_light)
                }
            }
        } else if(Black && turnColor == Color.BLACK || !Black && turnColor == Color.WHITE){
            makeMove(cell.first, cell.second)
            for ((pos, _) in selected!!.validMoves) {
                viewGrid[pos.first][pos.second].setBackgroundResource(empty)
            }
            selected = null
        }
    }
     fun onCellSelected(cell : Pair<Int, Int>, select : ChessPiece?) {
         selected = select
        if (selected == null) {
            if (board[cell.first][cell.second] != null &&
                board[cell.first][cell.second]!!.color == turnColor &&
                board[cell.first][cell.second]!!.validMoves.isNotEmpty()) {
                selected = board[cell.first][cell.second]
                for ((pos, _) in selected!!.validMoves) {
                    viewGrid[pos.first][pos.second].setBackgroundResource(android.R.color.holo_green_light)
                }
            }
        } else {
            makeMove(cell.first, cell.second)
            for ((pos, _) in selected!!.validMoves) {
                viewGrid[pos.first][pos.second].setBackgroundResource(empty)
            }
            selected = null
        }
    }

    private fun makeMove(i : Int, j : Int) {
        Log.i("Main", "Move from (${selected!!.i}, ${selected!!.j}) -> ($i, $j)")
        val iprev = selected!!.i;
        val jprev = selected!!.j;
        val function = selected!!.validMoves[Pair(i, j)] ?: return
        function(board, viewGrid, selected!!, i, j)
        turnColor = if (turnColor == Color.WHITE) Color.BLACK else Color.WHITE
        sendData.sendGameChoice(iprev, jprev, i, j)
        update()
        Log.i("Main", "new pos: ${selected!!.i}, ${selected!!.j}")
    }
     fun makeMove(iprev:Int, jprev:Int, i : Int, j : Int) {
         if (board[iprev][jprev] != null &&
             board[iprev][jprev]!!.color == turnColor &&
             board[iprev][jprev]!!.validMoves.isNotEmpty()) {
             selected = board[iprev][jprev]
         }
            Log.i("Main", "$iprev, $jprev, $i, $j g djfslf")

             Toast.makeText(cntxt, selected.toString(),Toast.LENGTH_SHORT).show()
        //Log.i("Main", "Move from (${selected!!.i}, ${selected!!.j}) -> ($i, $j)")
        val function = selected!!.validMoves[Pair(i, j)] ?: return
        function(board, viewGrid, selected!!, i, j)
        turnColor = if (turnColor == Color.WHITE) Color.BLACK else Color.WHITE
        update()
        Log.i("Main", "new pos: ${selected!!.i}, ${selected!!.j}")
         for ((pos, _) in selected!!.validMoves) {
             viewGrid[pos.first][pos.second].setBackgroundResource(empty)
         }
         selected = null
    }

    private fun update() {
        var turnList = whitePieces
        var otherList = blackPieces
        var turnKing = whiteKing
        if (turnColor == Color.BLACK) {
            turnList = blackPieces
            otherList = whitePieces
            turnKing = blackKing
        }
        control.clear()
        checkList.clear()
        var it = otherList.iterator()
        while (it.hasNext()) {
            val piece = it.next()
            if (piece.alive) {
                piece.addControl(board, control, checkList)
            } else {
                it.remove()
            }
        }
        it = turnList.iterator()
        while (it.hasNext()) {
            val piece = it.next()
            if (piece.alive){
                piece.updateValidMoves(board, control)
            } else {
                it.remove()
            }
        }
        Log.i("Main", "checklist for $turnColor")
        for (piece in checkList) {
            Log.i("Main", "${piece.color}, $piece, ${piece.j}")
        }
        turnKing.restrictMoves(board, checkList, turnList)
    }
}