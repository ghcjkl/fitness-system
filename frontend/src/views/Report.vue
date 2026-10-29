<template>
  <div class="report-page">
    <section class="panel upload-panel">
      <div class="panel-head">
        <div>
          <h3>体检报告归档</h3>
          <p>上传 PDF 或图片报告，长期留存，随时回看</p>
        </div>
        <div class="stat">
          <strong>{{ list.length }}</strong>
          <span>份已归档</span>
        </div>
      </div>

      <div class="upload-body">
        <el-upload
          drag
          class="uploader"
          accept=".pdf,.jpg,.jpeg,.png"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleFileChange"
        >
          <div class="upload-inner">
            <span class="upload-icon"><app-icon name="upload" :size="24" /></span>
            <strong>{{ selected ? selected.name : '拖拽报告到这里，或点击选择文件' }}</strong>
            <span>{{ selected ? formatSize(selected.size) + ' · 点击可重新选择' : '支持 PDF / JPG / PNG，单个不超过 10MB' }}</span>
          </div>
        </el-upload>

        <div class="upload-meta">
          <el-date-picker
            v-model="reportDate"
            type="date"
            placeholder="体检日期（选填）"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
          <el-button type="primary" :loading="uploading" :disabled="!selected" @click="handleUpload">
            上传归档
          </el-button>
          <span class="hint">报告仅本人可见，可随时删除</span>
        </div>
      </div>
    </section>

    <section class="panel list-panel">
      <div class="panel-head">
        <div>
          <h3>我的报告</h3>
          <p>按上传时间倒序排列</p>
        </div>
        <button class="refresh" type="button" title="刷新" @click="load">
          <app-icon name="refresh" :size="17" />
        </button>
      </div>

      <div v-loading="loading" class="report-grid">
        <article v-for="item in list" :key="item.id" class="report-card">
          <div class="file-badge" :class="item.fileType === 'pdf' ? 'is-pdf' : 'is-img'">
            <app-icon :name="item.fileType === 'pdf' ? 'file' : 'image'" :size="21" />
            <span>{{ (item.fileType || '').toUpperCase() }}</span>
          </div>
          <div class="file-main">
            <strong :title="item.fileName">{{ item.fileName }}</strong>
            <div class="file-meta">
              <span>{{ item.reportDate || formatTime(item.createTime) }}</span>
              <em>{{ formatSize(item.fileSize) }}</em>
            </div>
          </div>
          <div class="file-actions">
            <button type="button" title="预览" @click="openPreview(item)">
              <app-icon name="eye" :size="17" />
            </button>
            <button type="button" title="下载" @click="handleDownload(item)">
              <app-icon name="download" :size="17" />
            </button>
            <button type="button" class="danger" title="删除" @click="handleDelete(item)">
              <app-icon name="trash" :size="17" />
            </button>
          </div>
        </article>
      </div>

      <el-empty v-if="!loading && !list.length" description="还没有上传体检报告，先归档第一份吧" :image-size="96" />
    </section>

    <el-dialog
      v-model="previewVisible"
      :title="previewItem.fileName || '报告预览'"
      width="860px"
      class="preview-dialog"
      destroy-on-close
    >
      <div v-if="previewItem.fileType === 'pdf'" class="preview-box">
        <iframe :src="previewUrl" title="PDF 预览" />
      </div>
      <div v-else class="preview-box image-box">
        <img :src="previewUrl" :alt="previewItem.fileName" />
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleDownload(previewItem)">下载文件</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadReport, reportList, deleteReport } from '../api/report'
import AppIcon from '../components/AppIcon.vue'

const list = ref([])
const loading = ref(false)
const uploading = ref(false)
const selected = ref(null)
const reportDate = ref('')

const previewVisible = ref(false)
const previewItem = ref({})
const previewUrl = computed(() => (previewItem.value.filePath ? `/upload/${previewItem.value.filePath}` : ''))

const load = async () => {
  loading.value = true
  try {
    const res = await reportList()
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '报告加载失败')
  } finally {
    loading.value = false
  }
}

const handleFileChange = (uploadFile) => {
  const raw = uploadFile.raw
  if (!raw) return
  const ext = raw.name.substring(raw.name.lastIndexOf('.') + 1).toLowerCase()
  if (!['pdf', 'jpg', 'jpeg', 'png'].includes(ext)) {
    return ElMessage.warning('仅支持 PDF、JPG、JPEG、PNG 格式')
  }
  if (raw.size > 10 * 1024 * 1024) {
    return ElMessage.warning('文件不能超过 10MB')
  }
  selected.value = raw
}

const handleUpload = async () => {
  if (!selected.value) return
  uploading.value = true
  try {
    const res = await uploadReport(selected.value, reportDate.value)
    ElMessage.success(res.msg || '报告上传成功')
    selected.value = null
    reportDate.value = ''
    await load()
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

const openPreview = (item) => {
  previewItem.value = item
  previewVisible.value = true
}

const handleDownload = (item) => {
  const a = document.createElement('a')
  a.href = `/upload/${item.filePath}`
  a.download = item.fileName || 'report'
  a.click()
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm(`确定删除「${item.fileName}」吗？删除后不可恢复。`, '删除报告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    const res = await deleteReport(item.id)
    ElMessage.success(res.msg || '报告已删除')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

const formatSize = (size) => {
  if (!size && size !== 0) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(2)} MB`
}

const formatTime = (time) => (time ? String(time).replace('T', ' ').slice(0, 16) : '-')

onMounted(load)
</script>

<style scoped>
.report-page {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel {
  padding: 22px 24px 24px;
  border-radius: 20px;
  border: 1px solid rgba(20, 53, 42, 0.06);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-head h3 {
  font-size: 16px;
  letter-spacing: -0.01em;
}

.panel-head p {
  margin-top: 4px;
  font-size: 12px;
  color: var(--muted);
}

.stat {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 14px;
  background: #f3f8ee;
  color: var(--brand-2);
}

.stat strong {
  font-size: 20px;
  line-height: 1;
  color: var(--brand);
}

.stat span {
  font-size: 11.5px;
  color: var(--ink-soft);
}

.upload-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 16px;
}

.uploader :deep(.el-upload) {
  width: 100%;
}

.uploader :deep(.el-upload-dragger) {
  width: 100%;
  padding: 26px 20px;
  border: 1.5px dashed #d3e0d4;
  border-radius: 16px;
  background: #f8fbf5;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.uploader :deep(.el-upload-dragger:hover) {
  border-color: var(--brand-3);
  background: #f3f9ee;
}

.upload-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.upload-icon {
  width: 46px;
  height: 46px;
  margin-bottom: 2px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: var(--lime);
  color: var(--brand);
  box-shadow: 0 8px 18px rgba(200, 241, 105, 0.32);
}

.upload-inner strong {
  max-width: 100%;
  font-size: 13.5px;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-inner span {
  font-size: 11.5px;
  color: var(--muted);
}

.upload-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

.upload-meta .hint {
  font-size: 11px;
  color: var(--muted);
  text-align: center;
}

.list-panel .panel-head {
  margin-bottom: 14px;
}

.refresh {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  border: 1px solid var(--line);
  background: #fafcf9;
  color: var(--ink-soft);
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease;
}

.refresh:hover {
  color: var(--brand-2);
  border-color: var(--brand-3);
}

.report-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  min-height: 40px;
}

.report-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 14px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: #fcfdfb;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.report-card:hover {
  border-color: #cfe0d2;
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.file-badge {
  flex-shrink: 0;
  width: 44px;
  height: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 10px;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.file-badge.is-pdf {
  background: #fdeceb;
  color: #c0392b;
}

.file-badge.is-img {
  background: #e9f3e4;
  color: var(--brand-3);
}

.file-main {
  min-width: 0;
  flex: 1;
}

.file-main strong {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 5px;
  font-size: 11.5px;
  color: var(--muted);
}

.file-meta em {
  font-style: normal;
  padding: 1px 7px;
  border-radius: 999px;
  background: #f1f4f0;
}

.file-actions {
  display: flex;
  gap: 6px;
}

.file-actions button {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 9px;
  border: 1px solid var(--line);
  background: #ffffff;
  color: var(--ink-soft);
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.file-actions button:hover {
  color: var(--brand-2);
  border-color: var(--brand-3);
  background: #f6faf3;
}

.file-actions button.danger:hover {
  color: #c0392b;
  border-color: #e6b3ae;
  background: #fdf3f2;
}

.preview-box {
  height: 68vh;
  border-radius: 12px;
  overflow: hidden;
  background: #f6f8f5;
}

.preview-box iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.image-box {
  display: grid;
  place-items: center;
  overflow: auto;
  padding: 10px;
}

.image-box img {
  max-width: 100%;
  max-height: 100%;
  border-radius: 8px;
}

@media (max-width: 720px) {
  .upload-body {
    grid-template-columns: minmax(0, 1fr);
  }

  .report-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .panel {
    padding: 18px;
  }

  .report-card {
    gap: 10px;
    padding: 12px;
  }

  .file-main strong {
    font-size: 13px;
  }

  .file-meta {
    flex-wrap: wrap;
    row-gap: 2px;
  }

  .file-actions {
    gap: 4px;
  }
}
</style>
