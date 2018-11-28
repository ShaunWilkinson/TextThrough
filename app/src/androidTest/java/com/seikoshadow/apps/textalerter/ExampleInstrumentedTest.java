package com.seikoshadow.apps.textalerter;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented indie_flower, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class) public class ExampleInstrumentedTest {
  @Test public void useAppContext() {
    // Context of the app under indie_flower.
    Context appContext = InstrumentationRegistry.getTargetContext();

    assertEquals("com.seikoshadow.apps.textthrough", appContext.getPackageName());
  }
}
