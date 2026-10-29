import request from './request'

export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const getUserInfo = () => request.get('/user/info')
export const updateProfile = (data) => request.post('/user/profile', data)
export const updatePassword = (data) => request.post('/user/password', data)
export const uploadAvatar = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/avatar', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}