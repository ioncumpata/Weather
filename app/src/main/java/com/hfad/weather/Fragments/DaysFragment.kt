package com.hfad.weather.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.weather.MainViewModel
import com.hfad.weather.R
import com.hfad.weather.adapters.WeatherAdapter
import com.hfad.weather.databinding.FragmentDaysBinding

class DaysFragment : Fragment() {

   private lateinit var adapter: WeatherAdapter
    private lateinit var binding: FragmentDaysBinding
    private val viewModel:MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        viewModel.liveDataList.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }
    }

    private fun init() = with(binding) {
        adapter = WeatherAdapter()
        rcViewDays.layoutManager = LinearLayoutManager(activity)
        rcViewDays.adapter = adapter
    }

    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()
    }
}

