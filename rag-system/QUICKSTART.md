# Quick Start Guide - 快速开始指南

## 5分钟快速上手

### 前置条件（Prerequisites）

确保已安装：
- Python 3.8+ 
- Docker（用于Qdrant）
- Ollama（用于LLM）

### 步骤1：安装Qdrant

```bash
# 使用Docker运行Qdrant向量数据库
docker run -d -p 6333:6333 --name qdrant qdrant/qdrant
```

### 步骤2：安装Ollama和模型

```bash
# Linux/Mac:
curl -fsSL https://ollama.com/install.sh | sh

# 下载轻量级模型
ollama pull llama3.2:1b
```

### 步骤3：安装Python依赖

```bash
cd rag-system
pip install -r requirements.txt
```

### 步骤4：导入文档

```bash
# 导入示例文档
python cli.py ingest data/documents/example_privacy.txt
python cli.py ingest data/documents/example_rag.txt
```

### 步骤5：开始提问

```bash
# 命令行查询
python cli.py query "什么是AES加密？"

# 或启动交互模式
python cli.py interactive
```

## 完整功能演示

### 1. 批量导入文档

```bash
# 导入目录下所有文档
for file in data/documents/*.txt; do
    python cli.py ingest "$file"
done
```

### 2. 查看系统状态

```bash
python cli.py stats
```

输出示例：
```
================================================================================
System Statistics
================================================================================

Vector Store (encrypted_knowledge_base):
  Points: 142
  Status: green

LLM Model: llama3.2:1b
  Available: Yes

Embedding Model: sentence-transformers/all-MiniLM-L6-v2

Audit Log Integrity Hash:
  abc123def456...
================================================================================
```

### 3. 使用Web API

```bash
# 启动API服务器
python api.py

# 在另一个终端测试API
curl -X POST "http://localhost:8000/api/query" \
  -H "Content-Type: application/json" \
  -d '{"question": "什么是RAG系统？", "top_k": 5}'
```

### 4. 使用Python API

```python
from rag_pipeline import PrivacyPreservingRAG

# 初始化系统
rag = PrivacyPreservingRAG(
    config_path='config/config.yaml',
    master_password='your_password'
)

# 导入文档
result = rag.ingest_document('document.pdf')
print(f"Successfully ingested {result['num_chunks']} chunks")

# 查询
response = rag.query("你的问题？")
print(f"Answer: {response['answer']}")
print(f"Processing time: {response['processing_time']}s")
```

## 常见问题（FAQ）

### Q1: Qdrant连接失败
```bash
# 检查Qdrant是否运行
docker ps | grep qdrant

# 如果没有运行，启动它
docker start qdrant
```

### Q2: Ollama模型未找到
```bash
# 检查已安装的模型
ollama list

# 如果模型不存在，拉取它
ollama pull llama3.2:1b
```

### Q3: 内存不足
编辑 `config/config.yaml`:
```yaml
document:
  chunk_size: 300  # 减小chunk大小
retrieval:
  top_k: 3  # 减少检索数量
```

### Q4: 查询速度慢
可能原因：
1. 模型下载中：首次使用需要下载embedding模型
2. CPU计算：使用GPU可大幅提升速度
3. 文档过多：考虑清理不需要的文档

### Q5: 如何更改密码？
```bash
# 使用--password参数指定新密码
python cli.py --password "new_password" query "test"
```

## 进阶使用

### 自定义配置

编辑 `config/config.yaml`:

```yaml
# 更改模型
embedding:
  model_name: "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"

llm:
  model_name: "llama3.2:3b"  # 使用更大的模型
  temperature: 0.5  # 降低随机性

# 调整检索
retrieval:
  top_k: 10  # 检索更多结果
  similarity_threshold: 0.6  # 降低相似度阈值
```

### 使用Docker部署

```bash
# 使用docker-compose一键启动
docker-compose up -d

# 检查服务状态
docker-compose ps

# 查看日志
docker-compose logs -f rag-api
```

### 启用GPU加速

编辑 `config/config.yaml`:
```yaml
embedding:
  device: "cuda"  # 使用GPU
```

## 性能优化建议

1. **使用SSD存储**：向量数据库性能依赖磁盘I/O
2. **增加内存**：至少8GB RAM，推荐16GB+
3. **GPU加速**：使用GPU可提升10-50倍速度
4. **批量处理**：一次导入多个文档而非逐个导入
5. **缓存策略**：常用查询可以缓存结果

## 下一步

- 📖 阅读完整文档：[README.md](README.md)
- 🎓 论文写作指导：[docs/THESIS_GUIDE.md](docs/THESIS_GUIDE.md)
- 🔬 运行测试：`pytest tests/ -v`
- 🚀 生产部署：使用Docker和反向代理

## 获取帮助

```bash
# CLI帮助
python cli.py --help

# 查看API文档
# 启动服务后访问: http://localhost:8000/docs
```

---

**提示**：首次运行会下载embedding模型（约90MB），请耐心等待。
