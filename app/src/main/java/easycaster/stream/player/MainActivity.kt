package easycaster.stream.player

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.nodemedia.NodePlayer
import easycaster.stream.player.databinding.ActivityMainBinding
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IVLCVout


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapterView: ArrayAdapter<String>
    private var np: NodePlayer? = null
    private val PREFS_NAME = "MyPrefs"
    private val RECENT_VALUES_KEY = "recentValues"

    var new = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.imageView.setImageResource(R.drawable.baseline_pause_24)

        np = NodePlayer(this, "")

        np!!.attachView(binding.videoView)

        np!!.setBufferTime(3400)
        np!!.start("http://145.239.88.156:63165")

        for(i in 1 .. 10){
            Toast.makeText(this, np!!.bufferPercentage.toString(), Toast.LENGTH_LONG).show()
        }

        binding.imageView.setOnClickListener {
            playAudio()
        }

        val recentValues = loadRecentValues()

        // Set up the spinner with the recent values
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)

        adapter.addAll(recentValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterView = adapter
        binding.spinner.adapter = adapterView

        binding.videoView.setOnClickListener {
            binding.spinner.isActivated = false
        }


        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedValue = parent?.getItemAtPosition(position).toString()
                binding.editText.setText(selectedValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        setContentView(binding.root)
    }

    private fun playAudio(){
        if(binding.editText.text?.isNotEmpty() == true){

            np!!.stop()

            np!!.detachView()

//            np!!.attachView(binding.videoView)

            if(binding.editText.text.toString().contains("srt://")){
                val str = binding.editText.text.toString().replace("srt://", "http://")
                np!!.start(str)
            } else {
                np!!.start(binding.editText.text.toString())
            }
            saveRecentValue(binding.editText.text.toString())
            adapterView.clear()

            adapterView.addAll(loadRecentValues())
            adapterView.notifyDataSetChanged()
            binding.editText.text!!.clear()
        } else {
            if (!new) {
                np!!.pause(true)
                new = true
                np!!.setScaleMode(0)
                binding.imageView.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                np!!.pause(false)
                new = false
                binding.imageView.setImageResource(R.drawable.baseline_pause_24)
            }
        }
    }

    private fun loadRecentValues(): Set<String> {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(RECENT_VALUES_KEY, HashSet<String>()) ?: HashSet()
    }

    private fun saveRecentValue(value: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val recentValues = loadRecentValues().toMutableSet()
        recentValues.add(value)

        // Limit the number of recent values if needed
        if (recentValues.size > 5) {
            recentValues.remove(recentValues.first())
        }

        prefs.edit().putStringSet(RECENT_VALUES_KEY, recentValues).apply()
    }

}