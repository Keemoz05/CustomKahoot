# QuizYa — Fallback & Resilience Issues Analysis

> **Scope:** Analyzing `live.html`, `host-live.js`, `play.html`, `HostController.java`, `HostApiController.java`, `GuestController.java`, `GuestService.java`, `EventService.java`.
> **Status:** Analysis only — no code changes yet.

---

## 1. Host Accidentally Closes the `/live` Tab

### Ways the Host Can Re-enter `/live`

The URL for the live panel is deterministic: `/host/events/{id}/live`.  
The host can recover it through any of these paths:

| # | Method | How |
|---|--------|-----|
| **A** | **Browser history** | Press `Ctrl+Shift+T` (reopen closed tab) or check browser history and navigate back to the exact URL. |
| **B** | **Dashboard redirect** | Go to `/host/dashboard` → find the event card → click **"Live Controls"** (if a link to `/live` is shown there). *Currently the dashboard does not show this link for LOBBY/LIVE events — this needs to be added.* |
| **C** | **Setup page redirect** | Navigate to `/host/events/{id}/setup`. The "Go Live" overlay already shows a **"Live Controls"** link pointing to `/host/events/{id}/live`. The host can reuse this if they remember the event ID. |
| **D** | **Bookmark** | If the host bookmarked the URL before closing. |
| **E** | **Presenter window** | If the presenter popup window is still open, it could have a "Return to Live Controls" button linking back to the parent `/live` URL. |

**Root Problem:** The dashboard currently has no visual indicator that an event is in `LOBBY`/`LIVE` state, and no quick-access link to re-enter the live panel. This is the primary thing to fix.

---

## 2. "Recreate Presenter Window" Button (Requested Feature)

### Context
`host-live.js` has no presenter popup window code currently. The conversation history mentions a "presenter window" — this likely refers to a `window.open()` popup that shows a separate presenter/projector view.

### Proposed Solution
Add a **"Reopen Presenter"** button in the live panel header (or controls zone). Clicking it calls `window.open('/display/{joinCode}', 'quizya-presenter', 'width=1280,height=720')`. The reference to the opened window is stored in a variable (e.g., `presenterWindow`). The button should:
- If `presenterWindow` is `null` or `presenterWindow.closed === true` → open a new one.
- Otherwise → call `presenterWindow.focus()` to bring the existing one to front.

This button should live in the **live panel header** alongside the existing "Show Previews" toggle for easy access.

---

## 3. Other Fallback Issues Found in the Project

---

### 3.1 🔴 Host Re-entry Blocked by `goLive()` Status Guard

**File:** `EventService.java` → `goLive()` (line 120)

**Problem:**  
`goLive()` throws `IllegalStateException` if the event is not in `DRAFT` status. If the host accidentally navigates away and tries to "Go Live" again from the setup page, they get a `400 Bad Request` error. There is no graceful re-entry path from the setup page once the event is already `LOBBY`.

**Proposed Fix:**  
In `HostApiController.goLive()`, catch the `IllegalStateException` and instead of returning an error, return the already-live event's data (joinCode, qrCodeUrl, hostLiveUrl). This way the setup overlay re-renders correctly and the host can click "Live Controls" again without confusion.

---

### 3.2 🔴 Guest Session Token Lost on Page Refresh

**File:** `GuestController.java` → `showPlayPage()` (line 38), `play.html`

**Problem:**  
The guest's session token is passed as a URL query parameter: `/play?token=abc123`. If the guest refreshes or closes and reopens the browser, they still have the correct URL in history. **However**, if they accidentally navigate away (e.g., hit the back button and land on a different page), they lose the token and **cannot rejoin** — the event may already be in `LIVE` state which does allow joining (`GuestService.joinEvent()` line 46 accepts `LOBBY` or `LIVE`), but the guest would appear as a **new** guest with a fresh score.

**Proposed Fix:**  
Store the session token in `sessionStorage` when `play.html` loads. On load, if `?token=` is missing from the URL, attempt to recover from `sessionStorage`. This is purely a frontend fix in `play.html`.

---

### 3.3 🟠 WebSocket Disconnection — No Reconnect Logic

**File:** `play.html` → `connect()` (line 328), `host-live.js` → `connectWebSocket()` (line 232)

**Problem:**  
Both the guest (`play.html`) and host (`host-live.js`) connect to STOMP/SockJS once on page load. There is **no reconnection logic** if the WebSocket drops (e.g., network hiccup, server restart, ngrok tunnel timeout). The guest silently stops receiving questions; the host silently stops seeing guest joins.

**Proposed Fix:**  
Add a `stompClient.onWebSocketClose` (or `stompClient.on('error', ...)`) callback that attempts a reconnect after a delay (e.g., exponential backoff starting at 2 seconds). Show a small "Reconnecting..." status indicator on both pages during the gap. In `host-live.js`, an existing `#statusBadge` element could be used for this.

---

### 3.4 🟠 `liveIndex` State Lost if Host Refreshes the Live Page

**File:** `host-live.js` (lines 67–68)

**Problem:**  
`liveIndex` (the currently live question index) and `selectedIndex` are JavaScript variables initialized to `0`. If the host refreshes `/live`, these reset to `0` even though the event's `currentQuestionIndex` on the server may be at question 5. The UI would appear desynced — showing "Welcome" as the live slide when the event is actually mid-game.

**Proposed Fix:**  
On `init()`, fetch the current event state from the server (e.g., add a `GET /api/host/events/{id}/state` endpoint that returns `{currentQuestionIndex, status}`). Use the returned `currentQuestionIndex` to restore `liveIndex` and `selectedIndex` on page load.

---

### 3.5 🟠 `next-to` with `questionIndex: 0` Goes to Welcome but Broadcasts a Question

**File:** `HostApiController.java` → `jumpToQuestion()` (line 411)

**Problem:**  
The `/next-to` endpoint rejects `targetIndex <= 0` and returns a `400`. However, `host-live.js`'s "Previous Slide" button (line 332) guards against `liveIndex > 1`, meaning it never sends `questionIndex: 0`. But if the `liveIndex` is somehow `1` and the host clicks "Previous" rapidly twice, a race condition could send `questionIndex: 0`. 

More importantly: the **"Welcome" slide (index 0)** has no corresponding server-side broadcast. If the host wants to go back to the lobby/welcome screen (e.g., a guest joined late), there is no mechanism to broadcast a "return to welcome" state to guests — they remain stuck on whatever state they last received.

**Proposed Fix:**  
Add a `targetIndex === 0` special case in `/next-to` that broadcasts a `SHOW_LOBBY` (or `SHOW_WELCOME`) WebSocket message to guests, returning them to the waiting screen. Guard the Previous button to also handle index 0 cleanly.

---

### 3.6 🟡 Dashboard Shows No "Re-Enter Live" Link for Active Events

**File:** `HostController.java` → `showDashboard()` (line 22), `host/dashboard.html` (not reviewed)

**Problem:**  
The dashboard lists all events but likely shows only "Setup" and "Delete" actions, with no awareness of whether an event is currently `LOBBY` or `LIVE`. A host who accidentally closes both the setup AND live tabs has no obvious path back.

**Proposed Fix:**  
In the dashboard template, conditionally show a **"Re-enter Live Controls →"** button for any event with `status == 'LOBBY' || status == 'LIVE'`, linking to `/host/events/{id}/live`. This is the most critical UX fix to enable host recovery.

---

### 3.7 🟡 `finishEvent` Clears All Guests — Irreversible

**File:** `HostApiController.java` → `finishEvent()` (line 539), `GuestService.java` → `clearGuestsForEvent()` (line 161)

**Problem:**  
Clicking "End Event" immediately clears all guest data (answers, scores) and resets the event to `DRAFT`. There is only a `confirm()` dialog. If the host accidentally confirms, all participant data is permanently deleted with no way to recover results or scores.

**Proposed Fix:**  
1. **Short term:** Make the confirmation dialog more explicit — require the host to type "END" or click a second confirmation.
2. **Long term:** Separate the "archive/export" step from the "clear data" step. Offer a results summary page before clearing guests. This is a larger feature, but the first step is just a stronger confirmation UX.

---

### 3.8 🟡 Preview iframes Use Live WebSocket — Not True "Preview"

**File:** `live.html` (lines 215, 223), `audience-preview.html`

**Problem:**  
The projector preview iframe at `/display/{joinCode}` connects to the real WebSocket. If the host triggers an action on the live panel, the preview iframe reacts just like the real projector. This is mostly fine, but the **audience preview** iframe joins as a `[Preview]` guest (via `joinEventAsPreview()`) which creates a DB record. If the preview iframe crashes or fails to load, a zombie `[Preview]` record persists in the database.

**Proposed Fix:**  
Add a cleanup mechanism. Either:  
- Delete preview guests on server restart (mark them as ephemeral).  
- Or add a `/api/host/events/{id}/cleanup-previews` endpoint that the live panel calls on `beforeunload`.

---

### 3.9 🟡 No "Reconnect" Button for Guests After Kick or Network Drop

**File:** `play.html` → `state-finished` (line 302)

**Problem:**  
When a guest is kicked, they see the "Removed" state and are stranded. When a guest's WebSocket disconnects and they miss game events (e.g., `SHOW_QUESTION`), they're stuck on the last state they received with no way to resync. There is no "Rejoin" or "Reconnect" button.

**Proposed Fix:**  
For kicked guests: show a "Return to Join Page" button that links to `/join?pin={joinCode}` (the join code could be stored in a JS variable on `play.html`). For network drops: the reconnect logic from **Issue 3.3** would resync them. Add a visible "Connection Lost — Reconnecting..." banner.

---

### 3.10 🟡 `currentQuestionIndex` Not Persisted Between `next-to` Calls

**File:** `HostApiController.java` → `jumpToQuestion()` (line 405), existing TODO comment (line 354)

**Problem:**  
There is a **TODO comment** in the codebase itself acknowledging this: `event.setCurrentQuestionIndex(targetIndex)` is called but the entity is never saved (`eventRepository.save(event)` is missing in `jumpToQuestion`). This means the `currentQuestionIndex` the server holds in memory may diverge from what's in the database if the server restarts.

**Proposed Fix:**  
Add `eventRepository.save(event)` (or a `eventService.saveCurrentIndex(eventId, targetIndex)` method) after setting `currentQuestionIndex` in both `nextQuestion()` and `jumpToQuestion()`. This is a straightforward one-line fix but has resilience implications.

---

## Summary Table

| # | Severity | Issue | Where |
|---|----------|-------|--------|
| 1 | 🔴 Critical | `goLive()` blocks host re-entry from setup page | `EventService.java` |
| 2 | 🔴 Critical | Guest session token lost on navigation away | `play.html` |
| 3 | 🟠 High | No WebSocket reconnect logic (host + guest) | `host-live.js`, `play.html` |
| 4 | 🟠 High | `liveIndex` resets to 0 on host page refresh | `host-live.js` |
| 5 | 🟠 High | No "return to welcome" broadcast for Welcome slide | `HostApiController.java` |
| 6 | 🟡 Medium | Dashboard has no "Re-enter Live" link for active events | `dashboard.html` |
| 7 | 🟡 Medium | End Event has weak confirmation (data is unrecoverable) | `HostApiController.java` |
| 8 | 🟡 Medium | Zombie `[Preview]` guest records on iframe crash | `GuestService.java` |
| 9 | 🟡 Medium | No rejoin/reconnect UI for guests | `play.html` |
| 10 | 🟡 Medium | `currentQuestionIndex` not saved to DB after `next-to` | `HostApiController.java` |

> **Requested Features (not bugs):**
> - Re-entry button/link on dashboard for active events → see Issues 3.6 + Section 1
> - "Recreate Presenter Window" button on live panel → see Section 2
