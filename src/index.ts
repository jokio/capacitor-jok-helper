import { registerPlugin } from '@capacitor/core'
import type { JokHelperPlugin } from './definitions'
import { JokHelperWeb } from './web'

const JokHelper = registerPlugin<JokHelperPlugin>('JokHelper', {
  web: () => new JokHelperWeb(),
})

export * from './definitions'
export { JokHelper }
