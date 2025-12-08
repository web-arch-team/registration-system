#!/usr/bin/env python3
"""
Command-line interface for Privacy-Preserving RAG System
"""

import sys
import os
import argparse
from pathlib import Path

# Add src to path
sys.path.insert(0, str(Path(__file__).parent / 'src'))

from rag_pipeline import PrivacyPreservingRAG


def ingest_command(args):
    """Handle document ingestion"""
    rag = PrivacyPreservingRAG(
        config_path=args.config,
        master_password=args.password
    )
    
    result = rag.ingest_document(args.file)
    
    if result['success']:
        print(f"✓ Successfully ingested: {result['filename']}")
        print(f"  Chunks created: {result['num_chunks']}")
        print(f"  Processing time: {result['processing_time']}s")
    else:
        print(f"✗ Error: {result.get('error', 'Unknown error')}")
        sys.exit(1)


def query_command(args):
    """Handle query"""
    rag = PrivacyPreservingRAG(
        config_path=args.config,
        master_password=args.password
    )
    
    result = rag.query(
        question=args.question,
        top_k=args.top_k,
        similarity_threshold=args.threshold
    )
    
    if 'error' in result:
        print(f"✗ Error: {result['error']}")
        sys.exit(1)
    
    print("\n" + "="*80)
    print(f"Question: {result['question']}")
    print("="*80)
    print(f"\nAnswer:\n{result['answer']}\n")
    print("-"*80)
    print(f"Chunks retrieved: {result['num_chunks_retrieved']}")
    if result['num_chunks_retrieved'] > 0:
        print(f"Average similarity: {result['avg_similarity']}")
    print(f"Processing time: {result['processing_time']}s")
    if 'breakdown' in result:
        print(f"  - Retrieval: {result['breakdown']['retrieval']}s")
        print(f"  - Decryption: {result['breakdown']['decryption']}s")
        print(f"  - Generation: {result['breakdown']['generation']}s")
    print("="*80 + "\n")


def stats_command(args):
    """Handle stats display"""
    rag = PrivacyPreservingRAG(
        config_path=args.config,
        master_password=args.password
    )
    
    stats = rag.get_system_stats()
    
    if 'error' in stats:
        print(f"✗ Error: {stats['error']}")
        sys.exit(1)
    
    print("\n" + "="*80)
    print("System Statistics")
    print("="*80)
    
    if 'vector_store' in stats:
        print(f"\nVector Store ({stats['vector_store']['name']}):")
        print(f"  Points: {stats['vector_store']['points_count']}")
        print(f"  Status: {stats['vector_store']['status']}")
    
    print(f"\nLLM Model: {stats.get('llm_model', 'N/A')}")
    print(f"  Available: {'Yes' if stats.get('llm_available') else 'No'}")
    
    print(f"\nEmbedding Model: {stats.get('embedding_model', 'N/A')}")
    
    if stats.get('log_integrity_hash'):
        print(f"\nAudit Log Integrity Hash:")
        print(f"  {stats['log_integrity_hash']}")
    
    print("="*80 + "\n")


def clear_command(args):
    """Handle clearing knowledge base"""
    if not args.confirm:
        response = input("Are you sure you want to clear the knowledge base? [y/N]: ")
        if response.lower() != 'y':
            print("Operation cancelled.")
            return
    
    rag = PrivacyPreservingRAG(
        config_path=args.config,
        master_password=args.password
    )
    
    result = rag.clear_knowledge_base()
    
    if result['success']:
        print("✓ Knowledge base cleared successfully")
    else:
        print(f"✗ Error: {result.get('error', 'Unknown error')}")
        sys.exit(1)


def interactive_command(args):
    """Handle interactive mode"""
    rag = PrivacyPreservingRAG(
        config_path=args.config,
        master_password=args.password
    )
    
    print("\n" + "="*80)
    print("Privacy-Preserving RAG System - Interactive Mode")
    print("="*80)
    print("Commands:")
    print("  - Type your question to query the system")
    print("  - Type 'stats' to see system statistics")
    print("  - Type 'exit' or 'quit' to leave")
    print("="*80 + "\n")
    
    while True:
        try:
            question = input("\nYour question: ").strip()
            
            if not question:
                continue
            
            if question.lower() in ['exit', 'quit', 'q']:
                print("Goodbye!")
                break
            
            if question.lower() == 'stats':
                stats_command(args)
                continue
            
            # Process query
            result = rag.query(
                question=question,
                top_k=args.top_k,
                similarity_threshold=args.threshold
            )
            
            if 'error' in result:
                print(f"\n✗ Error: {result['error']}")
                continue
            
            print(f"\nAnswer:\n{result['answer']}")
            print(f"\n[Retrieved {result['num_chunks_retrieved']} chunks in {result['processing_time']}s]")
            
        except KeyboardInterrupt:
            print("\n\nGoodbye!")
            break
        except Exception as e:
            print(f"\n✗ Error: {str(e)}")


def main():
    """Main CLI entry point"""
    parser = argparse.ArgumentParser(
        description='Privacy-Preserving RAG System CLI'
    )
    
    parser.add_argument(
        '--config',
        default='config/config.yaml',
        help='Path to configuration file'
    )
    
    parser.add_argument(
        '--password',
        default='default_master_password_change_me',
        help='Master password for encryption'
    )
    
    subparsers = parser.add_subparsers(dest='command', help='Command to execute')
    
    # Ingest command
    ingest_parser = subparsers.add_parser('ingest', help='Ingest a document')
    ingest_parser.add_argument('file', help='Path to document file')
    ingest_parser.set_defaults(func=ingest_command)
    
    # Query command
    query_parser = subparsers.add_parser('query', help='Query the system')
    query_parser.add_argument('question', help='Question to ask')
    query_parser.add_argument('--top-k', type=int, default=5, help='Number of chunks to retrieve')
    query_parser.add_argument('--threshold', type=float, default=0.7, help='Similarity threshold')
    query_parser.set_defaults(func=query_command)
    
    # Stats command
    stats_parser = subparsers.add_parser('stats', help='Show system statistics')
    stats_parser.set_defaults(func=stats_command)
    
    # Clear command
    clear_parser = subparsers.add_parser('clear', help='Clear knowledge base')
    clear_parser.add_argument('--confirm', action='store_true', help='Skip confirmation prompt')
    clear_parser.set_defaults(func=clear_command)
    
    # Interactive command
    interactive_parser = subparsers.add_parser('interactive', help='Start interactive mode')
    interactive_parser.add_argument('--top-k', type=int, default=5, help='Number of chunks to retrieve')
    interactive_parser.add_argument('--threshold', type=float, default=0.7, help='Similarity threshold')
    interactive_parser.set_defaults(func=interactive_command)
    
    args = parser.parse_args()
    
    if not args.command:
        parser.print_help()
        sys.exit(1)
    
    # Execute command
    args.func(args)


if __name__ == '__main__':
    main()
