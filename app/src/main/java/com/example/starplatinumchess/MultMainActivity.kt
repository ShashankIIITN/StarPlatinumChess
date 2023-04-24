package com.example.starplatinumchess

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import com.example.starplatinumchess.databinding.ActivityMainMultBinding
import com.example.starplatinumchess.game.Chessboard
import com.example.starplatinumchess.game.gameChoice
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.util.Random
import kotlin.text.Charsets.UTF_8


const val empty = android.R.color.transparent

var Black: Boolean = false

var OpChoice: gameChoice? = null


typealias Array2D<T> = Array<Array<T>>

var opponentEndpointId: String? = null
var opponentName: String? = null

//var opponentChoice: GameChoice? = null
lateinit var connectionsClient: ConnectionsClient
lateinit var cntxt: Context
lateinit var chessboard: Chessboard
private var totalMatches: Int = 0
private var MatchesWon: Int = 0
private lateinit var userId: String
private var usrPoints: Int = 0

class MultMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMultBinding
    private lateinit var database: DatabaseReference

    private lateinit var tempGrid:Array2D<ImageView?>

    var endPoints = arrayOf<String>()
    val map = mutableMapOf<String, String>()
    var sentReq = false
    private var usrName: String? = null


    private val STRATEGY = Strategy.P2P_STAR

    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    private var opponentScore = 0

    private var myCodeName: String = CodenameGenerator.generate()
    private var myScore = 0
//    private var myChoice: GameChoice? = null

    private lateinit var myDialog: Dialog

    private var first = true

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_mult)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_mult)
        Initit()

        //GameStart()
        cntxt = this

        binding.msgBttn.setOnClickListener {
            showpopup(1)
        }
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.Draw -> ShowDialog(0)
                R.id.Resign -> ShowDialog(1)
            }
            false;
        }

        usrName = intent.getStringExtra("UserName")
        usrPoints = intent.getIntExtra("UserPoints", 0)
        userId = intent.getStringExtra("UserId")!!
        totalMatches = intent.getIntExtra("totalMatches", 0)
        MatchesWon = intent.getIntExtra("MatchesWon", 0)
        database = Firebase.database.reference


//        Toast.makeText(this, usrName, Toast.LENGTH_SHORT).show()
        myCodeName = usrName!!

        myDialog = Dialog(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (opponentEndpointId.isNullOrBlank()) {

                    if (findViewById<Button>(R.id.findOpponent).visibility.equals(View.VISIBLE)) {
                        finish()
                    } else {

                        connectionsClient.stopAdvertising()
                        connectionsClient.stopDiscovery()
                        findViewById<Button>(R.id.findOpponent).visibility = View.VISIBLE
                        binding.status.visibility = View.GONE
                    }
                } else {
                    val builder = AlertDialog.Builder(this@MultMainActivity)
                    builder.setTitle("Disconnect!!")
                    builder.setMessage("Are you sure you want to disconnect, you will lose the match !! ")
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    builder.setPositiveButton("Yes") { _, _ ->

                        connectionsClient.stopAdvertising()
                        connectionsClient.stopDiscovery()
                        UpdateFireBaseData(-50)
                        opponentEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
                        resetGame()
                        Toast.makeText(
                            applicationContext, "Disconnected Successfully", Toast.LENGTH_LONG
                        ).show()

                    }
                    builder.setNegativeButton("No") { dialogInterface, which ->
//                        Toast.makeText(applicationContext, "clicked No", Toast.LENGTH_LONG).show()
                        dialogInterface.dismiss()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
        })

        connectionsClient = Nearby.getConnectionsClient(this)
        findViewById<TextView>(R.id.myName).text = "You\n($myCodeName)"
        binding.findOpponent.setOnClickListener {
            startAdvertising()
            startDiscovery()

            binding.status.text = "Searching for opponents..."
            binding.status.visibility = View.VISIBLE

            binding.findOpponent.visibility = View.GONE

//            findViewById<ConstraintLayout>(R.id.conLayout_1).visibility = View.VISIBLE
//            findViewById<ConstraintLayout>(R.id.conLayout_1).rotation = 180F
//
//
//            GameStart()


//            findViewById<AppCompatButton>(R.id.disconnect).visibility = View.VISIBLE
        }
//        Main.apply {
//            findViewById<AppCompatButton>(R.id.rock).setOnClickListener { sendGameChoice(GameChoice.ROCK) }
//            findViewById<AppCompatButton>(R.id.paper).setOnClickListener { sendGameChoice(GameChoice.PAPER) }
//            findViewById<AppCompatButton>(R.id.scissors).setOnClickListener {
//                sendGameChoice(
//                    GameChoice.SCISSORS
//                )
//            }
//        }
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
            ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_ADVERTISE,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.NEARBY_WIFI_DEVICES

                ), REQUEST_CODE_REQUIRED_PERMISSIONS
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
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
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

//     fun sendGameChoice(choice: GameChoice) {
//        myChoice = choice
//        connectionsClient.sendPayload(
//            opponentEndpointId!!,
//            Payload.fromBytes(choice.name.toByteArray(UTF_8))
//        )
//        binding.status.text = "You chose ${choice.name}"
//
//        setGameControllerEnabled(false)
//    }

    fun sendGameChoice(iprev: Int, jprev: Int, i: Int, j: Int, type: Int, msg: String?) {

        val gson = Gson()
//        val gameCh = gson.toJson(gameChoice(selected, i, j))
        val gameCh = gson.toJson(gameChoice(iprev, jprev, i, j, -1, type, msg))
        Log.i("MainMSG", msg + "inside Send Game Choice")
//        val gamech1 = gameChoice(selected, i, j)

        connectionsClient.sendPayload(
            opponentEndpointId!!, Payload.fromBytes(gameCh.toByteArray(UTF_8))
        )

//        setGameControllerEnabled(false)
    }

//    private fun setGameControllerEnabled(state: Boolean) {
//        Main.apply {
//            findViewById<AppCompatButton>(R.id.rock).isEnabled = state
//            findViewById<AppCompatButton>(R.id.paper).isEnabled = state
//            findViewById<AppCompatButton>(R.id.scissors).isEnabled = state
//        }
//    }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        lateinit var opData: String
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                opData = String(it, UTF_8)
            }

            val gson = Gson()
////
            OpChoice = gson.fromJson(opData, gameChoice::class.java)
            Log.i("Main", OpChoice.toString() + "sadasfasf")

            if (OpChoice!!.type == 0) {

//            Toast.makeText(
//                baseContext, "i: ${OpChoice!!.i} j: ${OpChoice!!.j}", Toast.LENGTH_SHORT
//            ).show()

//                chessboard.onCellSelected(Pair(OpChoice!!.i, OpChoice!!.j), OpChoice!!.selected)
                chessboard.makeMove(OpChoice!!.iprev, OpChoice!!.jprev, OpChoice!!.i, OpChoice!!.j, OpChoice!!.choice)
            } else if (OpChoice!!.type == 1) {
                Toast.makeText(
                    baseContext, "${OpChoice!!.msg}", Toast.LENGTH_SHORT
                ).show()
            } else if (OpChoice!!.type == 2) {
                ShowDialog(2)
            } else if (OpChoice!!.type == 3) {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
                resetGame()
                Toast.makeText(
                    applicationContext, "The Match Ended in Draw", Toast.LENGTH_LONG
                ).show()
            } else if (OpChoice!!.type == 4) {
                Toast.makeText(baseContext, "Rejected Draw Request!!", Toast.LENGTH_SHORT).show()
            } else if (OpChoice!!.type == 5) {

                Toast.makeText(
                    baseContext,
                    "Other Player Resigned, You have Won the Match!!",
                    Toast.LENGTH_SHORT
                ).show()
                UpdateFireBaseData(+50)
                resetGame()
            }

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
//            if (update.status == PayloadTransferUpdate.Status.SUCCESS && OpChoice != null) {
////                Toast.makeText(
////                    baseContext,
////                    "${OpChoice!!.selected} i: ${OpChoice!!.i} j: ${OpChoice!!.j}",
////                    Toast.LENGTH_SHORT
////                ).show()
////                Toast.makeText(
////                    baseContext,
////                    "i: ${OpChoice!!.i} j: ${OpChoice!!.j}",
////                    Toast.LENGTH_SHORT
////                ).show()
////
//////                chessboard.onCellSelected(Pair(OpChoice!!.i, OpChoice!!.j), OpChoice!!.selected)
////                chessboard.makeMove(OpChoice!!.iprev, OpChoice!!.jprev, OpChoice!!.i, OpChoice!!.j)
//
////                val mc = myChoice!!
////                val oc = opponentChoice!!
////                when {
////                    mc.beats(oc) -> {
////                        binding.status.text = "${mc.name} beats ${oc.name}"
////                        myScore++
////                    }
////
////                    mc == oc -> {
////                        binding.status.text = "You both chose ${mc.name}"
////                    }
////
////                    else -> {
////                        binding.status.text = "${mc.name} loses to ${oc.name}"
////                        opponentScore++
////                    }
////                }
////                findViewById<TextView>(R.id.score).text = "$myScore : $opponentScore"
//                myChoice = null
//                opponentChoice = null
//                setGameControllerEnabled(true)
//            }
        }
    }

    fun ShowDialog(type: Int) {
        val builder = AlertDialog.Builder(this@MultMainActivity)
        if (type == 2) {
            builder.setTitle("Draw!!")
            builder.setMessage("Opponent has Proposed to Draw the match, would You like to Accept? ")
            builder.setIcon(android.R.drawable.ic_dialog_info)

            builder.setPositiveButton("Accept") { _, _ ->

                sendGameChoice(0, 0, 0, 0, 3, null)
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
                resetGame()
                Toast.makeText(
                    applicationContext, "The Match Ended in Draw", Toast.LENGTH_LONG
                ).show()
            }
            builder.setNegativeButton("Deny") { dialogInterface, which ->
                Toast.makeText(applicationContext, "Rejected Draw Request!!", Toast.LENGTH_LONG)
                    .show()
                sendGameChoice(0, 0, 0, 0, 4, null)
            }
        } else if (type == 0) {
            builder.setTitle("Draw!!")
            builder.setMessage("Would You like to Propose a Draw? ")
            builder.setIcon(android.R.drawable.ic_dialog_info)

            builder.setPositiveButton("Yes") { _, _ ->
                Toast.makeText(
                    applicationContext, "Draw Request Send!", Toast.LENGTH_LONG
                ).show()
                sendGameChoice(0, 0, 0, 0, 2, null)
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
        } else if (type == 1) {
            builder.setTitle("Resign!!")
            builder.setMessage("Are you sure you want to Resign, you will lose the match !! ")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            builder.setPositiveButton("Yes") { _, _ ->
                sendGameChoice(0, 0, 0, 0, 5, null)
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
                resetGame()
                Toast.makeText(
                    applicationContext, "Disconnected Successfully", Toast.LENGTH_LONG
                ).show()
                UpdateFireBaseData(-50)
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
        }else if (type == 4){
            connectionsClient.stopAdvertising()
            connectionsClient.stopDiscovery()
            builder.setTitle("LOST!!")
            builder.setMessage("You have Lost the Match!! ")
            builder.setIcon(R.drawable.baseline_assist_walker_24)
            UpdateFireBaseData(-50)
            builder.setPositiveButton("Ok") { _, _ ->
                Toast.makeText(
                    this, "Lost 50 Points!!", Toast.LENGTH_LONG
                ).show()
            }
            resetGame()
        }else if (type == 5){
            connectionsClient.stopAdvertising()
            connectionsClient.stopDiscovery()
            builder.setTitle("WON!!")
            builder.setMessage("You have WON the Match!! ")
            builder.setIcon(R.drawable.baseline_rowing_24)
            UpdateFireBaseData(50)
            builder.setPositiveButton("Ok") { _, _ ->
                Toast.makeText(
                    this, "Gained 50 Points!!", Toast.LENGTH_LONG
                ).show()
            }
            resetGame()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()

    }

    internal object CodenameGenerator {
        private val COLORS = arrayOf(
            "Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet", "Purple", "Lavender"
        )
        private val TREATS = arrayOf(
            "Cupcake",
            "Donut",
            "Eclair",
            "Froyo",
            "Gingerbread",
            "Honeycomb",
            "Ice Cream Sandwich",
            "Jellybean",
            "Kit Kat",
            "Lollipop",
            "Marshmallow",
            "Nougat",
            "Oreo",
            "Pie"
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
                            applicationContext, "Accepted Successfully", Toast.LENGTH_LONG
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
                                applicationContext, "Accepted Successfully", Toast.LENGTH_LONG
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                        }
                    opponentName = "Opponent\n(${info.endpointName})"
                    Black = true
                }
                builder.setNegativeButton("No") { dialogInterface, which ->
                    connectionsClient.rejectConnection(endpointId)
                    Toast.makeText(
                        applicationContext,
                        "Rejected ${info.endpointName}'s Request!!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }

        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                sentReq = false
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId = endpointId
                binding.opponentName.text = opponentName
                binding.status.text = "Connected"
                binding.status.visibility = View.GONE
//                setGameControllerEnabled(true)
                findViewById<ConstraintLayout>(R.id.conLayout_1).visibility = View.VISIBLE
                findViewById<CoordinatorLayout>(R.id.coorlay).visibility = View.VISIBLE

                if (Black) {
                    findViewById<ConstraintLayout>(R.id.conLayout_1).rotation = 180F
                }

                binding.myName.visibility = View.VISIBLE
                binding.opponentName.visibility = View.VISIBLE

                GameStart()
            } else {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()

                Toast.makeText(
                    baseContext, result.status.statusMessage.toString(), Toast.LENGTH_SHORT
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
//        opponentChoice = null
        opponentScore = 0
//        myChoice = null
        myScore = 0
        sentReq = false

//        findViewById<AppCompatButton>(R.id.disconnect).visibility = View.GONE
        binding.findOpponent.visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.conLayout_1).visibility = View.GONE
        findViewById<CoordinatorLayout>(R.id.coorlay).visibility = View.GONE
//        setGameControllerEnabled(false)
        binding.opponentName.text = "opponent\n(none yet)"
        binding.opponentName.visibility = View.GONE
        binding.myName.visibility = View.GONE
    }

    private fun startAdvertising() {

        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            myCodeName, packageName, connectionLifecycleCallback, options
        ).addOnSuccessListener {
            Toast.makeText(this, "Started Advertising", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Toast.makeText(baseContext, "Found Endpoint: " + info.endpointName, Toast.LENGTH_SHORT).show()
            if (info.endpointName !in endPoints) {
                endPoints += info.endpointName
                map[endpointId] = info.endpointName
            }
            showpopup(0)

        }

        override fun onEndpointLost(endpointId: String) {
            Toast.makeText(baseContext, "Endpont lost  ${map[endpointId]}", Toast.LENGTH_SHORT).show()

            if (map[endpointId] in endPoints) {
                endPoints = endPoints.filter { it != map[endpointId] }.toTypedArray()
                map.remove(endpointId)
            }
            if (myDialog.isShowing) {
                myDialog.dismiss()

                if (endPoints.isNotEmpty()) {
                    showpopup(0)
                }
            }
        }
    }

    private fun startDiscovery() {

        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName, endpointDiscoveryCallback, options)
            .addOnSuccessListener {
                Toast.makeText(this, "Started Discovering", Toast.LENGTH_SHORT).show()
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

    private fun showpopup(type: Int) {

        if (type == 0) {

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
                connectionsClient.requestConnection(
                    myCodeName, endpointId, connectionLifecycleCallback
                ).addOnSuccessListener {
                    sentReq = true
                    Toast.makeText(baseContext, "Reg Sent!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(baseContext, it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            txtclose.setOnClickListener {
                myDialog.dismiss()
            }
            myDialog.show()
        } else if (type == 1) {
            myDialog.setContentView(R.layout.activity_popup_window2)
            val txtclose: TextView = myDialog.findViewById(R.id.txtclose)
            val snd: Button = myDialog.findViewById(R.id.send_btn)
            val msg = myDialog.findViewById<EditText>(R.id.msg)
            snd.setOnClickListener {
                sendGameChoice(0, 0, 0, 0, 1, msg.text.toString())
                Log.i("MainMSG", msg.text.toString() + "asdasfsaf")
                myDialog.dismiss()
            }
            txtclose.setOnClickListener {
                myDialog.dismiss()
            }
            myDialog.show()
        }

    }

    private fun GameStart() {
        if (first) {
            tempGrid = Array2D<ImageView?>(8) {
                Array(8) { null }
            }
            tempGrid[0][0] = binding.sampleCell
            if (Black) {
                tempGrid[0][0]?.rotation = 180f
            }
            for (j in 1 until 8) {
                val cell = ImageView(this)
                cell.layoutParams = binding.sampleCell.layoutParams
                if (Black) {
                    cell.rotation = 180f
                }
                binding.sampleRow.addView(cell)
                tempGrid[0][j] = cell
            }
            for (i in 1 until 8) {
                val row = LinearLayout(this)
                row.layoutParams = binding.sampleRow.layoutParams
                for (j in 0 until 8) {
                    val cell = ImageView(this)
                    if (Black) {
                        cell.rotation = 180f
                    }
                    cell.layoutParams = binding.sampleCell.layoutParams
                    row.addView(cell)
                    tempGrid[i][j] = cell
                }
                binding.chessboard.addView(row)
            }
            first = false
        }
        chessboard = Chessboard(tempGrid, this@MultMainActivity)
    }

    internal fun UpdateFireBaseData(Pts: Int) {

        usrPoints += Pts
        totalMatches++
        if (Pts > 0) MatchesWon++
        database.child("Users").child(userId).child("points").setValue(usrPoints)
        database.child("Users").child(userId).child("MatchesPlayed").setValue(totalMatches)
        database.child("Users").child(userId).child("MatchesWon").setValue(MatchesWon)
    }

    fun Initit(){

    }

}
