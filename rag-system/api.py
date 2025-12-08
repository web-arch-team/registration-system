"""
FastAPI Web Interface for Privacy-Preserving RAG System
"""

import sys
from pathlib import Path
from typing import Optional, List
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import tempfile
import os

# Add src to path
sys.path.insert(0, str(Path(__file__).parent / 'src'))

from rag_pipeline import PrivacyPreservingRAG

# Initialize FastAPI app
app = FastAPI(
    title="Privacy-Preserving RAG System",
    description="Lightweight RAG with end-to-end encryption",
    version="1.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize RAG system
rag_system: Optional[PrivacyPreservingRAG] = None


# Pydantic models
class QueryRequest(BaseModel):
    question: str = Field(..., description="Question to ask")
    top_k: Optional[int] = Field(5, description="Number of chunks to retrieve")
    similarity_threshold: Optional[float] = Field(0.7, description="Minimum similarity score")


class QueryResponse(BaseModel):
    question: str
    answer: str
    num_chunks_retrieved: int
    avg_similarity: Optional[float] = None
    processing_time: float
    breakdown: Optional[dict] = None


class IngestResponse(BaseModel):
    success: bool
    filename: Optional[str] = None
    num_chunks: Optional[int] = None
    processing_time: Optional[float] = None
    error: Optional[str] = None


class SystemStats(BaseModel):
    vector_store: Optional[dict] = None
    llm_available: Optional[bool] = None
    llm_model: Optional[str] = None
    embedding_model: Optional[str] = None
    log_integrity_hash: Optional[str] = None


@app.on_event("startup")
async def startup_event():
    """Initialize RAG system on startup"""
    global rag_system
    
    config_path = "config/config.yaml"
    master_password = os.getenv("MASTER_PASSWORD", "default_master_password_change_me")
    
    try:
        rag_system = PrivacyPreservingRAG(
            config_path=config_path,
            master_password=master_password
        )
        print("✓ RAG System initialized successfully")
    except Exception as e:
        print(f"✗ Failed to initialize RAG system: {str(e)}")
        raise


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "Privacy-Preserving RAG System API",
        "version": "1.0.0",
        "endpoints": {
            "query": "/api/query",
            "ingest": "/api/ingest",
            "stats": "/api/stats",
            "health": "/api/health"
        }
    }


@app.get("/api/health")
async def health_check():
    """Health check endpoint"""
    if rag_system is None:
        raise HTTPException(status_code=503, detail="RAG system not initialized")
    
    return {
        "status": "healthy",
        "system_initialized": True
    }


@app.post("/api/query", response_model=QueryResponse)
async def query(request: QueryRequest):
    """
    Query the RAG system
    
    Args:
        request: Query request with question and parameters
        
    Returns:
        Query response with answer and metadata
    """
    if rag_system is None:
        raise HTTPException(status_code=503, detail="RAG system not initialized")
    
    try:
        result = rag_system.query(
            question=request.question,
            top_k=request.top_k,
            similarity_threshold=request.similarity_threshold
        )
        
        if 'error' in result:
            raise HTTPException(status_code=500, detail=result['error'])
        
        return result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ingest", response_model=IngestResponse)
async def ingest_document(file: UploadFile = File(...)):
    """
    Ingest a document into the knowledge base
    
    Args:
        file: Document file to ingest
        
    Returns:
        Ingestion result
    """
    if rag_system is None:
        raise HTTPException(status_code=503, detail="RAG system not initialized")
    
    # Check file extension
    filename = file.filename
    supported_formats = ['.pdf', '.docx', '.txt', '.md']
    file_ext = Path(filename).suffix.lower()
    
    if file_ext not in supported_formats:
        raise HTTPException(
            status_code=400, 
            detail=f"Unsupported file format. Supported: {', '.join(supported_formats)}"
        )
    
    # Save uploaded file to temporary location
    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=file_ext) as tmp_file:
            content = await file.read()
            tmp_file.write(content)
            tmp_file_path = tmp_file.name
        
        # Ingest document
        result = rag_system.ingest_document(tmp_file_path)
        
        # Clean up temporary file
        os.unlink(tmp_file_path)
        
        if not result['success']:
            raise HTTPException(status_code=500, detail=result.get('error', 'Ingestion failed'))
        
        return result
        
    except Exception as e:
        # Clean up temporary file if it exists
        if 'tmp_file_path' in locals():
            try:
                os.unlink(tmp_file_path)
            except:
                pass
        
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/stats", response_model=SystemStats)
async def get_stats():
    """
    Get system statistics
    
    Returns:
        System statistics
    """
    if rag_system is None:
        raise HTTPException(status_code=503, detail="RAG system not initialized")
    
    try:
        stats = rag_system.get_system_stats()
        
        if 'error' in stats:
            raise HTTPException(status_code=500, detail=stats['error'])
        
        return stats
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/api/clear")
async def clear_knowledge_base():
    """
    Clear the knowledge base
    
    Returns:
        Success status
    """
    if rag_system is None:
        raise HTTPException(status_code=503, detail="RAG system not initialized")
    
    try:
        result = rag_system.clear_knowledge_base()
        
        if not result['success']:
            raise HTTPException(status_code=500, detail=result.get('error', 'Clear failed'))
        
        return result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
