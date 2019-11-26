package br.com.mdr.mobilechallenge.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.mdr.mobilechallenge.App

import br.com.mdr.mobilechallenge.databinding.LoginFragmentBinding
import br.com.mdr.mobilechallenge.util.Utils
import br.com.mdr.mobilechallenge.viewmodel.LoginViewModel
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception
import java.util.*

class LoginFragment : Fragment() {
    private lateinit var viewModel: LoginViewModel
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var callbackManager: CallbackManager
    lateinit var loginBinding: LoginFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        FacebookSdk.sdkInitialize(App.activity!!.applicationContext)
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        App.activity?.hideBottomNavigation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = LoginViewModel()
        loginBinding = LoginFragmentBinding.inflate(inflater)
        loginBinding.viewModel = viewModel
        loginBinding.btnSignIn.setOnClickListener(fbClickListener())

        LoginManager.getInstance().registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onError(error: FacebookException?) {
                Log.e("App", error?.message)
            }
            override fun onCancel() {
                Log.e("App", "CANCELADO")
            }

            override fun onSuccess(result: LoginResult?) {
                signInWithToken(result!!.accessToken)
            }
        })

        return loginBinding.root
    }

    override fun onResume() {
        super.onResume()
        val user = firebaseAuth.currentUser
        if (user != null) {
            viewModel.loginUser(loginBinding.btnSignIn)
        }
    }

    private fun signInWithToken(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnFailureListener { e->
                Utils.showErrorSnack(e.message!!)
            }
            .addOnSuccessListener {
                viewModel.loginUser(loginBinding.btnSignIn)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun fbClickListener() = View.OnClickListener {
        try {
            LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("public_profile", "email"))
        } catch(e: Exception) {
            Log.e("App", e.message)
        }
    }
}
