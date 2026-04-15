// 存储token
export function setToken(token){
    localStorage.setItem('token',token);
}

// 获取token
export function getToken(){
    return localStorage.getItem('token');
}

// 移除token
export function removeToken(){
    localStorage.removeItem('token');
}

// 检查是否登录
export function isLoggedIn(){
    return !!getToken();
}

// 获取后端基础 URL（不含 /api）
export const BACKEND_BASE_URL = 'http://localhost:8080';

// 将相对路径（如 /uploads/xxx.jpg）转换为完整 URL
export function getFullUrl(path) {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    return BACKEND_BASE_URL + path;
}