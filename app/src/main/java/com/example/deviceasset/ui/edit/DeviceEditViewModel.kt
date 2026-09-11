package com.example.deviceasset.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceCategory
import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.repository.DeviceRepository
import com.example.deviceasset.domain.usecase.DeviceValidationError
import com.example.deviceasset.domain.usecase.ValidateDevice
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DeviceEditField {
    NAME,
    PURCHASE_DATE,
    PURCHASE_PRICE,
    RETIRED_DATE,
    SOLD_DATE,
    SOLD_PRICE,
    ADDITIONAL_COST,
    ESTIMATED_VALUE,
}

data class DeviceEditUiState(
    val deviceId: Long? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val name: String = "",
    val category: DeviceCategory = DeviceCategory.PHONE,
    val brand: String = "",
    val model: String = "",
    val storage: String = "",
    val purchaseDate: LocalDate = LocalDate.now(),
    val purchasePrice: String = "",
    val status: DeviceStatus = DeviceStatus.ACTIVE,
    val retiredDate: LocalDate? = null,
    val soldDate: LocalDate? = null,
    val soldPrice: String = "",
    val additionalCost: String = "",
    val estimatedCurrentValue: String = "",
    val note: String = "",
    val fieldErrors: Map<DeviceEditField, String> = emptyMap(),
    val validationErrors: Set<DeviceValidationError> = emptySet(),
    val formError: String? = null,
)

class DeviceEditViewModel(
    private val repository: DeviceRepository,
    private val deviceId: Long?,
    private val validateDevice: ValidateDevice = ValidateDevice(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        DeviceEditUiState(
            deviceId = deviceId,
            isLoading = deviceId != null,
        ),
    )
    val uiState: StateFlow<DeviceEditUiState> = _uiState.asStateFlow()

    private var originalDevice: Device? = null

    init {
        if (deviceId != null) {
            loadDevice(deviceId)
        }
    }

    fun onNameChanged(value: String) = updateForm { it.copy(name = value) }

    fun onCategoryChanged(value: DeviceCategory) = updateForm { it.copy(category = value) }

    fun onBrandChanged(value: String) = updateForm { it.copy(brand = value) }

    fun onModelChanged(value: String) = updateForm { it.copy(model = value) }

    fun onStorageChanged(value: String) = updateForm { it.copy(storage = value) }

    fun onPurchaseDateChanged(value: LocalDate) = updateForm { it.copy(purchaseDate = value) }

    fun onPurchasePriceChanged(value: String) = updateForm { it.copy(purchasePrice = value) }

    fun onAdditionalCostChanged(value: String) = updateForm { it.copy(additionalCost = value) }

    fun onEstimatedValueChanged(value: String) = updateForm { it.copy(estimatedCurrentValue = value) }

    fun onNoteChanged(value: String) = updateForm { it.copy(note = value) }

    fun onStatusChanged(value: DeviceStatus) {
        updateForm { current ->
            when (value) {
                DeviceStatus.ACTIVE -> current.copy(
                    status = value,
                    retiredDate = null,
                    soldDate = null,
                    soldPrice = "",
                )

                DeviceStatus.RETIRED -> current.copy(
                    status = value,
                    retiredDate = current.retiredDate ?: LocalDate.now(),
                    soldDate = null,
                    soldPrice = "",
                )

                DeviceStatus.SOLD -> current.copy(
                    status = value,
                    soldDate = current.soldDate ?: LocalDate.now(),
                )
            }
        }
    }

    fun onRetiredDateChanged(value: LocalDate) = updateForm { it.copy(retiredDate = value) }

    fun onSoldDateChanged(value: LocalDate) = updateForm { it.copy(soldDate = value) }

    fun onSoldPriceChanged(value: String) = updateForm { it.copy(soldPrice = value) }

    fun save() {
        if (_uiState.value.isLoading || _uiState.value.isSaving) return

        val current = _uiState.value
        val fieldErrors = linkedMapOf<DeviceEditField, String>()
        val purchasePriceCents = parseRequiredMoney(
            text = current.purchasePrice,
            field = DeviceEditField.PURCHASE_PRICE,
            label = "购买价格",
            errors = fieldErrors,
        )
        val additionalCostCents = parseOptionalMoney(
            text = current.additionalCost,
            field = DeviceEditField.ADDITIONAL_COST,
            label = "后续支出",
            errors = fieldErrors,
        ) ?: 0L
        val estimatedValueCents = parseOptionalMoney(
            text = current.estimatedCurrentValue,
            field = DeviceEditField.ESTIMATED_VALUE,
            label = "当前估值",
            errors = fieldErrors,
        )
        val soldPriceCents = if (current.status == DeviceStatus.SOLD) {
            parseRequiredMoney(
                text = current.soldPrice,
                field = DeviceEditField.SOLD_PRICE,
                label = "出售价格",
                errors = fieldErrors,
            )
        } else {
            null
        }

        if (fieldErrors.isNotEmpty() || purchasePriceCents == null) {
            _uiState.update { it.copy(fieldErrors = fieldErrors) }
            return
        }

        val now = Instant.now()
        val device = Device(
            id = originalDevice?.id ?: 0L,
            name = current.name.trim(),
            category = current.category,
            brand = current.brand.trim().ifBlank { null },
            model = current.model.trim().ifBlank { null },
            storage = current.storage.trim().ifBlank { null },
            purchaseDate = current.purchaseDate,
            purchasePriceCents = purchasePriceCents,
            status = current.status,
            retiredDate = current.retiredDate,
            soldDate = current.soldDate,
            soldPriceCents = soldPriceCents,
            additionalCostCents = additionalCostCents,
            estimatedCurrentValueCents = estimatedValueCents,
            note = current.note.trim().ifBlank { null },
            imagePath = originalDevice?.imagePath,
            createdAt = originalDevice?.createdAt ?: now,
            updatedAt = now,
        )
        val validation = validateDevice(device)
        val validationFieldErrors = validation.errors.associateWithMessage()
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    fieldErrors = validationFieldErrors,
                    validationErrors = validation.errors,
                )
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, formError = null) }
        viewModelScope.launch {
            runCatching {
                if (originalDevice == null) {
                    repository.insert(device)
                } else {
                    repository.update(device)
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        formError = error.message ?: "保存失败，请稍后重试",
                    )
                }
            }
        }
    }

    private fun loadDevice(id: Long) {
        viewModelScope.launch {
            val device = repository.observeDeviceById(id).first()
            if (device == null) {
                _uiState.update {
                    it.copy(isLoading = false, formError = "未找到要编辑的设备")
                }
                return@launch
            }

            originalDevice = device
            _uiState.value = device.toUiState()
        }
    }

    private fun updateForm(transform: (DeviceEditUiState) -> DeviceEditUiState) {
        _uiState.update {
            transform(it).copy(
                fieldErrors = emptyMap(),
                validationErrors = emptySet(),
                formError = null,
                saved = false,
            )
        }
    }

    private fun parseRequiredMoney(
        text: String,
        field: DeviceEditField,
        label: String,
        errors: MutableMap<DeviceEditField, String>,
    ): Long? {
        if (text.isBlank()) {
            errors[field] = "${label}不能为空"
            return null
        }
        return parseMoney(text).also { value ->
            if (value == null) errors[field] = "${label}格式不正确"
        }
    }

    private fun parseOptionalMoney(
        text: String,
        field: DeviceEditField,
        label: String,
        errors: MutableMap<DeviceEditField, String>,
    ): Long? {
        if (text.isBlank()) return null
        return parseMoney(text).also { value ->
            if (value == null) errors[field] = "${label}格式不正确"
        }
    }

    class Factory(
        private val repository: DeviceRepository,
        private val deviceId: Long?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DeviceEditViewModel::class.java)) {
                return DeviceEditViewModel(repository, deviceId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

private fun Device.toUiState(): DeviceEditUiState = DeviceEditUiState(
    deviceId = id,
    name = name,
    category = category,
    brand = brand.orEmpty(),
    model = model.orEmpty(),
    storage = storage.orEmpty(),
    purchaseDate = purchaseDate,
    purchasePrice = centsToMoneyText(purchasePriceCents),
    status = status,
    retiredDate = retiredDate,
    soldDate = soldDate,
    soldPrice = soldPriceCents?.let(::centsToMoneyText).orEmpty(),
    additionalCost = if (additionalCostCents == 0L) "" else centsToMoneyText(additionalCostCents),
    estimatedCurrentValue = estimatedCurrentValueCents?.let(::centsToMoneyText).orEmpty(),
    note = note.orEmpty(),
)

private fun Set<DeviceValidationError>.associateWithMessage(): Map<DeviceEditField, String> = buildMap {
    for (error in this@associateWithMessage) {
        when (error) {
            DeviceValidationError.EMPTY_NAME -> put(DeviceEditField.NAME, "设备名称不能为空")
            DeviceValidationError.PURCHASE_DATE_IN_FUTURE -> put(DeviceEditField.PURCHASE_DATE, "购买日期不能晚于今天")
            DeviceValidationError.NEGATIVE_PURCHASE_PRICE -> put(DeviceEditField.PURCHASE_PRICE, "购买价格不能小于 0")
            DeviceValidationError.NEGATIVE_ADDITIONAL_COST -> put(DeviceEditField.ADDITIONAL_COST, "后续支出不能小于 0")
            DeviceValidationError.NEGATIVE_ESTIMATED_VALUE -> put(DeviceEditField.ESTIMATED_VALUE, "当前估值不能小于 0")
            DeviceValidationError.RETIRED_DATE_MISSING -> put(DeviceEditField.RETIRED_DATE, "请选择退役日期")
            DeviceValidationError.RETIRED_DATE_BEFORE_PURCHASE -> put(DeviceEditField.RETIRED_DATE, "退役日期不能早于购买日期")
            DeviceValidationError.SOLD_DATE_MISSING -> put(DeviceEditField.SOLD_DATE, "请选择出售日期")
            DeviceValidationError.SOLD_DATE_BEFORE_PURCHASE -> put(DeviceEditField.SOLD_DATE, "出售日期不能早于购买日期")
            DeviceValidationError.SOLD_PRICE_MISSING -> put(DeviceEditField.SOLD_PRICE, "出售价格不能为空")
            DeviceValidationError.NEGATIVE_SOLD_PRICE -> put(DeviceEditField.SOLD_PRICE, "出售价格不能小于 0")
        }
    }
}

private fun parseMoney(text: String): Long? = runCatching {
    val amount = BigDecimal(text.trim())
    amount.movePointRight(2)
        .setScale(0, RoundingMode.UNNECESSARY)
        .longValueExact()
}.getOrNull()

private fun centsToMoneyText(cents: Long): String =
    BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.HALF_UP).toPlainString()
