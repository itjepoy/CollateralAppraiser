package com.cremcashcamfin.collateralappraiser.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cremcashcamfin.collateralappraiser.helper.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that manages a list of ClientInfo objects.
 * This class is responsible for loading and exposing client data to the UI.
 */
class ClientViewModel : ViewModel() {

    // Backing property for the client list, mutable within the ViewModel
    private val _clients = MutableStateFlow<List<ClientInfo>>(emptyList())

    // Publicly exposed immutable StateFlow for UI observation
    val clients: StateFlow<List<ClientInfo>> = _clients

    // Called when the ViewModel is initialized
    init {
        loadClients() // Start loading clients from the data source
    }

    /**
     * Loads the client list from the database asynchronously.
     * Uses Dispatchers.IO for background work.
     */
    private fun loadClients() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = DBHelper.getClientNames() // Assume this is a suspending or blocking DB call
            _clients.value = result // Update the state flow with retrieved client list
        }
    }
}
