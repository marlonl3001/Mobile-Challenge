package br.com.mdr.mobilechallenge.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import br.com.mdr.mobilechallenge.App
import br.com.mdr.mobilechallenge.databinding.DialogAddComentarioBinding
import br.com.mdr.mobilechallenge.databinding.PontoItemBinding
import br.com.mdr.mobilechallenge.databinding.PontosHeaderBinding
import br.com.mdr.mobilechallenge.model.Comentario
import br.com.mdr.mobilechallenge.model.PontoTuristico
import br.com.mdr.mobilechallenge.util.Constants.Companion.FAVORITOS_DESC
import br.com.mdr.mobilechallenge.util.Constants.Companion.RECENTES_DESC
import br.com.mdr.mobilechallenge.util.Utils
import br.com.mdr.mobilechallenge.view.PontoTuristicoBottomSheet
import br.com.mdr.mobilechallenge.viewmodel.MainViewModel
import com.squareup.picasso.Picasso

class PontosTuristicosAdapter(val mainViewModel: MainViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AdapterItemsContract{
    private val NORMAL_TYPE = 1
    private val HEADER_TYPE = 2

    private var itens: MutableList<PontoTuristico>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        if (viewType == NORMAL_TYPE) {
            val binding = PontoItemBinding.inflate(inflater, parent, false)
            return PontosTuristicosViewHolder(binding, mainViewModel)
        } else {
            val binding = PontosHeaderBinding.inflate(inflater, parent, false)
            return HeaderViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        var viewType = NORMAL_TYPE
        if (mainViewModel.pontosRecentes.value!!.size > 0) {
            val ponto = itens!![position]
            if (ponto.nome == FAVORITOS_DESC || ponto.nome == RECENTES_DESC) {
                viewType = HEADER_TYPE
            }
        }

        return viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itens!![position]

        if (holder is PontosTuristicosViewHolder) {
            holder.bind(item)
        } else {
            (holder as HeaderViewHolder).bind(item.nome)
        }
    }

    override fun getItemCount() = if (itens != null) itens!!.size else 0

    @Suppress("UNCHECKED_CAST")
    override fun replaceItens(list: List<*>) {
        itens = list as MutableList<PontoTuristico>
        notifyDataSetChanged()
    }

    class PontosTuristicosViewHolder(val binding: PontoItemBinding, val viewModel: MainViewModel):
        RecyclerView.ViewHolder(binding.root) {

        lateinit var bottomSheet: PontoTuristicoBottomSheet

        fun bind(ponto: PontoTuristico) {
            binding.ponto = ponto
            binding.executePendingBindings()
            Picasso.get().load(ponto.urlImagem).into(binding.imgPonto)
            itemView.tag = ponto

            this.itemView.setOnClickListener {
                ponto.salvaRecente()
                Utils.hideKeyBoard()
                viewModel.showList.value = false

                bottomSheet = PontoTuristicoBottomSheet.newInstance(ponto)
                bottomSheet.show(App.activity!!.supportFragmentManager, bottomSheet.tag)
            }
        }
    }

    class HeaderViewHolder(val binding: PontosHeaderBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(cabecalho: String) {
            binding.cabecalho = cabecalho
        }
    }
}