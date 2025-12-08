"""
Main RAG Pipeline for Privacy-Preserving System
Integrates encryption, retrieval, and generation
"""

import time
import yaml
from pathlib import Path
from typing import List, Dict, Optional

from encryption import CryptoManager
from retrieval import VectorStore
from generation import LLMManager, RAGGenerator
from audit import AuditLogger
from utils.document_processor import DocumentProcessor


class PrivacyPreservingRAG:
    """Main RAG pipeline with end-to-end encryption"""
    
    def __init__(self, config_path: str = "config/config.yaml", master_password: str = None):
        """
        Initialize Privacy-Preserving RAG System
        
        Args:
            config_path: Path to configuration file
            master_password: Master password for encryption
        """
        # Load configuration
        self.config = self._load_config(config_path)
        
        # Initialize master password
        if master_password is None:
            master_password = "default_master_password_change_me"
        self.master_password = master_password
        
        # Initialize components
        self.crypto_manager = CryptoManager(master_password)
        
        self.vector_store = VectorStore(
            host=self.config['vector_db']['host'],
            port=self.config['vector_db']['port'],
            collection_name=self.config['vector_db']['collection_name'],
            embedding_model=self.config['embedding']['model_name'],
            vector_size=self.config['vector_db']['vector_size']
        )
        
        self.llm_manager = LLMManager(
            model_name=self.config['llm']['model_name'],
            base_url=self.config['llm']['base_url'],
            temperature=self.config['llm']['temperature'],
            max_tokens=self.config['llm']['max_tokens']
        )
        
        self.rag_generator = RAGGenerator(self.llm_manager)
        
        self.audit_logger = AuditLogger(
            log_path=self.config['audit']['log_path'],
            log_level=self.config['audit']['log_level']
        )
        
        self.document_processor = DocumentProcessor(
            chunk_size=self.config['document']['chunk_size'],
            chunk_overlap=self.config['document']['chunk_overlap']
        )
        
        self.audit_logger.log_system_event('SYSTEM_INITIALIZED', {
            'embedding_model': self.config['embedding']['model_name'],
            'llm_model': self.config['llm']['model_name'],
            'vector_db': self.config['vector_db']['type']
        })
    
    def _load_config(self, config_path: str) -> Dict:
        """Load configuration from YAML file"""
        with open(config_path, 'r') as f:
            return yaml.safe_load(f)
    
    def ingest_document(self, file_path: str) -> Dict:
        """
        Ingest a document: parse, chunk, encrypt, and store
        
        Args:
            file_path: Path to document file
            
        Returns:
            Ingestion statistics
        """
        start_time = time.time()
        
        try:
            # Step 1: Process document (parse and chunk)
            self.audit_logger.log_system_event('DOCUMENT_PROCESSING_START', {
                'file': file_path
            })
            
            chunks = self.document_processor.process_document(file_path)
            
            if not chunks:
                self.audit_logger.log_error('DOCUMENT_INGESTION', 'No chunks created from document')
                return {'success': False, 'error': 'No content extracted'}
            
            # Step 2: Encrypt chunks
            self.audit_logger.log_system_event('ENCRYPTION_START', {
                'num_chunks': len(chunks)
            })
            
            encrypted_chunks = []
            for chunk in chunks:
                encrypted_text, nonce = self.crypto_manager.encrypt(chunk['text'])
                encrypted_chunks.append({
                    'text': chunk['text'],  # For embedding only
                    'encrypted_text': encrypted_text,
                    'nonce': nonce,
                    'metadata': chunk['metadata']
                })
            
            self.audit_logger.log_encryption(len(encrypted_chunks), success=True)
            
            # Step 3: Store in vector database
            self.audit_logger.log_system_event('VECTOR_STORE_START', {
                'num_chunks': len(encrypted_chunks)
            })
            
            point_ids = self.vector_store.add_encrypted_chunks_batch(encrypted_chunks)
            
            # Log success
            processing_time = time.time() - start_time
            self.audit_logger.log_document_ingestion(
                filename=Path(file_path).name,
                num_chunks=len(chunks),
                success=True
            )
            
            return {
                'success': True,
                'filename': Path(file_path).name,
                'num_chunks': len(chunks),
                'point_ids': point_ids,
                'processing_time': round(processing_time, 2)
            }
            
        except Exception as e:
            self.audit_logger.log_error('DOCUMENT_INGESTION', str(e), type(e).__name__)
            return {
                'success': False,
                'error': str(e)
            }
    
    def query(
        self, 
        question: str, 
        top_k: Optional[int] = None,
        similarity_threshold: Optional[float] = None
    ) -> Dict:
        """
        Query the RAG system
        
        Args:
            question: User question
            top_k: Number of chunks to retrieve (default from config)
            similarity_threshold: Minimum similarity score (default from config)
            
        Returns:
            Answer with metadata
        """
        start_time = time.time()
        
        # Use config defaults if not specified
        if top_k is None:
            top_k = self.config['retrieval']['top_k']
        if similarity_threshold is None:
            similarity_threshold = self.config['retrieval']['similarity_threshold']
        
        try:
            # Step 1: Retrieve similar encrypted chunks
            retrieval_start = time.time()
            
            results = self.vector_store.search(
                query=question,
                top_k=top_k,
                score_threshold=similarity_threshold
            )
            
            retrieval_time = time.time() - retrieval_start
            
            if not results:
                self.audit_logger.log_query(
                    query=question,
                    num_results=0,
                    processing_time=time.time() - start_time,
                    log_query_text=self.config['audit']['log_queries']
                )
                return {
                    'question': question,
                    'answer': 'No relevant information found in the knowledge base.',
                    'num_chunks_retrieved': 0,
                    'processing_time': round(time.time() - start_time, 2)
                }
            
            # Log retrieval
            avg_score = sum(r['score'] for r in results) / len(results)
            query_hash = self.crypto_manager.hash_text(question)
            self.audit_logger.log_retrieval(
                query_hash=query_hash,
                num_retrieved=len(results),
                avg_similarity_score=avg_score
            )
            
            # Step 2: Decrypt retrieved chunks
            decryption_start = time.time()
            
            decrypted_chunks = []
            for result in results:
                try:
                    decrypted_text = self.crypto_manager.decrypt(
                        result['encrypted_text'],
                        result['nonce']
                    )
                    decrypted_chunks.append(decrypted_text)
                except Exception as e:
                    self.audit_logger.log_error('DECRYPTION', str(e))
                    continue
            
            self.audit_logger.log_decryption(len(decrypted_chunks), success=True)
            decryption_time = time.time() - decryption_start
            
            # Step 3: Generate answer using LLM
            generation_start = time.time()
            
            result = self.rag_generator.generate_answer(question, decrypted_chunks)
            
            generation_time = time.time() - generation_start
            self.audit_logger.log_generation(
                model_name=self.config['llm']['model_name'],
                processing_time=generation_time,
                success=True
            )
            
            # Total processing time
            total_time = time.time() - start_time
            
            # Log query completion
            self.audit_logger.log_query(
                query=question,
                num_results=len(results),
                processing_time=total_time,
                log_query_text=self.config['audit']['log_queries']
            )
            
            # Return detailed result
            return {
                'question': question,
                'answer': result['answer'],
                'num_chunks_retrieved': len(results),
                'similarity_scores': [r['score'] for r in results],
                'avg_similarity': round(avg_score, 4),
                'processing_time': round(total_time, 2),
                'breakdown': {
                    'retrieval': round(retrieval_time, 2),
                    'decryption': round(decryption_time, 2),
                    'generation': round(generation_time, 2)
                }
            }
            
        except Exception as e:
            self.audit_logger.log_error('QUERY', str(e), type(e).__name__)
            return {
                'question': question,
                'error': str(e),
                'processing_time': round(time.time() - start_time, 2)
            }
    
    def get_system_stats(self) -> Dict:
        """
        Get system statistics
        
        Returns:
            System statistics
        """
        try:
            collection_info = self.vector_store.get_collection_info()
            model_available = self.llm_manager.check_model_availability()
            
            return {
                'vector_store': collection_info,
                'llm_available': model_available,
                'llm_model': self.config['llm']['model_name'],
                'embedding_model': self.config['embedding']['model_name'],
                'log_integrity_hash': self.audit_logger.get_log_integrity_hash()
            }
        except Exception as e:
            return {'error': str(e)}
    
    def clear_knowledge_base(self):
        """Clear all data from the knowledge base"""
        try:
            self.vector_store.delete_collection()
            self.vector_store._init_collection()
            self.audit_logger.log_system_event('KNOWLEDGE_BASE_CLEARED', {})
            return {'success': True}
        except Exception as e:
            self.audit_logger.log_error('CLEAR_KNOWLEDGE_BASE', str(e))
            return {'success': False, 'error': str(e)}
