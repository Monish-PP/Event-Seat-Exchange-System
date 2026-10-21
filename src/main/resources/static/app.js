/**
 * Event Seat Exchange - Frontend Application Logic
 * Supports Student & Admin Dedicated Authentication, Peer-to-Peer Transfer, and Event Management.
 */

document.addEventListener('DOMContentLoaded', () => {
    // ==========================================
    // STATE
    // ==========================================
    let currentUser = null; // { role: 'STUDENT' | 'ADMIN', studentId, name, email, department, yearOfStudy }
    let currentStudentId = null;
    let students = [];
    let events = [];

    // ==========================================
    // DOM ELEMENTS - AUTH & SHELL
    // ==========================================
    const loginScreen = document.getElementById('login-screen');
    const appLayout = document.getElementById('app-layout');

    // Login Tabs & Panels
    const tabBtnStudent = document.getElementById('tab-btn-student');
    const tabBtnAdmin = document.getElementById('tab-btn-admin');
    const panelStudentLogin = document.getElementById('panel-student-login');
    const panelAdminLogin = document.getElementById('panel-admin-login');
    const quickStudentsChips = document.getElementById('quick-students-chips');

    // Forms
    const formStudentLogin = document.getElementById('form-student-login');
    const studentLoginEmail = document.getElementById('student-login-email');
    const studentLoginPassword = document.getElementById('student-login-password');
    const btnToggleStudentSignup = document.getElementById('btn-toggle-student-signup');
    const formStudentSignup = document.getElementById('form-student-signup');
    const signupPasswordInput = document.getElementById('signup-password');
    const formAdminLogin = document.getElementById('form-admin-login');
    const adminLoginEmail = document.getElementById('admin-login-email');
    const adminLoginPassword = document.getElementById('admin-login-password');

    // Top Navbar Profile & Controls
    const navAvatarCircle = document.getElementById('nav-avatar-circle');
    const navAvatarInitial = document.getElementById('nav-avatar-initial');
    const navUserName = document.getElementById('nav-user-name');
    const navUserRole = document.getElementById('nav-user-role');
    const btnLogout = document.getElementById('btn-logout');

    // Navbar Navigation Tabs
    const navTabEvents = document.getElementById('nav-tab-events');
    const navTabMyRegistrations = document.getElementById('nav-tab-my-registrations');
    const navTabMarketplace = document.getElementById('nav-tab-marketplace');
    const navTabIncoming = document.getElementById('nav-tab-incoming');
    const navTabOutgoing = document.getElementById('nav-tab-outgoing');
    const navTabHistory = document.getElementById('nav-tab-history');
    const navTabCreateEvent = document.getElementById('nav-tab-create-event');

    // Content Containers
    const mySeatsStudentName = document.getElementById('my-seats-student-name');
    const eventsGrid = document.getElementById('events-grid-container');
    const myRegistrationsContainer = document.getElementById('my-registrations-container');
    const marketplaceContainer = document.getElementById('marketplace-list-container');
    const incomingContainer = document.getElementById('incoming-requests-container');
    const outgoingContainer = document.getElementById('outgoing-requests-container');
    const historyTableBody = document.getElementById('history-table-body');
    const eventsSearchInput = document.getElementById('events-search-input');
    const createEventForm = document.getElementById('create-event-form');

    // Badges & Stats Elements
    const statTotalEvents = document.getElementById('stat-total-events');
    const statDirectSeats = document.getElementById('stat-direct-seats');
    const statListedExchange = document.getElementById('stat-listed-exchange');
    const statTransfers = document.getElementById('stat-transfers');
    const badgeMySeats = document.getElementById('badge-my-seats');
    const badgeMarketplace = document.getElementById('badge-marketplace-seats');
    const badgeIncoming = document.getElementById('badge-incoming-count');
    const badgeOutgoing = document.getElementById('badge-outgoing-count');

    // ==========================================
    // APP INITIALIZATION
    // ==========================================
    initTheme();
    setupAuthListeners();
    setupProfileDropdown();
    setupTabs();
    setupSearch();
    setupCreateEventForm();
    setupThemeToggle();

    // Check if user session already exists
    checkExistingAuth();

    // ==========================================
    // THEME MANAGEMENT
    // ==========================================
    function initTheme() {
        const savedTheme = localStorage.getItem('seat_exchange_theme') || 'light';
        applyTheme(savedTheme);
    }

    function applyTheme(theme) {
        const themeIcon = document.getElementById('theme-icon');
        if (theme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
            if (themeIcon) {
                themeIcon.innerHTML = `<circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>`;
            }
        } else {
            document.documentElement.removeAttribute('data-theme');
            if (themeIcon) {
                themeIcon.innerHTML = `<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>`;
            }
        }
        localStorage.setItem('seat_exchange_theme', theme);
    }

    function setupThemeToggle() {
        const btn = document.getElementById('btn-theme-toggle');
        if (btn) {
            btn.addEventListener('click', () => {
                const currentTheme = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
                const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
                applyTheme(nextTheme);
                showToast(`Switched to ${nextTheme === 'light' ? 'Light' : 'Dark'} theme`, 'info');
            });
        }
    }

    // ==========================================
    // AUTHENTICATION & LOGIN LOGIC
    // ==========================================
    function checkExistingAuth() {
        const savedSession = sessionStorage.getItem('seat_exchange_user');
        if (savedSession) {
            try {
                const user = JSON.parse(savedSession);
                if (user && user.role) {
                    loginSuccess(user, false);
                    return;
                }
            } catch (e) {
                sessionStorage.removeItem('seat_exchange_user');
            }
        }
        // Show login screen
        showLoginScreen();
    }

    function showLoginScreen() {
        if (loginScreen) loginScreen.style.display = 'flex';
        if (appLayout) appLayout.style.display = 'none';
    }

    function setupAuthListeners() {
        // Toggle Student vs Admin Login Tabs
        if (tabBtnStudent && tabBtnAdmin) {
            tabBtnStudent.addEventListener('click', () => {
                tabBtnStudent.classList.add('active');
                tabBtnAdmin.classList.remove('active');
                if (panelStudentLogin) panelStudentLogin.style.display = 'block';
                if (panelAdminLogin) panelAdminLogin.style.display = 'none';
            });

            tabBtnAdmin.addEventListener('click', () => {
                tabBtnAdmin.classList.add('active');
                tabBtnStudent.classList.remove('active');
                if (panelAdminLogin) panelAdminLogin.style.display = 'block';
                if (panelStudentLogin) panelStudentLogin.style.display = 'none';
            });
        }

        // Student Login Form Submit
        if (formStudentLogin) {
            formStudentLogin.addEventListener('submit', async (e) => {
                e.preventDefault();
                const email = studentLoginEmail ? studentLoginEmail.value.trim() : '';
                const password = studentLoginPassword ? studentLoginPassword.value.trim() : '';
                await performStudentLogin(email, password);
            });
        }

        // Toggle Student Signup Collapsible Form
        if (btnToggleStudentSignup && formStudentSignup) {
            btnToggleStudentSignup.addEventListener('click', () => {
                const isHidden = formStudentSignup.style.display === 'none' || formStudentSignup.style.display === '';
                formStudentSignup.style.display = isHidden ? 'block' : 'none';
                const label = btnToggleStudentSignup.querySelector('span');
                if (label) {
                    label.textContent = isHidden ? 'Cancel Registration' : 'New Student? Register Account';
                }
            });
        }

        // Student Signup Form Submit
        if (formStudentSignup) {
            formStudentSignup.addEventListener('submit', async (e) => {
                e.preventDefault();
                const name = document.getElementById('signup-name').value.trim();
                const email = document.getElementById('signup-email').value.trim();
                const password = signupPasswordInput ? signupPasswordInput.value.trim() : '';
                const dept = document.getElementById('signup-dept').value;
                const year = parseInt(document.getElementById('signup-year').value, 10);

                if (!name || !email || !password) {
                    showToast('Please provide your name, college email, and password.', 'error');
                    return;
                }

                try {
                    const res = await fetch('/api/students', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ 
                            studentName: name, 
                            email: email, 
                            department: dept, 
                            yearOfStudy: year,
                            password: password 
                        })
                    });
                    const data = await res.json();
                    if (data.success && data.student) {
                        showToast(`Account registered successfully! Welcome, ${name}`, 'success');
                        formStudentSignup.reset();
                        formStudentSignup.style.display = 'none';
                        // Auto-login registered student
                        loginSuccess({
                            role: 'STUDENT',
                            studentId: data.student.studentId,
                            name: data.student.studentName,
                            email: data.student.email,
                            department: data.student.department,
                            yearOfStudy: data.student.yearOfStudy
                        }, true);
                    } else {
                        showToast(data.message || 'Registration failed.', 'error');
                    }
                } catch (err) {
                    console.error('Student signup error:', err);
                    showToast('Network error during student registration.', 'error');
                }
            });
        }

        // Admin Login Form Submit
        if (formAdminLogin) {
            formAdminLogin.addEventListener('submit', async (e) => {
                e.preventDefault();
                const email = adminLoginEmail ? adminLoginEmail.value.trim() : '';
                const password = adminLoginPassword ? adminLoginPassword.value.trim() : '';
                await performAdminLogin(email, password);
            });
        }

        // Top Navbar Logout Button
        if (btnLogout) {
            btnLogout.addEventListener('click', () => {
                const container = document.getElementById('profile-dropdown-container');
                const menu = document.getElementById('profile-dropdown-menu');
                if (menu) menu.classList.remove('show');
                if (container) container.classList.remove('open');

                sessionStorage.removeItem('seat_exchange_user');
                currentUser = null;
                currentStudentId = null;
                showToast('Logged out of system.', 'info');
                showLoginScreen();
            });
        }
    }

    // ==========================================
    // PROFILE DROPDOWN COMPONENT
    // ==========================================
    function setupProfileDropdown() {
        const trigger = document.getElementById('btn-profile-trigger');
        const container = document.getElementById('profile-dropdown-container');
        const menu = document.getElementById('profile-dropdown-menu');
        const mySeatsBtn = document.getElementById('btn-profile-my-seats');

        if (trigger && menu) {
            trigger.addEventListener('click', (e) => {
                e.stopPropagation();
                const isOpen = menu.classList.contains('show');
                if (isOpen) {
                    menu.classList.remove('show');
                    container?.classList.remove('open');
                } else {
                    menu.classList.add('show');
                    container?.classList.add('open');
                }
            });

            // Close when clicking outside
            document.addEventListener('click', (e) => {
                if (container && !container.contains(e.target)) {
                    menu.classList.remove('show');
                    container.classList.remove('open');
                }
            });

            // Close on escape
            document.addEventListener('keydown', (e) => {
                if (e.key === 'Escape') {
                    menu.classList.remove('show');
                    container?.classList.remove('open');
                }
            });
        }

        if (mySeatsBtn) {
            mySeatsBtn.addEventListener('click', () => {
                menu?.classList.remove('show');
                container?.classList.remove('open');
                if (navTabMyRegistrations) navTabMyRegistrations.click();
            });
        }
    }

    async function performStudentLogin(email, password) {
        if (!email || !password) {
            showToast('Please enter both your college email and password.', 'error');
            return;
        }

        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ role: 'STUDENT', email, password })
            });
            const data = await res.json();
            if (data.success) {
                loginSuccess(data, true);
            } else {
                showToast(data.message || 'Invalid email or password.', 'error');
            }
        } catch (err) {
            console.error('Student login error:', err);
            showToast('Network error during student login.', 'error');
        }
    }

    async function performAdminLogin(email, password) {
        if (!email || !password) {
            showToast('Please enter both admin email and password.', 'error');
            return;
        }

        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ role: 'ADMIN', email, password })
            });
            const data = await res.json();
            if (data.success) {
                loginSuccess(data, true);
            } else {
                showToast(data.message || 'Invalid administrator credentials.', 'error');
            }
        } catch (err) {
            console.error('Admin login error:', err);
            showToast('Network error during admin login.', 'error');
        }
    }

    function loginSuccess(userData, showWelcomeToast = false) {
        currentUser = userData;
        sessionStorage.setItem('seat_exchange_user', JSON.stringify(userData));

        if (userData.role === 'STUDENT') {
            currentStudentId = userData.studentId;
        } else {
            currentStudentId = null;
        }

        if (showWelcomeToast) {
            showToast(`Welcome, ${userData.name || 'User'}!`, 'success');
        }

        applyUserRoleLayout(userData);
    }

    function applyUserRoleLayout(user) {
        if (loginScreen) loginScreen.style.display = 'none';
        if (appLayout) appLayout.style.display = 'block';

        const isAdmin = user.role === 'ADMIN';
        const name = user.name || (isAdmin ? 'Administrator' : 'Student');
        const initial = (name.trim().charAt(0) || 'U').toUpperCase();

        // Update Top Navbar Profile Trigger Button
        if (navAvatarInitial) navAvatarInitial.textContent = initial;
        if (navUserName) {
            navUserName.textContent = name;
            navUserName.title = user.email || '';
        }
        if (navAvatarCircle) {
            if (isAdmin) navAvatarCircle.classList.add('admin');
            else navAvatarCircle.classList.remove('admin');
        }

        // Update Profile Dropdown Details
        const dropdownAvatarLetter = document.getElementById('dropdown-avatar-letter');
        const dropdownAvatarCircle = document.getElementById('dropdown-avatar-circle');
        const dropdownUserName = document.getElementById('dropdown-user-name');
        const dropdownUserEmail = document.getElementById('dropdown-user-email');
        const dropdownUserDept = document.getElementById('dropdown-user-dept');
        const btnProfileMySeats = document.getElementById('btn-profile-my-seats');

        if (dropdownAvatarLetter) dropdownAvatarLetter.textContent = initial;
        if (dropdownAvatarCircle) {
            if (isAdmin) dropdownAvatarCircle.classList.add('admin');
            else dropdownAvatarCircle.classList.remove('admin');
        }
        if (dropdownUserName) dropdownUserName.textContent = name;
        if (dropdownUserEmail) dropdownUserEmail.textContent = user.email || '';

        if (navUserRole) {
            navUserRole.textContent = isAdmin ? 'Admin' : 'Student';
            if (isAdmin) {
                navUserRole.className = 'nav-user-role admin';
            } else {
                navUserRole.className = 'nav-user-role';
            }
        }

        if (dropdownUserDept) {
            if (isAdmin) {
                dropdownUserDept.textContent = 'Campus Event Management';
            } else {
                dropdownUserDept.textContent = `${user.department || 'Student'} • Year ${user.yearOfStudy || 'N/A'}`;
            }
        }

        if (btnProfileMySeats) {
            btnProfileMySeats.style.display = isAdmin ? 'none' : 'flex';
        }

        // Adjust Navbar Tabs visibility by Role
        if (isAdmin) {
            // Admin View:
            // Can see: Upcoming Events, Seat Marketplace, Audit History, + Add Event
            // Cannot see personal student tabs: My Booked Seats, Incoming Requests, My Sent Requests
            if (navTabEvents) navTabEvents.style.display = 'inline-flex';
            if (navTabMarketplace) navTabMarketplace.style.display = 'inline-flex';
            if (navTabHistory) navTabHistory.style.display = 'inline-flex';
            if (navTabCreateEvent) navTabCreateEvent.style.display = 'inline-flex';

            if (navTabMyRegistrations) navTabMyRegistrations.style.display = 'none';
            if (navTabIncoming) navTabIncoming.style.display = 'none';
            if (navTabOutgoing) navTabOutgoing.style.display = 'none';
        } else {
            // Student View:
            // Can see: Upcoming Events, My Booked Seats, Seat Marketplace, Incoming Requests, My Sent Requests, Audit History
            // Cannot see: + Add Event
            if (navTabEvents) navTabEvents.style.display = 'inline-flex';
            if (navTabMyRegistrations) navTabMyRegistrations.style.display = 'inline-flex';
            if (navTabMarketplace) navTabMarketplace.style.display = 'inline-flex';
            if (navTabIncoming) navTabIncoming.style.display = 'inline-flex';
            if (navTabOutgoing) navTabOutgoing.style.display = 'inline-flex';
            if (navTabHistory) navTabHistory.style.display = 'inline-flex';

            if (navTabCreateEvent) navTabCreateEvent.style.display = 'none';

            if (mySeatsStudentName) {
                mySeatsStudentName.textContent = user.name || `Student #${user.studentId}`;
            }
        }

        // Switch default tab to Upcoming Events
        if (navTabEvents) {
            navTabEvents.click();
        }

        // Fetch fresh data
        refreshAllData();
    }

    function getActiveStudentName() {
        if (currentUser && currentUser.role === 'STUDENT') {
            return currentUser.name || `Student #${currentUser.studentId}`;
        }
        return 'Student';
    }

    // ==========================================
    // TABS SYSTEM
    // ==========================================
    function setupTabs() {
        const tabBtns = document.querySelectorAll('.tab-btn');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetTabId = btn.getAttribute('data-tab');
                
                // Toggle active button
                tabBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');

                // Toggle active pane
                document.querySelectorAll('.tab-pane').forEach(pane => {
                    pane.classList.remove('active');
                });
                const targetPane = document.getElementById(targetTabId);
                if (targetPane) {
                    targetPane.classList.add('active');
                }

                // Refresh specific pane data
                handleTabChange(targetTabId);
            });
        });

        // Tab manual refresh button listeners
        document.getElementById('btn-refresh-events')?.addEventListener('click', loadEvents);
        document.getElementById('btn-refresh-my-seats')?.addEventListener('click', loadMyRegistrations);
        document.getElementById('btn-refresh-marketplace')?.addEventListener('click', loadMarketplace);
        document.getElementById('btn-refresh-incoming')?.addEventListener('click', loadIncomingRequests);
        document.getElementById('btn-refresh-outgoing')?.addEventListener('click', loadOutgoingRequests);
        document.getElementById('btn-refresh-history')?.addEventListener('click', loadHistory);
    }

    function handleTabChange(tabId) {
        switch (tabId) {
            case 'tab-events':
                loadEvents();
                break;
            case 'tab-my-registrations':
                loadMyRegistrations();
                break;
            case 'tab-marketplace':
                loadMarketplace();
                break;
            case 'tab-incoming':
                loadIncomingRequests();
                break;
            case 'tab-outgoing':
                loadOutgoingRequests();
                break;
            case 'tab-history':
                loadHistory();
                break;
        }
    }

    // ==========================================
    // DATA REFRESH ALL
    // ==========================================
    async function refreshAllData() {
        const promises = [
            loadStats(),
            loadEvents(),
            loadMarketplace(),
            loadHistory()
        ];

        if (currentUser && currentUser.role === 'STUDENT' && currentStudentId) {
            promises.push(loadMyRegistrations());
            promises.push(loadIncomingRequests());
            promises.push(loadOutgoingRequests());
        }

        await Promise.all(promises);
    }

    // ==========================================
    // 1. STATS METRICS
    // ==========================================
    async function loadStats() {
        try {
            const res = await fetch('/api/stats');
            const data = await res.json();
            if (statTotalEvents) statTotalEvents.textContent = data.totalEvents ?? 0;
            if (statDirectSeats) statDirectSeats.textContent = data.totalDirectAvailableSeats ?? 0;
            if (statListedExchange) statListedExchange.textContent = data.seatsListedForExchange ?? 0;
            if (statTransfers) statTransfers.textContent = data.completedTransfers ?? 0;
        } catch (err) {
            console.error('Stats error:', err);
        }
    }

    // ==========================================
    // 2. UPCOMING EVENTS
    // ==========================================
    async function loadEvents() {
        try {
            const res = await fetch('/api/events');
            events = await res.json();
            renderEvents(events);
        } catch (err) {
            console.error('Events error:', err);
            if (eventsGrid) {
                eventsGrid.innerHTML = `<div class="empty-state"><p>Could not load events.</p></div>`;
            }
        }
    }

    function renderEvents(eventsList) {
        if (!eventsGrid) return;

        if (!eventsList || eventsList.length === 0) {
            eventsGrid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <div class="empty-state-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="8" y1="12" x2="16" y2="12"></line></svg>
                    </div>
                    <div class="empty-state-title">No Events Available</div>
                    <div class="empty-state-desc">There are no upcoming college events scheduled at this moment.</div>
                </div>`;
            return;
        }

        const isAdmin = currentUser && currentUser.role === 'ADMIN';

        eventsGrid.innerHTML = eventsList.map(event => {
            const available = event.availableSeats;
            const total = event.totalSeats;
            const pct = total > 0 ? Math.round((available / total) * 100) : 0;
            const isLow = pct <= 20;
            const isFull = available <= 0;

            const dateStr = event.eventDate ? new Date(event.eventDate).toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' }) : '';
            const deadlineStr = event.registrationDeadline ? new Date(event.registrationDeadline).toLocaleString() : 'N/A';

            let buttonLabel = 'Reserve Direct Seat';
            let isDisabled = false;

            if (isFull) {
                buttonLabel = 'Capacity Reached';
                isDisabled = true;
            } else if (isAdmin) {
                buttonLabel = 'Admin View';
                isDisabled = true;
            }

            return `
                <div class="event-card">
                    <div>
                        <div class="event-card-header">
                            <h3 class="event-name">${escapeHtml(event.eventName)}</h3>
                            <span class="badge-status ${isFull ? 'rejected' : 'confirmed'}">
                                ${isFull ? 'Housefull' : 'Open'}
                            </span>
                        </div>
                        <div class="event-meta-list">
                            <div class="event-meta-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                                <span>${dateStr} at ${event.eventTime || ''}</span>
                            </div>
                            <div class="event-meta-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                <span>${escapeHtml(event.venue)}</span>
                            </div>
                            <div class="event-meta-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                <span>Deadline: ${deadlineStr}</span>
                            </div>
                        </div>
                        <div class="seat-progress-box">
                            <div class="seat-progress-label">
                                <span>Seat Capacity</span>
                                <strong>${available} / ${total} seats left</strong>
                            </div>
                            <div class="seat-progress-bar">
                                <div class="seat-progress-fill ${isLow ? 'low' : ''}" style="width: ${pct}%"></div>
                            </div>
                        </div>
                    </div>
                    <div class="event-card-footer">
                        <span class="text-muted" style="font-size: 0.8rem;">Event ID: #${event.eventId}</span>
                        <button class="btn btn-primary btn-sm btn-book-seat" 
                                data-event-id="${event.eventId}" 
                                data-event-name="${escapeHtml(event.eventName)}"
                                ${isDisabled ? 'disabled' : ''}>
                            ${buttonLabel}
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        // Attach listeners for direct booking
        document.querySelectorAll('.btn-book-seat').forEach(btn => {
            btn.addEventListener('click', () => {
                if (!currentStudentId) {
                    showToast('Please sign in as a student to reserve seats.', 'error');
                    return;
                }
                const eventId = parseInt(btn.getAttribute('data-event-id'), 10);
                const eventName = btn.getAttribute('data-event-name');
                handleDirectBooking(eventId, eventName);
            });
        });
    }

    async function handleDirectBooking(eventId, eventName) {
        if (!currentStudentId) return;

        try {
            const res = await fetch('/api/registrations/book', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ eventId, studentId: currentStudentId })
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Seat reserved successfully for ${eventName}!`, 'success');
                refreshAllData();
            } else {
                showToast(data.message || 'Booking failed.', 'error');
            }
        } catch (err) {
            console.error('Booking error:', err);
            showToast('Network error during booking.', 'error');
        }
    }

    // ==========================================
    // 3. MY REGISTRATIONS
    // ==========================================
    async function loadMyRegistrations() {
        if (!currentStudentId || !myRegistrationsContainer) return;

        try {
            const res = await fetch(`/api/registrations/student/${currentStudentId}`);
            const regs = await res.json();
            if (badgeMySeats) badgeMySeats.textContent = regs.length;

            if (!regs || regs.length === 0) {
                myRegistrationsContainer.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path></svg>
                        </div>
                        <div class="empty-state-title">No Booked Seats</div>
                        <div class="empty-state-desc">${getActiveStudentName()} has not registered for any events yet. Browse Upcoming Events to book!</div>
                    </div>`;
                return;
            }

            myRegistrationsContainer.innerHTML = regs.map(reg => {
                const dateStr = reg.eventDate ? new Date(reg.eventDate).toLocaleDateString() : '';
                const statusClass = reg.seatStatus.toLowerCase();
                const isConfirmed = reg.seatStatus === 'CONFIRMED';
                const isListed = reg.seatStatus === 'AVAILABLE_FOR_TRANSFER';
                const isTransferred = reg.seatStatus === 'TRANSFERRED';

                return `
                    <div class="item-card">
                        <div class="item-info">
                            <div class="item-title">${escapeHtml(reg.eventName)}</div>
                            <div class="item-subtext">
                                <span>📅 ${dateStr} at ${reg.eventTime || ''}</span>
                                <span>📍 ${escapeHtml(reg.venue || '')}</span>
                                <span>Reg ID: #${reg.registrationId}</span>
                            </div>
                        </div>
                        <div class="item-actions">
                            <span class="badge-status ${statusClass}">${reg.seatStatus}</span>
                            ${isConfirmed ? `
                                <button class="btn btn-warning btn-sm btn-list-transfer" data-reg-id="${reg.registrationId}" title="List this seat on the exchange marketplace">
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4"></path></svg>
                                    <span>List for Transfer</span>
                                </button>
                            ` : ''}
                            ${isListed ? `
                                <button class="btn btn-secondary btn-sm btn-cancel-listing" data-reg-id="${reg.registrationId}" title="Withdraw seat from exchange marketplace">
                                    <span>Withdraw Listing</span>
                                </button>
                            ` : ''}
                            ${isTransferred ? `
                                <span class="text-muted" style="font-size: 0.8rem;">Transferred to peer</span>
                            ` : ''}
                        </div>
                    </div>
                `;
            }).join('');

            // Action listeners
            document.querySelectorAll('.btn-list-transfer').forEach(btn => {
                btn.addEventListener('click', () => {
                    const regId = parseInt(btn.getAttribute('data-reg-id'), 10);
                    handleListSeatForTransfer(regId);
                });
            });

            document.querySelectorAll('.btn-cancel-listing').forEach(btn => {
                btn.addEventListener('click', () => {
                    const regId = parseInt(btn.getAttribute('data-reg-id'), 10);
                    handleCancelSeatListing(regId);
                });
            });

        } catch (err) {
            console.error('My registrations error:', err);
        }
    }

    async function handleListSeatForTransfer(registrationId) {
        if (!currentStudentId) return;

        try {
            const res = await fetch(`/api/registrations/${registrationId}/list-transfer`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId: currentStudentId })
            });
            const data = await res.json();
            if (data.success) {
                showToast('Seat listed on Exchange Marketplace!', 'success');
                refreshAllData();
            } else {
                showToast(data.message || 'Failed to list seat.', 'error');
            }
        } catch (err) {
            console.error('List seat error:', err);
            showToast('Network error while listing seat.', 'error');
        }
    }

    async function handleCancelSeatListing(registrationId) {
        if (!currentStudentId) return;

        try {
            const res = await fetch(`/api/registrations/${registrationId}/cancel-listing`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId: currentStudentId })
            });
            const data = await res.json();
            if (data.success) {
                showToast('Seat withdrawn from exchange. Status is now CONFIRMED.', 'info');
                refreshAllData();
            } else {
                showToast(data.message || 'Action failed.', 'error');
            }
        } catch (err) {
            console.error('Cancel listing error:', err);
        }
    }

    // ==========================================
    // 4. SEAT EXCHANGE MARKETPLACE
    // ==========================================
    async function loadMarketplace() {
        if (!marketplaceContainer) return;

        try {
            const res = await fetch('/api/transfers/marketplace');
            const seats = await res.json();
            if (badgeMarketplace) badgeMarketplace.textContent = seats.length;

            if (!seats || seats.length === 0) {
                marketplaceContainer.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><path d="m4.93 4.93 4.24 4.24M14.83 9.17l4.24-4.24M14.83 14.83l4.24 4.24M9.17 14.83l-4.24 4.24"></path></svg>
                        </div>
                        <div class="empty-state-title">No Seats Available for Transfer</div>
                        <div class="empty-state-desc">Currently no students have listed seats for exchange. Check back later or book direct seats!</div>
                    </div>`;
                return;
            }

            const isAdmin = currentUser && currentUser.role === 'ADMIN';

            marketplaceContainer.innerHTML = seats.map(s => {
                const isOwnSeat = currentStudentId && s.studentId === currentStudentId;
                const dateStr = s.eventDate ? new Date(s.eventDate).toLocaleDateString() : '';

                let reqBtnText = 'Request Transfer';
                let reqDisabled = false;

                if (isOwnSeat) {
                    reqBtnText = 'Your Listed Seat';
                    reqDisabled = true;
                } else if (isAdmin) {
                    reqBtnText = 'Admin View';
                    reqDisabled = true;
                }

                return `
                    <div class="item-card">
                        <div class="item-info">
                            <div class="item-title">${escapeHtml(s.eventName)}</div>
                            <div class="item-subtext">
                                <span>📅 ${dateStr} at ${s.eventTime || ''}</span>
                                <span>📍 ${escapeHtml(s.venue || '')}</span>
                                <span>Listed By: <strong>${escapeHtml(s.studentName)}</strong> (${escapeHtml(s.department)})</span>
                            </div>
                        </div>
                        <div class="item-actions">
                            <span class="badge-status available_for_transfer">Available</span>
                            <button class="btn btn-primary btn-sm btn-request-seat" 
                                    data-reg-id="${s.registrationId}" 
                                    data-event-name="${escapeHtml(s.eventName)}"
                                    ${reqDisabled ? 'disabled' : ''}>
                                ${reqBtnText}
                            </button>
                        </div>
                    </div>
                `;
            }).join('');

            document.querySelectorAll('.btn-request-seat').forEach(btn => {
                btn.addEventListener('click', () => {
                    if (!currentStudentId) {
                        showToast('Please sign in as a student to request seats.', 'error');
                        return;
                    }
                    const regId = parseInt(btn.getAttribute('data-reg-id'), 10);
                    const eventName = btn.getAttribute('data-event-name');
                    handleRequestSeatTransfer(regId, eventName);
                });
            });

        } catch (err) {
            console.error('Marketplace error:', err);
        }
    }

    async function handleRequestSeatTransfer(registrationId, eventName) {
        if (!currentStudentId) return;

        try {
            const res = await fetch('/api/transfers/request', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ registrationId, studentId: currentStudentId })
            });
            const data = await res.json();
            if (data.success) {
                showToast('Seat transfer request submitted! Waiting for owner approval.', 'success');
                refreshAllData();
            } else {
                showToast(data.message || 'Request failed.', 'error');
            }
        } catch (err) {
            console.error('Request transfer error:', err);
            showToast('Network error while requesting transfer.', 'error');
        }
    }

    // ==========================================
    // 5. INCOMING TRANSFER REQUESTS (APPROVALS)
    // ==========================================
    async function loadIncomingRequests() {
        if (!currentStudentId || !incomingContainer) return;

        try {
            const res = await fetch(`/api/transfers/incoming/${currentStudentId}`);
            const requests = await res.json();
            
            const pendingRequests = requests.filter(r => r.transferStatus === 'PENDING');
            if (badgeIncoming) {
                badgeIncoming.textContent = pendingRequests.length;
                if (pendingRequests.length > 0) {
                    badgeIncoming.classList.add('badge-pulse');
                } else {
                    badgeIncoming.classList.remove('badge-pulse');
                }
            }

            if (!requests || requests.length === 0) {
                incomingContainer.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-6l-2 3h-4l-2-3H2"></path></svg>
                        </div>
                        <div class="empty-state-title">No Incoming Requests</div>
                        <div class="empty-state-desc">No peers have requested seats listed by ${getActiveStudentName()}.</div>
                    </div>`;
                return;
            }

            incomingContainer.innerHTML = requests.map(req => {
                const isPending = req.transferStatus === 'PENDING';
                const statusClass = req.transferStatus.toLowerCase();
                const reqTime = req.requestTime ? new Date(req.requestTime).toLocaleString() : '';

                return `
                    <div class="item-card">
                        <div class="item-info">
                            <div class="item-title">${escapeHtml(req.eventName)}</div>
                            <div class="item-subtext">
                                <span>Requested by: <strong>${escapeHtml(req.requesterName)}</strong> (${escapeHtml(req.requesterDepartment || '')})</span>
                                <span>Email: ${escapeHtml(req.requesterEmail || '')}</span>
                                <span>Time: ${reqTime}</span>
                            </div>
                        </div>
                        <div class="item-actions">
                            <span class="badge-status ${statusClass}">${req.transferStatus}</span>
                            ${isPending ? `
                                <button class="btn btn-success btn-sm btn-approve-transfer" data-transfer-id="${req.transferId}">
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>
                                    <span>Approve</span>
                                </button>
                                <button class="btn btn-danger btn-sm btn-reject-transfer" data-transfer-id="${req.transferId}">
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                                    <span>Reject</span>
                                </button>
                            ` : ''}
                        </div>
                    </div>
                `;
            }).join('');

            document.querySelectorAll('.btn-approve-transfer').forEach(btn => {
                btn.addEventListener('click', () => {
                    const id = parseInt(btn.getAttribute('data-transfer-id'), 10);
                    handleApproveTransfer(id);
                });
            });

            document.querySelectorAll('.btn-reject-transfer').forEach(btn => {
                btn.addEventListener('click', () => {
                    const id = parseInt(btn.getAttribute('data-transfer-id'), 10);
                    handleRejectTransfer(id);
                });
            });

        } catch (err) {
            console.error('Incoming requests error:', err);
        }
    }

    async function handleApproveTransfer(transferId) {
        if (!currentStudentId) return;

        try {
            const res = await fetch(`/api/transfers/${transferId}/approve`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId: currentStudentId })
            });
            const data = await res.json();
            if (data.success) {
                showToast('Seat transfer approved and logged successfully!', 'success');
                refreshAllData();
            } else {
                showToast(data.message || 'Approval failed.', 'error');
            }
        } catch (err) {
            console.error('Approve transfer error:', err);
            showToast('Network error during transfer approval.', 'error');
        }
    }

    async function handleRejectTransfer(transferId) {
        if (!currentStudentId) return;

        try {
            const res = await fetch(`/api/transfers/${transferId}/reject`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId: currentStudentId })
            });
            const data = await res.json();
            if (data.success) {
                showToast('Transfer request rejected.', 'info');
                refreshAllData();
            } else {
                showToast(data.message || 'Action failed.', 'error');
            }
        } catch (err) {
            console.error('Reject transfer error:', err);
        }
    }

    // ==========================================
    // 6. MY SENT REQUESTS
    // ==========================================
    async function loadOutgoingRequests() {
        if (!currentStudentId || !outgoingContainer) return;

        try {
            const res = await fetch(`/api/transfers/outgoing/${currentStudentId}`);
            const requests = await res.json();
            if (badgeOutgoing) badgeOutgoing.textContent = requests.length;

            if (!requests || requests.length === 0) {
                outgoingContainer.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
                        </div>
                        <div class="empty-state-title">No Sent Requests</div>
                        <div class="empty-state-desc">You have not requested any seats from other students.</div>
                    </div>`;
                return;
            }

            outgoingContainer.innerHTML = requests.map(req => {
                const isPending = req.transferStatus === 'PENDING';
                const statusClass = req.transferStatus.toLowerCase();
                const reqTime = req.requestTime ? new Date(req.requestTime).toLocaleString() : '';

                return `
                    <div class="item-card">
                        <div class="item-info">
                            <div class="item-title">${escapeHtml(req.eventName)}</div>
                            <div class="item-subtext">
                                <span>Current Owner: <strong>${escapeHtml(req.currentOwnerName)}</strong></span>
                                <span>Requested On: ${reqTime}</span>
                            </div>
                        </div>
                        <div class="item-actions">
                            <span class="badge-status ${statusClass}">${req.transferStatus}</span>
                            ${isPending ? `
                                <button class="btn btn-secondary btn-sm btn-cancel-req" data-transfer-id="${req.transferId}">
                                    Cancel Request
                                </button>
                            ` : ''}
                        </div>
                    </div>
                `;
            }).join('');

            document.querySelectorAll('.btn-cancel-req').forEach(btn => {
                btn.addEventListener('click', () => {
                    const id = parseInt(btn.getAttribute('data-transfer-id'), 10);
                    handleCancelOutgoingRequest(id);
                });
            });

        } catch (err) {
            console.error('Outgoing requests error:', err);
        }
    }

    async function handleCancelOutgoingRequest(transferId) {
        if (!currentStudentId) return;

        try {
            const res = await fetch(`/api/transfers/${transferId}/cancel`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId: currentStudentId })
            });
            const data = await res.json();
            if (data.success) {
                showToast('Request cancelled.', 'info');
                refreshAllData();
            } else {
                showToast(data.message || 'Action failed.', 'error');
            }
        } catch (err) {
            console.error('Cancel request error:', err);
        }
    }

    // ==========================================
    // 7. AUDIT HISTORY
    // ==========================================
    async function loadHistory() {
        if (!historyTableBody) return;

        try {
            const res = await fetch('/api/history');
            const history = await res.json();

            if (!history || history.length === 0) {
                historyTableBody.innerHTML = `
                    <tr>
                        <td colspan="6" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                            No transfer history recorded yet.
                        </td>
                    </tr>`;
                return;
            }

            historyTableBody.innerHTML = history.map(h => {
                const timeStr = h.transferTime ? new Date(h.transferTime).toLocaleString() : '';

                return `
                    <tr>
                        <td><strong>#${h.historyId}</strong></td>
                        <td>
                            <div style="font-weight: 600;">${escapeHtml(h.eventName)}</div>
                            <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(h.venue || '')}</div>
                        </td>
                        <td>
                            <div>${escapeHtml(h.previousStudentName)}</div>
                            <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(h.previousDepartment || '')}</div>
                        </td>
                        <td>
                            <div style="color: var(--accent-emerald); font-weight: 600;">${escapeHtml(h.newStudentName)}</div>
                            <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(h.newDepartment || '')}</div>
                        </td>
                        <td>${timeStr}</td>
                        <td><span class="badge-status approved">Completed</span></td>
                    </tr>
                `;
            }).join('');

        } catch (err) {
            console.error('History error:', err);
        }
    }

    // ==========================================
    // 8. EVENT CREATION FORM (ADMIN ONLY)
    // ==========================================
    function setupCreateEventForm() {
        if (!createEventForm) return;

        createEventForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('event-name-input').value.trim();
            const date = document.getElementById('event-date-input').value;
            const time = document.getElementById('event-time-input').value;
            const venue = document.getElementById('event-venue-input').value.trim();
            const totalSeats = parseInt(document.getElementById('event-total-seats-input').value, 10);
            const deadline = document.getElementById('event-deadline-input').value;

            try {
                const res = await fetch('/api/events', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        eventName: name,
                        eventDate: date,
                        eventTime: time + (time.length === 5 ? ':00' : ''),
                        venue: venue,
                        totalSeats: totalSeats,
                        availableSeats: totalSeats,
                        registrationDeadline: deadline
                    })
                });
                const data = await res.json();
                if (data.success) {
                    showToast(`Event "${name}" published successfully!`, 'success');
                    createEventForm.reset();
                    // Switch to events tab
                    if (navTabEvents) navTabEvents.click();
                    loadEvents();
                    loadStats();
                } else {
                    showToast(data.message || 'Failed to create event.', 'error');
                }
            } catch (err) {
                console.error('Event creation error:', err);
                showToast('Network error while creating event.', 'error');
            }
        });
    }

    // ==========================================
    // 9. SEARCH FILTERING
    // ==========================================
    function setupSearch() {
        if (eventsSearchInput) {
            eventsSearchInput.addEventListener('input', (e) => {
                const term = e.target.value.toLowerCase().trim();
                if (!term) {
                    renderEvents(events);
                    return;
                }
                const filtered = events.filter(ev =>
                    ev.eventName.toLowerCase().includes(term) ||
                    (ev.venue && ev.venue.toLowerCase().includes(term))
                );
                renderEvents(filtered);
            });
        }
    }

    // ==========================================
    // 10. TOAST NOTIFICATIONS
    // ==========================================
    function showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        let iconSvg = '';
        if (type === 'success') {
            iconSvg = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>`;
        } else if (type === 'error') {
            iconSvg = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#f43f5e" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>`;
        } else {
            iconSvg = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#6366f1" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`;
        }

        toast.innerHTML = `${iconSvg}<span>${escapeHtml(message)}</span>`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
});
