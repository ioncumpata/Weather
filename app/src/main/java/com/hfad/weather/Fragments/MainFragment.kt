package com.hfad.weather.Fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.weather.DialogManager
import com.hfad.weather.MainViewModel
import com.hfad.weather.adapters.VpAdapter
import com.hfad.weather.adapters.WeatherModel
import com.hfad.weather.databinding.FragmentMainBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject

const val API_KEY = "e156a17f382c43fcae935131231008"


class MainFragment : Fragment() {
    private lateinit var fLocationClient:FusedLocationProviderClient
    private val frag = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private val tabList = listOf(
        "Hours", "Days"
    )

    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var pLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        viewInit()
        updateCurrentCard()


    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun viewInit() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val viewPager = VpAdapter(activity as FragmentActivity, frag)
        vp.adapter = viewPager

        TabLayoutMediator(tabLayout, vp) { tab, position ->
            tab.text = tabList[position]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()

        }
        ibSearche.setOnClickListener {

            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }
            })
        }
    }

    private fun checkLocation(){
        if(isLocationEnabled()){
            getLocation()
        }else{
            DialogManager.locationSettingsDialog(requireContext(),object:DialogManager.Listener{
                override fun onClick(name:String?) {
                   startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })


        }

    }
    private fun isLocationEnabled():Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation(){

        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,ct.token)
            .addOnCompleteListener {
              requestWeatherData("${it.result.latitude},${it.result.longitude}")
            }
    }

    private fun updateCurrentCard() = with(binding) {
        viewModel.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMin = "${it.maxTemp}C/${it.minTemp}C"
            tvData.text = it.time
            tvCity.text = it.city
            tvCondition.text = it.condition
            tvCurrentTemp.text = it.currentTemp
            tvMaxMin.text = maxMin
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)

        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permision is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city
        "&days=3" +
                "&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseWeatherData(result)
            },
            { error ->
                Log.d("My log", "Error = $error")
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])

    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")

        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c"),
                day.getJSONObject("day").getString("mintemp_c"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        viewModel.liveDataList.value = list

        return list

    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            weatherItem.hours

        )
        viewModel.liveDataCurrent.value = item
    }

    companion object {

        @JvmStatic
        fun newInstance() = MainFragment()
    }
}