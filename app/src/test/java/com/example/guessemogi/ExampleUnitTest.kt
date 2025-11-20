package com.example.guessemogi

import com.example.guessemogi.viewmodel.authviewmodel.AuthViewModel
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit


class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

class FormatTimeTest {
    private fun formatTime(millis: Long): String {
        if (millis <= 0) return "00:00"
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        return String.format("%02d:%02d", minutes, seconds)
    }

    @Test
    fun formatTime_withSeveralMinutes_returnsMinutesAndSeconds() {
        assertEquals("02:30", formatTime(150000))
    }
}

class GetUsernameFromEmailTest {

    private fun getUsernameFromEmail(email: String): String {
        return email.substringBefore('@')
    }

    @Test
    fun getUsernameFromEmail_withStandardEmail_returnsUsername() {
        val email = "testuser@example.com"
        val expected = "testuser"
        assertEquals(expected, getUsernameFromEmail(email))
    }

    @Test
    fun getUsernameFromEmail_withComplexUsername_returnsUsername() {
        val email = "test.user+123@gmail.com"
        val expected = "test.user+123"
        assertEquals(expected, getUsernameFromEmail(email))
    }

    @Test
    fun getUsernameFromEmail_withNoAtSymbol_returnsOriginalString() {
        val email = "testuser"
        val expected = "testuser"
        assertEquals(expected, getUsernameFromEmail(email))
    }

    @Test
    fun getUsernameFromEmail_withEmptyString_returnsEmptyString() {
        val email = ""
        val expected = ""
        assertEquals(expected, getUsernameFromEmail(email))
    }
}
