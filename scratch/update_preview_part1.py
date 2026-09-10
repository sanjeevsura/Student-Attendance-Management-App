# update_preview.py
import re

with open("public/index.html", "r", encoding="utf-8") as f:
    html = f.read()

# 1. Add Dark Theme CSS and Drawer CSS right before </style>
new_css = """
    /* Dark Theme Scheme */
    [data-theme="dark"], body.dark-mode {
      --primary: #3B82F6;
      --on-primary: #FFFFFF;
      --primary-container: #1D4ED8;
      --on-primary-container: #EFF6FF;
      --secondary: #14B8A6;
      --on-secondary: #042F2E;
      --secondary-container: #0F766E;
      --on-secondary-container: #CCFBF1;
      --tertiary: #60A5FA;
      --background: #0B1120;
      --on-background: #F1F5F9;
      --surface: #1E293B;
      --on-surface: #F8FAFC;
      --surface-variant: #334155;
      --on-surface-variant: #94A3B8;
      --surface-lowest: #0B1120;
      --surface-low: #1E293B;
      --surface-card: #1E293B;
      --surface-container: #334155;
      --surface-high: #475569;
      --outline: #64748B;
      --outline-variant: #475569;
      --present-green: #22C55E;
      --present-bg: #064E3B;
      --present-text: #A7F3D0;
      --absent-red: #EF4444;
      --absent-bg: #7F1D1D;
      --absent-text: #FECACA;
      --warning-amber: #F59E0B;
      --warning-bg: #78350F;
      --warning-text: #FDE68A;
      --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.4);
      --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.5);
      --shadow-lg: 0 12px 28px rgba(0, 0, 0, 0.6);
    }
    body.dark-mode {
      background: #060913;
      color: #F1F5F9;
    }

    /* Navigation Drawer */
    .drawer-overlay {
      position: absolute;
      inset: 0;
      background: rgba(15, 23, 42, 0.65);
      backdrop-filter: blur(2px);
      z-index: 120;
      opacity: 0;
      pointer-events: none;
      transition: opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .drawer-overlay.open {
      opacity: 1;
      pointer-events: auto;
    }
    .drawer-panel {
      position: absolute;
      top: 0;
      left: 0;
      bottom: 0;
      width: 290px;
      background: var(--surface);
      box-shadow: var(--shadow-lg);
      z-index: 121;
      transform: translateX(-100%);
      transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      display: flex;
      flex-direction: column;
      overflow-y: auto;
    }
    .drawer-overlay.open .drawer-panel {
      transform: translateX(0);
    }
    .drawer-header {
      padding: 22px 18px 16px 18px;
      background: var(--surface-low);
      border-bottom: 1px solid var(--surface-container);
    }
    .drawer-divider {
      border: none;
      border-top: 1px solid var(--surface-container);
      margin: 6px 0;
    }
    .drawer-nav-list {
      padding: 10px 12px;
      display: flex;
      flex-direction: column;
      gap: 4px;
      flex: 1;
    }
    .drawer-nav-item {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 10px 14px;
      border-radius: 12px;
      border: none;
      background: transparent;
      color: var(--on-surface);
      font-size: 13px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.15s ease;
      text-align: left;
      width: 100%;
    }
    .drawer-nav-item:hover {
      background: var(--surface-low);
      color: var(--primary);
    }
    .drawer-nav-item.active {
      background: var(--primary-container);
      color: var(--on-primary-container);
    }
    .theme-segment-bar {
      display: flex;
      background: var(--surface-variant);
      border-radius: 12px;
      padding: 4px;
      gap: 4px;
      border: 1px solid var(--outline-variant);
    }
    .theme-segment-chip {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 5px;
      padding: 7px 6px;
      border: none;
      background: transparent;
      border-radius: 9px;
      font-size: 11px;
      font-weight: 700;
      color: var(--on-surface-variant);
      cursor: pointer;
      transition: all 0.18s;
    }
    .theme-segment-chip.active {
      background: var(--surface);
      color: var(--primary);
      box-shadow: var(--shadow-sm);
    }
    .category-chips-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 8px;
      margin-top: 8px;
    }
    .category-chip-btn {
      padding: 8px 6px;
      border-radius: 10px;
      border: 1.5px solid var(--outline-variant);
      background: var(--surface);
      font-size: 11px;
      font-weight: 700;
      color: var(--on-surface);
      text-align: center;
      cursor: pointer;
      transition: all 0.15s;
    }
    .category-chip-btn.selected {
      border-color: var(--primary);
      background: var(--primary-container);
      color: var(--on-primary-container);
    }
"""

if "/* Navigation Drawer */" not in html:
    html = html.replace("</style>", new_css + "\n  </style>")

# 2. Add Drawer Markup inside #phone-container right before <!-- Android Status Bar -->
drawer_markup = """
    <!-- Navigation Drawer Overlay -->
    <div class="drawer-overlay" id="drawer-overlay" onclick="handleDrawerBackdropClick(event)">
      <div class="drawer-panel" id="drawer-panel">
        <!-- Drawer Header -->
        <div class="drawer-header">
          <div style="display: flex; align-items: center; gap: 12px;">
            <div class="student-avatar" id="drawer-avatar" style="width: 44px; height: 44px; font-size: 16px; background: var(--primary); color: white;">PS</div>
            <div>
              <div id="drawer-user-name" style="font-size: 14px; font-weight: 800; color: var(--on-surface);">Prof. Sharma</div>
              <div id="drawer-user-email" style="font-size: 11px; color: var(--on-surface-variant);">admin@campus.edu</div>
              <span class="badge badge-good" id="drawer-role-badge" style="font-size: 9px; margin-top: 3px; display: inline-block;">ADMIN</span>
            </div>
          </div>
        </div>
        
        <hr class="drawer-divider" />
        
        <!-- Drawer Nav List -->
        <div class="drawer-nav-list">
          <button class="drawer-nav-item" onclick="drawerNavigate('DASHBOARD')">
            <span class="material-symbols-rounded">dashboard</span>
            <span>Dashboard</span>
          </button>
          <button class="drawer-nav-item" onclick="drawerNavigate('STUDENTS')">
            <span class="material-symbols-rounded">groups</span>
            <span>Students Roster</span>
          </button>
          <button class="drawer-nav-item" onclick="drawerNavigate('MARK_ATTENDANCE')">
            <span class="material-symbols-rounded">assignment_turned_in</span>
            <span>Mark Attendance</span>
          </button>
          <button class="drawer-nav-item" onclick="drawerNavigate('REQUESTS')">
            <span class="material-symbols-rounded">fact_check</span>
            <span>Requests Center</span>
          </button>
          <button class="drawer-nav-item" onclick="drawerNavigate('NOTIFICATIONS')">
            <span class="material-symbols-rounded">notifications</span>
            <span>Notifications</span>
          </button>
          <button class="drawer-nav-item" onclick="drawerNavigate('REPORTS')">
            <span class="material-symbols-rounded">insights</span>
            <span>Academic Analytics</span>
          </button>
          <button class="drawer-nav-item" onclick="drawerNavigate('SETTINGS')">
            <span class="material-symbols-rounded">settings</span>
            <span>Settings</span>
          </button>
        </div>
        
        <hr class="drawer-divider" />
        
        <!-- Appearance Switcher in Drawer -->
        <div style="padding: 10px 16px;">
          <div style="font-size: 11px; font-weight: 700; color: var(--on-surface-variant); margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.5px;">Appearance</div>
          <div class="theme-segment-bar">
            <button class="theme-segment-chip active" id="drawer-theme-light" onclick="setTheme('LIGHT')">
              <span class="material-symbols-rounded" style="font-size: 16px;">light_mode</span>
              <span>Light</span>
            </button>
            <button class="theme-segment-chip" id="drawer-theme-dark" onclick="setTheme('DARK')">
              <span class="material-symbols-rounded" style="font-size: 16px;">dark_mode</span>
              <span>Dark</span>
            </button>
            <button class="theme-segment-chip" id="drawer-theme-system" onclick="setTheme('SYSTEM')">
              <span class="material-symbols-rounded" style="font-size: 16px;">devices</span>
              <span>System</span>
            </button>
          </div>
        </div>
        
        <hr class="drawer-divider" style="margin-top: auto;" />
        
        <div style="padding: 12px 16px;">
          <button class="btn btn-outline" style="width: 100%; color: var(--absent-red); border: 1.5px solid #FFDAD6; background: #FFF5F5; font-weight: 700; font-size: 12px;" onclick="closeDrawer(); showLogoutConfirmModal();">
            <span class="material-symbols-rounded" style="font-size: 18px;">logout</span>
            <span>Sign Out</span>
          </button>
        </div>
      </div>
    </div>
"""

if 'id="drawer-overlay"' not in html:
    html = html.replace('<!-- Android Status Bar -->', drawer_markup + '\n    <!-- Android Status Bar -->')

# 3. Add Hamburger Button to Top Bar
topbar_target = """      <div class="app-brand">
        <div class="app-logo-icon">"""
topbar_replacement = """      <div class="app-brand">
        <button class="icon-btn" onclick="openDrawer()" title="Navigation Drawer" style="margin-right: 4px;" id="topbar-drawer-btn">
          <span class="material-symbols-rounded">menu</span>
        </button>
        <div class="app-logo-icon">"""

if 'id="topbar-drawer-btn"' not in html:
    html = html.replace(topbar_target, topbar_replacement)

# 4. Save file
with open("public/index.html", "w", encoding="utf-8") as f:
    f.write(html)

print("Updated public/index.html with CSS and Drawer!")
