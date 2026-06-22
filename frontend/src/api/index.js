import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  response => response.data,
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export const scriptApi = {
  getAll: () => request.get('/scripts'),
  getById: (id) => request.get(`/scripts/${id}`),
  create: (data) => request.post('/scripts', data),
  update: (id, data) => request.put(`/scripts/${id}`, data),
  delete: (id) => request.delete(`/scripts/${id}`)
}

export const roomApi = {
  getAll: () => request.get('/rooms'),
  getById: (id) => request.get(`/rooms/${id}`),
  create: (data) => request.post('/rooms', data),
  update: (id, data) => request.put(`/rooms/${id}`, data),
  delete: (id) => request.delete(`/rooms/${id}`),
  getStatuses: () => request.get('/orders/rooms/status')
}

export const orderApi = {
  getActive: () => request.get('/orders/active'),
  getPending: () => request.get('/orders/pending'),
  getById: (id) => request.get(`/orders/${id}`),
  create: (data) => request.post('/orders', data),
  addPlayer: (data) => request.post('/orders/add-player', data),
  cancel: (id) => request.post(`/orders/${id}/cancel`),
  start: (id) => request.post(`/orders/${id}/start`),
  finish: (id) => request.post(`/orders/${id}/finish`)
}

export default request
