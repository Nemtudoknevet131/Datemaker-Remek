import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  logout: () => api.post('/auth/logout'),
  getCurrentUser: () => api.get('/auth/me'),
};

export const profileAPI = {
  getProfile: (userId) => api.get(`/profiles/${userId}`),
  getMyProfile: () => api.get('/profiles/me'),
  updateProfile: (profileData) => api.put('/profiles/me', profileData),
  uploadPhoto: (formData) => api.post('/profiles/me/photos', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  deletePhoto: (photoId) => api.delete(`/profiles/me/photos/${photoId}`),
};

export const postsAPI = {
  getFeed: (page = 0, size = 10) => api.get(`/posts?page=${page}&size=${size}`),
  getPost: (postId) => api.get(`/posts/${postId}`),
  createPost: (postData) => api.post('/posts', postData),
  updatePost: (postId, postData) => api.put(`/posts/${postId}`, postData),
  deletePost: (postId) => api.delete(`/posts/${postId}`),
  likePost: (postId) => api.post(`/posts/${postId}/like`),
  unlikePost: (postId) => api.delete(`/posts/${postId}/like`),
  getComments: (postId) => api.get(`/posts/${postId}/comments`),
  addComment: (postId, content) => api.post(`/posts/${postId}/comments`, { content }),
};

export const matchesAPI = {
  getMatches: () => api.get('/matches'),
  getSuggestedMatches: () => api.get('/matches/suggestions'),
  likeUser: (userId) => api.post(`/matches/like/${userId}`),
  passUser: (userId) => api.post(`/matches/pass/${userId}`),
  unmatch: (matchId) => api.delete(`/matches/${matchId}`),
};

export const chatAPI = {
  sendMessage: (message) => api.post('/chat/ai', { message }),
  getChatHistory: () => api.get('/chat/history'),
};

export const locationsAPI = {
  getNearbyPlaces: (lat, lng, radius = 5000, category = 'all') => 
    api.get(`/places?lat=${lat}&lng=${lng}&radius=${radius}&category=${category}`),
  getPlaceDetails: (placeId) => api.get(`/places/${placeId}`),
  saveFavoritePlace: (placeId) => api.post(`/places/${placeId}/favorite`),
  getFavoritePlaces: () => api.get('/places/favorites'),
};

export default api;
