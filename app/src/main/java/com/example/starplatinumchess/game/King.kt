package com.example.starplatinumchess.game

import android.util.Log
import android.widget.ImageView
import com.example.starplatinumchess.Array2D
import com.example.starplatinumchess.R
import com.example.starplatinumchess.game.ChessPiece.Color
import java.util.LinkedList
import kotlin.math.min
import kotlin.math.sign

fun kingSimpleMove(
    board: Array2D<ChessPiece?>,
    viewGrid: Array2D<ImageView>,
    selected: ChessPiece,
    i: Int,
    j: Int
) {
    (selected as King).moved = true
    simpleMove(board, viewGrid, selected, i, j)
}

fun kingSideCastlingMove(
    board: Array2D<ChessPiece?>,
    viewGrid: Array2D<ImageView>,
    selected: ChessPiece,
    i: Int,
    j: Int
) {
    (selected as King).moved = true
    (board[i][0]!! as Rook).moved = true
    simpleMove(board, viewGrid, selected, selected.i, 1)
    simpleMove(board, viewGrid, board[i][0]!!, selected.i, 2)
}

fun queenSideCastlingMove(
    board: Array2D<ChessPiece?>,
    viewGrid: Array2D<ImageView>,
    selected: ChessPiece,
    i: Int,
    j: Int
) {
    (selected as King).moved = true
    (board[i][0]!! as Rook).moved = true
    simpleMove(board, viewGrid, selected, selected.i, 5)
    simpleMove(board, viewGrid, board[i][7]!!, selected.i, 4)
}

class King(override val color: Color) : ChessPiece {

    var moved = false

    override val res = if (color == Color.WHITE) R.drawable.w_king else R.drawable.b_king

    override val validMoves = HashMap<Pair<Int, Int>, MoveFunction>()
    override var alive = true
    override var i = 0
    override var j = 0

    private val okList = HashSet<Pair<Int, Int>>()

    override fun updateValidMoves(board: Array2D<ChessPiece?>, danger: HashSet<Pair<Int, Int>>) {
        validMoves.clear()
        val moves = arrayOf(
            Pair(i + 1, j + 1), Pair(i + 1, j - 1), Pair(i - 1, j + 1), Pair(i - 1, j - 1),
            Pair(i + 1, j), Pair(i - 1, j), Pair(i, j + 1), Pair(i, j - 1)
        )
        for (m in moves) {
            if (m.first in 0..7 && m.second in 0..7 && board[m.first][m.second]?.color != color && !danger.contains(
                    m
                )
            ) {
                validMoves[m] = ::kingSimpleMove
            }
        }
        if (!moved) {
            if (!danger.contains(Pair(i, 3)) &&
                board[i][2] == null && !danger.contains(Pair(i, 2)) &&
                board[i][1] == null && !danger.contains(Pair(i, 1)) &&
                board[i][0] is Rook && !(board[i][0] as Rook).moved
            ) {
                validMoves[Pair(i, 1)] = ::kingSideCastlingMove
            }
            if (!danger.contains(Pair(i, 3)) &&
                board[i][4] == null && !danger.contains(Pair(i, 4)) &&
                board[i][5] == null && !danger.contains(Pair(i, 5)) &&
                board[i][6] == null &&
                board[i][7] is Rook && !(board[i][7] as Rook).moved
            ) {
                validMoves[Pair(i, 5)] = ::queenSideCastlingMove
            }
        }
    }

    override fun addControl(
        board: Array2D<ChessPiece?>,
        control: HashSet<Pair<Int, Int>>,
        checkList: ArrayList<ChessPiece>
    ) {
        val moves = arrayOf(
            Pair(i + 1, j + 1), Pair(i + 1, j - 1), Pair(i - 1, j + 1), Pair(i - 1, j - 1),
            Pair(i + 1, j), Pair(i - 1, j), Pair(i, j + 1), Pair(i, j - 1)
        )
        for (m in moves) {
            if (m.first in 0..7 && m.second in 0..7) {
                if (board[m.first][m.second] is King)
                    checkList.add(this)
                control.add(m)
            }
        }
    }

    fun restrictMoves(
        board: Array2D<ChessPiece?>, checkList: ArrayList<ChessPiece>,
        pieces: LinkedList<ChessPiece>
    ) {
        when (checkList.size) {
            0 -> {
                Log.i("Main", "king safe. Restricting moves")
                var toRestrict: ChessPiece? = null
                var restrict = false
                okList.clear()
                for (k in 1..7 - i) {
                    val pos = Pair(i + k, j)
                    okList.add(pos)
                    if (board[pos.first][pos.second] != null) {
                        if (toRestrict == null) {
                            toRestrict = board[pos.first][pos.second]
                        } else {
                            if (board[pos.first][pos.second]!!.color == color ||
                                (board[pos.first][pos.second] !is Rook &&
                                        board[pos.first][pos.second] !is Queen)
                            ) {
                                toRestrict = null
                            } else {
                                restrict = true
                            }
                            break
                        }
                    }
                }
                if (restrict)
                    toRestrict!!.validMoves.keys.retainAll(okList)
                toRestrict = null
                restrict = false
                okList.clear()
                for (k in 1..i) {
                    val pos = Pair(i - k, j)
                    okList.add(pos)
                    if (board[pos.first][pos.second] != null) {
                        if (toRestrict == null) {
                            toRestrict = board[pos.first][pos.second]
                        } else {
                            if (board[pos.first][pos.second]!!.color == color ||
                                (board[pos.first][pos.second] !is Rook &&
                                        board[pos.first][pos.second] !is Queen)
                            ) {
                                toRestrict = null
                            } else {
                                restrict = true
                            }
                            break
                        }
                    }
                }
                if (restrict)
                    toRestrict!!.validMoves.keys.retainAll(okList)
                toRestrict = null
                restrict = false
                okList.clear()
                for (k in 1..7 - j) {
                    val pos = Pair(i, j + k)
                    okList.add(pos)
                    if (board[pos.first][pos.second] != null) {
                        if (toRestrict == null) {
                            toRestrict = board[pos.first][pos.second]
                        } else {
                            if (board[pos.first][pos.second]!!.color == color ||
                                (board[pos.first][pos.second] !is Rook &&
                                        board[pos.first][pos.second] !is Queen)
                            ) {
                                toRestrict = null
                            } else {
                                restrict = true
                            }
                            break
                        }
                    }
                }
                if (restrict)
                    toRestrict!!.validMoves.keys.retainAll(okList)
                toRestrict = null
                restrict = false
                okList.clear()
                for (k in 1..j) {
                    val pos = Pair(i, j - k)
                    okList.add(pos)
                    if (board[pos.first][pos.second] != null) {
                        if (toRestrict == null) {
                            toRestrict = board[pos.first][pos.second]
                        } else {
                            if (board[pos.first][pos.second]!!.color == color ||
                                (board[pos.first][pos.second] !is Rook &&
                                        board[pos.first][pos.second] !is Queen)
                            ) {
                                toRestrict = null
                            } else {
                                restrict = true
                            }
                            break
                        }
                    }
                }
                if (restrict)
                    toRestrict!!.validMoves.keys.retainAll(okList)
                toRestrict = null
                restrict = false
                okList.clear()
                for (k in 1..min(7 - i, 7 - j)) {
                    val pos = Pair(i + k, j + k)
                    okList.add(pos)
                    if (board[pos.first][pos.second] != null) {
                        if (toRestrict == null) {
                            toRestrict = board[pos.first][pos.second]
                        } else {
                            if (board[pos.first][pos.second]!!.color == color ||
                                (board[pos.first][pos.second] !is Rook &&
                                        board[pos.first][pos.second] !is Queen)
                            ) {
                                toRestrict = null
                            } else {
                                restrict = true
                            }
                            break
                        }
                    }
                }
                if (restrict)
                    toRestrict!!.validMoves.keys.retainAll(okList)
                toRestrict = null
                restrict = false
                okList.clear()
                for (k in 1..min(i, j)) {
                    val pos = Pair(i - k, j - k)
                    okList.add(pos)
                    if (board[pos.first][pos.second] != null) {
                        if (toRestrict == null) {
                            toRestrict = board[pos.first][pos.second]
                        } else {
                            if (board[pos.first][pos.second]!!.color == color ||
                                (board[pos.first][pos.second] !is Rook &&
                                        board[pos.first][pos.second] !is Queen)
                            ) {
                                toRestrict = null
                            } else {
                                restrict = true
                            }
                            break
                        }
                    }
                }
                if (toRestrict != null) {
                    Log.i("Main", "hello")
                }
                if (restrict)
                    toRestrict!!.validMoves.keys.retainAll(okList)
                toRestrict = null
                restrict = false
                okList.clear()
                for (k in 1..min(7 - i, j)) {
                    val pos = Pair(i + k, j - k)
                    okList.add(pos)
                    if (board[pos.first][pos.second] != null) {
                        if (toRestrict == null) {
                            toRestrict = board[pos.first][pos.second]
                        } else {
                            if (board[pos.first][pos.second]!!.color == color ||
                                (board[pos.first][pos.second] !is Rook &&
                                        board[pos.first][pos.second] !is Queen)
                            ) {
                                toRestrict = null
                            } else {
                                restrict = true
                            }
                            break
                        }
                    }
                }
                if (restrict)
                    toRestrict!!.validMoves.keys.retainAll(okList)
                toRestrict = null
                restrict = false
                okList.clear()
                for (k in 1..min(i, 7 - j)) {
                    val pos = Pair(i - k, j + k)
                    okList.add(pos)
                    if (board[pos.first][pos.second] != null) {
                        if (toRestrict == null) {
                            toRestrict = board[pos.first][pos.second]
                        } else {
                            if (board[pos.first][pos.second]!!.color == color ||
                                (board[pos.first][pos.second] !is Rook &&
                                        board[pos.first][pos.second] !is Queen)
                            ) {
                                toRestrict = null
                            } else {
                                restrict = true
                            }
                            break
                        }
                    }
                }
                if (restrict)
                    toRestrict!!.validMoves.keys.retainAll(okList)
            }

            1 -> {
                Log.i("Main", "King in danger from 1")
                val check = checkList[0]
                okList.clear()
                okList.add(Pair(check.i, check.j))
                if (check is Rook || check is Bishop || check is Queen) {

                    val dirI = (check.i - i).sign
                    val dirJ = (check.j - j).sign
                    var tempi = i
                    var tempj = j
                    while (tempi != check.i || tempj != check.j) {
                        tempi += dirI
                        tempj += dirJ
                        okList.add(Pair(tempi, tempj))
                    }
                }
                for (piece in pieces) {
                    if (piece != this)
                        piece.validMoves.keys.retainAll(okList)
                }
            }

            2 -> {
                Log.i("Main", "King in danger from 2!!")
                okList.clear()
                for (piece in pieces) {
                    if (piece != this) {
                        piece.validMoves.clear()
                    }
                }
            }
        }
    }
}
