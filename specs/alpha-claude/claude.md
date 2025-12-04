# Builder Syndicate Mockups - Claude Guide

## Design System Overview

This project uses a **premium purple-themed design system** matching modern SaaS aesthetics. The design prioritizes depth, polish, and smooth interactions over flat minimalism.

### Core Philosophy
- **Premium over Simple**: Use gradients, shadows, and effects to create depth
- **Purple Theme**: Primary color is `#8b5cf6` with gradient variations
- **Glassmorphism**: Nav bar uses frosted glass effect with backdrop blur
- **Smooth Animations**: All interactive elements have hover states with transforms
- **Light Mode Only**: Clean white backgrounds with subtle ambient gradients

## CSS Architecture

### Variable Naming Conventions

**Spacing System** (use `--sp-X` for new code):
```css
--sp-1: 4px    /* Tight spacing */
--sp-2: 8px    /* Small gaps */
--sp-3: 12px   /* Default gap */
--sp-4: 16px   /* Medium padding */
--sp-5: 20px   /* Large padding */
--sp-6: 24px   /* Section spacing */
--sp-8: 32px   /* Page padding */
--sp-10: 40px  /* Large sections */
--sp-12: 48px  /* Extra large */
--sp-16: 64px  /* Hero spacing */
```

**Backwards Compatibility**: Old `--space-X` variables are aliased to `--sp-X` for legacy mockups.

**Color Gradients** (key feature of premium design):
```css
--gradient-primary: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%);
--gradient-gold: linear-gradient(135deg, #f59e0b 0%, #fbbf24 50%, #fcd34d 100%);
--gradient-fire: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
--gradient-cool: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
```

**Shadow System** (creates depth):
```css
--shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.08), 0 1px 2px rgba(0, 0, 0, 0.04);
--shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04);
--shadow-lg: 0 12px 40px rgba(0, 0, 0, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
--shadow-glow: 0 0 30px var(--color-primary-glow);
```

**Border Radius**:
```css
--radius-sm: 6px    /* Tags, small elements */
--radius-md: 10px   /* Buttons, inputs */
--radius-lg: 14px   /* Cards, containers */
--radius-xl: 20px   /* Hero sections */
--radius-full: 9999px /* Pills, avatars */
```

## Component Patterns

### Top Navigation (Glassmorphism)

**Required Structure**:
```html
<nav class="top-nav">
  <a href="feed.html" class="nav-brand">
    <div class="nav-brand-icon">⚡</div>
    <span>Builder Syndicate</span>
  </a>

  <div class="nav-spacer"></div>

  <div class="nav-search">
    <span class="nav-search-icon">🔍</span>
    <input type="text" placeholder="Search posts, tags, authors..." />
  </div>

  <div class="nav-spacer"></div>

  <div class="nav-actions">
    <button class="btn-icon" title="Notifications">🔔</button>
    <div class="nav-user">
      <div class="nav-user-avatar">CO</div>
      <div class="nav-user-info">
        <span class="nav-user-name">C. Olleague</span>
        <span class="nav-user-points">⚡ 2,847 pts</span>
      </div>
    </div>
  </div>
</nav>
```

**Key Features**:
- Gradient brand icon with shadow + glow
- Glassmorphism: `backdrop-filter: blur(20px) saturate(180%)`
- User points displayed in nav (gold color)
- Search icon positioned absolutely inside input

### Post Cards (Hover Effects)

**Structure**:
```html
<article class="post-card">  <!-- or .post-card.pinned or .post-card.golden -->
  <div class="post-vote">
    <button class="vote-btn">▲</button>  <!-- Use ▲ not ⬆ -->
    <span class="vote-count">142</span>
  </div>
  <div class="post-body">
    <div class="post-badges">
      <span class="badge badge-pinned">📌 Pinned</span>
    </div>
    <h2 class="post-title">
      <a href="post-detail.html">Post Title Here</a>
    </h2>
    <a href="https://example.com" class="post-link" target="_blank">
      <span class="post-link-icon">🔗</span>
      <span>example.com</span>
      <span>↗</span>
    </a>
    <div class="post-meta">
      <a href="#" class="post-author">@username</a>
      <span class="post-meta-sep">•</span>
      <span>3 hours ago</span>
      <span class="post-meta-sep">•</span>
      <div class="post-tags">
        <a href="#" class="tag">databases</a>
        <a href="#" class="tag">scaling</a>
      </div>
    </div>
    <div class="post-summary">
      AI-generated summary text...
    </div>
    <div class="post-actions">
      <span class="post-action">💬 34 comments</span>
      <span class="post-action">🔗 Share</span>
      <span class="post-action active">⭐ Following</span>
    </div>
  </div>
</article>
```

**Special Effects**:
- **Pinned Posts**: Add gradient top border with `.post-card.pinned::before`
- **Golden Posts**: Add radial gradient overlay with `.post-card.golden::after`
- **Hover State**: Cards lift with `transform: translateY(-2px)` and increase shadow
- **Vote Button Active**: Uses `--gradient-fire` with glow effect

### Buttons (Gradient Primary)

**Types**:
```html
<!-- Primary (gradient with glow) -->
<button class="btn btn-primary btn-lg">✨ New Post</button>

<!-- Ghost/Outline -->
<button class="btn btn-ghost">Load More</button>
<button class="btn btn-outline">Cancel</button>

<!-- Icon Button -->
<button class="btn-icon" title="Notifications">🔔</button>

<!-- FAB (Floating Action Button) -->
<button class="fab" title="Create">+</button>
```

**Hover Behaviors**:
- Primary: `transform: translateY(-1px)` + increased glow
- Ghost/Outline: background changes, border becomes accent color
- FAB: `transform: scale(1.08) translateY(-2px)` + large glow

### Tabs (Active Gradient)

```html
<div class="feed-tabs">
  <div class="feed-tab active">
    <span class="feed-tab-icon">🔥</span>
    <span>Hot</span>
  </div>
  <div class="feed-tab">
    <span class="feed-tab-icon">🆕</span>
    <span>New</span>
  </div>
</div>
```

**Active State**: Uses `--gradient-primary` background with white text

### AI Summary Box

```html
<div class="post-summary">
  Content here...
</div>
```

**Styling**:
- Purple gradient background: `linear-gradient(135deg, rgba(139, 92, 246, 0.06) 0%, rgba(217, 70, 239, 0.03) 100%)`
- Purple border: `1px solid rgba(139, 92, 246, 0.12)`
- Adds "✨ AI Summary" label via `::before` pseudo-element

### Form Inputs

**Standard Input**:
```html
<input type="text" class="form-input" placeholder="..." />
```

**Textarea**:
```html
<textarea class="form-textarea" rows="4" placeholder="..."></textarea>
```

**Focus State**:
- Border becomes `--color-primary`
- Adds purple glow: `box-shadow: 0 0 0 3px var(--color-primary-glow)`
- Background changes from elevated to card

## File Structure

```
mockups/
├── styles.css                    # Main stylesheet (premium design system)
├── feed.html                     # M1: Main feed page
├── login.html                    # M1: Login screen
├── post-detail.html              # M1: Post with comments
├── create-post.html              # M2: Link post creation
├── edit-post.html                # M2: Post editing with history
├── create-markdown-post.html     # M2: Markdown post creation
├── profile.html                  # M3: User profile (TODO)
├── search.html                   # M3: Search results (TODO)
└── admin.html                    # M3: Admin dashboard (TODO)
```

## HTML Boilerplate

**Always include** in `<head>`:
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
<link rel="stylesheet" href="styles.css">
```

**Main content wrapper**:
```html
<main class="main-content">
  <!-- Max-width 800px, centered, responsive padding -->
</main>
```

## Design Tokens Reference

### Colors

**Primary Purple Palette**:
- `--color-primary: #8b5cf6` (main purple)
- `--color-primary-dark: #7c3aed` (hover state)
- `--color-primary-light: #a78bfa` (lighter variant)
- `--color-primary-glow: rgba(139, 92, 246, 0.15)` (glow effects)

**Accent Colors**:
- `--color-accent-gold: #d97706` (badges, highlights)
- `--color-accent-fire: #ea580c` (upvotes, hot items)
- `--color-accent-cyan: #0891b2` (user avatars, cool elements)
- `--color-success: #059669` (success states)
- `--color-error: #dc2626` (delete, errors)

**Text Hierarchy**:
- `--color-text-primary: #0f172a` (headings, body text)
- `--color-text-secondary: #475569` (metadata, labels)
- `--color-text-tertiary: #64748b` (subtle text)
- `--color-text-muted: #94a3b8` (placeholders, disabled)

**Backgrounds**:
- `--color-bg-deep: #f8fafc` (page background)
- `--color-bg-card: #ffffff` (cards, elevated surfaces)
- `--color-bg-elevated: #f1f5f9` (inputs, tabs)
- `--color-bg-hover: #f8fafc` (hover backgrounds)

### Typography

**Font Sizes**:
```css
--text-xs: 11px     /* Tiny labels, meta info */
--text-sm: 13px     /* Small text, secondary info */
--text-base: 15px   /* Body text */
--text-lg: 17px     /* Post titles, subheadings */
--text-xl: 20px     /* Page titles, large headings */
--text-2xl: 24px    /* Hero text */
--text-3xl: 32px    /* Major headers */
--text-4xl: 40px    /* Login logo, hero */
```

**Font Weights** (Inter):
- 400: Regular body text
- 500: Medium (slightly emphasized)
- 600: Semibold (labels, buttons)
- 700: Bold (headings, important text)
- 800: Extra bold (page titles, hero)

**Line Heights**:
- Headings: 1.2 - 1.35
- Body text: 1.6 - 1.7
- Tight (badges): 1.2

**Letter Spacing**:
- Page titles: -0.03em (tighter)
- Headings: -0.02em to -0.01em
- Badges/labels: 0.02em to 0.05em (wider)

## Common Patterns

### Gradient Text Effect

```css
.page-title {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
```

### Card Lift Animation

```css
.post-card {
  transition: all var(--transition-base);
  box-shadow: var(--shadow-sm);
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-border-accent);
}
```

### Glassmorphism Nav

```css
.top-nav {
  background: var(--glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
}
```

### Ambient Background Gradient

```css
body::before {
  content: '';
  position: fixed;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(139, 92, 246, 0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(217, 70, 239, 0.03) 0%, transparent 50%);
  pointer-events: none;
  z-index: -1;
}
```

## Badge Types

```html
<span class="badge badge-pinned">📌 Pinned</span>
<span class="badge badge-golden">✨ Golden</span>
<span class="badge badge-read">✓ Read</span>
<span class="badge badge-new">New</span>
```

**Styling Notes**:
- All badges use `text-transform: uppercase` and `letter-spacing: 0.02em`
- Pinned/Golden use gold gradient backgrounds
- Read uses green transparency
- New uses primary gradient

## Interactive States

### Focus States (Inputs)
```css
input:focus, textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-glow);
  background: var(--color-bg-card);
}
```

### Active Vote Button
```css
.vote-btn.active {
  background: var(--gradient-fire);
  border-color: transparent;
  color: white;
  box-shadow: 0 0 12px rgba(234, 88, 12, 0.3);
}
```

### Hover Links
```css
a {
  color: var(--color-primary);
  transition: color var(--transition-fast);
}

a:hover {
  color: var(--color-text-primary);
}
```

## Responsive Breakpoints

```css
@media (max-width: 768px) {
  .nav-search { display: none; }
  .main-content { padding: var(--sp-4); }
  .post-card { padding: var(--sp-4); }
  .feed-tab { font-size: var(--text-xs); }
}
```

## Common Mistakes to Avoid

1. **Don't use flat colors for buttons** - Always use gradients for primary actions
2. **Don't use ⬆ for upvotes** - Use ▲ (cleaner look)
3. **Don't skip shadows** - Premium design requires depth
4. **Don't use --space-X for new code** - Use --sp-X (but aliases exist for compatibility)
5. **Don't forget hover states** - Everything interactive should respond
6. **Don't mix flat and gradient styles** - Consistency is key
7. **Don't use emojis as img tags** - Keep them as text (better rendering)

## Transitions

Standard timing functions:
```css
--transition-fast: 0.15s ease   /* Hover states, micro-interactions */
--transition-base: 0.2s ease    /* Standard interactions */
--transition-slow: 0.3s ease    /* Page transitions, major changes */
```

## Future Improvements

When creating M3 mockups or enhancing existing ones:

1. **Profile Page**: Use gradient hero section, stat cards with hover effects
2. **Search Page**: Highlight matched text with purple background
3. **Admin Dashboard**: Use data visualization with purple gradients
4. **Settings Page**: Tab-based layout with smooth transitions
5. **Notifications**: Slide-in panel with backdrop blur

## Testing Checklist

When creating new mockups:

- [ ] Inter font loaded from Google Fonts
- [ ] Nav bar has glassmorphism effect
- [ ] Brand icon has gradient + shadow + glow
- [ ] User points displayed in nav
- [ ] All buttons have hover states with transforms
- [ ] Cards lift on hover (translateY -2px)
- [ ] Vote buttons use ▲ symbol
- [ ] Active vote uses fire gradient
- [ ] Inputs have purple glow on focus
- [ ] Tags have pill shape (full border-radius)
- [ ] Proper spacing using --sp-X variables
- [ ] Shadows applied for depth
- [ ] Transitions smooth (0.15s - 0.3s)
- [ ] Responsive padding on mobile

## Color Psychology

The purple theme was chosen for:
- **Trust & Creativity**: Purple balances blue's reliability with red's passion
- **Premium Feel**: Historically associated with royalty and luxury
- **Tech-Forward**: Modern SaaS products often use purple (Twitch, Stripe, etc.)
- **Gender Neutral**: Works well for diverse engineering audiences

## Performance Notes

- **Font Loading**: Uses `font-display: swap` to prevent FOIT
- **Backdrop Blur**: May be expensive on older devices, only used on nav
- **Transitions**: Use `transform` and `opacity` (GPU accelerated) not `top`/`left`/`width`
- **Shadows**: Multiple shadow layers are fine, browsers optimize well

## Accessibility Considerations

- **Contrast**: All text meets WCAG AA standards (4.5:1 minimum)
- **Focus States**: Purple glow clearly indicates focus
- **Button Size**: Minimum 44x44px touch targets
- **Hover States**: Not relied upon for critical functionality
- **Semantic HTML**: Use `<article>`, `<nav>`, `<button>`, etc.
- **Alt Text**: Images should have alt attributes (though using emoji text currently)

---

**Last Updated**: December 2025
**Design System Version**: 1.0 (Premium Purple Edition)
**Maintained By**: Builder Syndicate Team
