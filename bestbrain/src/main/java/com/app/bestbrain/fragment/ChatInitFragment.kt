package com.app.bestbrain.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.bestbrain.R
import com.app.bestbrain.databinding.FragmentChatInitBinding
import com.app.bestbrain.utils.CommonMethods

class ChatInitFragment : Fragment() {

    private lateinit var binding: FragmentChatInitBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatInitBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.edtConvTitle.setText(
            "Chat On ${CommonMethods.getCurrentDateTime("dd/MM/yyyy")} @ ${
                CommonMethods.getCurrentDateTime(
                    "hh:mm:ss"
                )
            }"
        )

        binding.btnSubmit.setOnClickListener {
            if (binding.edtConvTitle.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    requireActivity(),
                    "Please enter conversation title",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val fragment = ChatScreenFragment()
            val bundle = Bundle()
            bundle.putString("session_name", binding.edtConvTitle.text.toString().trim())
            fragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}