const API_BASE = `/api/host/events/${window.EVENT_ID}`;
let defaultBankId = null;
let currentQuestions = [];
let activeQuestionId = null;

// DOM Elements
const questionList = document.getElementById('questionList');
const addQuestionBtn = document.getElementById('addQuestionBtn');
const emptyState = document.getElementById('emptyEditorState');
const activeEditor = document.getElementById('activeEditor');

// Active Editor Elements
const qTypeSelect = document.getElementById('questionTypeSelect');
const qPromptInput = document.getElementById('questionPrompt');
const optionsContainer = document.getElementById('optionsContainer');
const addOptionBtn = document.getElementById('addOptionBtn');
const deleteQBtn = document.getElementById('deleteQuestionBtn');

// Library
const btnImportLibrary = document.getElementById('btnImportLibrary');
const libraryModal = document.getElementById('libraryModal');
const curatedBanksList = document.getElementById('curatedBanksList');

// Init
async function init() {
    // 1. Fetch Banks for this event (find the default one)
    let res = await fetch(`${API_BASE}/banks`);
    if(res.ok) {
        let data = await res.json();
        if(data.banks.length > 0) {
            defaultBankId = data.banks[0].id;
            loadQuestions();
        }
    }
}

// Load
async function loadQuestions() {
    let res = await fetch(`${API_BASE}/banks/${defaultBankId}/questions`);
    if(res.ok) {
        let data = await res.json();
        currentQuestions = data.questions;
        renderSidebar();
        
        if (currentQuestions.length > 0) {
            // Find active or just pick first
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
    if (type === 'MULTIPLE_CHOICE') return 'Quiz';
    if (type === 'POLL') return 'Poll';
    if (type === 'WORD_CLOUD') return 'Word Cloud';
    if (type === 'SLIDE') return 'Slide';
    return type;
}

function selectQuestion(qId) {
    activeQuestionId = qId;
    renderSidebar(); // Update active class
    
    let q = currentQuestions.find(q => q.id === qId);
    if (!q) return;

    emptyState.classList.add('hidden');
    activeEditor.classList.remove('hidden');

    qTypeSelect.value = q.type;
    qPromptInput.value = q.prompt;

    renderOptions(q);
}

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

    // Option Event Listeners
    optionsContainer.querySelectorAll('.opt-input').forEach(inp => {
        inp.addEventListener('blur', (e) => updateOption(e.target.dataset.id));
    });
    optionsContainer.querySelectorAll('.correct-toggle').forEach(btn => {
        btn.addEventListener('click', (e) => {
            let id = e.target.dataset.id;
            // Toggle
            let opt = q.options.find(o => o.id == id);
            opt.isCorrect = !opt.isCorrect;
            e.target.classList.toggle('active', opt.isCorrect);
            updateOption(id);
        });
    });
    optionsContainer.querySelectorAll('.del-opt-btn').forEach(btn => {
        btn.addEventListener('click', (e) => deleteOption(e.target.dataset.id));
    });
}

// API Interactions
async function addNewQuestion() {
    let res = await fetch(`${API_BASE}/banks/${defaultBankId}/questions`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({type: 'MULTIPLE_CHOICE'})
    });
    if(res.ok) {
        let data = await res.json();
        activeQuestionId = data.id;
        window.__newQuestionId = data.id;
        await loadQuestions();
    }
}

async function updateActiveQuestion() {
    if(!activeQuestionId) return;
    let type = qTypeSelect.value;
    let prompt = qPromptInput.value;
    let timeLimit = 20; // Safe default
    let points = 100;   // Safe default

    let res = await fetch(`${API_BASE}/questions/${activeQuestionId}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ type, prompt, timeLimit, points })
    });
    if(res.ok) {
        // Find if type changed to re-render options safely
        let q = currentQuestions.find(q => q.id === activeQuestionId);
        if (q.type !== type) {
            await loadQuestions();
        } else {
            q.type = type; q.prompt = prompt;
            renderSidebar();
        }
    }
}

async function deleteActiveQuestion() {
    if(!activeQuestionId) return;
    let res = await fetch(`${API_BASE}/questions/${activeQuestionId}`, { method: 'DELETE' });
    if(res.ok) {
        activeQuestionId = null;
        await loadQuestions();
    }
}

async function addNewOption() {
    if(!activeQuestionId) return;
    let res = await fetch(`${API_BASE}/questions/${activeQuestionId}/options`, { method: 'POST' });
    if(res.ok) {
        await loadQuestions();
    }
}

async function updateOption(optId) {
    let optEl = document.querySelector(`.opt-input[data-id="${optId}"]`);
    if(!optEl) return;
    let text = optEl.value;
    let q = currentQuestions.find(q => q.id === activeQuestionId);
    let opt = q.options.find(o => o.id == optId);
    
    let res = await fetch(`${API_BASE}/options/${optId}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ text, isCorrect: opt.isCorrect })
    });
}

async function deleteOption(optId) {
    let res = await fetch(`${API_BASE}/options/${optId}`, { method: 'DELETE' });
    if(res.ok) {
        await loadQuestions();
    }
}

async function loadCuratedBanks() {
    let res = await fetch(`${API_BASE}/curated-banks`);
    if(res.ok) {
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
    if(res.ok) {
        libraryModal.classList.remove('active');
        await loadQuestions();
    }
}

// Event Listeners
addQuestionBtn.addEventListener('click', addNewQuestion);
deleteQBtn.addEventListener('click', deleteActiveQuestion);
addOptionBtn.addEventListener('click', addNewOption);
btnImportLibrary.addEventListener('click', loadCuratedBanks);

qTypeSelect.addEventListener('change', updateActiveQuestion);
qPromptInput.addEventListener('blur', updateActiveQuestion);

// Startup
init();
