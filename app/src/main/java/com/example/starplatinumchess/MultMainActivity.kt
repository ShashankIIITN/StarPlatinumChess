package com.example.starplatinumchess

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.Dispatchers.Main
import java.util.Random
import kotlin.text.Charsets.UTF_8

private enum class GameChoice {
    ROCK, PAPER, SCISSORS;

    fun beats(other: GameChoice): Boolean =
        (this == ROCK && other == SCISSORS)
                || (this == SCISSORS && other == PAPER)
                || (this == PAPER && other == ROCK)
}

class MultMainActivity : AppCompatActivity() {
    var endPoints = arrayOf<String>()
    val map = mutableMapOf<String, String>()
    var sentReq = false;
    private var  usrName : String? = null
    private var  usrPoints : Int = 0

    private val STRATEGY = Strategy.P2P_STAR
    private lateinit var connectionsClient: ConnectionsClient
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    private var opponentName: String? = null
    private var opponentEndpointId: String? = null
    private var opponentScore = 0
    private var opponentChoice: GameChoice? = null
    private  var myCodeName: String = CodenameGenerator.generate()
    private var myScore = 0
    private var myChoice: GameChoice? = null

    private lateinit var myDialog: Dialog

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_mult)


        usrName = intent.getStringExtra("UserName")
        usrPoints = intent.getIntExtra("UserPoints", 0)

        Toast.makeText(this, usrName, Toast.LENGTH_SHORT).show()
        myCodeName = usrName!!

        myDialog = Dialog(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (opponentEndpointId.isNullOrBlank()) {
                    finish()
                } else {
                    val builder = AlertDialog.Builder(this@MultMainActivity)
                    builder.setTitle("Disconnect!!")
                    builder.setMessage("Are you sure you want to disconnect, you will lose the match !! ")
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    builder.setPositiveButton("Yes") { _, _ ->

                        connectionsClient.stopAdvertising()
                        connectionsClient.stopDiscovery()
                        opponentEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
                        resetGame()
                        Toast.makeText(
                            applicationContext,
                            "Disconnected Successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    builder.setNegativeButton("No") { dialogInterface, which ->
                        Toast.makeText(applicationContext, "clicked No", Toast.LENGTH_LONG).show();
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
        })

        connectionsClient = Nearby.getConnectionsClient(this)
        findViewById<TextView>(R.id.myName).text = "You\n($myCodeName)"
        findViewById<AppCompatButton>(R.id.findOpponent).setOnClickListener {
            startAdvertising()
            startDiscovery()

            findViewById<TextView>(R.id.status).text = "Searching for opponents..."

            findViewById<AppCompatButton>(R.id.findOpponent).visibility = View.GONE
//            findViewById<AppCompatButton>(R.id.disconnect).visibility = View.VISIBLE
        }
        Main.apply {
            findViewById<AppCompatButton>(R.id.rock).setOnClickListener { sendGameChoice(GameChoice.ROCK) }
            findViewById<AppCompatButton>(R.id.paper).setOnClickListener { sendGameChoice(GameChoice.PAPER) }
            findViewById<AppCompatButton>(R.id.scissors).setOnClickListener {
                sendGameChoice(
                    GameChoice.SCISSORS
                )
            }
        }
//        findViewById<AppCompatButton>(R.id.disconnect).setOnClickListener {
//        }

        resetGame()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @CallSuper
    override fun onStart() {
        super.onStart()

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                android.Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
            ||checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_ADVERTISE, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT

                ),
                REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ), REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
    }

    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val errMsg = "Cannot start without required permissions"
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "denied: $errMsg", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
            }
            recreate()
        }
    }

    private fun sendGameChoice(choice: GameChoice) {
        myChoice = choice
        connectionsClient.sendPayload(
            opponentEndpointId!!,
            Payload.fromBytes(choice.name.toByteArray(UTF_8))
        )
        findViewById<TextView>(R.id.status).text = "You chose ${choice.name}"

        setGameControllerEnabled(false)
    }

    private fun setGameControllerEnabled(state: Boolean) {
        Main.apply {
            findViewById<AppCompatButton>(R.id.rock).isEnabled = state
            findViewById<AppCompatButton>(R.id.paper).isEnabled = state
            findViewById<AppCompatButton>(R.id.scissors).isEnabled = state
        }
    }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                opponentChoice = GameChoice.valueOf(String(it, UTF_8))
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS
                && myChoice != null && opponentChoice != null
            ) {
                val mc = myChoice!!
                val oc = opponentChoice!!
                when {
                    mc.beats(oc) -> {
                        findViewById<TextView>(R.id.status).text = "${mc.name} beats ${oc.name}"
                        myScore++
                    }

                    mc == oc -> {
                        findViewById<TextView>(R.id.status).text = "You both chose ${mc.name}"
                    }

                    else -> {
                        findViewById<TextView>(R.id.status).text = "${mc.name} loses to ${oc.name}"
                        opponentScore++
                    }
                }
                findViewById<TextView>(R.id.score).text = "$myScore : $opponentScore"
                myChoice = null
                opponentChoice = null
                setGameControllerEnabled(true)
            }
        }
    }

    internal object CodenameGenerator {
        private val COLORS = arrayOf(
            "Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet", "Purple", "Lavender"
        )
        private val TREATS = arrayOf(
            "Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb",
            "Ice Cream Sandwich", "Jellybean", "Kit Kat", "Lollipop", "Marshmallow", "Nougat",
            "Oreo", "Pie"
        )
        private val generator = Random()

        /** Generate a random Android agent codename  */
        fun generate(): String {
            val color = COLORS[generator.nextInt(COLORS.size)]
            val treat = TREATS[generator.nextInt(TREATS.size)]
            return "$color $treat"
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {

            //show alert for connection request
            if (sentReq) {
                connectionsClient.acceptConnection(endpointId, payloadCallback)
                    .addOnSuccessListener {
                        myDialog.dismiss()
                        Toast.makeText(
                            applicationContext,
                            "Accepted Successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }.addOnFailureListener {
                        Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                opponentName = "Opponent\n(${info.endpointName})"
            } else {
                val builder =
                    AlertDialog.Builder((if (myDialog.isShowing) myDialog.context else this@MultMainActivity))
                builder.setTitle("Accept Challenge!!")
                builder.setMessage("Do you want to accept ${info.endpointName}'s challenge ?? ")
                builder.setIcon(android.R.drawable.ic_dialog_info)

                builder.setPositiveButton("Accept") { dialogInterface, which ->
                    connectionsClient.acceptConnection(endpointId, payloadCallback)
                        .addOnSuccessListener {
                            myDialog.dismiss()
                            Toast.makeText(
                                applicationContext,
                                "Accepted Successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                        }
                    opponentName = "Opponent\n(${info.endpointName})"
                }
                builder.setNegativeButton("No") { dialogInterface, which ->
                    connectionsClient.rejectConnection(endpointId)
                    Toast.makeText(
                        applicationContext,
                        "Rejected ${info.endpointName}'s Request!!",
                        Toast.LENGTH_LONG
                    ).show();
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }

        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                sentReq = false;
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId = endpointId
                findViewById<TextView>(R.id.opponentName).text = opponentName
                findViewById<TextView>(R.id.status).text = "Connected"
                setGameControllerEnabled(true)
            } else {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()

                Toast.makeText(
                    baseContext,
                    result.status.statusMessage.toString(),
                    Toast.LENGTH_SHORT
                ).show();
            }
        }

        override fun onDisconnected(endpointId: String) {
            resetGame()
        }
    }

    private fun resetGame() {

        opponentEndpointId = null
        opponentName = null
        opponentChoice = null
        opponentScore = 0
        myChoice = null
        myScore = 0
        sentReq = false

//        findViewById<AppCompatButton>(R.id.disconnect).visibility = View.GONE
        findViewById<AppCompatButton>(R.id.findOpponent).visibility = View.VISIBLE
        setGameControllerEnabled(false)
        findViewById<TextView>(R.id.opponentName).text = "opponent\n(none yet)"
        findViewById<TextView>(R.id.status).text = "..."
        findViewById<TextView>(R.id.status).text = ":"
    }

    private fun startAdvertising() {

        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            myCodeName,
            packageName,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            Toast.makeText(this, "Started Advertising", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Toast.makeText(baseContext, "found" + info.endpointName, Toast.LENGTH_SHORT).show()
            if (info.endpointName !in endPoints) {
                endPoints += info.endpointName
                map[endpointId] = info.endpointName
            }
            showpopup()

        }

        override fun onEndpointLost(endpointId: String) {
            Toast.makeText(baseContext, "found lost ${map[endpointId]}", Toast.LENGTH_SHORT).show()

            if (map[endpointId] in endPoints) {
                endPoints = endPoints.filter { it != map[endpointId] }.toTypedArray()
                map.remove(endpointId)
            }
            if (myDialog.isShowing) {
                myDialog.dismiss()

                if (endPoints.isNotEmpty()) {
                    showpopup()
                }
            }
        }
    }

    private fun startDiscovery() {

        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName, endpointDiscoveryCallback, options)
            .addOnSuccessListener {
                Toast.makeText(this, "Stated Discovering", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    @CallSuper
    override fun onStop() {
        connectionsClient.apply {
            stopAdvertising()
            stopDiscovery()
            stopAllEndpoints()
        }
        resetGame()
        super.onStop()
    }

    private fun <K, V> getKey(map: Map<K, V>, target: V): K {
        return map.keys.first { target == map[it] };
    }

    private fun showpopup() {
        myDialog.setContentView(R.layout.activity_pop_wondow)
        val txtclose: TextView = myDialog.findViewById(R.id.txtclose)
        val Connect: Button = myDialog.findViewById(R.id.connext_btn)
        val sp = myDialog.findViewById<Spinner>(R.id.spinner)
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, endPoints)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp.adapter = aa
        Connect.setOnClickListener {

            val endpointName = sp.selectedItem
            val endpointId = getKey(map, endpointName)

            sentReq = true
            connectionsClient.requestConnection(myCodeName, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    sentReq = true
                    Toast.makeText(baseContext, "Reg Sent!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(baseContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
        }
        txtclose.setOnClickListener {
            myDialog.dismiss()
        }
        myDialog.show()

    }

}
