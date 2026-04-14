import api from './api.js';
import { getToken, isLoggedIn } from './utils.js';

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

// DOM 元素
const noteDetailDiv = document.getElementById('noteDetail');

// 加载笔记详情
async function loadNoteDetail() {
    try {
        const note = await api.getNote(noteId);
        renderNoteDetail(note);
    } catch (err) {
        console.error(err);
        noteDetailDiv.innerHTML = `<div class="error">加载失败：${err.message}</div>`;
    }
}

// 渲染笔记详情
function renderNoteDetail(note) {
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

    const html = `
        <div class="note-title">${escapeHtml(note.title)}</div>
        <div class="note-meta">
            <span>标签：${escapeHtml(note.tags || '无')}</span>&nbsp;&nbsp;
            <span>更新时间：${new Date(note.updateTime).toLocaleString()}</span>
        </div>
        <div class="note-content">${escapeHtml(note.content).replace(/\n/g, '<br>')}</div>
        <div class="ai-section">
            <h3>🤖 AI 智能分析</h3>
            <div class="ai-summary">
                <strong>摘要：</strong> ${escapeHtml(note.aiSummary || '暂无')}
            </div>
            <div class="ai-keypoints">
                <strong>要点：</strong> ${keyPointsHtml}
            </div>
            <button id="analyzeBtn" class="btn" data-force="false">✨ AI 分析（重新）</button>
            <button id="forceAnalyzeBtn" class="btn btn-warning" data-force="true">🔄 强制重新分析</button>
        </div>
    `;
    noteDetailDiv.innerHTML = html;

    // 绑定 AI 分析按钮事件
    document.getElementById('analyzeBtn').addEventListener('click', () => analyzeNote(false));
    document.getElementById('forceAnalyzeBtn').addEventListener('click', () => analyzeNote(true));
}

// 调用 AI 分析接口
async function analyzeNote(force) {
    const btn = force ? document.getElementById('forceAnalyzeBtn') : document.getElementById('analyzeBtn');
    const originalText = btn.innerText;
    btn.innerText = '分析中...';
    btn.disabled = true;
    try {
        const result = await api.analyzeNote(noteId, force);
        // 分析成功后重新加载笔记详情以显示最新 AI 结果
        await loadNoteDetail();
        alert('AI 分析完成，已更新摘要和要点');
    } catch (err) {
        alert('分析失败：' + err.message);
    } finally {
        btn.innerText = originalText;
        btn.disabled = false;
    }
}

// 简单防XSS
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
loadNoteDetail();