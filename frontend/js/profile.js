import api from './api.js';
import { isLoggedIn, getFullUrl} from './utils.js';

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
const mottoInput = document.getElementById('motto');
const avatarInput = document.getElementById('avatar');         // 隐藏域，存储头像URL
const avatarPreview = document.getElementById('avatarPreview');
const avatarFile = document.getElementById('avatarFile');
const uploadAvatarBtn = document.getElementById('uploadAvatarBtn');

// 加载用户信息并填充表单
async function loadProfile() {
    try {
        const user = await api.getProfile();
        usernameInput.value = user.username || '';
        emailInput.value = user.email || '';
        phoneInput.value = user.phone || '';
        nicknameInput.value = user.nickname || '';
        mottoInput.value = user.motto || '';
        if (user.avatar) {
            avatarInput.value = user.avatar;
            avatarPreview.src = getFullUrl(user.avatar);
            avatarPreview.style.display = 'block';
        } else {
            avatarPreview.style.display = 'none';
        }
    } catch (err) {
        alert('加载个人信息失败：' + err.message);
        console.error(err);
    }
}

// 上传头像
uploadAvatarBtn.addEventListener('click', async () => {
    const file = avatarFile.files[0];
    if (!file) {
        alert('请先选择图片文件');
        return;
    }
    if (!file.type.startsWith('image/')) {
        alert('只能上传图片文件');
        return;
    }
    if (file.size > 2 * 1024 * 1024) {
        alert('图片大小不能超过10MB');
        return;
    }
    const formData = new FormData();
    formData.append('file', file);
    try {
        const token = localStorage.getItem('token');  // 获取 token
        const response = await axios.post('http://localhost:8080/api/upload/avatar', formData, {
            headers: { 
                'Content-Type': 'multipart/form-data',
                'Authorization': `Bearer ${token}`   // 添加认证头
            }
        });
        if (response.data.code === 200) {
            const avatarUrl = response.data.data;
            avatarInput.value = avatarUrl;
            avatarPreview.src = getFullUrl(avatarUrl);  // 添加 getFullUrl
            avatarPreview.style.display = 'block';
            alert('头像上传成功');
        } else {
            alert('上传失败：' + response.data.message);
        }
    } catch (err) {
        alert('上传失败：' + (err.response?.data?.message || err.message));
    }
});

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
        await loadProfile();  // 重新加载显示最新信息
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
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    } catch (err) {
        alert('修改密码失败：' + err.message);
    }
});

// 初始加载
loadProfile();