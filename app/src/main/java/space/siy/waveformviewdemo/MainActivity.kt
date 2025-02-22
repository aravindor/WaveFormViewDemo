package space.siy.waveformviewdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import space.siy.waveformview.WaveFormData
import space.siy.waveformview.WaveFormView
import space.siy.waveformviewdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Open From Assets Folder
        val afd = assets.openFd("jazz_in_paris.mp3")


        //Build WaveFormData


        setButtonClicks()
        initPermission()
    }

    private fun initPermission() {
        try {
            val mPermission = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            mPermission[0]
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            applicationContext,
                            mPermission[1]
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            applicationContext,
                            mPermission[2]
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            applicationContext,
                            mPermission[3]
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            applicationContext,
                            mPermission[4]
                    ) != PackageManager.PERMISSION_GRANTED

            ) {
                ActivityCompat.requestPermissions(this, mPermission, 1)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setButtonClicks() {
        binding.file.setOnClickListener {
            fileReq.launch("video/*")
        }
        binding.next.setOnClickListener {
            startActivity(Intent(this,FixedWaveformDemoActivity::class.java))
        }
    }

  private val fileReq = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
    Log.d(TAG, "uri: ${uri.path}")
      setWaveForm(uri)
  }

    private fun setWaveForm(uri:Uri){
        WaveFormData.Factory(applicationContext,uri,null)
                .build(object : WaveFormData.Factory.Callback {
                    //When Complete, you can receive data and set to the view
                    override fun onComplete(waveFormData: WaveFormData) {
                        progressBar.visibility = View.GONE

                        waveFormView.data = waveFormData

                        //UI setup
                        seekBar1.progress = (waveFormView.secPerBlock * 100f).toInt()
                        seekBar2.progress = (waveFormView.blockWidth).toInt()
                        seekBar3.progress = (waveFormView.topBlockScale * 100f).toInt()
                        seekBar4.progress = (waveFormView.bottomBlockScale * 100f).toInt()

                        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    progress: Int,
                                    fromUser: Boolean
                            ) {
                                waveFormView.secPerBlock = seekBar.progress / 100f
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        })
                        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    progress: Int,
                                    fromUser: Boolean
                            ) {
                                waveFormView.blockWidth = seekBar.progress.toFloat()
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        })
                        seekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    progress: Int,
                                    fromUser: Boolean
                            ) {
                                waveFormView.topBlockScale = seekBar.progress / 100f
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        })
                        seekBar4.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                    seekBar: SeekBar,
                                    progress: Int,
                                    fromUser: Boolean
                            ) {
                                waveFormView.bottomBlockScale = seekBar.progress / 100f
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        })

                        //Initialize MediaPlayer
                        val player = MediaPlayer()
                        player.setDataSource(applicationContext,uri,null)
                        player.prepare()
                        player.start()

                        //Synchronize with MediaPlayer using WaveFormView.Callback
                        waveFormView.callback = object : WaveFormView.Callback {
                            override fun onPlayPause() {
                                if (player.isPlaying)
                                    player.pause()
                                else
                                    player.start()
                            }

                            override fun onSeek(pos: Long) {
                                player.seekTo(pos.toInt())
                            }
                        }

                        //You have to notify current position to the view
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                waveFormView.position = player.currentPosition.toLong()
                                Handler().postDelayed(this, 20)
                            }
                        }, 20)

                    }
                })
    }

}
