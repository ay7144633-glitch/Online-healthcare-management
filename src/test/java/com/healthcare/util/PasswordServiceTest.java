package com.healthcare.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/** Verifies password hashing, salt uniqueness, and malformed-hash handling. */
class PasswordServiceTest {
    /** Confirms that only the password used to create a hash verifies successfully. */
    @Test
    void verifiesPasswordAgainstItsHash() {
        String hash = PasswordService.hash("correct-horse-battery");

        assertTrue(PasswordService.verify("correct-horse-battery", hash));
        assertFalse(PasswordService.verify("different-password", hash));
    }

    /** Confirms identical passwords receive independent salts and remain verifiable. */
    @Test
    void generatesDifferentHashesForSamePassword() {
        String firstHash = PasswordService.hash("clinic-password");
        String secondHash = PasswordService.hash("clinic-password");

        assertNotEquals(firstHash, secondHash);
        assertTrue(PasswordService.verify("clinic-password", firstHash));
        assertTrue(PasswordService.verify("clinic-password", secondHash));
    }

    /** Confirms invalid encoded hash data is rejected without authenticating. */
    @Test
    void rejectsMalformedHashes() {
        assertFalse(PasswordService.verify("password", "not-a-hash"));
    }
}