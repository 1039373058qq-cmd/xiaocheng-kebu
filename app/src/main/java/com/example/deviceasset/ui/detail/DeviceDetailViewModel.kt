package com.example.deviceasset.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.deviceasset.domain.model.DeviceMetrics
import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.repository.DeviceRepository
import com.example.deviceasset.domain.usecase.CalculateDeviceMetrics
import com.example.deviceasset.domain.usecase.DeleteDevice
import com.example.deviceasset.domain.usecase.RetireDevice
import com.example.deviceasset.domain.usecase.SellDevice
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DeviceDetailUiState(
    val device: Device? = null,
    val metrics: DeviceMetrics? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val deleted: Boolean = false,
    val actionError: String? = null,
)

class DeviceDetailViewModel(
    private val repository: DeviceRepository,
    private val deviceId: Long,
    private val calculateDeviceMetrics: CalculateDeviceMetrics = CalculateDeviceMetrics(),
    private val retireDevice: RetireDevice = RetireDevice(repository),
    private val sellDevice: SellDevice = SellDevice(repository),
    private val deleteDevice: DeleteDevice = DeleteDevice(repository),
) : ViewModel() {
    private val isSaving = MutableStateFlow(false)
    private val actionError = MutableStateFlow<String?>(null)
    private val deleted = MutableStateFlow(false)

    private val observedDevice = repository.observeDeviceById(deviceId)

    val uiState: StateFlow<DeviceDetailUiState> = combine(
        observedDevice,
        isSaving,
        actionError,
        deleted,
    ) { device, saving, error, isDeleted ->
        DeviceDetailUiState(
            device = device,
            metrics = device?.let(calculateDeviceMetrics::invoke),
            isLoading = false,
            isSaving = saving,
            deleted = isDeleted,
            actionError = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeviceDetailUiState(),
    )

    fun retire() {
        val device = uiState.value.device ?: return
        runAction {
            retireDevice(device)
        }
    }

    fun sell(soldDate: LocalDate, soldPriceCents: Long) {
        val device = uiState.value.device ?: return
        runAction {
            sellDevice(device, soldDate, soldPriceCents)
        }
    }

    fun delete() {
        val device = uiState.value.device ?: return
        runAction {
            deleteDevice(device)
            deleted.value = true
        }
    }

    private fun runAction(action: suspend () -> Unit) {
        if (isSaving.value) return
        isSaving.value = true
        actionError.value = null
        viewModelScope.launch {
            runCatching { action() }
                .onFailure { error ->
                    actionError.value = error.message ?: "操作失败，请稍后重试"
                }
            isSaving.value = false
        }
    }

    class Factory(
        private val repository: DeviceRepository,
        private val deviceId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DeviceDetailViewModel::class.java)) {
                return DeviceDetailViewModel(repository, deviceId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
