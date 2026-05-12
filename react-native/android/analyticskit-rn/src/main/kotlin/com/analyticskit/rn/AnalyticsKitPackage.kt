package com.analyticskit.rn

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * React Native package that registers [AnalyticsKitModule].
 *
 * Add this to your MainApplication's getPackages():
 * ```java
 * @Override
 * protected List<ReactPackage> getPackages() {
 *     List<ReactPackage> packages = new PackageList(this).getPackages();
 *     packages.add(new AnalyticsKitPackage());
 *     return packages;
 * }
 * ```
 */
class AnalyticsKitPackage : ReactPackage {

    override fun createNativeModules(
        reactContext: ReactApplicationContext
    ): List<NativeModule> = listOf(AnalyticsKitModule(reactContext))

    override fun createViewManagers(
        reactContext: ReactApplicationContext
    ): List<ViewManager<*, *>> = emptyList()
}

