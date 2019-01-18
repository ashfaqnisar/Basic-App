package com.ezerka.pingo.mapfunctionality

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    //Constant Variables
    private val TAG: String = "MainActivity: "
    private var mContext:Context? = null

    //Normal Variables
    private var mStoreTheDataButton:Button? = null
    private var mOpenMapsButton:Button? = null
    private var mLogoutButton:Button?  = null

    private var mMainProgressBar:ProgressBar? = null

    //Firebase Variables
    private var mAuth:FirebaseAuth? = null
    private var mAuthListener:FirebaseAuth.AuthStateListener? = null
    private var mUser:FirebaseUser? = null

    private var mDatabase:FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignTheViews()
        assignTheLinks()
    }

    private fun assignTheViews() {
        mStoreTheDataButton = findViewById(R.id.id_But_StoreData)
        mLogoutButton =findViewById(R.id.id_But_Logout)
        mOpenMapsButton = findViewById(R.id.id_But_Load_Map)
        mMainProgressBar = findViewById(R.id.id_PB_Main)

        mContext = applicationContext

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser

        mAuthListener = FirebaseAuth.AuthStateListener {firebaseAuth ->
            mUser = firebaseAuth.currentUser
            if (mUser != null){
                log("User is signed in with " + mUser!!.uid)
                startTheActivity(MainActivity::class.java)
            }
            else{
                log("User is signed out")
                startTheActivity(LoginActivity::class.java)
            }
        }

        mDatabase = FirebaseFirestore.getInstance()
    }

    private fun assignTheLinks() {
        mStoreTheDataButton!!.setOnClickListener{
            log("Storing the Data")
            storeTheDataOnDB()
        }

        mOpenMapsButton!!.setOnClickListener {
            log("Starting Maps Activity")
            startTheActivity(MapsActivity::class.java)
        }

        mLogoutButton!!.setOnClickListener {
            log("Logging out the user")
            mAuth!!.signOut()
        }
    }


    private fun storeTheDataOnDB() {
        showLoadingbar("storeTheDataOnDB()")
        val user_id:String = mAuth!!.currentUser!!.uid

        val user_details = HashMap<String,Any>()
        user_details["Email Id"] = "Ashfaq"
        user_details["Password"] = "Helloworld"
        user_details["Mobile"] = "8328277518"

        mDatabase!!.collection("Users")
            .document("drivers")
            .collection(user_id)
            .add(user_details)
            .addOnCompleteListener { Task ->
                if (Task.isSuccessful){
                    closeLoadingBar("storeTheDataOnDB():Task Successful")
                    log("Data has been successfully stored ")
                    makeToast("Data Stored Successfully")
                }
                else{
                    closeLoadingBar("storeTheDataOnDB(): Task Failed")
                    log_error("Unable to store the data " + Task.exception)
                    makeToast("Error: " + Task.exception.toString())
                }
            }
    }

    private fun log(log:String){
        Log.d(TAG,log)
    }

    private fun log_error(error:String){
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
        mMainProgressBar!!.visibility=View.VISIBLE
    }

    private fun closeLoadingBar(method: String){
        log("Loading Bar has been closed by $method")
        mMainProgressBar!!.visibility = View.GONE
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
