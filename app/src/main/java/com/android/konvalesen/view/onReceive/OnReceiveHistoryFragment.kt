package com.android.konvalesen.view.onReceive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentOnReceiveHistoryBinding

class OnReceiveHistoryFragment : Fragment() {
    private lateinit var binding: FragmentOnReceiveHistoryBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOnReceiveHistoryBinding.inflate(inflater,container,false)
        return binding.root
    }
}