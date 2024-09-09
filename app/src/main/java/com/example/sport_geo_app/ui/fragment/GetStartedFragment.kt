package com.example.sport_geo_app.ui.fragment

import android.Manifest
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.sport_geo_app.R
import com.example.sport_geo_app.di.Toaster
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GetStartedFragment : Fragment() {

    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences

    private lateinit var buttonHeight: MaterialButton
    private lateinit var buttonWeight: MaterialButton
    private lateinit var buttonGetStarted: MaterialButton
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    @Inject lateinit var toaster: Toaster

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_getstarted, container, false)
        initializeViews(view)
        initializeListeners()
        return view
    }

    private fun initializeViews(view: View) {
        buttonHeight = view.findViewById(R.id.buttonHeight)
        buttonWeight = view.findViewById(R.id.buttonWeight)
        buttonGetStarted = view.findViewById(R.id.buttonGetStarted)
    }

    private fun initializeListeners() {
        buttonHeight.setOnClickListener {
            showNumberPickerDialog("Height", "cm", buttonHeight, 120, 220, 170)
        }

        buttonWeight.setOnClickListener {
            showNumberPickerDialog("Weight", "kg", buttonWeight, 15, 350, 75)
        }

        buttonGetStarted.setOnClickListener {
            requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val allPermissionsGranted = permissions.values.all { it }
                if (allPermissionsGranted) {
                   //TODO
                } else {
                    toaster.showToast("Location permission is required to proceed")
                }
            }

            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }



    private fun showNumberPickerDialog(
        title: String, unit: String, button: MaterialButton, minValue: Int, maxValue: Int, defaultValue: Int
    ) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_number_picker, null)

        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPicker)
        val unitTextView = dialogView.findViewById<TextView>(R.id.unitTextView)
        val saveButton = dialogView.findViewById<Button>(R.id.save_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)


        numberPicker.minValue = minValue
        numberPicker.maxValue = maxValue
        numberPicker.wrapSelectorWheel = true
        numberPicker.value = defaultValue
        unitTextView.text = unit

        builder.setView(dialogView)
            .setTitle(title)

        val dialog = builder.create()


        dialog.setOnShowListener {
            saveButton.setOnClickListener {
                val selectedValue = numberPicker.value
                button.text = "$title: $selectedValue ${unitTextView.text}"
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }



    companion object {
        private const val TAG = "GetStartedFragment"
    }

}
