package com.comunitytv.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comunitytv.data.models.Channel
import com.comunitytv.data.repository.PlaylistRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = PlaylistRepository()

    // Estado de la UI
    val isLoading = mutableStateOf(false)
    val channels = mutableStateOf<List<Channel>>(emptyList())
    val currentChannel = mutableStateOf<Channel?>(null)
    val errorMessage = mutableStateOf<String?>(null)

    // Tipo de fuente: "m3u" o "xtream"
    var sourceType = "m3u"

    fun loadM3U(url: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val list = repository.fetchM3U(url)
                if (list.isEmpty()) {
                    errorMessage.value = "No se encontraron canales o URL inválida"
                }
                channels.value = list
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            }
            isLoading.value = false
        }
    }

    fun loadXtream(server: String, user: String, pass: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val list = repository.fetchXtream(server, user, pass)
                if (list.isEmpty()) {
                    errorMessage.value = "Credenciales incorrectas o servidor sin canales"
                }
                channels.value = list
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            }
            isLoading.value = false
        }
    }

    fun setCurrentChannel(channel: Channel) {
        currentChannel.value = channel
    }

    fun toggleFavorite(channel: Channel) {
        val updated = channels.value.map {
            if (it.id == channel.id) it.copy(isFavorite = !it.isFavorite) else it
        }
        channels.value = updated
        // Actualizar también el current si está seleccionado
        currentChannel.value = updated.find { it.id == channel.id }
    }
}
