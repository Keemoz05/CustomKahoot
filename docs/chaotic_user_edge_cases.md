# UAT Edge Cases: The Chaotic User

This document outlines testing scenarios designed to break the system through unpredictable, non-logical human behavior. As a "Chaotic User," the goal is to test the system's resilience against button-mashing, weird network conditions, malicious intent, and complete disregard for the "happy path."

---

## 1. The Chaotic Host

The Host has the most power, which means they can cause the most damage if they behave unpredictably.

| Feature Area | Chaotic Scenario | Steps to Execute | Expected Resilient Result |
| :--- | :--- | :--- | :--- |
| **Slide Control** | **The Machine Gunner** | Mash the "Next Slide" or "Show Results" button 20 times in a single second. | "Safe Tap" or debouncing logic prevents multiple API/WebSocket calls. Only one transition occurs. |
| **Session Management** | **The Multi-Verse Host** | Open the Host Control Panel in 5 different browser tabs simultaneously. Try to advance slides from all of them randomly. | State remains synchronized across all tabs via WebSockets. No duplicate slides are shown; system gracefully handles simultaneous commands without crashing. |
| **Network Stability** | **The Ghost Host** | Start a question, then immediately turn off Wi-Fi or close the laptop lid. Reconnect 5 minutes later. | The projector/guests continue counting down. When the host reconnects, the dashboard pulls the latest state instead of resetting the event. |
| **Session End** | **The Time Traveler** | Click "End Event", wait for the summary page, then rapidly hit the browser's "Back" button and try to click "Next Slide". | API should return a `403` or `400` error (Event Ended). The UI should reflect that the event is dead and prevent further actions. |
| **Data Integrity** | **The Impatient Grader** | Click "Show Results" at the *exact* millisecond a guest submits an answer. | Race condition test. The backend handles the transaction lock correctly; the guest's score is either counted before the reveal or rejected if the reveal triggers first. No server crashes (500 errors). |

---

## 2. The Chaotic Guest (Audience)

Guests are numerous and often use unpredictable devices under terrible network conditions.

| Feature Area | Chaotic Scenario | Steps to Execute | Expected Resilient Result |
| :--- | :--- | :--- | :--- |
| **Authentication** | **The Clone Army** | Have 10 guests join simultaneously using the exact same display name (e.g., "John") or use automated scripts to spam join requests. | System appends a unique identifier (e.g., "John (1)"), or uses underlying session IDs to distinguish them. No database unique constraint crashes. |
| **Input Validation** | **The Payload Dropper** | Enter a display name that is 50,000 characters long, entirely emojis, right-to-left text, or contains XSS (`<script>alert(1)</script>`). | Input is truncated to a reasonable length (e.g., 20 chars). XSS is sanitized. Emojis and RTL text render without breaking the projector UI layout. |
| **Answering** | **The Piano Player** | When a question appears, use 4 fingers to tap all 4 multiple-choice options at the exact same time. | Only one input is registered. The first processed touch event disables the other buttons immediately to prevent multiple submissions. |
| **State Manipulation** | **The Indecisive Voter** | Tap an answer, immediately refresh the browser tab, and try to tap a different answer before the timer runs out. | Upon refresh, the system recognizes the session ID, sees the user already answered, and locks them into the "Waiting for others" screen. |
| **Device Behavior** | **The Sleepy Phone** | Lock the phone screen mid-question. Wake the phone up 15 minutes later when the host is 3 questions ahead. | WebSocket reconnects automatically. The screen immediately skips to the current active question without showing the backlog of missed questions. |

---

## 3. The Chaotic System Display (Projector)

The projector is a passive display, but the browser running it can experience weird environments.

| Feature Area | Chaotic Scenario | Steps to Execute | Expected Resilient Result |
| :--- | :--- | :--- | :--- |
| **Responsiveness** | **The Window Resizer** | Rapidly resize the projector window from fullscreen to 100x100 pixels, or move it to a 32:9 ultra-wide monitor. | Flexbox/CSS Grid handles the resizing dynamically. Text scales appropriately; QR code remains scannable and doesn't stretch or break out of its container. |
| **Resource Mgmt** | **The Marathon Session** | Leave the projector tab open on the "Join" screen for 48 hours without interacting with it. | No memory leaks. The WebSocket ping/pong keeps the connection alive, or it auto-reconnects gracefully without crashing the browser tab. |
| **Browser Focus** | **The Background Tab** | Minimize the projector window or switch to a different tab while a heavy animation (like the leaderboard) is playing. | Browser background throttling doesn't desync the state. When brought back to focus, the UI catches up to the current state instantly rather than playing 5 minutes of queued animations. |
| **Tampering** | **The Inspect Element Hacker** | A mischievous student right-clicks the Projector screen, opens DevTools, and changes the HTML text of a question to something inappropriate. | While local HTML changes can't be stopped, the next WebSocket update (e.g., showing results) completely overwrites the DOM, wiping the tampered text. |
