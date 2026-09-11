package com.example.deviceasset.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.deviceasset.domain.model.DeviceSort
import com.example.deviceasset.domain.repository.DeviceRepository
import com.example.deviceasset.domain.usecase.CalculateAssetSummary
import com.example.deviceasset.domain.usecase.CalculateDeviceMetrics
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DeviceListViewModel(
    private val repository: DeviceRepository,
    private val calculateDeviceMetrics: CalculateDeviceMetrics = CalculateDeviceMetrics(),
    private val calculateAssetSummary: CalculateAssetSummary = CalculateAssetSummary(),
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(DeviceStatusFilter.ACTIVE)
    private val selectedSort = MutableStateFlow(DeviceSort.PURCHASE_DATE)
    private val isDescending = MutableStateFlow(true)

    private val allDevices = repository.observeAllDevices()

    val uiState: StateFlow<DeviceListUiState> = combine(
        allDevices,
        selectedFilter,
        selectedSort,
        isDescending,
    ) { devices, filter, sort, descending ->
        val today = LocalDate.now()
        val items = devices.map { device ->
            DeviceListItem(device, calculateDeviceMetrics(device, today))
        }
        val filteredItems = items.filter { filter.matches(it.device) }
        val sortedItems = filteredItems.sortedWith(sortComparator(sort, descending))

        DeviceListUiState(
            items = sortedItems,
            summary = calculateAssetSummary(devices, today = today),
            filter = filter,
            sort = sort,
            descending = descending,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeviceListUiState(),
    )

    fun setFilter(filter: DeviceStatusFilter) {
        selectedFilter.value = filter
    }

    fun setSort(sort: DeviceSort) {
        selectedSort.value = sort
    }

    fun toggleSortDirection() {
        isDescending.value = !isDescending.value
    }

    private fun sortComparator(sort: DeviceSort, descending: Boolean): Comparator<DeviceListItem> {
        val comparator = when (sort) {
            DeviceSort.PURCHASE_DATE -> compareBy<DeviceListItem> { it.device.purchaseDate }
            DeviceSort.PURCHASE_PRICE -> compareBy { it.device.purchasePriceCents }
            DeviceSort.DAILY_COST -> compareBy { it.metrics.dailyCost }
            DeviceSort.NAME -> compareBy { it.device.name.lowercase(Locale.ROOT) }
        }
        return if (descending) comparator.reversed() else comparator
    }

    class Factory(
        private val repository: DeviceRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DeviceListViewModel::class.java)) {
                return DeviceListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
