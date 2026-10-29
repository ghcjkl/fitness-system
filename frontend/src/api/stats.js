import request from './request'

export const getTrend = (days) => request.get('/stats/trend', { params: { days } })
