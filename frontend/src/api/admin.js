import request from './request'

export const getAdminUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) =>
  request.post(`/admin/user/${id}/status`, null, { params: { status } })
export const resetUserPassword = (id) => request.post(`/admin/user/${id}/reset-password`)
export const adminDeleteCommunity = (id) => request.delete(`/admin/community/${id}`)
export const getAdminLogs = (params) => request.get('/admin/logs', { params })

export const getAdminFoods = (params) => request.get('/admin/foods', { params })
export const createFood = (data) => request.post('/admin/food', data)
export const updateFood = (id, data) => request.put(`/admin/food/${id}`, data)
export const deleteFood = (id) => request.delete(`/admin/food/${id}`)
