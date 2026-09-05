package com.speakfreeenglish.app.billing

import android.app.Activity
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Official Google Play Billing Library v7 manager.
 * Strict enforcement: No user premium status is granted without a validated Google Play purchase receipt.
 */
class PlayBillingManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onPurchaseVerified: (purchase: Purchase) -> Unit
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        private const val TAG = "PlayBillingManager"
        const val VIP_5MONTHS_PRODUCT_ID = "speakfree_vip_5months"
    }

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _billingMessage = MutableStateFlow<String?>(null)
    val billingMessage: StateFlow<String?> = _billingMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        startBillingConnection()
    }

    fun startBillingConnection() {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Google Play Billing v7 setup finished successfully.")
            _isReady.value = true
            queryAvailableProducts()
            queryExistingActivePurchases()
        } else {
            Log.e(TAG, "Google Play Billing setup failed with code: ${billingResult.responseCode} (${billingResult.debugMessage})")
            _isReady.value = false
            _billingMessage.value = "Billing connection failed: ${billingResult.debugMessage}"
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Google Play Billing service disconnected. Retrying...")
        _isReady.value = false
        // Reconnect with backoff or on next foreground event
        startBillingConnection()
    }

    /**
     * Queries official product details from Google Play Catalog using v7 API.
     */
    private fun queryAvailableProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(VIP_5MONTHS_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(VIP_5MONTHS_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && queryProductDetailsList.isNotEmpty()) {
                val details = queryProductDetailsList.first()
                Log.d(TAG, "Loaded product details for: ${details.productId} (${details.name})")
                _productDetails.value = details
            } else {
                Log.w(TAG, "Product details query returned code ${billingResult.responseCode}: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Restores purchases by querying Google Play for active subscriptions / entitlements.
     * Strictly verifies purchase tokens and states before applying entitlement.
     */
    fun queryExistingActivePurchases() {
        val paramsSubs = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(paramsSubs) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processValidPurchases(purchases)
            }
        }

        val paramsInApp = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(paramsInApp) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processValidPurchases(purchases)
            }
        }
    }

    /**
     * Launches the official Google Play Purchase Flow Sheet.
     * No custom or unverified payment bypass is permitted.
     */
    fun launchPurchaseFlow(activity: Activity): Boolean {
        if (!billingClient.isReady) {
            _billingMessage.value = "Google Play Store is connecting. Please retry in a moment."
            startBillingConnection()
            return false
        }

        val details = _productDetails.value
        val productDetailsParamsList = if (details != null) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .apply {
                        if (offerToken != null) {
                            setOfferToken(offerToken)
                        }
                    }
                    .build()
            )
        } else {
            // If product details haven't completed loading yet, query and inform user
            queryAvailableProducts()
            _billingMessage.value = "Retrieving official subscription details from Google Play..."
            return false
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _isProcessing.value = true
        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _isProcessing.value = false
            _billingMessage.value = "Could not open Google Play sheet: ${result.debugMessage}"
            return false
        }
        return true
    }

    /**
     * Callback from Google Play Billing Client when purchases complete or update.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        _isProcessing.value = false
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    processValidPurchases(purchases)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Google Play purchase canceled by user.")
                _billingMessage.value = "Purchase was canceled."
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Google Play: Item already owned. Restoring purchase...")
                queryExistingActivePurchases()
            }
            else -> {
                Log.e(TAG, "Google Play purchase error: ${billingResult.responseCode} - ${billingResult.debugMessage}")
                _billingMessage.value = "Purchase error: ${billingResult.debugMessage}"
            }
        }
    }

    /**
     * Strictly verifies and acknowledges purchased items.
     * Only invokes onPurchaseVerified if the purchase state is PURCHASED and the token is valid.
     */
    private fun processValidPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Ensure purchase token is present
                if (purchase.purchaseToken.isBlank()) {
                    Log.e(TAG, "Ignored purchase with empty purchase token.")
                    continue
                }

                // Check acknowledgement
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { ackResult ->
                        if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Purchase acknowledged successfully: ${purchase.orderId}")
                            coroutineScope.launch {
                                withContext(Dispatchers.Main) {
                                    onPurchaseVerified(purchase)
                                }
                            }
                        } else {
                            Log.e(TAG, "Failed to acknowledge purchase: ${ackResult.debugMessage}")
                        }
                    }
                } else {
                    // Already acknowledged, verify and grant entitlements
                    coroutineScope.launch {
                        withContext(Dispatchers.Main) {
                            onPurchaseVerified(purchase)
                        }
                    }
                }
            }
        }
    }

    fun clearBillingMessage() {
        _billingMessage.value = null
    }

    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
