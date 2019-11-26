package br.com.mdr.mobilechallenge.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import br.com.mdr.mobilechallenge.App
import br.com.mdr.mobilechallenge.databinding.ComentarioItemBinding
import br.com.mdr.mobilechallenge.databinding.DialogAddComentarioBinding
import br.com.mdr.mobilechallenge.databinding.PontoItemBinding
import br.com.mdr.mobilechallenge.model.Comentario
import br.com.mdr.mobilechallenge.model.PontoTuristico
import br.com.mdr.mobilechallenge.util.Utils
import br.com.mdr.mobilechallenge.view.PontoTuristicoBottomSheet
import br.com.mdr.mobilechallenge.viewmodel.MainViewModel
import com.squareup.picasso.Picasso

class ComentariosAdapter: RecyclerView.Adapter<ComentariosAdapter.ComentariosViewHolder>(),
    AdapterItemsContract{

    private var itens: MutableList<Comentario>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentariosViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ComentarioItemBinding.inflate(inflater, parent, false)
        return ComentariosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComentariosViewHolder, position: Int) {
        val item = itens!![position]
        holder.bind(item)
    }

    override fun getItemCount() = if (itens != null) itens!!.size else 0

    @Suppress("UNCHECKED_CAST")
    override fun replaceItens(list: List<*>) {
        itens = list as MutableList<Comentario>
        notifyDataSetChanged()
    }

    class ComentariosViewHolder(val binding: ComentarioItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comentario: Comentario) {
            binding.comentario = comentario
            binding.txtNota.text = (comentario.nota.toInt()).toString()
            binding.executePendingBindings()
        }
    }
}