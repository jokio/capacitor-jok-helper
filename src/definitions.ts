declare module "@capacitor/core" {
  interface PluginRegistry {
    JokHelper: JokHelperPlugin;
  }
}

export interface JokHelperPlugin {
  echo(options: { value: string }): Promise<{ value: string }>
  setKeychainItem(data: SetKeychainItemProps): Promise<{ value: string }>
  getKeychainItem(data: GetKeychainItemProps): Promise<{ value: string }>
  setOrientationLock(data: SetOrientationLockProps): Promise<void>
  listenDeviceOrientationChanges(): Promise<void>
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
  orientationRawValue: number
}

export enum JokPluginEvents {
  DeviceOrientationChange = 'DeviceOrientationChange'
}