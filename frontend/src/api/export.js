import request from './request'

const saveBlob = (blob, filename) => {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

const download = async (url, params, filename) => {
  const blob = await request.get(url, { params, responseType: 'blob' })
  // 后端返回 JSON 说明导出失败（如月份格式错误）
  if (blob.type && blob.type.includes('json')) {
    const text = await blob.text()
    throw new Error(JSON.parse(text).msg || '导出失败')
  }
  saveBlob(blob, filename)
}

export const exportSportExcel = (month) => download('/sport/export', { month }, `运动记录_${month}.xlsx`)
export const exportDietExcel = (month) => download('/diet/export', { month }, `饮食记录_${month}.xlsx`)
