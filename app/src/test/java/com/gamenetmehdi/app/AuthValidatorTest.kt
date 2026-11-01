package com.gamenetmehdi.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {
    @Test
    fun login_requiresValidEmailAndEightCharacterPassword() {
        val result = AuthValidator.validate(
            AuthMode.LOGIN,
            AuthInput(email = "invalid", password = "short"),
        )

        assertFalse(result.isValid)
        assertTrue("email" in result.errors)
        assertTrue("password" in result.errors)
    }

    @Test
    fun loginDoesNotRequireRegistrationFields() {
        val result = AuthValidator.validate(
            AuthMode.LOGIN,
            AuthInput(email = "player@example.com", password = "safe-pass-123"),
        )

        assertTrue(result.isValid)
    }

    @Test
    fun registrationRequiresNameAndMatchingConfirmation() {
        val result = AuthValidator.validate(
            AuthMode.REGISTER,
            AuthInput(
                name = "م",
                email = "player@example.com",
                password = "safe-pass-123",
                passwordConfirmation = "different-pass",
            ),
        )

        assertFalse(result.isValid)
        assertTrue("name" in result.errors)
        assertTrue("passwordConfirmation" in result.errors)
    }

    @Test
    fun demoSubmissionExplicitlyDoesNotCreateSession() {
        val result = InMemoryMockRepository().submitDemo(
            AuthMode.REGISTER,
            AuthInput(
                name = "کاربر نمونه",
                email = "player@example.com",
                password = "safe-pass-123",
                passwordConfirmation = "safe-pass-123",
            ),
        )

        assertTrue(result.accepted)
        assertTrue(result.message.contains("هیچ حساب یا نشست واقعی"))
    }
}
