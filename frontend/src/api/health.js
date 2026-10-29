import request from './request'

export const saveHealth = (data) => request.post('/health/save', data)
export const getHealth = () => request.get('/health/get')