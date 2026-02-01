package com.tadevolta.gym.ui.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de estado global da aplicação.
 * Controla flags que sinalizam necessidade de atualização de dados.
 * 
 * Singleton gerenciado pelo Hilt, não é um ViewModel pois não precisa de lifecycle.
 */
@Singleton
class AppStateManager @Inject constructor() {
    
    /**
     * Flag que indica se os dados precisam ser atualizados.
     * Ativada após login ou quando usuário solicita refresh manual.
     */
    private val _needsDataRefresh = MutableStateFlow(false)
    val needsDataRefresh: StateFlow<Boolean> = _needsDataRefresh.asStateFlow()
    
    /**
     * Marca que os dados precisam ser atualizados.
     * Usado após login ou outras ações que requerem sincronização.
     */
    fun markNeedsRefresh() {
        _needsDataRefresh.value = true
    }
    
    /**
     * Limpa a flag de necessidade de atualização.
     * Chamado após os dados serem atualizados.
     */
    fun clearRefreshFlag() {
        _needsDataRefresh.value = false
    }
}
