package com.itsyourturn.moscow.ui.routes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import com.itsyourturn.moscow.R
import com.itsyourturn.moscow.databinding.FragmentRouteGenerationBinding

class RouteGenerationFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentRouteGenerationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RouteGenerationViewModel by viewModels()
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Snackbar.make(
                binding.root,
                R.string.location_permission_denied,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouteGenerationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupMapView(savedInstanceState)
        setupBottomSheet()
        setupClickListeners()
        observeRouteState()
    }

    private fun setupMapView(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setupClickListeners() {
        binding.apply {
            generateButton.setOnClickListener {
                generateRoute()
            }

            myLocationFab.setOnClickListener {
                checkLocationPermission()
            }
        }
    }

    private fun generateRoute() {
        val duration = binding.durationInput.text.toString().toIntOrNull()
        if (duration == null || duration <= 0) {
            binding.durationInputLayout.error = getString(R.string.invalid_duration)
            return
        }

        val selectedPlaceTypes = binding.placeTypesChipGroup.checkedChipIds.map { chipId ->
            val chip = binding.placeTypesChipGroup.findViewById<Chip>(chipId)
            chip.text.toString().lowercase()
        }

        googleMap?.cameraPosition?.target?.let { center ->
            viewModel.generateRoute(
                startPoint = GeoPoint(center.latitude, center.longitude),
                duration = duration,
                placeTypes = selectedPlaceTypes
            )
        }
    }

    private fun observeRouteState() {
        viewModel.routeState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RouteState.Loading -> {
                    binding.generateButton.isEnabled = false
                }
                is RouteState.Success -> {
                    binding.generateButton.isEnabled = true
                    displayRoute(state.route)
                }
                is RouteState.Error -> {
                    binding.generateButton.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                is RouteState.RouteSaved -> {
                    Snackbar.make(
                        binding.root,
                        R.string.route_saved_successfully,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun displayRoute(route: com.itsyourturn.moscow.data.model.Route) {
        googleMap?.clear()

        // Add start marker
        route.startPoint?.let { startPoint ->
            googleMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(startPoint.latitude, startPoint.longitude))
                    .title(getString(R.string.start_point))
            )
        }

        // Add waypoint markers and connect them with polylines
        val points = mutableListOf<LatLng>()
        route.startPoint?.let { points.add(LatLng(it.latitude, it.longitude)) }

        route.waypoints.forEach { waypoint ->
            val position = LatLng(waypoint.location.latitude, waypoint.location.longitude)
            points.add(position)
            googleMap?.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(waypoint.name)
            )
        }

        // Draw route line
        googleMap?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(ContextCompat.getColor(requireContext(), R.color.route_line))
                .width(5f)
        )

        // Move camera to show the entire route
        if (points.isNotEmpty()) {
            val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
            points.forEach { builder.include(it) }
            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    100 // padding in pixels
                )
            )
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                binding.root,
                R.string.location_permission_denied,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            
            // Center on Moscow by default
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(55.7558, 37.6173),
                    10f
                )
            )
        }
        checkLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
