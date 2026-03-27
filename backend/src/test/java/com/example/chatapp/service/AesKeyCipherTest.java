package com.example.chatapp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesKeyCipherTest {

    @Test
    void shouldEncryptAndDecrypt() {
        AesKeyCipher cipher = new AesKeyCipher("0123456789abcdef0123456789abcdef");
        String plain = "sk-3c2f150e3a1f4b0e92a86a17524cdba8";

        String encrypted = cipher.encrypt(plain);
        System.out.println("加密后的密文: " + encrypted);

        String decrypted = cipher.decrypt(encrypted);
        System.out.println("解密还原的明文: " + decrypted);

        assertNotEquals(plain, encrypted);
        assertEquals(plain, decrypted);
    }

    @Test
    void shouldThrowWhenDecryptInvalidCipher() {
        AesKeyCipher cipher = new AesKeyCipher("test-master-key-123");
        assertThrows(IllegalStateException.class, () -> cipher.decrypt("invalid-content"));
    }
}
