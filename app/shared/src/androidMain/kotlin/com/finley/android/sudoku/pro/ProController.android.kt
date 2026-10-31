package com.finley.android.sudoku.pro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.finley.android.sudoku.util.Persistence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
actual fun rememberProController(): ProController {
    val context = LocalContext.current
    val controller = remember(context) { AndroidProController(context) }
    DisposableEffect(controller) {
        controller.start()
        onDispose { controller.stop() }
    }
    return controller
}

/**
 * Google Play Billing implementation of the one-time lifetime unlock.
 *
 * Entitlement rules:
 *  - The cached local flag is used immediately so the app works offline.
 *  - After connecting, Play is queried: an owned purchase keeps Pro on, a
 *    successful query with no purchase revokes it (refund / cancellation).
 *  - If Play cannot be reached the cached flag is preserved.
 */
class AndroidProController(private val context: Context) : ProController {

    private val _isPro = MutableStateFlow(Persistence.isPro())
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _priceLabel = MutableStateFlow<String?>(null)
    override val priceLabel: StateFlow<String?> = _priceLabel.asStateFlow()

    private val _busy = MutableStateFlow(false)
    override val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _available = MutableStateFlow(false)
    override val available: StateFlow<Boolean> = _available.asStateFlow()

    private val _message = MutableStateFlow<ProMessage?>(null)
    override val message: StateFlow<ProMessage?> = _message.asStateFlow()

    private var productDetails: ProductDetails? = null
    private var connected = false
    private var restoreRequested = false

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    _busy.value = false
                    _message.value = ProMessage.PURCHASE_CANCELLED
                } else {
                    purchases.forEach { handlePurchase(it, announce = true) }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _busy.value = false
                _message.value = ProMessage.PURCHASE_CANCELLED
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _busy.value = false
                queryPurchases(announce = true)
            }

            else -> {
                _busy.value = false
                _message.value = ProMessage.PURCHASE_FAILED
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun start() {
        if (billingClient.isReady) {
            connected = true
            _available.value = true
            queryProductDetails()
            queryPurchases(announce = false)
        } else {
            connect()
        }
    }

    fun stop() {
        connected = false
        _available.value = false
        runCatching { billingClient.endConnection() }
    }

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    connected = true
                    _available.value = true
                    queryProductDetails()
                    queryPurchases(announce = false)
                } else {
                    connected = false
                    _available.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                connected = false
                _available.value = false
            }
        })
    }

    private fun queryProductDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(ProProduct.ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = queryResult.productDetailsList.firstOrNull()
                productDetails = details
                _priceLabel.value = details?.oneTimePurchaseOfferDetails?.formattedPrice
            }
        }
    }

    private fun queryPurchases(announce: Boolean) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val owned = purchases.any {
                    it.products.contains(ProProduct.ID) &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                purchases
                    .filter { it.products.contains(ProProduct.ID) }
                    .forEach { handlePurchase(it, announce = false) }

                if (owned) {
                    grantPro()
                    if (announce || restoreRequested) {
                        _message.value = ProMessage.RESTORE_SUCCESS
                    }
                } else {
                    revokePro()
                    if (announce || restoreRequested) {
                        _message.value = ProMessage.RESTORE_NOTHING
                    }
                }
                _busy.value = false
                restoreRequested = false
            } else {
                // Query failed (offline / Play unavailable): keep the cached flag.
                if (restoreRequested) {
                    _message.value = ProMessage.BILLING_UNAVAILABLE
                }
                _busy.value = false
                restoreRequested = false
            }
        }
    }

    private fun handlePurchase(purchase: Purchase, announce: Boolean) {
        if (!purchase.products.contains(ProProduct.ID)) return
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!purchase.isAcknowledged) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(params) { }
                }
                grantPro()
                _busy.value = false
                if (announce) _message.value = ProMessage.PURCHASE_SUCCESS
            }

            Purchase.PurchaseState.PENDING -> {
                _busy.value = false
                if (announce) _message.value = ProMessage.PURCHASE_PENDING
            }

            else -> {
                _busy.value = false
            }
        }
    }

    private fun grantPro() {
        Persistence.setPro(true)
        _isPro.value = true
    }

    private fun revokePro() {
        Persistence.setPro(false)
        _isPro.value = false
    }

    override fun purchase() {
        val activity = context.findActivity()
        if (activity == null) {
            _message.value = ProMessage.BILLING_UNAVAILABLE
            return
        }
        val details = productDetails
        if (!connected || details == null) {
            _message.value = ProMessage.BILLING_UNAVAILABLE
            if (!connected) connect()
            return
        }
        _busy.value = true
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _busy.value = false
            _message.value = ProMessage.PURCHASE_FAILED
        }
    }

    override fun restore() {
        restoreRequested = true
        _busy.value = true
        if (!connected) connect()
        queryPurchases(announce = true)
    }

    override fun consumeMessage() {
        _message.value = null
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
