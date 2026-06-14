package com.spacefarm.economy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet(100f);
    }

    @Test
    void testInitialBalance() {
        assertEquals(100f, wallet.getBalance());
    }

    @Test
    void testEarn() {
        wallet.earn(50f);
        assertEquals(150f, wallet.getBalance());
    }

    @Test
    void testEarnNegative() {
        wallet.earn(-50f);
        assertEquals(100f, wallet.getBalance(), "Earning negative amount should not change balance");
    }

    @Test
    void testSpendSuccessful() {
        assertTrue(wallet.spend(40f));
        assertEquals(60f, wallet.getBalance());
    }

    @Test
    void testSpendInsufficientFunds() {
        assertFalse(wallet.spend(150f));
        assertEquals(100f, wallet.getBalance());
    }

    @Test
    void testSpendNegative() {
        assertFalse(wallet.spend(-10f));
        assertEquals(100f, wallet.getBalance());
    }

    @Test
    void testCanAfford() {
        assertTrue(wallet.canAfford(100f));
        assertTrue(wallet.canAfford(50f));
        assertFalse(wallet.canAfford(101f));
    }

    @Test
    void testSetBalance() {
        wallet.setBalance(500f);
        assertEquals(500f, wallet.getBalance());
    }
}
