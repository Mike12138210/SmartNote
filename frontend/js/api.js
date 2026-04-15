import { getToken, removeToken } from './utils.js';

// 确保 axios 已经通过 HTML 的 script 标签全局加载
if (typeof axios === 'undefined') {
    console.error('axios 未加载，请检查 HTML 中是否引入 axios CDN');
}

// 创建 axios 实例
const api = axios.create({
    baseURL: 'http://localhost:8080/api',   // 后端接口地址
    timeout: 10000,
});

// 请求拦截器：自动添加 token
api.interceptors.request.use(
    config => {
        const token = getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => Promise.reject(error)
);

// 响应拦截器：统一处理错误
api.interceptors.response.use(
    response => {
        // 后端返回格式 { code, message, data }
        if (response.data.code === 200) {
            return response.data.data;   // 直接返回 data
        } else {
            // 业务错误
            throw new Error(response.data.message || '请求失败');
        }
    },
    error => {
        if (error.response?.status === 401) {
            // token 失效，跳转登录
            removeToken();
            window.location.href = '/login.html';
        }
        return Promise.reject(error);
    }
);

// 导出具体接口
export default {
    // 用户相关
    register(data) {
        return api.post('/auth/register', data);
    },
    login(data) {
        return api.post('/auth/login', data);
    },
    getProfile() {
        return api.get('/users/me');
    },
    updateProfile(data) {
        return api.put('/users/me/profile', data);
    },
    updatePassword(data) {
        return api.put('/users/me/password', data);
    },
    // 笔记相关
    getNotes(params) {
        return api.get('/notes', { params });
    },
    getNote(id) {
        return api.get(`/notes/${id}`);
    },
    createNote(data) {
        return api.post('/notes', data);
    },
    updateNote(data) {
        return api.patch('/notes', data);
    },
    deleteNote(id) {
        return api.delete(`/notes/${id}`);
    },
    analyzeNote(id, force = false) {
        return api.post(`/notes/${id}/analyze`, null, { params: { force } });
    },
    getPublicNote(id) {
        return api.get(`/notes/public/${id}`);
    },
    getRecentHistory(limit = 10) {
        return api.get('/notes/history/recent', { params: { limit } });
    },
    updateNotePermission(noteId, permission) {
    return api.put(`/notes/${noteId}/permission?permission=${encodeURIComponent(permission)}`);
    },

    // 好友相关
    searchUsers(keyword) {
        return api.get('/friends/search', { params: { keyword } });
    },
    sendFriendRequest(friendId) {
        return api.post('/friends/requests', { friendId });
    },
    getPendingRequests() {
        return api.get('/friends/requests/pending');
    },
    approveRequest(applyId) {
        return api.put(`/friends/requests/${applyId}/approve`);
    },
    rejectRequest(applyId) {
        return api.put(`/friends/requests/${applyId}/reject`);
    },
    getFriends(pageNum = 1, pageSize = 10, group = '') {
        return api.get('/friends', { params: { pageNum, pageSize, group } });
    },
};