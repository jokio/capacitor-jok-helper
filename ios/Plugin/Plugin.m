#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(JokHelper, "JokHelper",
           CAP_PLUGIN_METHOD(echo, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setKeychainItem, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getKeychainItem, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setDeviceOrientationLock, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getDeviceOrientation, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(listenDeviceOrientationChanges, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(listenPushNotificationEvents, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(isWideScreen, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getPushNotificationsState, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(askPushNotificationsPermission, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(openAppSettings, CAPPluginReturnPromise);
           
           // Payments
           CAP_PLUGIN_METHOD(canMakePayments, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(loadProducts, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(requestPayment, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(finishPayment, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(listenTransactionStateChanges, CAPPluginReturnPromise);

           CAP_PLUGIN_METHOD(platformInfo, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(viewAppPage, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(playAudio, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(openMailbox, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(vibrate, CAPPluginReturnPromise);
           
           CAP_PLUGIN_METHOD(requestReview, CAPPluginReturnPromise);
)

