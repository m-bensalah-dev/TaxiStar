package com.example.taxistar1

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import java.io.ByteArrayOutputStream

class FourthFragment : Fragment() {

    private lateinit var ivProfilePhoto: ImageView
    private lateinit var btnEditPhoto: ImageView
    private lateinit var tvDisplayName: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etLicense: TextInputEditText
    private lateinit var etCarModel: TextInputEditText
    private lateinit var etCarPlate: TextInputEditText
    private lateinit var btnSave: Button

    private lateinit var preferencesHelper: PreferencesHelper
    private var selectedPhotoBase64: String = ""

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                handleSelectedImage(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fourth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        initializeViews(view)

        // Initialize preferences
        preferencesHelper = PreferencesHelper(requireContext())

        // Load existing data if available
        loadExistingData()

        // Setup click listeners
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto)
        btnEditPhoto = view.findViewById(R.id.btnEditPhoto)
        tvDisplayName = view.findViewById(R.id.tvDisplayName)
        etName = view.findViewById(R.id.etName)
        etAge = view.findViewById(R.id.etAge)
        etPhone = view.findViewById(R.id.etPhone)
        etLicense = view.findViewById(R.id.etLicense)
        etCarModel = view.findViewById(R.id.etCarModel)
        etCarPlate = view.findViewById(R.id.etCarPlate)
        btnSave = view.findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        // Photo selection
        btnEditPhoto.setOnClickListener {
            openImagePicker()
        }

        ivProfilePhoto.setOnClickListener {
            openImagePicker()
        }

        // Save button
        btnSave.setOnClickListener {
            saveDriverInfo()
        }
    }

    private fun loadExistingData() {
        val driverInfo = preferencesHelper.getDriverInfo()

        if (driverInfo != null) {
            // Populate fields with existing data
            etName.setText(driverInfo.name)
            etAge.setText(driverInfo.age)
            etPhone.setText(driverInfo.phone)
            etLicense.setText(driverInfo.licenseNumber)
            etCarModel.setText(driverInfo.carModel)
            etCarPlate.setText(driverInfo.carPlate)
            selectedPhotoBase64 = driverInfo.photoBase64

            // Update display name
            tvDisplayName.text = if (driverInfo.name.isNotEmpty()) {
                driverInfo.name
            } else {
                "Driver Name"
            }

            // Load photo if exists
            if (driverInfo.photoBase64.isNotEmpty()) {
                loadPhotoFromBase64(driverInfo.photoBase64)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun handleSelectedImage(imageUri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Resize bitmap to reduce size
            val resizedBitmap = resizeBitmap(bitmap, 400)

            // Convert to Base64
            selectedPhotoBase64 = bitmapToBase64(resizedBitmap)

            // Display photo
            ivProfilePhoto.setImageBitmap(resizedBitmap)

            Toast.makeText(requireContext(), "Photo selected", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratio: Float = width.toFloat() / height.toFloat()

        return if (ratio > 1) {
            Bitmap.createScaledBitmap(bitmap, maxSize, (maxSize / ratio).toInt(), true)
        } else {
            Bitmap.createScaledBitmap(bitmap, (maxSize * ratio).toInt(), maxSize, true)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun loadPhotoFromBase64(base64: String) {
        try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            ivProfilePhoto.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveDriverInfo() {
        // Get input values
        val name = etName.text.toString().trim()
        val age = etAge.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val license = etLicense.text.toString().trim()
        val carModel = etCarModel.text.toString().trim()
        val carPlate = etCarPlate.text.toString().trim()

        // Validate inputs
        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return
        }

        if (age.isEmpty()) {
            etAge.error = "Age is required"
            etAge.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            etPhone.error = "Phone is required"
            etPhone.requestFocus()
            return
        }

        if (license.isEmpty()) {
            etLicense.error = "License number is required"
            etLicense.requestFocus()
            return
        }

        if (carModel.isEmpty()) {
            etCarModel.error = "Car model is required"
            etCarModel.requestFocus()
            return
        }

        if (carPlate.isEmpty()) {
            etCarPlate.error = "Car plate is required"
            etCarPlate.requestFocus()
            return
        }

        // Create driver info object
        val driverInfo = DriverInfo(
            name = name,
            age = age,
            licenseNumber = license,
            phone = phone,
            carModel = carModel,
            carPlate = carPlate,
            photoBase64 = selectedPhotoBase64
        )

        // Save to preferences
        preferencesHelper.saveDriverInfo(driverInfo)

        // Update display name
        tvDisplayName.text = name

        // Show success message
        Toast.makeText(
            requireContext(),
            "Profile saved! QR Code generated in QR Code tab",
            Toast.LENGTH_LONG
        ).show()

        // Hide keyboard
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}