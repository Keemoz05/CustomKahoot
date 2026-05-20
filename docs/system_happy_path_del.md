# System Happy Path: End-to-End Use Case Scenario

This document outlines a complete "happy path" scenario for a live interactive event (e.g., a quiz or presentation) using the system. It covers the full lifecycle of an event from creation to completion, detailing the experience from three distinct perspectives: **Host**, **Guest (Audience)**, and **System Display (Projector)**.

---

## 1. Host Perspective

The Host is responsible for creating, managing, and driving the live event.

| Feature (Actions & UI Elements) | Scenario | Steps to Execute | Expected Result |
| :--- | :--- | :--- | :--- |
| **Dashboard / Login**<br>*(Login Form, Dashboard UI)* | Host logs into the system to manage events. | 1. Navigate to the login page.<br>2. Enter valid credentials.<br>3. Click "Login". | Host is authenticated and redirected to the main Dashboard showing a list of past and upcoming events. |
| **Event Creation**<br>*(Create Button, Form Inputs)* | Host creates a new interactive session. | 1. Click "Create New Event".<br>2. Fill in Event Title and Description.<br>3. Click "Save & Continue". | A new event is created, and the host is taken to the event editor/setup page. |
| **Content Management**<br>*(Add Question, Save)* | Host adds interactive slides/questions to the event. | 1. Click "Add Question".<br>2. Enter the question text and multiple-choice options.<br>3. Mark the correct answer.<br>4. Click "Save". | The question is successfully added to the event's slide deck. |
| **Launch Live Setup**<br>*(Host Live Button)* | Host prepares to start the event and opens the control panel. | 1. From the dashboard, click "Host Live" next to the prepared event. | The Host Live Control Panel opens, initializing the WebSocket connection and generating the join PIN/QR code. |
| **Open Displays**<br>*(Projector Toggle, Start)* | Host opens the public projector view and invites guests. | 1. Click "Open Projector View".<br>2. Instruct audience to join using the displayed QR code or PIN. | A new window opens showing the System Display (join screen). Guests begin populating the "Audience" list on the host dashboard. |
| **Start Event / Next Slide**<br>*(Next Button, Slide Preview)* | Host begins the first question. | 1. Wait for guests to join.<br>2. Click "Start Event" / "Next Slide". | The Host Control Panel updates to show the active question stats. The Projector and Guest devices synchronize to display the question. |
| **View Live Feedback**<br>*(Results Toggle, Charts)* | Host views incoming answers and reveals the correct answer. | 1. Monitor real-time answer submissions.<br>2. Click "Show Results" once time is up or all guests have answered. | The control panel displays a bar chart of audience responses. The Projector and Guest devices update to show the results and the correct answer. |
| **End Event**<br>*(End Session Button)* | Host concludes the live session. | 1. Click the "End Event" button in the control panel.<br>2. Confirm the action. | The session is closed, WebSocket connections are terminated, and the host is redirected to a summary/reports page. |

---

## 2. Guest (Audience) Perspective

The Guest participates in the event using their mobile device or personal browser.

| Feature (Actions & UI Elements) | Scenario | Steps to Execute | Expected Result |
| :--- | :--- | :--- | :--- |
| **Join Session**<br>*(PIN Input, Join Button)* | Guest connects to the live event. | 1. Scan the QR code on the System Display OR navigate to the join URL and enter the PIN.<br>2. Enter a display name.<br>3. Click "Join". | Guest connects successfully and sees a "Waiting for the host to start" lobby screen. |
| **Wait in Lobby**<br>*(Lobby Screen, Loader)* | Guest waits for the host to initiate the content. | 1. Look at the device screen while waiting. | The screen displays "You're in! Look at the projector." and awaits a WebSocket signal. |
| **Answer Question**<br>*(Answer Buttons, Timer)* | Guest receives a question and submits an answer. | 1. Host transitions to a question slide.<br>2. Read the question on the device.<br>3. Tap one of the multiple-choice options. | The tapped option highlights. The screen updates to show "Answer Submitted. Waiting for others..." |
| **View Results**<br>*(Feedback Screen, Score)* | Guest sees if their answer was correct. | 1. Host clicks "Show Results".<br>2. Look at the device screen. | The screen reveals the correct answer, indicates whether the guest's choice was correct or incorrect, and displays any points earned. |
| **Event Conclusion**<br>*(Thank You Screen)* | Guest reaches the end of the event. | 1. Host ends the session. | The screen updates to a "Thank You for Participating!" message, and the active connection is closed. |

---

## 3. System Display (Projector) Perspective

The System Display is the public-facing screen shown on a large monitor or projector for all attendees to see.

| Feature (Actions & UI Elements) | Scenario | Steps to Execute | Expected Result |
| :--- | :--- | :--- | :--- |
| **Join Screen (Idle)**<br>*(QR Code, PIN, Player Count)* | System displays connection instructions before the event starts. | 1. Host opens the Projector View. | A clean, high-visibility screen displays the Event Title, a large QR Code, a URL, the session PIN, and a live counter of joined guests. |
| **Display Question**<br>*(Large Text, Timer, Options)* | System shows the current question to the audience. | 1. Host clicks "Next Slide" to start a question. | The screen transitions smoothly to display the question text, the available options (usually color-coded), and a countdown timer. |
| **Display Results**<br>*(Bar Chart, Correct Answer)* | System reveals the distribution of answers and the correct choice. | 1. Host clicks "Show Results". | The screen animates a bar chart showing how many people voted for each option. The correct answer is prominently highlighted. |
| **Show Leaderboard**<br>*(Top Players List)* | System displays current standings. | 1. Host navigates to the leaderboard slide. | The screen displays a ranked list of the top-scoring guests with celebratory micro-animations. |
| **End Screen**<br>*(Summary, Goodbye Message)* | System shows the final conclusion. | 1. Host clicks "End Event". | The screen displays a final podium or "Event Ended" splash screen, finalizing the public broadcast. |
