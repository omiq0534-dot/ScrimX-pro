package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.MainActivity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class AppCrashTest {

    @Test
    fun testAppLaunch() {
        try {
            Robolectric.buildActivity(MainActivity::class.java).setup().get()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
