<br>

# Part A — Design Report

This section defines the visual language, personality, and brand standards that govern every pixel across the QuizYa platform. Every page, component, and micro-interaction must be evaluated against these guidelines before implementation.

---

## A1. Visual Tone

QuizYa's visual tone is **"Quiet Elegance."** The platform should feel like a beautifully typeset invitation to a celebration — not a game show, not a corporate tool, and never a children's toy.

| Attribute | Description |
|-----------|-------------|
| **Personality** | Warm, confident, and unhurried. The interface communicates *"take your time — we're here to celebrate."* |
| **Mood** | Premium, calm, and joyful. Imagine soft ambient lighting at a well-planned reception. |
| **Energy level** | Medium-low. Deliberate and graceful — never frenetic, never loud. |
| **Design era** | Modern minimalist with timeless serif/sans-serif pairings. Avoids trendy maximalism, neon palettes, and gamer aesthetics. |
| **Photography style** | When stock imagery is used (e.g., marketing landing page), prefer warm, candid, slightly desaturated editorial photography of real celebrations — golden hour lighting, soft bokeh, genuine laughter. |

### Tone Do's and Don'ts

| ✅ Do | ❌ Don't |
|-------|---------|
| Use generous whitespace to let content breathe | Cram elements together to "fit more in" |
| Animate slowly and purposefully (≥ 200ms ease) | Use bounce-heavy, playful spring animations |
| Let typography carry the hierarchy | Rely on thick borders, heavy box shadows, or busy patterns |
| Use muted, warm color accents | Use saturated primary reds, blues, or neon greens |
| Design states (hover, focus, active) that feel like gentle weight shifts | Design states that flash, shake, or aggressively transform |

---

<br>

## A2. Color Palette

QuizYa uses a **warm-neutral base** with a signature deep-teal accent. The palette is intentionally restrained so that event-specific template themes (injected via `css_config`) become the visual star, while the platform chrome remains quietly supportive.

### Platform Chrome (Dashboard, Login, Landing Page)

| Role | Name | Hex | HSL | Usage |
|------|------|-----|-----|-------|
| **Background** | Snow | `#FAFAF9` | `40 20% 98%` | Page backgrounds, card surfaces |
| **Surface** | Warm White | `#F5F3EF` | `36 25% 95%` | Sidebar, card fills, modal overlays |
| **Surface Elevated** | Linen | `#EDEBE5` | `40 18% 91%` | Hover state backgrounds, active sidebar items |
| **Border** | Pebble | `#E0DDD6` | `40 14% 86%` | Subtle dividers, card borders, input outlines |
| **Text Primary** | Charcoal | `#1C1917` | `20 10% 10%` | Headings, primary body text |
| **Text Secondary** | Slate | `#57534E` | `20 5% 33%` | Captions, timestamps, helper text |
| **Text Tertiary** | Stone | `#A8A29E` | `20 4% 64%` | Placeholder text, disabled labels |
| **Accent Primary** | Deep Teal | `#0F766E` | `175 77% 26%` | Primary buttons, active states, links |
| **Accent Hover** | Teal Dark | `#0D6359` | `175 77% 22%` | Button hover states |
| **Accent Light** | Seafoam | `#CCFBF1` | `167 85% 89%` | Accent backgrounds, success badges |
| **Danger** | Rosewood | `#B91C1C` | `0 73% 42%` | Delete buttons, error states, danger zones |
| **Danger Light** | Blush | `#FEF2F2` | `0 80% 97%` | Danger zone backgrounds |
| **Warning** | Amber | `#D97706` | `38 90% 44%` | Warning banners, draft/lobby badges |
| **Success** | Forest | `#15803D` | `142 68% 30%` | Success toasts, live badges, correct answers |

### Status Badge Colors

| Status | Background | Text | Animation |
|--------|-----------|------|-----------|
| `DRAFT` | `#E7E5E4` (Stone 200) | `#57534E` | None |
| `LOBBY` | `#FEF3C7` (Amber 100) | `#92400E` | Subtle pulse (opacity 0.8 → 1.0, 2s) |
| `LIVE` | `#DCFCE7` (Green 100) | `#166534` | Gentle pulse (opacity 0.8 → 1.0, 1.5s) |
| `COMPLETED` | `#DBEAFE` (Blue 100) | `#1E40AF` | None |
| `ARCHIVED` | `#F1F5F9` (Slate 100) | `#64748B` | None |

### Contrast Compliance

All text-background combinations meet **WCAG 2.1 AA** (4.5:1 for normal text, 3:1 for large text). Template-specific palettes are validated independently via the Admin QA pipeline (see Section 19).

---

<br>

## A3. Copywriting


### Tone of Voice

QuizYa's copy voice is **warm, encouraging, and gently sophisticated.** It should feel like a thoughtful friend who happens to be very organized — never robotic, never condescending, never overly casual.

| Principle | Example ✅ | Anti-example ❌ |
|-----------|------------|-----------------|
| **Encouraging, not commanding** | *"Ready when you are"* | *"Click here to proceed"* |
| **Warm, not corporate** | *"Let's make it unforgettable"* | *"Optimize your event experience"* |
| **Clear, not clever** | *"Your event is live"* | *"You're ON 🔥🔥🔥"* |
| **Inclusive, not patronizing** | *"Designed for every generation"* | *"Even your grandma can use it!"* |
| **Calm, not urgent** | *"Take your time"* | *"Hurry! Time is running out!"* |

### Delivery Guidelines

- **Button labels:** 2–4 words, action-oriented. Use sentence case: *"Create event"*, *"Join game"*, *"Reveal answer"*.
- **Headings:** Sentence case. Prefer questions or soft imperatives: *"What would you like to create?"*
- **Empty states:** Always include a warm message + clear next step: *"No events yet — let's create your first one."*
- **Error messages:** Lead with what happened, then offer a solution. Never blame the user: *"That email is already registered. Try logging in instead?"*
- **Toasts/notifications:** Brief and positive: *"Event created successfully"*, *"Question imported"*.
- **Emoji usage:** Sparingly and only in guest-facing celebratory moments (confetti ✨, tier badges 🥇). Never in buttons, headings, or error messages within the dashboard.

---

<br>

## A4. Branding Guidelines

### Logo

- **Primary mark:** The wordmark **"QuizYa"** set in the heading typeface (Fraunces) at weight 600, with the "Q" in Deep Teal (`#0F766E`) and the remaining letters in Charcoal (`#1C1917`).
- **Alternate mark:** For small sizes (favicons, mobile header), use a standalone **"Q"** letterform in Deep Teal on a transparent background.
- **Clear space:** Maintain a padding equal to the height of the "Y" on all sides of the logo.
- **Minimum size:** The wordmark should never render below **20px height** in digital contexts.
- **Background rules:** The logo may appear on Snow (`#FAFAF9`), Warm White (`#F5F3EF`), or pure white (`#FFFFFF`). On dark backgrounds (for venue display templates), use the inverted variant: full wordmark in white.

### Typography

| Role | Typeface | Weight | Usage |
|------|----------|--------|-------|
| **Display / Logo** | [Fraunces](https://fonts.google.com/specimen/Fraunces) (Variable) | 500–700 | Landing page hero, event titles on venue display, logo wordmark |
| **Headings** | [Plus Jakarta Sans](https://fonts.google.com/specimen/Plus+Jakarta+Sans) | 600–700 | Section headings, card titles, navigation labels |
| **Body** | [Inter](https://fonts.google.com/specimen/Inter) | 400–500 | Body text, form labels, button labels, helper text |
| **Monospace** | [JetBrains Mono](https://fonts.google.com/specimen/JetBrains+Mono) | 400 | Join codes, code snippets, data values |

#### Type Scale

| Token | Size | Line Height | Usage |
|-------|------|-------------|-------|
| `--text-xs` | 12px | 1.5 | Timestamps, fine print |
| `--text-sm` | 14px | 1.5 | Captions, helper text |
| `--text-base` | 16px | 1.6 | Body text, form inputs |
| `--text-lg` | 18px | 1.55 | Answer block text (mobile) |
| `--text-xl` | 20px | 1.4 | Card titles, question prompts (mobile) |
| `--text-2xl` | 24px | 1.35 | Section headings |
| `--text-3xl` | 30px | 1.3 | Page titles |
| `--text-4xl` | 36px | 1.25 | Landing page subheadings |
| `--text-5xl` | 48px | 1.2 | Landing page hero, venue display question text |
| `--text-6xl` | 60px | 1.1 | Venue display event title, join code |

### Brand Values (Reflected in Design)

| Value | How It Manifests in the UI |
|-------|---------------------------|
| **Inclusive** | Elder-friendly touch targets (≥ 48px), high-contrast defaults, no rapid-fire timers, no flashing animations. |
| **Premium** | Restrained palette, generous whitespace, curated template library, sophisticated type pairings. |
| **Human-centered** | "Storyteller Pause" feature, warm copy voice, celebratory micro-animations (confetti, not explosions). |
| **Effortless** | One-scan QR join, pre-built question banks, drag-and-drop media with auto-formatting. |
| **Trustworthy** | Consistent visual language, predictable navigation, clear feedback on every action. |

### Iconography

- **Style:** Outlined (stroke-based), 1.5px stroke weight, rounded caps and joins.
- **Library:** [Lucide Icons](https://lucide.dev/) — consistent, MIT-licensed, and visually light.
- **Size:** 20×20px in navigation and cards, 24×24px in toolbars, 16×16px inline with text.
- **Color:** Icons inherit the color of their closest text element by default. Interactive icons use `Accent Primary` on hover.

---

<br>

## A5. Site Look and Feel

### Desired Aesthetic

QuizYa aims for a **"modern editorial"** aesthetic — the visual language of premium lifestyle magazines and invitation design studios, translated to a web application. The design should feel **curated, intentional, and respectful of the user's time and attention.**

#### Core Aesthetic Principles

1. **Intuitiveness and Predictability**  
   The layout is designed so users know immediately where to click, swipe, or press without needing instructions. Navigation patterns are conventional (sidebar for dashboard, centered card for auth, full-width for venue display). Interactive elements look interactive. Non-interactive elements look static. There is zero ambiguity.

2. **Minimalist Design (Whitespace)**  
   Effective use of empty space keeps the design from looking cluttered, making it easier to read and focus on key content. Every section has breathing room. Cards never touch each other. Content areas have generous padding (24–32px). The design should feel *spacious*, like a well-curated gallery — not a packed shelf.

3. **Visual Hierarchy Through Typography**  
   Size, weight, and color do the heavy lifting — not borders, backgrounds, or decorative elements. A user scanning any page should understand the information hierarchy in under 2 seconds.

4. **Calm Interactivity**  
   Hover states, transitions, and animations exist to confirm user actions and guide attention — never to entertain or distract. Every animation is eased, purposeful, and ≥ 200ms.

### Reference Aesthetics

#### Websites We Like (Aspirational)

| Website | Traits We Admire |
|---------|-----------------|
| [Linear.app](https://linear.app) | Clean sidebar navigation, minimal chrome, smooth transitions, keyboard-friendly, elegant dark/light modes. |
| [Notion.so](https://notion.so) | Generous whitespace, clear hierarchy, calm color palette, welcoming empty states. |
| [Stripe.com/docs](https://stripe.com/docs) | Impeccable typography, sophisticated layout system, gentle color coding, zero visual noise. |
| [Squarespace Templates](https://www.squarespace.com/templates) | Editorial-quality template gallery browsing, large preview imagery, premium feel. |
| [Lottie by Airbnb](https://airbnb.design/lottie/) | Tasteful micro-animations, warm illustration style, smooth scrolling interactions. |

#### Websites We Dislike (Anti-patterns)

| Website Type | Traits to Avoid |
|-------------|----------------|
| Traditional quiz apps (Kahoot, Quizizz) | Saturated primary colors, aggressive countdown timers, arcade/gamer aesthetic, childish sound effects. |
| Generic SaaS dashboards | Dense tables, tiny text, overwhelming toolbars, corporate blue-gray palettes, information overload. |
| Overdecorated event websites | Glitter effects, auto-playing music on page load, busy background patterns, Comic Sans/novelty typefaces. |
| Gamified education platforms | XP bars, achievement badges everywhere, leaderboards that shame, frenetic particle animations. |

### Spacing System

All spacing derives from a **4px base unit**:

| Token | Value | Usage |
|-------|-------|-------|
| `--space-1` | 4px | Inline icon padding, tight gaps |
| `--space-2` | 8px | Input internal padding, icon-to-text gap |
| `--space-3` | 12px | Small element margins |
| `--space-4` | 16px | Card internal padding (compact) |
| `--space-5` | 20px | Standard list item padding |
| `--space-6` | 24px | Card internal padding (standard), section gaps |
| `--space-8` | 32px | Large section padding, page margins |
| `--space-10` | 40px | Hero section vertical padding |
| `--space-12` | 48px | Section separators |
| `--space-16` | 64px | Page-level vertical rhythm |

### Border Radius System

| Token | Value | Usage |
|-------|-------|-------|
| `--radius-sm` | 4px | Small pills, tags |
| `--radius-md` | 8px | Buttons, inputs, cards |
| `--radius-lg` | 12px | Modals, large cards |
| `--radius-xl` | 16px | Image containers, hero sections |
| `--radius-full` | 9999px | Avatars, circular badges |

### Shadow System

| Token | Value | Usage |
|-------|-------|-------|
| `--shadow-xs` | `0 1px 2px rgba(0,0,0,0.04)` | Subtle card lift |
| `--shadow-sm` | `0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)` | Default card shadow |
| `--shadow-md` | `0 4px 6px rgba(0,0,0,0.05), 0 2px 4px rgba(0,0,0,0.04)` | Hovered cards, dropdowns |
| `--shadow-lg` | `0 10px 15px rgba(0,0,0,0.06), 0 4px 6px rgba(0,0,0,0.04)` | Modals, elevated panels |
| `--shadow-xl` | `0 20px 25px rgba(0,0,0,0.08), 0 8px 10px rgba(0,0,0,0.04)` | Fullscreen overlays |

---

<br>
<br>