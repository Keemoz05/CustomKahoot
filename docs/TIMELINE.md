Key Milestones
Week 1: Architecture & Guest Onboarding (April 2 – April 8)
Goal: Establish the repository, database schema, and achieve a frictionless entry for users.
Configure the real-time database and web socket connections.
Develop the Venue Main Display layout and the QR code generation logic.
Build the mobile-responsive, minimalist landing page for guests.
Deadline (April 8): Complete Use Case 1. A guest can scan a QR code, enter their display name, and successfully appear on the main venue screen's waiting lobby.
Week 2: Core Gameplay Loop & Interaction Types (April 9 – April 15)
Goal: Implement the primary answering mechanics and ensure the guest UI is accessible and low-pressure.
Build the state management system for advancing slides (Host -> Main Screen -> Guest Device).
Develop the large, high-contrast UI blocks for mobile answer submission.
Implement the dynamic question type selector (Multiple Choice, Poll, Word Cloud, Slide) and their corresponding front-end renders.
Deadline (April 15): Complete Use Case 2 and Use Case 9. Guests can submit untimed answers on their devices, and the system securely records the selection state without a countdown timer.
Week 3: Content Management & Media Handling (April 16 – April 22)
Goal: Allow the Host to easily build and customize their trivia deck.
Design the backend Host Dashboard and "Content Setup" phase UI.
Integrate cloud storage for dragging, dropping, and cropping media files.
Develop the "Question Library" database and the import functionality for pre-built templates.
Deadline (April 22): Complete Use Case 3 and Use Case 4. A host can import a curated question bank, edit placeholders, and attach cropped, cloud-hosted images/videos to specific questions.
Week 4: Live Controls & Leaderboard Logic (April 23 – April 29)
Goal: Give the Host absolute control over the room's pacing and positive reinforcement.
Develop the Host's live control view, including the "Next Slide" and "Pause/Hold" toggles.
Write the backend logic to instantly calculate scores based strictly on accuracy (ignoring speed).
Design the minimalist, top-performer leaderboard for the main screen and the private ranking message for guest devices.
Deadline (April 29): Complete Use Case 5 and Use Case 6. The host can trigger the "Storyteller Pause" to freeze the game state, and seamlessly transition into the top 3-5 minimalist leaderboard display.
Week 5: Audio Integration & Aesthetic QA (April 30 – May 6)
Goal: Finalize the sensory experience and ensure strict accessibility compliance.
Integrate the ambient audio player with auto-looping.
Wire the audio system to dip volume by 50% automatically when the "Storyteller Pause" is triggered.
Build the automated script/testing utility to check WCAG contrast ratios and font sizes for the visual templates.
Deadline (May 6): Complete Use Case 7 and Use Case 8. The ambient soundscape functions smoothly alongside game state changes, and the "Midnight Velvet" template is verified and pushed to production.
Week 6: End-to-End Integration (May 7 – May 13)
Goal: Connect all isolated modules into a single, cohesive user journey.
Conduct integration testing across the three primary views (Host Dashboard, Venue Display, Guest Mobile).
Optimize database query performance and real-time state synchronization to ensure zero latency during live play.
Deadline (May 13): A complete, bug-free playthrough of a 10-question event from lobby creation to final leaderboard.
Week 7: User Acceptance Testing (UAT) & Refinement (May 14 – May 20)
Goal: Validate the accessibility and friction points with actual target users.
Conduct a mock event with a small group, specifically including elderly users to test the QR code joining and untimed answer buttons.
Gather feedback on mobile UI contrast, button sizing, and audio volume levels.
Deadline (May 20): Implement all critical UI/UX tweaks based on the mock event feedback.
Week 8: Production Deployment & Launch (May 21 – May 27)
Goal: Finalize the infrastructure for public use.
Set up production servers, domain routing, and SSL certificates.
Perform final security checks on database read/write rules.
Deadline (May 27): Version 1.0 is live and ready for Event Hosts to create their first real-world events.
