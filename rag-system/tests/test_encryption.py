"""
Tests for encryption module
"""

import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent / 'src'))

import pytest
from encryption import CryptoManager


def test_encryption_decryption():
    """Test basic encryption and decryption"""
    password = "test_password"
    crypto = CryptoManager(password)
    
    plaintext = "This is a secret message for testing"
    
    # Encrypt
    encrypted, nonce = crypto.encrypt(plaintext)
    
    # Verify encrypted text is different from plaintext
    assert encrypted != plaintext
    
    # Decrypt
    decrypted = crypto.decrypt(encrypted, nonce)
    
    # Verify decrypted text matches original
    assert decrypted == plaintext


def test_encryption_with_unicode():
    """Test encryption with unicode characters"""
    password = "test_password"
    crypto = CryptoManager(password)
    
    plaintext = "这是一个包含中文的测试消息 🔒"
    
    # Encrypt and decrypt
    encrypted, nonce = crypto.encrypt(plaintext)
    decrypted = crypto.decrypt(encrypted, nonce)
    
    # Verify
    assert decrypted == plaintext


def test_different_passwords():
    """Test that different passwords produce different keys"""
    plaintext = "Secret message"
    
    crypto1 = CryptoManager("password1")
    crypto2 = CryptoManager("password2")
    
    encrypted1, nonce1 = crypto1.encrypt(plaintext)
    
    # Try to decrypt with wrong password - should fail
    with pytest.raises(Exception):
        crypto2.decrypt(encrypted1, nonce1)


def test_same_password_same_salt():
    """Test that same password and salt produce same key"""
    password = "test_password"
    
    crypto1 = CryptoManager(password)
    salt = crypto1.salt
    
    # Create second crypto manager with same salt
    crypto2 = CryptoManager(password, salt=salt)
    
    plaintext = "Test message"
    encrypted, nonce = crypto1.encrypt(plaintext)
    
    # Should be able to decrypt with second manager
    decrypted = crypto2.decrypt(encrypted, nonce)
    assert decrypted == plaintext


def test_hash_text():
    """Test text hashing"""
    text = "Test message for hashing"
    hash1 = CryptoManager.hash_text(text)
    hash2 = CryptoManager.hash_text(text)
    
    # Same text should produce same hash
    assert hash1 == hash2
    
    # Different text should produce different hash
    hash3 = CryptoManager.hash_text(text + " modified")
    assert hash1 != hash3


def test_empty_string():
    """Test encryption of empty string"""
    password = "test_password"
    crypto = CryptoManager(password)
    
    plaintext = ""
    encrypted, nonce = crypto.encrypt(plaintext)
    decrypted = crypto.decrypt(encrypted, nonce)
    
    assert decrypted == plaintext


def test_long_text():
    """Test encryption of long text"""
    password = "test_password"
    crypto = CryptoManager(password)
    
    # Generate long text
    plaintext = "Lorem ipsum dolor sit amet. " * 1000
    
    encrypted, nonce = crypto.encrypt(plaintext)
    decrypted = crypto.decrypt(encrypted, nonce)
    
    assert decrypted == plaintext
    assert len(decrypted) == len(plaintext)


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
