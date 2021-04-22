package space.siy.waveformviewdemo

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.btnPlayPause
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.btnSpeakerToggle
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.btnStop
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.progressBar1
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.progressBar2
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.tvWaveFormView2Duration
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.waveFormView1
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.waveFormView2
import space.siy.waveformview.FixedWaveFormPlayer
import space.siy.waveformview.FixedWaveFormPlayer.Callback
import space.siy.waveformviewdemo.databinding.ActivityFittedWaveformDemoBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class FixedWaveformDemoActivity : AppCompatActivity() {

  private var waveFormPlayer1: FixedWaveFormPlayer? = null
  private var waveFormPlayer2: FixedWaveFormPlayer? = null
  private lateinit var binding:ActivityFittedWaveformDemoBinding
  private  val TAG = "FixedWaveformDemo"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFittedWaveformDemoBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setButtonClicks()
  }

  private fun setButtonClicks() {
    binding.file.setOnClickListener {
      fileReq.launch("video/*")
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    waveFormPlayer1?.dispose()
    waveFormPlayer2?.dispose()
  }

  private val fileReq = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
    Log.d(TAG, "uri: ${uri.path}")
    setWaveForm(uri)
  }



  private fun setWaveForm(uri:Uri){
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager;

    try {
      waveFormPlayer1 = FixedWaveFormPlayer(uri, applicationContext)
      waveFormPlayer1?.snapToStartAtCompletion = false
      progressBar1.visibility = View.VISIBLE

      waveFormPlayer2 = FixedWaveFormPlayer(uri, applicationContext)
      progressBar2.visibility = View.VISIBLE
      waveFormPlayer2?.loadInto(waveFormView2, object : Callback {
        override fun onLoadingComplete() {
          progressBar2.visibility = View.GONE
          tvWaveFormView2Duration.text = waveFormPlayer2?.duration.toString()
        }

        override fun onError() {
          btnPlayPause.text = "play"
          progressBar2.visibility = View.GONE
        }

        override fun onPlay() {
          btnPlayPause.text = "pause"
        }

        override fun onPause() {
          btnPlayPause.text = "play"
        }

        override fun onStop() {
          btnPlayPause.text = "play"
        }
      })
      btnPlayPause.setOnClickListener {
        if (waveFormPlayer2?.isPlaying() == true) {
          waveFormPlayer2?.pause()
        } else {
          fun randomColor() = Random.nextInt(0, 256)
          waveFormView2.blockColor = Color.rgb(randomColor(), randomColor(), randomColor())
          waveFormView2.blockColorPlayed = Color.rgb(randomColor(), randomColor(), randomColor())
          waveFormPlayer2?.play()
        }
      }
      btnStop.setOnClickListener {
        btnPlayPause.text = "play"
        waveFormPlayer2?.stop()
      }
      btnSpeakerToggle.setOnClickListener {
        waveFormPlayer2?.toggleSpeakerphone(!audioManager.isSpeakerphoneOn)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
