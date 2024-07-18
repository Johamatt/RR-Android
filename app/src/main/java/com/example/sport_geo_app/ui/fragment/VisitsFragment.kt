package com.example.sport_geo_app.ui.fragment
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.CoordinatesPoint
import com.example.sport_geo_app.data.model.PlaceModel
import com.example.sport_geo_app.data.model.Visit
import com.example.sport_geo_app.data.network.NetworkService
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil


import java.text.SimpleDateFormat
import java.util.*

import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException


class VisitsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var visitsAdapter: VisitsAdapter
    private lateinit var encryptedSharedPreferences: SharedPreferences
    private lateinit var networkService: NetworkService
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_visits, container, false)

        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(requireContext())
        networkService = NetworkService(requireContext())


        val userId = encryptedSharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            fetchVisitsData(userId, view)
        } else {
            showCustomToast("Unknown error occurred")
        }

        return view
    }

    private fun fetchVisitsData(userId: Int, view: View) {
        val networkService = NetworkService(requireContext())
        networkService.getVisits(userId) { response, error ->
            when {
                error != null -> showCustomToast(error.message)
                response != null -> {
                    val visitsData = parseVisitsResponse(response)
                    initializeRecyclerView(view, visitsData)
                }
                else -> showCustomToast("Unknown error occurred")
            }
        }
    }
    private fun showCustomToast(message: String?) {
        val layoutInflater = layoutInflater
        val layout: View = layoutInflater.inflate(
            R.layout.custom_toast, requireView().findViewById(R.id.custom_toast_container)
        )
        val textView: TextView = layout.findViewById(R.id.custom_toast_message)
        textView.text = message ?: "An unknown error occurred"
        with(Toast(requireContext())) {
            duration = Toast.LENGTH_LONG
            setView(layout)
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }



    private fun parseVisitsResponse(responseBody: ResponseBody): List<Visit> {
        val visits = mutableListOf<Visit>()
        val jsonArray = JSONArray(responseBody.string())


        for (i in 0 until jsonArray.length()) {
            Log.d("VisitsFragment", jsonArray.toString(i))

            val jsonObject = jsonArray.getJSONObject(i)
            val placeObject = jsonObject.getJSONObject("place")
            Log.d("VisitsFragment",placeObject.getJSONObject("pointCoordinates").toString())
            val coordinatesArray = placeObject.getJSONObject("pointCoordinates").getJSONArray("coordinates")

            val coordinates = CoordinatesPoint(
                type = placeObject.getJSONObject("pointCoordinates").getString("type"),
                coordinates = listOf(coordinatesArray.getDouble(0), coordinatesArray.getDouble(1))
            )


            //TODO requestModels
            val place = PlaceModel(
                placeId = placeObject.getString("placeId"),
                nameFi = placeObject.getString("nameFi"),
                country = placeObject.getString("country"),
                lisätieto = placeObject.getString("lisätieto"),
                pointCoordinates = coordinates,
                liikuntapaikkaTyyppi = placeObject.getString("liikuntapaikkaTyyppi"),
                liikuntapaikkatyypinAlaryhmä = placeObject.getString("liikuntapaikkatyypinAlaryhmä"),
                liikuntapaikkatyypinPääryhmä = placeObject.getString("liikuntapaikkatyypinPääryhmä"),
                postinumero = placeObject.getString("postinumero"),
                katuosoite = placeObject.getString("katuosoite"),
                kentänLeveysM = null,
                kentänPituusM = null,
                kunta = null,
                kuntaosa = null,
                liikuntapintaalaM2 = null,
                linestringCoordinates = null,
                maakunta = null,
                markkinointinimi = null,
                muokattuViimeksi = null,
                omistaja = null,
                peruskorjausvuodet = null,
                pintamateriaali = null,
                pintamateriaaliLisätieto = null,
                polygonCoordinates = null,
                postitoimipaikka = null,
                puhelinnumero = null,
                sähköposti = null,
                www = null,
                aviAlue = null
            )

            val visit = Visit(
                visitId = jsonObject.getInt("visit_id"),
                createdAt = jsonObject.getString("created_at"),
                place = place
            )

            visits.add(visit)
        }

        return visits
    }

    private fun initializeRecyclerView(view: View, visitsData: List<Visit>) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        visitsAdapter = VisitsAdapter(visitsData)
        recyclerView.adapter = visitsAdapter
    }
}



class VisitsAdapter(private val visits: List<Visit>) : RecyclerView.Adapter<VisitsAdapter.VisitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_visit, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]

        holder.visitIdTextView.text = visit.visitId.toString()
        holder.createdAtTextView.text = formatDate(visit.createdAt)
        holder.nameTextView.text = visit.place.nameFi
        holder.countryTextView.text = visit.place.country
        holder.descriptionTextView.text = visit.place.lisätieto
        holder.coordinatesTextView.text = visit.place.pointCoordinates.toString()
    }

    override fun getItemCount(): Int {
        return visits.size
    }

    inner class VisitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val visitIdTextView: TextView = itemView.findViewById(R.id.visitIdTextView)
        val createdAtTextView: TextView = itemView.findViewById(R.id.createdAtTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val countryTextView: TextView = itemView.findViewById(R.id.countryTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val coordinatesTextView: TextView = itemView.findViewById(R.id.coordinatesTextView)
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        try {
            val date = inputFormat.parse(dateString)
            return outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
    }
}


