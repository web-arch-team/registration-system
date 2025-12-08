"""
LLM Manager for RAG System
Manages interaction with lightweight LLM (Ollama)
"""

from typing import List, Dict, Optional
import requests
import json


class LLMManager:
    """Manages LLM operations using Ollama"""
    
    def __init__(
        self,
        model_name: str = "llama3.2:1b",
        base_url: str = "http://localhost:11434",
        temperature: float = 0.7,
        max_tokens: int = 512
    ):
        """
        Initialize LLMManager
        
        Args:
            model_name: Name of the Ollama model
            base_url: Ollama API base URL
            temperature: Sampling temperature
            max_tokens: Maximum tokens to generate
        """
        self.model_name = model_name
        self.base_url = base_url
        self.temperature = temperature
        self.max_tokens = max_tokens
        self.api_url = f"{base_url}/api/generate"
    
    def generate(
        self, 
        prompt: str, 
        context: Optional[str] = None,
        stream: bool = False
    ) -> str:
        """
        Generate text using LLM
        
        Args:
            prompt: User prompt/question
            context: Retrieved context for RAG
            stream: Whether to stream the response
            
        Returns:
            Generated text
        """
        # Build full prompt with context
        full_prompt = self._build_prompt(prompt, context)
        
        # Prepare request payload
        payload = {
            "model": self.model_name,
            "prompt": full_prompt,
            "stream": stream,
            "options": {
                "temperature": self.temperature,
                "num_predict": self.max_tokens
            }
        }
        
        try:
            # Make API request
            response = requests.post(
                self.api_url,
                json=payload,
                timeout=60
            )
            response.raise_for_status()
            
            if stream:
                # Handle streaming response
                return self._handle_stream(response)
            else:
                # Parse non-streaming response
                result = response.json()
                return result.get('response', '')
                
        except requests.exceptions.RequestException as e:
            return f"Error generating response: {str(e)}"
    
    def _build_prompt(self, question: str, context: Optional[str] = None) -> str:
        """
        Build prompt with retrieved context for RAG
        
        Args:
            question: User question
            context: Retrieved context chunks
            
        Returns:
            Formatted prompt
        """
        if context:
            prompt = f"""Based on the following context, please answer the question. If the answer cannot be found in the context, say "I cannot find the answer in the provided context."

Context:
{context}

Question: {question}

Answer:"""
        else:
            prompt = question
        
        return prompt
    
    def _handle_stream(self, response) -> str:
        """Handle streaming response from Ollama"""
        full_response = ""
        for line in response.iter_lines():
            if line:
                try:
                    json_response = json.loads(line)
                    if 'response' in json_response:
                        full_response += json_response['response']
                except json.JSONDecodeError:
                    continue
        return full_response
    
    def check_model_availability(self) -> bool:
        """
        Check if the model is available in Ollama
        
        Returns:
            True if model is available, False otherwise
        """
        try:
            # List available models
            list_url = f"{self.base_url}/api/tags"
            response = requests.get(list_url, timeout=5)
            response.raise_for_status()
            
            models = response.json().get('models', [])
            model_names = [model.get('name', '') for model in models]
            
            return self.model_name in model_names
            
        except requests.exceptions.RequestException:
            return False
    
    def pull_model(self) -> bool:
        """
        Pull/download the model if not available
        
        Returns:
            True if successful, False otherwise
        """
        try:
            pull_url = f"{self.base_url}/api/pull"
            payload = {"name": self.model_name}
            
            response = requests.post(pull_url, json=payload, timeout=300)
            response.raise_for_status()
            
            return True
            
        except requests.exceptions.RequestException as e:
            print(f"Error pulling model: {str(e)}")
            return False


class RAGGenerator:
    """RAG-specific generator that combines retrieval and generation"""
    
    def __init__(self, llm_manager: LLMManager):
        """
        Initialize RAG Generator
        
        Args:
            llm_manager: LLMManager instance
        """
        self.llm = llm_manager
    
    def generate_answer(
        self, 
        question: str, 
        retrieved_chunks: List[str]
    ) -> Dict:
        """
        Generate answer using retrieved context
        
        Args:
            question: User question
            retrieved_chunks: List of retrieved text chunks
            
        Returns:
            Dict with answer and metadata
        """
        # Combine retrieved chunks into context
        context = self._format_context(retrieved_chunks)
        
        # Generate answer
        answer = self.llm.generate(question, context)
        
        # Return result with metadata
        return {
            'question': question,
            'answer': answer,
            'num_chunks_used': len(retrieved_chunks),
            'context_length': len(context)
        }
    
    def _format_context(self, chunks: List[str]) -> str:
        """
        Format retrieved chunks into context
        
        Args:
            chunks: List of text chunks
            
        Returns:
            Formatted context string
        """
        formatted_chunks = []
        for i, chunk in enumerate(chunks, 1):
            formatted_chunks.append(f"[{i}] {chunk}")
        
        return "\n\n".join(formatted_chunks)
