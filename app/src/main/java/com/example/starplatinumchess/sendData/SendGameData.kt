package com.example.starplatinumchess.sendData

import android.util.Log
import android.widget.Toast
import com.example.starplatinumchess.OpChoice
import com.example.starplatinumchess.chessboard
import com.example.starplatinumchess.cntxt
import com.example.starplatinumchess.connectionsClient
import com.example.starplatinumchess.game.gameChoice
import com.example.starplatinumchess.opponentEndpointId
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.common.base.Charsets.UTF_8
import com.google.gson.Gson


class SendGameData {
    //    private lateinit var connectionsClient: ConnectionsClient
    fun sendGameChoice( iprev: Int, jprev: Int, i: Int, j: Int) {

        Log.i("Main", "send game choice: $iprev, $jprev, $i, $j")
        val gson = Gson()
        try {
//            val gameCh = gson.toJson(gameChoice(selected, i, j))
            val gameCh = gson.toJson(gameChoice(iprev, jprev, i, j))


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

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        lateinit var opData: String;
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
//            Toast.makeText(cntxt, "Got Response!", Toast.LENGTH_SHORT).show()
            payload.asBytes()?.let {
                opData = String(it, UTF_8)
            }

//            Toast.makeText(cntxt, "opData", Toast.LENGTH_SHORT).show()
//            val gson = Gson()
//
//            OpChoice = gson.fromJson(opData, gameChoice::class.java)

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS && OpChoice != null) {
//                Toast.makeText(
//                    cntxt,
//                    "${OpChoice!!.selected} i: ${OpChoice!!.i} j: ${OpChoice!!.j}",
//                    Toast.LENGTH_SHORT
//                ).show()
                Toast.makeText(
                    cntxt,
                    "i: ${OpChoice!!.i} j: ${OpChoice!!.j}",
                    Toast.LENGTH_SHORT
                ).show()

//                chessboard.onCellSelected(Pair(OpChoice!!.i, OpChoice!!.j), OpChoice!!.selected)
                chessboard.onCellSelected(Pair(OpChoice!!.i, OpChoice!!.j))

//                val mc = myChoice!!
//                val oc = opponentChoice!!
//                when {
//                    mc.beats(oc) -> {
//                        findViewById<TextView>(R.id.status).text = "${mc.name} beats ${oc.name}"
//                        myScore++
//                    }
//
//                    mc == oc -> {
//                        findViewById<TextView>(R.id.status).text = "You both chose ${mc.name}"
//                    }
//
//                    else -> {
//                        findViewById<TextView>(R.id.status).text = "${mc.name} loses to ${oc.name}"
//                        opponentScore++
//                    }
//                }
//                findViewById<TextView>(R.id.score).text = "$myScore : $opponentScore"
            } else {
                Toast.makeText(cntxt, "got error  ${update.status}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}