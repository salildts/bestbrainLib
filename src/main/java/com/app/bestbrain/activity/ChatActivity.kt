package com.app.bestbrain.activity

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.app.bestbrain.adapter.ChatAdapter
import com.app.bestbrain.adapter.ChatButtonAdapter
import com.app.bestbrain.databinding.ActivityChatBinding
import com.app.bestbrain.models.ChatMessageModel
import com.app.bestbrain.models.SessionIdResponse
import com.app.bestbrain.network.RetrofitInstance
import com.app.bestbrain.utils.Constants
import com.app.bestbrain.utils.ProgressDialog
import com.app.bestbrain.utils.SharedPreferenceManager
import com.app.bestbrain.utils.SpaceItemDecoration
import com.google.gson.GsonBuilder
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URISyntaxException


class ChatActivity : AppCompatActivity(), ChatButtonAdapter.ChatButtonClickListener {

    private lateinit var binding: ActivityChatBinding
    private lateinit var mSocket: Socket
    private var sessionId: String? = null
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var pd: ProgressDialog
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Constants.API_BASE_URL.isEmpty())
            throw Exception("API BASE URL is empty")
        if (Constants.SOCKET_URL.isEmpty())
            throw Exception("Socket URL is empty")
        if (Constants.API_KEY.isEmpty())
            throw Exception("API Key is empty")
        if (Constants.APP_ID.isEmpty())
            throw Exception("APP ID is empty")

        sharedPreferenceManager = SharedPreferenceManager(this)
        sessionId = sharedPreferenceManager.sessionId

        initView()
    }

    private fun initView() {
        pd = ProgressDialog(this, "", false)
        initSocket()
        connectSocket()

        chatAdapter = ChatAdapter(this, this)
        binding.rvChat.adapter = chatAdapter
        binding.rvChat.addItemDecoration(SpaceItemDecoration(10))

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSend.setOnClickListener {
            val messageStr = binding.edtMessage.text.toString().trim()
            if (messageStr.isNotEmpty()) {
                if (!sessionId.isNullOrEmpty()) {
                    sendMessage(messageStr)
                }
            }
        }

        getSessionId()
    }

    private fun initSocket() {
        try {
            mSocket = IO.socket(Constants.SOCKET_URL)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
    }

    private fun connectSocket() {
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.on("channel_chat_reply", onNewMessage)
        mSocket.connect()
    }

    private fun disconnectSocket() {
        mSocket.disconnect()
        mSocket.off(Socket.EVENT_CONNECT, onConnect)
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket.off("channel_chat_reply", onNewMessage)
    }

    private val onConnect =
        Emitter.Listener { args: Array<Any?>? ->
            Log.e("Status", "Connected")
        }

    private val onDisconnect = Emitter.Listener { args: Array<Any?>? ->
        Log.e("Status", "disconnected")
    }

    private val onConnectError =
        Emitter.Listener { args: Array<Any?>? ->
            Log.e(
                "Error",
                args.toString()
            )
        }

    private val onNewMessage =
        Emitter.Listener { args ->
            runOnUiThread {
                try {
                    val data = args[0] as JSONObject
                    Log.e("data====", data.toString())
                    val mGson = GsonBuilder().create()
                    val chatMessageModel = mGson.fromJson(
                        data.toString(),
                        ChatMessageModel::class.java
                    )
                    chatMessageModel.itemType = 2
                    if (chatMessageModel.session_id == sessionId && chatMessageModel.data?.bb_type != "end_session")
                        chatAdapter.addItem(chatMessageModel)

                    if (chatMessageModel.data?.bb_type == "end_session")
                        sessionId = ""
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

    private fun getSessionId() {
        pd.showProgressDialog()
        val json = JSONObject()
        json.put("appId", Constants.APP_ID)
        json.put("session_id", sessionId)
        json.put("bb_agent_name", "Emily")
        val body: RequestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val call: Call<ResponseBody?> = RetrofitInstance.api.getSessionId(Constants.API_KEY, body)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                pd.hideProgressDialog()
                try {
                    if (response.body() != null) {
                        val apiResponse = response.body()!!.string()
                        val mGson = GsonBuilder().create()
                        val sessionIdResponse = mGson.fromJson(
                            apiResponse,
                            SessionIdResponse::class.java
                        )
                        sessionId = sessionIdResponse?.data?.session_id
                        val oldSessionId = sharedPreferenceManager.sessionId
                        if (oldSessionId.isNullOrEmpty() || oldSessionId != sessionId) {
                            sharedPreferenceManager.sessionId = sessionId
                            sendMessage("")
                        }

                    } else {
                        val apiResponse = response.errorBody()!!.string()
                        Log.i("error response===", apiResponse)
                    }


                } catch (e: Exception) {
                    Log.e("Error---", e.printStackTrace().toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.d("TAG", t.message.toString())
                pd.hideProgressDialog()
            }
        })
    }

    private fun sendMessage(message: String) {
        val jsonObject = JSONObject()
        jsonObject.put("channel", "websocket")
        jsonObject.put("session_id", sessionId)
        jsonObject.put("data", message)
        jsonObject.put("thread_id", "")

        mSocket.emit("channel_chat", jsonObject)

        if (message.isNotEmpty()) {
            val chatMessageModel = ChatMessageModel()
            chatMessageModel.itemType = 1
            chatMessageModel.session_id = sessionId
            val chatData = ChatMessageModel.Data()
            chatData.bb_type = "output_text"
            chatData.bb_value = message
            chatMessageModel.data = chatData
            chatAdapter.addItem(chatMessageModel)
        }

        binding.edtMessage.setText("")
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)
    }

    override fun onButtonClick(message: String) {
        sendMessage(message)
    }
}