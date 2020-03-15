declare module '@capacitor/core' {
  interface PluginRegistry {
    JokHelper: JokHelperPlugin
  }
}

export interface JokHelperPlugin {
  echo(options: { value: string }): Promise<{ value: string }>
  setKeychainItem(
    data: SetKeychainItemProps,
  ): Promise<{ value: string }>
  getKeychainItem(
    data: GetKeychainItemProps,
  ): Promise<{ value: string }>
  setDeviceOrientationLock(
    data: SetOrientationLockProps,
  ): Promise<void>
  getDeviceOrientation(): Promise<DeviceOrientationData | null>
  listenDeviceOrientationChanges(): Promise<void>
  listenPushNotificationEvents(): Promise<void>
  isWideScreen(): Promise<{ value: boolean }>
  isMobileDevice(): Promise<{ value: boolean }>
  getPushNotificationsState(): Promise<PushNotificationState | null>
  askPushNotificationsPermission(): Promise<{ accepted: boolean }>
  openAppSettings(): Promise<{ success: boolean }>
  canMakePayments(): Promise<{ value: boolean }>
  loadProducts(
    data: LoadProductsProps,
  ): Promise<{
    success: boolean
    products: SKProduct[]
    invalidProducts: string[]
  }>
  requestPayment(
    data: RequestPaymentProps,
  ): Promise<{ success: boolean; message: string }>

  finishPayment(
    data: FinishPaymentProps,
  ): Promise<{ success: boolean; message: string }>

  listenTransactionStateChanges(): Promise<void>

  platformInfo(): Promise<{
    success: boolean
    platform: string
    clientVersion: string
  }>

  viewAppPage(data: ViewAppPageProps): Promise<{ value: boolean }>
}

export interface ViewAppPageProps {
  appId: string
  showReviewPage: boolean
}

export interface LoadProductsProps {
  productIds: string[]
}

export interface SKProduct {
  title: string
  description: string
  price: number
  formattedPrice: string
  currencySymbol: string
  currencyCode: string
  productIdentifier: string
  isDownloadable: boolean
  downloadContentLengths: number
  contentVersion: string
  downloadContentVersion: string
}

export interface RequestPaymentProps {
  productId: string
}

export interface FinishPaymentProps {
  transactionId: string
}

export interface SetKeychainItemProps {
  key: string
  value: string
  accessGroup?: string
}

export interface GetKeychainItemProps {
  key: string
  accessGroup?: string
}

export interface PushNotificationState {
  userId: string
  hasPrompted: boolean
  userStatus: PushNotificationStateUserStatus
  pushToken: string // "74b3b61d835351754367e926ae18be50091ab6d5f40cd8870f5ec5db291c7e77"
  isSubscribed: boolean
  userSubscriptionSetting: boolean
}

export enum PushNotificationStateUserStatus {
  // The user has not yet made a choice regarding whether your app can show notifications.
  OSNotificationPermissionNotDetermined = 0,

  // The application is not authorized to post user notifications.
  OSNotificationPermissionDenied = 1,

  // The application is authorized to post user notifications.
  OSNotificationPermissionAuthorized = 2,

  // the application is only authorized to post Provisional notifications (direct to history)
  OSNotificationPermissionProvisional = 3,
}

export interface SetOrientationLockProps {
  orientationMask:
    | 'all'
    | 'portrait'
    | 'portraitUpsideDown'
    | 'landscape'
    | 'landscapeLeft'
    | 'landscapeRight'
    | 'allButUpsideDown'
  preferredOrientation: InterfaceOrientation
}

export enum InterfaceOrientation {
  unknown = 0,
  portrait = 1,
  portraitUpsideDown = 2,
  landscapeLeft = 3,
  landscapeRight = 4,
}

export interface DeviceOrientationChangeData {
  isPortrait: boolean
  isLandscape: boolean
  isFlat: boolean
  rawValue: number
}

export interface DeviceOrientationData {
  isPortrait: boolean
  isLandscape: boolean
  isFlat: boolean
  rawValue: number
  orientation: string
}

export enum JokPluginEvents {
  DeviceOrientationChange = 'DeviceOrientationChange',
}

export interface TransactionStateChangeData {
  transactionId: string
  transactionState: TransactionState
  transactionReceipt: string
  productId: string
  hasError: boolean
  errorCode?: TransactionErrorCode
  errorMessage?: string
}

export enum TransactionState {
  // Transaction is being added to the server queue.
  Purchasing = 0,

  // Transaction is in queue, user has been charged.  Client should complete the transaction.
  Purchased = 1,

  // Transaction was cancelled or failed before being added to the server queue.
  Failed = 2,

  // Transaction was restored from user's purchase history.  Client should complete the transaction.
  Restored = 3,

  // The transaction is in the queue, but its final status is pending external action.
  Deferred = 4,
}

export enum TransactionErrorCode {
  unknown = 0,

  // client is not allowed to issue the request, etc.
  clientInvalid = 1,

  // user cancelled the request, etc.
  paymentCancelled = 2,

  // purchase identifier was invalid, etc.
  paymentInvalid = 3,

  // this device is not allowed to make the payment
  paymentNotAllowed = 4,

  // Product is not available in the current storefront
  storeProductNotAvailable = 5,

  // user has not allowed access to cloud service information
  cloudServicePermissionDenied = 6,

  // the device could not connect to the nework
  cloudServiceNetworkConnectionFailed = 7,

  // user has revoked permission to use this cloud service
  cloudServiceRevoked = 8,

  // The user needs to acknowledge Apple's privacy policy
  privacyAcknowledgementRequired = 9,

  // The app is attempting to use SKPayment's requestData property, but does not have the appropriate entitlement
  unauthorizedRequestData = 10,

  // The specified subscription offer identifier is not valid
  invalidOfferIdentifier = 11,

  // The cryptographic signature provided is not valid
  invalidSignature = 12,

  // One or more parameters from SKPaymentDiscount is missing
  missingOfferParams = 13,

  // The price of the selected offer is not valid (e.g. lower than the current base subscription price)
  invalidOfferPrice = 14,
}
