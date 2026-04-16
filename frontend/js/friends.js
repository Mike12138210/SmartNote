import api from './api.js';
import { isLoggedIn } from './utils.js';

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

// 预设分组选项（用于下拉框）
const presetGroups = ['我的好友', '同学', '家人', '同事'];

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
            alert(`你们已经是好友了：${result.nickname || result.username}`);
        } else {
            alert('好友申请已发送');
        }
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
                <span>${escapeHtml(apply.username)} (${escapeHtml(apply.username)}) 申请添加好友</span>
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

// 修改分组：显示下拉框（预设 + 新建）
function renderFriends(friends) {
    if (!friends.length) {
        friendListDiv.innerHTML = '<div>暂无好友</div>';
        return;
    }
    let html = '';
    for (const friend of friends) {
        // 构建分组下拉框选项
        let groupOptions = '';
        for (const g of presetGroups) {
            const selected = (friend.groupName === g) ? 'selected' : '';
            groupOptions += `<option value="${escapeHtml(g)}" ${selected}>${escapeHtml(g)}</option>`;
        }
        // 添加一个“新建分组”选项（特殊值）
        groupOptions += `<option value="__NEW__">+ 新建分组</option>`;

        html += `
            <div class="friend-item" data-relation-id="${friend.relationId}">
                <div style="flex:2;">
                    <strong>${escapeHtml(friend.nickname || friend.username)}</strong> (${escapeHtml(friend.username)})
                </div>
                <div style="flex:1;">
                    <select class="group-select" data-relation-id="${friend.relationId}" data-current-group="${escapeHtml(friend.groupName)}">
                        ${groupOptions}
                    </select>
                </div>
                <div>
                    <button class="btn-sm btn update-group-btn" data-relation-id="${friend.relationId}">更新分组</button>
                </div>
            </div>
        `;
    }
    friendListDiv.innerHTML = html;

    // 绑定“更新分组”按钮事件
    document.querySelectorAll('.update-group-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const relationId = parseInt(btn.dataset.relationId);
            const select = document.querySelector(`.group-select[data-relation-id="${relationId}"]`);
            let newGroup = select.value;
            if (newGroup === '__NEW__') {
                // 弹出输入框让用户输入新分组名
                newGroup = prompt('请输入新分组名称：');
                if (!newGroup || newGroup.trim() === '') {
                    alert('分组名不能为空');
                    return;
                }
                // 可选：将新分组添加到预设列表中（下次直接可选）
                if (!presetGroups.includes(newGroup)) {
                    presetGroups.push(newGroup);
                }
            }
            // 调用后端接口修改分组
            try {
                await api.updateFriendGroup(relationId, newGroup);
                alert('分组修改成功');
                // 重新加载列表以显示新分组
                loadFriends(currentFriendPage);
            } catch (err) {
                alert('修改失败：' + err.message);
            }
        });
    });
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