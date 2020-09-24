package io.jok.helper;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin()
public class JokHelper extends Plugin {

    @PluginMethod()
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.success(ret);
    }
}



//  @PluginMethod
//   fun echo(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun setKeychainItem(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun getKeychainItem(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun listenPushNotificationEvents(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun isWideScreen(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun isMobileDevice(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun getPushNotificationsState(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun askPushNotificationsPermission(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun canMakePayments(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun loadProducts(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun requestPayment(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun finishPayment(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun listenTransactionStateChanges(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun platformInfo(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun viewAppPage(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun playAudio(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun openMailbox(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

//   @PluginMethod
//   fun vibrate(call: PluginCall) {
//     val value = call.getString("value")
//     val ret = JSObject()
//     ret.put("value", value)
//     call.success(ret)
//   }

