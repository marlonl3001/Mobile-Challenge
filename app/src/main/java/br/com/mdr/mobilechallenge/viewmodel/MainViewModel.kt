package br.com.mdr.mobilechallenge.viewmodel

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.mdr.mobilechallenge.App
import br.com.mdr.mobilechallenge.R
import br.com.mdr.mobilechallenge.databinding.DialogAddSpotBinding
import br.com.mdr.mobilechallenge.model.PontoTuristico
import br.com.mdr.mobilechallenge.util.ConfiguracaoFirebase
import br.com.mdr.mobilechallenge.util.Constants.Companion.IMG_REQUEST_CODE
import br.com.mdr.mobilechallenge.util.Constants.Companion.PONTOS_BUSCA_TABLE
import br.com.mdr.mobilechallenge.util.Constants.Companion.PONTOS_TURISTICOS_TABLE
import br.com.mdr.mobilechallenge.util.Utils
import br.com.mdr.mobilechallenge.view.MainFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.IOException


class MainViewModel : ViewModel() {
    var pontosFiltrados = MutableLiveData(mutableListOf<PontoTuristico>())
    var pontosRecentes = MutableLiveData(mutableListOf<PontoTuristico>())
    val pontoTuristico = PontoTuristico()
    var dialogAddSpotBinding: DialogAddSpotBinding? = null
    var mainFragment: MainFragment? = null
    var showList = MutableLiveData(false)

    var imgSpot: Uri? = null
        set(value) {
            field = value
            if (value != null && dialogAddSpotBinding != null) {
                dialogAddSpotBinding!!.imgPonto.setImageURI(value)
                dialogAddSpotBinding!!.imgPonto.scaleType = ImageView.ScaleType.FIT_XY
            }
        }

    fun clickListener() = View.OnClickListener {
        when (it.id) {
            R.id.imgAdd -> showDialogNovoPonto()
        }
    }

    private fun showDialogNovoPonto() {
        val inflater = App.activity?.layoutInflater!!
        dialogAddSpotBinding = DialogAddSpotBinding.inflate(inflater)
        val builder = AlertDialog.Builder(App.activity!!)
        builder.setView(dialogAddSpotBinding!!.root)
        val show = builder.show()
        dialogAddSpotBinding!!.edtCor.addTextChangedListener(edtCorTextChange())
        dialogAddSpotBinding!!.btnSalvar.setOnClickListener {
            pontoTuristico.nome = dialogAddSpotBinding!!.edtNome.text.toString()
            pontoTuristico.categoria = dialogAddSpotBinding!!.edtCategoria.text.toString()
            pontoTuristico.endereco = dialogAddSpotBinding!!.edtLocal.text.toString().trim()
            salvaPonto()
            show.dismiss()
        }
        dialogAddSpotBinding!!.imgPonto.setOnClickListener {
            tiraFoto()
        }
    }

    private fun tiraFoto() {
        if (Utils.hasPermission(Manifest.permission.CAMERA) &&
            Utils.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mainFragment!!.startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                IMG_REQUEST_CODE)
        } else
            Utils.checkPermissions()
    }

    private fun salvaPonto() {
        App.activity?.showLoading()
        val firebaseReference = ConfiguracaoFirebase.getFirebase()!!
            .child(PONTOS_TURISTICOS_TABLE)
        val idPonto = firebaseReference.push().key
        pontoTuristico.id = idPonto!!

        val storageReference = ConfiguracaoFirebase.getFirebaseStorage()!!
            .child(PONTOS_TURISTICOS_TABLE)
            .child(idPonto)

        val uploadTask = storageReference.putFile(imgSpot!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Retorna a Uri (link da imagem no Firebase)
                pontoTuristico.urlImagem = task.result!!.toString()
                if (pontoTuristico.endereco.isNotEmpty()) {
                    val location = getLocationFromAddress(pontoTuristico.endereco.trim())
                    pontoTuristico.latitude = location!!.latitude
                    pontoTuristico.longitute = location.longitude
                    pontoTuristico.salvar()
                }

                App.activity?.hideLoading()
                Utils.showSuccesSnack("${pontoTuristico.nome} foi salvo corretamente.")
            } else  {
                Utils.showErrorSnack("Erro ao salvar imagem.")
            }
        }
    }

    private fun edtCorTextChange() = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) = Unit

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            pontoTuristico.cor = s.toString()

            if (s!!.length == 7) {
                //Altera a cor do icone da ImageView
                try {
                    val iconColor = Color.parseColor(s.toString())
                    dialogAddSpotBinding!!.imgCor.setColorFilter(iconColor)
                    dialogAddSpotBinding!!.layoutCor.isErrorEnabled = false
                } catch (error: Exception) {
                    dialogAddSpotBinding!!.layoutCor.error = "Cor inválida"
                    dialogAddSpotBinding!!.layoutCor.isErrorEnabled = true
                }

            } else {
                dialogAddSpotBinding!!.layoutCor.isErrorEnabled = true
                dialogAddSpotBinding!!.layoutCor.error = "Cor inválida"
            }
        }
    }

    fun edtBuscaTextChange(s: CharSequence) {
        val nomeBusca = s.toString()
        if (s.isEmpty())
            buscaPontos("")
        else if (nomeBusca.length > 3) {
            buscaPontos(nomeBusca)
        }
    }

    private fun buscaPontos(filtroBusca: String) {
        val firebaseRef = ConfiguracaoFirebase.getFirebase()!!
            .child(
                if (filtroBusca.isEmpty())
                    PONTOS_BUSCA_TABLE
                else
                    PONTOS_TURISTICOS_TABLE)

        firebaseRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                if (dataSnapShot.exists()) {
                    var listaPontos =
                        if (filtroBusca.isNotEmpty())
                            pontosFiltrados.value
                        else
                            pontosRecentes.value

                    if (listaPontos == null)
                        listaPontos = mutableListOf()
                    else
                        listaPontos.clear()

                    for(snapShot in dataSnapShot.children) {
                        val ponto = snapShot.getValue(PontoTuristico::class.java)!!
                        //Faz a média de nota dada nos comentários
                        if (ponto.comentarios != null) {
                            var nota = 0f
                            var count = 0
                            for(comentario in ponto.comentarios!!) {
                                count += 1
                                nota += comentario.nota
                            }
                            nota = (nota / count)
                            ponto.nota = nota
                        }
                        val pontoLocation = Location("User Distance")
                        pontoLocation.latitude = ponto.latitude
                        pontoLocation.longitude = ponto.longitute
                        val dist = mainFragment?.userLocation?.distanceTo(pontoLocation)!!
                        //Verifica se a distância é menor que 5km
                        if (dist <= 5000.0 && ponto.nome.contains(filtroBusca, true)) {
                            listaPontos.add(ponto)
                        }
                    }
                    if (filtroBusca.isEmpty()) {
                        pontosFiltrados.value?.clear()
                        pontosRecentes.value = listaPontos
                    } else {
                        pontosRecentes.value?.clear()
                        pontosFiltrados.value = listaPontos
                    }
                    showList.value = listaPontos.size > 0
                }
            }
        })
    }

    fun getLocationFromAddress(strAddress: String?): LatLng? {
        val coder = Geocoder(App.context)
        val address: List<Address>?
        var p1: LatLng? = null
        try { // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            val location: Address = address[0]
            p1 = LatLng(location.getLatitude(), location.getLongitude())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return p1
    }

    fun buscaPontos() {
        val reference = ConfiguracaoFirebase.getFirebase()!!
            .child(PONTOS_TURISTICOS_TABLE)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) = Unit

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                if (dataSnapShot.exists()) {
                    App.activity!!.pontosTuristicos.clear()

                    for(snapShot in dataSnapShot.children) {
                        val ponto = snapShot.getValue(PontoTuristico::class.java)!!
                        App.activity!!.pontosTuristicos.add(ponto)
                    }
                }
            }
        })
    }
}
