package io.jok.helper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.android.billingclient.api.BillingClient.SkuType.SUBS
import com.getcapacitor.*


object JokHelperStatic {
  var activity: Activity? = null
  var isStoreReady: Boolean = false
  var isStoreDisconnected: Boolean = false
  var isStoreNotConfigured: Boolean = false
  var versionName: String = ""
  var openAppUrl: (url: Uri) -> Any = { x -> x }
  var getAudioEffect: (name: String) -> MediaPlayer? = { _ -> null }
  var onConnection: (onSuccess: () -> Any) -> BillingClientStateListener = { _ -> null as BillingClientStateListener }

  var billingRepo: BillingRepo? = null

  val transactionsObservers = mutableListOf<(Purchase) -> Unit>()

  var publishNewTransaction: (x: Purchase) -> Any = { x ->
    transactionsObservers.forEach { it(x) }
  }

  var pushNotificationsObservers = mutableListOf<(JSObject) -> Unit>()

  var pendingPushNotifications = mutableListOf<JSObject>()

  var publishNewPushNotification: (x: JSObject) -> Any = { x ->
    pushNotificationsObservers.forEach { it(x) }
  }

  var getPushNotificationState: (_: Any) -> JSObject = { _ -> JSObject() }

  var showAppReviewUI: () -> Boolean = { false }


  fun init() {
    onConnection = { onSuccess: () -> Any ->
      object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
          Log.w("BILLING", "onBillingSetupFinished: " + billingResult.debugMessage)
          if (billingResult.responseCode == BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            isStoreReady = true
            isStoreDisconnected = false

            if (onSuccess != null) {
              onSuccess()
            }
          }

          if (billingResult.responseCode == BillingResponseCode.BILLING_UNAVAILABLE) {
            isStoreNotConfigured = true
          }
        }

        override fun onBillingServiceDisconnected() {
          // Try to restart the connection on the next request to
          // Google Play by calling the startConnection() method.
          isStoreReady = false
          isStoreDisconnected = true
        }
      }
    }

    billingRepo = BillingRepo()
    this.createBillingClient(true) {}
  }

  fun purchaseUpdatedListener(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
    // To be implemented in a later section.
    if (billingResult.responseCode == BillingResponseCode.OK) {
      purchases?.forEach { publishNewTransaction(it) }
    }
  }

  fun createBillingClient(reconnect: Boolean, onSuccess: () -> Any) {
    billingRepo?.onSuccess = onSuccess

    if (reconnect) {
      billingRepo?.connect()
    }
  }

  fun destroy() {
    billingRepo?.disconnect()
  }

  fun finishTransaction(transactionId: String, cb: (success: Boolean, shouldRetry: Boolean, errorMessage: String?) -> Any) {
    val consumablePurchases = billingRepo?.billingClient?.queryPurchases(INAPP)
    var purchase = consumablePurchases?.purchasesList?.find { it.purchaseToken == transactionId }
    if (purchase !== null) {
      val consumeParams =
        ConsumeParams.newBuilder()
          .setPurchaseToken(purchase.purchaseToken)
          .build()

      billingRepo?.billingClient?.consumeAsync(consumeParams) { billingResult, _ ->
        if (billingResult.responseCode == BillingResponseCode.OK) {
          cb(true, false, null)
        } else {
          val shouldRetry = billingResult.responseCode == BillingResponseCode.SERVICE_DISCONNECTED || billingResult.responseCode == BillingResponseCode.SERVICE_TIMEOUT

          cb(false, shouldRetry, billingResult.responseCode.toString())
        }
      }

      return
    }

    val subscriptionPurchases = billingRepo?.billingClient?.queryPurchases(SUBS)
    purchase = subscriptionPurchases?.purchasesList?.find { it.purchaseToken == transactionId }

    if (purchase !== null) {

      if (purchase.isAcknowledged) {
        cb(true, false, null)
        return
      }

      val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)

      billingRepo?.billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build()) { billingResult ->

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          cb(true, false, null)
        } else {
          val shouldRetry = billingResult.responseCode == BillingResponseCode.SERVICE_DISCONNECTED || billingResult.responseCode == BillingResponseCode.SERVICE_TIMEOUT

          cb(false, shouldRetry, billingResult.debugMessage)
        }
      }

      return
    }

    cb(false, true, "Order not found")
  }
}

class BillingRepo : BillingClientStateListener {
  var billingClient: BillingClient
  var onSuccess: () -> Any = {}

  init {
    billingClient = BillingClient.newBuilder(JokHelperStatic.activity!!)
      .setListener { p0, p1 -> JokHelperStatic.purchaseUpdatedListener(p0, p1) }
      .enablePendingPurchases()
      .build()
  }

  fun connect() {
    billingClient.startConnection(this)
  }

  fun disconnect() {
    billingClient.endConnection()
  }

  override fun onBillingSetupFinished(res: BillingResult) {
    Log.w("BILLING", "onBillingSetupFinished: " + res.debugMessage)
    if (res.responseCode == BillingResponseCode.OK) {
      // The BillingClient is ready. You can query purchases here.
      JokHelperStatic.isStoreReady = true
      JokHelperStatic.isStoreDisconnected = false

      if (onSuccess != null) {
        onSuccess()
      }
    }

    if (res.responseCode == BillingResponseCode.BILLING_UNAVAILABLE) {
      JokHelperStatic.isStoreNotConfigured = true
    }
  }

  override fun onBillingServiceDisconnected() {
    // Try to restart the connection on the next request to
    // Google Play by calling the startConnection() method.
    JokHelperStatic.isStoreReady = false
    JokHelperStatic.isStoreDisconnected = true
  }
}

@NativePlugin
class JokHelper : Plugin() {
  @PluginMethod
  fun echo(call: PluginCall) {
    val value = call.getString("value")
    val ret = JSObject()
    ret.put("value", value)
    call.success(ret)
  }

  @PluginMethod
  fun setKeychainItem(call: PluginCall) {
    val key = call.getString("key")
    val value = call.getString("value")
//    val accessGroup = call.getString("accessGroup")

    val pref = JokHelperStatic.activity?.getPreferences(Context.MODE_PRIVATE)
    if (pref != null) {
      with(pref.edit()) {
        putString(key, value)
        commit()
      }
    }

    val ret = JSObject()
    ret.put("value", value)
    call.success(ret)
  }

  @PluginMethod
  fun getKeychainItem(call: PluginCall) {
    val key = call.getString("key")

    val pref = JokHelperStatic.activity?.getPreferences(Context.MODE_PRIVATE)
    val result = pref?.getString(key, null)

    val ret = JSObject()
    ret.put("value", result)
    call.success(ret)
  }

  @PluginMethod
  fun isWideScreen(call: PluginCall) {
    val metrics = DisplayMetrics()

    JokHelperStatic.activity?.windowManager?.defaultDisplay?.getMetrics(metrics)

    val result = metrics.heightPixels >= 812

    val ret = JSObject()
    ret.put("value", result)
    call.success(ret)
  }

  @PluginMethod
  fun setDeviceOrientationLock(call: PluginCall) {

    // NOT IMPLEMENTED. DEPRECATED

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun getDeviceOrientation(call: PluginCall) {

    // NOT IMPLEMENTED. DEPRECATED

    val ret = JSObject()
    ret.put("isPortrait", true)
    ret.put("isFlat", true)
    ret.put("isLandscape", false)
    ret.put("rawValue", 1)
    ret.put("orientation", "portrait")
    call.success(ret)
  }

  @PluginMethod
  fun listenDeviceOrientationChanges(call: PluginCall) {

    // NOT IMPLEMENTED. DEPRECATED

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun listenPushNotificationEvents(call: PluginCall) {

    JokHelperStatic.pushNotificationsObservers.add { x ->
      this.notifyListeners("appPushNotificationEvent", x)
    }

    JokHelperStatic.pendingPushNotifications.forEach {
      JokHelperStatic.publishNewPushNotification(it)
    }

    val ret = JSObject()
    ret.put("value", false)
    call.success(ret)
  }

  @PluginMethod
  fun getPushNotificationsState(call: PluginCall) {

    val state = JokHelperStatic.getPushNotificationState(0)

    call.success(state)
  }

  @PluginMethod
  fun askPushNotificationsPermission(call: PluginCall) {

    // open app settings
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS

    val uri = Uri.fromParts("package", JokHelperStatic.activity?.packageName, null)
    intent.data = uri
    context.startActivity(intent)
    // open app settings

    val ret = JSObject()
    ret.put("accepted", true)
    call.success(ret)
  }

  @PluginMethod
  fun openAppSettings(call: PluginCall) {

    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS

    val uri = Uri.fromParts("package", JokHelperStatic.activity?.packageName, null)
    intent.data = uri
    context.startActivity(intent)

    val ret = JSObject()
    ret.put("success", false)
    call.success(ret)
  }

  @PluginMethod
  fun canMakePayments(call: PluginCall) {

    val result = JokHelperStatic.isStoreReady

    val ret = JSObject()
    ret.put("value", result)
    ret.put("isStoreNotConfigured", JokHelperStatic.isStoreNotConfigured)
    call.success(ret)
  }


  private var productsCache: Array<SkuDetails> = emptyArray()

  @PluginMethod
  fun loadProducts(call: PluginCall) {
    val productIds = call.getArray("productIds")
    val type = call.getString("type", "")

    Log.i("BILLING", "loadProducts requested items: ${productIds.length()}")

    if (type != "") {
      val skuType: String = if (type == "SUBSCRIPTION") {
        INAPP
      } else {
        SUBS
      }

      this.requestLoadProducts(productIds.toList(), skuType) { success, items ->
        Log.i("BILLING", "loadProducts finished, loaded: ${items.size}")

        val ret = JSObject()
        ret.put("success", success)
        ret.put("products", JSArray(items))
        ret.put("invalidProducts", JSArray())
        call.success(ret)
      }

      return
    }

    val productsByType = productIds.toList<String>().groupBy {
      if (it.contains("membership")) {
        SUBS
      } else {
        INAPP
      }
    }

    var responseCount = 0
    val needResponseCount = productsByType.keys.size
    var tempResult = emptyArray<JSObject>()


    if (productsByType.keys.contains(SUBS) && productsByType[SUBS]!!.isNotEmpty()) {
      this.requestLoadProducts(productsByType[SUBS]!!, SUBS) { success, res ->
        Log.i("BILLING", "loadProducts finished, loaded: ${res.size}")

        tempResult = tempResult.plus(res)
        responseCount++

        if (responseCount >= needResponseCount) {
          val ret = JSObject()
          ret.put("success", success)
          ret.put("products", JSArray(tempResult))
          ret.put("invalidProducts", JSArray())
          call.success(ret)
        }
      }
    }

    if (productsByType.keys.contains(INAPP) && productsByType[INAPP]!!.isNotEmpty()) {
      this.requestLoadProducts(productsByType[INAPP]!!, INAPP) { success, res ->
        Log.i("BILLING", "loadProducts finished, loaded: ${res.size}")

        tempResult = tempResult.plus(res)
        responseCount++

        if (responseCount >= needResponseCount) {
          val ret = JSObject()
          ret.put("success", success)
          ret.put("products", JSArray(tempResult))
          ret.put("invalidProducts", JSArray())
          call.success(ret)
        }
      }
    }
  }

  private fun requestLoadProducts(productIds: List<String>, skuType: String, cb: (success: Boolean, res: List<JSObject>) -> Any) {

    val params = SkuDetailsParams.newBuilder()
    params.setSkusList(productIds.toList()).setType(skuType)

    if (JokHelperStatic.billingRepo?.billingClient != null) {
      JokHelperStatic.billingRepo?.billingClient?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
        Log.i("billingProducts", skuDetailsList?.size?.toString() ?: "")

        when (billingResult.responseCode) {
          BillingClient.BillingResponseCode.OK -> {

            if (skuDetailsList != null) {
              this.productsCache = this.productsCache.plus(skuDetailsList)
            }

            val items = skuDetailsList?.map { x ->
              val res = JSObject()
              res.put("title", when (x.sku) {
                "io.jok.vip_membership_1_month" -> "Month"
                "io.jok.vip_membership_6_month" -> "6 Months"
                else -> x.title
              })
              res.put("originalTitle", x.title)
              res.put("description", x.description)
              res.put("price", x.originalPrice)
              res.put("formattedPrice", x.price)
              res.put("currencySymbol", x.priceCurrencyCode)
              res.put("currencyCode", x.priceCurrencyCode)
              res.put("productIdentifier", x.sku)
              res.put("isDownloadable", true)
              res.put("downloadContentLengths", 0)
              res.put("contentVersion", "")
              res.put("downloadContentVersion", "")

              res
            } ?: emptyList()

            cb(true, items)
          }
          else -> {
            Log.w("InvalidBillingResponse", billingResult.responseCode.toString())
            cb(false, emptyList())
          }
        }
      }
    }
  }

  @PluginMethod
  fun requestPayment(call: PluginCall) {
    val productId = call.getString("productId")

    Log.i("BILLING", "requestPayment starting...")

    if (this.productsCache.isEmpty()) {
      call.reject("PRODUCTS_NOT_LOADED")
      return
    }

    if (JokHelperStatic.billingRepo?.billingClient?.isReady == true) {
      var responseCode = initRequestPayment(productId)

      Log.i("BILLING", "requestPayment finished 1")

      val ret = JSObject()
      ret.put("success", true)
      ret.put("platform", "ANDROID")
      ret.put("responseCode", responseCode)
      call.success(ret)
      return
    }


    JokHelperStatic.createBillingClient(true) {
      var responseCode = initRequestPayment(productId)

      Log.i("BILLING", "requestPayment finished 2")

      val ret = JSObject()
      ret.put("success", true)
      ret.put("platform", "ANDROID")
      ret.put("responseCode", responseCode)
      call.success(ret)
    }

  }

  private fun initRequestPayment(productId: String): Int? {
    val product = this.productsCache?.find { it.sku == productId }
    if (product == null) return null

    if (JokHelperStatic.billingRepo?.billingClient?.isReady == true) {
    }

    val flowParams = BillingFlowParams.newBuilder()
      .setSkuDetails(product)
      .build()

    return JokHelperStatic.billingRepo?.billingClient?.launchBillingFlow(JokHelperStatic.activity!!, flowParams)?.responseCode
  }

  @PluginMethod
  fun finishPayment(call: PluginCall) {
    val transactionId = call.getString("transactionId")

    Log.i("BILLING", "finishPayment starting...")

    finishPaymentWrapper(transactionId, 0) { isSuccess, errorMessage ->
      val ret = JSObject()
      ret.put("success", isSuccess)
      ret.put("message", errorMessage)
      call.success(ret)
    }
  }

  private fun finishPaymentWrapper(transactionId: String, attemptsCount: Int, cb: (success: Boolean, errorMessage: String?) -> Any) {

    if (JokHelperStatic.billingRepo?.billingClient?.isReady == true) {
      JokHelperStatic.finishTransaction(transactionId) { isSuccess, shouldRetry, errorMessage ->
        if (shouldRetry && (attemptsCount < 5)) {
          finishPaymentWrapper(transactionId, attemptsCount + 1, cb)
          false
        } else {
          Log.i("BILLING", "finishPayment finished 1 $isSuccess, $errorMessage")

          cb(isSuccess, errorMessage)

          true
        }
      }
      return
    }

    JokHelperStatic.createBillingClient(true) {
      JokHelperStatic.finishTransaction(transactionId) { isSuccess, shouldRetry, errorMessage ->

        Log.i("BILLING", "finishPayment finished 2 $isSuccess, $errorMessage")

        if (shouldRetry && (attemptsCount < 5)) {
          finishPaymentWrapper(transactionId, attemptsCount + 1, cb)
          false
        } else {
          cb(isSuccess, errorMessage)

          true
        }
      }
    }
  }

  @PluginMethod
  fun listenTransactionStateChanges(call: PluginCall) {

    JokHelperStatic.transactionsObservers.add { x ->
      val ret = JSObject()
      ret.put("transactionId", x.purchaseToken)
      ret.put("transactionState", when (x.purchaseState) {
        Purchase.PurchaseState.PURCHASED -> 1 /*Purchased*/
        Purchase.PurchaseState.PENDING -> 0 /*Purchasing*/
        else -> 4 /*Deferred*/
      })
      ret.put("transactionReceipt", x.originalJson)
      ret.put("productId", x.sku)
      ret.put("platform", "ANDROID")
      ret.put("hasError", false)
      ret.put("errorCode", "")
      ret.put("errorMessage", "")
      ret.put("platform", "ANDROID")

      this.notifyListeners("TransactionStateChange", ret)
    }

    val consumablePurchases = JokHelperStatic.billingRepo?.billingClient?.queryPurchases(INAPP)
    val subscriptionPurchases = JokHelperStatic.billingRepo?.billingClient?.queryPurchases(SUBS)

    consumablePurchases?.purchasesList?.forEach { JokHelperStatic.publishNewTransaction(it) }
    subscriptionPurchases?.purchasesList?.forEach { JokHelperStatic.publishNewTransaction(it) }

    Log.i("BILLING", "Pending transactions on startup consumable: ${consumablePurchases?.purchasesList?.size} subscription: ${subscriptionPurchases?.purchasesList?.size}")
  }

  @PluginMethod
  fun platformInfo(call: PluginCall) {

    val clientVersion = JokHelperStatic.versionName

    val ret = JSObject()
    ret.put("success", true)
    ret.put("platform", "ANDROID")
    ret.put("packageName", JokHelperStatic.activity!!.packageName)
    ret.put("isMobileApp", true)
    ret.put("clientVersion", clientVersion)
    call.success(ret)
  }

  @PluginMethod
  fun viewAppPage(call: PluginCall) {
    val appId = call.getString("appId")
//    var showReviewPage = call.getBoolean("showReviewPage", false)

    try {
      JokHelperStatic.openAppUrl(Uri.parse("market://details?id=$appId"))
    } catch (ex: ActivityNotFoundException) {
      JokHelperStatic.openAppUrl(Uri.parse("https://play.google.com/store/apps/details?id=$appId"))
    }

    val ret = JSObject()
    ret.put("success", true)
    call.success(ret)
  }

  var fxEffects = HashMap<String, MediaPlayer>()

  @PluginMethod
  fun playAudio(call: PluginCall) {
    val name = call.getString("name")

    var audioEffect = fxEffects[name]

    if (audioEffect == null) {
      audioEffect = JokHelperStatic.getAudioEffect(name)
      if (audioEffect == null) {
        val ret = JSObject()
        ret.put("value", false)
        call.success(ret)
        return
      }

      fxEffects[name] = audioEffect
    }

    if (audioEffect.isPlaying) {
      audioEffect.stop()
    }
    audioEffect.start()

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun openMailbox(call: PluginCall) {

    try {
      val intent = Intent(Intent.ACTION_MAIN)

      intent.action = Intent.ACTION_MAIN
      intent.addCategory(Intent.CATEGORY_APP_EMAIL)

      JokHelperStatic.activity?.startActivity(intent)
    } catch (err: Error) {
      val ret = JSObject()
      ret.put("value", false)
      call.success(ret)
      return
    }

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun vibrate(call: PluginCall) {

    val vibrator = JokHelperStatic.activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
      vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
      vibrator.vibrate(500)
    }

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun requestReview(call: PluginCall) {
    JokHelperStatic.showAppReviewUI()

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }
}
