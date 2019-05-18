declare module "@capacitor/core" {
  interface PluginRegistry {
    JokHelper: JokHelperPlugin;
  }
}

export interface JokHelperPlugin {
  echo(options: { value: string }): Promise<{ value: string }>
  setKeychainItem(data: SetKeychainItemProps): Promise<{ value: string }>
  getKeychainItem(data: GetKeychainItemProps): Promise<{ value: string }>
}

export interface SetKeychainItemProps {
  key: string
  value: string
}

export interface GetKeychainItemProps {
  key: string
}