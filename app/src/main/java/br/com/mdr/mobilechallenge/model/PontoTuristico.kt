package br.com.mdr.mobilechallenge.model

import br.com.mdr.mobilechallenge.util.ConfiguracaoFirebase
import br.com.mdr.mobilechallenge.util.Constants.Companion.PONTOS_BUSCA_TABLE
import br.com.mdr.mobilechallenge.util.Constants.Companion.PONTOS_TURISTICOS_TABLE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PontoTuristico {
    var id: String = ""
    var nome: String = ""
    var categoria: String = ""
    var endereco: String = ""
    var latitude: Double = 0.0
    var longitute: Double = 0.0
    var cor: String = ""
    var urlImagem: String = ""
    var favorito: Boolean = false
    var nota: Float = 0f
    var descricao: String = ""
    var comentarios: MutableList<Comentario>? = null

    fun salvar() {
        val reference = ConfiguracaoFirebase.getFirebase()!!
            .child(PONTOS_TURISTICOS_TABLE)
            .child(id)
        reference.setValue(this)
    }

    fun salvaFavorito() {
        val reference = ConfiguracaoFirebase.getFirebase()!!
            .child(PONTOS_BUSCA_TABLE)
            .child(id)
        if (!favorito)
            reference.removeValue()
        else
            reference.setValue(this)

        salvar()
    }

    fun salvaRecente() {
        val reference = ConfiguracaoFirebase.getFirebase()!!
            .child(PONTOS_BUSCA_TABLE)
            .child(id)
        reference.setValue(this)
    }
}