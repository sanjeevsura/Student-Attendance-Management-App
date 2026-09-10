# scratch/update_preview_part2.py
import re
import shutil

with open("public/index.html", "r", encoding="utf-8") as f:
    html = f.read()

# 1. Update state to track themeMode
if "themeMode:" not in html:
    html = html.replace("activeSessionDraft: {}", 'activeSessionDraft: {},\n      themeMode: localStorage.getItem("trackedu_theme_mode") || "LIGHT"')

# 2. Add SPECIAL_ATTENDANCE_REQUEST to renderScreen switch
if "case 'SPECIAL_ATTENDANCE_REQUEST':" not in html:
    html = html.replace("case 'REQUEST_STUDENT': renderRequestStudent(container); break;", 
                        "case 'REQUEST_STUDENT': renderRequestStudent(container); break;\n        case 'SPECIAL_ATTENDANCE_REQUEST': renderSpecialAttendanceRequest(container); break;")

# 3. Add Special Attendance Button in renderRequestsScreen for teachers
old_requests_header_buttons = """          ${!isAdmin ? `
            <button class="btn btn-primary btn-sm" onclick="navigateTo('REQUEST_STUDENT')">
              <span class="material-symbols-rounded">add</span>
              <span>New Request</span>
            </button>
          ` : ''}"""

new_requests_header_buttons = """          ${!isAdmin ? `
            <div style="display: flex; gap: 6px;">
              <button class="btn btn-secondary btn-sm" onclick="navigateTo('REQUEST_STUDENT')" title="Register new student">
                <span class="material-symbols-rounded" style="font-size: 16px;">person_add</span>
                <span>Register</span>
              </button>
              <button class="btn btn-primary btn-sm" onclick="navigateTo('SPECIAL_ATTENDANCE_REQUEST')" title="OD / Medical / Special attendance">
                <span class="material-symbols-rounded" style="font-size: 16px;">verified</span>
                <span>Special</span>
              </button>
            </div>
          ` : ''}"""

if old_requests_header_buttons in html:
    html = html.replace(old_requests_header_buttons, new_requests_header_buttons)

# 4. In renderRequestsScreen: better badge and title for SPECIAL_ATTENDANCE
old_req_card_badge = """                  <span class="badge ${req.type === 'ADD_STUDENT' ? 'badge-good' : 'badge-special'}">
                    ${req.type === 'ADD_STUDENT' ? 'Student Registration' : 'Attendance Correction'}
                  </span>"""

new_req_card_badge = """                  <span class="badge ${req.type === 'ADD_STUDENT' ? 'badge-good' : (req.type === 'SPECIAL_ATTENDANCE' ? 'badge-special' : 'badge-pending')}">
                    ${req.type === 'ADD_STUDENT' ? 'Student Registration' : (req.type === 'SPECIAL_ATTENDANCE' ? `Special Attendance (${req.category || 'OD'})` : 'Attendance Correction')}
                  </span>"""

if old_req_card_badge in html:
    html = html.replace(old_req_card_badge, new_req_card_badge)

# 5. In approveRequest: add support for SPECIAL_ATTENDANCE
old_approve_correction = """      } else if (req.type === 'ATTENDANCE_CORRECTION' && req.studentId) {
        state.records.push({
          id: Date.now(),
          studentId: req.studentId,
          date: req.date,
          status: req.requestedStatus || "PRESENT",
          type: req.requestedType || "REGULAR",
          subject: req.subject,
          period: "Period 2 (10:15 AM - 11:15 AM)",
          note: `Approved correction: ${req.reason}`,
          createdBy: "Admin"
        });
      }"""

new_approve_correction = """      } else if (req.type === 'ATTENDANCE_CORRECTION' && req.studentId) {
        state.records.push({
          id: Date.now(),
          studentId: req.studentId,
          date: req.date,
          status: req.requestedStatus || "PRESENT",
          type: req.requestedType || "REGULAR",
          subject: req.subject,
          period: "Period 2 (10:15 AM - 11:15 AM)",
          note: `Approved correction: ${req.reason}`,
          createdBy: "Admin"
        });
      } else if (req.type === 'SPECIAL_ATTENDANCE' && req.studentId) {
        state.records.push({
          id: Date.now(),
          studentId: req.studentId,
          date: req.date,
          status: "PRESENT",
          type: req.category || "OD",
          subject: req.subject || "CS502 - Database Systems",
          period: "Period 2 (10:15 AM - 11:15 AM)",
          note: `Approved Special (${req.category || 'OD'}): ${req.reason}`,
          createdBy: "Admin"
        });
      }"""

if old_approve_correction in html:
    html = html.replace(old_approve_correction, new_approve_correction)

# 6. Add renderSpecialAttendanceRequest and handleSpecialAttendanceSubmit
special_screen_functions = """
    // --- SPECIAL ATTENDANCE REQUEST SCREEN (Faculty OD / Medical) ---
    let selectedSpecialCategory = 'OD';

    function renderSpecialAttendanceRequest(container) {
      const todayStr = new Date().toISOString().split('T')[0];
      container.innerHTML = `
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px;">
          <button class="btn btn-sm btn-secondary" onclick="navigateTo('REQUESTS')">
            <span class="material-symbols-rounded">arrow_back</span>
            <span>Back</span>
          </button>
          <div>
            <div style="font-size: 15px; font-weight: 800; color: var(--on-surface);">Special Attendance Request</div>
            <div style="font-size: 11px; color: var(--on-surface-variant);">Faculty On-Duty & Medical Exemption</div>
          </div>
        </div>

        <div class="card" style="background: var(--surface-low); border-left: 4px solid var(--primary); margin-bottom: 12px; box-shadow: var(--shadow-sm);">
          <div style="display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 700; color: var(--primary);">
            <span class="material-symbols-rounded" style="font-size: 16px;">verified_user</span>
            <span>Institutional Exemption Workflow</span>
          </div>
          <p style="font-size: 11px; color: var(--on-surface-variant); margin-top: 4px; line-height: 1.4;">
            Special attendance requests require physical or verified documentary evidence (OD letter, hospital prescription, competition badge) and administrator endorsement.
          </p>
        </div>

        <form class="card" onsubmit="handleSpecialAttendanceSubmit(event)" style="box-shadow: var(--shadow-md); display: flex; flex-direction: column; gap: 12px;">
          <div class="form-group">
            <label style="font-size: 11px; font-weight: 700; color: var(--on-surface-variant); margin-bottom: 4px; display: block;">Select Student *</label>
            <select class="form-control" id="special-student-select" required style="width: 100%; font-weight: 600;">
              ${state.students.map(s => `<option value="${s.id}">${s.name} (${s.rollNumber}) • ${s.section}</option>`).join('')}
            </select>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label style="font-size: 11px; font-weight: 700; color: var(--on-surface-variant); margin-bottom: 4px; display: block;">Event Date *</label>
              <input type="date" class="form-control" id="special-date" value="${todayStr}" required style="width: 100%;" />
            </div>
            <div class="form-group">
              <label style="font-size: 11px; font-weight: 700; color: var(--on-surface-variant); margin-bottom: 4px; display: block;">Subject / Course *</label>
              <select class="form-control" id="special-subject" required style="width: 100%;">
                <option value="CS502 - Database Systems">CS502 - Database Systems</option>
                <option value="CS504 - Software Engineering">CS504 - Software Engineering</option>
                <option value="CS601 - Operating Systems">CS601 - Operating Systems</option>
                <option value="CS603 - Computer Networks">CS603 - Computer Networks</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label style="font-size: 11px; font-weight: 700; color: var(--on-surface-variant); margin-bottom: 4px; display: block;">Special Attendance Category *</label>
            <div class="category-chips-grid">
              <button type="button" class="category-chip-btn ${selectedSpecialCategory === 'OD' ? 'selected' : ''}" onclick="selectSpecialCategory('OD')">🏛️ On Duty (OD)</button>
              <button type="button" class="category-chip-btn ${selectedSpecialCategory === 'MEDICAL' ? 'selected' : ''}" onclick="selectSpecialCategory('MEDICAL')">🏥 Medical</button>
              <button type="button" class="category-chip-btn ${selectedSpecialCategory === 'SPORTS' ? 'selected' : ''}" onclick="selectSpecialCategory('SPORTS')">🏆 Sports Event</button>
              <button type="button" class="category-chip-btn ${selectedSpecialCategory === 'EXAM' ? 'selected' : ''}" onclick="selectSpecialCategory('EXAM')">📝 Univ. Exam</button>
              <button type="button" class="category-chip-btn ${selectedSpecialCategory === 'LEAVE' ? 'selected' : ''}" onclick="selectSpecialCategory('LEAVE')">✈️ Formal Leave</button>
              <button type="button" class="category-chip-btn ${selectedSpecialCategory === 'OTHER' ? 'selected' : ''}" onclick="selectSpecialCategory('OTHER')">📌 Other</button>
            </div>
          </div>

          <div class="form-group">
            <label style="font-size: 11px; font-weight: 700; color: var(--on-surface-variant); margin-bottom: 4px; display: block;">Justification & Document Remarks *</label>
            <textarea class="form-control" id="special-reason" rows="3" placeholder="e.g. Participated in Inter-College Smart India Hackathon finals. Signed certificate attached with HOD approval." required style="width: 100%; font-family: inherit; font-size: 12px;"></textarea>
          </div>

          <!-- Mandatory Verification Checkbox -->
          <div style="background: var(--surface-low); border: 1.5px dashed var(--outline-variant); border-radius: 12px; padding: 12px; margin-top: 4px;">
            <label style="display: flex; align-items: flex-start; gap: 10px; cursor: pointer;">
              <input type="checkbox" id="special-verify-checkbox" onchange="toggleSpecialSubmitBtn()" style="margin-top: 2px; width: 18px; height: 18px; accent-color: var(--primary); cursor: pointer;" />
              <div style="font-size: 12px; font-weight: 700; color: var(--on-surface); line-height: 1.4;">
                I have verified the supporting documents and official duty records for this student.
                <div style="font-size: 10px; font-weight: 500; color: var(--on-surface-variant); margin-top: 2px;">
                  Submission is locked until physical/digital evidence has been reviewed.
                </div>
              </div>
            </label>
          </div>

          <button type="submit" class="btn btn-primary" id="btn-submit-special" disabled style="width: 100%; height: 46px; font-weight: 800; margin-top: 4px; opacity: 0.6; cursor: not-allowed;">
            <span class="material-symbols-rounded">send</span>
            <span>Submit Special Request</span>
          </button>
        </form>
      `;
    }

    function selectSpecialCategory(cat) {
      selectedSpecialCategory = cat;
      const buttons = document.querySelectorAll('.category-chip-btn');
      buttons.forEach(btn => {
        btn.classList.toggle('selected', btn.innerText.includes(cat) || (cat === 'LEAVE' && btn.innerText.includes('Leave')) || (cat === 'SPORTS' && btn.innerText.includes('Sports')) || (cat === 'EXAM' && btn.innerText.includes('Exam')));
      });
    }

    function toggleSpecialSubmitBtn() {
      const chk = document.getElementById('special-verify-checkbox');
      const btn = document.getElementById('btn-submit-special');
      if (chk && btn) {
        btn.disabled = !chk.checked;
        btn.style.opacity = chk.checked ? '1' : '0.6';
        btn.style.cursor = chk.checked ? 'pointer' : 'not-allowed';
      }
    }

    function handleSpecialAttendanceSubmit(e) {
      e.preventDefault();
      const studentId = parseInt(document.getElementById('special-student-select').value);
      const student = state.students.find(s => s.id === studentId);
      const date = document.getElementById('special-date').value;
      const subject = document.getElementById('special-subject').value;
      const reason = document.getElementById('special-reason').value.trim();
      const chk = document.getElementById('special-verify-checkbox');

      if (!chk || !chk.checked) {
        showToast('You must confirm document verification before submitting.', true);
        return;
      }

      state.requests.unshift({
        id: Date.now(),
        type: "SPECIAL_ATTENDANCE",
        submittedBy: state.currentUserName,
        studentId: student.id,
        studentName: student.name,
        studentRollNumber: student.rollNumber,
        department: student.department,
        subject: subject,
        category: selectedSpecialCategory,
        requestedStatus: "PRESENT",
        requestedType: selectedSpecialCategory,
        reason: `[${selectedSpecialCategory}] ${reason}`,
        status: "PENDING",
        date: date
      });

      state.notifications.unshift({
        id: Date.now(),
        title: "Special Attendance Request",
        message: `${state.currentUserName} submitted ${selectedSpecialCategory} request for ${student.name} (${student.rollNumber}).`,
        category: "REQUESTS",
        isRead: false
      });

      saveState();
      showToast(`Special attendance request (${selectedSpecialCategory}) submitted to Admin`);
      navigateTo('REQUESTS');
    }
"""

if "function renderSpecialAttendanceRequest(" not in html:
    html = html.replace("function renderRequestStudent(container) {", special_screen_functions + "\n    function renderRequestStudent(container) {")

# 7. Update renderSettings to include 3-Chip Appearance Selector & Authentication Session & Security Card
old_settings_user_card = """        <div class="card">
          <div class="section-title">Active User Session</div>
          <div style="display: flex; align-items: center; gap: 12px; margin-top: 8px;">
            <div class="student-avatar" style="width: 46px; height: 46px; font-size: 16px;">${isAdmin ? 'PS' : 'AR'}</div>
            <div>
              <div style="font-size: 14px; font-weight: 700; color: var(--on-surface);">${state.currentUserName}</div>
              <div style="font-size: 12px; color: var(--on-surface-variant);">${state.currentUserEmail}</div>
              <span class="badge ${isAdmin ? 'badge-good' : 'badge-special'}" style="font-size: 10px; margin-top: 4px;">Role: ${state.role}</span>
            </div>
          </div>
          <button class="btn btn-outline" style="width: 100%; margin-top: 14px; color: var(--absent-red); border: 1.5px solid #FFDAD6; background: #FFF5F5; font-weight: 700;" onclick="showLogoutConfirmModal()">
            <span class="material-symbols-rounded">logout</span>
            <span>Sign Out of TrackEdu</span>
          </button>
        </div>"""

new_settings_cards = """        <div class="card">
          <div class="section-title">Active User Session</div>
          <div style="display: flex; align-items: center; gap: 12px; margin-top: 8px;">
            <div class="student-avatar" style="width: 46px; height: 46px; font-size: 16px;">${isAdmin ? 'PS' : 'AR'}</div>
            <div>
              <div style="font-size: 14px; font-weight: 700; color: var(--on-surface);">${state.currentUserName}</div>
              <div style="font-size: 12px; color: var(--on-surface-variant);">${state.currentUserEmail}</div>
              <span class="badge ${isAdmin ? 'badge-good' : 'badge-special'}" style="font-size: 10px; margin-top: 4px;">Role: ${state.role}</span>
            </div>
          </div>
          <button class="btn btn-outline" style="width: 100%; margin-top: 14px; color: var(--absent-red); border: 1.5px solid #FFDAD6; background: #FFF5F5; font-weight: 700;" onclick="showLogoutConfirmModal()">
            <span class="material-symbols-rounded">logout</span>
            <span>Sign Out of TrackEdu</span>
          </button>
        </div>

        <!-- Appearance Mode Selector (3 Segments) -->
        <div class="card">
          <div class="section-title">Theme & Appearance</div>
          <div style="font-size: 11px; color: var(--on-surface-variant); margin-top: 2px; margin-bottom: 10px;">
            Choose system default or switch between Stitch White Light and Sleek Dark modes.
          </div>
          <div class="theme-segment-bar">
            <button class="theme-segment-chip ${state.themeMode === 'LIGHT' ? 'active' : ''}" id="settings-theme-light" onclick="setTheme('LIGHT')">
              <span class="material-symbols-rounded" style="font-size: 16px;">light_mode</span>
              <span>Light</span>
            </button>
            <button class="theme-segment-chip ${state.themeMode === 'DARK' ? 'active' : ''}" id="settings-theme-dark" onclick="setTheme('DARK')">
              <span class="material-symbols-rounded" style="font-size: 16px;">dark_mode</span>
              <span>Dark</span>
            </button>
            <button class="theme-segment-chip ${state.themeMode === 'SYSTEM' ? 'active' : ''}" id="settings-theme-system" onclick="setTheme('SYSTEM')">
              <span class="material-symbols-rounded" style="font-size: 16px;">devices</span>
              <span>System</span>
            </button>
          </div>
        </div>

        <!-- Authentication Session & Security Card (5-Minute Absolute Timeout) -->
        <div class="card" style="border-left: 4px solid var(--secondary);">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div class="section-title" style="margin-bottom: 0;">Authentication Session & Security</div>
            <span class="badge badge-good" style="font-size: 10px;">Active</span>
          </div>
          <div style="background: var(--surface-low); border-radius: 12px; padding: 12px; margin-top: 10px; display: flex; align-items: center; justify-content: space-between;">
            <div>
              <div style="font-size: 11px; font-weight: 700; color: var(--on-surface-variant);">AUTO-LOGOUT COUNTDOWN</div>
              <div style="font-size: 10px; color: var(--outline); margin-top: 1px;">Strict 5-minute absolute lifetime</div>
            </div>
            <div id="settings-session-countdown" style="font-size: 20px; font-weight: 800; font-family: monospace; color: var(--primary);">
              04:59
            </div>
          </div>
          <p style="font-size: 11px; color: var(--on-surface-variant); margin-top: 8px; line-height: 1.4;">
            For statutory student privacy, your session automatically expires after 5 minutes. User activity or navigation does not extend this window.
          </p>
          <button class="btn btn-secondary btn-sm" style="width: 100%; margin-top: 10px; font-weight: 700;" onclick="triggerSessionExpiry()">
            <span class="material-symbols-rounded" style="font-size: 16px;">alarm_off</span>
            <span>Simulate 5-Min Session Expiry (QA Test)</span>
          </button>
        </div>"""

if old_settings_user_card in html:
    html = html.replace(old_settings_user_card, new_settings_cards)

# 8. Add Theme switching functions, Drawer functions, and 5-Minute Session Timer functions
engine_functions = """
    // --- Appearance & Theme Engine ---
    function setTheme(mode) {
      state.themeMode = mode;
      localStorage.setItem('trackedu_theme_mode', mode);

      let isDark = false;
      if (mode === 'DARK') isDark = true;
      else if (mode === 'SYSTEM') isDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
      else isDark = false;

      document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
      document.body.classList.toggle('dark-mode', isDark);

      ['drawer-theme', 'settings-theme'].forEach(prefix => {
        const l = document.getElementById(`${prefix}-light`);
        const d = document.getElementById(`${prefix}-dark`);
        const s = document.getElementById(`${prefix}-system`);
        if (l) l.classList.toggle('active', mode === 'LIGHT');
        if (d) d.classList.toggle('active', mode === 'DARK');
        if (s) s.classList.toggle('active', mode === 'SYSTEM');
      });

      showToast(`Appearance: ${mode.charAt(0) + mode.slice(1).toLowerCase()} Mode`);
    }

    // Initialize Theme
    const savedTheme = localStorage.getItem('trackedu_theme_mode') || 'LIGHT';
    if (savedTheme === 'DARK' || (savedTheme === 'SYSTEM' && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      document.documentElement.setAttribute('data-theme', 'dark');
      document.body.classList.add('dark-mode');
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
      document.body.classList.remove('dark-mode');
    }

    // --- Navigation Drawer Functions ---
    function openDrawer() {
      const overlay = document.getElementById('drawer-overlay');
      const avatar = document.getElementById('drawer-avatar');
      const name = document.getElementById('drawer-user-name');
      const email = document.getElementById('drawer-user-email');
      const roleBadge = document.getElementById('drawer-role-badge');
      
      if (avatar) avatar.innerText = state.role === 'ADMIN' ? 'PS' : 'AR';
      if (name) name.innerText = state.currentUserName;
      if (email) email.innerText = state.currentUserEmail;
      if (roleBadge) {
        roleBadge.innerText = state.role;
        roleBadge.className = `badge ${state.role === 'ADMIN' ? 'badge-good' : 'badge-special'}`;
      }
      
      if (overlay) overlay.classList.add('open');
    }

    function closeDrawer() {
      const overlay = document.getElementById('drawer-overlay');
      if (overlay) overlay.classList.remove('open');
    }

    function handleDrawerBackdropClick(e) {
      if (e.target.id === 'drawer-overlay') {
        closeDrawer();
      }
    }

    function drawerNavigate(screen) {
      closeDrawer();
      navigateTo(screen);
    }

    // --- Absolute 5-Minute Session Expiry Engine ---
    let sessionRemainingSeconds = 300;
    let sessionTimerInterval = null;

    function startSessionTimer() {
      if (sessionTimerInterval) clearInterval(sessionTimerInterval);
      
      const session = JSON.parse(localStorage.getItem('trackedu_session'));
      if (session && session.isLoggedIn) {
        if (!session.expiryTime) {
          session.expiryTime = Date.now() + 300 * 1000;
          localStorage.setItem('trackedu_session', JSON.stringify(session));
        }
      }

      sessionTimerInterval = setInterval(() => {
        if (!state.isLoggedIn) return;
        const curSession = JSON.parse(localStorage.getItem('trackedu_session'));
        if (!curSession || !curSession.expiryTime) return;

        const diff = Math.max(0, Math.floor((curSession.expiryTime - Date.now()) / 1000));
        sessionRemainingSeconds = diff;

        const countdownEl = document.getElementById('settings-session-countdown');
        if (countdownEl) {
          const mins = String(Math.floor(sessionRemainingSeconds / 60)).padStart(2, '0');
          const secs = String(sessionRemainingSeconds % 60).padStart(2, '0');
          countdownEl.innerText = `${mins}:${secs}`;
        }

        if (sessionRemainingSeconds <= 0) {
          triggerSessionExpiry();
        }
      }, 1000);
    }

    function triggerSessionExpiry() {
      state.isLoggedIn = false;
      localStorage.removeItem('trackedu_session');
      showModal(`
        <div style="text-align: center; padding: 10px 4px;">
          <div style="width: 52px; height: 52px; border-radius: 50%; background: #FFDAD6; color: var(--absent-red); display: flex; align-items: center; justify-content: center; margin: 0 auto 12px auto;">
            <span class="material-symbols-rounded" style="font-size: 30px;">timer_off</span>
          </div>
          <div style="font-size: 16px; font-weight: 800; color: var(--on-surface); margin-bottom: 6px;">Session Expired</div>
          <p style="font-size: 12px; color: var(--on-surface-variant); line-height: 1.5; margin-bottom: 16px;">
            For statutory student privacy and security compliance, your session has timed out after 5 minutes of absolute lifetime.
          </p>
          <button class="btn btn-primary" style="width: 100%; height: 44px; font-weight: 800;" onclick="hideModal(); navigateTo('LOGIN');">
            <span class="material-symbols-rounded">login</span>
            <span>Return to Sign In</span>
          </button>
        </div>
      `);
    }

    // Hook session timer to login success
"""

if "function setTheme(" not in html:
    html = html.replace("navigateTo('SPLASH');", engine_functions + "\n    startSessionTimer();\n    navigateTo('SPLASH');")

# Also ensure login stores expiryTime
if "expiryTime: Date.now() + 300 * 1000," not in html:
    html = html.replace("isLoggedIn: true\n        }));", "isLoggedIn: true,\n          expiryTime: Date.now() + 300 * 1000\n        }));\n        startSessionTimer();")

# Save to public/index.html
with open("public/index.html", "w", encoding="utf-8") as f:
    f.write(html)

# Synchronize with root index.html
shutil.copyfile("public/index.html", "index.html")

print("Successfully updated public/index.html and index.html!")
