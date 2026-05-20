# Use Cases

---

## Actor Priority Summary

| Priority | Actor | Requires Login? | Auth Phase |
|----------|-------|-----------------|------------|
| 1st | Event Host (Dashboard) | No — open access during Weeks 1–6 | Auth added in Week 7 |
| 2nd | Guest | **No — join by name only, permanently** | Never required |
| 3rd | System (Display / Audio) | N/A | N/A |
| 4th | System Admin | Yes | Week 7+ |

---

## HOST DASHBOARD — Use Cases

> **Development stance:** During Weeks 1–6, the host dashboard is open-access (no login required) so all features can be built and tested without auth blocking progress. Host login and registration are added as a security layer in **Week 7**, after the full game loop is proven to work. At that point, all `/host/**` routes will be protected.

---

### Use Case H1: Create a New Event

**Goal:** Allow the host to initialize a new trivia event from their dashboard and configure its basic settings before adding content.

**Pre-condition:** The host is logged into their account and is on the dashboard home page.

**Process:**
1. The host clicks "New Event" on the dashboard.
2. The system prompts for a basic event name and optional description.
3. The host fills in the details and clicks "Create."
4. The system generates the event and opens the Event Editor view.

**Post-condition:** A new draft event is saved in the database. The host is now inside the content setup phase, ready to add questions or import a template.

---

### Use Case H2: Import Curated Question Bank

**Goal:** Allow the host to drastically reduce preparation time by importing pre-built, event-appropriate trivia templates rather than writing questions from scratch.

**Pre-condition:** The host has created a new event and is in the Content Setup phase.

**Process:**
1. The host navigates to the "Question Library" section of the dashboard.
2. The host selects a category matching their event (e.g., "Family Reunion," "Bridal Shower").
3. The host previews a specific template (e.g., "Guess the Baby Photo") and clicks "Import."
4. The system populates the event deck with pre-written questions.
5. The host clicks into individual questions to edit placeholder text (e.g., changing "[Name]" to "Uncle John").

**Post-condition:** The event has a fully populated, themed question set saved to the database, ready for media uploads or final approval.

---

### Use Case H3: Select Question Format

**Goal:** Allow the host to assign the most appropriate interaction type to each prompt, varying the event's pacing and maintaining accessibility for all guests.

**Pre-condition:** The host has an active event draft open and is editing or creating a specific question.

**Process:**
1. The host selects a question or prompt from their event timeline.
2. The host clicks the "Question Type" selector for that prompt.
3. The system presents a curated menu of accessible interaction types: Poll, Quiz (Multiple Choice), Word Cloud, or Slide.
4. Text-heavy or high-friction formats are either hidden or flagged with an accessibility warning.
5. The host selects the desired format.
6. The system dynamically updates the input fields to match the selected format.
7. The host inputs the content and saves the configuration.

**Post-condition:** The prompt is saved with the new format attached. During the live event, the venue display and guest devices will automatically render the correct UI for that question type.

---

### Use Case H4: Upload Nostalgic Media

**Goal:** Allow the host to personalize trivia questions with family photos or videos without needing graphic design skills.

**Pre-condition:** The host is editing a specific trivia question and has a digitized photo or video file saved on their device.

**Process:**
1. The host clicks the "Add Media" placeholder on the question editor.
2. The host drags and drops a file into the upload zone, or clicks to browse.
3. The system processes the file, automatically cropping and centering it within the chosen aesthetic template.
4. The system displays a live preview of how the slide will look on the main venue screen.
5. The host approves the preview and clicks "Save."

**Post-condition:** The media asset is securely stored and attached to the specific question, guaranteed to display without distorting the visual theme.

---

### Use Case H5: Launch the Game Lobby

**Goal:** Allow the host to open the game session so that guests can start joining via QR code.

**Pre-condition:** The host has finished setting up their event and is ready to go live.

**Process:**
1. The host clicks "Start Event" or "Go Live" from the dashboard.
2. The system generates a unique game PIN and a corresponding QR code.
3. The venue's main display transitions to the lobby waiting screen, prominently showing the QR code and game PIN.
4. The host's control dashboard switches to the live control view.

**Post-condition:** The game lobby is open. Guests can now scan the QR code or enter the PIN directly to join. The host sees joining participants appear on their dashboard in real time.

---

### Use Case H6: Trigger "Storyteller Pause"

**Goal:** Allow the host to freeze the digital game state so guests can share anecdotes or banter about the previous question without time pressure.

**Pre-condition:** The host has just revealed the correct answer to a question, and a related photo is displayed on the main screen.

**Process:**
1. The host observes a strong audience reaction or someone beginning to tell a story.
2. The host taps the "Pause / Hold" toggle on their live control dashboard.
3. The system disables the auto-advance and holds the current image on the main screen.
4. The guest tells their story to the room.
5. Once the conversation concludes, the host toggles "Resume" and clicks "Next."

**Post-condition:** The system resumes normal operation and gracefully transitions to the automated leaderboard.

---

### Use Case H7: Advance Slides & Control Game Flow

**Goal:** Give the host complete, real-time control over the pacing of the event — moving to the next question, locking answers, and triggering results.

**Pre-condition:** The game is live and at least one question has been displayed.

**Process:**
1. The host monitors the guest joining/answering progress on their control dashboard.
2. When satisfied, the host clicks "Lock Answers" to prevent further answer changes.
3. The host clicks "Reveal Answer" — the main screen shows the correct answer and the corresponding media.
4. The host clicks "Next" to advance to the leaderboard or the next question.

**Post-condition:** The game state advances for all connected devices simultaneously.

---

## GUEST WORKFLOW — Use Cases

> **Guests do not need to create an account, register, or log in.** They join using only a display name. The entire guest-side experience is frictionless by design.

---

### Use Case G1: Join via QR Code (No Login Required)

**Goal:** Allow guests — especially elderly attendees — to enter the game lobby without downloading an app, creating an account, or typing a complex URL.

**Pre-condition:** The host has launched the game lobby. The venue's main screen is displaying the welcome screen with a large QR code.

**Process:**
1. The guest opens the camera application on their smartphone.
2. The guest points the camera at the QR code shown on the main screen.
3. The guest taps the pop-up link generated by their camera app.
4. The mobile browser opens a minimalist landing page asking only for a "Display Name."
5. The guest types their name and taps the large "Join Game" button.
6. No account creation, email, or password is required at any step.

**Post-condition:** The guest's smartphone transitions to a "Waiting for Host" screen. Their chosen name appears on the venue's main display, confirming their entry.

---

### Use Case G2: Join via Game PIN (No Login Required)

**Goal:** Provide an alternative entry path for guests who cannot scan QR codes (e.g., older devices or camera difficulties).

**Pre-condition:** The host has launched the game lobby and the game PIN is visible on the main screen.

**Process:**
1. The guest opens any browser on their device and navigates to the app's root URL.
2. The landing page displays a single "Enter Game PIN" input field.
3. The guest types the PIN shown on the main screen.
4. The guest enters their display name and taps "Join."

**Post-condition:** The guest joins the same lobby as QR code joiners. Their name appears on the main display.

---

### Use Case G3: Submit Untimed Answer

**Goal:** Allow guests of all ages to confidently select their answer without the anxiety of a rapid-fire countdown clock.

**Pre-condition:** The host has advanced the game to a live question. The guest's mobile device shows large, high-contrast answer blocks.

**Process:**
1. The guest reads the question displayed on the venue screen.
2. The guest looks at their device and identifies their preferred answer among the large, labeled buttons.
3. The guest taps their chosen answer.
4. The mobile UI visually confirms the selection — the chosen button gently highlights, and a "Waiting for others" message appears.
5. The guest can change their answer at any time before the host locks the round.

**Post-condition:** The system records the guest's final answer. The guest's screen remains in a calm, resting state until the host reveals the correct answer.

---

### Use Case G4: View Personal Score & Rank

**Goal:** Allow each guest to privately see their own standing without the embarrassment of a public ranking that exposes low scorers.

**Pre-condition:** The host has triggered the leaderboard after a question.

**Process:**
1. The main venue screen displays only the top 3–5 performing players.
2. Simultaneously, each guest's mobile device privately displays their personal score and rank (e.g., "You are in 12th place!").

**Post-condition:** The guest understands their progress without their low rank being publicly announced. The positive, inclusive atmosphere of the room is maintained.

---

## SYSTEM — Use Cases

---

### Use Case S1: Render Minimalist Leaderboard

**Goal:** Display current game standings in an elegant, uncluttered format that celebrates top performers without publicly embarrassing lower-ranked guests.

**Pre-condition:** A trivia question has concluded, answers have been revealed, and the host has clicked "Next."

**Process:**
1. The system instantly calculates total points for all guests based strictly on answer accuracy (no speed bonus).
2. The system filters to isolate only the top 3–5 scoring players.
3. The system applies the host's chosen aesthetic template to the data.
4. The main venue screen smoothly transitions to display the top players.
5. Each guest's mobile device privately shows their personal score and rank.

**Post-condition:** The room's energy is maintained through positive reinforcement. The system waits for the host to trigger the next question.

---

### Use Case S2: Play Ambient Soundscape

**Goal:** Provide a continuous, sophisticated audio backdrop that enhances the event atmosphere without being intrusive or anxiety-inducing.

**Pre-condition:** The host selected an audio profile (e.g., "Acoustic Cafe") during event setup. The host's device is connected to the venue's audio system.

**Process:**
1. The host launches the game lobby.
2. The system begins streaming the selected ambient audio track.
3. The track loops seamlessly in the background as guests join and answer questions.
4. When a "Storyteller Pause" is triggered, the system automatically dips the audio volume by 50%.
5. Audio returns to full volume when the game resumes.

**Post-condition:** The event maintains a curated auditory aesthetic until the host manually mutes the audio or ends the session.

---

## SYSTEM ADMIN — Use Cases

---

### Use Case A1: QA Aesthetic Templates

**Goal:** Ensure that all visual themes provided to hosts look premium while strictly adhering to accessibility standards for older users.

**Pre-condition:** A UI designer has submitted a new visual template (e.g., "Midnight Velvet") for inclusion in the platform's library.

**Process:**
1. The Admin loads the new template into the backend testing environment.
2. The Admin runs an automated script to check WCAG contrast ratios between background and text colors.
3. The Admin verifies that font sizes on mobile do not drop below established elder-friendly minimums.
4. If the template passes all checks, the Admin marks it as "Approved."

**Post-condition:** The verified template is pushed to the live production database, making it available for hosts to select.
