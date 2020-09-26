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
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat.startActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.android.billingclient.api.BillingClient.SkuType.SUBS
import com.getcapacitor.*


object SingletonClass {
  var activity: Activity? = null
  var billingClient: BillingClient? = null
  var isStoreReady: Boolean = false
  var versionName: String = ""
  var openAppUrl: (url: Uri) -> Any = { x -> x }
  var getAudioResourceId: (name: String) -> Int = { _ -> 0 }

  val transactionsObservers = mutableListOf<(Purchase) -> Unit>()

  var publishNewTransaction: (x: Purchase) -> Any = { x ->
    transactionsObservers.forEach { it(x) }
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
    val accessGroup = call.getString("accessGroup")

    var pref = SingletonClass.activity?.getPreferences(Context.MODE_PRIVATE)
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

    var pref = SingletonClass.activity?.getPreferences(Context.MODE_PRIVATE)
    var result = pref?.getString(key, null)

    val ret = JSObject()
    ret.put("value", result)
    call.success(ret)
  }

  @PluginMethod
  fun isMobileDevice(call: PluginCall) {
    var result = true

    val ret = JSObject()
    ret.put("value", result)
    call.success(ret)
  }

  @PluginMethod
  fun isWideScreen(call: PluginCall) {
    var metrics = DisplayMetrics()

    SingletonClass.activity?.windowManager?.defaultDisplay?.getMetrics(metrics)

    var result = metrics.heightPixels >= 812

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

    // NOT IMPLEMENTED

    val ret = JSObject()
    ret.put("value", false)
    call.success(ret)
  }

  @PluginMethod
  fun getPushNotificationsState(call: PluginCall) {

    // NOT IMPLEMENTED

    val ret = JSObject()
//    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun askPushNotificationsPermission(call: PluginCall) {

    // NOT IMPLEMENTED

    val ret = JSObject()
//    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun openAppSettings(call: PluginCall) {

    // NOT IMPLEMENTED

    val ret = JSObject()
    ret.put("success", false)
    call.success(ret)
  }

  @PluginMethod
  fun canMakePayments(call: PluginCall) {

    var result = SingletonClass.isStoreReady

    val ret = JSObject()
    ret.put("value", result)
    call.success(ret)
  }


  var productsCache: List<SkuDetails>? = null

  @PluginMethod
  fun loadProducts(call: PluginCall) {
    val productIds = call.getArray("productIds")
    val type = call.getString("type")

    var skyType: String = if (type == "SUBSCRIPTION") {
      INAPP
    } else {
      SUBS
    }

    val params = SkuDetailsParams.newBuilder()
    params.setSkusList(productIds.toList()).setType(skyType)

    if (SingletonClass.billingClient != null) {
      SingletonClass.billingClient?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
        when (billingResult.responseCode) {
          BillingClient.BillingResponseCode.OK -> {

            this.productsCache = skuDetailsList

            var items = skuDetailsList?.map { x ->
              var res = JSObject()
              res.put("title", x.title)
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
            }


            val ret = JSObject()
            ret.put("success", true)
            ret.put("products", JSArray(items))
            ret.put("invalidProducts", JSArray())
            call.success(ret)
          }
          else -> {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("products", JSArray())
            ret.put("invalidProducts", JSArray())
            call.success(ret)
          }
        }
      }
    }

  }


  @PluginMethod
  fun requestPayment(call: PluginCall) {
    val productId = call.getString("productId")

    if (this.productsCache == null) {
      call.reject("PRODUCTS_NOT_LOADED")
      return
    }

    var product = this.productsCache?.find { it.sku === productId }
    if (product == null) {
      call.reject("PRODUCT_NOT_FOUND")
      return
    }

    val flowParams = BillingFlowParams.newBuilder()
      .setSkuDetails(product)
      .build()

    val responseCode = SingletonClass.billingClient?.launchBillingFlow(SingletonClass.activity!!, flowParams)?.responseCode

    val ret = JSObject()
    ret.put("success", true)
    ret.put("platform", "ANDROID")
    ret.put("responseCode", responseCode)
    call.success(ret)
  }

  @PluginMethod
  fun finishPayment(call: PluginCall) {
    val transactionId = call.getString("transactionId")

    var consumablePurchases = SingletonClass.billingClient?.queryPurchases(INAPP)
    var purchase = consumablePurchases?.purchasesList?.find { it.orderId === transactionId }
    if (purchase !== null) {
      val consumeParams =
        ConsumeParams.newBuilder()
          .setPurchaseToken(purchase.purchaseToken)
          .build()

      SingletonClass.billingClient?.consumeAsync(consumeParams) { billingResult, _ ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          val ret = JSObject()
          ret.put("success", true)
          call.success(ret)
        } else {
          val ret = JSObject()
          ret.put("success", false)
          ret.put("message", billingResult.debugMessage)
          call.success(ret)
        }
      }

      return
    }

    var subscriptionPurchases = SingletonClass.billingClient?.queryPurchases(SUBS)
    purchase = subscriptionPurchases?.purchasesList?.find { it.orderId === transactionId }

    if (purchase !== null) {

      if (purchase.isAcknowledged) {
        val ret = JSObject()
        ret.put("success", true)
        call.success(ret)
      }

      val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)

      SingletonClass.billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build()) { billingResult ->

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          val ret = JSObject()
          ret.put("success", true)
          call.success(ret)
        } else {
          val ret = JSObject()
          ret.put("success", false)
          ret.put("message", billingResult.debugMessage)
          call.success(ret)
        }
      }

      return
    }


    val ret = JSObject()
    ret.put("success", false)
    ret.put("message", "Order not found")
    call.success(ret)
  }

  @PluginMethod
  fun listenTransactionStateChanges(call: PluginCall) {

    SingletonClass.transactionsObservers.add() { x ->
      val ret = JSObject()
      ret.put("transactionId", x.orderId)
      ret.put("transactionState", x.purchaseState)
      ret.put("transactionReceipt", x.purchaseToken)
      ret.put("productId", x.sku)
      ret.put("platform", "ANDROID")
      ret.put("hasError", false)
      ret.put("errorCode", "")
      ret.put("errorMessage", "")
      ret.put("platform", "ANDROID")

      this.notifyListeners("TransactionStateChange", ret)
    }

    var consumablePurchases = SingletonClass.billingClient?.queryPurchases(INAPP)
    var subscriptionPurchases = SingletonClass.billingClient?.queryPurchases(SUBS)

    consumablePurchases?.purchasesList?.forEach { SingletonClass.publishNewTransaction(it) }
    subscriptionPurchases?.purchasesList?.forEach { SingletonClass.publishNewTransaction(it) }
  }

  @PluginMethod
  fun platformInfo(call: PluginCall) {

    var clientVersion = SingletonClass.versionName

    val ret = JSObject()
    ret.put("success", true)
    ret.put("platform", "ANDROID")
    ret.put("clientVersion", clientVersion)
    call.success(ret)
  }

  @PluginMethod
  fun viewAppPage(call: PluginCall) {
    var appId = call.getString("appId")
    var showReviewPage = call.getBoolean("showReviewPage", false)

    try {
      SingletonClass.openAppUrl(Uri.parse("market://details?id=$appId"))
    } catch (ex: ActivityNotFoundException) {
      SingletonClass.openAppUrl(Uri.parse("https://play.google.com/store/apps/details?id=$appId"))
    }

    val ret = JSObject()
    ret.put("success", true)
    call.success(ret)
  }

  var fxEffects = HashMap<String, MediaPlayer>()

  @PluginMethod
  fun playAudio(call: PluginCall) {
    var name = call.getString("name")

    var audioEffect = fxEffects[name]

    if (audioEffect == null) {
      val resourceId = SingletonClass.getAudioResourceId(name)

      if (resourceId == 0) {
        val ret = JSObject()
        ret.put("value", false)
        call.success(ret)
        return
      }

      audioEffect =  MediaPlayer.create(SingletonClass.activity, resourceId);

      fxEffects[name] = audioEffect
    }

    audioEffect?.stop();
    audioEffect?.start()

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun openMailbox(call: PluginCall) {

    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_APP_EMAIL)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    SingletonClass.activity?.startActivity(intent)

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }

  @PluginMethod
  fun vibrate(call: PluginCall) {

    val vibrator = SingletonClass.activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
      vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
      vibrator.vibrate(200)
    }

    val ret = JSObject()
    ret.put("value", true)
    call.success(ret)
  }
}