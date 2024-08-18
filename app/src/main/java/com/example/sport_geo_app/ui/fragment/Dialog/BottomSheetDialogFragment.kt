package com.example.sport_geo_app.ui.fragment.Dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.sport_geo_app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


    class BottomSheetFragment : BottomSheetDialogFragment() {

        companion object {
            private const val ARG_NAME = "arg_name"
            private const val ARG_ADDRESS = "arg_address"
            private const val ARG_TYPE = "arg_type"

            fun newInstance(name: String, address: String, type: String): BottomSheetFragment {
                val fragment = BottomSheetFragment()
                val args = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_ADDRESS, address)
                    putString(ARG_TYPE, type)
                }
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.bottom_sheet_layout, container, false).apply {
                val name = arguments?.getString(ARG_NAME)
                val address = arguments?.getString(ARG_ADDRESS)
                val type = arguments?.getString(ARG_TYPE)

                findViewById<TextView>(R.id.point_name).text = name
                findViewById<TextView>(R.id.point_address).text = address
                findViewById<TextView>(R.id.point_type).text = type
            }
        }
    }
