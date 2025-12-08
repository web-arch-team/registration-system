"""
Tests for document processor
"""

import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent / 'src'))

import pytest
import tempfile
import os
from utils.document_processor import DocumentProcessor


def test_chunk_text():
    """Test text chunking"""
    processor = DocumentProcessor(chunk_size=100, chunk_overlap=20)
    
    text = "This is a test sentence. " * 20
    chunks = processor.chunk_text(text)
    
    # Should create multiple chunks
    assert len(chunks) > 1
    
    # Each chunk should have metadata
    for chunk in chunks:
        assert 'text' in chunk
        assert 'chunk_id' in chunk
        assert 'start_pos' in chunk
        assert 'end_pos' in chunk


def test_chunk_with_overlap():
    """Test that chunks overlap correctly"""
    processor = DocumentProcessor(chunk_size=50, chunk_overlap=10)
    
    text = "Word " * 30
    chunks = processor.chunk_text(text)
    
    # Verify overlap exists
    if len(chunks) > 1:
        # Check that end of first chunk overlaps with start of second
        assert chunks[1]['start_pos'] < chunks[0]['end_pos']


def test_load_text_file():
    """Test loading text file"""
    processor = DocumentProcessor()
    
    # Create temporary text file
    with tempfile.NamedTemporaryFile(mode='w', suffix='.txt', delete=False) as f:
        f.write("This is a test document.\nWith multiple lines.")
        temp_path = f.name
    
    try:
        content = processor.load_document(temp_path)
        assert "test document" in content
        assert "multiple lines" in content
    finally:
        os.unlink(temp_path)


def test_load_md_file():
    """Test loading markdown file"""
    processor = DocumentProcessor()
    
    # Create temporary markdown file
    with tempfile.NamedTemporaryFile(mode='w', suffix='.md', delete=False) as f:
        f.write("# Test Header\n\nThis is markdown content.")
        temp_path = f.name
    
    try:
        content = processor.load_document(temp_path)
        assert "Test Header" in content
        assert "markdown content" in content
    finally:
        os.unlink(temp_path)


def test_unsupported_format():
    """Test that unsupported format raises error"""
    processor = DocumentProcessor()
    
    with tempfile.NamedTemporaryFile(suffix='.xyz', delete=False) as f:
        temp_path = f.name
    
    try:
        with pytest.raises(ValueError):
            processor.load_document(temp_path)
    finally:
        os.unlink(temp_path)


def test_empty_text():
    """Test chunking empty text"""
    processor = DocumentProcessor()
    chunks = processor.chunk_text("")
    assert len(chunks) == 0


def test_short_text():
    """Test chunking text shorter than chunk_size"""
    processor = DocumentProcessor(chunk_size=1000)
    text = "This is a short text."
    chunks = processor.chunk_text(text)
    
    # Should create exactly one chunk
    assert len(chunks) == 1
    assert chunks[0]['text'] == text.strip()


def test_metadata_preservation():
    """Test that metadata is preserved in chunks"""
    processor = DocumentProcessor()
    
    text = "Test content"
    metadata = {'source': 'test.txt', 'author': 'Test Author'}
    
    chunks = processor.chunk_text(text, metadata=metadata)
    
    assert len(chunks) > 0
    assert chunks[0]['metadata'] == metadata


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
