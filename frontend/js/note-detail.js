import api from './api.js';
import { isLoggedIn, getFullUrl } from './utils.js';

// 检查登录状态
if (!isLoggedIn()) {
    window.location.href = 'login.html';
}

// 获取 URL 中的笔记 ID
const urlParams = new URLSearchParams(window.location.search);
const noteId = urlParams.get('id');
if (!noteId) {
    alert('无效的笔记ID');
    window.location.href = 'index.html';
}

let currentNote = null; // 存储当前笔记数据

// 加载笔记详情
async function loadNoteDetail() {
    try {
        const note = await api.getNote(noteId);
        currentNote = note;
        renderNoteDetail(note);
    } catch (err) {
        console.error(err);
        const container = document.getElementById('noteDetail');
        if (container) container.innerHTML = `<div class="error">加载失败：${err.message}</div>`;
    }
}

// 渲染笔记详情（包含只读视图和编辑视图）
function renderNoteDetail(note) {
    const container = document.getElementById('noteDetail');
    if (!container) return;

    // 解析 AI 要点
    let keyPointsHtml = '';
    if (note.aiKeyPoints) {
        try {
            const points = JSON.parse(note.aiKeyPoints);
            if (points.length) {
                keyPointsHtml = `<ul>${points.map(p => `<li>${escapeHtml(p)}</li>`).join('')}</ul>`;
            } else {
                keyPointsHtml = '<p>暂无</p>';
            }
        } catch(e) {
            keyPointsHtml = '<p>格式错误</p>';
        }
    } else {
        keyPointsHtml = '<p>暂无</p>';
    }

    // 构建 HTML
    const html = `
        <div id="viewMode">
            <div class="note-title">${escapeHtml(note.title)}</div>
            <div class="note-meta">
                标签：${escapeHtml(note.tags || '无')} &nbsp;|&nbsp;
                更新时间：${new Date(note.updateTime).toLocaleString()} &nbsp;|&nbsp;
                权限：${escapeHtml(note.permission)}
            </div>
            <div class="note-content markdown-body">${marked.parse(note.content)}</div>
            <div class="ai-section">
                <h3>🤖 AI 智能分析</h3>
                <div class="ai-summary">
                    <strong>摘要：</strong> ${escapeHtml(note.aiSummary || '暂无')}
                </div>
                <div class="ai-keypoints">
                    <strong>要点：</strong> ${keyPointsHtml}
                </div>
                <div class="action-buttons">
                    <button id="analyzeBtn" class="btn">🤖 AI智能分析（生成/刷新）</button>
                    <button id="editNoteBtn" class="btn btn-warning">✏️ 编辑</button>
                </div>
            </div>
            <div class="share-section">
                <h4>分享设置</h4>
                <div style="margin-bottom: 10px;">
                    <label>笔记权限：</label>
                    <select id="permissionSelect">
                        <option value="仅自己可见" ${note.permission === '仅自己可见' ? 'selected' : ''}>仅自己可见</option>
                        <option value="仅好友可见" ${note.permission === '仅好友可见' ? 'selected' : ''}>仅好友可见</option>
                        <option value="所有人可见" ${note.permission === '所有人可见' ? 'selected' : ''}>所有人可见</option>
                    </select>
                    <button id="updatePermissionBtn" class="btn">保存权限</button>
                </div>
                <div id="publicLinkArea" style="margin-top: 10px; ${note.permission !== '所有人可见' ? 'display: none;' : ''}">
                    <label>公开链接：</label>
                    <input type="text" id="publicLink" readonly style="width: 70%;" value="${note.permission === '所有人可见' ? `${window.location.origin}/public-note.html?id=${note.noteId}` : ''}">
                    <button id="copyLinkBtn" class="btn">复制链接</button>
                </div>
            </div>
        </div>
        <div id="editMode" style="display: none;">
            <label>标题：</label>
            <input type="text" id="editTitle" class="form-control" value="${escapeHtml(note.title)}">
            <label>标签：</label>
            <input type="text" id="editTags" class="form-control" value="${escapeHtml(note.tags || '')}">
            <label>内容（支持Markdown）：</label>
            <textarea id="editContent" class="form-control" rows="15">${escapeHtml(note.content)}</textarea>
            <div class="action-buttons">
                <button id="saveEditBtn" class="btn btn-primary">保存修改</button>
                <button id="cancelEditBtn" class="btn btn-secondary">取消</button>
            </div>
        </div>
    `;
    container.innerHTML = html;

    // 获取元素引用
    const viewMode = document.getElementById('viewMode');
    const editMode = document.getElementById('editMode');
    const editTitle = document.getElementById('editTitle');
    const editTags = document.getElementById('editTags');
    const editContent = document.getElementById('editContent');
    const editNoteBtn = document.getElementById('editNoteBtn');
    const saveEditBtn = document.getElementById('saveEditBtn');
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    const analyzeBtn = document.getElementById('analyzeBtn');
    const permissionSelect = document.getElementById('permissionSelect');
    const updatePermissionBtn = document.getElementById('updatePermissionBtn');
    const publicLinkArea = document.getElementById('publicLinkArea');
    const copyLinkBtn = document.getElementById('copyLinkBtn');

    // 编辑模式切换
    if (editNoteBtn) {
        editNoteBtn.addEventListener('click', () => {
            viewMode.style.display = 'none';
            editMode.style.display = 'block';
        });
    }
    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', () => {
            // 恢复原有内容
            editTitle.value = currentNote.title;
            editTags.value = currentNote.tags || '';
            editContent.value = currentNote.content;
            viewMode.style.display = 'block';
            editMode.style.display = 'none';
        });
    }
    if (saveEditBtn) {
        saveEditBtn.addEventListener('click', saveNoteEdit);
    }

    // AI 分析
    if (analyzeBtn) {
        analyzeBtn.addEventListener('click', () => analyzeNote(true));
    }

    // 修改权限
    if (updatePermissionBtn) {
        updatePermissionBtn.addEventListener('click', async () => {
            const newPermission = permissionSelect.value;
            try {
                await api.updateNotePermission(noteId, newPermission);
                alert('权限修改成功');
                // 重新加载笔记详情
                await loadNoteDetail();
            } catch (err) {
                alert('修改失败：' + err.message);
            }
        });
    }

    // 公开链接区域显示/隐藏（根据权限下拉变化）
    if (permissionSelect) {
        permissionSelect.addEventListener('change', () => {
            if (permissionSelect.value === '所有人可见') {
                publicLinkArea.style.display = 'block';
                const linkInput = document.getElementById('publicLink');
                if (linkInput) linkInput.value = `${window.location.origin}/public-note.html?id=${noteId}`;
            } else {
                publicLinkArea.style.display = 'none';
            }
        });
    }

    // 复制链接
    if (copyLinkBtn) {
        copyLinkBtn.addEventListener('click', () => {
            const linkInput = document.getElementById('publicLink');
            if (linkInput) {
                linkInput.select();
                document.execCommand('copy');
                alert('链接已复制到剪贴板');
            }
        });
    }
}

// 保存编辑
async function saveNoteEdit() {
    const newTitle = document.getElementById('editTitle').value.trim();
    const newContent = document.getElementById('editContent').value.trim();
    const newTags = document.getElementById('editTags').value.trim();
    if (!newTitle || !newContent) {
        alert('标题和内容不能为空');
        return;
    }
    try {
        await api.updateNote({
            noteId: currentNote.noteId,
            title: newTitle,
            content: newContent,
            tags: newTags
        });
        alert('保存成功');
        await loadNoteDetail(); // 重新加载
    } catch (err) {
        alert('保存失败：' + err.message);
    }
}

// 调用 AI 分析（force=true）
async function analyzeNote(force = true) {
    const analyzeBtn = document.getElementById('analyzeBtn');
    if (!analyzeBtn) return;
    const originalText = analyzeBtn.innerText;
    analyzeBtn.innerText = '分析中...';
    analyzeBtn.disabled = true;
    try {
        const result = await api.analyzeNote(noteId, force);
        await loadNoteDetail();
        alert('AI 分析完成，已更新摘要和要点');
    } catch (err) {
        alert('分析失败：' + err.message);
    } finally {
        analyzeBtn.innerText = originalText;
        analyzeBtn.disabled = false;
    }
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

// 启动
loadNoteDetail();