package com.example.taxistar1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class SecondFragment : Fragment() {

    private lateinit var ivQRCode: ImageView
    private lateinit var tvNoData: TextView
    private lateinit var cardQRCode: CardView
    private lateinit var preferencesHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        ivQRCode = view.findViewById(R.id.ivQRCode)
        tvNoData = view.findViewById(R.id.tvNoData)
        cardQRCode = view.findViewById(R.id.cardQRCode)

        preferencesHelper = PreferencesHelper(requireContext())

        // Load QR code
        loadAndDisplayQRCode()
    }

    override fun onResume() {
        super.onResume()
        // Refresh QR code when returning to this fragment
        loadAndDisplayQRCode()
    }

    private fun loadAndDisplayQRCode() {
        val driverInfo = preferencesHelper.getDriverInfo()

        if (driverInfo != null && driverInfo.name.isNotEmpty()) {
            // Only include textual info, NOT photo
            val qrData = """
                Name: ${driverInfo.name}
                Age: ${driverInfo.age}
                Phone: ${driverInfo.phone}
                License: ${driverInfo.licenseNumber}
                Car Model: ${driverInfo.carModel}
                Car Plate: ${driverInfo.carPlate}
            """.trimIndent()

            val qrBitmap = QRCodeHelper.generateQRCode(qrData, 512)

            if (qrBitmap != null) {
                ivQRCode.setImageBitmap(qrBitmap)

                // Show QR code, hide no data message
                cardQRCode.visibility = View.VISIBLE
                tvNoData.visibility = View.GONE
            } else {
                // QR generation failed
                cardQRCode.visibility = View.GONE
                tvNoData.visibility = View.VISIBLE
            }
        } else {
            // No driver info - show message
            cardQRCode.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
        }
    }
}
