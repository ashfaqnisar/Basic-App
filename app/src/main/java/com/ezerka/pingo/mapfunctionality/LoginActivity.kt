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


class LoginActivity : AppCompatActivity() {

    //Constant Variables
    private val TAG: String = "LoginActivity: "
    private var mContext:Context? = null

    //Normal Variables
    private var mEmailET:EditText? = null
    private var mPassET:EditText? = null
    private var mLoginButton:Button? = null
    private var mLoginRegisterButton:Button? = null
    private var mSkipLoginText:TextView? = null
    private var mLoginProgressBar:ProgressBar? = null

    //Firebase Variables
    private var mAuth:FirebaseAuth? = null
    private var mAuthListener:FirebaseAuth.AuthStateListener? = null
    private var mUser:FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        assignTheViews()
        assignTheLinks()
    }

    private fun assignTheViews() {
        mEmailET = findViewById(R.id.id_ET_Login_Email)
        mPassET = findViewById(R.id.id_ET_Login_Pass)

        mLoginButton = findViewById(R.id.id_But_Login_User)
        mLoginRegisterButton = findViewById(R.id.id_But_Login_GoToRegister)

        mSkipLoginText = findViewById(R.id.id_Text_SkipLogin)

        mLoginProgressBar = findViewById(R.id.id_PB_Login)

        mContext = applicationContext

        mAuth = FirebaseAuth.getInstance()

        mUser  = mAuth!!.currentUser

        mAuthListener = FirebaseAuth.AuthStateListener {firebaseAuth ->
            mUser = firebaseAuth.currentUser
            if (mUser != null){
                log("User is signed in with " + mUser!!.uid)
            }
            else{
                log("User is signed out")
            }
        }
    }

    private fun assignTheLinks() {
        mLoginButton!!.setOnClickListener{
            log("Logging in the user")
            loginTheUser()
        }

        mLoginRegisterButton!!.setOnClickListener{
            log("Starting The Register Activity")
            startTheActivity(RegisterActivity::class.java)
        }

        mSkipLoginText!!.setOnClickListener{
            log("Starting The Maps Activity")
            startTheActivity(MainActivity::class.java)
        }
    }


    private fun loginTheUser() {
        showLoadingbar("loginTheUser")
        val sEmail:String= mEmailET!!.text.toString().trim()
        val sPass:String= mPassET!!.text.toString().trim()

        if (checkForErrors(sEmail,sPass)){
            mAuth!!.signInWithEmailAndPassword(sEmail,sPass).addOnCompleteListener { Task ->
                if (Task.isSuccessful){
                    closeLoadingBar("loginTheUser: Successfull listener")
                    log("Successfully Logged In with user: "+ mAuth!!.uid)
                    makeToast("Signed In  Successfully ")

                    startTheActivity(MainActivity::class.java)
                }

                else{
                    closeLoadingBar("loginTheUser: Failure Listener")
                    makeToast("Unable to Sign in, Please Try Again "+Task.exception.toString())
                    log("Error: " + Task.exception.toString())
                }
            }

        }

    }

    private fun checkForErrors(Email:String,Pass:String): Boolean {
        if (Email.isEmpty()){
            mEmailET!!.error = "Please Enter The Email Id"
            return false
        }

        if (Pass.isEmpty()){
            mPassET!!.error = "Please, Enter The Password"
            return false
        }
        return true
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
        mLoginProgressBar!!.visibility=View.VISIBLE
    }

    private fun closeLoadingBar(method: String){
        log("Loading Bar has been closed by $method")
        mLoginProgressBar!!.visibility = View.GONE
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
