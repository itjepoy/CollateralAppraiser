package com.cremcashcamfin.collateralappraiser.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cremcashcamfin.collateralappraiser.helper.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientViewModel : ViewModel() {
    private val _clients = MutableStateFlow<List<ClientInfo>>(emptyList())
    val clients: StateFlow<List<ClientInfo>> = _clients

    init {
        loadClients()
    }

    private fun loadClients() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = DBHelper.getClientNames()
            _clients.value = result
        }
    }
}
