package com.example.eventmanager

import com.example.eventmanager.model.Validation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test
    fun validEventHasNoErrors() {
        val errors = Validation.eventErrors(
            name = "Mobile Futures",
            date = "28/02/2027",
            organiser = "Greenwich Computing Society",
            location = "Queen Anne Court",
            email = "events@example.org",
            phone = "+44 20 1234 5678"
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun impossibleDateIsRejected() {
        assertFalse(Validation.isValidDate("31/02/2027"))
        assertTrue(Validation.isValidDate("28/02/2027"))
    }

    @Test
    fun requiredEventFieldsAreReported() {
        val errors = Validation.eventErrors("", "", "", "", "", "")
        assertEquals(setOf("name", "date", "organiser", "location"), errors.keys)
    }

    @Test
    fun optionalContactFieldsAreValidatedOnlyWhenPresent() {
        val valid = Validation.eventErrors("Event", "01/04/2027", "UoG", "London", "", "")
        val invalid = Validation.eventErrors("Event", "01/04/2027", "UoG", "London", "bad", "abc")
        assertTrue(valid.isEmpty())
        assertEquals(setOf("email", "phone"), invalid.keys)
    }

    @Test
    fun sessionTimeUsesTwentyFourHourFormat() {
        assertTrue(Validation.sessionErrors("Opening", "09:30").isEmpty())
        assertTrue("time" in Validation.sessionErrors("Opening", "25:30"))
    }
}
