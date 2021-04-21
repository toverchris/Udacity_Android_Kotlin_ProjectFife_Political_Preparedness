package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_representative.*
import kotlinx.coroutines.runBlocking
import java.util.*

class DetailFragment : Fragment() {

    private var userLocation : Location? = null
    private lateinit var adapter : ArrayAdapter<CharSequence>
    private var storedAddress : Address? = null

    companion object {
        private const val REQUEST_FOREGROUND_PERMISSIONS_REQUEST_CODE = 34
    }

    private val _viewModel: RepresentativeViewModel by activityViewModels()//viewModels()
    private lateinit var binding: FragmentRepresentativeBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_representative,
                container,
                false
        )

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        initSpinner()

        binding.recyclerViewRepresentatives.adapter = RepresentativeListAdapter()

        if (savedInstanceState != null) {
            storedAddress = Address(
                    savedInstanceState.getString("addressLine1").toString(),
                    savedInstanceState.getString("addressLine2").toString(),
                    savedInstanceState.getString("city").toString(),
                    savedInstanceState.getString("state").toString(),
                    savedInstanceState.getString("zip").toString()
            )
        }

        binding.buttonLocation.setOnClickListener {
            if (checkLocationPermissions()){
                getLocation()
                _viewModel.getRepresentativesFromApi(getAddressAsString())
            }
        }

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            _viewModel.getRepresentativesFromApi(getAddressAsString())
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("addressLine1", binding.addressLine1.text.toString())
        outState.putString("addressLine2", binding.addressLine2.text.toString())
        outState.putString("city", binding.city.text.toString())
        outState.putString("zip", binding.zip.text.toString())
        outState.putString("state", binding.state.selectedItem.toString())
    }

    override fun onResume() {
        super.onResume()
        storedAddress?.let {
            updateInputFields(it)
            storedAddress = null
        }
    }

    private fun getAddressAsString(): String{
        if(binding.state.selectedItem.toString().isNotEmpty()){
            return binding.state.selectedItem.toString()
        }else if(!binding.city.text.isNullOrEmpty()){
            return binding.city.text.toString()
        }else if(!binding.zip.text.isNullOrEmpty()){
            return binding.zip.text.toString()
        } else
            return "denver"
    }

    private fun initSpinner(){
        val spinner = binding.state
        adapter =  ArrayAdapter.createFromResource(context!!,R.array.states,android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun updateSpinner(state: String){
        val spinner = binding.state
        val arrayList = resources.getStringArray(R.array.states)
        val mutableArrayList = arrayList.toMutableList()
        mutableArrayList.add(state)

        adapter = ArrayAdapter(context!!,android.R.layout.simple_spinner_item, mutableArrayList.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(mutableArrayList.indexOf(state))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_FOREGROUND_PERMISSIONS_REQUEST_CODE) getLocation()
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
            )
            getLocation()
            false
        }
    }

    private fun isPermissionGranted() : Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(context!!,
                                Manifest.permission.ACCESS_FINE_LOCATION))
        return foregroundLocationApproved
    }

    private fun getLocation() = runBlocking {
        try {
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!) as FusedLocationProviderClient
            val locationResult = fusedLocationProviderClient.lastLocation

            locationResult.addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    userLocation = task.result
                    if (userLocation != null) {
                        hideKeyboard()
                        val address : Address  = geoCodeLocation(userLocation!!)
                        updateInputFields(address)
                    }else {
                        Toast.makeText(context, "Can not get your location. Using default location", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Can not get your location. Please typ in manually", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun updateInputFields(address: Address){
        binding.addressLine1.text.clear()
        binding.addressLine1.text.insert(0,address.line1)
        binding.addressLine2.text.clear()
        binding.addressLine2.text.insert(0,address.line2)
        binding.city.text.clear()
        binding.city.text.insert(0, address.city)
        binding.zip.text.clear()
        binding.zip.text.insert(0, address.zip)
        updateSpinner(address.state)
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}