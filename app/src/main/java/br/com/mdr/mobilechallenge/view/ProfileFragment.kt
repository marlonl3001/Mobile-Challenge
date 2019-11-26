package br.com.mdr.mobilechallenge.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import br.com.mdr.mobilechallenge.databinding.ProfileFragmentBinding
import br.com.mdr.mobilechallenge.util.ConfiguracaoFirebase
import com.squareup.picasso.Picasso

class ProfileFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = ProfileFragmentBinding.inflate(inflater)
        val firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth()!!
        val user = firebaseAuth.currentUser!!
        binding.user = user
        Picasso.get().load(user.photoUrl).into(binding.imgProfile)
        binding.btnLogout.setOnClickListener(logoutUser())
        return binding.root
    }

    private fun logoutUser() = View.OnClickListener {
        val firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth()!!
        firebaseAuth.signOut()
        val direction = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
        it.findNavController().navigate(direction)
    }
}