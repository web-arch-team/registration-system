"""
Encryption Manager for Privacy-Preserving RAG System
Implements AES-256-GCM encryption for text chunks
"""

import os
import base64
import hashlib
from typing import Tuple
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.backends import default_backend


class CryptoManager:
    """Manages encryption and decryption of text chunks"""
    
    def __init__(self, master_password: str, salt: bytes = None):
        """
        Initialize CryptoManager with a master password
        
        Args:
            master_password: Master password for key derivation
            salt: Optional salt for key derivation (generated if not provided)
        """
        self.salt = salt if salt else os.urandom(32)
        self.key = self._derive_key(master_password, self.salt)
        self.aesgcm = AESGCM(self.key)
    
    def _derive_key(self, password: str, salt: bytes) -> bytes:
        """
        Derive encryption key from password using PBKDF2
        
        Args:
            password: Master password
            salt: Salt for key derivation
            
        Returns:
            32-byte encryption key
        """
        kdf = PBKDF2HMAC(
            algorithm=hashes.SHA256(),
            length=32,
            salt=salt,
            iterations=100000,
            backend=default_backend()
        )
        return kdf.derive(password.encode())
    
    def encrypt(self, plaintext: str) -> Tuple[str, str]:
        """
        Encrypt plaintext using AES-256-GCM
        
        Args:
            plaintext: Text to encrypt
            
        Returns:
            Tuple of (encrypted_text_base64, nonce_base64)
        """
        # Generate random nonce
        nonce = os.urandom(12)
        
        # Encrypt
        ciphertext = self.aesgcm.encrypt(nonce, plaintext.encode('utf-8'), None)
        
        # Return base64 encoded values
        encrypted_b64 = base64.b64encode(ciphertext).decode('utf-8')
        nonce_b64 = base64.b64encode(nonce).decode('utf-8')
        
        return encrypted_b64, nonce_b64
    
    def decrypt(self, encrypted_text: str, nonce: str) -> str:
        """
        Decrypt ciphertext using AES-256-GCM
        
        Args:
            encrypted_text: Base64 encoded encrypted text
            nonce: Base64 encoded nonce
            
        Returns:
            Decrypted plaintext
        """
        # Decode from base64
        ciphertext = base64.b64decode(encrypted_text)
        nonce_bytes = base64.b64decode(nonce)
        
        # Decrypt
        plaintext_bytes = self.aesgcm.decrypt(nonce_bytes, ciphertext, None)
        
        return plaintext_bytes.decode('utf-8')
    
    def get_salt(self) -> str:
        """
        Get the salt used for key derivation (base64 encoded)
        
        Returns:
            Base64 encoded salt
        """
        return base64.b64encode(self.salt).decode('utf-8')
    
    @staticmethod
    def hash_text(text: str) -> str:
        """
        Create SHA-256 hash of text for integrity checking
        
        Args:
            text: Text to hash
            
        Returns:
            Hex string of hash
        """
        return hashlib.sha256(text.encode('utf-8')).hexdigest()
