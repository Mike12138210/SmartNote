import api from './api.js';
import { isLoggedIn } from './utils.js';

// 检查登录状态
if (!isLoggedIn()) {
    window.location.href = 'login.html';
}

// DOM 元素
const profileForm = document.getElementById('profileForm');
const passwordForm = document.getElementById('passwordForm');
const usernameInput = document.getElementById('username');
const emailInput = document.getElementById('email');
const phoneInput = document.getElementById('phone');
const nicknameInput = document.getElementById('nickname');
const avatarInput = document.getElementById('avatar');
const mottoInput = document.getElementById('motto');

// 加载用户信息并填充表单
async function loadProfile() {
    try {
        const user = await api.getProfile();
        usernameInput.value = user.username || '';
        emailInput.value = user.email || '';
        phoneInput.value = user.phone || '';
        nicknameInput.value = user.nickname || '';
        avatarInput.value = user.avatar || '';
        mottoInput.value = user.motto || '';
    } catch (err) {
        alert('加载个人信息失败：' + err.message);
        console.error(err);
    }
}

// 保存个人信息
profileForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        nickname: nicknameInput.value.trim(),
        avatar: avatarInput.value.trim(),
        motto: mottoInput.value.trim()
    };
    try {
        await api.updateProfile(data);
        alert('保存成功');
        // 重新加载以显示最新信息
        await loadProfile();
    } catch (err) {
        alert('保存失败：' + err.message);
    }
});

// 修改密码
passwordForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (!oldPassword || !newPassword || !confirmPassword) {
        alert('请填写完整');
        return;
    }
    if (newPassword !== confirmPassword) {
        alert('两次输入的新密码不一致');
        return;
    }
    if (newPassword.length < 6) {
        alert('新密码长度至少6位');
        return;
    }
    try {
        await api.updatePassword({ oldPassword, newPassword });
        alert('密码修改成功，请重新登录');
        // 清空 token 并跳转到登录页
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    } catch (err) {
        alert('修改密码失败：' + err.message);
    }
});

// 初始加载
loadProfile();