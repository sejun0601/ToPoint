package com.example.topoint.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.topoint.R
import com.example.topoint.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // 지도 설정
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val initialLocation = LatLng(37.5665, 126.9780)
        mMap.addMarker(MarkerOptions().position(initialLocation).title("Marker in Seoul"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))

        mMap.setOnMapClickListener { latLng ->
            addMarkerOnLocation(latLng)
        }
    }

    private fun addMarkerOnLocation(latLng: LatLng) {
        mMap.addMarker(MarkerOptions().position(latLng).title("New Marker"))
        homeViewModel.updateLocation(latLng)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
