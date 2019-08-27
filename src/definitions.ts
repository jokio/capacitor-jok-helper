declare module "@capacitor/core" {
  interface PluginRegistry {
    JokHelper: JokHelperPlugin;
  }
}

export interface JokHelperPlugin {
  echo(options: { value: string }): Promise<{ value: string }>
  setKeychainItem(data: SetKeychainItemProps): Promise<{ value: string }>
  getKeychainItem(data: GetKeychainItemProps): Promise<{ value: string }>
  setDeviceOrientationLock(data: SetOrientationLockProps): Promise<void>
  getDeviceOrientation(): Promise<DeviceOrientationData | null>
  listenDeviceOrientationChanges(): Promise<void>
  isWideScreen(): Promise<{ value: boolean }>
  isMobileDevice(): Promise<{ value: boolean }>
  getPushNotificationsState(): Promise<PushNotificationState | null>
  askPushNotificationsPermission(): Promise<{ accepted: boolean }>
  openAppSettings(): Promise<{ success: boolean }>
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
  OSNotificationPermissionProvisional = 3

}

export interface SetOrientationLockProps {
  orientationMask: 'all' | 'portrait' | 'portraitUpsideDown' | 'landscape' | 'landscapeLeft' | 'landscapeRight' | 'allButUpsideDown'
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
  DeviceOrientationChange = 'DeviceOrientationChange'
}