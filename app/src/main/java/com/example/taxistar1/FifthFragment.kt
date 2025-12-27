package com.example.taxistar1

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlin.system.exitProcess

class FifthFragment : Fragment() {

    private lateinit var ivProfilePhoto: ImageView
    private lateinit var tvDriverName: TextView
    private lateinit var preferencesHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fifth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto)
        tvDriverName = view.findViewById(R.id.tvDriverName)

        // Initialize preferences
        preferencesHelper = PreferencesHelper(requireContext())

        // Load driver info
        loadDriverInfo()

        // Setup menu clicks
        setupMenuClicks(view)
    }

    override fun onResume() {
        super.onResume()
        // Refresh driver info when returning to this fragment
        loadDriverInfo()
    }

    private fun loadDriverInfo() {
        val driverInfo = preferencesHelper.getDriverInfo()

        if (driverInfo != null && driverInfo.name.isNotEmpty()) {
            // Set driver name
            tvDriverName.text = driverInfo.name

            // Load photo if exists
            if (driverInfo.photoBase64.isNotEmpty()) {
                try {
                    val decodedBytes = Base64.decode(driverInfo.photoBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    ivProfilePhoto.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            tvDriverName.text = "Driver Name"
        }
    }

    private fun setupMenuClicks(view: View) {
        // Update User Profile -> Go to FourthFragment (Profile)
        view.findViewById<LinearLayout>(R.id.menuUpdateProfile).setOnClickListener {
            findNavController().navigate(R.id.fourthFragment)
        }

        // Notification -> Simple toggle (you can enhance this)
        view.findViewById<LinearLayout>(R.id.menuNotification).setOnClickListener {
            Toast.makeText(requireContext(), "Notifications are enabled", Toast.LENGTH_SHORT).show()
        }

        // Invoices -> Coming Soon Dialog
        view.findViewById<LinearLayout>(R.id.menuInvoices).setOnClickListener {
            showComingSoonDialog()
        }

        // Historique -> Go to ThirdFragment (History)
        view.findViewById<LinearLayout>(R.id.menuHistorique).setOnClickListener {
            findNavController().navigate(R.id.thirdFragment)
        }

        // Notification Management -> Dialog with ON/OFF options
        view.findViewById<LinearLayout>(R.id.menuNotificationManagement).setOnClickListener {
            showNotificationManagementDialog()
        }

        // About Me -> Go to SecondFragment (QR Code)
        view.findViewById<LinearLayout>(R.id.menuAboutMe).setOnClickListener {
            findNavController().navigate(R.id.secondFragment)
        }

        // Language -> Coming Soon (you'll implement later)
        view.findViewById<LinearLayout>(R.id.menuLanguage).setOnClickListener {
            Toast.makeText(requireContext(), "Language settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // Exit Button -> Show exit confirmation
        view.findViewById<Button>(R.id.btnExit).setOnClickListener {
            showExitDialog()
        }
    }

    private fun showComingSoonDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Coming Soon")
            .setMessage("Invoices feature will be available in the next update!")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showNotificationManagementDialog() {
        val options = arrayOf("Turn ON Notifications", "Turn OFF Notifications")

        AlertDialog.Builder(requireContext())
            .setTitle("Notification Management")
            .setIcon(android.R.drawable.ic_popup_reminder)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Turn ON
                        Toast.makeText(requireContext(), "Notifications turned ON", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // Turn OFF
                        Toast.makeText(requireContext(), "Notifications turned OFF", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit TaxiStar?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes") { _, _ ->
                // Exit the app
                requireActivity().finishAffinity()
                exitProcess(0)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}