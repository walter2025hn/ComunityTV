package com.comunitytv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.comunitytv.data.models.Channel
import com.comunitytv.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var showXtreamDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunity TV", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showXtreamDialog = true }) {
                        Icon(Icons.Default.Key, contentDescription = "Xtream")
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar M3U")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.isLoading.value -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                viewModel.channels.value.isNotEmpty() -> {
                    ChannelList(
                        channels = viewModel.channels.value,
                        onChannelClick = { channel ->
                            viewModel.setCurrentChannel(channel)
                            onNavigateToPlayer()
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LiveTv, contentDescription = null, modifier = Modifier.size(80.dp))
                            Text("Sin canales", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("Agrega una lista M3U o Xtream", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            if (viewModel.errorMessage.value != null) {
                                Text(
                                    text = viewModel.errorMessage.value!!,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para M3U
    if (showDialog) {
        AddM3UDialog(
            onDismiss = { showDialog = false },
            onConfirm = { url ->
                viewModel.sourceType = "m3u"
                viewModel.loadM3U(url)
                showDialog = false
            }
        )
    }

    // Diálogo para Xtream
    if (showXtreamDialog) {
        AddXtreamDialog(
            onDismiss = { showXtreamDialog = false },
            onConfirm = { server, user, pass ->
                viewModel.sourceType = "xtream"
                viewModel.loadXtream(server, user, pass)
                showXtreamDialog = false
            }
        )
    }
}

// --- LISTA DE CANALES (Optimizada para TV y Móvil) ---
@Composable
fun ChannelList(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit
) {
    val grouped = channels.groupBy { it.group }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        grouped.forEach { (group, groupChannels) ->
            item {
                Text(
                    text = group,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(groupChannels) { channel ->
                ChannelItem(
                    channel = channel,
                    onClick = { onChannelClick(channel) },
                    onFavoriteClick = { onToggleFavorite(channel) }
                )
            }
        }
    }
}

// --- ITEM DE CANAL ---
@Composable
fun ChannelItem(
    channel: Channel,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    // FocusRequester para navegación con mando (TV)
    val focusRequester = remember { FocusRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(channel.logo)
                    .crossfade(true)
                    .build(),
                contentDescription = channel.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre
            Text(
                text = channel.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )

            // Favorito
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    if (channel.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (channel.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// --- DIÁLOGO PARA AÑADIR M3U ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddM3UDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir lista M3U") },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL de la lista M3U") },
                placeholder = { Text("https://ejemplo.com/lista.m3u") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (url.isNotBlank()) onConfirm(url) }) {
                Text("Cargar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// --- DIÁLOGO PARA XTREAM CODES ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddXtreamDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var server by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xtream Codes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = server,
                    onValueChange = { server = it },
                    label = { Text("Servidor (URL)") },
                    placeholder = { Text("http://tu-servidor.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (server.isNotBlank() && user.isNotBlank() && pass.isNotBlank()) {
                    onConfirm(server, user, pass)
                }
            }) {
                Text("Conectar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
