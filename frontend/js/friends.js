import api from './api.js';
import { isLoggedIn, getToken } from './utils.js';

// 检查登录状态
if (!isLoggedIn()) {
    window.location.href = 'login.html';
}

// DOM 元素
const searchKeyword = document.getElementById('searchKeyword');
const searchBtn = document.getElementById('searchBtn');
const searchResult = document.getElementById('searchResult');
const pendingListDiv = document.getElementById('pendingList');
const friendListDiv = document.getElementById('friendList');
const groupFilter = document.getElementById('groupFilter');
const friendPagination = document.getElementById('friendPagination');

let currentFriendPage = 1;
let friendPageSize = 10;
let totalFriendPages = 0;
let currentGroup = '';

// ========== 搜索用户 ==========
searchBtn.addEventListener('click', async () => {
    const keyword = searchKeyword.value.trim();
    if (!keyword) {
        alert('请输入搜索关键词');
        return;
    }
    try {
        const users = await api.searchUsers(keyword);
        renderSearchResult(users);
    } catch (err) {
        alert('搜索失败：' + err.message);
    }
});

function renderSearchResult(users) {
    if (!users.length) {
        searchResult.innerHTML = '<div>未找到用户</div>';
        return;
    }
    let html = '';
    for (const user of users) {
        html += `
            <div class="user-item">
                <span>${escapeHtml(user.nickname || user.username)} (${user.username})</span>
                <button class="btn-sm btn" data-id="${user.id}" data-action="addFriend">添加好友</button>
            </div>
        `;
    }
    searchResult.innerHTML = html;
    // 绑定添加好友事件
    document.querySelectorAll('[data-action="addFriend"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const friendId = parseInt(btn.dataset.id);
            await sendFriendRequest(friendId);
        });
    });
}

async function sendFriendRequest(friendId) {
    try {
        const result = await api.sendFriendRequest(friendId);
        if (result) {
            // 如果返回好友信息（已是好友）
            alert(`你们已经是好友了：${result.nickname || result.username}`);
        } else {
            alert('好友申请已发送');
        }
        // 刷新待处理列表和好友列表
        loadPendingRequests();
        loadFriends();
    } catch (err) {
        alert('发送失败：' + err.message);
    }
}

// ========== 待处理申请 ==========
async function loadPendingRequests() {
    try {
        const applies = await api.getPendingRequests();
        renderPendingRequests(applies);
    } catch (err) {
        pendingListDiv.innerHTML = '<div>加载失败</div>';
        console.error(err);
    }
}

function renderPendingRequests(applies) {
    if (!applies.length) {
        pendingListDiv.innerHTML = '<div>暂无待处理申请</div>';
        return;
    }
    let html = '';
    for (const apply of applies) {
        html += `
            <div class="pending-item">
                <span>${escapeHtml(apply.fromUser.nickname || apply.fromUser.username)} (${apply.fromUser.username}) 申请添加好友</span>
                <div>
                    <button class="btn-sm btn" data-id="${apply.applyId}" data-action="approve">同意</button>
                    <button class="btn-sm btn-danger" data-id="${apply.applyId}" data-action="reject">拒绝</button>
                </div>
            </div>
        `;
    }
    pendingListDiv.innerHTML = html;
    // 绑定同意/拒绝事件
    document.querySelectorAll('[data-action="approve"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const applyId = parseInt(btn.dataset.id);
            await handleRequest(applyId, 'approve');
        });
    });
    document.querySelectorAll('[data-action="reject"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const applyId = parseInt(btn.dataset.id);
            await handleRequest(applyId, 'reject');
        });
    });
}

async function handleRequest(applyId, action) {
    try {
        if (action === 'approve') {
            await api.approveRequest(applyId);
            alert('已添加好友');
        } else {
            await api.rejectRequest(applyId);
            alert('已拒绝');
        }
        // 刷新列表
        loadPendingRequests();
        loadFriends();
    } catch (err) {
        alert('操作失败：' + err.message);
    }
}

// ========== 好友列表 ==========
async function loadFriends(page = 1) {
    currentFriendPage = page;
    try {
        const data = await api.getFriends(currentFriendPage, friendPageSize, currentGroup);
        renderFriends(data.records);
        totalFriendPages = data.pages;
        renderFriendPagination();
    } catch (err) {
        friendListDiv.innerHTML = '<div>加载失败</div>';
        console.error(err);
    }
}

function renderFriends(friends) {
    if (!friends.length) {
        friendListDiv.innerHTML = '<div>暂无好友</div>';
        return;
    }
    let html = '';
    for (const friend of friends) {
        html += `
            <div class="friend-item">
                <span>${escapeHtml(friend.nickname || friend.username)} (${friend.username}) - ${friend.groupName}</span>
                <!-- 可扩展修改分组功能，暂不实现 -->
            </div>
        `;
    }
    friendListDiv.innerHTML = html;
}

function renderFriendPagination() {
    if (totalFriendPages <= 1) {
        friendPagination.innerHTML = '';
        return;
    }
    let html = `<button class="btn-sm" data-page="1">首页</button>
                <button class="btn-sm" data-page="${currentFriendPage-1}" ${currentFriendPage===1 ? 'disabled' : ''}>上一页</button>
                <span>第 ${currentFriendPage} / ${totalFriendPages} 页</span>
                <button class="btn-sm" data-page="${currentFriendPage+1}" ${currentFriendPage===totalFriendPages ? 'disabled' : ''}>下一页</button>
                <button class="btn-sm" data-page="${totalFriendPages}">末页</button>`;
    friendPagination.innerHTML = html;
    document.querySelectorAll('#friendPagination [data-page]').forEach(btn => {
        btn.addEventListener('click', () => {
            const page = parseInt(btn.dataset.page);
            if (!isNaN(page) && page >=1 && page <= totalFriendPages) {
                loadFriends(page);
            }
        });
    });
}

// 分组筛选：下拉框变化时自动触发
if (groupFilter) {
    groupFilter.addEventListener('change', () => {
        currentGroup = groupFilter.value;
        loadFriends(1);
    });
}

// 防XSS
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

// 初始加载
loadPendingRequests();
loadFriends();