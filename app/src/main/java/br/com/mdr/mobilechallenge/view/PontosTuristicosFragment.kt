package br.com.mdr.mobilechallenge.view


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import br.com.mdr.mobilechallenge.App
import br.com.mdr.mobilechallenge.adapter.PontosFavoritosAdapter

import br.com.mdr.mobilechallenge.databinding.PontosTuristicosFragmentBinding
import br.com.mdr.mobilechallenge.viewmodel.PontosTuristicosViewModel

class PontosTuristicosFragment : Fragment() {
    private lateinit var viewModel: PontosTuristicosViewModel
    private lateinit var mainBinding: PontosTuristicosFragmentBinding
    lateinit var adapter: PontosFavoritosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = PontosTuristicosViewModel()
        mainBinding = PontosTuristicosFragmentBinding.inflate(inflater)
        adapter = PontosFavoritosAdapter()
        mainBinding.recyclerPontos.adapter = adapter
        return mainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.buscaFavoritos()
        App.view = view!!
    }
    private fun fetchObservers() {
        viewModel.pontosFavoritos.observe(viewLifecycleOwner, Observer {
            if (it.size > 0)
                adapter.replaceItens(it)
        })
    }
}
