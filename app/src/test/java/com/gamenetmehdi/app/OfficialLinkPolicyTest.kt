package com.gamenetmehdi.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfficialLinkPolicyTest {
    @Test
    fun validate_acceptsHttpsAllowlistedHostAndSubdomain() {
        assertEquals("www.ea.com", OfficialLinkPolicy.validate("https://www.ea.com/games/fc")?.host)
        assertEquals("tekken.com", OfficialLinkPolicy.validate("https://tekken.com/")?.host)
    }

    @Test
    fun validate_rejectsUnsafeSchemeLookalikeHostAndCredentials() {
        assertNull(OfficialLinkPolicy.validate("javascript:alert(1)"))
        assertNull(OfficialLinkPolicy.validate("http://ea.com/games/fc"))
        assertNull(OfficialLinkPolicy.validate("https://ea.com.evil.example/games/fc"))
        assertNull(OfficialLinkPolicy.validate("https://ea.com@evil.example/games/fc"))
    }

    @Test
    fun validate_rejectsFragmentsAndUnexpectedPorts() {
        assertNull(OfficialLinkPolicy.validate("https://ea.com:8443/games/fc"))
        assertNull(OfficialLinkPolicy.validate("https://ea.com/games/fc#token"))
    }
}
