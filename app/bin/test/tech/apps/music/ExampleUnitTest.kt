package tech.apps.music

import org.junit.Assert.assertEquals
import org.junit.Test
import org.videolan.libvlc.util.VLCUtil

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val string = VLCUtil.UriFromMrl("https://www.youtube.com/watch?v=7MMd0kqtVTs")
        println(string)
        assertEquals("", string)
    }

}