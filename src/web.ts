import { WebPlugin } from '@capacitor/core'
import {
  JokHelperPlugin,
  SetKeychainItemProps,
  SetOrientationLockProps,
  LoadProductsProps,
  SKProduct,
  RequestPaymentProps,
  FinishPaymentProps,
  ViewAppPageProps,
  PlayAudioProps,
} from './definitions'

export class JokHelperWeb
  extends WebPlugin
  implements JokHelperPlugin {
  constructor() {
    super({
      name: 'JokHelper',
      platforms: ['web'],
    })
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options)
    return options
  }

  async setKeychainItem({ key, value }: SetKeychainItemProps) {
    localStorage.setItem(key, value)

    return { value }
  }

  async getKeychainItem({ key }: SetKeychainItemProps) {
    const value = localStorage.getItem(key)

    return { value }
  }

  async setDeviceOrientationLock({
    orientationMask,
  }: SetOrientationLockProps) {
    console.log('setOrientationLock', orientationMask)
  }

  async getDeviceOrientation() {
    console.log('getDeviceOrientation')

    return <any>null
  }

  async listenDeviceOrientationChanges() {
    console.log('listenDeviceOrientationChanges')
  }

  async listenPushNotificationEvents() {
    console.log('listenPushNotificationEvents')
  }

  isWideScreen() {
    return Promise.resolve({ value: false })
  }

  isMobileDevice() {
    return Promise.resolve({ value: false })
  }

  getPushNotificationsState() {
    return Promise.resolve(null)
  }

  askPushNotificationsPermission() {
    return Promise.resolve({ accepted: false })
  }

  openAppSettings() {
    return Promise.resolve({ success: false })
  }

  canMakePayments(): Promise<{ value: boolean }> {
    return Promise.resolve({ value: false })
  }

  loadProducts(
    _: LoadProductsProps,
  ): Promise<{
    success: boolean
    products: SKProduct[]
    invalidProducts: string[]
  }> {
    return Promise.resolve({
      success: false,
      products: [],
      invalidProducts: [],
    })
  }

  requestPayment(
    _: RequestPaymentProps,
  ): Promise<{ success: boolean; message: string }> {
    return Promise.resolve({
      success: false,
      message: '',
    })
  }

  finishPayment(
    _: FinishPaymentProps,
  ): Promise<{ success: boolean; message: string }> {
    return Promise.resolve({
      success: false,
      message: '',
    })
  }

  listenTransactionStateChanges(): Promise<void> {
    return Promise.resolve(null)
  }

  platformInfo(): Promise<{
    success: boolean
    platform: string
    clientVersion: string
  }> {
    return Promise.resolve({
      success: true,
      platform: 'WEB',
      clientVersion: '',
    })
  }

  viewAppPage(_data: ViewAppPageProps): Promise<{ value: boolean }> {
    return Promise.resolve({ value: false })
  }

  playAudio(_data: PlayAudioProps): Promise<{ value: boolean }> {
    return Promise.resolve({ value: false })
  }

  openMailbox(): Promise<{ value: boolean }> {
    return Promise.resolve({ value: false })
  }

  vibrate(pattern?: number | number[]): Promise<{ value: boolean }> {
    if (!navigator || !navigator.vibrate) {
      return Promise.resolve({ value: false })
    }

    navigator.vibrate(pattern)

    return Promise.resolve({ value: true })
  }
}

const JokHelper = new JokHelperWeb()

export { JokHelper }

import { registerWebPlugin } from '@capacitor/core'
registerWebPlugin(JokHelper)
