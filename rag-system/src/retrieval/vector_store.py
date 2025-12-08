"""
Vector Store Manager for RAG System
Manages vector embeddings and encrypted text storage using Qdrant
"""

from typing import List, Dict, Tuple
import uuid
from qdrant_client import QdrantClient
from qdrant_client.models import (
    Distance, VectorParams, PointStruct, 
    Filter, FieldCondition, MatchValue
)
from sentence_transformers import SentenceTransformer


class VectorStore:
    """Manages vector database operations with Qdrant"""
    
    def __init__(
        self, 
        host: str = "localhost", 
        port: int = 6333,
        collection_name: str = "encrypted_knowledge_base",
        embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2",
        vector_size: int = 384
    ):
        """
        Initialize VectorStore
        
        Args:
            host: Qdrant server host
            port: Qdrant server port
            collection_name: Name of the collection
            embedding_model: Sentence transformer model name
            vector_size: Dimension of embedding vectors
        """
        self.client = QdrantClient(host=host, port=port)
        self.collection_name = collection_name
        self.vector_size = vector_size
        
        # Initialize embedding model
        self.embedding_model = SentenceTransformer(embedding_model)
        
        # Create collection if it doesn't exist
        self._init_collection()
    
    def _init_collection(self):
        """Initialize Qdrant collection"""
        collections = self.client.get_collections().collections
        collection_exists = any(
            col.name == self.collection_name for col in collections
        )
        
        if not collection_exists:
            self.client.create_collection(
                collection_name=self.collection_name,
                vectors_config=VectorParams(
                    size=self.vector_size,
                    distance=Distance.COSINE
                )
            )
    
    def embed_text(self, text: str) -> List[float]:
        """
        Generate embedding vector for text
        
        Args:
            text: Text to embed
            
        Returns:
            Embedding vector
        """
        embedding = self.embedding_model.encode(text, convert_to_numpy=True)
        return embedding.tolist()
    
    def embed_batch(self, texts: List[str]) -> List[List[float]]:
        """
        Generate embeddings for multiple texts
        
        Args:
            texts: List of texts to embed
            
        Returns:
            List of embedding vectors
        """
        embeddings = self.embedding_model.encode(texts, convert_to_numpy=True)
        return embeddings.tolist()
    
    def add_encrypted_chunk(
        self, 
        text: str, 
        encrypted_text: str, 
        nonce: str,
        metadata: Dict = None
    ) -> str:
        """
        Add encrypted chunk with its embedding to vector store
        
        Args:
            text: Original plaintext (for embedding only)
            encrypted_text: Encrypted text (stored in payload)
            nonce: Encryption nonce (stored in payload)
            metadata: Additional metadata
            
        Returns:
            Point ID
        """
        # Generate embedding from plaintext
        embedding = self.embed_text(text)
        
        # Create unique ID
        point_id = str(uuid.uuid4())
        
        # Prepare payload with encrypted data
        payload = {
            'encrypted_text': encrypted_text,
            'nonce': nonce,
            'text_length': len(text),
            'metadata': metadata or {}
        }
        
        # Insert into Qdrant
        point = PointStruct(
            id=point_id,
            vector=embedding,
            payload=payload
        )
        
        self.client.upsert(
            collection_name=self.collection_name,
            points=[point]
        )
        
        return point_id
    
    def add_encrypted_chunks_batch(
        self, 
        chunks_data: List[Dict]
    ) -> List[str]:
        """
        Add multiple encrypted chunks in batch
        
        Args:
            chunks_data: List of dicts with keys: 'text', 'encrypted_text', 'nonce', 'metadata'
            
        Returns:
            List of point IDs
        """
        if not chunks_data:
            return []
        
        # Extract texts for batch embedding
        texts = [chunk['text'] for chunk in chunks_data]
        embeddings = self.embed_batch(texts)
        
        # Create points
        points = []
        point_ids = []
        
        for i, chunk in enumerate(chunks_data):
            point_id = str(uuid.uuid4())
            point_ids.append(point_id)
            
            payload = {
                'encrypted_text': chunk['encrypted_text'],
                'nonce': chunk['nonce'],
                'text_length': len(chunk['text']),
                'metadata': chunk.get('metadata', {})
            }
            
            point = PointStruct(
                id=point_id,
                vector=embeddings[i],
                payload=payload
            )
            points.append(point)
        
        # Batch insert
        self.client.upsert(
            collection_name=self.collection_name,
            points=points
        )
        
        return point_ids
    
    def search(
        self, 
        query: str, 
        top_k: int = 5,
        score_threshold: float = 0.0
    ) -> List[Dict]:
        """
        Search for similar encrypted chunks
        
        Args:
            query: Query text
            top_k: Number of results to return
            score_threshold: Minimum similarity score
            
        Returns:
            List of results with encrypted_text, nonce, score, and metadata
        """
        # Generate query embedding
        query_embedding = self.embed_text(query)
        
        # Search in Qdrant
        results = self.client.search(
            collection_name=self.collection_name,
            query_vector=query_embedding,
            limit=top_k,
            score_threshold=score_threshold
        )
        
        # Format results
        formatted_results = []
        for result in results:
            formatted_results.append({
                'id': result.id,
                'score': result.score,
                'encrypted_text': result.payload['encrypted_text'],
                'nonce': result.payload['nonce'],
                'text_length': result.payload['text_length'],
                'metadata': result.payload['metadata']
            })
        
        return formatted_results
    
    def delete_collection(self):
        """Delete the entire collection"""
        self.client.delete_collection(collection_name=self.collection_name)
    
    def get_collection_info(self) -> Dict:
        """Get information about the collection"""
        info = self.client.get_collection(collection_name=self.collection_name)
        return {
            'name': self.collection_name,
            'vectors_count': info.vectors_count,
            'points_count': info.points_count,
            'status': info.status
        }
