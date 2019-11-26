package br.com.mdr.mobilechallenge.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import br.com.mdr.mobilechallenge.App
import br.com.mdr.mobilechallenge.view.LoginFragmentDirections

class LoginViewModel : ViewModel() {

    fun loginUser(view: View) {
        App.activity?.showBottomNavigation()
        val direction = LoginFragmentDirections.actionLoginFragmentToMainFragment()
        view.findNavController().navigate(direction)
    }
}