package com.example.gpstracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gpstracker.server.APIService
import com.example.gpstracker.server.get_ortu

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

class MapsView : Fragment() {

    private lateinit var mMap: GoogleMap

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        val uper = LatLng(-6.116500, 106.788967)
        val kranji = LatLng(-6.224860160583242, 106.9797861304309)
        val ortu = LatLng(-6.135565, 106.177429)

        // Hitung jarak menggunakan SphericalUtil
        val distance: Double = SphericalUtil.computeDistanceBetween(uper, kranji)
        val distanceOrtukeAnak1: Double = (SphericalUtil.computeDistanceBetween(ortu, uper))/1000
        val distanceOrtukeAnak2: Double = (SphericalUtil.computeDistanceBetween(ortu, kranji))/1000
        val distanceInKm = distance / 1000 // Ubah ke kilometer
        val decimalFormat = DecimalFormat("#.#")
        val roundedDistance = decimalFormat.format(distanceInKm)

        setCustomMarkerIcon(uper, "UniversitasPertamina", distanceInKm)
        setCustomMarkerIcon(kranji, "StasiunKranji", distanceInKm)
        setCustomMarkerIconOrtu(ortu, "Orang Tua",distanceOrtukeAnak1, distanceOrtukeAnak2)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(uper))

//        if (ContextCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // Izin lokasi sudah diberikan, lanjutkan membaca posisi.
//        } else {
//            // Izin belum diberikan, minta izin kepada pengguna.
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                MY_PERMISSIONS_REQUEST_LOCATION
//            )
//        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        checkLocationPermission()
//
//        // Example: Get last known location
//        getLastLocation()
//
//        // Example: Request location updates
//        requestLocationUpdates()
        makeApiCall()
    }

    // Apply Marker and Marker Option on Map (anak)
    @SuppressLint("PotentialBehaviorOverride")
    private fun setCustomMarkerIcon(location: LatLng, name: String, JarakAntarKeduanya: Double) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title(name)
            .snippet("$JarakAntarKeduanya km")

        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.icon_anak)
        )

        markerOptions.icon(bitmapDescriptor)
        val marker = mMap.addMarker(markerOptions)
        // Menambahkan event listener untuk marker
        marker?.tag = CustomInfoMarker(
            requireContext(),
            name,
            "", // Ganti dengan data alamat yang sesuai
            "Online", // Ganti dengan data status yang sesuai
            "Tanggal: tanggal" // Ganti dengan data tanggal yang sesuai
        )

        mMap.setOnMarkerClickListener {  clickedMarker ->
            // Menampilkan info window saat marker di klik
            clickedMarker.tag?.let { customInfoMarker ->
                mMap.setInfoWindowAdapter(customInfoMarker as GoogleMap.InfoWindowAdapter)
                clickedMarker.showInfoWindow()
            }
            true
        }
        mMap.addMarker(markerOptions)
    }

    // Apply Marker and Marker Option on Map (Ortu)
    @SuppressLint("PotentialBehaviorOverride")
    private fun setCustomMarkerIconOrtu(location: LatLng, name: String, JarakKeAnak1: Double, JarakKeAnak2: Double) {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.icon_ortu)
        )

        val markerOptions = MarkerOptions()
            .position(location)
            .title(name)
            .snippet("$JarakKeAnak1 km, $JarakKeAnak2 km")

        markerOptions.icon(bitmapDescriptor)
        val marker = mMap.addMarker(markerOptions)

        // Menambahkan event listener untuk marker
        marker?.tag = CustomInfoMarker(
            requireContext(),
            name,
            "Serang, Banten. ", // Ganti dengan data alamat yang sesuai
            "Online", // Ganti dengan data status yang sesuai
            "Tanggal: tanggal" // Ganti dengan data tanggal yang sesuai
        )

        mMap.setOnMarkerClickListener { clickedMarker ->
            // Menampilkan info window saat marker di klik
            clickedMarker.tag?.let { customInfoMarker ->
                mMap.setInfoWindowAdapter(customInfoMarker as GoogleMap.InfoWindowAdapter)
                clickedMarker.showInfoWindow()
            }
            true
        }
    }


    // Take all data with passing from API using Bearer Token by Retrofit Library
    object RetrofitInstance {
        private const val BASE_URL = "http://127.0.0.1:8000/parents/"

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Main Process of Managing data from API to Map Visualization
    private fun makeApiCall() {
        val apiService = RetrofitInstance.retrofit.create(APIService::class.java)
        val call = apiService.getData()

        call.enqueue(object : retrofit2.Callback<get_ortu> {
            override fun onResponse(call: Call<get_ortu>, response: retrofit2.Response<get_ortu>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("API_CALL", "Request successful. Data received: $data")
                } else {
                    // Handle kesalahan
                }
            }

            override fun onFailure(call: Call<get_ortu>, t: Throwable) {
                Log.e("API_CALL", "Network request failed: ${t.message}")
            }
        })
    }

    inner class CustomInfoMarker(
        private val context: Context,
        private val nama: String,
        private val alamat: String,
        private val status: String,
        s: String
    ) : GoogleMap.InfoWindowAdapter {

        @SuppressLint("SimpleDateFormat")
        private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

        @SuppressLint("InflateParams", "SetTextI18n")
        override fun getInfoContents(marker: Marker): View? {
            val infoView = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)

            // Temukan view yang ada di layout kustom
            val namaAnakTextView = infoView.findViewById<TextView>(R.id.nama_anak)
            val alamatTextView = infoView.findViewById<TextView>(R.id.alamat)
            val statusTextView = infoView.findViewById<TextView>(R.id.status)
            val tanggalTextView = infoView.findViewById<TextView>(R.id.tanggal)

            // Mengatur teks sesuai data yang diberikan
            namaAnakTextView.text = "Nama Anak: $nama"
            alamatTextView.text = "Alamat: $alamat"
            statusTextView.text = "Status: $status"

            // Mengatur teks tanggal sesuai waktu saat ini
            val currentDate = Date()
            val formattedDate = dateFormat.format(currentDate)
            tanggalTextView.text = "Tanggal: $formattedDate"

            return infoView
        }

        override fun getInfoWindow(marker: Marker): View? {
            return null // return null to use getInfoContents method
        }
    }

//    private fun checkLocationPermission() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // Permission granted, continue with location-related operations.
//        } else {
//            // Permission not granted, request it from the user.
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                MY_PERMISSIONS_REQUEST_LOCATION
//            )
//        }
//    }
//
//    private fun getLastLocation() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            fusedLocationClient.lastLocation
//                .addOnSuccessListener { location: Location? ->
//                    location?.let {
//                        val currentLatLng = LatLng(it.latitude, it.longitude)
//                        // Do something with currentLatLng
//                    }
//                }
//        }
//    }
//
//    private fun requestLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            val locationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(10000) // 10 seconds
//
//            val locationCallback = object : LocationCallback() {
//                override fun onLocationResult(locationResult: LocationResult) {
//                    locationResult.lastLocation?.let {
//                        val updatedLatLng = LatLng(it.latitude, it.longitude)
//                        // Do something with updatedLatLng
//                    }
//                }
//            }
//
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest,
//                locationCallback,
//                Looper.getMainLooper()
//            )
//        }
//    }

}