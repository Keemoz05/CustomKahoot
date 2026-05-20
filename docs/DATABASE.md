# Database Schema — CustomKahoot (QuizYa)

> **RDBMS:** PostgreSQL  
> **ORM:** Spring Data JPA / Hibernate  
> **Migration tool (recommended):** Flyway or Liquibase  

---

## Table of Contents

| #  | Table                                                  | Purpose                                          |
|----|--------------------------------------------------------|--------------------------------------------------|
| 1  | [`users`](#1-users)                                    | Registered hosts, planners, admin accounts       |
| 2  | [`events`](#2-events)                                  | Core trivia event created by a host              |
| 3  | [`event_guests`](#3-event_guests)                      | Anonymous guest sessions joined via QR scan      |
| 4  | [`template_categories`](#4-template_categories)        | Groupings for aesthetic templates                |
| 5  | [`templates`](#5-templates)                            | Visual themes with CSS config                    |
| 6  | [`question_banks`](#6-question_banks)                  | Curated / host-custom question collections       |
| 7  | [`questions`](#7-questions)                            | Individual slides / prompts                      |
| 8  | [`question_options`](#8-question_options)              | Answer choices (up to 4 per question)            |
| 9  | [`guest_answers`](#9-guest_answers)                    | Every guest submission for scoring               |
| 10 | [`leaderboard_snapshots`](#10-leaderboard_snapshots)   | Point-in-time rankings after each reveal         |
| 11 | [`media_assets`](#11-media_assets)                     | Cloudinary-managed images and videos             |
| 12 | [`audio_tracks`](#12-audio_tracks)                     | Ambient soundscape library                       |
| 13 | [`event_audio`](#13-event_audio)                       | Per-event audio playlist config                  |
| 14 | [`event_sessions`](#14-event_sessions)                 | Live play-through / rehearsal tracking           |
|    | [Design Analysis](#design-analysis)                    | Rationale behind every major design decision     |

---

<br>

## 1. `users`

> Registered hosts, event planners, and admin accounts.

| Field Name      | Data Type       | Constraint                | Description                                                                                     |
|-----------------|-----------------|---------------------------|-------------------------------------------------------------------------------------------------|
| `id`            | `BIGINT`        | `PK` · `AUTO_INCREMENT`  | Unique identifier for the user.                                                                 |
| `email`         | `VARCHAR(255)`  | `UNIQUE` · `NOT NULL`    | Login credential and primary contact for the host.                                              |
| `password_hash` | `VARCHAR(255)`  | `NOT NULL`                | BCrypt-hashed password stored by Spring Security.                                               |
| `display_name`  | `VARCHAR(100)`  | `NOT NULL`                | Shown on the Host Dashboard and in event metadata.                                              |
| `role`          | `VARCHAR(20)`   | `NOT NULL` · `DEFAULT 'HOST'` | Authorization role — `HOST`, `PLANNER`, `ADMIN`. Enables future B2B event-planner features. |
                                                    |
| `is_active`     | `BOOLEAN`       | `NOT NULL` · `DEFAULT TRUE` | Soft-delete flag; deactivated accounts retain data for audit.                                |
| `created_at`    | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()` | Account creation timestamp for analytics and support.                                       |
| `updated_at`    | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()` | Tracks the last profile modification.                                                       |

---

<br>

## 2. `events`

> A single trivia event created by a host (e.g. *"Sarah & Tom's Wedding Trivia Night"*).

| Field Name               | Data Type       | Constraint                         | Description                                                                                          |
|--------------------------|-----------------|-------------------------------------|------------------------------------------------------------------------------------------------------|
| `id`                     | `BIGINT`        | `PK` · `AUTO_INCREMENT`            | Unique event identifier.                                                                             |
| `host_id`                | `BIGINT`        | `FK → users.id` · `NOT NULL`       | The user who created and controls this event.                                                        |
| `template_id`            | `BIGINT`        | `FK → templates.id` · `NULLABLE`   | Visual aesthetic template applied (e.g. *"Midnight Velvet"*, *"Rustic Reunion"*).                    |
| `title`                  | `VARCHAR(200)`  | `NOT NULL`                          | Event display title shown on the venue main screen.                                                  |
| `description`            | `TEXT`          | `NULLABLE`                          | Optional event description or subtitle displayed in the lobby.                                       |
| `join_code`              | `VARCHAR(10)`   | `UNIQUE` · `NOT NULL`              | Short alphanumeric code embedded in the QR code for guest joining.                                   |
| `qr_code_url`            | `TEXT`          | `NULLABLE`                          | Stored URL or data URI of the generated QR code image.                                               |
| `status`                 | `VARCHAR(20)`   | `NOT NULL` · `DEFAULT 'DRAFT'`     | Lifecycle state — `DRAFT` · `LOBBY` · `LIVE` · `PAUSED` · `COMPLETED` · `ARCHIVED`.                 |
| `current_question_index` | `INT`           | `NOT NULL` · `DEFAULT 0`           | Tracks which question the host is currently presenting. Drives WebSocket state sync.                 |
| `max_guests`             | `INT`           | `NOT NULL` · `DEFAULT 200`         | Maximum concurrent guest connections (per the 200-device requirement).                               |
                             |
                                                      |
| `started_at`             | `TIMESTAMPTZ`   | `NULLABLE`                          | Actual timestamp when the host pressed "Start".                                                      |
| `ended_at`               | `TIMESTAMPTZ`   | `NULLABLE`                          | Timestamp when the event completed or was manually ended.                                            |
| `created_at`             | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`       | When the event was first created.                                                                    |
| `updated_at`             | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`       | Last modification timestamp.                                                                         |

---

<br>

## 3. `event_guests`

> Anonymous guest sessions — no account required. They join via QR scan and type a display name.

| Field Name      | Data Type       | Constraint                       | Description                                                                                  |
|-----------------|-----------------|----------------------------------|----------------------------------------------------------------------------------------------|
| `id`            | `BIGINT`        | `PK` · `AUTO_INCREMENT`         | Unique guest-session identifier.                                                             |
| `event_id`      | `BIGINT`        | `FK → events.id` · `NOT NULL`   | The event this guest joined.                                                                 |
| `display_name`  | `VARCHAR(50)`   | `NOT NULL`                       | Name the guest entered on the mobile landing page. Shown on lobby and leaderboard.           |
| `session_token` | `VARCHAR(255)`  | `UNIQUE` · `NOT NULL`           | Random token to identify the guest's browser session (no login required).                    |                            |
| `correct_count` | `INT`           | `NOT NULL` · `DEFAULT 0`        | Number of correctly answered questions. Used for final percentage calculation.                |
| `is_connected`  | `BOOLEAN`       | `NOT NULL` · `DEFAULT TRUE`     | WebSocket connection status. Lets the host see who is still active.                          |
| `joined_at`     | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`    | When the guest entered the lobby. Used for ordering and analytics.                           |

---

<br>

## 4. `template_categories`

> Groups aesthetic templates by event type — Wedding, Reunion, Anniversary, etc.

| Field Name    | Data Type       | Constraint                       | Description                                                        |
|---------------|-----------------|----------------------------------|--------------------------------------------------------------------|
| `id`          | `BIGINT`        | `PK` · `AUTO_INCREMENT`         | Unique category identifier.                                        |
| `name`        | `VARCHAR(100)`  | `UNIQUE` · `NOT NULL`           | Category name (e.g. *"Wedding"*, *"Birthday"*, *"Corporate"*).     |
| `description` | `TEXT`          | `NULLABLE`                       | Brief description for the host's template browser.                 |
| `icon_url`    | `VARCHAR(500)`  | `NULLABLE`                       | Cloudinary URL for the category icon / thumbnail.                  |
| `sort_order`  | `INT`           | `NOT NULL` · `DEFAULT 0`        | Controls display order in the template gallery.                    |
| `created_at`  | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`    | Record creation timestamp.                                         |

---

<br>

## 5. `templates`

> 50+ professionally designed visual themes (e.g. *"Minimalist Wedding"*, *"Golden Anniversary"*, *"Midnight Velvet"*).

| Field Name          | Data Type       | Constraint                                  | Description                                                                                        |
|---------------------|-----------------|---------------------------------------------|----------------------------------------------------------------------------------------------------|
| `id`                | `BIGINT`        | `PK` · `AUTO_INCREMENT`                    | Unique template identifier.                                                                        |
| `category_id`       | `BIGINT`        | `FK → template_categories.id` · `NULLABLE` | Which event-type category this template belongs to.                                                |
| `name`              | `VARCHAR(150)`  | `UNIQUE` · `NOT NULL`                      | Template display name shown in the host's gallery.                                                 |
| `description`       | `TEXT`          | `NULLABLE`                                  | Marketing copy describing the template's vibe and best-fit events.                                 |
| `preview_image_url` | `VARCHAR(500)`  | `NOT NULL`                                  | Cloudinary URL for the template's thumbnail preview.                                               |
| `css_config`        | `JSONB`         | `NOT NULL`                                  | JSON object — color palette, font families, border radii, CSS custom properties. Rendered by Thymeleaf. |
| `is_premium`        | `BOOLEAN`       | `NOT NULL` · `DEFAULT FALSE`               | Gate for future monetization. Premium templates may require a paid plan.                            |
| `is_active`         | `BOOLEAN`       | `NOT NULL` · `DEFAULT TRUE`                | Soft-delete / disable flag for removing templates without data loss.                               |
| `created_at`        | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`               | When the template was added to the system.                                                         |
| `updated_at`        | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`               | Last modification timestamp.                                                                       |

---

<br>

## 6. `question_banks`

> Pre-built or host-created collections of questions (e.g. *"The Newlywed Game"*, *"Family Folklore"*).

| Field Name       | Data Type       | Constraint                                 | Description                                                                                                |
|------------------|-----------------|--------------------------------------------|------------------------------------------------------------------------------------------------------------|
| `id`             | `BIGINT`        | `PK` · `AUTO_INCREMENT`                   | Unique bank identifier.                                                                                    |
| `event_id`       | `BIGINT`        | `FK → events.id` · `NULLABLE`             | If linked to a specific event, this is a custom copy. `NULL` = system-wide curated bank.                   |
| `source_bank_id` | `BIGINT`        | `FK → question_banks.id` · `NULLABLE`     | Self-referencing FK. If cloned from a curated template, points to the original for analytics.              |
| `title`          | `VARCHAR(200)`  | `NOT NULL`                                 | Bank title displayed in the import dialog (e.g. *"How Well Do You Know the Bride?"*).                      |
| `description`    | `TEXT`          | `NULLABLE`                                 | Brief summary to help the host decide whether to import this bank.                                         |
| `is_curated`     | `BOOLEAN`       | `NOT NULL` · `DEFAULT FALSE`              | `TRUE` = system-provided template bank. `FALSE` = host-created custom bank.                                |
| `created_by`     | `BIGINT`        | `FK → users.id` · `NULLABLE`              | The host who created this bank. `NULL` for system-curated banks.                                           |
| `created_at`     | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`              | Record creation timestamp.                                                                                 |
| `updated_at`     | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`              | Last modification timestamp.                                                                               |

---

<br>

## 7. `questions`

> Individual prompts within a question bank — each rendered as one "slide" during live play.

| Field Name           | Data Type       | Constraint                              | Description                                                                                           |
|----------------------|-----------------|------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `id`                 | `BIGINT`        | `PK` · `AUTO_INCREMENT`                 | Unique question identifier.                                                                           |
| `bank_id`            | `BIGINT`        | `FK → question_banks.id` · `NOT NULL`   | Which question bank this question belongs to.                                                         |
| `question_type`      | `VARCHAR(20)`   | `NOT NULL`                               | Format — `MULTIPLE_CHOICE` · `POLL` · `WORD_CLOUD` · `SLIDE` (non-interactive info slide).            |
| `prompt_text`        | `TEXT`          | `NOT NULL`                               | The question or prompt displayed on both the venue screen and guest devices.                           |
| `media_asset_id`     | `BIGINT`        | `FK → media_assets.id` · `NULLABLE`     | Optional attached image / video (nostalgia media) displayed alongside the prompt.                     |
| `sort_order`         | `INT`           | `NOT NULL`                               | Display order within the bank. The host can drag to reorder.                                          |
     |
             |
| `explanation_text`   | `TEXT`          | `NULLABLE`                               | Shown after the answer is revealed — perfect for the "Storyteller Pause" moment.                      |
| `created_at`         | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`            | Record creation timestamp.                                                                            |
| `updated_at`         | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`            | Last modification timestamp.                                                                          |

---

<br>

## 8. `question_options`

> Answer choices for `MULTIPLE_CHOICE` and `POLL` type questions — up to 4 large, high-contrast blocks.

| Field Name    | Data Type       | Constraint                            | Description                                                                                     |
|---------------|-----------------|---------------------------------------|-------------------------------------------------------------------------------------------------|
| `id`          | `BIGINT`        | `PK` · `AUTO_INCREMENT`              | Unique option identifier.                                                                       |
| `question_id` | `BIGINT`        | `FK → questions.id` · `NOT NULL`     | Parent question.                                                                                |
| `option_text` | `VARCHAR(300)`  | `NOT NULL`                            | Text displayed inside the high-contrast answer block on the guest's device.                     |
| `is_correct`  | `BOOLEAN`       | `NOT NULL` · `DEFAULT FALSE`         | Marks the correct answer(s). For `POLL` types, all options are `FALSE` (no right answer).       |
| `sort_order`  | `INT`           | `NOT NULL`                            | Display position (A, B, C, D). Controls the layout of the tap blocks.                           |
| `color_hex`   | `VARCHAR(7)`    | `NULLABLE`                            | Optional custom color override for this option block. Falls back to template defaults if `NULL`. |

---

<br>

## 9. `guest_answers`

> Every answer submission from every guest. The core data for scoring and analytics.

| Field Name           | Data Type       | Constraint                                  | Description                                                                                     |
|----------------------|-----------------|---------------------------------------------|-------------------------------------------------------------------------------------------------|
| `id`                 | `BIGINT`        | `PK` · `AUTO_INCREMENT`                    | Unique answer record identifier.                                                                |
| `event_id`           | `BIGINT`        | `FK → events.id` · `NOT NULL`              | Denormalized for fast leaderboard queries without joining through question_banks.                |
| `guest_id`           | `BIGINT`        | `FK → event_guests.id` · `NOT NULL`        | The guest who submitted this answer.                                                            |
| `question_id`        | `BIGINT`        | `FK → questions.id` · `NOT NULL`           | The question being answered.                                                                    |
| `selected_option_id` | `BIGINT`        | `FK → question_options.id` · `NULLABLE`    | The option the guest tapped. `NULL` for `WORD_CLOUD` type answers.                              |
| `word_cloud_text`    | `VARCHAR(100)`  | `NULLABLE`                                  | Free-text input for `WORD_CLOUD` question types.                                                |
| `is_correct`         | `BOOLEAN`       | `NOT NULL` · `DEFAULT FALSE`               | Pre-computed correctness flag. Avoids recalculating during live leaderboard updates.             |
| `points_awarded`     | `INT`           | `NOT NULL` · `DEFAULT 0`                   | Actual points given (may differ from `questions.points_value` for future partial-credit modes).  |
| `submitted_at`       | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`               | Exact submission timestamp. Recorded even in untimed mode for analytics.                        |

> **Unique Constraint:** `(guest_id, question_id)` — a guest can only answer each question once.

---

<br>

## 10. `leaderboard_snapshots`

> Point-in-time leaderboard captures after each question reveal, enabling historical playback.

| Field Name         | Data Type     | Constraint                            | Description                                                              |
|--------------------|---------------|---------------------------------------|--------------------------------------------------------------------------|
| `id`               | `BIGINT`      | `PK` · `AUTO_INCREMENT`              | Unique snapshot record identifier.                                       |
| `event_id`         | `BIGINT`      | `FK → events.id` · `NOT NULL`        | The event this snapshot belongs to.                                      |
| `question_index`   | `INT`         | `NOT NULL`                            | Which question had just been revealed when this snapshot was captured.    |
| `guest_id`         | `BIGINT`      | `FK → event_guests.id` · `NOT NULL`  | The guest in this ranking row.                                           |
| `rank`             | `INT`         | `NOT NULL`                            | The guest's position at this point in time.                              |
                               |
| `captured_at`      | `TIMESTAMPTZ` | `NOT NULL` · `DEFAULT NOW()`         | When this snapshot was recorded.                                         |

> **Unique Constraint:** `(event_id, question_index, guest_id)` — one entry per guest per question per event.

---

<br>

## 11. `media_assets`

> Uploaded images and videos managed via Cloudinary — nostalgic family photos, event media.

| Field Name              | Data Type       | Constraint                      | Description                                                                        |
|-------------------------|-----------------|---------------------------------|------------------------------------------------------------------------------------|
| `id`                    | `BIGINT`        | `PK` · `AUTO_INCREMENT`        | Unique media asset identifier.                                                     |
| `uploaded_by`           | `BIGINT`        | `FK → users.id` · `NOT NULL`   | The host who uploaded this asset.                                                  |
| `cloudinary_public_id`  | `VARCHAR(255)`  | `UNIQUE` · `NOT NULL`          | Cloudinary's unique reference ID; constructs transformation URLs on the fly.       |
| `original_url`          | `VARCHAR(500)`  | `NOT NULL`                      | Full Cloudinary URL to the original unprocessed file.                              |
| `transformed_url`       | `VARCHAR(500)`  | `NULLABLE`                      | Cloudinary URL with auto-crop / face-center transformations applied.               |
| `media_type`            | `VARCHAR(10)`   | `NOT NULL`                      | `IMAGE` or `VIDEO`. Determines how Thymeleaf renders the asset.                   |
| `file_size_bytes`       | `BIGINT`        | `NULLABLE`                      | Original file size for upload quota tracking.                                      |
| `alt_text`              | `VARCHAR(300)`  | `NULLABLE`                      | Accessibility alt-text for screen readers (WCAG compliance).                       |
| `uploaded_at`           | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`   | Upload timestamp.                                                                  |

---

<br>

## 12. `audio_tracks`

> Library of ambient soundscape options — acoustic guitar, soft jazz, etc.

| Field Name         | Data Type       | Constraint                       | Description                                                                   |
|--------------------|-----------------|----------------------------------|-------------------------------------------------------------------------------|
| `id`               | `BIGINT`        | `PK` · `AUTO_INCREMENT`         | Unique track identifier.                                                      |
| `title`            | `VARCHAR(150)`  | `NOT NULL`                       | Track name in the host's audio picker (e.g. *"Soft Jazz Lounge"*).            |
| `cdn_url`          | `VARCHAR(500)`  | `NOT NULL`                       | CDN-hosted URL for the audio file. Streamed via HTML5 Web Audio API.          |
| `duration_seconds` | `INT`           | `NULLABLE`                       | Track length. Used to configure seamless auto-loop points.                    |
| `genre`            | `VARCHAR(50)`   | `NULLABLE`                       | Tag for filtering (e.g. *"Acoustic"*, *"Jazz"*, *"Classical"*).               |
| `is_active`        | `BOOLEAN`       | `NOT NULL` · `DEFAULT TRUE`     | Soft-delete flag.                                                             |
| `created_at`       | `TIMESTAMPTZ`   | `NOT NULL` · `DEFAULT NOW()`    | Record creation timestamp.                                                    |

---

<br>

## 13. `event_audio`

> Many-to-many join between events and audio tracks, with per-event configuration.

| Field Name       | Data Type | Constraint                            | Description                                                                     |
|------------------|-----------|---------------------------------------|---------------------------------------------------------------------------------|
| `id`             | `BIGINT`  | `PK` · `AUTO_INCREMENT`              | Unique record identifier.                                                       |
| `event_id`       | `BIGINT`  | `FK → events.id` · `NOT NULL`        | The event using this track.                                                     |
| `audio_track_id` | `BIGINT`  | `FK → audio_tracks.id` · `NOT NULL`  | The selected audio track.                                                       |
| `volume_percent` | `INT`     | `NOT NULL` · `DEFAULT 75`            | Default playback volume (0–100). Auto-dips by 50% during Storyteller Pause.     |
| `play_order`     | `INT`     | `NOT NULL` · `DEFAULT 0`             | Order in a playlist if multiple tracks are selected.                            |
| `is_looping`     | `BOOLEAN` | `NOT NULL` · `DEFAULT TRUE`          | Whether this track auto-loops.                                                  |

> **Unique Constraint:** `(event_id, audio_track_id)` — prevent duplicate track assignments.

---

<br>

## 14. `event_sessions`

> Captures each live play-through session for a given event (an event could be rehearsed or replayed).

| Field Name               | Data Type     | Constraint                       | Description                                                                                   |
|--------------------------|---------------|----------------------------------|-----------------------------------------------------------------------------------------------|
| `id`                     | `BIGINT`      | `PK` · `AUTO_INCREMENT`         | Unique session identifier.                                                                    |
| `event_id`               | `BIGINT`      | `FK → events.id` · `NOT NULL`   | Parent event.                                                                                 |
| `session_number`         | `INT`         | `NOT NULL` · `DEFAULT 1`        | Ordinal session count (1 = first run, 2 = replay, etc.).                                      |
| `peak_guest_count`       | `INT`         | `NULLABLE`                       | Maximum simultaneous guests during this session. Useful for venue analytics.                   |
| `total_questions_played` | `INT`         | `NULLABLE`                       | How many questions were actually played (may differ from bank size if host ended early).       |
| `started_at`             | `TIMESTAMPTZ` | `NOT NULL`                       | Session start time.                                                                           |
| `ended_at`               | `TIMESTAMPTZ` | `NULLABLE`                       | Session end time. `NULL` = still in progress.                                                 |

---

<br>

## Entity-Relationship Overview

```
users ──1:N──▶ events
users ──1:N──▶ question_banks        (created_by)
users ──1:N──▶ media_assets          (uploaded_by)

events ──1:N──▶ event_guests
events ──1:N──▶ guest_answers        (denormalized)
events ──1:N──▶ leaderboard_snapshots
events ──1:N──▶ event_sessions
events ──N:1──▶ templates
events ──M:N──▶ audio_tracks         (via event_audio)

question_banks ──1:N──▶ questions
question_banks ──N:1──▶ events       (event-specific copy)
question_banks ──self──▶ question_banks (source_bank_id)

questions ──1:N──▶ question_options
questions ──N:1──▶ media_assets

guest_answers ──N:1──▶ event_guests
guest_answers ──N:1──▶ questions
guest_answers ──N:1──▶ question_options

template_categories ──1:N──▶ templates
```

---

<br>

## Design Analysis

| Design Choice                                        | Reason                                                                                                                                                                                                                                                                                   |
|------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **PostgreSQL over MongoDB**                          | Data is deeply relational (`Host → Event → Bank → Question → Option → Answer `). PostgreSQL enforces referential integrity at the DB level, preventing orphaned answers or corrupted leaderboards — critical during live events.                                                   |
| **`BIGINT` PKs instead of `UUID`**                   | More storage-efficient, faster B-tree index lookups, and naturally sortable by creation order. UUIDs can be added later as public-facing `external_id` columns if the API needs non-guessable identifiers.                                                                               |
| **`TIMESTAMPTZ` for all timestamps**                 | Events span time zones (destination weddings, corporate gatherings). `TIMESTAMPTZ` stores the absolute point in time and lets the app layer convert to the user's local zone — avoids subtle bugs with `TIMESTAMP WITHOUT TIME ZONE`.                                                     |
| **`JSONB` for template CSS config**                  | Template styling is semi-structured and varies per template. `JSONB` avoids a rigid EAV anti-pattern while still supporting indexed queries (GIN index) on specific properties like font family.                                                                                         |
| **Separate `template_categories` table**             | Normalizing categories lets admins add new event types (e.g. *"Baby Shower"*) without altering the template schema. Supports the 50+ template catalog promised in the docs.                                                                                                             |
| **`question_banks.source_bank_id` self-reference**   | When a host imports a curated bank, the system clones it. `source_bank_id` tracks lineage so analytics can report "most imported banks" without host edits mutating the shared template.                                                                                                 |
| **`question_banks.event_id` nullable**               | `NULL` = system-wide curated template. Non-NULL = private copy attached to a specific event. Avoids duplicating the entire question / option schema into separate "template" and "instance" tables.                                                                                      |
| **`questions.time_limit_seconds` nullable**           | `NULL` = untimed (the default elder-friendly UX). Allows future per-question timer overrides without schema changes.                                                                                                                                                                     |
| **`guest_answers.event_id` denormalized**            | Technically derivable via `question → bank → event`. Denormalized because the leaderboard query (`SUM(points_awarded) WHERE event_id = ? GROUP BY guest_id`) runs on *every* question reveal for up to 200 guests — eliminates a 3-table join on the hottest query path.                 |
| **`guest_answers.is_correct` pre-computed**          | Storing the boolean at write-time avoids re-joining `question_options.is_correct` during live scoring. Answers are immutable once submitted, so there is no risk of staleness.                                                                                                            |
| **Separate `leaderboard_snapshots` table**           | Enables "leaderboard replay" for post-event recaps and social sharing. Without snapshots, the leaderboard only reflects the final state, losing the dramatic progression of a close game.                                                                                                |
| **`event_guests` with `session_token`**              | Guests join via QR code with zero friction — no sign-up, no download. A random session token authenticates the WebSocket connection, matching the doc's elder-friendly onboarding requirement.                                                                                            |
| **`events.scoring_mode` column**                     | Currently only `ACCURACY` (no speed bonus, per docs). Future-proofs for an optional `SPEED_BONUS` mode for younger / competitive audiences without a schema migration.                                                                                                                   |
| **`events.show_leaderboard_count` column**           | Docs specify "top 3 to 5 players" to prevent embarrassment. Making this configurable per event respects host preference rather than hard-coding a constant.                                                                                                                              |
| **`events.status` as `VARCHAR` enum**                | Using `VARCHAR` instead of PostgreSQL `ENUM` because adding new states requires zero DDL migration. PostgreSQL `ENUM` types require `ALTER TYPE` which cannot run inside a transaction in older versions.                                                                                 |
| **`media_assets` separate from `questions`**         | A single media asset can be reused across multiple questions / events (e.g. a family photo). Separate table avoids duplicate Cloudinary URLs and makes storage quota tracking straightforward.                                                                                            |
| **`event_audio` join table**                         | Hosts may select multiple ambient tracks (playlist). A join table with `volume_percent`, `play_order`, and `is_looping` encapsulates per-event audio config without polluting `events` or `audio_tracks`.                                                                                |
| **`event_sessions` table**                           | Supports rehearsals (hosts testing before the real event) and replays (same trivia at a different table). Decouples "event content" from "event execution" for cleaner analytics.                                                                                                         |
| **Soft-delete (`is_active`) over hard-delete**       | Used on `users`, `templates`, and `audio_tracks`. Preserves referential integrity (no cascade-delete surprises) and supports audit trails. Actual removal can be done by a scheduled cleanup job after a retention period.                                                                |
| **`question_options.color_hex` nullable**            | Per-option color overrides while defaulting to the template palette. Supports accessibility customization without adding complexity for hosts who accept defaults.                                                                                                                        |
| **`questions.explanation_text` column**              | Directly supports the "Storyteller Pause" use case from ACTORS.md — after revealing an answer, the host freezes the game and this text provides the backstory.                                                                                                                           |
| **No `accounts` table for guests**                   | Guests are ephemeral, event-scoped entities. Full user accounts would violate the zero-friction joining requirement. `event_guests` provides just enough identity (display name + session token) for one event.                                                                           |
| **`events.max_guests` configurable**                 | Defaults to 200 (per WebSocket scalability doc), but allows smaller limits for intimate events or larger if infrastructure is scaled via Redis broker. Prevents unbounded connections from crashing the server.                                                                            |
