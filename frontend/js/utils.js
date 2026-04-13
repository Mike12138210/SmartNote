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