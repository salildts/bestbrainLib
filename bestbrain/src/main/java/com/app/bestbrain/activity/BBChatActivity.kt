package com.app.bestbrain.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bestbrain.R
import com.app.bestbrain.adapter.ThreadHeaderAdapter
import com.app.bestbrain.databinding.ActivityBbChatBinding
import com.app.bestbrain.fragment.ChatInitFragment
import com.app.bestbrain.fragment.ChatScreenFragment
import com.app.bestbrain.init.BBInit
import com.app.bestbrain.models.SessionGroup
import com.app.bestbrain.models.SessionItem
import com.app.bestbrain.network.RetrofitInstance
import com.app.bestbrain.utils.ProgressDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class BBChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBbChatBinding
    private lateinit var pd: ProgressDialog
    private lateinit var threadListAdapter: ThreadHeaderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBbChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BBInit.getConfig().apiBase.isNullOrEmpty())
            throw Exception("API BASE URL is empty")
        if (BBInit.getConfig().socketURL.isNullOrEmpty())
            throw Exception("Socket URL is empty")
        if (BBInit.getConfig().apiKey.isNullOrEmpty())
            throw Exception("API Key is empty")
        if (BBInit.getConfig().appID.isNullOrEmpty())
            throw Exception("APP ID is empty")

        initView()
        initClickListener()
    }

    private fun initView() {
        pd = ProgressDialog(this, "", false)

        threadListAdapter = ThreadHeaderAdapter(
            context = this,
            onThreadSelect = { thread ->
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val fragment = ChatScreenFragment()
                val bundle = Bundle()
                bundle.putString("session_id", thread.session_id)
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            },
            onDeleteClick = { sessionId ->
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                showDeleteThreadAlert(sessionId)
            }
        )
        binding.sideMenu.rvChatThread.adapter = threadListAdapter

        val dividerItemDecoration = DividerItemDecoration(
            binding.sideMenu.rvChatThread.context,
            (binding.sideMenu.rvChatThread.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(this, R.drawable.white_line_divider)!!
        )
        binding.sideMenu.rvChatThread.addItemDecoration(dividerItemDecoration)

        if (!BBInit.getConfig().headerText.isNullOrEmpty())
            binding.tvHeader.text = BBInit.getConfig().headerText

        loadChatInitFragment()

        getThreadList(true)
    }

    private fun initClickListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) binding.drawerLayout.closeDrawer(
                    GravityCompat.START
                ) else
                    finish()
            }
        })

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnMenu.setOnClickListener({ v ->
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) binding.drawerLayout.openDrawer(
                GravityCompat.START
            )
            else binding.drawerLayout.closeDrawer(GravityCompat.START)
        })

        binding.sideMenu.btnAddNew.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            loadChatInitFragment()
        }
    }

    fun getThreadList(showLoader: Boolean) {
        if (showLoader)
            pd.showProgressDialog()
        val call: Call<ResponseBody?> = RetrofitInstance.api.getThreadList(
            BBInit.getConfig().apiKey,
            BBInit.getConfig().appID,
            BBInit.getConfig().userId
        )
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (showLoader)
                    pd.hideProgressDialog()
                try {
                    if (response.body() != null) {
                        val apiResponse = response.body()!!.string()
                        val groupedList = groupSessionsWithLabels(apiResponse)
                        threadListAdapter.sessionGroupList = groupedList

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
                if (showLoader)
                    pd.hideProgressDialog()
            }
        })
    }

    private fun loadChatInitFragment() {
        val fragment = ChatInitFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showDeleteThreadAlert(sessionId: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Thread")
            .setMessage("Are you sure you want to delete this?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteThread(sessionId)
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    fun deleteThread(sessionId: String) {
        pd.showProgressDialog()
        val call: Call<ResponseBody?> = RetrofitInstance.api.deleteThread(
            BBInit.getConfig().apiKey,
            sessionId
        )
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                pd.hideProgressDialog()
                try {
                    if (response.body() != null) {
                        val apiResponse = response.body()!!.string()
                        val jsonObject = JSONObject(apiResponse)
                        if (jsonObject.optBoolean("deleted"))
                            getThreadList(true)

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

    fun groupSessionsWithLabels(jsonString: String): List<SessionGroup> {
        val gson = Gson()
        val listType = object : TypeToken<List<SessionItem>>() {}.type
        val sessions: List<SessionItem> = gson.fromJson(jsonString, listType)

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return sessions
            .groupBy { item ->
                val date = inputFormat.parse(item.ts)!!
                dateOnlyFormat.format(date)
            }
            .map { (dateString, items) ->
                val sessionDate = dateOnlyFormat.parse(dateString)!!
                val label = when (dateString) {
                    dateOnlyFormat.format(today.time) -> "Today"
                    dateOnlyFormat.format(yesterday.time) -> "Yesterday"
                    else -> labelFormat.format(sessionDate)
                }
                SessionGroup(label, items)
            }
            .sortedByDescending { group ->
                val parsed = when (group.date) {
                    "Today" -> today.time
                    "Yesterday" -> yesterday.time
                    else -> labelFormat.parse(group.date) ?: Date(0)
                }
                parsed
            }
    }
}