package dev.nura.locationtracker.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dev.nura.locationtracker.R
import dev.nura.locationtracker.Utils.toFormattedDateTime
import dev.nura.locationtracker.commen.AppPreferences
import dev.nura.locationtracker.databinding.FragmentMapViewBinding
import dev.nura.locationtracker.databinding.LayoutCustomInfoWindowBinding
import dev.nura.locationtracker.realm.LocationInfo
import dev.nura.locationtracker.viewmodal.AppViewModel


class MapViewFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private var _binding: FragmentMapViewBinding? = null
    private val binding get() = _binding!!

    private var location = LocationInfo()

    private val viewModel: AppViewModel by activityViewModels()

    private var locationList: List<LocationInfo> = listOf()

    private var currentIndex: Int = 0

    private var newLocationData = MutableLiveData<LocationInfo>()
    private var oldLocationData: LocationInfo? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        viewModel.localLocationInfo.observe(viewLifecycleOwner) {
            location = it
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        Log.d(TAG, "onViewCreated:Handle the case when the fragment is found ")
        mapFragment.getMapAsync(this)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

        viewModel.locations.observe(viewLifecycleOwner) { loca ->
            locationList = listOf()
            locationList = loca.filter { it.userId == AppPreferences.loginUuid }
        }

        binding.playBack.setOnClickListener {
            try {
                currentIndex = locationList.indexOf(location)
                if (currentIndex > 0) {
                    currentIndex -= 1
                }
                Toast.makeText(requireContext(), "$currentIndex", Toast.LENGTH_SHORT).show()
                newLocationData.postValue(locationList[currentIndex])

            }catch (e:Exception){
                Log.e(TAG, "onViewStateRestored: ${e.message}", )                
            }
            
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG: String = "MapViewFragment"
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        setGoogleMap(location)
        newLocationData.observe(viewLifecycleOwner) { newLocation ->
            if (this::googleMap.isInitialized) {
                oldLocationData = location
                location = newLocation
                setGoogleMap(newLocation)
            }
        }
    }

    private fun setGoogleMap(locationInfo: LocationInfo) {
        val latitude = locationInfo.latitude // Example latitude
        val longitude = locationInfo.longitude // Example longitude
        val latLng = LatLng(latitude, longitude)
        // Move the camera to the specified location
        googleMap.clear()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))

        if (oldLocationData != null) {
            val lat = oldLocationData!!.latitude // Example latitude
            val lon = oldLocationData!!.longitude // Example longitude
            val destinationLatLng = LatLng(lat, lon)

            googleMap.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))

            // Add Polyline
            val polylineOptions = PolylineOptions()
                .add(latLng, destinationLatLng)
                .color(Color.RED) // Change color as needed
                .width(5f) // Change width as needed
            val polyline = googleMap.addPolyline(polylineOptions)
            /*// Fetch directions using Directions API
            val context = GeoApiContext.Builder()
                .apiKey(AppPreferences.gMapKey) // Replace with your actual API key
                .build()

            val directionsResult: DirectionsResult = DirectionsApi.newRequest(context)
                .mode(TravelMode.DRIVING)
                .origin(com.google.maps.model.LatLng(locationInfo.latitude, locationInfo.longitude))
                .destination(com.google.maps.model.LatLng(destinationLatLng.latitude, destinationLatLng.longitude))
                .await()

            // Draw route on the map
            val decodedPath = PolylineEncoding.decode(directionsResult.routes[0].overviewPolyline.encodedPath)


            // Decoding path to a list of LatLng
            val pathPoints = decodedPath.map { LatLng(it.lat, it.lng) }

            // Adding polyline to the map
            val polyline =
                googleMap.addPolyline(PolylineOptions().addAll(pathPoints).color(Color.RED))*/

        }

        val marker = googleMap.addMarker(MarkerOptions().position(latLng).title("Marker Title"))
        googleMap.addMarker(
            MarkerOptions().position(latLng).title(viewModel.localUserInfo.userName)
        )
        if (marker != null) {
            marker.tag = viewModel.localUserInfo.userEmail
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): android.view.View? {
                return null
            }

            override fun getInfoContents(marker: Marker): android.view.View {
                val bindingMap = LayoutCustomInfoWindowBinding.inflate(layoutInflater)
                bindingMap.titleText.text = viewModel.localUserInfo.userName.toString()
                bindingMap.snippetText.text = viewModel.localUserInfo.userEmail
                bindingMap.dateText.text = locationInfo.time.toFormattedDateTime()
                return bindingMap.root
            }
        })
        googleMap.setOnMarkerClickListener { clickedMarker ->
            clickedMarker.showInfoWindow()
            true
        }
    }
}