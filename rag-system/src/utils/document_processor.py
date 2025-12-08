"""
Document Processor for RAG System
Handles parsing and chunking of various document formats
"""

import os
from typing import List, Dict
from pathlib import Path
import pypdf
import docx
import chardet


class DocumentProcessor:
    """Processes documents and splits them into chunks"""
    
    def __init__(self, chunk_size: int = 500, chunk_overlap: int = 50):
        """
        Initialize DocumentProcessor
        
        Args:
            chunk_size: Maximum size of each text chunk
            chunk_overlap: Overlap between consecutive chunks
        """
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap
    
    def load_document(self, file_path: str) -> str:
        """
        Load document based on file extension
        
        Args:
            file_path: Path to document
            
        Returns:
            Extracted text content
        """
        path = Path(file_path)
        extension = path.suffix.lower()
        
        if extension == '.pdf':
            return self._load_pdf(file_path)
        elif extension == '.docx':
            return self._load_docx(file_path)
        elif extension in ['.txt', '.md']:
            return self._load_text(file_path)
        else:
            raise ValueError(f"Unsupported file format: {extension}")
    
    def _load_pdf(self, file_path: str) -> str:
        """Load text from PDF file"""
        text = []
        with open(file_path, 'rb') as file:
            pdf_reader = pypdf.PdfReader(file)
            for page in pdf_reader.pages:
                page_text = page.extract_text()
                if page_text:
                    text.append(page_text)
        return '\n'.join(text)
    
    def _load_docx(self, file_path: str) -> str:
        """Load text from DOCX file"""
        doc = docx.Document(file_path)
        text = []
        for paragraph in doc.paragraphs:
            if paragraph.text.strip():
                text.append(paragraph.text)
        return '\n'.join(text)
    
    def _load_text(self, file_path: str) -> str:
        """Load text from TXT/MD file with encoding detection"""
        with open(file_path, 'rb') as file:
            raw_data = file.read()
            detected = chardet.detect(raw_data)
            encoding = detected['encoding'] or 'utf-8'
        
        with open(file_path, 'r', encoding=encoding) as file:
            return file.read()
    
    def chunk_text(self, text: str, metadata: Dict = None) -> List[Dict]:
        """
        Split text into overlapping chunks
        
        Args:
            text: Text to chunk
            metadata: Optional metadata to attach to each chunk
            
        Returns:
            List of chunk dictionaries with text and metadata
        """
        if not text:
            return []
        
        chunks = []
        start = 0
        text_length = len(text)
        chunk_id = 0
        
        while start < text_length:
            end = start + self.chunk_size
            
            # If not at the end, try to break at a sentence or word boundary
            if end < text_length:
                # Look for sentence boundary (., !, ?)
                for delimiter in ['. ', '! ', '? ', '\n\n', '\n']:
                    last_delimiter = text.rfind(delimiter, start, end)
                    if last_delimiter != -1:
                        end = last_delimiter + len(delimiter)
                        break
                else:
                    # If no sentence boundary, try word boundary
                    last_space = text.rfind(' ', start, end)
                    if last_space != -1:
                        end = last_space
            
            chunk_text = text[start:end].strip()
            
            if chunk_text:
                chunk_data = {
                    'chunk_id': chunk_id,
                    'text': chunk_text,
                    'start_pos': start,
                    'end_pos': end,
                    'metadata': metadata or {}
                }
                chunks.append(chunk_data)
                chunk_id += 1
            
            # Move start position with overlap
            start = end - self.chunk_overlap
            if start < 0:
                start = 0
            elif start >= end:
                start = end
        
        return chunks
    
    def process_document(self, file_path: str) -> List[Dict]:
        """
        Process a document: load and chunk it
        
        Args:
            file_path: Path to document
            
        Returns:
            List of text chunks with metadata
        """
        # Load document
        text = self.load_document(file_path)
        
        # Create metadata
        metadata = {
            'source': file_path,
            'filename': os.path.basename(file_path),
            'file_size': os.path.getsize(file_path)
        }
        
        # Chunk text
        chunks = self.chunk_text(text, metadata)
        
        return chunks
