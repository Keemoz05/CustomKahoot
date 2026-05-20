# Interface Design — CustomKahoot (QuizYa)

> **Rendering:** Thymeleaf (server-rendered HTML) + HTMX + Alpine.js  
> **Real-time:** Spring WebSockets (STOMP) over Redis broker  
> **Aesthetic engine:** Per-event `templates.css_config` (JSONB) injected as CSS custom properties  

---



 View Map & Page Specifications

The platform has **three simultaneous views** during a live event, plus a **backend dashboard** used before and after events, and a **public landing page** for first-time visitors.

| View | Device | Audience | Auth |
|------|--------|----------|------|
| **Landing Page** | Any browser | Public visitors | None (unauthenticated) |
| **Guest Mobile** | Smartphone browser | Event guests (anonymous) | Session token via QR scan |
| **Venue Main Display** | Large screen / projector | Entire room | Host-authenticated browser tab |
| **Host Control Dashboard** | Laptop / tablet | Event host | Email + password (Spring Security) |
| **Admin Panel** | Desktop browser | System admins | Email + password (elevated role) |

---

<br>

## 1. Landing Page — Public Entry Point

**URL:** `/`

**Navigation:** The first page any visitor sees when they type the domain into their browser. This is the public face of QuizYa — it must instantly communicate the product's value, aesthetic quality, and ease of use. Users who are not logged in and visit any authenticated route (`/dashboard/**`) are redirected to `/login`, but users who visit the root domain arrive here.

**Key Features:**

- **Hero section (above the fold):**
  - **Headline** in Fraunces (display weight): *"Unforgettable moments, one question at a time."*
  - **Subheadline** in Inter (body weight): *"The premium trivia platform for weddings, reunions, and celebrations that every generation can enjoy."*
  - **Two call-to-action buttons** — side by side, horizontally centered:
    - **"Get started"** — primary filled button (Deep Teal background, white text) → navigates to `/register`.
    - **"Log in"** — secondary outlined button (Deep Teal border and text, transparent background) → navigates to `/login`.
  - **Hero visual:** A large, softly rounded screenshot or illustration showing the venue display with guests' names appearing — demonstrating the product in action. Use a warm, golden-hour color treatment.
  - **Background:** Snow (`#FAFAF9`) with a very subtle, slow-moving gradient that shifts between warm white tones — never distracting, just enough to feel alive.

- **Social proof strip** (just below the hero):
  - A quiet, horizontally centered line of text: *"Designed for weddings · family reunions · milestone celebrations"* — in Text Secondary color, Inter weight 400.

- **Value propositions section (three-column grid on desktop, stacked on mobile):**
  - Each card is a minimal white card on the Warm White background, with ample internal padding (`--space-6`), no border, and a `--shadow-xs` on hover.
  - **Card 1:** Icon (smartphone scan) + heading *"One scan to join"* + body *"Guests scan a QR code and they're in — no app downloads, no accounts, no friction."*
  - **Card 2:** Icon (palette) + heading *"Beautifully themed"* + body *"Choose from 50+ professionally designed templates that match your celebration's aesthetic perfectly."*
  - **Card 3:** Icon (heart) + heading *"Every generation welcome"* + body *"Large text, calm pacing, and zero countdown anxiety — designed so every guest from 8 to 88 can play together."*

- **How it works section (vertical visual timeline, 3 steps):**
  - **Step 1:** *"Create your event"* — *"Import curated questions or write your own. Drag in family photos. Pick a beautiful theme."*
  - **Step 2:** *"Guests scan and join"* — *"Display the QR code on any screen. Guests scan with their phone camera — that's it."*
  - **Step 3:** *"Play together"* — *"Control the pace from your dashboard. Pause for stories. Celebrate at the leaderboard."*
  - Each step has a numbered circle (Deep Teal, white text, `--radius-full`) and a subtle connecting vertical line between steps.

- **Final CTA section (centered, generous vertical padding):**
  - Heading: *"Ready to make your next event unforgettable?"*
  - Single large **"Create your free event"** button → navigates to `/register`.
  - Below the button, a small reassuring line: *"No credit card required."*

- **Footer:**
  - Minimal. Centered layout. Links: *About · Contact · Privacy · Terms*
  - Copyright line: *"© 2026 QuizYa"*
  - All links are text-secondary color, Inter weight 400.

- **Global behavior:**
  - No navigation bar on the landing page — the page IS the introduction. The only navigation is through the CTA buttons.
  - The page is fully responsive (single column at `≤ 768px`, two/three columns above).
  - All sections fade-in softly on scroll (IntersectionObserver, 400ms ease-out, 50px translate-up).
  - Page loads without any blocking JavaScript — Thymeleaf renders the full HTML server-side.

---

<br>

## 2. Host Login Page

**URL:** `/login`

**Navigation:** Reached via the "Log in" button on the landing page (`/`), or by an automatic redirect when unauthenticated users hit any `/dashboard/**` route via Spring Security.

**Key Features:**

- **Centered card layout** on a Snow background, with the QuizYa wordmark at the top of the card and the tagline *"Premium trivia for life's best moments"* beneath it in Text Secondary.
- **Email + password form** with large, accessible input fields (minimum height 48px, `--text-base` font size, `--radius-md` corners).
- Subtle **gradient background** that slowly shifts hue using a CSS animation — creates an immediately premium first impression. The gradient uses very subtle warm tones (barely perceptible shift between Snow and Warm White).
- **"Create account"** link beneath the form, navigating to `/register`.
- **"Back to home"** link (small, text-tertiary) navigating to `/` — so users can return to the landing page.
- On successful login → smooth **fade-out transition** (300ms ease) → redirect to `/dashboard`.
- On failure → **inline error toast** slides down from the top of the card with a gentle shake animation.

---

<br>

## 3. Host Registration Page

**URL:** `/register`

**Navigation:** Reached via the "Get started" button on the landing page (`/`), or via the "Create account" link on `/login`.

**Key Features:**

- Same centered card layout as `/login` for visual consistency.
- Fields: **Display Name**, **Email**, **Password**, **Confirm Password**.
- Real-time **inline validation** via Alpine.js (password match check, email format).
- On submit → Thymeleaf server-side validation → BCrypt hash → redirect to `/login` with a success banner: *"Account created — log in to get started."*
- **"Already have an account?"** link back to `/login`.
- **"Back to home"** link (small, text-tertiary) back to `/`.

---

<br>

## 4. Host Dashboard — Event List

**URL:** `/dashboard`

**Navigation:** Landing page after successful login. Also accessible from the sidebar nav at any time.

**Key Features:**

- **Left sidebar navigation** (persistent across all `/dashboard/**` pages):
  - My Events, Question Library, Template Gallery, Audio Library, Account Settings.
  - Sidebar collapses to icon-only on smaller screens (responsive).
- **Main area — Event cards grid:**
  - Each card shows: event **title**, **status badge** (`DRAFT` / `LOBBY` / `LIVE` / `COMPLETED` / `ARCHIVED`), **template thumbnail**, **guest count**, and **created date**.
  - Status badges use color-coded pills (see A2 Status Badge Colors above).
  - Hovering a card lifts it with a subtle **box-shadow elevation + scale(1.02)** transition.
  - Clicking a card → navigates to `/dashboard/events/{eventId}`.
- **"+ Create New Event"** floating action button (bottom-right) or prominent header button.
  - Opens a **modal overlay** (slides up from bottom on mobile, fades in centered on desktop).
  - Modal fields: **Title**, **Description** (optional), **Template** (dropdown picker with thumbnail previews), **Max Guests** (default 200).
  - On submit → HTMX POST → card appears in the grid with a **slide-in animation** → auto-navigates to the new event's setup page.

---

<br>

## 5. Event Setup Page — Content Setup Phase

**URL:** `/dashboard/events/{eventId}`

**Navigation:** Reached by clicking an event card on `/dashboard`, or after creating a new event.

**Key Features:**

- **Horizontal stepper/tab bar** at the top: `Content` → `Aesthetics` → `Audio` → `Settings` → `Launch`. Active step is highlighted with an underline + color accent. Clicking any step navigates via HTMX partial swap.
- **Content tab (default):**
  - **Left panel — Question timeline:** A vertical, drag-and-drop reorderable list of question/slide cards. Each card shows:
    - Sort number, prompt text preview (truncated), question type icon (`MC` / `Poll` / `☁️` / `📄`), attached media thumbnail (if any).
    - Drag handle (⠿ icon) on the left edge. Reordering fires an HTMX PATCH to update `questions.sort_order`.
  - **Right panel — Question editor** (detail view of the selected question):
    - **Question Type selector:** A row of four styled toggle buttons — `Multiple Choice`, `Poll`, `Word Cloud`, `Slide`. Selecting one dynamically swaps the input fields below (HTMX swap) per Use Case 9.
    - **Prompt text:** Large textarea with character count.
    - **Answer options** (for MC/Poll): Up to 4 option input fields, each with a color swatch picker and a "✓ Correct" radio toggle (MC) or no correct marker (Poll).
    - **Media attachment zone:** A dashed-border drop zone with a centered upload icon. Supports drag-and-drop (Dropzone.js) and click-to-browse. On upload → Cloudinary processes → live preview appears inside the zone showing the auto-cropped result. A **"Preview on main screen"** button opens a modal simulating the venue display render.
    - **Points value** input (default 100).
    - **Timer override** toggle: Off by default (untimed, elder-friendly). Toggling on reveals a seconds input field.
    - **Explanation text** textarea: "Story behind this question" — displayed during Storyteller Pause.
  - **Bottom bar:** `+ Add Question` button, `Import from Library` button (navigates to `/dashboard/events/{eventId}/import`).

---

<br>

## 6. Question Library — Import Dialog

**URL:** `/dashboard/events/{eventId}/import`

**Navigation:** Reached via the "Import from Library" button on the Content tab of an event. The page can also be browsed standalone from the sidebar under "Question Library" at `/dashboard/library`.

**Key Features:**

- **Category filter bar:** Horizontal scrollable chips — `All`, `Wedding`, `Family Reunion`, `Bridal Shower`, `Anniversary`, `Birthday`, `Corporate`, etc. Active chip is filled, inactive are outlined. Clicking a chip filters the grid via HTMX GET with a smooth fade-replace.
- **Template bank cards grid:** Each card shows:
  - Bank title (e.g. *"Guess the Baby Photo"*), description preview, question count badge, and a themed accent color strip on the left edge.
  - **"Preview"** button → expands the card inline (accordion-style) to reveal all question prompts in a condensed list.
  - **"Import"** button → HTMX POST clones the bank into the event's `question_banks`, redirects back to the Content tab with the new questions populated and a **success toast** sliding in from the top-right.

---

<br>

## 7. Aesthetics Tab — Template Gallery

**URL:** `/dashboard/events/{eventId}#aesthetics`

**Navigation:** Clicking the "Aesthetics" step in the horizontal stepper on the event setup page.

**Key Features:**

- **Template gallery grid:** Cards for each of the 50+ templates, organized by `template_categories`.
  - Each card shows: template name, preview image (full-width thumbnail), category tag, and a `★ Premium` badge if `is_premium = TRUE`.
  - Hovering a card → the preview image **gently zooms (scale 1.05)** and the card border glows with the template's primary accent color.
  - Clicking a card → **modal with a full-screen preview** simulating: (a) venue main display view, (b) guest mobile view, and (c) leaderboard view, all rendered with the template's `css_config` values. A **"Apply to Event"** button confirms the selection.
- **Currently applied template** is shown at the top with a highlighted border and a "✓ Active" badge.
- Selecting a new template fires an HTMX PATCH to `events.template_id` → the page transitions with a **smooth cross-fade** as the new theme's CSS custom properties propagate.

---

<br>

## 8. Audio Tab

**URL:** `/dashboard/events/{eventId}#audio`

**Navigation:** Clicking the "Audio" step in the horizontal stepper.

**Key Features:**

- **Audio track browser:** List of available ambient tracks from `audio_tracks`, each row showing:
  - Track title, genre tag pill, duration, and a **mini play button** (▶) that streams a 15-second preview via the Web Audio API.
  - **"+ Add to Event"** button → adds the track to `event_audio` join table.
- **Event playlist panel** (right side or below on mobile):
  - Drag-and-drop reorderable list of selected tracks.
  - Per-track controls: **volume slider** (0–100%, default 75%), **loop toggle**, **remove** (✕) button.
  - A master **"Preview Playlist"** play button that simulates the full ambient stream.

---

<br>

## 9. Settings Tab

**URL:** `/dashboard/events/{eventId}#settings`

**Navigation:** Clicking the "Settings" step in the horizontal stepper.

**Key Features:**

- **Max Guests** — number input (default 200). Shows a subtle warning if set above 200: *"Ensure your infrastructure supports this count."*
- **Leaderboard Display Count** — dropdown: 3, 4, or 5 (default 5). Controls `events.show_leaderboard_count`. Preview text: *"Only the top N players will be shown on the venue screen."*
- **Scoring Mode** — toggle between `Accuracy Only` (default) and `Speed Bonus` (future, grayed out with "Coming Soon" tag).
- **Event Description** — editable textarea.
- **Danger Zone** (red-bordered section at bottom): `Archive Event`, `Delete Event` with confirmation modals.

---

<br>

## 10. Launch Tab — QR Code & Lobby Activation

**URL:** `/dashboard/events/{eventId}#launch`

**Navigation:** Clicking the "Launch" step in the horizontal stepper. Only fully enabled when the event has at least 1 question.

**Key Features:**

- **Pre-launch checklist** with animated checkmarks:
  - ✅ Questions added (count shown)
  - ✅ Template selected
  - ✅ Audio configured (optional — shown as ⬜ if skipped)
- **Generated QR Code** displayed prominently (large, center-screen). Encodes the URL `{domain}/join/{joinCode}`. The QR image is also downloadable as PNG.
- **Join Code** displayed below the QR in a large, copy-to-clipboard monospace font.
- **"Open Venue Display"** button → opens `/venue/{joinCode}` in a new tab (intended for the projector-connected screen).
- **"Go Live"** button (large, gradient-filled, centered):
  - Changes `events.status` from `DRAFT` → `LOBBY`.
  - The button has a **pulsing glow animation** to draw attention.
  - On click → HTMX POST → status updates → the button transforms into a **"Start Game"** button. The Venue Display tab simultaneously transitions to the lobby view via WebSocket.

---

<br>

## 11. Venue Main Display — Lobby / Welcome Screen

**URL:** `/venue/{joinCode}`

**Navigation:** Opened by the host via the "Open Venue Display" button on the Launch tab. Displayed on the venue's projector or large screen. This page runs fullscreen (host presses F11 or clicks a "Go Fullscreen" prompt on first load).

**Key Features:**

- **Full-screen, template-themed layout** using the event's `css_config` custom properties (colors, fonts, border radii).
- **Event title** displayed prominently at the top in elegant, large typography.
- **Massive QR code** centered on screen — large enough to scan from across a room.
- **Join code** displayed below the QR in oversized text: `Join at quizya.com/join/ABCD12`.
- **Guest roster** — as guests join via WebSocket, their display names **animate in one-by-one** (fade-up + slight bounce) in a flowing, wrapping grid/list below the QR code. Each name appears inside a styled chip/badge matching the template.
- **Guest count indicator** in the corner: `12 / 200 joined`.
- **Ambient audio** begins streaming automatically once the host clicks "Go Live" (respects browser autoplay — the host's initial "Go Live" click counts as user interaction).
- The page listens on a STOMP WebSocket subscription. When the host clicks "Start Game", the display receives a state-change event and **smoothly transitions** (crossfade, 500ms) to the first question slide.

---

<br>

## 12. Guest Mobile — Join Landing Page

**URL:** `/join/{joinCode}`

**Navigation:** The guest arrives here by scanning the QR code on the venue display with their smartphone camera. The camera app shows a pop-up link; tapping it opens this page in the mobile browser.

**Key Features:**

- **Ultra-minimalist, single-purpose page.** No nav bar, no footer, no distractions — just the essentials.
- **Event title** at the top so the guest confirms they're joining the right event.
- **"Enter Your Name"** — a single, large input field with oversized placeholder text and a thick, high-contrast border. Input font size is at least **24px** for elder readability.
- **"Join Game"** — a single, large, full-width button below the input. High-contrast colors (e.g. white text on a deep colored background from the template). Touch target height ≥ **56px**.
- The template's `css_config` is applied, so the page visually matches the venue display's aesthetic.
- On submit → HTMX POST creates an `event_guests` record with a generated `session_token` (stored in a cookie/localStorage) → **smooth page transition** (the form slides out left, the waiting screen slides in from the right) → guest lands on the Waiting Screen.
- **Error states:** If the event is not in `LOBBY` or `LIVE` status, the page shows a friendly message: *"This event hasn't started yet"* or *"This event has ended."*

---

<br>

## 13. Guest Mobile — Waiting Screen

**URL:** `/play/{joinCode}` (same page, state-driven via WebSocket)

**Navigation:** Automatically shown after the guest successfully joins. The guest does not manually navigate here.

**Key Features:**

- **"Waiting for host to start…"** message with a **subtle loading animation** (pulsing dots or a gentle breathing circle — not a spinner, which implies urgency).
- **Guest's display name** shown: *"You joined as Lola 👋"*
- **Template-themed background** — the page feels cohesive with the venue display.
- The page subscribes to a STOMP WebSocket topic. When the host clicks "Start Game", the server pushes a `GAME_START` event → the waiting screen **transitions** (fade-out, then slide-up of the first question) to the Answer Screen.

---

<br>

## 14. Guest Mobile — Answer Screen

**URL:** `/play/{joinCode}` (same URL, real-time state changes via WebSocket)

**Navigation:** Transitions automatically from the Waiting Screen when the host advances the game.

**Key Features:**

- **Question prompt** displayed at the top in large, readable text (minimum **20px**, high contrast against the template background).
- **Media display** (if attached): The auto-cropped image/video from Cloudinary renders below the prompt, filling the width of the screen.
- **Answer blocks** — up to 4 large, high-contrast, text-labeled buttons arranged in a **2×2 grid** (or stacked vertically on very narrow screens):
  - Each block uses the `color_hex` from `question_options` or falls back to template defaults.
  - Touch target: full-width, minimum height **72px**, with large **18px+ bold text**.
  - On tap → the selected block **gently highlights** (border glow + slight scale-up + checkmark icon overlay) and the other blocks dim to 60% opacity. A calm *"Answer locked ✓"* message appears below.
  - **The guest can change their answer** by tapping a different block — the highlight smoothly transfers.
  - No timer is displayed by default (untimed). If `time_limit_seconds` is set, a subtle, non-anxiety-inducing thin progress bar appears at the top (no ticking sound, no flashing).
- **For Word Cloud type:** A single large text input replaces the answer blocks, with a "Submit" button.
- **For Slide type:** No interactive elements — just the prompt and media. A *"Waiting for host…"* message is shown.
- **For Poll type:** Same as Multiple Choice layout but without a "correct answer" indicator on reveal.
- When the host locks the round / reveals the answer, a WebSocket event triggers:
  - The correct answer block **pulses green** ✅
  - If the guest chose correctly → **confetti micro-animation** + *"Correct! +100 points"*
  - If the guest chose incorrectly → the guest's selected block shows a subtle ✗, with a gentle *"Not this time"* message (no harsh red X or failure sounds).
  - Transition to a **mini personal score card**: *"Your score: 300 pts · Rank: 8th of 45"* — private to the guest's device.

---

<br>

## 15. Guest Mobile — Final Results Screen

**URL:** `/play/{joinCode}` (state-driven, appears when host ends the game)

**Navigation:** Automatically shown when the host completes the event (`events.status` → `COMPLETED`).

**Key Features:**

- **Personal summary card:**
  - Guest's display name, total score, correct count / total questions, accuracy percentage.
  - A **tier badge** based on performance (e.g. 🥇 "Trivia Champion" for top 3, 🌟 "Sharp Mind" for top 10, 👏 "Great Sport" for everyone else).
- **"Thanks for playing!"** message with event title.
- The page is static — no further WebSocket updates. The guest can close their browser.

---

<br>

## 16. Venue Main Display — Live Question Slide

**URL:** `/venue/{joinCode}` (same page, WebSocket-driven state changes)

**Navigation:** Transitions from the Lobby when the host clicks "Start Game". Each subsequent question is pushed via WebSocket when the host clicks "Next".

**Key Features:**

- **Full-screen, template-themed slide:**
  - **Question prompt** in large, venue-readable typography (40–60px depending on length, auto-scaled).
  - **Media** (if attached): displayed prominently — images fill up to 50% of the screen area, videos auto-play muted with the option for the host to unmute via their control dashboard.
  - **Answer options** (for MC/Poll): Displayed as 4 large colored blocks in a 2×2 grid, labeled A/B/C/D with the option text. Each block uses the template's color palette or custom `color_hex`.
  - **Live response counter** (optional, bottom corner): *"32 of 45 answered"* — updates in real-time as WebSocket events arrive for each `guest_answers` submission.
- **Answer reveal animation:** When the host triggers reveal:
  - Incorrect options **fade to grayscale and shrink slightly**.
  - The correct option **pulses with a golden glow** and scales up 5%.
  - If `explanation_text` exists, it fades in below the question as elegant italic text — the "story behind the answer" for the Storyteller Pause moment.
- **Transition between questions:** smooth **crossfade + slide** (old slide fades and slides left, new slide fades in from right). Duration: 600ms.

---

<br>

## 17. Venue Main Display — Storyteller Pause

**URL:** `/venue/{joinCode}` (same page, paused state via WebSocket)

**Navigation:** Triggered when the host taps "Pause/Hold" on their control dashboard after an answer reveal.

**Key Features:**

- The current slide **freezes** — the revealed answer and media remain on screen.
- A subtle **"Paused"** indicator appears in the top-right corner (small, semi-transparent pill — not intrusive).
- The `explanation_text` remains fully visible, giving context to the storyteller.
- **Ambient audio automatically dips to 50% volume** via a Web Audio API gain node adjustment, triggered by the `PAUSE` WebSocket event. The lower volume lets the speaker be heard without killing the mood.
- The slide remains static until the host taps "Resume" → audio fades back to 100% → host clicks "Next" → smooth transition to the leaderboard or next question.

---

<br>

## 18. Venue Main Display — Leaderboard

**URL:** `/venue/{joinCode}` (same page, leaderboard state via WebSocket)

**Navigation:** Automatically displayed after the host reveals an answer and clicks "Next" (if the host configured post-question leaderboards). Also shown as the final screen when the event completes.

**Key Features:**

- **Top 3–5 players only** (controlled by `events.show_leaderboard_count`). Lower-ranking players are deliberately hidden to prevent embarrassment.
- **Staggered reveal animation:**
  - 5th place appears first → slides up from below.
  - Each higher rank **animates in** with increasing dramatic delay (200ms stagger).
  - 1st place appears last with a **golden glow + slight bounce** and a subtle sparkle effect.
- Each entry shows: **rank number**, **display name**, **cumulative score**.
- Template-themed styling: fonts, colors, and decorative elements all match the event's chosen aesthetic.
- A `leaderboard_snapshots` record is written to the database at this point for historical playback.
- After a configurable dwell time (or host "Next" click), the display transitions to the next question slide.

---

<br>

## 19. Host Control Dashboard — Live Game Controls

**URL:** `/dashboard/events/{eventId}/live`

**Navigation:** After the host clicks "Start Game" on the Launch tab, the dashboard automatically transitions to this live control view. A prominent **"Return to Live Controls"** button is also always visible in the sidebar when an event is `LIVE`.

**Key Features:**

- **Current question preview:** Shows the prompt, options, and media for the question currently on the venue display. The host confirms what the room is seeing.
- **Response tracker:** Real-time bar chart or progress ring showing how many guests have answered vs. total connected. Updates live via WebSocket.
- **Control buttons (large, clearly labeled):**
  - **"Reveal Answer"** — sends a WebSocket event that triggers the answer reveal animation on both the venue display and all guest devices.
  - **"Pause / Hold"** toggle — triggers the Storyteller Pause. Button changes to **"Resume"** when active, with a timer showing pause duration.
  - **"Next"** — advances to the leaderboard or next question. Disabled until the answer has been revealed.
  - **"End Event"** — confirmation modal → sets `events.status` to `COMPLETED`, triggers final leaderboard on venue display, and final results on guest devices.
- **Guest management panel** (collapsible sidebar section):
  - List of connected guests with `is_connected` status indicators (green dot = active, gray = disconnected).
  - Guest count: *"38 connected / 45 joined"*.
- **Audio controls:** Small audio panel in the footer — play/pause, volume slider, and current track title. These override the ambient stream in real-time.
- **Question timeline** (compact, left rail): Clickable list of all questions with completion status. The host can see which questions remain. Clicking a future question does NOT skip — it only previews. Sequence is strictly linear.

---

<br>

## 20. Admin Panel — Template QA

**URL:** `/admin/templates`

**Navigation:** Accessible only to users with `role = ADMIN`. The admin logs in via the same `/login` page and is routed to the admin panel based on their role. A top navigation bar distinguishes the admin panel from the host dashboard.

**Key Features:**

- **Template list table:**
  - Columns: Name, Category, Status (`Pending Review` / `Approved` / `Rejected`), Submitted Date, Actions.
  - Sortable and filterable by category and status.
- **Template review page** (`/admin/templates/{templateId}`):
  - Full preview of the template rendered across all three views (venue, mobile, leaderboard).
  - **Automated WCAG report panel:**
    - Contrast ratio results for every text/background color pair.
    - Font size compliance check (minimum sizes for elder-friendly mobile view).
    - Pass/fail badges with specific WCAG level (AA, AAA).
  - **Approve / Reject** buttons with a comment field for feedback to the designer.
  - Approving sets `templates.is_active = TRUE` and pushes the template to the production database.

---

<br>

## 21. Admin Panel — Audio & Content Management

**URL:** `/admin/audio` and `/admin/banks`

**Navigation:** Via the admin top navigation bar tabs.

**Key Features:**

- **Audio management** (`/admin/audio`):
  - Upload new tracks (with CDN URL, title, genre tag, duration).
  - Toggle `is_active` to show/hide tracks from hosts.
  - Playback preview.
- **Curated question bank management** (`/admin/banks`):
  - Create and edit system-wide curated banks (`is_curated = TRUE`).
  - Assign banks to categories.
  - View analytics: *"This bank has been imported 142 times."* (via `source_bank_id` tracking).

---

<br>

## Navigation Flow Summary

```
                        ┌───────────────────┐
                        │       /           │
                        │   Landing Page    │
                        └───┬──────────┬────┘
                 "Get started"         "Log in"
                            │          │
                            ▼          │
                   ┌─────────────┐     │
                   │  /register  │     │
                   └──────┬──────┘     │
                          │ success    │
                          ▼            ▼
                        ┌─────────────────────┐
                        │     /login          │
                        └────────┬────────────┘
                                 │ auth success
                                 ▼
                        ┌─────────────────────┐
                  ┌─────│    /dashboard        │─────┐
                  │     └────────┬────────────┘     │
                  │              │ click event      │ sidebar
                  │              ▼                  ▼
                  │     ┌─────────────────────┐   /dashboard/library
                  │     │ /dashboard/events/  │   /dashboard/templates
                  │     │     {eventId}       │   /dashboard/audio
                  │     │                     │   /dashboard/settings
                  │     │  [Content]          │
                  │     │  [Aesthetics]       │
                  │     │  [Audio]            │
                  │     │  [Settings]         │
                  │     │  [Launch]           │
                  │     └────┬───────────┬────┘
                  │          │           │
              "Open Venue"   │      "Start Game"
                  │          │           │
                  ▼          │           ▼
        ┌──────────────┐     │  ┌──────────────────────┐
        │  /venue/     │     │  │ /dashboard/events/   │
        │  {joinCode}  │◄────┘  │   {eventId}/live     │
        │              │  WS    │                      │
        │  Lobby       │◄──────►│  Reveal / Pause /    │
        │  Question    │        │  Next / End          │
        │  Leaderboard │        └──────────────────────┘
        └──────────────┘
              ▲
  QR Scan     │
┌─────────────┘
│
┌───────┴──────────┐
│  /join/{joinCode} │  ──►  /play/{joinCode}
│                   │       (Waiting → Answer → Results)
│  Guest Mobile     │
└──────────────────┘
```

---

<br>

## Transition & Animation Reference

| Trigger | Animation | Duration | Easing |
|---------|-----------|----------|--------|
| Page load (all) | Fade-in from white | 300ms | ease-out |
| Landing page sections on scroll | Fade-up (50px translate-Y) | 400ms | ease-out |
| Guest name appears in lobby | Fade-up + bounce | 400ms | cubic-bezier(0.34, 1.56, 0.64, 1) |
| Question slide transition | Crossfade + slide-left | 600ms | ease-in-out |
| Answer block selected | Border glow + scale(1.03) | 200ms | ease |
| Answer reveal — correct | Golden pulse + scale(1.05) | 500ms | ease-out |
| Answer reveal — incorrect | Grayscale + scale(0.97) | 400ms | ease |
| Leaderboard rank entry | Slide-up, staggered 200ms | 400ms each | ease-out |
| Storyteller Pause — audio dip | Volume 100% → 50% | 800ms | linear fade |
| Toast notification | Slide-in from top-right | 300ms | ease-out |
| Modal open | Fade backdrop + scale card from 0.95 | 250ms | ease-out |
| Modal close | Reverse of open | 200ms | ease-in |
| Landing page hero gradient | Subtle warm hue shift | 12s | linear infinite |
| Card hover (dashboard) | Elevation + scale(1.02) | 200ms | ease |

---

<br>

## Responsive Breakpoints

| Breakpoint | Target | Layout Notes |
|------------|--------|--------------|
| `≤ 480px` | Guest mobile (primary) | Single column. Answer blocks stack 1×4 or 2×2. Maximum touch-target sizing. |
| `481–768px` | Tablet / small laptop | Sidebar collapses. Two-column layouts where appropriate. Landing page value props stack single-column. |
| `769–1024px` | Host laptop dashboard | Full sidebar + main content area. Landing page shows 2-column value props. |
| `≥ 1025px` | Venue display / widescreen | Full-screen slides. Maximum typography scaling. Landing page shows 3-column value props grid. |

---

<br>

## Accessibility Baseline (All Pages)

- **Minimum font size (mobile):** 16px body, 20px question prompts, 18px answer blocks.
- **Minimum touch target:** 48×48px (WCAG 2.5.5), answer blocks ≥ 72px height.
- **Contrast ratio:** Minimum 4.5:1 (AA) for all text, enforced by template QA pipeline.
- **No autoplay audio on guest devices** — audio only streams from the host's machine.
- **No rapid-fire animations** — all transitions are calm, eased, and ≥ 200ms.
- **Focus indicators:** Visible focus ring on all interactive elements for keyboard navigation.
- **Screen reader support:** Semantic HTML, ARIA labels on all buttons and inputs, `alt_text` on all media assets.
- **Landing page:** Fully functional without JavaScript — form links work as standard `<a>` tags, CTA buttons are native links. Scroll animations are progressive enhancement only.
