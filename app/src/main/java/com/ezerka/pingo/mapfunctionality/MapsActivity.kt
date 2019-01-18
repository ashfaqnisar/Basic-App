package com.ezerka.pingo.mapfunctionality

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast

//Maps Imports
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.type.LatLng
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment


//Firebase Imports
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

//Dexter Imports
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class MapsActivity : AppCompatActivity(), OnMapReadyCallback
{


    //Constant Variables
    private val TAG:String = "MapsActivity: "
    private var mContext:Context = applicationContext

    //Firebase Variables
    private var mAuth:FirebaseAuth? = null
    private var mAuthListener:FirebaseAuth.AuthStateListener? = null
    private var mUser:FirebaseUser? = null
    private var mDatabase: FirebaseFirestore? = null

    //Normal Varaibles
    private var mChangeLocationButton: Button? = null
    private var mMapsLoadingBar:ProgressBar?  = null


    //Maps Variables
    private var mMap: GoogleMap? = null
    private var mMapFragment:SupportMapFragment? = null
    private var mLatLong:LatLng? = null
    private var mLastLocationDetected: Location? =null
    private var mLocationRequest: LocationRequest?  = null
    private var mLocationCallback:LocationCallback? = null
    private var mFusedLocationClient:FusedLocationProviderClient? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        assignTheViews()
        requestTheMapPermission()
        assignTheLinks()
        assignTheMethods()

    }



    private fun assignTheViews() {
        showLoadingbar("assignTheViews")
        mChangeLocationButton  = findViewById(R.id.id_But_ChangeLocation)
        mMapsLoadingBar  = findViewById(R.id.id_PB_Maps_Loading_Bar)

        mContext  = applicationContext

        mAuth  =  FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser

        mAuthListener  = FirebaseAuth.AuthStateListener { firebaseAuth ->
            mUser = firebaseAuth.currentUser

            if (mUser!=null){
                startTheActivity(MainActivity::class.java)
            }
            else{
                startTheActivity(LoginActivity::class.java)
            }
        }
        mDatabase = FirebaseFirestore.getInstance()

        mMapFragment = supportFragmentManager.findFragmentById(R.id.id_Map_Main_Fragment) as SupportMapFragment
        mMapFragment!!.getMapAsync(this)

        mLocationRequest = LocationRequest()
        mFusedLocationClient = FusedLocationProviderClient(this)
    }

    private fun assignTheLinks() {
        mChangeLocationButton!!.setOnClickListener {
            log("Changed the location in the map")
            makeToast("Change the location button clicked")
        }
    }

    private fun assignTheMethods() {
        attachLocalCallback()
    }

    private fun requestTheMapPermission() {
        log("Requesting the Map Permissions")
        Dexter.withActivity(this)

            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE)

            .withListener(object: MultiplePermissionsListener{

                override fun onPermissionsChecked(reportResult: MultiplePermissionsReport?) {
                    log("Checking Whether all the permissions are granted")

                    if (reportResult!!.areAllPermissionsGranted()) {
                        log("All permissions are granted")
                        makeToast("All Permissions Are Granted")
                    }

                    if (reportResult.isAnyPermissionPermanentlyDenied) {
                        log("Unable to grant all the permission")
                        makeToast("Unable to provide all the permissions")
                    }

                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {

                    token!!.continuePermissionRequest()
                }
            })

            .withErrorListener{error ->
                log("A error has been occured: $error")
                makeToast("Error Occured")
            }

            .onSameThread()
            .check()


    }

    override fun onMapReady(googleMap: GoogleMap) {
        closeLoadingBar("onMapReady()")
        requestTheMapPermission()
        mMap = googleMap

        mLocationRequest!!.apply {
            interval = 3000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
            mMap!!.isMyLocationEnabled = true
        }

        else{
            requestTheMapPermission()
            logError("Unable to assign the permissions")
            makeToast("Please Provide Permissions")
        }

    }


    private fun checkThePermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestTheMapPermission()
            logError("Unable to assign the permissions")
            makeToast("Please provide the permission to make the  application work")
        }
        else{
            log("Permissions for the map are granted")
        }
    }

    private fun attachLocalCallback() {
        mLocationCallback = object: LocationCallback(){
            override fun onLocationResult(result: LocationResult?) {
                checkThePermission()
                super.onLocationResult(result)

                for (location: Location in result!!.locations ){

                }

            }
        }
    }

    private fun connectTheDriver(){
        checkThePermission()
        mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
        mMap!!.isMyLocationEnabled = true
    }

    private fun disconnectTheDriver(){
        mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
    }




    private fun log(log:String){
        Log.d(TAG,log)

    }

    private fun logError(error:String){
        Log.w(TAG,error)
    }

    private fun startTheActivity(mClass: Class<*>) {
        log("Starting the $mClass.class Activity")
        val intent = Intent(mContext, mClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        log("Opened the $mClass.class Activity")
        finish()
    }

    private fun showLoadingbar(method : String){
        log("Loading Bar has been started by $method")
        mMapsLoadingBar!!.visibility= View.VISIBLE
    }

    private fun closeLoadingBar(method: String){
        log("Loading Bar has been closed by $method")
        mMapsLoadingBar!!.visibility = View.GONE
    }

    private fun makeToast(toast: String) {
        log("Making a toast of $toast")
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener { mAuthListener }
    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener { mAuthListener }
    }
}
