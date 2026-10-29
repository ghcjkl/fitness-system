import request from './request'

export const publishCommunity = (content, imgFiles = []) => {
  const fd = new FormData()
  fd.append('content', content)
  imgFiles.forEach(file => fd.append('imgs', file))
  return request.post('/community/publish', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const communityPage = (params) => request.get('/community/page', { params })
export const likeCommunity = (id) => request.post(`/community/like/${id}`)
export const deleteCommunity = (id) => request.delete(`/community/delete/${id}`)
export const communityComments = (params) => request.get('/community/comments', { params })
export const addCommunityComment = (dynamicId, content) => request.post(`/community/comment/${dynamicId}`, { content })
export const deleteCommunityComment = (id) => request.delete(`/community/comment/${id}`)
