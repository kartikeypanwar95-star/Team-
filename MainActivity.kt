package com.lucifer.music

import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlin.math.abs

private val Purple = Color(0xFFA970FF)
private val Background = Color(0xFF000000)
private val SurfaceDark = Color(0xFF111111)
private val TextMuted = Color(0xFF999999)

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private var equalizer: Equalizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        setContent {
            LuciferApp(
                onPlayUri = { uri -> playUri(uri) },
                onPlayUrl = { url -> playUrl(url) },
                onToggle = { togglePlayback() },
                onStop = { player?.stop() },
                onEqChange = { index, value -> setEqBand(index, value) },
                isPlaying = player?.isPlaying == true
            )
        }
    }

    private fun playUri(uri: Uri) {
        player?.setMediaItem(MediaItem.fromUri(uri))
        player?.prepare()
        player?.play()
        attachEqualizer()
    }

    private fun playUrl(url: String) {
        if (url.isBlank()) return
        player?.setMediaItem(MediaItem.fromUri(url.trim()))
        player?.prepare()
        player?.play()
        attachEqualizer()
    }

    private fun togglePlayback() {
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    private fun attachEqualizer() {
        try {
            equalizer?.release()
            val session = player?.audioSessionId ?: 0
            if (session != 0) {
                equalizer = Equalizer(0, session).apply { enabled = true }
            }
        } catch (_: Exception) {
            equalizer = null
        }
    }

    private fun setEqBand(index: Int, value: Float) {
        try {
            val eq = equalizer ?: return
            val count = eq.numberOfBands.toInt()
            if (count <= 0) return
            val targetHz = floatArrayOf(31f, 62f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)[index]
            var nearest = 0
            var best = Float.MAX_VALUE
            for (i in 0 until count) {
                val range = eq.getBandFreqRange(i.toShort())
                val center = (range[0] + range[1]) / 2f
                val distance = abs(center - targetHz)
                if (distance < best) { best = distance; nearest = i }
            }
            val range = eq.bandLevelRange
            val min = range[0].toFloat()
            val max = range[1].toFloat()
            val level = (value / 12f * max.coerceAtMost(abs(min))).coerceIn(min, max).toInt()
            eq.setBandLevel(nearest.toShort(), level.toShort())
        } catch (_: Exception) { }
    }

    override fun onDestroy() {
        equalizer?.release()
        player?.release()
        player = null
        super.onDestroy()
    }
}

@Composable
private fun LuciferApp(
    onPlayUri: (Uri) -> Unit,
    onPlayUrl: (String) -> Unit,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    onEqChange: (Int, Float) -> Unit,
    isPlaying: Boolean
) {
    var tab by remember { mutableStateOf(0) }
    var serverUrl by remember { mutableStateOf("") }
    var selectedName by remember { mutableStateOf("Choose a song") }
    val bands = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    var levels by remember { mutableStateOf(List(10) { 0f }) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedName = uri.lastPathSegment?.substringAfterLast('/') ?: "Local audio"
            onPlayUri(uri)
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            background = Background,
            surface = SurfaceDark,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF080808), contentColor = Purple) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.LibraryMusic, null) }, label = { Text("Library") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Equalizer, null) }, label = { Text("Equalizer") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3 }, icon = { Icon(Icons.Default.Cloud, null) }, label = { Text("Sources") })
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("LUCIFER", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            Text("YOUR SOUND. YOUR CONTROL.", color = Purple, fontSize = 9.sp, letterSpacing = 2.sp)
                        }
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Purple, modifier = Modifier.size(30.dp))
                    }
                }

                when (tab) {
                    0 -> {
                        item { SectionTitle("WELCOME BACK") }
                        item {
                            DarkCard {
                                Text("Music without limits.", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Text("Play music from your phone or your own server.", color = TextMuted)
                                Button(onClick = { picker.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.FolderOpen, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Choose music")
                                }
                            }
                        }
                        item { PlayerCard(selectedName, isPlaying, onToggle, onStop) }
                        item {
                            DarkCard {
                                Text("QUICK ACCESS", color = Purple, fontWeight = FontWeight.Bold)
                                Text("10-band equalizer • Local library • Personal server", color = TextMuted)
                                OutlinedButton(onClick = { tab = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Open equalizer") }
                            }
                        }
                    }
                    1 -> {
                        item { SectionTitle("YOUR LIBRARY") }
                        item {
                            DarkCard {
                                Text("Local music", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                                Text("Select audio files stored on your phone.", color = TextMuted)
                                Button(onClick = { picker.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Browse audio files") }
                            }
                        }
                    }
                    2 -> {
                        item { SectionTitle("10-BAND EQUALIZER") }
                        item {
                            DarkCard {
                                Text("Adjust the sound", color = Purple, fontWeight = FontWeight.Bold)
                                Text("Range: -12 dB to +12 dB. Device support may vary.", color = TextMuted, fontSize = 12.sp)
                                bands.forEachIndexed { index, label ->
                                    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("$label Hz", color = Color.White)
                                            Text("${levels[index].toInt()} dB", color = Purple)
                                        }
                                        Slider(
                                            value = levels[index],
                                            onValueChange = { value ->
                                                levels = levels.toMutableList().also { it[index] = value }
                                                onEqChange(index, value)
                                            },
                                            valueRange = -12f..12f,
                                            colors = SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple)
                                        )
                                    }
                                }
                                OutlinedButton(onClick = {
                                    levels = List(10) { 0f }
                                    levels.indices.forEach { onEqChange(it, 0f) }
                                }, modifier = Modifier.fillMaxWidth()) { Text("Reset to flat") }
                            }
                        }
                    }
                    3 -> {
                        item { SectionTitle("YOUR MUSIC SOURCES") }
                        item {
                            DarkCard {
                                Text("Personal music server", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                                Text("Paste a direct audio stream URL (for example, an HTTPS MP3 or FLAC stream).", color = TextMuted)
                                OutlinedTextField(
                                    value = serverUrl,
                                    onValueChange = { serverUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("https://your-server.com/song.mp3") },
                                    singleLine = true
                                )
                                Button(onClick = {
                                    selectedName = serverUrl.substringAfterLast('/').ifBlank { "Server stream" }
                                    onPlayUrl(serverUrl)
                                }, modifier = Modifier.fillMaxWidth()) { Text("Connect and play") }
                                Text("Only direct playable audio URLs are supported in this first version. Server login and library browsing can be added later.", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Purple, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 12.sp)
}

@Composable
private fun DarkCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(SurfaceDark, RoundedCornerShape(20.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun PlayerCard(name: String, isPlaying: Boolean, onToggle: () -> Unit, onStop: () -> Unit) {
    DarkCard {
        Text("NOW PLAYING", color = Purple, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.sp)
        Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 2)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onStop) { Icon(Icons.Default.Stop, "Stop", tint = Color.White) }
            IconButton(onClick = onToggle) {
                Icon(if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled, "Play or pause", tint = Purple, modifier = Modifier.size(58.dp))
            }
        }
    }
}
