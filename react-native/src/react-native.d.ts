/**
 * Minimal type declarations for React Native modules used by the bridge.
 * This avoids pulling the full react-native package (and its 800+ transitive
 * dependencies) into the TypeScript compilation.
 *
 * In a real React Native project, these types come from react-native itself.
 * This file exists only so `tsc --noEmit` works in the standalone bridge package.
 */
declare module 'react-native' {
  export interface NativeModule {
    [key: string]: (...args: any[]) => any;
  }

  export interface NativeModulesStatic {
    [name: string]: NativeModule;
  }

  export const NativeModules: NativeModulesStatic;

  export interface EmitterSubscription {
    remove(): void;
  }

  export class NativeEventEmitter {
    constructor(nativeModule?: NativeModule);
    addListener(
      eventType: string,
      listener: (event: any) => void,
    ): EmitterSubscription;
    removeAllListeners(eventType: string): void;
  }
}
