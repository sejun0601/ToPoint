package com.example.topoint.ui.home

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.sothree.slidinguppanel.SlidingUpPanelLayout




class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var placesClient : PlacesClient
    private lateinit var searchAdapter: ArrayAdapter<String>
    private val  searchResults = mutableListOf<AutocompletePrediction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val apiKey = getApiKey()

        // Initialize the Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }
        placesClient = Places.createClient(requireContext())


        // Set up the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up the sliding panel
        val slidingLayout = root.findViewById<SlidingUpPanelLayout>(R.id.sliding_layout)
        slidingLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                // Handle the panel sliding
            }

            override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
                // Handle the panel state change
            }
        })

        val searchBar: EditText = root.findViewById(R.id.search_bar)
        val panelList: ListView = root.findViewById(R.id.panel_list)

        searchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf<String>())
        panelList.adapter = searchAdapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    searchPlaces(it.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        panelList.setOnItemClickListener { _, _, position, _ ->
            val selectedPlace = searchResults[position]
            val placeId = selectedPlace.placeId
            // Get the place details and update the map
            getPlaceDetails(placeId)
        }

        return root
    }



    private fun getApiKey(): String {
        val ai = requireContext().packageManager.getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
        val bundle = ai.metaData
        return bundle.getString("com.google.android.geo.API_KEY") ?: ""
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

    private fun searchPlaces(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            searchResults.clear()
            searchResults.addAll(response.autocompletePredictions)
            val resultNames = response.autocompletePredictions.map { it.getFullText(null).toString() }
            searchAdapter.clear()
            searchAdapter.addAll(resultNames)
            searchAdapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    private fun getPlaceDetails(placeId: String) {
        // Use the Places API to get the place details and update the map
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = com.google.android.libraries.places.api.net.FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            val location = place.latLng
            location?.let {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                mMap.addMarker(MarkerOptions().position(it).title(place.name))
                homeViewModel.updateLocation(it)
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
