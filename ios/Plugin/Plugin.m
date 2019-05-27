#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(JokHelper, "JokHelper",
           CAP_PLUGIN_METHOD(echo, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setKeychainItem, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getKeychainItem, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setOrientationLock, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(listenDeviceOrientationChanges, CAPPluginReturnPromise);
)
