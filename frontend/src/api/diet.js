import request from './request'

export const addDiet = (data) => request.post('/diet/add', data)
export const dietPage = (params) => request.get('/diet/page', { params })
export const deleteDiet = (id) => request.delete(`/diet/delete/${id}`)
export const dietStats = (date) => request.get('/diet/stats', { params: { date } })
export const dietGoal = (date) => request.get('/diet/goal', { params: { date } })
export const recommendDiet = (params) => request.get('/diet/recommend', { params })
export const uploadDiet = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/diet/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}