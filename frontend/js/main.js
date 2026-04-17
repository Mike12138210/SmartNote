import api from './api.js';
import { removeToken, isLoggedIn, getFullUrl } from './utils.js';

// 检查登录状态，未登录则跳转
if (!isLoggedIn()) {
    window.location.href = 'login.html';
}

// DOM 元素
const userInfoSpan = document.getElementById('userInfo');
const logoutBtn = document.getElementById('logoutBtn');
const profileBtn = document.getElementById('profileBtn');
const searchTitle = document.getElementById('searchTitle');
const searchTag = document.getElementById('searchTag');
const searchBtn = document.getElementById('searchBtn');
const addNoteBtn = document.getElementById('addNoteBtn');
const noteTableBody = document.getElementById('noteTableBody');
const paginationDiv = document.getElementById('pagination');
const modal = document.getElementById('noteModal');
const modalTitle = document.getElementById('modalTitle');
const noteIdInput = document.getElementById('noteId');
const noteTitleInput = document.getElementById('noteTitle');
const noteContentInput = document.getElementById('noteContent');
const noteTagsInput = document.getElementById('noteTags');
const saveNoteBtn = document.getElementById('saveNoteBtn');
const cancelModalBtn = document.getElementById('cancelModalBtn');
const friendsBtn = document.getElementById('friendsBtn');
const recycleBtn = document.getElementById('recycleBtn');
const historyBtn = document.getElementById('historyBtn');   // 新增

// 当前分页参数
let currentPage = 1;
let pageSize = 10;
let totalPages = 0;

// 排序变量
let sortField = null;   // 'noteId' 或 'updateTime'
let sortOrder = 'asc';  // 'asc' 或 'desc'

// ========== 加载用户信息 ==========
async function loadUserInfo() {
    try {
        const user = await api.getProfile();
        userInfoSpan.textContent = `${user.nickname || user.username} (${user.username})`;
        // 显示头像
        const avatarImg = document.getElementById('avatarImg');
        if (user.avatar && user.avatar.trim() !== '') {
            avatarImg.src = getFullUrl(user.avatar);
            avatarImg.style.display = 'block';
        } else {
            avatarImg.style.display = 'none';
        }
    } catch (err) {
        console.error(err);
        userInfoSpan.textContent = '加载失败';
    }
}

// ========== 加载笔记列表 ==========
async function loadNotes(page = 1) {
    currentPage = page;
    const params = {
        pageNum: currentPage,
        pageSize: pageSize,
        title: searchTitle.value.trim(),
        tag: searchTag.value.trim()
    };
    try {
        const data = await api.getNotes(params);
        renderNotes(data.records);
        totalPages = data.pages;
        renderPagination();
        updateSortIcons();
    } catch (err) {
        alert('加载笔记失败：' + err.message);
    }
}

// ========== 渲染笔记表格 ==========
function renderNotes(notes) {
    let sortedNotes = [...notes];
    if (sortField) {
        sortedNotes.sort((a, b) => {
            let valA = a[sortField];
            let valB = b[sortField];
            if (sortField === 'updateTime') {
                valA = new Date(valA);
                valB = new Date(valB);
            }
            if (valA < valB) return sortOrder === 'asc' ? -1 : 1;
            if (valA > valB) return sortOrder === 'asc' ? 1 : -1;
            return 0;
        });
    }

    if (!sortedNotes.length) {
        noteTableBody.innerHTML = '<td><td colspan="8">暂无笔记</td></tr>';
        return;
    }

    let html = '';
    for (const note of sortedNotes) {
        let keyPointsText = '-';
        if (note.aiKeyPoints) {
            try {
                const points = JSON.parse(note.aiKeyPoints);
                keyPointsText = points.join(', ');
            } catch(e) {}
        }

        html += `<tr>
            <td>${note.noteId}</td>
            <td><a href="note-detail.html?id=${note.noteId}" class="note-title">${escapeHtml(note.title)}</a></td>
            <td>${escapeHtml(note.tags || '')}</td>
            <td>${escapeHtml(note.aiSummary || '-')}</td>
            <td>${escapeHtml(keyPointsText)}</td>
            <td>${new Date(note.updateTime).toLocaleString()}</td>
            <td>
                <select class="permission-select" data-id="${note.noteId}" data-permission="${note.permission}">
                    <option value="仅自己可见" ${note.permission === '仅自己可见' ? 'selected' : ''}>仅自己可见</option>
                    <option value="仅好友可见" ${note.permission === '仅好友可见' ? 'selected' : ''}>仅好友可见</option>
                    <option value="所有人可见" ${note.permission === '所有人可见' ? 'selected' : ''}>所有人可见</option>
                </select>
             </td>
            <td>
                <button class="btn btn-warning" data-id="${note.noteId}" data-action="edit">编辑</button>
                <button class="btn btn-danger" data-id="${note.noteId}" data-action="delete">删除</button>
             </td>
        </tr>`;
    }
    noteTableBody.innerHTML = html;

    document.querySelectorAll('[data-action="edit"]').forEach(btn => {
        btn.addEventListener('click', () => editNote(parseInt(btn.dataset.id)));
    });
    document.querySelectorAll('[data-action="delete"]').forEach(btn => {
        btn.addEventListener('click', () => deleteNote(parseInt(btn.dataset.id)));
    });
    document.querySelectorAll('.permission-select').forEach(select => {
        select.addEventListener('change', async () => {
            const noteId = parseInt(select.dataset.id);
            const newPermission = select.value;
            try {
                await api.updateNotePermission(noteId, newPermission);
                alert('权限修改成功');
                select.setAttribute('data-permission', newPermission);
            } catch (err) {
                alert('修改失败：' + err.message);
                select.value = select.getAttribute('data-permission');
            }
        });
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

function renderPagination() {
    if (totalPages <= 1) {
        paginationDiv.innerHTML = '';
        return;
    }
    let html = `<button class="btn" data-page="1">首页</button>
                <button class="btn" data-page="${currentPage-1}" ${currentPage===1 ? 'disabled' : ''}>上一页</button>
                <span>第 ${currentPage} / ${totalPages} 页</span>
                <button class="btn" data-page="${currentPage+1}" ${currentPage===totalPages ? 'disabled' : ''}>下一页</button>
                <button class="btn" data-page="${totalPages}">末页</button>`;
    paginationDiv.innerHTML = html;
    document.querySelectorAll('[data-page]').forEach(btn => {
        btn.addEventListener('click', () => {
            const page = parseInt(btn.dataset.page);
            if (!isNaN(page) && page >=1 && page <= totalPages) {
                loadNotes(page);
            }
        });
    });
}

function openModal(note = null) {
    if (note) {
        modalTitle.textContent = '编辑笔记';
        noteIdInput.value = note.noteId;
        noteTitleInput.value = note.title;
        noteContentInput.value = note.content;
        noteTagsInput.value = note.tags || '';
    } else {
        modalTitle.textContent = '新增笔记';
        noteIdInput.value = '';
        noteTitleInput.value = '';
        noteContentInput.value = '';
        noteTagsInput.value = '';
    }
    modal.style.display = 'flex';
}

function closeModal() {
    modal.style.display = 'none';
}

async function saveNote() {
    const id = noteIdInput.value;
    const title = noteTitleInput.value.trim();
    const content = noteContentInput.value.trim();
    const tags = noteTagsInput.value.trim();
    if (!title) {
        alert('标题不能为空');
        return;
    }
    if (!content) {
        alert('内容不能为空');
        return;
    }
    const data = { title, content, tags };
    try {
        if (id) {
            data.noteId = parseInt(id);
            await api.updateNote(data);
            alert('修改成功');
        } else {
            await api.createNote(data);
            alert('新增成功');
        }
        closeModal();
        loadNotes(currentPage);
    } catch (err) {
        alert('操作失败：' + err.message);
    }
}

async function editNote(noteId) {
    try {
        const note = await api.getNote(noteId);
        openModal(note);
    } catch (err) {
        alert('获取笔记详情失败：' + err.message);
    }
}

async function deleteNote(noteId) {
    if (!confirm('确定删除该笔记吗？')) return;
    try {
        await api.deleteNote(noteId);
        alert('删除成功');
        loadNotes(currentPage);
    } catch (err) {
        alert('删除失败：' + err.message);
    }
}

function logout() {
    removeToken();
    window.location.href = 'login.html';
}

function updateSortIcons() {
    const idIcon = document.querySelector('#sortId i');
    const timeIcon = document.querySelector('#sortUpdateTime i');
    if (idIcon) idIcon.className = 'fas fa-sort';
    if (timeIcon) timeIcon.className = 'fas fa-sort';
    if (sortField === 'noteId') {
        if (sortOrder === 'asc') idIcon.className = 'fas fa-sort-up';
        else if (sortOrder === 'desc') idIcon.className = 'fas fa-sort-down';
    } else if (sortField === 'updateTime') {
        if (sortOrder === 'asc') timeIcon.className = 'fas fa-sort-up';
        else if (sortOrder === 'desc') timeIcon.className = 'fas fa-sort-down';
    }
}

function bindSortEvents() {
    const sortIdHeader = document.getElementById('sortId');
    const sortTimeHeader = document.getElementById('sortUpdateTime');
    if (sortIdHeader) {
        sortIdHeader.addEventListener('click', () => {
            if (sortField === 'noteId') {
                sortOrder = sortOrder === 'asc' ? 'desc' : 'asc';
            } else {
                sortField = 'noteId';
                sortOrder = 'asc';
            }
            loadNotes(currentPage);
        });
    }
    if (sortTimeHeader) {
        sortTimeHeader.addEventListener('click', () => {
            if (sortField === 'updateTime') {
                sortOrder = sortOrder === 'desc' ? 'asc' : 'desc';
            } else {
                sortField = 'updateTime';
                sortOrder = 'asc';
            }
            loadNotes(currentPage);
        });
    }
}

logoutBtn.addEventListener('click', logout);
if (profileBtn) {
    profileBtn.addEventListener('click', () => window.location.href = 'profile.html');
}
if (friendsBtn) {
    friendsBtn.addEventListener('click', () => window.location.href = 'friends.html');
}
if (historyBtn) {
    historyBtn.addEventListener('click', () => window.location.href = 'history.html');
}
if (recycleBtn) {
    recycleBtn.addEventListener('click', () => window.location.href = 'recycle.html');
}
searchBtn.addEventListener('click', () => loadNotes(1));
addNoteBtn.addEventListener('click', () => openModal());
saveNoteBtn.addEventListener('click', saveNote);
cancelModalBtn.addEventListener('click', closeModal);

searchTitle.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') loadNotes(1);
});
searchTag.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') loadNotes(1);
});

let searchTimer;
searchTitle.addEventListener('input', () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadNotes(1), 500);
});
searchTag.addEventListener('input', () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadNotes(1), 500);
});

bindSortEvents();

const urlParams = new URLSearchParams(window.location.search);
const editNoteId = urlParams.get('edit');
if (editNoteId) {
    setTimeout(async () => {
        try {
            const note = await api.getNote(editNoteId);
            openModal(note);
        } catch (err) {
            alert('加载笔记失败：' + err.message);
        }
    }, 500);
}

loadUserInfo();
loadNotes(1);