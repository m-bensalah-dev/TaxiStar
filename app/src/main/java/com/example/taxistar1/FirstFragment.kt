package com.example.taxistar1

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class FirstFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    // UI Components
    private lateinit var googleMap: GoogleMap
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var myLocationButton: ImageButton
    private lateinit var tvDistance: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvFare: TextView
    private lateinit var tvCountry: TextView
    private lateinit var tvDayNight: TextView
    private var tvCurrency: TextView? = null

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocationMarker: Marker? = null

    // Ride tracking
    private var isRideActive = false
    private var rideData = RideData()
    private val routePoints = mutableListOf<LatLng>()
    private var polyline: Polyline? = null

    // Fare calculation
    private lateinit var fareCalculator: FareCalculator
    private var currentCountry: Country? = null

    // Notification
    private lateinit var notificationHelper: NotificationHelper

    // Update handler
    private val updateHandler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

    companion object {
        private const val LOCATION_PERMISSION_CODE = 123
        private const val NOTIFICATION_PERMISSION_CODE = 124
        private const val UPDATE_INTERVAL = 3000L
        private const val FASTEST_INTERVAL = 1000L
        private const val FARE_UPDATE_INTERVAL = 1000L
        private const val TAG = "FirstFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        initializeViews(view)

        // Initialize fare calculator
        fareCalculator = FareCalculator(requireContext())

        // Initialize notification helper
        notificationHelper = NotificationHelper(requireContext())

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup location callback
        setupLocationCallback()

        // Setup update runnable
        setupUpdateRunnable()

        // Request notification permission (Android 13+)
        requestNotificationPermission()
    }

    private fun initializeViews(view: View) {
        startButton = view.findViewById(R.id.startButton)
        stopButton = view.findViewById(R.id.stopButton)
        myLocationButton = view.findViewById(R.id.myLocationButton)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvTime = view.findViewById(R.id.tvTime)
        tvFare = view.findViewById(R.id.tvFare)
        tvCountry = view.findViewById(R.id.tvCountry)
        tvDayNight = view.findViewById(R.id.tvDayNight)
        tvCurrency = view.findViewById(R.id.tvCurrency)

        startButton.setOnClickListener {
            startRide()
        }

        stopButton.setOnClickListener {
            stopRide()
        }

        myLocationButton.setOnClickListener {
            moveToMyLocation()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Request location permission
        requestLocationPermission()

        // Set a default location (Morocco - Rabat)
        val defaultLocation = LatLng(34.0209, -6.8416)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Map settings
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false
    }

    @AfterPermissionGranted(LOCATION_PERMISSION_CODE)
    private fun requestLocationPermission() {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (EasyPermissions.hasPermissions(requireContext(), *perms)) {
            enableMyLocation()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs location permission to track your rides",
                LOCATION_PERMISSION_CODE,
                *perms
            )
        }
    }

    @AfterPermissionGranted(NOTIFICATION_PERMISSION_CODE)
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perms = arrayOf(Manifest.permission.POST_NOTIFICATIONS)

            if (!EasyPermissions.hasPermissions(requireContext(), *perms)) {
                EasyPermissions.requestPermissions(
                    this,
                    "This app needs notification permission to notify you about completed rides",
                    NOTIFICATION_PERMISSION_CODE,
                    *perms
                )
            }
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocation(location)
                }
            }
        }
    }

    private fun setupUpdateRunnable() {
        updateRunnable = object : Runnable {
            override fun run() {
                if (isRideActive) {
                    updateRideData()
                    updateHandler.postDelayed(this, FARE_UPDATE_INTERVAL)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    updateCarMarker(currentLatLng)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    determineCountry(it)
                    Toast.makeText(requireContext(), "Location found!", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        updateCarMarker(latLng)

        if (isRideActive) {
            routePoints.add(latLng)
            updatePolyline()

            if (rideData.startLocation == null) {
                rideData.startLocation = location
            }
            rideData.endLocation = location
        }
    }

    private fun updateCarMarker(latLng: LatLng) {
        currentLocationMarker?.remove()
        val carIcon = getBitmapFromVectorDrawable(R.drawable.mycar, 80, 80)
        currentLocationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("My Location")
                .icon(BitmapDescriptorFactory.fromBitmap(carIcon))
                .anchor(0.5f, 0.5f)
        )
    }

    private fun updatePolyline() {
        polyline?.remove()

        if (routePoints.size > 1) {
            val polylineOptions = PolylineOptions()
                .addAll(routePoints)
                .color(Color.YELLOW)
                .width(10f)

            polyline = googleMap.addPolyline(polylineOptions)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRide() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                rideData = RideData()
                rideData.startLocation = location
                rideData.startTime = System.currentTimeMillis()

                determineCountry(location)

                routePoints.clear()
                routePoints.add(LatLng(location.latitude, location.longitude))
                polyline?.remove()

                isRideActive = true

                // Enable Stop button, disable Start button
                startButton.isEnabled = false
                startButton.alpha = 0.5f
                stopButton.isEnabled = true
                stopButton.alpha = 1.0f

                startLocationUpdates()
                updateHandler.post(updateRunnable)

                Toast.makeText(requireContext(), "Ride started!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Ride started at: ${rideData.startLocation}")
            } else {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRide() {
        isRideActive = false
        rideData.endTime = System.currentTimeMillis()

        stopLocationUpdates()
        updateHandler.removeCallbacks(updateRunnable)

        // Final calculation
        updateRideData()

        // Enable Start button, disable Stop button
        startButton.isEnabled = true
        startButton.alpha = 1.0f
        stopButton.isEnabled = false
        stopButton.alpha = 0.5f

        // Save ride data
        saveRideData()

        // Send notification
        val fareFormatted = fareCalculator.formatFare(rideData.fare, rideData.currency)
        notificationHelper.sendRideCompletedNotification(rideData, fareFormatted)

        Toast.makeText(requireContext(), "Ride stopped! Saved to history.", Toast.LENGTH_SHORT).show()
    }

    private fun updateRideData() {
        if (rideData.startLocation != null && rideData.endLocation != null && currentCountry != null) {
            rideData.distanceKm = fareCalculator.calculateDistanceBetweenLocations(
                rideData.startLocation!!,
                rideData.endLocation!!
            )

            val currentTime = if (isRideActive) System.currentTimeMillis() else rideData.endTime
            val timeSeconds = (currentTime - rideData.startTime) / 1000
            rideData.timeMinutes = timeSeconds / 60

            rideData.isDay = fareCalculator.isDayTime(currentCountry!!)

            rideData.fare = fareCalculator.calculateFare(
                currentCountry!!,
                rideData.distanceKm,
                rideData.timeMinutes,
                rideData.isDay
            )

            updateUI(timeSeconds)
        }
    }

    private fun updateUI(timeSeconds: Long) {
        // Distance
        val safeDistance = rideData.distanceKm.coerceAtLeast(0.0)
        tvDistance.text = String.format("%.2f", safeDistance)

        // Fare (split number and currency)
        val safeFare = if (rideData.fare.isNaN() || rideData.fare < 0.0) 0.0 else rideData.fare
        tvFare.text = String.format("%.2f", safeFare)
        tvCurrency?.text = rideData.currency.ifEmpty { "MAD" }

        // Time (split number and unit)
        val safeTime = timeSeconds.coerceAtLeast(0)
        if (safeTime < 60) {
            tvTime.text = safeTime.toString()
        } else {
            val minutes = safeTime / 60
            val seconds = safeTime % 60
            tvTime.text = String.format("%d:%02d", minutes, seconds)
        }

        // Day/Night indicator
        tvDayNight.text = if (rideData.isDay) "Day Rate" else "Night Rate"
        tvDayNight.setTextColor(
            if (rideData.isDay)
                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
            else
                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
        )
    }

    private fun determineCountry(location: Location) {
        currentCountry = fareCalculator.getCountryFromLocation(location)
        currentCountry?.let { country ->
            rideData.country = country.name
            rideData.currency = country.currency
            tvCountry.text = country.name
            Log.d(TAG, "Country determined: ${country.name}")
        }
    }

    private fun saveRideData() {
        val rideHistoryManager = RideHistoryManager(requireContext())
        rideHistoryManager.saveRide(rideData)

        Log.d(TAG, "==================== RIDE COMPLETED ====================")
        Log.d(TAG, "Country: ${rideData.country}")
        Log.d(TAG, "Currency: ${rideData.currency}")
        Log.d(TAG, "Start Location: Lat=${rideData.startLocation?.latitude}, Lng=${rideData.startLocation?.longitude}")
        Log.d(TAG, "End Location: Lat=${rideData.endLocation?.latitude}, Lng=${rideData.endLocation?.longitude}")
        Log.d(TAG, "Distance: ${String.format("%.2f", rideData.distanceKm)} km")
        Log.d(TAG, "Time: ${rideData.timeMinutes} minutes")
        Log.d(TAG, "Rate Type: ${if (rideData.isDay) "Day" else "Night"}")
        Log.d(TAG, "Total Fare: ${fareCalculator.formatFare(rideData.fare, rideData.currency)}")
        Log.d(TAG, "Saved to history!")
        Log.d(TAG, "======================================================")
    }

    @SuppressLint("MissingPermission")
    private fun moveToMyLocation() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    Toast.makeText(requireContext(), "Moved to your location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getBitmapFromVectorDrawable(drawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> enableMyLocation()
            NOTIFICATION_PERMISSION_CODE -> Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_LONG).show()
            NOTIFICATION_PERMISSION_CODE -> Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLocationUpdates()
        updateHandler.removeCallbacks(updateRunnable)
    }
}