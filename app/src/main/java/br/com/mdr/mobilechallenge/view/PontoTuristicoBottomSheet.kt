package br.com.mdr.mobilechallenge.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.mdr.mobilechallenge.R
import br.com.mdr.mobilechallenge.adapter.ComentariosAdapter
import br.com.mdr.mobilechallenge.databinding.BottomSheetPontoBinding
import br.com.mdr.mobilechallenge.model.Comentario
import br.com.mdr.mobilechallenge.model.PontoTuristico
import br.com.mdr.mobilechallenge.util.ConfiguracaoFirebase
import br.com.mdr.mobilechallenge.util.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso

class PontoTuristicoBottomSheet(val ponto: PontoTuristico): BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetPontoBinding
    lateinit var comentariosAdapter: ComentariosAdapter

    companion object {
        fun newInstance(pontoTur: PontoTuristico) = PontoTuristicoBottomSheet(pontoTur)
    }
    override fun getTheme() = R.style.AppTheme_BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetPontoBinding.inflate(inflater, container, false)
        binding.ponto = ponto
        binding.clickListener = clickListener()
        binding.showComments = ponto.comentarios != null
        comentariosAdapter = ComentariosAdapter()

        if (ponto.comentarios != null)
            comentariosAdapter.replaceItens(ponto.comentarios!!)

        binding.recyclerComentarios.adapter = comentariosAdapter
        binding.btnFavorito.setImageResource(
            if (ponto.favorito)
                R.drawable.ic_favorite
            else
                R.drawable.ic_favorite_border)
        Picasso.get().load(ponto.urlImagem).into(binding.imgPonto)
        return binding.root
    }

    private fun clickListener() = View.OnClickListener {
        when (it.id) {
            R.id.btnFavorito -> {
                ponto.favorito = !ponto.favorito
                binding.btnFavorito.setImageResource(
                    if (ponto.favorito)
                            R.drawable.ic_favorite
                    else
                        R.drawable.ic_favorite_border)

                ponto.salvaFavorito()
                Utils.showSuccesSnack("${ponto.nome} agora é um ponto turístico favorito.")
                binding.executePendingBindings()
            }
            R.id.btnComentario -> {
                binding.showReview = true
            }
            R.id.btnSalvar -> {
                val msg = binding.edtComentario.text.toString().trim()
                val nota = binding.ratingPonto.rating
                val comentario = Comentario()
                comentario.nota = nota
                comentario.msg = msg
                val firebaseUser = ConfiguracaoFirebase.getFirebaseAuth()!!.currentUser
                comentario.usuario = firebaseUser?.displayName!!
                if (ponto.comentarios == null)
                    ponto.comentarios = mutableListOf()

                ponto.comentarios!!.add(comentario)
                ponto.salvar()
                binding.ponto = ponto
                binding.executePendingBindings()
                binding.showReview = false

            }
        }
    }
}