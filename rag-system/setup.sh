#!/bin/bash
# Setup script for Privacy-Preserving RAG System

echo "=========================================="
echo "Privacy-Preserving RAG System Setup"
echo "=========================================="
echo ""

# Check Python version
echo "Checking Python version..."
python_version=$(python3 --version 2>&1 | awk '{print $2}')
echo "Found Python $python_version"

if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 is not installed"
    exit 1
fi

# Create virtual environment
echo ""
echo "Creating virtual environment..."
python3 -m venv venv
source venv/bin/activate

# Install dependencies
echo ""
echo "Installing dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

# Check for Docker (for Qdrant)
echo ""
echo "Checking for Docker..."
if ! command -v docker &> /dev/null; then
    echo "Warning: Docker is not installed. You'll need Docker to run Qdrant."
    echo "Please install Docker from: https://www.docker.com/get-started"
else
    echo "Docker found: $(docker --version)"
    
    # Check if Qdrant is already running
    if docker ps | grep -q qdrant; then
        echo "Qdrant is already running"
    else
        echo ""
        echo "Starting Qdrant..."
        docker pull qdrant/qdrant
        docker run -d -p 6333:6333 -p 6334:6334 \
            -v $(pwd)/data/vectors:/qdrant/storage \
            --name qdrant \
            qdrant/qdrant
        echo "Qdrant started on port 6333"
    fi
fi

# Check for Ollama
echo ""
echo "Checking for Ollama..."
if ! command -v ollama &> /dev/null; then
    echo "Warning: Ollama is not installed."
    echo "Please install Ollama from: https://ollama.com/download"
    echo ""
    echo "After installation, run:"
    echo "  ollama pull llama3.2:1b"
else
    echo "Ollama found: $(ollama --version)"
    
    # Check if model is available
    if ollama list | grep -q "llama3.2:1b"; then
        echo "Model llama3.2:1b is already available"
    else
        echo ""
        echo "Pulling llama3.2:1b model..."
        ollama pull llama3.2:1b
    fi
fi

# Create necessary directories
echo ""
echo "Creating directories..."
mkdir -p data/documents
mkdir -p data/encrypted
mkdir -p data/vectors
mkdir -p logs
mkdir -p tests

# Test the setup
echo ""
echo "Running setup tests..."
python3 -c "
import sys
sys.path.insert(0, 'src')
try:
    from encryption import CryptoManager
    from retrieval import VectorStore
    from generation import LLMManager
    from audit import AuditLogger
    print('✓ All modules imported successfully')
except Exception as e:
    print(f'✗ Error importing modules: {e}')
    sys.exit(1)
"

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "Setup completed successfully!"
    echo "=========================================="
    echo ""
    echo "Next steps:"
    echo "1. Activate the virtual environment:"
    echo "   source venv/bin/activate"
    echo ""
    echo "2. Start using the system:"
    echo "   python cli.py ingest data/documents/example_privacy.txt"
    echo "   python cli.py query 'What is encryption?'"
    echo ""
    echo "3. Or start the API server:"
    echo "   python api.py"
    echo ""
else
    echo ""
    echo "Setup encountered errors. Please check the output above."
    exit 1
fi
