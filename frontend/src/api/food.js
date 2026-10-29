import request from './request'

export const searchFood = (params) => request.get('/food/search', { params })
export const getFoodCategories = () => request.get('/food/categories')
export const getFoodDetail = (id) => request.get(`/food/${id}`)
