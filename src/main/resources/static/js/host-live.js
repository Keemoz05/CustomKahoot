/**
 * host-live.js — Host Live Controls
 * Single-file, fully self-contained controller for the host live panel.
 *
 * Responsibilities:
 *   - WebSocket connection & real-time message handling
 *   - Navigation sidebar rendering & safe-tap protocol
 *   - All control button event bindings (Next, Skip, Lock, Reveal, Leaderboard, Feedback, Pause)
 *   - Sidebar toggle (preview panel)
 *   - Audience overview (load, search, kick, live updates)
 *   - Soundboard audio playback
 *   - End Event (finish) flow
 */

const API_BASE = `/api/host/events/${window.EVENT_ID}`;

// ─── Ngrok Warning Bypass ─────────────────────────────────────────────────────
// Passes the header to ngrok to skip the browser warning 
const _origFetch = window.fetch;
window.fetch = async function (...args) {
    let [resource, config] = args;
    config = config || {};
    config.headers = config.headers || {};
    config.headers['ngrok-skip-browser-warning'] = 'true';
    return _origFetch(resource, config);
};

// ─── DOM References ───────────────────────────────────────────────────────────
const btnNext        = document.getElementById('btnNext');
const btnPrev        = document.getElementById('btnPrev');
const btnLock        = document.getElementById('btnLock');
const btnFeedback    = document.getElementById('btnFeedback');
const btnReveal      = document.getElementById('btnReveal');
const btnEndEvent    = document.getElementById('btnEndEvent');   // sidebar only
const btnToggleSidebar = document.getElementById('btnToggleSidebar');
const toggleSidebarText = document.getElementById('toggleSidebarText');
const togglePause    = document.getElementById('togglePause');

const statusBadge    = document.getElementById('statusBadge');
const questionPreview = document.getElementById('questionPreview');
const previewOptions = document.getElementById('previewOptions');
const feedbackChart  = document.getElementById('feedbackChart');
const btnPushLive    = document.getElementById('btnPushLive');
const outOfSyncBadge = document.getElementById('outOfSyncBadge');

const navList        = document.getElementById('navList');
const progressFill   = document.getElementById('progressFill');
const progressLabel  = document.getElementById('progressLabel');
const selectedSlideNum = document.getElementById('selectedSlideNum');
const totalSlides    = document.getElementById('totalSlides');

// Hidden counters kept for internal counting (not displayed directly)
const guestCount     = document.getElementById('guestCount');
const answerCount    = document.getElementById('answerCount');
const navGuestCount  = document.getElementById('navGuestCount');

const guestList      = document.getElementById('guestList');
const guestSearch    = document.getElementById('guestSearch');
const guestCountBadge = document.getElementById('guestCountBadge');

// ─── State ────────────────────────────────────────────────────────────────────
let stompClient     = null;
let isPaused        = false;
let allQuestions    = [];
let allGuests       = [];
let selectedIndex   = 0;  // Slide the host is PREVIEWING (0 = Welcome)
let liveIndex       = 0;  // Slide currently LIVE on screens

// ─── Startup ──────────────────────────────────────────────────────────────────
async function init() {
    connectWebSocket();
    await Promise.all([loadQuestions(), loadGuests()]);
    updateUI();
}

// ─── Questions & Nav Sidebar ──────────────────────────────────────────────────
async function loadQuestions() {
    try {
        const bankRes = await fetch(`${API_BASE}/banks`);
        if (!bankRes.ok) return;
        const bankData = await bankRes.json();
        if (!bankData.banks || bankData.banks.length === 0) return;

        const bankId = bankData.banks[0].id;
        const qRes = await fetch(`${API_BASE}/banks/${bankId}/questions`);
        if (!qRes.ok) return;
        const qData = await qRes.json();

        allQuestions = qData.questions || [];
        const total = allQuestions.length + 1; // +1 for Welcome slide
        if (totalSlides) totalSlides.innerText = total;

        renderNavList();
    } catch (e) {
        console.error('Failed to load questions:', e);
    }
}

function renderNavList() {
    navList.innerHTML = '';

    // Welcome slide (index 0)
    const welcomeLi = document.createElement('li');
    welcomeLi.className = 'nav-item flat-item';
    welcomeLi.dataset.index = '0';
    welcomeLi.innerHTML = `<span class="nav-dot"></span> Welcome`;
    welcomeLi.addEventListener('click', () => selectSlide(0));
    navList.appendChild(welcomeLi);

    if (allQuestions.length === 0) {
        const emptyNote = document.createElement('li');
        emptyNote.className = 'nav-item';
        emptyNote.style.cssText = 'color:rgba(255,255,255,0.25);font-size:12px;padding:8px 16px;cursor:default;';
        emptyNote.innerText = 'No questions loaded';
        navList.appendChild(emptyNote);
        return;
    }

    // Round header
    const roundHeader = document.createElement('li');
    roundHeader.className = 'nav-item round-header';
    roundHeader.style.cursor = 'pointer';
    roundHeader.innerHTML = `<span class="nav-chevron" style="display:inline-block;transition:transform 0.2s ease;">▾</span> Main Round`;
    navList.appendChild(roundHeader);

    // Question items
    const roundUl = document.createElement('ul');
    roundUl.className = 'round-questions expanded';

    allQuestions.forEach((q, idx) => {
        const slideIndex = idx + 1;
        const li = document.createElement('li');
        li.className = 'nav-item question-item';
        li.dataset.index = String(slideIndex);

        // Strip HTML from prompt text for safe sidebar display
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = q.prompt || '';
        const plainText = (tempDiv.textContent || tempDiv.innerText || '').trim();
        const shortText = plainText.length > 32
            ? plainText.substring(0, 32) + '…'
            : (plainText || 'Untitled question');

        li.innerHTML = `<span class="nav-dot"></span><span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">Q${slideIndex}: ${shortText}</span>`;
        li.addEventListener('click', () => selectSlide(slideIndex));
        roundUl.appendChild(li);
    });

    roundHeader.addEventListener('click', () => {
        roundUl.classList.toggle('expanded');
        const chevron = roundHeader.querySelector('.nav-chevron');
        if (chevron) {
            chevron.style.transform = roundUl.classList.contains('expanded') ? 'rotate(0deg)' : 'rotate(-90deg)';
        }
    });

    navList.appendChild(roundUl);
    updateNavStatus();
}

function updateNavStatus() {
    const total = allQuestions.length + 1;
    const pct = total > 1 ? Math.min(100, (liveIndex / (total - 1)) * 100) : 0;
    if (progressFill)  progressFill.style.width = `${pct}%`;
    if (progressLabel) progressLabel.innerText   = `${liveIndex} / ${total - 1}`;

    document.querySelectorAll('.nav-item[data-index]').forEach(item => {
        const idx = parseInt(item.dataset.index, 10);
        const dot = item.querySelector('.nav-dot');

        item.classList.toggle('active', idx === selectedIndex);

        if (dot) {
            dot.className = 'nav-dot';
            if (idx < liveIndex)        dot.classList.add('done');
            else if (idx === liveIndex) dot.classList.add('active');
            else                        dot.classList.add('upcoming');
        }
    });

    // Auto-scroll active item into view
    const activeItem = navList.querySelector(`.nav-item[data-index="${selectedIndex}"]`);
    if (activeItem) activeItem.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// ─── Safe-Tap Protocol ────────────────────────────────────────────────────────
// Clicking a sidebar item selects it for preview. The slide is only PUSHED live
// when the host explicitly clicks "Push to Live" (or uses "Next Slide").
function selectSlide(index) {
    selectedIndex = index;
    updateUI();
}

function updateUI() {
    if (selectedSlideNum) selectedSlideNum.innerText = selectedIndex;
    updateNavStatus();

    const isSynced = selectedIndex === liveIndex;
    if (btnPushLive)   btnPushLive.style.display  = isSynced ? 'none' : 'block';
    if (outOfSyncBadge) outOfSyncBadge.style.display = isSynced ? 'none' : 'inline-block';

    // Render preview content
    if (previewOptions) previewOptions.innerHTML = '';
    if (selectedIndex === 0) {
        if (questionPreview) questionPreview.innerHTML = 'Welcome / Lobby Screen';
        return;
    }

    const q = allQuestions[selectedIndex - 1];
    if (!q || !questionPreview) return;

    questionPreview.innerHTML = q.prompt || 'Question';

    (q.options || []).forEach(opt => {
        const div = document.createElement('div');
        div.style.cssText = `
            padding: 10px 14px;
            background: ${opt.color || '#ccc'};
            color: white;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
        `;
        div.innerText = opt.text || '';
        if (previewOptions) previewOptions.appendChild(div);
    });
}

// ─── WebSocket ────────────────────────────────────────────────────────────────
function connectWebSocket() {
    //from WebSocketConfig.java
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        // Subscribe to the shared lobby topic — receives GUEST_JOINED,
        // ANSWER_SUBMITTED, VOTE_DISTRIBUTION and all game-state events.
        stompClient.subscribe(`/topic/event/${window.EVENT_ID}/lobby`, msg => {
            const payload = JSON.parse(msg.body);
            handleLobbyMessage(payload);
        });
    });
}

function handleLobbyMessage(payload) {
    switch (payload.type) {
        case 'GUEST_JOINED': {
            const count = (parseInt(guestCount?.innerText || '0', 10)) + 1;
            if (guestCount)    guestCount.innerText    = count;
            if (navGuestCount) navGuestCount.innerText = count;

            // Live-add to guest list without a full reload
            allGuests.push({
                id: payload.guestId || Date.now(),
                displayName: payload.guestName || 'New Guest',
                correctCount: 0
            });
            renderGuestList(guestSearch?.value || '');
            break;
        }
        case 'GUEST_KICKED': {
            allGuests = allGuests.filter(g => g.id !== payload.guestId);
            renderGuestList(guestSearch?.value || '');
            break;
        }
        case 'ANSWER_SUBMITTED': {
            const ac = parseInt(answerCount?.innerText || '0', 10) + 1;
            if (answerCount) answerCount.innerText = ac;
            break;
        }
        case 'VOTE_DISTRIBUTION':
            renderFeedbackChart(payload);
            break;
        default:
            // Game-state events (SHOW_QUESTION etc.) are handled by the
            // venue display — host panel doesn't need to react to them here.
            break;
    }
}

// ─── API Action Helper ────────────────────────────────────────────────────────
async function postAction(action, body = null) {
    const opts = { method: 'POST' };
    if (body) {
        opts.headers = { 'Content-Type': 'application/json' };
        opts.body = JSON.stringify(body);
    }
    try {
        const res = await fetch(`${API_BASE}/${action}`, opts);
        if (!res.ok) {
            console.warn(`Action "${action}" failed with status ${res.status}`);
            return null;
        }
        const data = await res.json();
        // Update status badge if the backend returns a state field
        if (data.state && statusBadge) {
            statusBadge.innerText = data.state;
        }
        return data;
    } catch (e) {
        console.error(`Failed to POST /${action}:`, e);
        return null;
    }
}

// ─── Control Button Bindings ──────────────────────────────────────────────────

// ── Next Slide ── uses /next-to for explicit index control
if (btnNext) {
    btnNext.addEventListener('click', async () => {
        // Prevent going past the last question
        if (liveIndex < allQuestions.length) {
            const data = await postAction('next-to', { questionIndex: liveIndex + 1 });
            if (data?.success) {
                liveIndex++;
                selectedIndex = liveIndex;
                updateUI();
                if (answerCount) answerCount.innerText = '0';
            }
        } else {
            console.warn("Already at the last slide.");
        }
    });
}

// ── Previous Slide ── calls /next-to to go back
if (btnPrev) {
    btnPrev.addEventListener('click', async () => {
        if (liveIndex > 1) {
            const data = await postAction('next-to', { questionIndex: liveIndex - 1 });
            if (data?.success) {
                liveIndex--;
                selectedIndex = liveIndex;
                updateUI();
            }
        }
    });
}

// ── Lock Answers ──
if (btnLock) {
    btnLock.addEventListener('click', () => postAction('lock'));
}

// ── Reveal Answer ──
if (btnReveal) {
    btnReveal.addEventListener('click', () => postAction('reveal'));
}

// ── Show/Hide Feedback Chart ──
// We toggle a local flag so we don't destructively overwrite the icon HTML.
let feedbackVisible = false;
if (btnFeedback) {
    btnFeedback.addEventListener('click', () => {
        feedbackVisible = !feedbackVisible;
        if (feedbackChart) {
            feedbackChart.style.display = feedbackVisible ? 'block' : 'none';
        }
        // Update only the text node — preserve the SVG icon inside .btn-icon
        const label = btnFeedback.lastChild;
        if (label && label.nodeType === Node.TEXT_NODE) {
            label.textContent = feedbackVisible ? 'Hide Chart' : 'Feedback';
        } else {
            // Fallback: append/update a text span
            let textEl = btnFeedback.querySelector('.btn-label');
            if (!textEl) {
                textEl = document.createElement('span');
                textEl.className = 'btn-label';
                btnFeedback.appendChild(textEl);
            }
            textEl.innerText = feedbackVisible ? 'Hide Chart' : 'Feedback';
        }
    });
}

// ── Push to Live (Safe-Tap) ── used when host has selected a different slide
if (btnPushLive) {
    btnPushLive.addEventListener('click', async () => {
        const data = await postAction('next-to', { questionIndex: selectedIndex });
        if (data?.success) {
            liveIndex = selectedIndex;
            updateUI();
        }
    });
}

// ── Storyteller Pause ──
if (togglePause) {
    togglePause.addEventListener('click', () => {
        isPaused = !isPaused;
        togglePause.classList.toggle('active', isPaused);
        postAction('pause', { paused: isPaused });
    });
}

// ── End Event (single button, lives in sidebar) ──
async function handleEndEvent() {
    const confirmed = confirm(
        'End this event?\n\nAll guest progress will be cleared and the event will return to DRAFT.'
    );
    if (!confirmed) return;

    const data = await postAction('finish');
    if (data?.success) {
        window.location.href = '/host/dashboard';
    }
}

if (btnEndEvent) {
    btnEndEvent.addEventListener('click', handleEndEvent);
}

// ── Show/Hide Preview Sidebar — with localStorage persistence ──
const SIDEBAR_KEY = 'quizya-sidebar-open';

function setSidebarOpen(isOpen) {
    document.body.classList.toggle('sidebar-open', isOpen);
    if (toggleSidebarText) {
        toggleSidebarText.innerText = isOpen ? 'Hide Previews' : 'Show Previews';
    }
    localStorage.setItem(SIDEBAR_KEY, isOpen ? '1' : '0');

    // After the CSS grid transition finishes, recompute iframe scales
    // (the sidebar column width changes, so the wrapper sizes change)
    setTimeout(scalePreviewIframes, 350);
}

// Restore sidebar state from localStorage on page load
const savedSidebar = localStorage.getItem(SIDEBAR_KEY);
if (savedSidebar === '1') {
    setSidebarOpen(true);
}

if (btnToggleSidebar) {
    btnToggleSidebar.addEventListener('click', () => {
        const isOpen = !document.body.classList.contains('sidebar-open');
        setSidebarOpen(isOpen);
    });
}

// ─── Dynamic iframe scaling ───────────────────────────────────────────────────
// Each preview iframe has a fixed pixel viewport (set in CSS):
//   Projector: 1440 × 810   (desktop 16:9)
//   Phone:      390 × 844   (iPhone 14-class 9:16)
//
// We compute: scale = container_width / iframe_native_width
// and apply it as a CSS transform.  This is recalculated whenever the
// sidebar opens/closes or the window resizes.

function scalePreviewIframes() {
    const projectorWrap   = document.querySelector('.projector-wrap');
    const projectorIframe = projectorWrap?.querySelector('iframe');
    if (projectorWrap && projectorIframe) {
        const scale = projectorWrap.clientWidth / 1440;
        projectorIframe.style.transform = `scale(${scale})`;
    }

    const phoneWrap   = document.querySelector('.phone-wrap');
    const phoneIframe = phoneWrap?.querySelector('iframe');
    if (phoneWrap && phoneIframe) {
        const scale = phoneWrap.clientWidth / 390;
        phoneIframe.style.transform = `scale(${scale})`;
    }
}

// Run on load (after a short delay so the layout has settled)
setTimeout(scalePreviewIframes, 500);

// Re-run on resize
window.addEventListener('resize', scalePreviewIframes);

// ─── Soundboard ───────────────────────────────────────────────────────────────
const SOUND_NAMES = ['drumroll', 'applause', 'buzzer', 'tick', 'tada', 'suspense'];
const audioCache = {};
let currentSound = null;

SOUND_NAMES.forEach(name => {
    const audio = new Audio(`/audio/${name}.mp3`);
    audio.preload = 'auto';
    audioCache[name] = audio;
});

document.querySelectorAll('.sound-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const name = btn.dataset.sound;
        if (!name || !audioCache[name]) return;

        if (currentSound) {
            currentSound.pause();
            currentSound.currentTime = 0;
        }
        currentSound = audioCache[name];
        currentSound.currentTime = 0;
        currentSound.play().catch(e => console.warn('Audio play failed:', e));
    });
});

// ─── Audience Overview ────────────────────────────────────────────────────────
async function loadGuests() {
    try {
        const res = await fetch(`${API_BASE}/guests`);
        if (!res.ok) return;
        const data = await res.json();
        allGuests = data.guests || [];
        renderGuestList();
    } catch (e) {
        console.error('Failed to load guests:', e);
    }
}

function renderGuestList(filterText = '') {
    if (!guestList) return;
    guestList.innerHTML = '';

    const lower = filterText.toLowerCase();
    const filtered = allGuests.filter(g =>
        (g.displayName || '').toLowerCase().includes(lower)
    );

    if (guestCountBadge) guestCountBadge.innerText = allGuests.length;
    if (navGuestCount)   navGuestCount.innerText   = allGuests.length;
    if (guestCount)      guestCount.innerText      = allGuests.length;

    if (filtered.length === 0) {
        const empty = document.createElement('li');
        empty.className = 'guest-item';
        empty.style.cssText = 'color:var(--color-stone);font-size:13px;justify-content:center;padding:12px 0;';
        empty.innerText = filterText ? 'No guests match your search.' : 'No guests yet.';
        guestList.appendChild(empty);
        return;
    }

    filtered.forEach(guest => {
        const li = document.createElement('li');
        li.className = 'guest-item';
        li.id = `guest-${guest.id}`;

        // Sanitise name to prevent XSS
        const safeDiv = document.createElement('div');
        safeDiv.innerText = guest.displayName;
        const safeName = safeDiv.innerHTML;

        li.innerHTML = `
            <span class="guest-name">${safeName}</span>
            <span class="guest-score">✓ ${guest.correctCount ?? 0}</span>
            <button class="btn-kick" data-guest-id="${guest.id}" data-tooltip="Remove ${safeName}">✕</button>
        `;

        li.querySelector('.btn-kick').addEventListener('click', async e => {
            e.stopPropagation();
            if (!confirm(`Remove ${guest.displayName} from this event?`)) return;
            try {
                const res = await fetch(`${API_BASE}/kick/${guest.id}`, { method: 'POST' });
                if (res.ok) {
                    allGuests = allGuests.filter(g => g.id !== guest.id);
                    renderGuestList(guestSearch?.value || '');
                }
            } catch (err) {
                console.error('Failed to kick guest:', err);
            }
        });

        guestList.appendChild(li);
    });
}

if (guestSearch) {
    guestSearch.addEventListener('input', e => renderGuestList(e.target.value));
}

// ─── Feedback Chart ───────────────────────────────────────────────────────────
function renderFeedbackChart(data) {
    const chartBars = document.getElementById('chartBars');
    if (!chartBars) return;
    chartBars.innerHTML = '';

    (data.options || []).forEach(opt => {
        const row = document.createElement('div');
        row.style.cssText = 'display:flex;align-items:center;gap:8px;';

        const label = document.createElement('div');
        label.style.cssText = 'width:80px;font-size:12px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;color:var(--color-slate);';
        label.innerText = opt.text || '';

        const track = document.createElement('div');
        track.style.cssText = 'flex:1;background:var(--color-pebble);height:10px;border-radius:5px;overflow:hidden;';

        const fill = document.createElement('div');
        fill.style.cssText = `width:${opt.percentage ?? 0}%;height:100%;background:${opt.color || 'var(--color-deep-teal)'};border-radius:5px;transition:width 0.4s ease;`;
        track.appendChild(fill);

        const count = document.createElement('div');
        count.style.cssText = 'width:28px;font-size:12px;text-align:right;color:var(--color-slate);font-weight:600;';
        count.innerText = opt.count ?? 0;

        row.appendChild(label);
        row.appendChild(track);
        row.appendChild(count);
        chartBars.appendChild(row);
    });
}

// ─── Kick off ─────────────────────────────────────────────────────────────────
init();
