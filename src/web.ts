import { WebPlugin } from '@capacitor/core';
import { JokHelperPlugin, SetKeychainItemProps, SetOrientationLockProps } from './definitions';

export class JokHelperWeb extends WebPlugin implements JokHelperPlugin {
  constructor() {
    super({
      name: 'JokHelper',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async setKeychainItem({ key, value }: SetKeychainItemProps) {
    localStorage.setItem(key, value)

    return { value }
  }

  async getKeychainItem({ key }: SetKeychainItemProps) {
    const value = localStorage.getItem(key)

    return { value }
  }

  async setOrientationLock({ orientationMask }: SetOrientationLockProps) {
    console.log('setOrientationLock', orientationMask)
  }

  async listenDeviceOrientationChanges() {
    console.log('listenDeviceOrientationChanges')
  }
}

const JokHelper = new JokHelperWeb();

export { JokHelper };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(JokHelper);
