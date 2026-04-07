# Development Timeline

---

## Priority Strategy

The development order follows three core decisions:

1. **Working features before security** — Host login and authentication are infrastructure concerns, not feature concerns. Building auth first blocks progress and produces nothing playable. Auth is added in Week 7, after a complete working demo exists.
2. **Host Dashboard before Guest workflow** — The host drives the entire event. Event creation, question setup, and the lobby launch must exist before there is anything for a guest to join.
3. **Guest workflow is account-free** — Guests never need to register or log in. Their entire experience — joining, answering, and viewing scores — is frictionless from day one and remains so permanently.

> **Development Stance:** Weeks 1–6 use an open, unprotected host dashboard for speed of development. The dashboard is locked behind auth only in Week 7, once the full game loop is proven to work.

---

## Milestones

---

### Week 1: Foundation & Project Setup (April 2 – April 8)

**Goal:** Get the project compiling, the database connected, and the first visible UI on screen. No auth. No login. Just a working skeleton.

**Tasks:**
- Set up the Spring Boot project, database connection, and WebSocket infrastructure.
- Design and apply the core database schema (Events, Questions, Sessions, Answers — Users table deferred to Week 7).
- Disable Spring Security / leave all routes publicly accessible for now.
- Build the host dashboard shell — an open, unprotected page accessible directly by URL.
- Build the guest landing page (root URL) — display name input and game PIN field, UI only, no logic yet.

**Deadline (April 8):** The project compiles cleanly, the database is connected, and navigating to `/host/dashboard` and `/join` renders visible HTML pages without a login prompt.

---

### Week 2: Host Dashboard — Event Creation & Content Setup (April 9 – April 15)

**Goal:** Allow the host to create events, build question decks, and assign question formats — all accessible without logging in during this phase.

**Tasks (implementing Use Cases H1, H2, H3):**
- Build the "New Event" flow — event name, description, and draft creation.
- Develop the Question Editor interface within the dashboard.
- Implement the Question Type selector (Multiple Choice, Poll, Word Cloud, Slide).
- Build the Question Library with curated templates the host can import and customize.

**Deadline (April 15):** Navigating to the dashboard, a host can create a new event, import a question bank template, edit placeholder text, and assign a question format. All content is persisted to the database.

---

### Week 3: Host Dashboard — Media Uploads & Lobby Launch (April 16 – April 22)

**Goal:** Allow the host to attach media to questions, then go live and generate a QR code for guests.

**Tasks (implementing Use Cases H4, H5):**
- Integrate cloud storage or local file storage for drag-and-drop media uploading (photos/videos).
- Build the automatic crop-and-center logic to fit media into aesthetic templates.
- Implement the live preview of how a slide will look on the venue's main screen.
- Build the "Start Event" / "Go Live" button — triggers game PIN generation and QR code rendering.
- Build the Venue Main Display layout (`/display`) — the screen projected at the venue.

**Deadline (April 22):** A host can upload a photo to a question, preview it, then click "Go Live." The venue display at `/display` shows a large QR code and game PIN. The lobby is open and waiting.

---

### Week 4: Guest Workflow — Frictionless Join & Answer Submission (April 23 – April 29)

**Goal:** Build the entire guest-side experience end-to-end. Guests join with a name only — no account, no login, no friction.

**Tasks (implementing Use Cases G1, G2, G3, G4):**
- Wire the QR code to the guest join URL: `/join?pin=<PIN>`.
- Build the minimalist guest join page — display name input only, zero registration fields.
- Build the WebSocket handler that places the guest into the correct live session after joining.
- Build the guest answer UI — large, high-contrast, text-labeled blocks, no countdown timer.
- Implement answer locking and the "Waiting for others" calm feedback state.
- Build the private rank/score display on each guest's device after a leaderboard reveal.

**Deadline (April 29):** A guest can scan a QR code or type a game PIN, enter only their display name, and appear on the lobby screen — no account required. They can submit untimed answers and see their private score after each round.

---

### Week 5: Host Live Controls & Leaderboard (April 30 – May 6)

**Goal:** Give the host complete real-time control over the game pacing and implement the minimalist leaderboard.

**Tasks (implementing Use Cases H6, H7, S1):**
- Build the host's live control view (`/host/live`) — separate from the setup dashboard.
- Implement "Lock Answers," "Reveal Answer," and "Next" controls with real-time broadcast.
- Build the "Storyteller Pause / Hold" toggle that freezes all game state.
- Write the accuracy-based scoring logic (no speed bonus — equal marks for all correct answers regardless of time).
- Build the minimalist top 3–5 leaderboard for the venue's main screen.
- Wire the private per-guest score and rank display to the leaderboard trigger.

**Deadline (May 6):** The host can run a full question cycle — display question → lock answers → reveal answer → Storyteller Pause → leaderboard. Guests see their private rank. The venue screen shows only top performers. **At this point, the full game loop works end-to-end with zero authentication.**

---

### Week 6: Audio & Aesthetic QA (May 7 – May 13)

**Goal:** Finalize the sensory experience and validate visual templates against accessibility standards.

**Tasks (implementing Use Cases S2, A1):**
- Integrate the ambient audio player with seamless auto-looping.
- Wire the audio system to automatically dip volume by 50% when the Storyteller Pause is activated.
- Build the automated script to check WCAG contrast ratios and minimum font sizes across all aesthetic templates.
- Run QA on all visual templates and mark passing ones as "Approved."

**Deadline (May 13):** The ambient soundscape functions smoothly alongside game state changes. The working demo is feature-complete and fully playable by a real group of people.

---

### Week 7: Host Authentication & Security Layer (May 14 – May 20)

**Goal:** Add login and registration to the host dashboard now that every feature behind it is proven to work.

> **Why here?** Auth is layered in last so that it never blocks feature development. Every host-side feature was built and tested on an open dashboard. Now we lock it down.

**Tasks:**
- Design and apply the Users table to the database schema.
- Implement host registration and login with secure password hashing (BCrypt).
- Configure Spring Security to protect all `/host/**` routes behind a session check.
- Build the login page and registration page UI.
- Implement session management and "remember me" handling.
- Ensure all `/join`, `/display`, and guest routes remain publicly accessible (no auth required).

**Deadline (May 20):** Host routes are protected. An unauthenticated user visiting `/host/dashboard` is redirected to `/login`. Guest and display routes remain completely open. All previous features still work.

---

### Week 8: End-to-End Integration & Performance (May 21 – May 27)

**Goal:** Connect all modules — including the new auth layer — into a single cohesive journey and stress-test real-time performance.

**Tasks:**
- Conduct full integration testing across all three primary views: Host Dashboard, Venue Display, Guest Mobile.
- Optimize database queries and WebSocket synchronization for near-zero latency during live play.
- Verify that auth does not break any existing game-state flows.
- Fix all cross-module bugs discovered during integration.

**Deadline (May 27):** A complete, bug-free playthrough of a 10-question event — from host login → lobby creation → guest join → final leaderboard — with multiple simulated guests connected simultaneously.

---

### Week 9: User Acceptance Testing (UAT) & Refinement (May 28 – June 3)

**Goal:** Validate the end-to-end experience with real target users, particularly elderly guests joining as participants.

**Tasks:**
- Conduct a mock event with a small group, specifically including elderly users testing the QR code join flow and untimed answer buttons.
- Gather feedback on mobile UI contrast, button sizing, and audio volume.
- Implement all critical UI/UX fixes identified during the mock event.

**Deadline (June 3):** All critical UAT feedback is resolved. The guest join-to-answer flow is validated as frictionless for users 65+.

---

### Week 10: Production Deployment & Launch (June 4 – June 10)

**Goal:** Finalize infrastructure and ship Version 1.0.

**Tasks:**
- Set up production servers, domain routing, and SSL certificates.
- Perform final security checks on database access rules and session handling.
- Deploy the application and run a final smoke test on the live environment.

**Deadline (June 10):** Version 1.0 is live and ready for Event Hosts to create their first real-world events.

---

## Use Case → Week Mapping

| Use Case | Description | Target Week |
|----------|-------------|-------------|
| H1 | Create a New Event | Week 2 |
| H2 | Import Curated Question Bank | Week 2 |
| H3 | Select Question Format | Week 2 |
| H4 | Upload Nostalgic Media | Week 3 |
| H5 | Launch the Game Lobby | Week 3 |
| G1 | Join via QR Code (No Login) | Week 4 |
| G2 | Join via Game PIN (No Login) | Week 4 |
| G3 | Submit Untimed Answer | Week 4 |
| G4 | View Personal Score & Rank | Week 4 |
| H6 | Trigger "Storyteller Pause" | Week 5 |
| H7 | Advance Slides & Control Game Flow | Week 5 |
| S1 | Render Minimalist Leaderboard | Week 5 |
| S2 | Play Ambient Soundscape | Week 6 |
| A1 | QA Aesthetic Templates | Week 6 |
| —  | Host Login & Registration (Auth) | **Week 7** |
