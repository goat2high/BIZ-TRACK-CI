package com.example.biztrackci.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biztrackci.domain.repository.BizTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val businessName: String = "Boutique Abidjan Express",
    val userRole: String = "OWNER", // "OWNER" or "STAFF"
    val subscriptionStatus: String = "ACTIVE", // "ACTIVE", "TRIAL", "EXPIRED"
    val isEditBusinessNameDialogOpen: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: BizTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.businessName,
                repository.userRole,
                repository.subscriptionStatus
            ) { name, role, status ->
                ProfileUiState(
                    businessName = name,
                    userRole = role,
                    subscriptionStatus = status,
                    isEditBusinessNameDialogOpen = _uiState.value.isEditBusinessNameDialogOpen,
                    userMessage = _uiState.value.userMessage
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateRole(newRole: String) {
        viewModelScope.launch {
            repository.updateRole(newRole)
            val roleLabel = if (newRole == "OWNER") "PROPRIÉTAIRE" else "EMPLOYÉ"
            _uiState.update { it.copy(userMessage = "Rôle basculé vers: $roleLabel") }
        }
    }

    fun updateBusinessName(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateBusinessName(newName.trim())
            _uiState.update {
                it.copy(
                    isEditBusinessNameDialogOpen = false,
                    userMessage = "Nom du commerce mis à jour"
                )
            }
        }
    }

    fun updateSubscriptionStatus(newStatus: String) {
        viewModelScope.launch {
            repository.updateSubscriptionStatus(newStatus)
            _uiState.update { it.copy(userMessage = "Statut d'abonnement mis à jour") }
        }
    }

    fun openEditBusinessNameDialog() {
        _uiState.update { it.copy(isEditBusinessNameDialogOpen = true) }
    }

    fun closeEditBusinessNameDialog() {
        _uiState.update { it.copy(isEditBusinessNameDialogOpen = false) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
