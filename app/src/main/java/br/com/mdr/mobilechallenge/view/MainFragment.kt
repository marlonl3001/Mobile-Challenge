package br.com.mdr.mobilechallenge.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.transition.TransitionInflater
import androidx.lifecycle.Observer
import br.com.mdr.mobilechallenge.App

import br.com.mdr.mobilechallenge.R
import br.com.mdr.mobilechallenge.adapter.PontosTuristicosAdapter
import br.com.mdr.mobilechallenge.databinding.AlertLayoutBinding
import br.com.mdr.mobilechallenge.databinding.MainFragmentBinding
import br.com.mdr.mobilechallenge.model.Categoria
import br.com.mdr.mobilechallenge.model.PontoTuristico
import br.com.mdr.mobilechallenge.util.ConfiguracaoFirebase
import br.com.mdr.mobilechallenge.util.Constants.Companion.CATEGORIA_FIREBASE_TABLE
import br.com.mdr.mobilechallenge.util.Constants.Companion.FAVORITOS_DESC
import br.com.mdr.mobilechallenge.util.Constants.Companion.GPS_REQUEST_CODE
import br.com.mdr.mobilechallenge.util.Constants.Companion.IMG_REQUEST_CODE
import br.com.mdr.mobilechallenge.util.Constants.Companion.RECENTES_DESC
import br.com.mdr.mobilechallenge.util.Utils
import br.com.mdr.mobilechallenge.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MainFragment : Fragment(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var locationManager = App.context.applicationContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val mGoogleMapZoom = 13f
    private var mUserPosition = LatLng(0.0, 0.0)
    var userLocation: Location? = null
    private var alertGps: AlertDialog? = null

    private lateinit var viewModel: MainViewModel
    private lateinit var mainBinding: MainFragmentBinding
    lateinit var adapter: PontosTuristicosAdapter
    private var firebaseUser: FirebaseUser? = null

    private var locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            mainBinding.hasLocation = location != null

            if (location != null) {
                mUserPosition = LatLng(location.latitude, location.longitude)
                atualizaLocalizacaoUsuario()
            }
        }
        override fun onProviderDisabled(provider: String?) = Unit

        override fun onProviderEnabled(provider: String?) = Unit

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(App.context)
            .inflateTransition(android.R.transition.move).setDuration(300)
        App.activity?.mainFragment = this
        criaCategorias()
        Utils.checkPermissions()
        firebaseUser = ConfiguracaoFirebase.getFirebaseAuth()?.currentUser
        if (firebaseUser == null) {
            Utils.showErrorSnack("Erro ao autenticar Login.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = MainViewModel()
        viewModel.buscaPontos()
        viewModel.mainFragment = this
        mainBinding = MainFragmentBinding.inflate(inflater)
        mainBinding.hasLocation = false
        mainBinding.viewModel = viewModel
        adapter = PontosTuristicosAdapter(viewModel)
        mainBinding.recyclerPontos.adapter = adapter
        return mainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        fetchObservers()

        view.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                Utils.hideKeyBoard()
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        App.view = view!!
        verifyLocationServiceEnabled()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            GPS_REQUEST_CODE -> {
                for (i in permissions.indices) {
                    val permission = permissions[i]
                    val grantResult = grantResults[i]

                    if (permission == Manifest.permission.ACCESS_COARSE_LOCATION || permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            getUserLocation()
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMG_REQUEST_CODE && resultCode == RESULT_OK) {
            //Adiciona a uri da imagem à ViewModel
            viewModel.imgSpot = data?.data
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onMapReady(map: GoogleMap?) {
        mMap = map!!

        moveToMyPlace()
        getUserLocation()
    }

    private fun fetchObservers() {
        viewModel.pontosFiltrados.observe(viewLifecycleOwner, Observer {
            if (it.size > 0)
                adapter.replaceItens(it)
        })

        viewModel.pontosRecentes.observe(viewLifecycleOwner, Observer {
            if (it.size > 0) {
                //Reorganiza a lista
                val resList = mutableListOf<PontoTuristico>()
                //Adiciona os de busca recentes
                val pontoCabecalho = PontoTuristico()
                pontoCabecalho.nome = RECENTES_DESC
                resList.add(pontoCabecalho)

                var hasFavorite = false

                for(ponto in it) {
                    if (!ponto.favorito)
                        resList.add(ponto)
                    else
                        hasFavorite = true
                }

                if (hasFavorite) {
                    //Adiciona os pontos favoritos
                    val pontoFavorito = PontoTuristico()
                    pontoFavorito.nome = FAVORITOS_DESC
                    resList.add(pontoFavorito)
                }

                for(ponto in it) {
                    if (ponto.favorito)
                        resList.add(ponto)
                }

                adapter.replaceItens(resList)
            }
        })

        viewModel.showList.observe(viewLifecycleOwner, Observer {
            mainBinding.showList = it
        })
    }

    private fun criaCategorias() {
        val firebaseRef = ConfiguracaoFirebase.getFirebase()!!
            .child(CATEGORIA_FIREBASE_TABLE)

        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount <= 0) {
                    for (i in 1..5) {
                        val idFirebase = firebaseRef.push().key!!
                        val nomeCat =
                            when (i) {
                                1 -> "Parque"
                                2 -> "Museu"
                                3 -> "Teatro"
                                4 -> "Monumento"
                                5 -> "Shopping"
                                else -> "Default"
                            }

                        val cat = Categoria(idFirebase, nomeCat)
                        firebaseRef.child(idFirebase).setValue(cat)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print(error.message)
            }
        })

    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(App.context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(App.context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Utils.checkPermissions()
        } else
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                3,
                0f,
                locationListener
            )
    }

    private fun verifyLocationServiceEnabled() {
        if (alertGps != null)
            alertGps?.cancel()

        var gpsEnable = false
        var networkEnabled = false

        try {
            gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }catch (e: Exception){}

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception){}

        if (!gpsEnable && !networkEnabled) {
            val binding = AlertLayoutBinding.inflate(App.activity!!.layoutInflater)
            val builder = AlertDialog.Builder(App.activity!!)
            builder.setView(binding.root)
            builder.setCancelable(false)

            alertGps = builder.show()
            binding.apply {
                msg = activity?.getString(R.string.msg_localizacao_n_encontrada)
                title = activity?.getString(R.string.localizacao_n_ativada)
                cancelDesc = "Cancelar"
                btnCancel.setOnClickListener {
                    alertGps?.cancel()
                }
                okDesc = "Ativar"
                btnOk.setOnClickListener {
                    alertGps?.cancel()
                    startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_REQUEST_CODE)
                }
            }
        }
    }

    private fun atualizaLocalizacaoUsuario() {
        if (this@MainFragment.isVisible && mMap != null) {
            mMap!!.clear()

            mMap!!.addMarker(
                MarkerOptions()
                    .position(mUserPosition)
                    .title(getString(R.string.meu_local))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_mark))
            )

            if (App.activity!!.pontosTuristicos.size > 0) {
                for(ponto in App.activity!!.pontosTuristicos) {
                    val pontoLocation = Location("User Distance")
                    pontoLocation.latitude = ponto.latitude
                    pontoLocation.longitude = ponto.longitute
                    val dist = userLocation?.distanceTo(pontoLocation)!!
                    //Verifica se a distância é menor que 5km
                    if (dist <= 5000.0) {
                        mMap!!.addMarker(
                            MarkerOptions()
                                .position(LatLng(ponto.latitude, ponto.longitute))
                                .title(ponto.nome)
                                .icon(getMarkerIcon(ponto.cor)))

                    }
                }
            }

            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserPosition, mGoogleMapZoom))
        }
        //fetchAddress()
    }

    private fun getMarkerIcon(color: String): BitmapDescriptor {
        val hsv = FloatArray(3)
        Color.colorToHSV(Color.parseColor(color), hsv)
        return BitmapDescriptorFactory.defaultMarker(hsv[0])
    }

    private fun moveToMyPlace(): Location? {
        if (ActivityCompat.checkSelfPermission(App.context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(App.context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (userLocation == null)
                userLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (userLocation == null)
                userLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            mainBinding.hasLocation = userLocation != null

            if (userLocation != null) {
                mUserPosition = LatLng(userLocation!!.latitude, userLocation!!.longitude)
                atualizaLocalizacaoUsuario()

                mMap!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(mUserPosition, mGoogleMapZoom)
                )
            }
        }
        return userLocation
    }


//    private fun bitmapDescriptorFromVector(context: Context, vectorDrawableResourceId: Int): BitmapDescriptor {
//        val background = ContextCompat.getDrawable(context, R.drawable.ic_place)
//        background?.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
//        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
//        vectorDrawable?.setBounds(40, 20, vectorDrawable.intrinsicWidth + 40, vectorDrawable.intrinsicHeight + 20)
//        val bitmap = Bitmap.createBitmap(background!!.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        background.draw(canvas)
//        vectorDrawable?.draw(canvas)
//        return BitmapDescriptorFactory.fromBitmap(bitmap)
//    }
}
