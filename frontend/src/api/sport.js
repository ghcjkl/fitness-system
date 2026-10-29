import request from './request'

export const addSport = (data) => request.post('/sport/add', data)
export const sportPage = (params) => request.get('/sport/page', { params })
export const deleteSport = (id) => request.delete(`/sport/delete/${id}`)
export const getMetList = () => request.get('/sport/met-list')
export const calcSportCalorie = (params) => request.get('/sport/calorie', { params })
export const getSportStreak = () => request.get('/sport/streak')
