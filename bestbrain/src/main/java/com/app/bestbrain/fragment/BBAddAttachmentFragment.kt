package com.app.bestbrain.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import com.app.bestbrain.databinding.FragmentAddBbAttachmentBinding
import com.app.bestbrain.utils.CommonMethods.createImageFile
import com.app.bestbrain.utils.CommonMethods.uriToBytes
import com.app.bestbrain.utils.PermissionHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class BBAddAttachmentFragment(val onFilePicked: (ByteArray) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var fragmentAddAttachmentBinding: FragmentAddBbAttachmentBinding
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraUri: Uri
    private lateinit var cameraFile: File

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val imageBytes = uriToBytes(requireContext(), cameraUri)
            println(imageBytes)
            onFilePicked(imageBytes)
            dismiss()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val imageBytes = uriToBytes(requireContext(), uri)
            println(imageBytes)
            onFilePicked(imageBytes)
            dismiss()
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val fileUri = result.data?.data
            fileUri?.let {
                val fileBytes = uriToBytes(requireContext(), fileUri)
                println(fileBytes)
                onFilePicked(fileBytes)
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAddAttachmentBinding =
            FragmentAddBbAttachmentBinding.inflate(inflater, container, false)
        return fragmentAddAttachmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraPermissionLauncher = PermissionHelper.registerCameraPermissionLauncher(
            lifecycleOwner = viewLifecycleOwner,
            registryOwner = requireActivity() as ActivityResultRegistryOwner
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            }
        }
        fragmentAddAttachmentBinding.btnCancel.setOnClickListener {
            dismiss()
        }
        fragmentAddAttachmentBinding.btnTakePhoto.setOnClickListener {
            requestCameraPermission()
        }
        fragmentAddAttachmentBinding.btnLibrary.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        fragmentAddAttachmentBinding.btnAddPdf.setOnClickListener {
            openDocumentPicker()
        }
    }

    private fun requestCameraPermission() {
        if (PermissionHelper.isCameraPermissionGranted(this)) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun openCamera() {
        val (file, uri) = createImageFile(requireContext())
        cameraFile = file
        cameraUri = uri
        takePictureLauncher.launch(cameraUri)
    }

    fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
                )
            )
        }
        filePickerLauncher.launch(intent)
    }
}