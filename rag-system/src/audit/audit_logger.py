"""
Audit Logger for Privacy-Preserving RAG System
Logs system operations without recording sensitive content
"""

import logging
import json
import hashlib
from datetime import datetime
from pathlib import Path
from typing import Dict, Optional, Any


class AuditLogger:
    """Manages audit logging for RAG system operations"""
    
    def __init__(
        self, 
        log_path: str = "./logs/audit.log",
        log_level: str = "INFO"
    ):
        """
        Initialize AuditLogger
        
        Args:
            log_path: Path to audit log file
            log_level: Logging level
        """
        self.log_path = Path(log_path)
        self.log_path.parent.mkdir(parents=True, exist_ok=True)
        
        # Configure logger
        self.logger = logging.getLogger("RAG_Audit")
        self.logger.setLevel(getattr(logging, log_level.upper()))
        
        # File handler
        file_handler = logging.FileHandler(self.log_path)
        file_handler.setLevel(getattr(logging, log_level.upper()))
        
        # Formatter
        formatter = logging.Formatter(
            '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
        )
        file_handler.setFormatter(formatter)
        
        self.logger.addHandler(file_handler)
        
        # Also log to console
        console_handler = logging.StreamHandler()
        console_handler.setFormatter(formatter)
        self.logger.addHandler(console_handler)
    
    def _hash_sensitive_data(self, data: str) -> str:
        """
        Hash sensitive data for audit trail
        
        Args:
            data: Sensitive data to hash
            
        Returns:
            SHA-256 hash of the data
        """
        return hashlib.sha256(data.encode()).hexdigest()
    
    def _create_log_entry(
        self, 
        operation: str, 
        details: Dict,
        include_sensitive: bool = False
    ) -> Dict:
        """
        Create structured log entry
        
        Args:
            operation: Operation type
            details: Operation details
            include_sensitive: Whether to include sensitive data (hashed)
            
        Returns:
            Structured log entry
        """
        entry = {
            'timestamp': datetime.utcnow().isoformat(),
            'operation': operation,
            'details': details
        }
        
        # Hash any sensitive fields if present
        if not include_sensitive and 'query' in details:
            entry['details']['query_hash'] = self._hash_sensitive_data(details['query'])
            del entry['details']['query']
        
        return entry
    
    def log_document_ingestion(
        self, 
        filename: str, 
        num_chunks: int,
        success: bool = True
    ):
        """
        Log document ingestion operation
        
        Args:
            filename: Name of the document
            num_chunks: Number of chunks created
            success: Whether operation succeeded
        """
        details = {
            'filename': filename,
            'num_chunks': num_chunks,
            'success': success
        }
        entry = self._create_log_entry('DOCUMENT_INGESTION', details)
        self.logger.info(json.dumps(entry))
    
    def log_encryption(
        self, 
        num_chunks: int,
        success: bool = True
    ):
        """
        Log encryption operation
        
        Args:
            num_chunks: Number of chunks encrypted
            success: Whether operation succeeded
        """
        details = {
            'num_chunks': num_chunks,
            'success': success
        }
        entry = self._create_log_entry('ENCRYPTION', details)
        self.logger.info(json.dumps(entry))
    
    def log_query(
        self, 
        query: str,
        num_results: int,
        processing_time: float,
        log_query_text: bool = False
    ):
        """
        Log query operation
        
        Args:
            query: Query text (will be hashed unless log_query_text=True)
            num_results: Number of results retrieved
            processing_time: Query processing time in seconds
            log_query_text: Whether to log actual query text
        """
        details = {
            'num_results': num_results,
            'processing_time_seconds': round(processing_time, 3)
        }
        
        if log_query_text:
            details['query'] = query
        
        entry = self._create_log_entry('QUERY', details, include_sensitive=log_query_text)
        self.logger.info(json.dumps(entry))
    
    def log_retrieval(
        self, 
        query_hash: str,
        num_retrieved: int,
        avg_similarity_score: float
    ):
        """
        Log retrieval operation
        
        Args:
            query_hash: Hash of the query
            num_retrieved: Number of chunks retrieved
            avg_similarity_score: Average similarity score
        """
        details = {
            'query_hash': query_hash,
            'num_retrieved': num_retrieved,
            'avg_similarity_score': round(avg_similarity_score, 4)
        }
        entry = self._create_log_entry('RETRIEVAL', details)
        self.logger.info(json.dumps(entry))
    
    def log_decryption(
        self, 
        num_chunks: int,
        success: bool = True
    ):
        """
        Log decryption operation
        
        Args:
            num_chunks: Number of chunks decrypted
            success: Whether operation succeeded
        """
        details = {
            'num_chunks': num_chunks,
            'success': success
        }
        entry = self._create_log_entry('DECRYPTION', details)
        self.logger.info(json.dumps(entry))
    
    def log_generation(
        self, 
        model_name: str,
        processing_time: float,
        success: bool = True
    ):
        """
        Log text generation operation (without actual generated content)
        
        Args:
            model_name: Name of the LLM model used
            processing_time: Generation time in seconds
            success: Whether operation succeeded
        """
        details = {
            'model_name': model_name,
            'processing_time_seconds': round(processing_time, 3),
            'success': success
        }
        entry = self._create_log_entry('GENERATION', details)
        self.logger.info(json.dumps(entry))
    
    def log_error(
        self, 
        operation: str,
        error_message: str,
        error_type: Optional[str] = None
    ):
        """
        Log error
        
        Args:
            operation: Operation that failed
            error_message: Error message
            error_type: Type of error
        """
        details = {
            'operation': operation,
            'error_message': error_message
        }
        if error_type:
            details['error_type'] = error_type
        
        entry = self._create_log_entry('ERROR', details)
        self.logger.error(json.dumps(entry))
    
    def log_system_event(
        self, 
        event_type: str,
        details: Dict[str, Any]
    ):
        """
        Log general system event
        
        Args:
            event_type: Type of system event
            details: Event details
        """
        entry = self._create_log_entry(event_type, details)
        self.logger.info(json.dumps(entry))
    
    def get_log_integrity_hash(self) -> str:
        """
        Calculate integrity hash of the entire log file
        
        Returns:
            SHA-256 hash of log file
        """
        if not self.log_path.exists():
            return ""
        
        with open(self.log_path, 'rb') as f:
            return hashlib.sha256(f.read()).hexdigest()
