package com.example.sport_geo_app.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.sport_geo_app.R

class InfoFragment : DialogFragment() {

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_ADDRESS = "address"
        private const val ARG_TYPE = "type"

        fun newInstance(name: String, address: String, type: String): InfoFragment {
            val fragment = InfoFragment()
            val args = Bundle().apply {
                putString(ARG_NAME, name)
                putString(ARG_ADDRESS, address)
                putString(ARG_TYPE, type)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_point_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString(ARG_NAME) ?: ""
        val address = arguments?.getString(ARG_ADDRESS) ?: ""
        val type = arguments?.getString(ARG_TYPE) ?: ""

        view.findViewById<TextView>(R.id.info_name).text = name
        view.findViewById<TextView>(R.id.info_address).text = address
        view.findViewById<TextView>(R.id.info_type).text = type

        view.findViewById<Button>(R.id.close_button).setOnClickListener {
            dismiss()
        }
    }
}


