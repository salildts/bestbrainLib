package com.app.bestbrain.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bestbrain.R
import com.app.bestbrain.adapter.DropdownItemAdapter
import com.app.bestbrain.databinding.FragmentDropdownBinding
import com.app.bestbrain.models.BbOption
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DropdownFragment(val onItemSelect: (BbOption) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var fragmentDropdownBinding: FragmentDropdownBinding
    private var itemList: ArrayList<BbOption> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemList = arguments?.getSerializable("itemList") as? ArrayList<BbOption> ?: ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentDropdownBinding = FragmentDropdownBinding.inflate(inflater, container, false)
        initView()
        return fragmentDropdownBinding.root
    }

    private fun initView() {
        val dividerItemDecoration = DividerItemDecoration(
            fragmentDropdownBinding.rvDropdown.context,
            (fragmentDropdownBinding.rvDropdown.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.gray_line_divider)!!
        )
        fragmentDropdownBinding.rvDropdown.addItemDecoration(dividerItemDecoration)
        val dropdownItemAdapter = DropdownItemAdapter(itemList) { item ->
            onItemSelect(item)
            dismiss()
        }
        fragmentDropdownBinding.rvDropdown.adapter = dropdownItemAdapter
    }
}