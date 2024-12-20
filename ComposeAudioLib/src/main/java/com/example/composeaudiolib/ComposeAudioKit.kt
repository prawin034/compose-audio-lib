package com.example.composeaudiolib

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import java.util.concurrent.TimeUnit


sealed class AudioSource() {
    data class Raw(val resourceId: Int) : AudioSource()
    data class Url(val url: String) : AudioSource()
    data class LocalFile(val filePath: String) : AudioSource()
}



@Composable
fun ComposeAudioKit(
    audio : AudioSource,
    modifier: Modifier = Modifier
){

    val composeAudioPlayerViewModel : ComposeAudioPlayerViewModel = viewModel()
    val isPlaying  by composeAudioPlayerViewModel.isMediaPlaying.observeAsState()
    val currentPosition by composeAudioPlayerViewModel.currentPositionMedia.observeAsState(0f)
    val mediaDuration by composeAudioPlayerViewModel.mediaDuration.observeAsState(0)
    val isFullScreen = remember { mutableStateOf(false) }


    val context = LocalContext.current


    LaunchedEffect(Unit) {

        composeAudioPlayerViewModel.initializePlayer(
            audioUrl = audio, context
        )

    }



    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        Card(
            modifier = Modifier
                .clickable {
                    isFullScreen.value = true
                }
                .fillMaxWidth()
                .padding(10.dp)
                .height(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 1 -> first row
                DescriptionContent {}

                // 2 -> second row
                IconButton(
                    onClick = {

                        if(isPlaying == true) {
                            composeAudioPlayerViewModel.pauseMusic()
                        }
                        else {
                            composeAudioPlayerViewModel.playMusic()
                        }

//                        val action = if (playing.value) {
//                            ComposeAudioKitService.ACTION.PAUSE
//                        } else {
//                            ComposeAudioKitService.ACTION.PLAY
//                        }
//
//                        val intent = Intent(context, ComposeAudioKitService::class.java).apply {
//                            this.action = action.toString()
//                            putExtra("AUDIO_FILE", R.raw.mockingbird)
//
//
//                        }
//                        context.startService(intent)
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            id =  if (isPlaying == true) R.drawable.baseline_pause_24
                            else R.drawable.baseline_play_arrow_24
                        ),
                        contentDescription = "",
                        tint = Color.White
                    )
                }

            }

        }

    }

    AnimatedVisibility(
        visible = isFullScreen.value,
        enter = slideInVertically(
            initialOffsetY = {it},
            animationSpec = tween(500, easing = EaseIn)
        ) ,
        exit = slideOutVertically(
            targetOffsetY = {-it},
            animationSpec = tween(300, easing = EaseOut)
        )
    ) {
        Column {

            Popup(
                properties = PopupProperties(
                    dismissOnBackPress = true,
                    clippingEnabled = true
                ),
                onDismissRequest = {
                    isFullScreen.value = false
                }
            ) {
                FullScreenComposeAudioKit(
                    currentPosition,
                    mediaDuration,
                    composeAudioPlayerViewModel,
                    isPlaying
                )
            }
        }
    }











}


@Composable
fun DescriptionContent(contentImage:@Composable () -> Unit){

    Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {

        Box(
            modifier = Modifier
                .size(60.dp)

                .clip(shape = RoundedCornerShape(17.dp))
                .background(color = Color.White),
            content = {
                contentImage.invoke()
            }
        )


        Column(modifier = Modifier
            .fillMaxHeight()
            .padding(bottom = 6.dp)
            .padding(horizontal = 7.dp), verticalArrangement = Arrangement.SpaceEvenly) {
            Text(
                "Sokkupodi",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Start,
                style = TextStyle(fontFamily = FontFamily.SansSerif)
            )
            Text(
                "Muppozhundum un Karpanaigal",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Start,
                style = TextStyle(fontFamily = FontFamily.SansSerif)
            )
        }



    }




}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenComposeAudioKit(
    currentPosition: Float?,
    mediaDuration: Int,
    composeAudioPlayerViewModel: ComposeAudioPlayerViewModel,
    isPlaying: Boolean?
) {

    val currentDurationRunning = (currentPosition!! * mediaDuration).toInt()
    LaunchedEffect(currentPosition) {

        Log.d("MediaPlayerStartDuration", currentDurationRunning.formatDuration())

    }

    Scaffold(
        modifier = Modifier
            .padding(10.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                title = { Text("ComposeAudioKit", fontSize = 17.5.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Cursive, color = Color.White, letterSpacing = 4.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // ComposeAudioKitRepository.updateFullScreen(false)
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "", tint = Color.White)
                    }
                }
            )


        },
        bottomBar = {},
        content = { paddinvalues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .imePadding()
                    .padding(paddinvalues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                        .background(color = Color.White),
                    contentAlignment = Alignment.Center
                ) {

                    if(isPlaying == true) {
                        AudioAnimation(
                            modifier = Modifier
                                .size(150.dp)
                                .align(Alignment.Center)
                        )
                    }
                    else {
                        Text("Paused", fontSize = 26.5.sp, color = Color.Black, fontFamily = FontFamily.Cursive, letterSpacing = 3.sp)
                    }
                }


                Spacer(modifier = Modifier.height(30.dp))


                CustomAudioSlider(
                    currentPosition = currentPosition,
                    onValueChange = { newValue ->
                        composeAudioPlayerViewModel.sliderPosition(newValue)
                    }
                )
                Spacer(modifier = Modifier.height(0.dp))


                Row(modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = -15.dp)
                    .padding(2.dp)
                    .padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {

                    Text(
                        currentDurationRunning.formatDuration(),
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Text(
                        mediaDuration.formatDuration(),
                        fontSize = 13.sp,
                        color = Color.White
                    )

                }




                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        modifier = Modifier.size(60.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White
                        ),
                        onClick = {}
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = Color.Black
                        )

                    }


                    IconButton(
                        modifier = Modifier.size(100.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White
                        ),
                        onClick = {
                            if(isPlaying == true) {
                                composeAudioPlayerViewModel.pauseMusic()
                            }
                            else {
                                composeAudioPlayerViewModel.playMusic()
                            }
//                            val action = if (playing.value) {
//                                ComposeAudioKitService.ACTION.PAUSE
//                            } else {
//                                ComposeAudioKitService.ACTION.PLAY
//                            }
//
//                            val intent = Intent(context, ComposeAudioKitService::class.java).apply {
//                                this.action = action.toString()
//                                putExtra("AUDIO_FILE", R.raw.mockingbird)
//
//                            }
//                            context.startService(intent)
                        }
                    ) {

                        Icon(
                            painter = painterResource(
                                id =  if (isPlaying == true) R.drawable.baseline_pause_24
                                else R.drawable.baseline_play_arrow_24
                            ),
                            contentDescription = "",
                            tint = Color.Black
                        )
                    }

                    IconButton(
                        modifier = Modifier.size(60.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White
                        ),
                        onClick = {
                            //composeAudioPlayerViewModel.increaseDuration()
//                            val increaseDurationBy10Seconds = currentDurationRunning + 10
//                            composeAudioPlayerViewModel.increaseDuration(increaseDurationBy10Seconds)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "",
                            tint = Color.Black
                        )
                    }


                }
            }
        }
    )







}






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAudioSlider(
    currentPosition: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = currentPosition,
        onValueChange =  {
            onValueChange.invoke(it)
        },
        modifier = modifier.padding(horizontal = 16.dp),
        colors = SliderDefaults.colors(
            thumbColor = Color.Black,
            activeTrackColor = Color.Green,
            inactiveTrackColor = Color.Gray
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(29.dp)
                    .background(Color.Blue, CircleShape)
            )
        },
        track = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp))
            ) {
                // Custom track rendering (optional)
            }
        }
    )
}



@Composable
fun AudioAnimation(modifier: Modifier = Modifier) {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.audio
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )


    LottieAnimation(
        composition = preloaderLottieComposition,
        progress = preloaderProgress,
        modifier = modifier

    )
}

@SuppressLint("DefaultLocale")
fun Float?.formatDuration(): String {
    val duration = this ?: 0f
    val hours = TimeUnit.MILLISECONDS.toHours(duration.toLong())
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong()) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
@SuppressLint("DefaultLocale")
fun Int?.formatDuration(): String {
    val duration = this ?: 0
    val hours = TimeUnit.MILLISECONDS.toHours(duration.toLong())
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong()) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}