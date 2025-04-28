package com.app.bestbrain.fragment

import android.Manifest.permission
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.bestbrain.R
import com.app.bestbrain.activity.BBChatActivity
import com.app.bestbrain.adapter.BBChatAdapter
import com.app.bestbrain.databinding.FragmentChatBbScreenBinding
import com.app.bestbrain.init.BBInit
import com.app.bestbrain.models.ChatHistoryResponse
import com.app.bestbrain.models.ChatMessageModel
import com.app.bestbrain.models.SessionIdResponse
import com.app.bestbrain.network.RetrofitInstance
import com.app.bestbrain.utils.AudioRecording
import com.app.bestbrain.utils.AudioRecording.RecordCompleteListener
import com.app.bestbrain.utils.BBType
import com.app.bestbrain.utils.CommonMethods
import com.app.bestbrain.utils.ProgressDialog
import com.app.bestbrain.utils.SpaceItemDecoration
import com.google.gson.GsonBuilder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.net.URISyntaxException

class BBChatScreenFragment : Fragment() {

    private lateinit var binding: FragmentChatBbScreenBinding
    private lateinit var mSocket: Socket
    private var sessionId: String? = null
    private var sessionName: String? = null
    private lateinit var chatAdapter: BBChatAdapter
    private lateinit var audioRecording: AudioRecording
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var isRecording = false
    private lateinit var pd: ProgressDialog
    private var qrScanPosition = 0
    private var typingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey("session_name") == true)
            sessionName = arguments?.getString("session_name").toString()
        if (arguments?.containsKey("session_id") == true)
            sessionId = arguments?.getString("session_id").toString()

        pd = ProgressDialog(requireActivity(), "", false)

        audioRecording = AudioRecording(requireActivity(), object : RecordCompleteListener {

            override fun onRecordComplete(outputText: String?) {
                setAudioOutput(outputText)
            }

            override fun onRecordError(outputText: String?) {
                setAudioOutput(outputText)
            }
        })
        audioRecording.initAudioRecording()

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    toggleRecording()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Please enable audio permission",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBbScreenBinding.inflate(inflater, container, false)
        initView()
        setClickListener()
        return binding.root
    }

    private fun initView() {
        initChat()
        initSocket()
        connectSocket()

        chatAdapter = BBChatAdapter(
            requireActivity(),
            { message ->
                sendMessage(message)
            },
            { position ->
                qrScanPosition = position
                startQRScanner()
            },
            { position ->
                BBAddAttachmentFragment { fileBytes, fileName ->
                    val dataArray = JSONArray()
                    fileBytes.forEach { byte ->
                        dataArray.put(byte.toInt() and 0xFF)
                    }
                    val fileJson = JSONObject().apply {
                        put("type", "Buffer")
                        put("data", dataArray)
                    }
                    val json = JSONObject().apply {
                        put("File", fileJson)
                        put("original_file_name", fileName)
                    }
                    chatAdapter.getChatList()[position].data?.enable = false
                    chatAdapter.notifyItemChanged(position)
                    sendMessage(json)

                }.show(childFragmentManager, "BBAddAttachmentFragment")
            },
            { optionList, pos ->
                val dropdownFragment = BBDropdownFragment { selectedOption ->
                    chatAdapter.getChatList()[pos].data?.enable = false
                    chatAdapter.notifyItemChanged(pos)
                    sendMessage(selectedOption.value ?: "")
                }
                dropdownFragment.arguments = Bundle().apply {
                    putSerializable("itemList", optionList as Serializable?)
                }
                dropdownFragment.show(childFragmentManager, "BBDropdownFragment")
            }
        )
        binding.rvChat.adapter = chatAdapter
        binding.rvChat.addItemDecoration(SpaceItemDecoration(10))
    }

    private fun setClickListener() {
        binding.btnSend.setOnClickListener {
            val messageStr = binding.edtMessage.text.toString().trim()
            if (messageStr.isNotEmpty()) {
                if (!sessionId.isNullOrEmpty()) {
                    sendMessage(messageStr)
                }
            }
        }

        binding.btnRecordAudio.setOnClickListener({
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                toggleRecording()
            } else {
                requestPermissionLauncher.launch(permission.RECORD_AUDIO)
            }
        })
    }

    private fun initSocket() {
        try {
            val options = IO.Options()
            options.transports = arrayOf("websocket")
            mSocket = IO.socket(BBInit.getConfig().socketURL, options)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
        audioRecording.destroyAudioRecording()
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
            Log.e("Error", args.toString())
        }

    private val onNewMessage =
        Emitter.Listener { args ->
            requireActivity().runOnUiThread {
                try {
                    val data = args[0] as JSONObject
                    val mGson = GsonBuilder().create()
                    val chatMessageModel = mGson.fromJson(
                        data.toString(),
                        ChatMessageModel::class.java
                    )
                    chatMessageModel.itemType = 2
                    if (chatMessageModel.session_id == sessionId) {
                        //Log.e("data====", data.toString())
                        if (chatMessageModel.data?.bb_type != BBType.end_session.toString())
                            chatAdapter.addItem(chatMessageModel)
                        else
                            sessionId = ""
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                hideTypingLoader()
            }
        }

    private fun sendMessage(message: Any) {
        val jsonObject = JSONObject()
        jsonObject.put("channel", "websocket")
        jsonObject.put("session_id", sessionId)
        jsonObject.put("data", message)
        jsonObject.put("thread_id", "")

        mSocket.emit("channel_chat", jsonObject)

        if (CommonMethods.isPrimitiveAndValid(message)) {
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
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)

        typingJob = showTypingLoader(binding.tvTyping)
    }

    private fun toggleRecording() {
        if (isRecording) {
            isRecording = false
            binding.btnRecordAudio.setImageResource(R.drawable.ic_audio_bb)
            audioRecording.stopRecording()
        } else {
            isRecording = true
            binding.btnRecordAudio.setImageResource(R.drawable.ic_stop_bb)
            audioRecording.startRecording()
            binding.edtMessage.setText("")
        }
    }

    private fun setAudioOutput(outputText: String?) {
        isRecording = false
        binding.btnRecordAudio.setImageResource(R.drawable.ic_audio_bb)

        if (!outputText.isNullOrEmpty()) {
            if (!sessionId.isNullOrEmpty()) {
                sendMessage(outputText)
            }
        }
    }

    private fun initChat() {
        pd.showProgressDialog()
        val json = JSONObject()
        json.put("appId", BBInit.getConfig().appID)
        if (!sessionId.isNullOrEmpty())
            json.put("session_id", sessionId)
        else
            json.put("session_title", sessionName)
        json.put("bb_agent_name", "Luna")
        json.put("default_voice", "en-US-LunaNeural")

        val jsonVar = JSONObject()
        jsonVar.put("user_id", BBInit.getConfig().userId)
        jsonVar.put("username", BBInit.getConfig().email)
        jsonVar.put("first_name", BBInit.getConfig().firstName)
        jsonVar.put("last_name", BBInit.getConfig().lastName)
        jsonVar.put("email", BBInit.getConfig().email)
        jsonVar.put("token", BBInit.getConfig().authToken)
        jsonVar.put("requesterType", 1)
        jsonVar.put("IncidentType", 1)
        jsonVar.put("UserID", 1)
        jsonVar.put("cmmsApiKey", BBInit.getConfig().cmmsApiKey)
        json.put("variables", jsonVar)

        val jsonLang = JSONObject()
        jsonLang.put("source", "en-US")
        jsonLang.put("target", "en-US-LunaNeural")
        jsonLang.put("label", "English - Female (Luna)")
        jsonLang.put("type", "Azure")
        jsonLang.put("gender", "female")
        jsonLang.put("name", "Luna")
        json.put("language", jsonLang)

        val body: RequestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val call: Call<ResponseBody?> =
            RetrofitInstance.api.getSessionId(BBInit.getConfig().apiKey, body)
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
                        if (sessionId.isNullOrEmpty()) {
                            sessionId = sessionIdResponse?.data?.session_id
                            sendMessage("")
                            (requireActivity() as BBChatActivity).getThreadList(false)
                        } else {
                            getChatHistory()
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

    private fun getChatHistory() {
        pd.showProgressDialog()
        val call: Call<ResponseBody?> = RetrofitInstance.api.getChatHistory(
            BBInit.getConfig().apiKey,
            BBInit.getConfig().appID,
            sessionId
        )
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                pd.hideProgressDialog()
                try {
                    if (response.body() != null) {
                        val apiResponse = response.body()!!.string()
                        val mGson = GsonBuilder().create()
                        val chatHistoryResponse = mGson.fromJson(
                            apiResponse,
                            ChatHistoryResponse::class.java
                        )
                        if (!chatHistoryResponse.data.isNullOrEmpty()) {
                            chatHistoryResponse.data.removeAll {
                                it.data?.bb_type == BBType.input_text.toString() &&
                                        !CommonMethods.isPrimitiveAndValid(it.data?.bb_value)
                            }

                            chatHistoryResponse.data.forEachIndexed { index, item ->
                                if (item.data?.bb_type == BBType.input_text.toString()) {
                                    item.itemType = 1
                                } else {
                                    item.itemType = 2

                                    if (item.data?.bb_type == BBType.output_button_group.toString()) {
                                        val isLast = index == chatHistoryResponse.data.lastIndex
                                        item.data?.bb_buttons?.filterNotNull()
                                            ?.forEach { it.enable = isLast }
                                    }
                                    if (item.data?.bb_type in listOf(
                                            BBType.input_qr_code.toString(),
                                            BBType.input_file.toString(),
                                            BBType.input_dropdown.toString()
                                        )
                                    ) {
                                        val isLast = index == chatHistoryResponse.data.lastIndex
                                        item.data?.enable = isLast
                                    }
                                }
                            }
                            chatAdapter.addAllItem(chatHistoryResponse.data.reversed())
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

    fun startQRScanner() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan a QR code")
        options.setBeepEnabled(true)
        qrScannerLauncher.launch(options)
    }

    private val qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        if (!result.contents.isNullOrEmpty()) {
            sendMessage(result.contents)
            chatAdapter.getChatList()[qrScanPosition].data?.enable = false
            chatAdapter.notifyItemChanged(qrScanPosition)
        }
    }

    private fun showTypingLoader(textView: TextView): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            var dotCount = 0
            while (coroutineContext.isActive) {
                textView.text = "Typing" + ".".repeat(dotCount % 4)
                dotCount++
                delay(500)
            }
        }
    }

    private fun hideTypingLoader() {
        typingJob?.cancel()
        binding.tvTyping.text = ""
    }

    override fun onDestroyView() {
        hideTypingLoader()
        super.onDestroyView()
    }
}