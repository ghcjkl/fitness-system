import request from './request'

export const generatePlan = (type, data = {}) => request.post(`/ai/plan/${type}`, data)
export const getLatestPlan = (type) => request.get('/ai/plan/latest', { params: { type } })
export const planHistory = (params) => request.get('/ai/plan/history', { params })
// 大模型生成耗时较长，单独放宽超时，避免全局 20 秒超时中断
export const aiChat = (question) => request.post('/ai/chat', { question }, { timeout: 120000 })
