import Foundation
import Capacitor
import UIKit
import Dispatch
import StoreKit

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(JokHelper)
public class JokHelper: CAPPlugin {
    
    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.success([
            "value": value
        ])
    }

    @objc func setKeychainItem(_ call: CAPPluginCall) {
        let key = call.getString("key") ?? ""
        let value = call.getString("value") ?? ""
        let accessGroup = call.getString("accessGroup") ?? ""

        let keychain = KeychainSwift()
        if (accessGroup != "") {
            keychain.accessGroup = accessGroup
        }
        
        keychain.synchronizable = true
        keychain.set(value, forKey: key)

        call.success([
            "value": value
        ])
    }
    
    @objc func getKeychainItem(_ call: CAPPluginCall) {
        let key = call.getString("key") ?? ""
        let accessGroup = call.getString("accessGroup") ?? ""
        
        let keychain = KeychainSwift()
        keychain.synchronizable = true
        if (accessGroup != "") {
            keychain.accessGroup = accessGroup
        }

        let value = keychain.get(key)
        
        call.success([
            "value": value
        ])
    }
    
    @objc func setDeviceOrientationLock(_ call: CAPPluginCall) {
        let orientationMaskString = call.getString("orientationMask", "")
        let preferredOrientationRawValue = call.getInt("preferredOrientation", 0)
        
        var orientationMask: UIInterfaceOrientationMask
        var preferredOrientation: UIInterfaceOrientation
        
        switch orientationMaskString {
        case "all":
            orientationMask = UIInterfaceOrientationMask.all
        case "portrait":
            orientationMask = UIInterfaceOrientationMask.portrait
        case "portraitUpsideDown":
            orientationMask = UIInterfaceOrientationMask.portraitUpsideDown
        case "landscape":
            orientationMask = UIInterfaceOrientationMask.landscape
        case "landscapeLeft":
            orientationMask = UIInterfaceOrientationMask.landscapeLeft
        case "landscapeRight":
            orientationMask = UIInterfaceOrientationMask.landscapeRight
        case "allButUpsideDown":
            orientationMask = UIInterfaceOrientationMask.allButUpsideDown
        default:
            orientationMask = UIInterfaceOrientationMask.all
        }

        switch preferredOrientationRawValue {
        case UIInterfaceOrientation.portrait.rawValue:
            preferredOrientation = UIInterfaceOrientation.portrait

        case UIInterfaceOrientation.portraitUpsideDown.rawValue:
            preferredOrientation = UIInterfaceOrientation.portraitUpsideDown

        case UIInterfaceOrientation.landscapeLeft.rawValue:
            preferredOrientation = UIInterfaceOrientation.landscapeLeft

        case UIInterfaceOrientation.landscapeRight.rawValue:
            preferredOrientation = UIInterfaceOrientation.landscapeRight

        default:
            preferredOrientation = UIInterfaceOrientation.unknown
        }
        
        
        NotificationCenter.default.post(name: Notification.Name("SET_ORIENTATION_LOCK"), object: nil, userInfo: [
            "orientationLock": orientationMask,
            "preferredOrientation": preferredOrientation
            ])

        call.success([
            "value": true
            ])
    }
    
    @objc func getDeviceOrientation(_ call: CAPPluginCall) {
        
        let orientation: String
        let currentOrientation = UIDevice.current.orientation

        switch currentOrientation {
            case UIDeviceOrientation.portrait:
                orientation = "portrait"
            case UIDeviceOrientation.portraitUpsideDown:
                orientation = "portraitUpsideDown"
            case UIDeviceOrientation.landscapeLeft:
                orientation = "landscapeLeft"
            case UIDeviceOrientation.landscapeRight:
                orientation = "landscapeRight"
            case UIDeviceOrientation.unknown:
                orientation = "unknown"
            default:
                orientation = "unknown"
        }

        call.success([
            "isPortrait":currentOrientation.isPortrait,
            "isFlat":currentOrientation.isFlat,
            "isLandscape":currentOrientation.isLandscape,
            "rawValue": currentOrientation.rawValue,
            "orientation": orientation
        ])
    }
    
    @objc func listenDeviceOrientationChanges(_ call: CAPPluginCall) {
        
        NotificationCenter.default.addObserver(forName: Notification.Name("ORIENTATION_CHANGE"), object: nil, queue: OperationQueue.main) { (notification) in
            
            if let data = notification.userInfo
            {
                self.notifyListeners("DeviceOrientationChange", data: [
                    "isLandscape" : data["isLandscape"],
                    "isFlat" : data["isFlat"],
                    "isPortrait" : data["isPortrait"],
                    "rawValue" : data["rawValue"]
                ])
            }
        }
        
        call.success([
            "value": true
            ])
    }
    
    @objc func listenPushNotificationEvents(_ call: CAPPluginCall) {
        
        NotificationCenter.default.addObserver(forName: Notification.Name("PUSH_NOTIFICATION_EVENT"), object: nil, queue: OperationQueue.main) { (notification) in
            
            if let data = notification.userInfo
            {
                self.notifyListeners("appPushNotificationEvent", data: data as? [String : Any])
            }
        }
        
        call.success([
            "value": true
            ])
    }
    
    @objc func isWideScreen(_ call: CAPPluginCall) {
        
        let result = UIScreen.main.bounds.height >= 812
        
        call.success([
            "value": result
            ])
    }

    @objc func isMobileDevice(_ call: CAPPluginCall) {
        
        let result = UIDevice.current.userInterfaceIdiom == .phone

        call.success([
            "value": result
            ])
    }

    @objc func getPushNotificationsState(_ call:CAPPluginCall) {
      
        NotificationCenter.default.post(name: Notification.Name("getPushNotificationsStateRequest"), object: nil, userInfo: [:])

        NotificationCenter.default.addObserver(forName: Notification.Name("getPushNotificationsStateResult"), object: nil, queue: OperationQueue.main) { (notification) in
            
            if let data = notification.userInfo
            {
                call.success(data as! [String:Any])
            }
        }

    }
    
    @objc func askPushNotificationsPermission(_ call:CAPPluginCall) {
        NotificationCenter.default.post(name: Notification.Name("askPushNotificationsPermissionRequest"), object: nil, userInfo: [:])
        
        NotificationCenter.default.addObserver(forName: Notification.Name("askPushNotificationsPermissionResult"), object: nil, queue: OperationQueue.main) { (notification) in
            
            if let data = notification.userInfo
            {
                call.success(data as! [String:Any])
            }
        }
    }

    @objc func openAppSettings(_ call:CAPPluginCall) {
        
        guard let settingsUrl = URL(string: UIApplication.openSettingsURLString) else {
            return
        }
        
        if UIApplication.shared.canOpenURL(settingsUrl) {
            UIApplication.shared.open(settingsUrl, completionHandler: { (success) in
                print("Settings opened: \(success)") // Prints true
            })
        }

        call.success([
            "success": true
            ])
    }
    
    @objc func canMakePayments(_ call: CAPPluginCall) {

        call.success([
            "value": SKPaymentQueue.canMakePayments()
        ])
        
    }
    
    
    var productsResultDelegate: ProductsResultDelegate?
    var products: [SKProduct] = []
    
    @objc func loadProducts(_ call: CAPPluginCall) {
        let identifiers = call.get("productIds", [String].self)
        
        // Create a set for the product identifiers.
        let productIdentifiers = Set<String>(identifiers!)

        self.productsResultDelegate = ProductsResultDelegate(productsReceived: { (response) in
            
            self.products = response.products
            
            call.success([
                "success": true,
                "products": response.products.map({(product: SKProduct) in
                    return [
                        "localizedDescription": product.localizedDescription,
                        "localizedTitle": product.description,
                        "price": product.price,
                        "formattedPrice": product.formattedPrice,
                        "currencySymbol": product.priceLocale.currencySymbol!,
                        "currencyCode": product.priceLocale.currencyCode!,
                        "productIdentifier": product.productIdentifier,
                        "isDownloadable": product.isDownloadable,
                        "downloadContentLengths": product.downloadContentLengths,
                        "contentVersion": product.contentVersion,
                        "downloadContentVersion": product.downloadContentVersion
                    ]
                }),
                "invalidProducts": response.invalidProductIdentifiers
            ])
            
            self.productsResultDelegate = nil
        })
        
        // Initialize the product request with the above identifiers.
        let productRequest = SKProductsRequest(productIdentifiers: productIdentifiers)
        productRequest.delegate = self.productsResultDelegate
        
        // Send the request to the App Store.
        productRequest.start()
    }
    
    @objc func requestPayment(_ call: CAPPluginCall) {

        let identifier = call.getString("productId")
        
        if let product = self.products.first(where: { $0.productIdentifier == identifier }) {
            
            let payment = SKMutablePayment(product: product)
            payment.quantity = 1
            
            SKPaymentQueue.default().add(payment)

            call.success([
                "success": true
            ])
            
            return
        }
        
        call.success([
            "success": false,
            "message": "Product not loaded, please call getProducts first"
        ])
    }
    
    @objc func finishPayment(_ call: CAPPluginCall) {
        
        let transactionId = call.getString("transactionId")
        
        let transactions =  SKPaymentQueue.default().transactions
        
        if let transaction = transactions.first(where: {$0.transactionIdentifier == transactionId}) {
            SKPaymentQueue.default().finishTransaction(transaction)
            
            call.success([
                "success": true
            ])
            return
        }
        
        call.success([
            "success": false,
            "message": "Transaction not found"
        ])
    }
    
    @objc func listenTransactionStateChanges(_ call: CAPPluginCall) {
        
        NotificationCenter.default.addObserver(forName: Notification.Name("TRANSACTION_STATE_CHANGE"), object: nil, queue: OperationQueue.main) { (notification) in
            
            if let data = notification.userInfo
            {
                self.notifyListeners("TransactionStateChange", data: data as! [String : Any])
            }
        }
        
        let transactions = SKPaymentQueue.default().transactions
        
        transactions.forEach { transaction in
            var errorMessage: String = ""
            var errorCode: SKError.Code?
            var hasError = false
            
            if let error = transaction.error {
                errorMessage = error.localizedDescription
                errorCode =  (transaction.error as? SKError)!.code
                hasError = true
            }

            NotificationCenter.default.post(name: Notification.Name("TRANSACTION_STATE_CHANGE"), object: nil, userInfo: [
                "transactionId": transaction.transactionIdentifier,
                "transactionState": transaction.transactionState.rawValue,
                "productId": transaction.payment.productIdentifier,
                "hasError": hasError,
                "errorCode": (errorCode == nil) ? "" : errorCode!.rawValue,
                "errorMessage": errorMessage
            ])
        }
      
        call.success([
            "value": true
            ])
    }
}

public class ProductsResultDelegate: NSObject, SKProductsRequestDelegate {
    var productsReceived: (SKProductsResponse)->()
    
    init(
        productsReceived:@escaping (SKProductsResponse)->()
    ) {
        self.productsReceived = productsReceived
    }

    public func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse){
        self.productsReceived(response)
    }
}

extension SKProduct {
    /// - returns: The cost of the product formatted in the local currency.
    var formattedPrice: String? {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = self.priceLocale
        return formatter.string(from: self.price)
    }
}


/**
 A collection of helper functions for saving text and data in the keychain.
 */
open class KeychainSwift {
    
    var lastQueryParameters: [String: Any]? // Used by the unit tests
    
    /// Contains result code from the last operation. Value is noErr (0) for a successful result.
    open var lastResultCode: OSStatus = noErr
    
    var keyPrefix = "" // Can be useful in test.
    
    /**
     Specify an access group that will be used to access keychain items. Access groups can be used to share keychain items between applications. When access group value is nil all application access groups are being accessed. Access group name is used by all functions: set, get, delete and clear.
     */
    open var accessGroup: String?
    
    
    /**
     
     Specifies whether the items can be synchronized with other devices through iCloud. Setting this property to true will
     add the item to other devices with the `set` method and obtain synchronizable items with the `get` command. Deleting synchronizable items will remove them from all devices. In order for keychain synchronization to work the user must enable "Keychain" in iCloud settings.
     
     Does not work on macOS.
     
     */
    open var synchronizable: Bool = false
    
    private let readLock = NSLock()
    
    /// Instantiate a KeychainSwift object
    public init() { }
    
    /**
     
     - parameter keyPrefix: a prefix that is added before the key in get/set methods. Note that `clear` method still clears everything from the Keychain.
     */
    public init(keyPrefix: String) {
        self.keyPrefix = keyPrefix
    }
    
    /**
     
     Stores the text value in the keychain item under the given key.
     
     - parameter key: Key under which the text value is stored in the keychain.
     - parameter value: Text string to be written to the keychain.
     - parameter withAccess: Value that indicates when your app needs access to the text in the keychain item. By default the .AccessibleWhenUnlocked option is used that permits the data to be accessed only while the device is unlocked by the user.
     
     - returns: True if the text was successfully written to the keychain.
     */
    @discardableResult
    open func set(_ value: String, forKey key: String,
                  withAccess access: KeychainSwiftAccessOptions? = nil) -> Bool {
        
        if let value = value.data(using: String.Encoding.utf8) {
            return set(value, forKey: key, withAccess: access)
        }
        
        return false
    }
    
    /**
     
     Stores the data in the keychain item under the given key.
     
     - parameter key: Key under which the data is stored in the keychain.
     - parameter value: Data to be written to the keychain.
     - parameter withAccess: Value that indicates when your app needs access to the text in the keychain item. By default the .AccessibleWhenUnlocked option is used that permits the data to be accessed only while the device is unlocked by the user.
     
     - returns: True if the text was successfully written to the keychain.
     
     */
    @discardableResult
    open func set(_ value: Data, forKey key: String,
                  withAccess access: KeychainSwiftAccessOptions? = nil) -> Bool {
        
        delete(key) // Delete any existing key before saving it
        let accessible = access?.value ?? KeychainSwiftAccessOptions.defaultOption.value
        
        let prefixedKey = keyWithPrefix(key)
        
        var query: [String : Any] = [
            KeychainSwiftConstants.klass       : kSecClassGenericPassword,
            KeychainSwiftConstants.attrAccount : prefixedKey,
            KeychainSwiftConstants.valueData   : value,
            KeychainSwiftConstants.accessible  : accessible,
            KeychainSwiftConstants.attrLabel   : key
        ]
        
        query = addAccessGroupWhenPresent(query)
        query = addSynchronizableIfRequired(query, addingItems: true)
        lastQueryParameters = query
        
        lastResultCode = SecItemAdd(query as CFDictionary, nil)
        
        if (lastResultCode != noErr) {
            print("Error saving to Keychain: \(lastResultCode)")
        }
        
        return lastResultCode == noErr
    }
    
    /**
     Stores the boolean value in the keychain item under the given key.
     - parameter key: Key under which the value is stored in the keychain.
     - parameter value: Boolean to be written to the keychain.
     - parameter withAccess: Value that indicates when your app needs access to the value in the keychain item. By default the .AccessibleWhenUnlocked option is used that permits the data to be accessed only while the device is unlocked by the user.
     - returns: True if the value was successfully written to the keychain.
     */
    @discardableResult
    open func set(_ value: Bool, forKey key: String,
                  withAccess access: KeychainSwiftAccessOptions? = nil) -> Bool {
        
        let bytes: [UInt8] = value ? [1] : [0]
        let data = Data(bytes)
        
        return set(data, forKey: key, withAccess: access)
    }
    
    /**
     
     Retrieves the text value from the keychain that corresponds to the given key.
     
     - parameter key: The key that is used to read the keychain item.
     - returns: The text value from the keychain. Returns nil if unable to read the item.
     
     */
    open func get(_ key: String) -> String? {
        if let data = getData(key) {
            
            if let currentString = String(data: data, encoding: .utf8) {
                return currentString
            }
            
            lastResultCode = -67853 // errSecInvalidEncoding
        }
        
        return nil
    }
    
    /**
     
     Retrieves the data from the keychain that corresponds to the given key.
     
     - parameter key: The key that is used to read the keychain item.
     - parameter asReference: If true, returns the data as reference (needed for things like NEVPNProtocol).
     - returns: The text value from the keychain. Returns nil if unable to read the item.
     
     */
    open func getData(_ key: String, asReference: Bool = false) -> Data? {
        // The lock prevents the code to be run simlultaneously
        // from multiple threads which may result in crashing
        readLock.lock()
        defer { readLock.unlock() }
        
        let prefixedKey = keyWithPrefix(key)
        
        var query: [String: Any] = [
            KeychainSwiftConstants.klass       : kSecClassGenericPassword,
            KeychainSwiftConstants.attrAccount : prefixedKey,
            KeychainSwiftConstants.matchLimit  : kSecMatchLimitOne
        ]
        
        if asReference {
            query[KeychainSwiftConstants.returnReference] = kCFBooleanTrue
        } else {
            query[KeychainSwiftConstants.returnData] =  kCFBooleanTrue
        }
        
        query = addAccessGroupWhenPresent(query)
        query = addSynchronizableIfRequired(query, addingItems: false)
        lastQueryParameters = query
        
        var result: AnyObject?
        
        lastResultCode = withUnsafeMutablePointer(to: &result) {
            SecItemCopyMatching(query as CFDictionary, UnsafeMutablePointer($0))
        }
        
        if lastResultCode == noErr {
            return result as? Data
        }
        
        return nil
    }
    
    /**
     Retrieves the boolean value from the keychain that corresponds to the given key.
     - parameter key: The key that is used to read the keychain item.
     - returns: The boolean value from the keychain. Returns nil if unable to read the item.
     */
    open func getBool(_ key: String) -> Bool? {
        guard let data = getData(key) else { return nil }
        guard let firstBit = data.first else { return nil }
        return firstBit == 1
    }
    
    /**
     Deletes the single keychain item specified by the key.
     
     - parameter key: The key that is used to delete the keychain item.
     - returns: True if the item was successfully deleted.
     
     */
    @discardableResult
    open func delete(_ key: String) -> Bool {
        let prefixedKey = keyWithPrefix(key)
        
        var query: [String: Any] = [
            KeychainSwiftConstants.klass       : kSecClassGenericPassword,
            KeychainSwiftConstants.attrAccount : prefixedKey
        ]
        
        query = addAccessGroupWhenPresent(query)
        query = addSynchronizableIfRequired(query, addingItems: false)
        lastQueryParameters = query
        
        lastResultCode = SecItemDelete(query as CFDictionary)
        
        return lastResultCode == noErr
    }
    
    /**
     
     Deletes all Keychain items used by the app. Note that this method deletes all items regardless of the prefix settings used for initializing the class.
     
     - returns: True if the keychain items were successfully deleted.
     
     */
    @discardableResult
    open func clear() -> Bool {
        var query: [String: Any] = [ kSecClass as String : kSecClassGenericPassword ]
        query = addAccessGroupWhenPresent(query)
        query = addSynchronizableIfRequired(query, addingItems: false)
        lastQueryParameters = query
        
        lastResultCode = SecItemDelete(query as CFDictionary)
        
        return lastResultCode == noErr
    }
    
    /// Returns the key with currently set prefix.
    func keyWithPrefix(_ key: String) -> String {
        return "\(keyPrefix)\(key)"
    }
    
    func addAccessGroupWhenPresent(_ items: [String: Any]) -> [String: Any] {
        guard let accessGroup = accessGroup else { return items }
        
        var result: [String: Any] = items
        result[KeychainSwiftConstants.accessGroup] = accessGroup
        return result
    }
    
    /**
     
     Adds kSecAttrSynchronizable: kSecAttrSynchronizableAny` item to the dictionary when the `synchronizable` property is true.
     
     - parameter items: The dictionary where the kSecAttrSynchronizable items will be added when requested.
     - parameter addingItems: Use `true` when the dictionary will be used with `SecItemAdd` method (adding a keychain item). For getting and deleting items, use `false`.
     
     - returns: the dictionary with kSecAttrSynchronizable item added if it was requested. Otherwise, it returns the original dictionary.
     
     */
    func addSynchronizableIfRequired(_ items: [String: Any], addingItems: Bool) -> [String: Any] {
        if !synchronizable { return items }
        var result: [String: Any] = items
        result[KeychainSwiftConstants.attrSynchronizable] = addingItems == true ? true : kSecAttrSynchronizableAny
        return result
    }
}

/**
 These options are used to determine when a keychain item should be readable. The default value is AccessibleWhenUnlocked.
 */
public enum KeychainSwiftAccessOptions {
    
    /**
     
     The data in the keychain item can be accessed only while the device is unlocked by the user.
     
     This is recommended for items that need to be accessible only while the application is in the foreground. Items with this attribute migrate to a new device when using encrypted backups.
     
     This is the default value for keychain items added without explicitly setting an accessibility constant.
     
     */
    case accessibleWhenUnlocked
    
    /**
     
     The data in the keychain item can be accessed only while the device is unlocked by the user.
     
     This is recommended for items that need to be accessible only while the application is in the foreground. Items with this attribute do not migrate to a new device. Thus, after restoring from a backup of a different device, these items will not be present.
     
     */
    case accessibleWhenUnlockedThisDeviceOnly
    
    /**
     
     The data in the keychain item cannot be accessed after a restart until the device has been unlocked once by the user.
     
     After the first unlock, the data remains accessible until the next restart. This is recommended for items that need to be accessed by background applications. Items with this attribute migrate to a new device when using encrypted backups.
     
     */
    case accessibleAfterFirstUnlock
    
    /**
     
     The data in the keychain item cannot be accessed after a restart until the device has been unlocked once by the user.
     
     After the first unlock, the data remains accessible until the next restart. This is recommended for items that need to be accessed by background applications. Items with this attribute do not migrate to a new device. Thus, after restoring from a backup of a different device, these items will not be present.
     
     */
    case accessibleAfterFirstUnlockThisDeviceOnly
    
    /**
     
     The data in the keychain item can always be accessed regardless of whether the device is locked.
     
     This is not recommended for application use. Items with this attribute migrate to a new device when using encrypted backups.
     
     */
    case accessibleAlways
    
    /**
     
     The data in the keychain can only be accessed when the device is unlocked. Only available if a passcode is set on the device.
     
     This is recommended for items that only need to be accessible while the application is in the foreground. Items with this attribute never migrate to a new device. After a backup is restored to a new device, these items are missing. No items can be stored in this class on devices without a passcode. Disabling the device passcode causes all items in this class to be deleted.
     
     */
    case accessibleWhenPasscodeSetThisDeviceOnly
    
    /**
     
     The data in the keychain item can always be accessed regardless of whether the device is locked.
     
     This is not recommended for application use. Items with this attribute do not migrate to a new device. Thus, after restoring from a backup of a different device, these items will not be present.
     
     */
    case accessibleAlwaysThisDeviceOnly
    
    static var defaultOption: KeychainSwiftAccessOptions {
        return .accessibleWhenUnlocked
    }
    
    var value: String {
        switch self {
        case .accessibleWhenUnlocked:
            return toString(kSecAttrAccessibleWhenUnlocked)
            
        case .accessibleWhenUnlockedThisDeviceOnly:
            return toString(kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
            
        case .accessibleAfterFirstUnlock:
            return toString(kSecAttrAccessibleAfterFirstUnlock)
            
        case .accessibleAfterFirstUnlockThisDeviceOnly:
            return toString(kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
            
        case .accessibleAlways:
            return toString(kSecAttrAccessibleAlways)
            
        case .accessibleWhenPasscodeSetThisDeviceOnly:
            return toString(kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly)
            
        case .accessibleAlwaysThisDeviceOnly:
            return toString(kSecAttrAccessibleAlwaysThisDeviceOnly)
        }
    }
    
    func toString(_ value: CFString) -> String {
        return KeychainSwiftConstants.toString(value)
    }
}

/// Constants used by the library
public struct KeychainSwiftConstants {
    /// Specifies a Keychain access group. Used for sharing Keychain items between apps.
    public static var accessGroup: String { return toString(kSecAttrAccessGroup) }
    
    /**
     
     A value that indicates when your app needs access to the data in a keychain item. The default value is AccessibleWhenUnlocked. For a list of possible values, see KeychainSwiftAccessOptions.
     
     */
    public static var accessible: String { return toString(kSecAttrAccessible) }
    
    /// Used for specifying a String key when setting/getting a Keychain value.
    public static var attrAccount: String { return toString(kSecAttrAccount) }
    
    /// Used for specifying a String key when setting/getting a Keychain value.
    public static var attrLabel: String { return toString(kSecAttrLabel) }
    
    /// Used for specifying synchronization of keychain items between devices.
    public static var attrSynchronizable: String { return toString(kSecAttrSynchronizable) }
    
    /// An item class key used to construct a Keychain search dictionary.
    public static var klass: String { return toString(kSecClass) }
    
    /// Specifies the number of values returned from the keychain. The library only supports single values.
    public static var matchLimit: String { return toString(kSecMatchLimit) }
    
    /// A return data type used to get the data from the Keychain.
    public static var returnData: String { return toString(kSecReturnData) }
    
    /// Used for specifying a value when setting a Keychain value.
    public static var valueData: String { return toString(kSecValueData) }
    
    /// Used for returning a reference to the data from the keychain
    public static var returnReference: String { return toString(kSecReturnPersistentRef) }
    
    static func toString(_ value: CFString) -> String {
        return value as String
    }
}
