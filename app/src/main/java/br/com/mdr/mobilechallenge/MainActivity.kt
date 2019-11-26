package br.com.mdr.mobilechallenge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import br.com.mdr.mobilechallenge.model.PontoTuristico
import br.com.mdr.mobilechallenge.view.MainFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var mainFragment: MainFragment
    lateinit var navBottom: BottomNavigationView
    var pontosTuristicos = mutableListOf<PontoTuristico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        App.activity = this
        App.context = applicationContext
        setupNavigation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (mainFragment != null)
            mainFragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupNavigation() {
        navBottom = bottomNavigation
        val navController = Navigation.findNavController(this, R.id.fragmentMain)
        navBottom.setupWithNavController(navController)
    }

    fun hideBottomNavigation() {
        navBottom.visibility = View.GONE
    }

    fun showBottomNavigation() {
        navBottom.visibility = View.VISIBLE
    }

    fun showLoading() {
        layoutProgress.visibility = View.VISIBLE
    }

    fun hideLoading() {
        layoutProgress.visibility = View.GONE
    }
}
