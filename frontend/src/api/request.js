import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 20000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.token = token
  return config
})

request.interceptors.response.use(res => {
  // 文件下载直接返回二进制数据
  if (res.config.responseType === 'blob') return res.data
  const r = res.data
  if (r.code === 200) return r
  if (r.code === 401) { localStorage.removeItem('token'); location.href = '/login' }
  return Promise.reject(new Error(r.msg))
}, err => Promise.reject(err))

export default request