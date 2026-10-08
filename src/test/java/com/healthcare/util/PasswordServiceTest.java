package com.healthcare.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PasswordServiceTest {
    @Test
    void verifiesPasswordAgainstItsHash() {
        String hash = PasswordService.hash("correct-horse-battery");

        assertTrue(PasswordService.verify("correct-horse-battery", hash));
        assertFalse(PasswordService.verify("different-password", hash));
    }

    @Test
    void generatesDifferentHashesForSamePassword() {
        String firstHash = PasswordService.hash("clinic-password");
        String secondHash = PasswordService.hash("clinic-password");

        assertNotEquals(firstHash, secondHash);
        assertTrue(PasswordService.verify("clinic-password", firstHash));
        assertTrue(PasswordService.verify("clinic-password", secondHash));
    }

    @Test
    void rejectsMalformedHashes() {
        assertFalse(PasswordService.verify("password", "not-a-hash"));
    }
}