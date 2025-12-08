# Web UI for Privacy-Preserving RAG System

## 使用方法

### 1. 启动后端API

```bash
cd rag-system
python api.py
```

API 将在 `http://localhost:8000` 运行

### 2. 打开Web界面

在浏览器中打开 `web/index.html` 文件，或使用简单的HTTP服务器：

```bash
# Python 3
cd web
python -m http.server 8080

# 然后访问 http://localhost:8080
```

## 功能特性

### 查询功能
- 输入问题进行查询
- 显示答案和元数据
- 显示检索块数量和相似度
- 显示处理时间

### 文档上传
- 支持 TXT, MD, PDF, DOCX 格式
- 实时显示上传进度
- 显示处理结果和块数量

### 系统统计
- 实时显示文档块数量
- LLM 状态监控
- 嵌入模型信息

## 界面预览

```
┌─────────────────────────────────────────────┐
│  🔒 Privacy-Preserving RAG System          │
│  隐私保护的轻量级检索增强生成系统              │
└─────────────────────────────────────────────┘

┌─────────────────┬─────────────────────────┐
│ 💬 Query System │ 📄 Upload Document      │
│                 │                         │
│ [Question Box]  │ [File Upload]          │
│                 │                         │
│ [Search Button] │ [Upload Button]        │
│                 │                         │
│ [Results...]    │ [Upload Results...]    │
└─────────────────┴─────────────────────────┘

┌─────────────────────────────────────────────┐
│ 📊 System Statistics                       │
│                                             │
│ [142]      [✅ Ready]    [MiniLM-L6-v2]   │
│ Chunks     LLM Status    Embedding Model   │
└─────────────────────────────────────────────┘
```

## 注意事项

1. **CORS设置**: 确保API已启用CORS（默认已配置）
2. **API地址**: 如果API不在 `localhost:8000`，需修改 `index.html` 中的 `API_BASE` 变量
3. **文件大小**: 大文件上传可能需要较长时间
4. **浏览器兼容**: 推荐使用现代浏览器（Chrome, Firefox, Safari, Edge）

## 自定义

### 修改API地址

编辑 `index.html`，找到：

```javascript
const API_BASE = 'http://localhost:8000';
```

改为你的API地址。

### 修改样式

所有CSS样式都在 `<style>` 标签中，可以根据需要修改颜色、布局等。

### 添加功能

在 `<script>` 标签中添加新的JavaScript函数，并调用API端点。

## 故障排除

### 无法连接到API
- 检查API是否运行：`curl http://localhost:8000/api/health`
- 检查CORS设置
- 查看浏览器控制台错误信息

### 文件上传失败
- 检查文件格式是否支持
- 检查文件大小限制
- 查看API日志

### 查询无响应
- 检查Ollama是否运行：`ollama list`
- 检查Qdrant是否运行：`docker ps | grep qdrant`
- 确保已导入文档

## 扩展建议

可以添加的功能：
- 历史查询记录
- 文档列表管理
- 更详细的统计图表
- 实时日志显示
- 用户认证
- 主题切换（深色/浅色）
