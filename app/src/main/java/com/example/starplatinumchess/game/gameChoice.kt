package com.example.starplatinumchess.game

data class gameChoice(
   // val selected : ChessPiece?,
    val iprev : Int,
    val jprev : Int,
    val i : Int,
    val j : Int,
    val choice : Int,
    val type : Int = 0,
    val msg : String? = null
)