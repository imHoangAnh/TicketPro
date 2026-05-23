/* eslint-disable no-unused-vars */
import axios from 'axios';

const API_BASE_URL = 'http://localhost:1122'; // Backend REST port

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Variables to handle atomic token refresh
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Request Interceptor: Attach access token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token && !config.headers['Authorization']) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle 401 Unauthorized with atomic token refresh rotation
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Avoid infinite loop if login or refresh request fails
    if (
      error.response &&
      error.response.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url.includes('/api/auth/login') &&
      !originalRequest.url.includes('/api/auth/refresh')
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(error));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Post refresh token held in HttpOnly cookie to /api/auth/refresh
        const response = await axios.post(
          `${API_BASE_URL}/api/auth/refresh`,
          {},
          {
            withCredentials: true,
            headers: {
              'Content-Type': 'application/json',
              'X-Requested-With': 'XMLHttpRequest', // Necessary for cookie mutations
            },
          }
        );

        const newAccessToken = response.data.result.accessToken;
        localStorage.setItem('accessToken', newAccessToken);
        if (response.data.result.user) {
          localStorage.setItem('user', JSON.stringify(response.data.result.user));
        }

        api.defaults.headers.common['Authorization'] = `Bearer ${newAccessToken}`;
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;

        processQueue(null, newAccessToken);
        isRefreshing = false;

        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;

        // Logout user and dispatch event so App can redirect to login
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        window.dispatchEvent(new CustomEvent('auth-logout', { detail: { sessionExpired: true } }));

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export const authService = {
  login: async ({ email, password }) => {
    const response = await api.post('/api/auth/login', { email, password });
    const { accessToken, user } = response.data.result;
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('user', JSON.stringify(user));
    return response.data.result;
  },

  register: async ({ email, password, fullName }) => {
    const response = await api.post('/api/auth/register', {
      email,
      password,
      fullName,
    });
    return response.data;
  },

  logout: async () => {
    try {
      await api.post(
        '/api/auth/logout',
        {},
        {
          withCredentials: true,
          headers: {
            'X-Requested-With': 'XMLHttpRequest',
          },
        }
      );
    } catch (err) {
      console.warn('Logout endpoint warning:', err);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.dispatchEvent(new CustomEvent('auth-logout', { detail: { sessionExpired: false } }));
    }
  },

  getMe: async () => {
    const response = await api.get('/api/auth/me');
    return response.data.result;
  },
};

export const eventService = {
  getEvents: async () => {
    const response = await api.get('/api/events');
    return response.data.result; // Returns list of active events
  },

  getEventById: async (eventId) => {
    const response = await api.get(`/api/events/${eventId}`);
    return response.data.result;
  },
};

export const orderService = {
  createOrder: async ({ ticketTypeId, quantity }) => {
    const response = await api.post('/api/orders', { ticketTypeId, quantity });
    return response.data.result;
  },

  getMyOrders: async () => {
    const response = await api.get('/api/orders/my');
    return response.data.result;
  },

  getOrderById: async (orderId) => {
    const response = await api.get(`/api/orders/${orderId}`);
    return response.data.result;
  },

  cancelOrder: async (orderId) => {
    const response = await api.put(`/api/orders/${orderId}/cancel`);
    return response.data.result;
  },
};

export const paymentService = {
  payMockSuccess: async (orderId) => {
    const response = await api.post(`/api/payments/${orderId}/mock-success`);
    return response.data.result;
  },

  getVNPAYUrl: async (orderId) => {
    const response = await api.post(`/api/payments/${orderId}/vnpay`);
    return response.data.result; // { paymentUrl }
  },
};

export const adminService = {
  // Event CRUD
  createEvent: async (payload) => {
    const response = await api.post('/api/admin/events', payload);
    return response.data.result;
  },

  updateEvent: async (eventId, payload) => {
    const response = await api.put(`/api/admin/events/${eventId}`, payload);
    return response.data.result;
  },

  deleteEvent: async (eventId) => {
    const response = await api.delete(`/api/admin/events/${eventId}`);
    return response.data.result;
  },

  setEventStatus: async (eventId, active) => {
    const action = active ? 'active' : 'inactive';
    const response = await api.put(`/api/admin/events/${eventId}/${action}`);
    return response.data.result;
  },

  // Ticket Type CRUD
  createTicketType: async (eventId, payload) => {
    const response = await api.post(`/api/admin/events/${eventId}/ticket-types`, payload);
    return response.data.result;
  },

  updateTicketType: async (ticketTypeId, payload) => {
    const response = await api.put(`/api/admin/ticket-types/${ticketTypeId}`, payload);
    return response.data.result;
  },

  deleteTicketType: async (ticketTypeId) => {
    const response = await api.delete(`/api/admin/ticket-types/${ticketTypeId}`);
    return response.data.result;
  },

  // Orders list
  getOrders: async () => {
    const response = await api.get('/api/admin/orders');
    return response.data.result;
  },

  getOrderById: async (orderId) => {
    const response = await api.get(`/api/admin/orders/${orderId}`);
    return response.data.result;
  },
};

export default api;
