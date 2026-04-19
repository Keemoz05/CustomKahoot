/**
 * host-live.js — Week 5: Host Live Controls
 *
 * This script powers the host's real-time game-pacing control panel at
 * /host/events/{id}/live.  It does two things:
 *
 * 1. SENDING:  Each control button (Next, Lock, Reveal, Leaderboard, Pause)
 *    fires a POST to the corresponding /api/host/events/{id}/{action} endpoint.
 *    The backend handles the business logic (loading questions, computing
 *    leaderboards) and broadcasts WebSocket events to guests and the venue
 *    display.  The host-live page itself does NOT receive those broadcasts
 *    for game state — it only uses the HTTP response to update its own UI
 *    labels (status badge + question preview text).
 *
 * 2. LISTENING:  The script subscribes to /topic/event/{id}/lobby via STOMP
 *    to receive real-time notifications about guest activity:
 *      - GUEST_JOINED:     increments the "Connected Guests" counter
 *      - ANSWER_SUBMITTED: increments the "Answers Submitted" counter
 *    These give the host live visibility into audience engagement.
 */

const API_BASE = `/api/host/events/${window.EVENT_ID}`;

// ─── DOM Elements ────────────────────────────────────────────────────────────
const btnNext = document.getElementById('btnNext');
const btnLock = document.getElementById('btnLock');
const btnReveal = document.getElementById('btnReveal');
const btnLeaderboard = document.getElementById('btnLeaderboard');
const btnFinish = document.getElementById('btnFinish');
const togglePause = document.getElementById('togglePause');
const statusBadge = document.getElementById('statusBadge');
const questionPreview = document.getElementById('questionPreview');
const guestCount = document.getElementById('guestCount');
const answerCount = document.getElementById('answerCount');

let isPaused = false;
let stompClient = null;

// ─── WebSocket Connection ────────────────────────────────────────────────────
// We subscribe to the LOBBY channel (same one the venue display uses) so we
// can piggyback on the GUEST_JOINED and ANSWER_SUBMITTED events that are
// already being broadcast there.  This avoids creating a separate WebSocket
// topic just for the host controls.
function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Suppress STOMP debug logs in the console

    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/event/' + window.EVENT_ID + '/lobby', function (message) {
            const payload = JSON.parse(message.body);

            // When a new guest joins, bump the "Connected Guests" counter.
            if (payload.type === 'GUEST_JOINED') {
                guestCount.innerText = parseInt(guestCount.innerText) + 1;
            }
            // When any guest submits an answer, bump the "Answers Submitted"
            // counter.  This is broadcast by GuestApiController after
            // persisting the answer.
            else if (payload.type === 'ANSWER_SUBMITTED') {
                answerCount.innerText = parseInt(answerCount.innerText) + 1;
            }
        });
    });
}

// ─── API Action Dispatcher ───────────────────────────────────────────────────
// All four control buttons and the pause toggle funnel through this function.
// It POSTs to the backend and updates the status badge + preview text based
// on the "state" field in the response.
//
// The backend response always includes { success: true, state: "..." } where
// state is one of: QUESTION, LOCKED, REVEAL, LEADERBOARD.
async function postAction(action, payload = null) {
    const opts = { method: 'POST' };
    if (payload) {
        opts.headers = { 'Content-Type': 'application/json' };
        opts.body = JSON.stringify(payload);
    }
    try {
        const res = await fetch(`${API_BASE}/${action}`, opts);
        if (res.ok) {
            const data = await res.json();
            if (data.state) {
                // Update the pill badge in the header to reflect current phase
                statusBadge.innerText = data.state;

                // Update the large preview text in the control panel
                if (data.state === 'QUESTION') {
                    questionPreview.innerText = "Question Active";
                    answerCount.innerText = "0"; // reset for the new question
                } else if (data.state === 'LOCKED') {
                    questionPreview.innerText = "Answers Locked";
                } else if (data.state === 'REVEAL') {
                    questionPreview.innerText = "Answer Revealed";
                } else if (data.state === 'LEADERBOARD') {
                    questionPreview.innerText = "Leaderboard Shown";
                }
            }
        }
    } catch (e) {
        console.error('Failed to execute action', e);
    }
}

// ─── Button Event Listeners ──────────────────────────────────────────────────
// Each button maps directly to one of the five backend endpoints defined
// in HostApiController under the "Live Controls (Week 5)" section.

btnNext.addEventListener('click', () => postAction('next'));           // POST /next
btnLock.addEventListener('click', () => postAction('lock'));           // POST /lock
btnReveal.addEventListener('click', () => postAction('reveal'));       // POST /reveal
btnLeaderboard.addEventListener('click', () => postAction('leaderboard')); // POST /leaderboard

btnFinish.addEventListener('click', async () => {
    if (!confirm('Are you sure you want to end this event? All guest progress will be cleared.')) return;
    
    try {
        const res = await fetch(`${API_BASE}/finish`, { method: 'POST' });
        if (res.ok) {
            window.location.href = '/host/dashboard';
        }
    } catch (e) {
        console.error('Failed to finish event', e);
    }
});

// The pause toggle is a stateless client-side boolean.  Each click flips it
// and sends the new value to the backend, which broadcasts PAUSE or RESUME
// to all connected clients.
togglePause.addEventListener('click', () => {
    isPaused = !isPaused;
    togglePause.classList.toggle('active', isPaused);
    postAction('pause', { paused: isPaused });                        // POST /pause
});

// ─── Startup ─────────────────────────────────────────────────────────────────
connect();
