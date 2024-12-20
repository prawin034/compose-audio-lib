package com.example.composeaudiolib

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ComposeAudioPlayerViewModel : ViewModel() {




    init {
        Log.d("ComposeAudioPlayerViewModel","Initialized ViewModel")
    }
    private var mediaPlayer : MediaPlayer? = null

    private val _isMediaPlaying  = MutableLiveData<Boolean?>(false)
    val isMediaPlaying : LiveData<Boolean?> get() = _isMediaPlaying

    private var updatePosition = true

    fun updateMediaPlaying(playing:Boolean) {
        _isMediaPlaying.value = playing
    }


    private val _currentPositionMedia = MutableLiveData<Float?>(0f)
    val currentPositionMedia : LiveData<Float?> get() = _currentPositionMedia



    private var _mediaStartDuration = MutableLiveData<Int>(0)
    val mediaStartDuration : LiveData<Int> get() = _mediaStartDuration


    private var _mediaDuration = MutableLiveData<Int>(0)
    val mediaDuration : LiveData<Int> get() = _mediaDuration

    fun updateCurrentPositionMedia(value:Float){
        _currentPositionMedia.postValue(value)
    }

    // 1 -> Initialize player, type audio - raw ,url ,local file
    fun initializePlayer(audioUrl:AudioSource,context: Context) {

        if(mediaPlayer !=null) return

        mediaPlayer = MediaPlayer().apply {
            when(audioUrl){
                is AudioSource.Raw ->  {
                    setRawResourceId(audioUrl.resourceId,context)
                    prepare()
                }
                is AudioSource.Url -> {
                    setDataSourceAudio(audioUrl.url)
                    prepare()
                }
                is AudioSource.LocalFile -> TODO()
            }

            setOnCompletionListener { mediaPlayer ->
                stopMusic()
                //releasePlayer()
            }
            setOnPreparedListener { mediaPlayer ->
                viewModelScope.launch {
                    while (updatePosition) {
                        if(mediaPlayer.duration > 0) {
                            updateCurrentPositionMedia(
                                mediaPlayer.currentPosition.toFloat()  / mediaPlayer.duration.toFloat()
                            )
                            _mediaDuration.value = mediaPlayer.duration
                        }
                        delay(1000)
                    }
                }

            }
        }
    }


    @SuppressLint("SuspiciousIndentation")
    private fun MediaPlayer.setRawResourceId(rawRes:Int, context: Context) {
        val fd =   context.resources.openRawResourceFd(rawRes) ?: throw IllegalArgumentException("Resources not found")
        setDataSource(fd.fileDescriptor,fd.startOffset,fd.length)
        fd.close()
    }

    private fun MediaPlayer.setDataSourceAudio(url:String) {
        setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        setDataSource(url)
    }

    //2 ->  playMusic()

    fun playMusic() {
        mediaPlayer?.let { player ->

            if(isMediaPlaying.value != true) {
                player.start()
                updateMediaPlaying(true)
                Log.d("ComposeAudioKit", "MediaPlayer started playing.")
            }

        } ?: Log.d("ComposeAudioKit","MediaPlayer is null")
    }

    // 3 -> pauseMusic()
    fun pauseMusic() {
        mediaPlayer?.let { player ->
            if(isMediaPlaying.value == true) {
                player.pause()
                updateMediaPlaying(false)
                Log.d("ComposeAudioKit","MediaPlayer paused")

            }
        } ?: Log.d("ComposeAudioKit","MediaPlayer is null")
    }

    //4 -> stopMusic()
    private fun stopMusic() {
        mediaPlayer?.let { player ->
            player.reset()
            player.stop()
            updateMediaPlaying(false)
            updateCurrentPositionMedia(0f)
            _mediaStartDuration.value = 0
            _mediaDuration.value = player.duration

            Log.d("ComposeAudioKit","MediaPlayer Stopped")

        } ?: Log.d("ComposeAudioKit","MediaPlayer is null")
    }

    fun increaseDuration() {
        mediaPlayer?.let { player ->
            if(isMediaPlaying.value == true) {

                val currentPosition = (player.currentPosition * mediaDuration.value!! + 10)
                player.seekTo(currentPosition)
                updateCurrentPositionMedia(player.currentPosition.toFloat())
                _mediaStartDuration.value = currentPosition


            }
        }
    }


    fun decreaseDuration() {}

    fun sliderPosition(position:Float) {
        val sliderPosition = (position * mediaPlayer?.duration!!).toInt()
        mediaPlayer?.seekTo(sliderPosition)
        updateCurrentPositionMedia(position)
        _mediaStartDuration.value = sliderPosition
    }

    //5 -> onCleared all

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopMusic()
        releasePlayer()
        Log.d("ComposeAudioPlayerViewModel","Destroyed ViewModel")

    }
}