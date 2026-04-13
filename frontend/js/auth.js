import api from './api.js';
import { setToken, removeToken } from './utils.js';

// 注册
const registerForm = document.getElementById('registerForm');
if(registerForm){
    registerForm.addEventListener('submit',async function(event){
        event.preventDefault();
        const username = document.getElementById('username').value.trim();
        const contact = document.getElementById('contact').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        
        if(!username){
            alert('用户名不能为空');
            return;
        }
        if(password !== confirmPassword){
            alert('两次密码不一致');
            return;
        }
        
        // 简单判断：包含@视为邮箱，否则视为手机号
        let email = null;
        let phone = null;
        if (contact.includes('@')) {
            email = contact;
        } else {
            phone = contact;
        }

        const requestData = {
            username: username,
            email: email || null,
            phone: phone || null,
            password: password,
            confirmPassword: confirmPassword
        };

        try{
            const response = await axios.post('http://localhost:8080/api/auth/register',requestData);
            alert(response.data.message || '注册成功，希望您使用愉快!');
            window.location.href = 'login.html';
        }catch(error){
            const msg = error.response?.data?.message || '注册失败，请稍后重试';
            alert(msg);
        }
    });
}

// 登录
const loginForm = document.getElementById('loginForm');
if(loginForm){
    loginForm.addEventListener('submit',async(e) =>{
        e.preventDefault();
        const account = document.getElementById('account').value;
        const password = document.getElementById('password').value;
        try{
            const data = await api.login({account, password});
            setToken(data.token);
            window.location.href = 'index.html';
        }catch(err){
            alert(err.message);
        }
    });
}