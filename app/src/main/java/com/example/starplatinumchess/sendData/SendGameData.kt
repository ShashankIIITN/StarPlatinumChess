package com.example.starplatinumchess.sendData

import android.util.Log
import android.widget.Toast
import com.example.starplatinumchess.cntxt
import com.example.starplatinumchess.connectionsClient
import com.example.starplatinumchess.game.gameChoice
import com.example.starplatinumchess.opponentEndpointId
import com.google.android.gms.nearby.connection.Payload
import com.google.common.base.Charsets.UTF_8
import com.google.gson.Gson


class SendGameData {
    //    private lateinit var connectionsClient: ConnectionsClient
    fun sendGameChoice( iprev: Int, jprev: Int, i: Int, j: Int, choice: Int, type:Int) {

        Log.i("Main", "send game choice: $iprev, $jprev, $i, $j")
        val gson = Gson()
        try {
//            val gameCh = gson.toJson(gameChoice(selected, i, j))
            val gameCh = gson.toJson(gameChoice(iprev, jprev, i, j, choice, type))


            connectionsClient.sendPayload(
                opponentEndpointId!!,
                Payload.fromBytes(gameCh.toByteArray(UTF_8))
            ).addOnSuccessListener {
//                Toast.makeText(cntxt, "Sent Succesfully!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(cntxt, "sending failed : " + it.message, Toast.LENGTH_SHORT).show()
            }
//        findViewById<TextView>(R.id.status).text = "You chose ${choice.name}"
        } catch (e: Exception) {
            Toast.makeText(cntxt, e.message, Toast.LENGTH_SHORT).show()
        }
    }

}