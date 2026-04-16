import api from './api.js';
import { isLoggedIn } from './utils.js';

if (!isLoggedIn()) {
    window.location.href = 'login.html';
}

const recycleListDiv = document.getElementById('recycleList');

async function loadRecycleBin() {
    try {
        const notes = await api.getRecycleBin();
        renderRecycleBin(notes);
    } catch (err) {
        recycleListDiv.innerHTML = `<div class="empty">加载失败：${err.message}</div>`;
    }
}

function renderRecycleBin(notes) {
    if (!notes.length) {
        recycleListDiv.innerHTML = '<div class="empty">回收站空空如也</div>';
        return;
    }
    let html = `
        <table>
            <thead>
                <tr>
                    <th>笔记序号</th>
                    <th>标题</th>
                    <th>标签</th>
                    <th>删除时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
    `;
    for (const note of notes) {
        // 处理删除时间为空的情况
        const deleteTimeStr = note.deleteTime ? new Date(note.deleteTime).toLocaleString() : '未知';
        html += `
            <tr>
                <td>${note.noteId}</td>
                <td>${escapeHtml(note.title)}</td>
                <td>${escapeHtml(note.tags || '')}</td>
                <td>${deleteTimeStr}</td>
                <td>
                    <button class="btn-sm btn" data-id="${note.noteId}" data-action="restore">还原</button>
                    <button class="btn-sm btn-danger" data-id="${note.noteId}" data-action="permanent">彻底删除</button>
                </td>
            </tr>
        `;
    }
    html += '</tbody></table>';
    recycleListDiv.innerHTML = html;

    // 绑定还原按钮事件
    document.querySelectorAll('[data-action="restore"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const noteId = parseInt(btn.dataset.id);
            if (confirm('确定还原该笔记吗？')) {
                try {
                    await api.restoreNote(noteId);
                    alert('还原成功');
                    loadRecycleBin(); // 刷新列表
                } catch (err) {
                    alert('还原失败：' + err.message);
                }
            }
        });
    });

    // 绑定彻底删除按钮事件
    document.querySelectorAll('[data-action="permanent"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const noteId = parseInt(btn.dataset.id);
            if (confirm('彻底删除后无法恢复，确定吗？')) {
                try {
                    await api.permanentDeleteNote(noteId);
                    alert('彻底删除成功');
                    loadRecycleBin();
                } catch (err) {
                    alert('彻底删除失败：' + err.message);
                }
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

loadRecycleBin();