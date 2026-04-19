const API_BASE = `/api/host/events/${window.EVENT_ID}`;
let defaultBankId = null;
let currentQuestions = [];
let activeQuestionId = null;

// ─── DOM Elements ────────────────────────────────────────────────────────────
const questionList    = document.getElementById('questionList');
const addQuestionBtn  = document.getElementById('addQuestionBtn');
const emptyState      = document.getElementById('emptyEditorState');
const activeEditor    = document.getElementById('activeEditor');

const qTypeSelect     = document.getElementById('questionTypeSelect');
const qTimerSelect    = document.getElementById('questionTimerSelect');
const qPromptInput    = document.getElementById('questionPrompt');
const optionsContainer = document.getElementById('optionsContainer');
const addOptionBtn    = document.getElementById('addOptionBtn');
const deleteQBtn      = document.getElementById('deleteQuestionBtn');

// Library
const btnImportLibrary = document.getElementById('btnImportLibrary');
const libraryModal     = document.getElementById('libraryModal');
const curatedBanksList = document.getElementById('curatedBanksList');

// Media zone elements
const mediaZone        = document.getElementById('mediaZone');
const mediaEmpty       = document.getElementById('mediaEmpty');
const mediaPreview     = document.getElementById('mediaPreview');
const mediaPreviewImg  = document.getElementById('mediaPreviewImg');
const mediaUploading   = document.getElementById('mediaUploading');
const mediaFileInput   = document.getElementById('mediaFileInput');
const mediaBrowseLink  = document.getElementById('mediaBrowseLink');
const removeMediaBtn   = document.getElementById('removeMediaBtn');

// Start Event / Lobby overlay
const btnStartEvent    = document.getElementById('btnStartEvent');
const lobbyOverlay     = document.getElementById('lobbyOverlay');
const closeLobbyBtn    = document.getElementById('closeLobbyBtn');
const lobbyQrImg       = document.getElementById('lobbyQrImg');
const lobbyPinCode     = document.getElementById('lobbyPinCode');
const lobbyDisplayLink = document.getElementById('lobbyDisplayLink');

// Week 5: Link to the new Host Live Controls page (/host/events/{id}/live).
// This <a> element is shown inside the lobby overlay alongside the
// Venue Display link, so after going live the host can navigate to
// either the projected display or their game-pacing control panel.
const hostLiveLink     = document.getElementById('hostLiveLink');

// Start event confirmation
const confirmStartModal = document.getElementById('confirmStartModal');
const btnConfirmStart   = document.getElementById('btnConfirmStart');

// ─── Init ─────────────────────────────────────────────────────────────────────
async function init() {
    let res = await fetch(`${API_BASE}/banks`);
    if (res.ok) {
        let data = await res.json();
        if (data.banks.length > 0) {
            defaultBankId = data.banks[0].id;
            loadQuestions();
        }
    }
}

// ─── Question Loading & Rendering ────────────────────────────────────────────
async function loadQuestions() {
    let res = await fetch(`${API_BASE}/banks/${defaultBankId}/questions`);
    if (res.ok) {
        let data = await res.json();
        currentQuestions = data.questions;
        renderSidebar();

        if (currentQuestions.length > 0) {
            let toSelect = currentQuestions.find(q => q.id === activeQuestionId);
            if (!toSelect) toSelect = currentQuestions[0];
            selectQuestion(toSelect.id);
        } else {
            activeQuestionId = null;
            emptyState.classList.remove('hidden');
            activeEditor.classList.add('hidden');
        }
    }
}

function renderSidebar() {
    questionList.innerHTML = '';
    currentQuestions.forEach((q, index) => {
        let div = document.createElement('div');
        div.className = `q-thumbnail ${q.id === activeQuestionId ? 'active' : ''}`;

        if (q.id === window.__newQuestionId) {
            div.classList.add('new-question-anim');
            setTimeout(() => { if (window.__newQuestionId === q.id) window.__newQuestionId = null; }, 500);
        }

        div.innerText = `${index + 1}. ${getTypeLabel(q.type)}`;
        div.onclick = () => selectQuestion(q.id);
        questionList.appendChild(div);
    });
}

function getTypeLabel(type) {
    const map = { MULTIPLE_CHOICE: 'Quiz', POLL: 'Poll', WORD_CLOUD: 'Word Cloud', SLIDE: 'Slide' };
    return map[type] || type;
}

function selectQuestion(qId) {
    activeQuestionId = qId;
    renderSidebar();

    let q = currentQuestions.find(q => q.id === qId);
    if (!q) return;

    emptyState.classList.add('hidden');
    activeEditor.classList.remove('hidden');

    qTypeSelect.value = q.type;
    if (qTimerSelect) {
        qTimerSelect.value = (q.timeLimitSeconds !== undefined && q.timeLimitSeconds !== null) ? q.timeLimitSeconds : 30;
    }
    qPromptInput.value = q.prompt;

    // Restore the media zone to the correct state for this question
    updateMediaZone(q.mediaUrl, q.transformedUrl);

    renderOptions(q);
}

// ─── Media Zone ───────────────────────────────────────────────────────────────

/**
 * Updates the media zone display to reflect the current question's media state.
 * Three possible states: empty, preview (has media), uploading.
 */
function updateMediaZone(mediaUrl, transformedUrl) {
    if (mediaUrl && mediaUrl.length > 0) {
        // Use the transformed (cropped) URL for the preview if available, else original
        mediaPreviewImg.src = transformedUrl || mediaUrl;
        showMediaState('preview');
    } else {
        showMediaState('empty');
    }
}

function showMediaState(state) {
    mediaEmpty.classList.add('hidden');
    mediaPreview.classList.add('hidden');
    mediaUploading.classList.add('hidden');

    if (state === 'empty')     mediaEmpty.classList.remove('hidden');
    if (state === 'preview')   mediaPreview.classList.remove('hidden');
    if (state === 'uploading') mediaUploading.classList.remove('hidden');
}

// Drag-and-drop handlers
mediaZone.addEventListener('dragenter', (e) => {
    e.preventDefault();
    mediaZone.classList.add('drag-over');
});
mediaZone.addEventListener('dragover', (e) => {
    e.preventDefault(); // required to allow drop
    mediaZone.classList.add('drag-over');
});
mediaZone.addEventListener('dragleave', () => {
    mediaZone.classList.remove('drag-over');
});
mediaZone.addEventListener('drop', (e) => {
    e.preventDefault();
    mediaZone.classList.remove('drag-over');
    const file = e.dataTransfer.files[0];
    if (file) handleFileUpload(file);
});

// Click-to-browse via the zone itself (but not when in preview mode, to avoid conflicts)
mediaZone.addEventListener('click', (e) => {
    // Only open file picker from the empty state
    if (!mediaPreview.classList.contains('hidden')) return;
    mediaFileInput.click();
});

// "click to browse" link inside the empty state
if (mediaBrowseLink) {
    mediaBrowseLink.addEventListener('click', (e) => {
        e.stopPropagation(); // don't bubble to mediaZone
        mediaFileInput.click();
    });
}

// File input change (click-to-browse chosen a file)
mediaFileInput.addEventListener('change', () => {
    const file = mediaFileInput.files[0];
    if (file) handleFileUpload(file);
    mediaFileInput.value = ''; // reset so same file can be re-uploaded if needed
});

/**
 * Uploads the given file to Cloudinary via the backend.
 * Shows the uploading spinner, then either displays the preview or shows an error.
 */
async function handleFileUpload(file) {
    if (!activeQuestionId) return;

    // Only allow image/video
    if (!file.type.startsWith('image/') && !file.type.startsWith('video/')) {
        alert('Please upload an image or video file.');
        return;
    }

    showMediaState('uploading');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const res = await fetch(`${API_BASE}/questions/${activeQuestionId}/media`, {
            method: 'POST',
            body: formData
            // Note: do NOT set Content-Type header — the browser sets it automatically
            //       with the correct multipart boundary for FormData
        });

        if (res.ok) {
            const data = await res.json();
            // Update the in-memory question so the preview persists when switching questions
            const q = currentQuestions.find(q => q.id === activeQuestionId);
            if (q) {
                q.mediaUrl = data.mediaUrl;
                q.transformedUrl = data.transformedUrl;
            }
            updateMediaZone(data.mediaUrl, data.transformedUrl);
        } else {
            showMediaState('empty');
            alert('Upload failed. Please try again.');
        }
    } catch (err) {
        showMediaState('empty');
        alert('Upload failed: ' + err.message);
    }
}

// Remove media button click
removeMediaBtn.addEventListener('click', async (e) => {
    e.stopPropagation(); // don't bubble to mediaZone
    if (!activeQuestionId) return;

    const res = await fetch(`${API_BASE}/questions/${activeQuestionId}/media`, {
        method: 'DELETE'
    });

    if (res.ok) {
        const q = currentQuestions.find(q => q.id === activeQuestionId);
        if (q) { q.mediaUrl = ''; q.transformedUrl = ''; }
        showMediaState('empty');
        mediaPreviewImg.src = '';
    }
});

// ─── Options ─────────────────────────────────────────────────────────────────
function renderOptions(q) {
    optionsContainer.innerHTML = '';

    if (q.type === 'WORD_CLOUD' || q.type === 'SLIDE') {
        addOptionBtn.style.display = 'none';
        return;
    }

    addOptionBtn.style.display = 'block';

    q.options.forEach(opt => {
        let div = document.createElement('div');
        div.className = 'option-box';
        div.style.setProperty('--opt-color', opt.color);

        let correctBtn = '';
        if (q.type !== 'POLL') {
            correctBtn = `<button class="correct-toggle ${opt.isCorrect ? 'active' : ''}" data-id="${opt.id}"></button>`;
        }

        div.innerHTML = `
            ${correctBtn}
            <input type="text" class="opt-input" data-id="${opt.id}" value="${opt.text}">
            <button class="del-opt-btn" data-id="${opt.id}">×</button>
        `;
        optionsContainer.appendChild(div);
    });

    optionsContainer.querySelectorAll('.opt-input').forEach(inp => {
        inp.addEventListener('blur', (e) => updateOption(e.target.dataset.id));
    });
    optionsContainer.querySelectorAll('.correct-toggle').forEach(btn => {
        btn.addEventListener('click', (e) => {
            let id = e.target.dataset.id;
            let opt = currentQuestions.find(q => q.id === activeQuestionId)?.options.find(o => o.id == id);
            if (!opt) return;
            opt.isCorrect = !opt.isCorrect;
            e.target.classList.toggle('active', opt.isCorrect);
            updateOption(id);
        });
    });
    optionsContainer.querySelectorAll('.del-opt-btn').forEach(btn => {
        btn.addEventListener('click', (e) => deleteOption(e.target.dataset.id));
    });
}

// ─── Question API Calls ───────────────────────────────────────────────────────
async function addNewQuestion() {
    let res = await fetch(`${API_BASE}/banks/${defaultBankId}/questions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'MULTIPLE_CHOICE' })
    });
    if (res.ok) {
        let data = await res.json();
        activeQuestionId = data.id;
        window.__newQuestionId = data.id;
        await loadQuestions();
    }
}

async function updateActiveQuestion() {
    if (!activeQuestionId) return;
    let timeLimitSeconds = qTimerSelect ? qTimerSelect.value : 30;
    let res = await fetch(`${API_BASE}/questions/${activeQuestionId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: qTypeSelect.value, prompt: qPromptInput.value, timeLimitSeconds: timeLimitSeconds })
    });
    if (res.ok) {
        let q = currentQuestions.find(q => q.id === activeQuestionId);
        if (q && q.type !== qTypeSelect.value) {
            await loadQuestions();
        } else if (q) {
            q.type = qTypeSelect.value;
            q.prompt = qPromptInput.value;
            q.timeLimitSeconds = timeLimitSeconds;
            renderSidebar();
        }
    }
}

async function deleteActiveQuestion() {
    if (!activeQuestionId) return;
    let res = await fetch(`${API_BASE}/questions/${activeQuestionId}`, { method: 'DELETE' });
    if (res.ok) {
        activeQuestionId = null;
        await loadQuestions();
    }
}

async function addNewOption() {
    if (!activeQuestionId) return;
    let res = await fetch(`${API_BASE}/questions/${activeQuestionId}/options`, { method: 'POST' });
    if (res.ok) await loadQuestions();
}

async function updateOption(optId) {
    let optEl = document.querySelector(`.opt-input[data-id="${optId}"]`);
    if (!optEl) return;
    let q = currentQuestions.find(q => q.id === activeQuestionId);
    let opt = q?.options.find(o => o.id == optId);
    await fetch(`${API_BASE}/options/${optId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: optEl.value, isCorrect: opt?.isCorrect ?? false })
    });
}

async function deleteOption(optId) {
    let res = await fetch(`${API_BASE}/options/${optId}`, { method: 'DELETE' });
    if (res.ok) await loadQuestions();
}

// ─── Question Library ─────────────────────────────────────────────────────────
async function loadCuratedBanks() {
    let res = await fetch(`${API_BASE}/curated-banks`);
    if (res.ok) {
        let data = await res.json();
        curatedBanksList.innerHTML = '';
        data.banks.forEach(b => {
            let div = document.createElement('div');
            div.className = 'bank-item';
            div.innerHTML = `
                <div class="bank-info">
                    <h3>${b.title}</h3>
                    <p>${b.description}</p>
                </div>
                <button class="btn-primary btn-sm" onclick="importBank(${b.id})">Import</button>
            `;
            curatedBanksList.appendChild(div);
        });
        libraryModal.classList.add('active');
    }
}

async function importBank(sourceBankId) {
    let res = await fetch(`${API_BASE}/banks/${defaultBankId}/import/${sourceBankId}`, { method: 'POST' });
    if (res.ok) {
        libraryModal.classList.remove('active');
        await loadQuestions();
    }
}

// ─── Go Live / Lobby Launch ───────────────────────────────────────────────────
async function goLive() {
    btnConfirmStart.disabled = true;
    btnConfirmStart.textContent = 'Going live...';

    try {
        const res = await fetch(`${API_BASE}/go-live`, { method: 'POST' });
        if (res.ok) {
            const data = await res.json();
            
            // Close the confirmation modal
            confirmStartModal.classList.remove('active');
            
            // Automatically open the display page in a new tab
            window.open(data.displayUrl, '_blank');
            
            // Week 5: The backend now returns hostLiveUrl alongside the
            // existing joinCode/qrCodeUrl/displayUrl, so we pass it through
            // to showLobbyOverlay() to populate the "Live Controls" link.
            showLobbyOverlay(data.joinCode, data.qrCodeUrl, data.displayUrl, data.hostLiveUrl);
        } else {
            const err = await res.json();
            alert(err.error || 'Could not go live. Please try again.');
            btnConfirmStart.disabled = false;
            btnConfirmStart.textContent = 'Yes, Start';
        }
    } catch (e) {
        alert('Network error. Please try again.');
        btnConfirmStart.disabled = false;
        btnConfirmStart.textContent = 'Yes, Start';
    }
}

// Week 5: Updated to accept hostLiveUrl as a 4th parameter.
// The lobby overlay now shows TWO action links:
//   1. "Venue Display" → opens the projected display in a new tab
//   2. "Live Controls" → navigates to /host/events/{id}/live
function showLobbyOverlay(joinCode, qrCodeUrl, displayUrl, hostLiveUrl) {
    lobbyPinCode.textContent = joinCode;
    lobbyQrImg.src = qrCodeUrl;
    lobbyDisplayLink.href = displayUrl;
    hostLiveLink.href = hostLiveUrl;   // Week 5: populate the live controls link
    lobbyOverlay.classList.remove('hidden');
}

// ─── Event Listeners ──────────────────────────────────────────────────────────
addQuestionBtn.addEventListener('click', addNewQuestion);
deleteQBtn.addEventListener('click', deleteActiveQuestion);
addOptionBtn.addEventListener('click', addNewOption);
btnImportLibrary.addEventListener('click', loadCuratedBanks);
btnStartEvent.addEventListener('click', () => confirmStartModal.classList.add('active'));
btnConfirmStart.addEventListener('click', goLive);
closeLobbyBtn.addEventListener('click', () => lobbyOverlay.classList.add('hidden'));

qTypeSelect.addEventListener('change', updateActiveQuestion);
if (qTimerSelect) {
    qTimerSelect.addEventListener('change', updateActiveQuestion);
}
qPromptInput.addEventListener('blur', updateActiveQuestion);

// Close library modal when clicking the backdrop
libraryModal.addEventListener('click', (e) => {
    if (e.target === libraryModal) libraryModal.classList.remove('active');
});

// ─── Startup ──────────────────────────────────────────────────────────────────
init();
