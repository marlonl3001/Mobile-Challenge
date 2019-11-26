package br.com.mdr.mobilechallenge.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.mdr.mobilechallenge.App
import br.com.mdr.mobilechallenge.model.PontoTuristico
import br.com.mdr.mobilechallenge.util.ConfiguracaoFirebase
import br.com.mdr.mobilechallenge.util.Constants.Companion.PONTOS_TURISTICOS_TABLE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PontosTuristicosViewModel : ViewModel() {
    var pontosFavoritos = MutableLiveData(mutableListOf<PontoTuristico>())

    fun buscaFavoritos() {
        val reference = ConfiguracaoFirebase.getFirebase()!!
            .child(PONTOS_TURISTICOS_TABLE)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) = Unit

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                if (dataSnapShot.exists()) {
                    val itens = mutableListOf<PontoTuristico>()
                    App.activity!!.pontosTuristicos.clear()

                    for(snapShot in dataSnapShot.children) {
                        val ponto = snapShot.getValue(PontoTuristico::class.java)!!
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
                        App.activity!!.pontosTuristicos.add(ponto)
                        if (ponto.favorito)
                            itens.add(ponto)
                    }
                    pontosFavoritos.value = itens
                }
            }
        })
    }
}