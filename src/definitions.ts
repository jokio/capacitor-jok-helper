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