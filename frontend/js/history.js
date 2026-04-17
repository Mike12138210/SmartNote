import api from './api.js';
import { isLoggedIn } from './utils.js';

if (!isLoggedIn()) {
    window.location.href = 'login.html';
}

const historyListDiv = document.getElementById('historyList');
const limitSelect = document.getElementById('limitSelect');

async function loadHistory(limit = 10) {
    try {
        const notes = await api.getRecentHistory(limit);
        renderHistory(notes);
    } catch (err) {
        historyListDiv.innerHTML = `<div class="empty">加载失败：${err.message}</div>`;
    }
}

function renderHistory(histories) {
    if (!histories.length) {
        historyListDiv.innerHTML = '<div class="empty">暂无浏览记录</div>';
        return;
    }
    let html = `
        <table>
            <thead>
                <tr>
                    <th>笔记序号</th>
                    <th>标题</th>
                    <th>标签</th>
                    <th>浏览时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
    `;
    for (const item of histories) {
        html += `
            <tr>
                <td>${item.noteId}</td>
                <td><a href="note-detail.html?id=${item.noteId}">${escapeHtml(item.title)}</a></td>
                <td>${escapeHtml(item.tags || '')}</td>
                <td>${new Date(item.viewTime).toLocaleString()}</td>
                <td>
                    <button class="btn-sm btn" data-id="${item.noteId}" data-action="view">查看</button>
                </td>
            </tr>
        `;
    }
    html += '</tbody></table>';
    historyListDiv.innerHTML = html;

    document.querySelectorAll('[data-action="view"]').forEach(btn => {
        btn.addEventListener('click', () => {
            const noteId = btn.dataset.id;
            window.location.href = `note-detail.html?id=${noteId}`;
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

// 下拉框变化时自动刷新
if (limitSelect) {
    limitSelect.addEventListener('change', () => {
        const limit = parseInt(limitSelect.value);
        loadHistory(limit);
    });
}

// 初始加载
loadHistory(10);