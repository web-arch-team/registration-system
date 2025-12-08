# Privacy-Preserving Lightweight RAG System

## 项目概述 (Project Overview)

本项目实现了一个面向本地化部署和隐私敏感场景的轻量级检索增强生成（RAG）系统。系统在保证问答准确性的前提下，集成端到端的数据加密机制以保护私有知识库的安全，同时通过采用轻量级深度学习模型和量化技术优化系统的推理延迟和资源占用。

This project implements a privacy-preserving, lightweight Retrieval-Augmented Generation (RAG) system designed for local deployment and privacy-sensitive scenarios. The system integrates end-to-end data encryption to protect private knowledge bases while maintaining answer accuracy, and uses lightweight deep learning models with quantization techniques to optimize inference latency and resource usage.

## 核心特性 (Core Features)

### 1. 隐私保护 (Privacy Protection)
- **端到端加密**: 使用 AES-256-GCM 加密所有文档内容
- **本地部署**: 所有数据处理在本地完成，不依赖云服务
- **密文存储**: 向量数据库仅存储加密密文和向量嵌入
- **审计日志**: 完整的操作审计，不记录敏感内容

### 2. 轻量化设计 (Lightweight Design)
- **轻量级嵌入模型**: 使用 `sentence-transformers/all-MiniLM-L6-v2` (约22MB)
- **轻量级LLM**: 支持 Ollama 的小参数量模型（如 llama3.2:1b）
- **低资源占用**: 可在普通CPU环境运行
- **快速推理**: 优化的检索和生成流程

### 3. 完整的RAG流程 (Complete RAG Pipeline)
- **文档解析**: 支持 PDF, DOCX, TXT, MD 等多种格式
- **智能分块**: 基于语义边界的文本分块
- **向量检索**: 高效的相似度搜索
- **上下文生成**: 基于检索结果生成准确答案

## 系统架构 (System Architecture)

```
┌─────────────────────────────────────────────────────────────────┐
│                     Privacy-Preserving RAG System                │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │   Document   │───▶│  Encryption  │───▶│    Vector    │      │
│  │  Processing  │    │   (AES-256)  │    │    Store     │      │
│  └──────────────┘    └──────────────┘    │   (Qdrant)   │      │
│         │                    │            └──────────────┘      │
│         │                    │                    │              │
│    ┌────▼────┐          ┌───▼───┐           ┌───▼───┐          │
│    │ Chunking│          │Vector │           │Vector │          │
│    │         │          │Embed  │           │Search │          │
│    └─────────┘          └───────┘           └───┬───┘          │
│                                                  │              │
│  User Query ──────────────────────────────────▶ │              │
│                                                  │              │
│                              ┌──────────────┐   │              │
│                              │  Decryption  │◀──┘              │
│                              └──────┬───────┘                  │
│                                     │                          │
│                              ┌──────▼───────┐                  │
│                              │     LLM      │                  │
│                              │  Generation  │                  │
│                              └──────┬───────┘                  │
│                                     │                          │
│                                  Answer                        │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              Audit Logger (Privacy-Safe)                 │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 技术栈 (Technology Stack)

### 加密 (Encryption)
- **cryptography**: AES-256-GCM 加密
- **PBKDF2**: 密钥派生

### 向量数据库 (Vector Database)
- **Qdrant**: 本地向量数据库
- **HNSW**: 高效近似最近邻搜索

### 嵌入模型 (Embedding Model)
- **sentence-transformers**: all-MiniLM-L6-v2
- **384维向量**: 平衡性能与精度

### 语言模型 (Language Model)
- **Ollama**: 本地LLM推理
- **llama3.2:1b**: 轻量级10亿参数模型

### 文档处理 (Document Processing)
- **pypdf**: PDF解析
- **python-docx**: Word文档解析
- **chardet**: 编码检测

## 快速开始 (Quick Start)

### 1. 环境准备 (Prerequisites)

```bash
# Python 3.8+
python --version

# 安装 Qdrant (向量数据库)
docker pull qdrant/qdrant
docker run -p 6333:6333 qdrant/qdrant

# 安装 Ollama (LLM引擎)
# Linux/Mac:
curl -fsSL https://ollama.com/install.sh | sh
# Windows: 下载安装包 https://ollama.com/download

# 拉取轻量级模型
ollama pull llama3.2:1b
```

### 2. 安装依赖 (Install Dependencies)

```bash
cd rag-system
pip install -r requirements.txt
```

### 3. 配置系统 (Configuration)

编辑 `config/config.yaml` 根据需要修改配置：

```yaml
# 主要配置项
vector_db:
  host: "localhost"
  port: 6333

llm:
  model_name: "llama3.2:1b"
  base_url: "http://localhost:11434"

encryption:
  algorithm: "AES-256-GCM"
```

### 4. 使用方式 (Usage)

#### 命令行界面 (CLI)

```bash
# 导入文档
python cli.py ingest /path/to/document.pdf

# 查询系统
python cli.py query "你的问题是什么？"

# 交互模式
python cli.py interactive

# 查看统计
python cli.py stats

# 清空知识库
python cli.py clear
```

#### Web API

```bash
# 启动API服务器
python api.py

# API将在 http://localhost:8000 运行
# API文档: http://localhost:8000/docs
```

API 端点：

- `POST /api/ingest` - 上传文档
- `POST /api/query` - 查询问题
- `GET /api/stats` - 系统统计
- `DELETE /api/clear` - 清空知识库

示例请求：

```bash
# 查询
curl -X POST "http://localhost:8000/api/query" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是机器学习？",
    "top_k": 5,
    "similarity_threshold": 0.7
  }'

# 上传文档
curl -X POST "http://localhost:8000/api/ingest" \
  -F "file=@document.pdf"
```

#### Python API

```python
from rag_pipeline import PrivacyPreservingRAG

# 初始化系统
rag = PrivacyPreservingRAG(
    config_path='config/config.yaml',
    master_password='your_secure_password'
)

# 导入文档
result = rag.ingest_document('document.pdf')
print(f"导入了 {result['num_chunks']} 个文本块")

# 查询
response = rag.query("你的问题？")
print(f"答案: {response['answer']}")
print(f"处理时间: {response['processing_time']}秒")

# 查看统计
stats = rag.get_system_stats()
print(stats)
```

## 项目结构 (Project Structure)

```
rag-system/
├── config/
│   └── config.yaml              # 系统配置
├── src/
│   ├── encryption/              # 加密模块
│   │   ├── __init__.py
│   │   └── crypto_manager.py    # AES加密管理器
│   ├── retrieval/               # 检索模块
│   │   ├── __init__.py
│   │   └── vector_store.py      # 向量存储管理器
│   ├── generation/              # 生成模块
│   │   ├── __init__.py
│   │   └── llm_manager.py       # LLM管理器
│   ├── audit/                   # 审计模块
│   │   ├── __init__.py
│   │   └── audit_logger.py      # 审计日志
│   ├── utils/                   # 工具模块
│   │   ├── __init__.py
│   │   └── document_processor.py # 文档处理器
│   ├── __init__.py
│   └── rag_pipeline.py          # 主RAG流程
├── data/                        # 数据目录
│   ├── documents/               # 原始文档
│   ├── encrypted/               # 加密数据
│   └── vectors/                 # 向量数据
├── logs/                        # 日志目录
├── tests/                       # 测试代码
├── docs/                        # 文档
├── cli.py                       # 命令行界面
├── api.py                       # Web API
├── requirements.txt             # 依赖列表
└── README.md                    # 本文件
```

## 核心功能详解 (Core Functions)

### 1. 文档导入流程

```python
# 1. 解析文档 (支持 PDF, DOCX, TXT, MD)
chunks = document_processor.process_document(file_path)

# 2. 加密文本块
for chunk in chunks:
    encrypted_text, nonce = crypto_manager.encrypt(chunk['text'])

# 3. 生成向量嵌入
embeddings = vector_store.embed_batch(texts)

# 4. 存储到向量数据库 (加密文本 + 向量)
vector_store.add_encrypted_chunks_batch(encrypted_chunks)
```

### 2. 查询流程

```python
# 1. 查询向量化
query_embedding = vector_store.embed_text(query)

# 2. 相似度搜索
results = vector_store.search(query, top_k=5)

# 3. 解密检索结果
decrypted_chunks = []
for result in results:
    decrypted = crypto_manager.decrypt(
        result['encrypted_text'], 
        result['nonce']
    )
    decrypted_chunks.append(decrypted)

# 4. LLM生成答案
answer = llm_manager.generate(query, context=decrypted_chunks)
```

### 3. 审计日志

系统记录所有操作但不记录敏感内容：

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "operation": "QUERY",
  "details": {
    "query_hash": "abc123...",
    "num_results": 5,
    "processing_time_seconds": 2.3
  }
}
```

## 安全特性 (Security Features)

### 数据静态安全
- ✅ 所有文档内容使用 AES-256-GCM 加密
- ✅ 向量数据库仅存储加密密文
- ✅ 主密钥使用 PBKDF2 派生
- ✅ 每个文本块使用独立的随机 nonce

### 数据传输安全
- ✅ 本地部署，无数据外传
- ✅ 所有处理在内存中完成
- ✅ 临时文件自动清理

### 操作审计
- ✅ 所有操作可追溯
- ✅ 日志完整性校验（SHA-256）
- ✅ 不记录敏感查询内容
- ✅ 时间戳和操作类型记录

### 隐私保护
- ✅ 无云服务依赖
- ✅ 无第三方数据传输
- ✅ 密文与向量分离存储
- ✅ 查询过程不泄露原文

## 性能优化 (Performance Optimization)

### 轻量化策略
1. **小模型选择**: 使用参数量小的模型
2. **批处理**: 向量嵌入批量处理
3. **缓存机制**: 常用查询结果缓存
4. **量化支持**: 支持模型量化（4-bit）

### 性能指标参考
- 文档导入: ~1-2秒/页
- 向量检索: ~50-100ms
- 解密操作: ~1-5ms
- LLM生成: ~2-5秒 (取决于模型和长度)

## 测试 (Testing)

```bash
# 运行测试
cd tests
pytest test_encryption.py -v
pytest test_retrieval.py -v
pytest test_pipeline.py -v
```

## 故障排除 (Troubleshooting)

### 1. Qdrant 连接失败
```bash
# 检查 Qdrant 是否运行
docker ps | grep qdrant

# 重启 Qdrant
docker restart qdrant
```

### 2. Ollama 模型未找到
```bash
# 检查已安装模型
ollama list

# 拉取模型
ollama pull llama3.2:1b
```

### 3. 内存不足
- 减小 `chunk_size` 配置
- 减少 `top_k` 检索数量
- 使用更小的模型

### 4. 加密/解密错误
- 确保使用相同的 `master_password`
- 检查 salt 是否正确保存

## 扩展开发 (Extension Development)

### 添加新的文档格式支持

```python
# 在 document_processor.py 中添加新方法
def _load_pptx(self, file_path: str) -> str:
    # 实现PPTX解析
    pass
```

### 自定义加密算法

```python
# 继承 CryptoManager 类
class CustomCrypto(CryptoManager):
    def encrypt(self, plaintext: str):
        # 自定义加密逻辑
        pass
```

### 集成其他LLM

```python
# 实现新的 LLM 管理器
class CustomLLMManager:
    def generate(self, prompt: str) -> str:
        # 集成其他LLM API
        pass
```

## 论文实验支持 (Research Support)

本系统设计用于支持毕业设计论文的实验需求：

### 实验1: 准确性测试
- F1分数计算
- 精确率/召回率评估
- 与基线系统对比

### 实验2: 性能测试
- 响应延迟测量
- 内存占用监控
- 吞吐量测试

### 实验3: 隐私性验证
- 数据静态安全证明
- 审计日志分析
- 与云端RAG对比

### 实验4: 轻量化效果
- 模型大小对比
- 量化前后性能
- 资源占用分析

## 贡献 (Contributing)

欢迎提交问题和改进建议！

## 许可证 (License)

本项目用于学术研究和教育目的。

## 联系方式 (Contact)

如有问题，请通过项目 Issue 联系。

---

**注意**: 本系统为毕业设计项目，设计用于演示隐私保护RAG系统的核心概念。在生产环境使用前，请进行充分的安全审计和性能测试。
