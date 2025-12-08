#!/usr/bin/env python3
"""
Performance Benchmarking Script for RAG System
Collects performance metrics for thesis experiments
"""

import sys
import os
import time
import json
import statistics
from pathlib import Path
from typing import List, Dict

sys.path.insert(0, str(Path(__file__).parent / 'src'))

from rag_pipeline import PrivacyPreservingRAG


class RAGBenchmark:
    """Benchmark suite for RAG system"""
    
    def __init__(self, config_path='config/config.yaml'):
        self.rag = PrivacyPreservingRAG(config_path=config_path)
        self.results = {
            'ingestion': [],
            'queries': [],
            'system_info': {}
        }
    
    def benchmark_ingestion(self, file_paths: List[str]) -> Dict:
        """Benchmark document ingestion"""
        print("\n" + "="*80)
        print("Document Ingestion Benchmark")
        print("="*80)
        
        results = []
        
        for file_path in file_paths:
            print(f"\nProcessing: {file_path}")
            
            start_time = time.time()
            result = self.rag.ingest_document(file_path)
            end_time = time.time()
            
            if result['success']:
                processing_time = end_time - start_time
                
                file_size = os.path.getsize(file_path) / 1024  # KB
                
                metrics = {
                    'filename': os.path.basename(file_path),
                    'file_size_kb': round(file_size, 2),
                    'num_chunks': result['num_chunks'],
                    'processing_time': round(processing_time, 3),
                    'chunks_per_second': round(result['num_chunks'] / processing_time, 2)
                }
                
                results.append(metrics)
                
                print(f"  ✓ Processed in {processing_time:.2f}s")
                print(f"  ✓ Created {result['num_chunks']} chunks")
                print(f"  ✓ Speed: {metrics['chunks_per_second']} chunks/s")
            else:
                print(f"  ✗ Failed: {result.get('error')}")
        
        # Calculate statistics
        if results:
            avg_time = statistics.mean(r['processing_time'] for r in results)
            total_chunks = sum(r['num_chunks'] for r in results)
            
            print(f"\n{'Summary':^80}")
            print("-"*80)
            print(f"Total documents: {len(results)}")
            print(f"Total chunks: {total_chunks}")
            print(f"Average processing time: {avg_time:.2f}s")
            print(f"Total time: {sum(r['processing_time'] for r in results):.2f}s")
        
        self.results['ingestion'] = results
        return results
    
    def benchmark_queries(self, questions: List[str], top_k: int = 5) -> Dict:
        """Benchmark query performance"""
        print("\n" + "="*80)
        print("Query Performance Benchmark")
        print("="*80)
        
        results = []
        
        for i, question in enumerate(questions, 1):
            print(f"\n[{i}/{len(questions)}] Query: {question[:60]}...")
            
            # Run query multiple times for better statistics
            times = []
            for _ in range(3):
                start_time = time.time()
                response = self.rag.query(question, top_k=top_k)
                end_time = time.time()
                times.append(end_time - start_time)
            
            avg_time = statistics.mean(times)
            
            metrics = {
                'question': question,
                'num_chunks_retrieved': response['num_chunks_retrieved'],
                'avg_processing_time': round(avg_time, 3),
                'min_time': round(min(times), 3),
                'max_time': round(max(times), 3),
                'breakdown': response.get('breakdown', {})
            }
            
            if response['num_chunks_retrieved'] > 0:
                metrics['avg_similarity'] = response.get('avg_similarity', 0)
            
            results.append(metrics)
            
            print(f"  ✓ Average time: {avg_time:.3f}s")
            print(f"  ✓ Retrieved: {response['num_chunks_retrieved']} chunks")
            if 'breakdown' in response:
                print(f"  └─ Retrieval: {response['breakdown']['retrieval']:.3f}s")
                print(f"  └─ Decryption: {response['breakdown']['decryption']:.3f}s")
                print(f"  └─ Generation: {response['breakdown']['generation']:.3f}s")
        
        # Calculate statistics
        if results:
            avg_query_time = statistics.mean(r['avg_processing_time'] for r in results)
            
            print(f"\n{'Summary':^80}")
            print("-"*80)
            print(f"Total queries: {len(results)}")
            print(f"Average query time: {avg_query_time:.3f}s")
            print(f"Fastest query: {min(r['min_time'] for r in results):.3f}s")
            print(f"Slowest query: {max(r['max_time'] for r in results):.3f}s")
        
        self.results['queries'] = results
        return results
    
    def benchmark_system(self) -> Dict:
        """Collect system information"""
        print("\n" + "="*80)
        print("System Information")
        print("="*80)
        
        stats = self.rag.get_system_stats()
        
        info = {
            'vector_store_points': stats.get('vector_store', {}).get('points_count', 0),
            'llm_model': stats.get('llm_model', 'N/A'),
            'llm_available': stats.get('llm_available', False),
            'embedding_model': stats.get('embedding_model', 'N/A')
        }
        
        for key, value in info.items():
            print(f"{key}: {value}")
        
        self.results['system_info'] = info
        return info
    
    def save_results(self, output_path='benchmark_results.json'):
        """Save benchmark results to file"""
        print(f"\nSaving results to {output_path}...")
        
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(self.results, f, indent=2, ensure_ascii=False)
        
        print(f"✓ Results saved")
    
    def generate_report(self) -> str:
        """Generate markdown report"""
        report = ["# RAG System Performance Benchmark Report\n"]
        
        # System info
        report.append("## System Configuration\n")
        for key, value in self.results['system_info'].items():
            report.append(f"- **{key}**: {value}")
        report.append("")
        
        # Ingestion results
        if self.results['ingestion']:
            report.append("## Document Ingestion Performance\n")
            report.append("| Filename | Size (KB) | Chunks | Time (s) | Speed (chunks/s) |")
            report.append("|----------|-----------|--------|----------|------------------|")
            
            for r in self.results['ingestion']:
                report.append(
                    f"| {r['filename']} | {r['file_size_kb']} | "
                    f"{r['num_chunks']} | {r['processing_time']} | "
                    f"{r['chunks_per_second']} |"
                )
            
            avg_time = statistics.mean(r['processing_time'] for r in self.results['ingestion'])
            report.append(f"\n**Average Processing Time**: {avg_time:.2f}s\n")
        
        # Query results
        if self.results['queries']:
            report.append("## Query Performance\n")
            report.append("| Question | Chunks | Avg Time (s) | Min (s) | Max (s) |")
            report.append("|----------|--------|--------------|---------|---------|")
            
            for r in self.results['queries']:
                q = r['question'][:40] + '...' if len(r['question']) > 40 else r['question']
                report.append(
                    f"| {q} | {r['num_chunks_retrieved']} | "
                    f"{r['avg_processing_time']} | {r['min_time']} | "
                    f"{r['max_time']} |"
                )
            
            avg_time = statistics.mean(r['avg_processing_time'] for r in self.results['queries'])
            report.append(f"\n**Average Query Time**: {avg_time:.3f}s\n")
            
            # Breakdown
            if self.results['queries'][0].get('breakdown'):
                report.append("### Time Breakdown (Average)\n")
                
                avg_retrieval = statistics.mean(
                    r['breakdown']['retrieval'] for r in self.results['queries']
                )
                avg_decryption = statistics.mean(
                    r['breakdown']['decryption'] for r in self.results['queries']
                )
                avg_generation = statistics.mean(
                    r['breakdown']['generation'] for r in self.results['queries']
                )
                
                report.append(f"- **Retrieval**: {avg_retrieval:.3f}s")
                report.append(f"- **Decryption**: {avg_decryption:.3f}s")
                report.append(f"- **Generation**: {avg_generation:.3f}s")
                report.append("")
        
        return '\n'.join(report)
    
    def save_report(self, output_path='benchmark_report.md'):
        """Save markdown report"""
        report = self.generate_report()
        
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(report)
        
        print(f"\n✓ Report saved to {output_path}")


def main():
    """Main benchmark function"""
    print("\n" + "="*80)
    print("Privacy-Preserving RAG System - Performance Benchmark")
    print("="*80)
    
    benchmark = RAGBenchmark()
    
    # Collect system info
    benchmark.benchmark_system()
    
    # Benchmark ingestion (if documents exist)
    doc_dir = Path('data/documents')
    if doc_dir.exists():
        doc_files = list(doc_dir.glob('*.txt'))
        if doc_files:
            print(f"\nFound {len(doc_files)} documents for ingestion benchmark")
            benchmark.benchmark_ingestion([str(f) for f in doc_files])
    
    # Benchmark queries
    test_questions = [
        "什么是AES加密？",
        "What is RAG?",
        "如何保护隐私？",
        "What are the benefits of local deployment?",
        "轻量化模型有哪些优势？"
    ]
    
    print(f"\nRunning {len(test_questions)} test queries...")
    benchmark.benchmark_queries(test_questions)
    
    # Save results
    benchmark.save_results()
    benchmark.save_report()
    
    print("\n" + "="*80)
    print("Benchmark completed!")
    print("="*80)
    print("\nGenerated files:")
    print("  - benchmark_results.json  (raw data)")
    print("  - benchmark_report.md     (formatted report)")
    print("\n")


if __name__ == '__main__':
    main()
