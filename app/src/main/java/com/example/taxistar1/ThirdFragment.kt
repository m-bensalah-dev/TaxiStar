package com.example.taxistar1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ThirdFragment : Fragment() {

    private lateinit var rvRideHistory: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var layoutEmptyState: LinearLayout

    private lateinit var rideHistoryManager: RideHistoryManager
    private lateinit var adapter: RideHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        rvRideHistory = view.findViewById(R.id.rvRideHistory)
        etSearch = view.findViewById(R.id.etSearch)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)

        // Initialize history manager
        rideHistoryManager = RideHistoryManager(requireContext())

        // Setup RecyclerView
        setupRecyclerView()

        // Setup search
        setupSearch()

        // Load rides
        loadRides()
    }

    override fun onResume() {
        super.onResume()
        // Refresh rides when returning to this fragment
        loadRides()
    }

    private fun setupRecyclerView() {
        adapter = RideHistoryAdapter(emptyList())
        rvRideHistory.layoutManager = LinearLayoutManager(requireContext())
        rvRideHistory.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                searchRides(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadRides() {
        val rides = rideHistoryManager.getAllRides()

        if (rides.isEmpty()) {
            // Show empty state
            rvRideHistory.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            // Show rides
            rvRideHistory.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            adapter.updateRides(rides)
        }
    }

    private fun searchRides(query: String) {
        val rides = rideHistoryManager.searchRides(query)

        if (rides.isEmpty()) {
            rvRideHistory.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvRideHistory.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            adapter.updateRides(rides)
        }
    }
}