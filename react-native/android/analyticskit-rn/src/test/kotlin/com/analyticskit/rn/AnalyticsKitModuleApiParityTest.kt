package com.analyticskit.rn

import com.facebook.react.bridge.ReactMethod
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Verifies that [AnalyticsKitModule] exposes every method defined in the
 * shared API spec (api-spec/AnalyticsKitSpec.ts).
 *
 * If a method is added to the spec but not to the bridge, this test fails.
 * The TypeScript compiler catches the reverse direction (spec defines a method
 * the wrapper doesn't implement).
 */
class AnalyticsKitModuleApiParityTest {

    private val requiredMethods = setOf(
        "initialize",
        "track",
        "flush",
        "destroy",
        "startObservingState",
        "stopObservingState"
    )

    @Test
    fun `bridge exposes all required methods`() {
        val bridgeMethods = AnalyticsKitModule::class.java.methods
            .filter { it.isAnnotationPresent(ReactMethod::class.java) }
            .map { it.name }
            .toSet()

        val missing = requiredMethods - bridgeMethods
        assertThat(missing).isEmpty()
    }

    @Test
    fun `no unexpected public ReactMethods exist`() {
        val bridgeMethods = AnalyticsKitModule::class.java.methods
            .filter { it.isAnnotationPresent(ReactMethod::class.java) }
            .map { it.name }
            .toSet()

        val unexpected = bridgeMethods - requiredMethods
        assertThat(unexpected).isEmpty()
    }
}

