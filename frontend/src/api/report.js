import request from './request'

export const uploadReport = (file, reportDate) => {
  const fd = new FormData()
  fd.append('file', file)
  if (reportDate) fd.append('reportDate', reportDate)
  return request.post('/report/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export const reportList = () => request.get('/report/list')
export const reportDetail = (id) => request.get(`/report/${id}`)
export const deleteReport = (id) => request.delete(`/report/${id}`)
