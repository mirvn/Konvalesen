package com.android.konvalesen.view.onRequest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentOnRequestBinding

class OnRequestFragment : Fragment() {
    private lateinit var binding: FragmentOnRequestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOnRequestBinding.inflate(inflater,container,false)
        return binding.root
    }

}