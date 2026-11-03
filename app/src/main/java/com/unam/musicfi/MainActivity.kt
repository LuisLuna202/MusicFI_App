package com.unam.musicfi

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unam.musicfi.data.AndroidAudioPlayer
import com.unam.musicfi.data.MediaStoreAudioRepository
import com.unam.musicfi.domain.filterSongs
import com.unam.musicfi.domain.hasLoginInput
import com.unam.musicfi.model.LocalSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF101033)
private val CardNavy = Color(0xFF1E1E4E)
private val Indigo = Color(0xFF5C6BC0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicFITheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MusicFINavigation()
                }
            }
        }
    }
}

@Composable
fun MusicFITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Indigo,
            onPrimary = Color.White,
            secondary = Color(0xFF9FA8DA),
            background = Navy,
            surface = CardNavy,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

@Composable
fun MusicFINavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen() }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().background(Navy).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.logo_musicfi),
            contentDescription = "Logotipo de MusicFI",
            modifier = Modifier.size(136.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("musicFI", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Juntos, a través de la música.", fontSize = 16.sp, color = Color.LightGray)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) { Text("Empezar") }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) { Text("Iniciar sesión") }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Navy).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = user,
            onValueChange = { user = it; error = false },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = false },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (error) {
            Text("Completa ambos campos para continuar.", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (hasLoginInput(user, password)) navController.navigate("home")
                else error = true
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Entrar") }
        Spacer(Modifier.height(12.dp))
        Text(
            "Acceso de demostración: no se autentica contra un servidor.",
            color = Color.LightGray,
            fontSize = 12.sp
        )
        TextButton(onClick = { navController.popBackStack() }) { Text("Volver") }
    }
}

private fun audioPermission(): String =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val repository = remember(context) { MediaStoreAudioRepository(context) }
    val player = remember(context) { AndroidAudioPlayer(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission()) == PackageManager.PERMISSION_GRANTED
        )
    }
    var songs by remember { mutableStateOf(emptyList<LocalSong>()) }
    var query by remember { mutableStateOf("") }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var playing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) message = "Se necesita permiso para mostrar tu música local."
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            try {
                songs = withContext(Dispatchers.IO) { repository.getLocalSongs() }
                message = if (songs.isEmpty()) "No se encontraron canciones en el dispositivo." else null
            } catch (_: SecurityException) {
                hasPermission = false
                message = "No hay permiso para acceder a los archivos de audio."
            } catch (_: Exception) {
                message = "No fue posible leer la biblioteca de música."
            }
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    val filteredSongs = remember(songs, query) { filterSongs(songs, query) }

    fun playSong(index: Int) {
        val song = songs.getOrNull(index) ?: return
        try {
            player.play(android.net.Uri.parse(song.uri)) { playing = false }
            currentIndex = index
            playing = true
            message = null
        } catch (_: Exception) {
            playing = false
            message = "No se pudo reproducir «${song.title}»."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Navy)
            .statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("musicFI", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Tu música, en un solo lugar", color = Color.LightGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar canción o artista") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(Modifier.height(12.dp))

        if (!hasPermission) {
            Card(colors = CardDefaults.cardColors(containerColor = CardNavy)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Acceso a tu música", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("MusicFI necesita permiso para leer los archivos de audio guardados en este dispositivo.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { permissionLauncher.launch(audioPermission()) }) {
                        Text("Conceder permiso")
                    }
                }
            }
        } else {
            Text("${filteredSongs.size} canciones", color = Color.LightGray, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            CurrentTrackCard(
                song = songs.getOrNull(currentIndex),
                playing = playing,
                onPlayPause = {
                    if (currentIndex >= 0) {
                        player.toggle()
                        playing = player.isPlaying()
                    } else if (songs.isNotEmpty()) {
                        playSong(0)
                    }
                },
                onPrevious = { if (songs.isNotEmpty()) playSong(if (currentIndex <= 0) songs.lastIndex else currentIndex - 1) },
                onNext = { if (songs.isNotEmpty()) playSong(if (currentIndex >= songs.lastIndex) 0 else currentIndex + 1) }
            )
            Spacer(Modifier.height(12.dp))
            if (filteredSongs.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        if (songs.isEmpty()) "Tu biblioteca está vacía." else "No hay resultados para «$query».",
                        color = Color.LightGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSongs, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            selected = songs.getOrNull(currentIndex)?.id == song.id,
                            onPlay = { playSong(songs.indexOfFirst { it.id == song.id }) }
                        )
                    }
                }
            }
        }
        message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color(0xFFFFCC80), fontSize = 12.sp)
        }
    }
}

@Composable
private fun CurrentTrackCard(
    song: LocalSong?,
    playing: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("REPRODUCIENDO", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlbumArtwork(song?.albumArtUri, Modifier.size(54.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(song?.title ?: "Ninguna canción seleccionada", fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(song?.artist.orEmpty(), color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior")
                }
                FilledIconButton(onClick = onPlayPause) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playing) "Pausar" else "Reproducir"
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente")
                }
            }
        }
    }
}

@Composable
private fun SongRow(song: LocalSong, selected: Boolean, onPlay: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF303060) else Color(0xFF16163F)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtwork(song.albumArtUri, Modifier.size(48.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(song.artist, color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
            }
            TextButton(onClick = onPlay) { Text("Reproducir") }
        }
    }
}

@Composable
private fun AlbumArtwork(uri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, uri) {
        value = if (uri == null) null else withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(android.net.Uri.parse(uri))?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Portada del álbum",
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFF303060), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = "Sin portada", tint = Color.LightGray)
        }
    }
}