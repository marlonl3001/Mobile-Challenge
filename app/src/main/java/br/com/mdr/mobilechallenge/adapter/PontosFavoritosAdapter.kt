package br.com.mdr.mobilechallenge.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.mdr.mobilechallenge.App
import br.com.mdr.mobilechallenge.databinding.PontoItemBinding
import br.com.mdr.mobilechallenge.databinding.PontosHeaderBinding
import br.com.mdr.mobilechallenge.model.PontoTuristico
import br.com.mdr.mobilechallenge.util.Utils
import br.com.mdr.mobilechallenge.view.PontoTuristicoBottomSheet
import br.com.mdr.mobilechallenge.viewmodel.MainViewModel
import com.squareup.picasso.Picasso

class PontosFavoritosAdapter: RecyclerView.Adapter<PontosFavoritosAdapter.PontosFavoritosViewHolder>(),
    AdapterItemsContract{
    private var itens: MutableList<PontoTuristico>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PontosFavoritosViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = PontoItemBinding.inflate(inflater, parent, false)
        return PontosFavoritosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PontosFavoritosViewHolder, position: Int) {
        val item = itens!![position]
        holder.bind(item)
    }

//    override fun onBindViewHolder(holder: PontosTuristicosViewHolder, position: Int) {
//        val item = itens!![position]
//        holder.bind(item)
//    }

    override fun getItemCount() = if (itens != null) itens!!.size else 0

    @Suppress("UNCHECKED_CAST")
    override fun replaceItens(list: List<*>) {
        itens = list as MutableList<PontoTuristico>
        notifyDataSetChanged()
    }

    class PontosFavoritosViewHolder(val binding: PontoItemBinding):
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

                bottomSheet = PontoTuristicoBottomSheet.newInstance(ponto)
                bottomSheet.show(App.activity!!.supportFragmentManager, bottomSheet.tag)
            }
        }
    }
}