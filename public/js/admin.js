/**
 * CODING BATTLE - DEDICATED ADMINISTRATOR COMMAND PORTAL CONTROLLER
 * Standalone portal exclusively for Faculty, Evaluators, and Super Administrators.
 */

class AdminPortalApp {
    constructor() {
        try {
            const rawAdmin = JSON.parse(localStorage.getItem('cb_admin_user'));
            this.currentUser = rawAdmin ? (rawAdmin.user || rawAdmin) : null;
            if (!this.currentUser) {
                const generalUser = JSON.parse(localStorage.getItem('cb_user'));
                if (generalUser && (generalUser.role === 'ADMIN' || (generalUser.user && generalUser.user.role === 'ADMIN'))) {
                    this.currentUser = generalUser.user || generalUser;
                }
            }
        } catch (e) {
            this.currentUser = null;
        }

        this.cachedStudents = [];
        this.cachedProblems = [];
        this.cachedAssignments = [];
        this.cachedSubmissions = [];
        this.cachedAnnouncements = [];
        this.cachedPlagiarism = [];
        this.cachedCurriculum = [];
        this.cachedSandbox = null;
        this.cachedAnalytics = null;
        this.currentStreakFilter = 'ALL';
        this.isHonorRollOnly = false;
        this.currentAssignFilter = 'ALL';
        this.assignSearchQuery = '';
        this.activeRosterData = null;
        this.activeAssignmentId = null;
        this.currentRosterFilter = 'ALL';
        this.rosterSearchQuery = '';

        this.init();
    }

    init() {
        this.setupNavigation();
        this.setupAuth();
        this.setupProblemCreator();
        this.setupAssignmentCreator();
        this.setupModalAssignmentCreator();
        this.setupAnnouncementBroadcast();
        this.setupModalDismissal();

        if (this.currentUser && this.currentUser.role === 'ADMIN') {
            this.showPortal();
            this.loadAllData();
        } else {
            // Strictly ONE login page across the entire application: redirect directly to unified login
            window.location.href = '/?role=faculty';
        }
    }

    showLogin() {
        window.location.href = '/?role=faculty';
    }

    showPortal() {
        document.getElementById('admin-login-view').style.display = 'none';
        document.getElementById('admin-portal-view').style.display = 'flex';

        if (this.currentUser) {
            const nameEl = document.getElementById('admin-sidebar-name');
            if (nameEl) nameEl.textContent = this.currentUser.fullName || 'Prof. Alexander Wright';
        }
    }

    setupAuth() {
        const form = document.getElementById('admin-login-form');
        const alertBox = document.getElementById('admin-login-alert');

        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                const username = document.getElementById('admin-input-user').value.trim();
                const password = document.getElementById('admin-input-pass').value.trim();

                try {
                    const res = await fetch('/api/auth/login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ username, password })
                    });
                    const data = await res.json();
                    const user = data.user || (data.userId ? data : null);

                    if (res.ok && user) {
                        if (user.role !== 'ADMIN') {
                            alertBox.style.display = 'block';
                            alertBox.textContent = '⛔ Access Denied: This portal is strictly restricted to Faculty and Chief Evaluators. Please sign in via the Student Arena.';
                            return;
                        }

                        this.currentUser = user;
                        localStorage.setItem('cb_admin_user', JSON.stringify(user));
                        localStorage.setItem('cb_user', JSON.stringify(user));
                        alertBox.style.display = 'none';
                        this.showPortal();
                        this.loadAllData();
                    } else {
                        alertBox.style.display = 'block';
                        alertBox.textContent = '❌ ' + (data.error || 'Invalid credentials');
                    }
                } catch (err) {
                    alertBox.style.display = 'block';
                    alertBox.textContent = '⚠️ Network error: ' + err.message;
                }
            });
        }

        const quickAdminBtn = document.getElementById('btn-quick-fill-admin');
        if (quickAdminBtn) {
            quickAdminBtn.addEventListener('click', () => {
                const userInp = document.getElementById('admin-input-user');
                const passInp = document.getElementById('admin-input-pass');
                if (userInp) userInp.value = 'admin';
                if (passInp) passInp.value = 'admin123';
                if (form) {
                    form.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
                }
            });
        }

        const logoutBtn = document.getElementById('admin-btn-logout');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                sessionStorage.removeItem('cb_session_active');
                localStorage.removeItem('cb_admin_user');
                localStorage.removeItem('cb_user');
                this.currentUser = null;
                this.showLogin();
            });
        }
    }

    setupNavigation() {
        const navBtns = document.querySelectorAll('.admin-nav-btn');
        navBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetView = btn.getAttribute('data-view');
                if (!targetView) return;

                // Sync sidebar navigation item active state
                document.querySelectorAll('.admin-sidebar .admin-nav-item').forEach(b => {
                    b.classList.toggle('active', b.getAttribute('data-view') === targetView);
                });

                document.querySelectorAll('.admin-view-panel').forEach(p => p.classList.remove('active'));
                const panel = document.getElementById(`panel-${targetView}`);
                if (panel) panel.classList.add('active');

                const mainWrap = document.querySelector('.admin-main-wrap');
                if (mainWrap) mainWrap.scrollTop = 0;

                if (targetView === 'assignments') {
                    this.loadAssignments();
                } else if (targetView === 'students') {
                    this.renderStudents(this.cachedStudents);
                } else if (targetView === 'submissions') {
                    this.renderSubmissions(this.cachedSubmissions);
                } else if (targetView === 'plagiarism') {
                    this.loadPlagiarismData();
                }
            });
        });

        // Plagiarism Risk Filter
        const plagFilter = document.getElementById('plagiarism-risk-filter');
        if (plagFilter) {
            plagFilter.addEventListener('change', () => {
                const r = plagFilter.value;
                let list = this.cachedPlagiarism || [];
                if (r !== 'ALL') {
                    list = list.filter(p => p.riskLevel === r);
                }
                this.renderPlagiarism(list);
            });
        }

        // Student Filters & Dynamic Search
        const searchInput = document.getElementById('admin-student-search');
        const deptFilter = document.getElementById('admin-dept-filter');

        if (searchInput) searchInput.addEventListener('input', () => this.applyAllStudentFilters());
        if (deptFilter) deptFilter.addEventListener('change', () => this.applyAllStudentFilters());

        // Submissions Verdict Filter
        const subFilter = document.getElementById('admin-subs-filter');
        if (subFilter) {
            subFilter.addEventListener('change', () => {
                const v = subFilter.value;
                let list = this.cachedSubmissions || [];
                if (v !== 'ALL') {
                    list = list.filter(s => s.status === v);
                }
                this.renderSubmissions(list);
            });
        }
    }

    async loadAllData() {
        try {
            const [sRes, overRes, subRes, probRes, annRes, assignRes, plagRes, currRes, sandRes, analyticsRes] = await Promise.all([
                fetch('/api/admin/students'),
                fetch('/api/admin/overview'),
                fetch('/api/admin/submissions'),
                fetch('/api/problems'),
                fetch('/api/announcements'),
                fetch('/api/assignments'),
                fetch('/api/admin/plagiarism'),
                fetch('/api/admin/curriculum-stats'),
                fetch('/api/admin/sandbox-health'),
                fetch('/api/admin/engagement-analytics')
            ]);

            const students = await sRes.json();
            const overview = await overRes.json();
            const subs = await subRes.json();
            const problems = await probRes.json();
            const announcements = await annRes.json();
            const assignments = await assignRes.json();
            const plagiarism = await plagRes.json();
            const curriculum = await currRes.json();
            const sandbox = await sandRes.json();
            const analytics = await analyticsRes.json();

            this.cachedStudents = students;
            this.cachedSubmissions = subs;
            this.cachedProblems = problems;
            this.cachedAnnouncements = announcements;
            this.cachedAssignments = assignments;
            this.cachedPlagiarism = plagiarism;
            this.cachedCurriculum = curriculum;
            this.cachedSandbox = sandbox;
            this.cachedAnalytics = analytics;

            // Update KPI cards
            const elStudents = document.getElementById('kpi-students');
            const elCoding = document.getElementById('kpi-coding');
            const elSubs = document.getElementById('kpi-subs');
            const elPass = document.getElementById('kpi-pass');

            if (elStudents) elStudents.textContent = overview.totalStudents || students.length;
            if (elCoding) elCoding.textContent = `${overview.activeCodingCount || 3} Active`;
            if (elSubs) elSubs.textContent = `${overview.totalSubmissions || subs.length}+`;
            if (elPass) elPass.textContent = `${overview.averagePassRate || 82}%`;

            // Populate Problem Select for Assignment & Challenge forms
            this.populateProblemSelects(problems);

            // Render components
            this.renderStudents(students);
            this.renderEngagementAnalytics(analytics);
            this.renderAssignments(assignments);
            this.renderSubmissions(subs);
            this.renderAnnouncements(announcements);
            this.renderCurriculum(curriculum);
            this.renderSandboxHealth(sandbox);
            this.renderPlagiarism(plagiarism);

            // Live Student Moments Surveillance Stream
            await this.loadStudentMoments();
            if (!this.momentsPolling) {
                this.momentsPolling = setInterval(() => this.loadStudentMoments(), 3500);
            }

        } catch (e) {
            console.error('Failed to load admin telemetry:', e);
        }
    }

    populateProblemSelects(problems) {
        this.cachedProblems = problems || [];
        const select = document.getElementById('admin-assign-problems-select');
        if (!select) return;

        select.innerHTML = '';
        problems.forEach((p, idx) => {
            const opt = document.createElement('option');
            opt.value = p.problemId;
            opt.textContent = `#${p.problemId} - ${p.title} (${p.difficulty} • ${p.points} pts)`;
            // Pre-select problems 9 and 10 by default
            if (p.problemId === 9 || p.problemId === 10 || idx < 2) {
                opt.selected = true;
            }
            select.appendChild(opt);
        });

        // Set default due date to 5 days ahead if not populated
        const dueInput = document.getElementById('admin-assign-due');
        if (dueInput && !dueInput.value) {
            const d = new Date(Date.now() + 5 * 86400000);
            const tzOffset = d.getTimezoneOffset() * 60000;
            dueInput.value = (new Date(d - tzOffset)).toISOString().slice(0, 16);
        }

        this.updateSelectedProblemsPreview();
        this.populateModalProblemSelects(problems);
    }

    updateSelectedProblemsPreview() {
        const select = document.getElementById('admin-assign-problems-select');
        const container = document.getElementById('admin-assign-problems-preview');
        const countBadge = document.getElementById('admin-assign-selected-count');
        if (!select || !container) return;

        const selectedIds = Array.from(select.selectedOptions).map(opt => parseInt(opt.value));
        const matched = (this.cachedProblems || []).filter(p => selectedIds.includes(p.problemId));

        const totalPoints = matched.reduce((sum, p) => sum + (p.points || 0), 0);
        if (countBadge) {
            countBadge.textContent = `${matched.length} Challenges Selected • Total ${totalPoints} Points`;
        }

        container.innerHTML = '';
        if (matched.length === 0) {
            container.innerHTML = '<span style="font-size: 11.5px; color: var(--text-muted); font-style: italic;">No problems selected. Please select at least one challenge.</span>';
            return;
        }

        matched.forEach(p => {
            let diffClass = 'badge-easy';
            if (p.difficulty === 'MEDIUM') diffClass = 'badge-medium';
            if (p.difficulty === 'HARD') diffClass = 'badge-hard';

            const chip = document.createElement('div');
            chip.style.cssText = 'display: inline-flex; align-items: center; gap: 8px; padding: 4px 10px; background: rgba(15, 23, 42, 0.8); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 8px; font-size: 12px;';
            chip.innerHTML = `
                <span style="font-weight: 700; color: #fff;">#${p.problemId} ${p.title}</span>
                <span class="badge ${diffClass}" style="font-size: 9.5px; padding: 2px 6px;">${p.difficulty}</span>
                <span style="font-size: 11px; color: #38bdf8; font-family: var(--font-code);">+${p.points}pts</span>
                <button type="button" style="background: none; border: none; color: #94a3b8; cursor: pointer; padding: 0 2px; font-size: 14px; line-height: 1;" onclick="adminApp.removeProblemFromAssignment(${p.problemId})" title="Remove">×</button>
            `;
            container.appendChild(chip);
        });
    }

    removeProblemFromAssignment(problemId) {
        const select = document.getElementById('admin-assign-problems-select');
        if (!select) return;
        Array.from(select.options).forEach(opt => {
            if (parseInt(opt.value) === problemId) opt.selected = false;
        });
        this.updateSelectedProblemsPreview();
    }

    setDueDateOffset(days) {
        const dueInput = document.getElementById('admin-assign-due');
        if (dueInput) {
            const d = new Date(Date.now() + days * 86400000);
            const tzOffset = d.getTimezoneOffset() * 60000;
            dueInput.value = (new Date(d - tzOffset)).toISOString().slice(0, 16);
            this.showToast(`Deadline set to ${days} day(s) from now`, 'info');
        }
    }

    fillAdminPreset(key) {
        const titleEl = document.getElementById('admin-assign-title');
        const typeEl = document.getElementById('admin-assign-type');
        const durationEl = document.getElementById('admin-assign-duration');
        const marksEl = document.getElementById('admin-assign-marks');
        const instructionsEl = document.getElementById('admin-assign-instructions');
        const select = document.getElementById('admin-assign-problems-select');

        let targetIds = [9, 10];
        let presetTitle = '';

        if (key === 'MIDTERM_EXAM') {
            presetTitle = 'Core Java Midterm Practical Examination';
            if (titleEl) titleEl.value = 'Core Java Midterm Practical Examination: Algorithms & Data Structures';
            if (typeEl) typeEl.value = 'EXAM';
            if (durationEl) durationEl.value = 45;
            if (marksEl) marksEl.value = 100;
            if (instructionsEl) instructionsEl.value = '1. Strict fullscreen focus monitoring active.\n2. No external libraries or clipboard paste.\n3. Solutions must pass all 4 hidden test suites under 2000ms.\n4. Automated AST Plagiarism Radar scans all submitted code.';
            targetIds = [9, 10];
            this.setDueDateOffset(5);
        } else if (key === 'OOP_LAB') {
            presetTitle = 'Unit 1: Classes & Objects Lab Practical';
            if (titleEl) titleEl.value = 'Unit 1: Classes, Constructors & Encapsulation Practical Lab';
            if (typeEl) typeEl.value = 'ASSIGNMENT';
            if (durationEl) durationEl.value = 60;
            if (marksEl) marksEl.value = 50;
            if (instructionsEl) instructionsEl.value = 'Implement classes with proper encapsulation. Handle boundary conditions and formatted console I/O matching the spec.';
            targetIds = [14, 15];
            this.setDueDateOffset(7);
        } else if (key === 'SPEED_QUIZ') {
            presetTitle = 'Unit 3: String Manipulations & Pattern Matching Quiz';
            if (titleEl) titleEl.value = 'Unit 3: Fast String Manipulations & Palindrome Detection Sprint';
            if (typeEl) typeEl.value = 'QUIZ';
            if (durationEl) durationEl.value = 20;
            if (marksEl) marksEl.value = 40;
            if (instructionsEl) instructionsEl.value = 'Rapid-fire algorithmic sprint. Scores prioritized by execution time and memory footprint.';
            targetIds = [19, 20];
            this.setDueDateOffset(3);
        } else if (key === 'FINAL_EXAM') {
            presetTitle = 'Comprehensive End-Semester Java Practical Evaluation';
            if (titleEl) titleEl.value = 'Comprehensive End-Semester Practical Lab Evaluation (Units 1 - 5)';
            if (typeEl) typeEl.value = 'EXAM';
            if (durationEl) durationEl.value = 120;
            if (marksEl) marksEl.value = 150;
            if (instructionsEl) instructionsEl.value = 'Comprehensive practical exam covering OOP design, array manipulation, string algorithms, and JDBC formatting.';
            targetIds = [9, 14, 19, 28];
            this.setDueDateOffset(10);
        }

        if (select) {
            Array.from(select.options).forEach(opt => {
                opt.selected = targetIds.includes(parseInt(opt.value));
            });
            this.updateSelectedProblemsPreview();
        }

        this.showToast(`Loaded template: "${presetTitle}"`, 'info');
    }

    // =========================================================================
    // EXAMINATIONS & ASSIGNMENT MANAGEMENT
    // =========================================================================

    setupAssignmentCreator() {
        const form = document.getElementById('admin-create-assignment-form');
        const alertBox = document.getElementById('admin-assign-alert');

        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                const title = document.getElementById('admin-assign-title')?.value;
                const type = document.getElementById('admin-assign-type')?.value;
                const target = document.getElementById('admin-assign-target')?.value;
                const due = document.getElementById('admin-assign-due')?.value;
                const duration = document.getElementById('admin-assign-duration')?.value;
                const marks = document.getElementById('admin-assign-marks')?.value;
                const instructions = document.getElementById('admin-assign-instructions')?.value;

                const problemSelect = document.getElementById('admin-assign-problems-select');
                let selectedProblems = [];
                if (problemSelect) {
                    selectedProblems = Array.from(problemSelect.selectedOptions).map(opt => parseInt(opt.value));
                }
                if (selectedProblems.length === 0) selectedProblems = [9, 10];

                try {
                    const res = await fetch('/api/admin/assignments', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            title,
                            type,
                            assignedTo: target,
                            problemIds: selectedProblems,
                            dueDate: due,
                            durationMinutes: duration,
                            maxMarks: marks,
                            instructions
                        })
                    });
                    const data = await res.json();
                    if (data.success) {
                        alertBox.style.display = 'block';
                        alertBox.style.background = 'rgba(46, 160, 67, 0.18)';
                        alertBox.style.border = '1px solid rgba(46, 160, 67, 0.5)';
                        alertBox.style.color = '#56d364';
                        const count = data.dispatchedCount || 'all';
                        const recips = data.recipients ? data.recipients.join(', ') : 'every enrolled student';
                        alertBox.innerHTML = `
                            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px;">
                                <span style="font-size: 18px;">🚀</span>
                                <strong style="font-size: 14px; color: #ffffff;">DIRECT DISPATCH SUCCESSFUL — TELEMETRY BROADCAST ACTIVE!</strong>
                            </div>
                            <div style="font-size: 13px; color: #e2e8f0; line-height: 1.6;">
                                "<strong>${title}</strong>" has been directly dispatched to <strong>${count} students</strong> across campus: <em>${recips}</em>.
                            </div>
                            <div style="font-size: 12px; color: #a7f3d0; margin-top: 6px; display: flex; align-items: center; gap: 6px;">
                                <span class="chip-dot" style="background: #10b981;"></span>
                                <span>Real-time audio chime, top floating banner, and stopwatch countdown timer delivered to student arena dashboards.</span>
                            </div>
                        `;
                        setTimeout(() => { alertBox.style.display = 'none'; }, 8000);
                        this.showToast(`Dispatched "${title}" to ${count} students!`, 'success');
                        form.reset();
                        this.loadAssignments();
                    } else {
                        alert(data.error || 'Failed to create assignment');
                    }
                } catch (err) {
                    alert('Error: ' + err.message);
                }
            });
        }
    }

    openAddAssignmentModal() {
        const modal = document.getElementById('admin-add-assignment-modal');
        if (!modal) return;

        // Ensure problems are populated in modal
        if (this.cachedProblems && this.cachedProblems.length > 0) {
            this.populateModalProblemSelects(this.cachedProblems);
        } else {
            fetch('/api/problems')
                .then(r => r.json())
                .then(probs => {
                    this.cachedProblems = probs;
                    this.populateModalProblemSelects(probs);
                })
                .catch(() => {});
        }

        // Set default values if empty
        const titleEl = document.getElementById('modal-assign-title');
        if (titleEl && !titleEl.value) {
            this.fillModalPreset('OOP_LAB');
        }

        const dueEl = document.getElementById('modal-assign-due');
        if (dueEl && !dueEl.value) {
            this.setModalDueDateOffset(5);
        }

        modal.classList.add('active');
    }

    closeAddAssignmentModal() {
        const modal = document.getElementById('admin-add-assignment-modal');
        if (modal) modal.classList.remove('active');
    }

    populateModalProblemSelects(problems) {
        const select = document.getElementById('modal-assign-problems-select');
        if (!select) return;

        select.innerHTML = '';
        problems.forEach((p, idx) => {
            const opt = document.createElement('option');
            opt.value = p.problemId;
            opt.textContent = `#${p.problemId} - ${p.title} (${p.difficulty} • ${p.points} pts)`;
            if (p.problemId === 9 || p.problemId === 10 || idx < 2) {
                opt.selected = true;
            }
            select.appendChild(opt);
        });

        this.updateModalSelectedProblemsPreview();
    }

    updateModalSelectedProblemsPreview() {
        const select = document.getElementById('modal-assign-problems-select');
        const container = document.getElementById('modal-assign-problems-preview');
        const countBadge = document.getElementById('modal-assign-selected-count');
        if (!select || !container) return;

        const selectedIds = Array.from(select.selectedOptions).map(opt => parseInt(opt.value));
        const matched = (this.cachedProblems || []).filter(p => selectedIds.includes(p.problemId));

        const totalPoints = matched.reduce((sum, p) => sum + (p.points || 0), 0);
        if (countBadge) {
            countBadge.textContent = `${matched.length} Challenges Selected • Total ${totalPoints} Points`;
        }

        container.innerHTML = '';
        if (matched.length === 0) {
            container.innerHTML = '<span style="font-size: 11.5px; color: var(--text-muted); font-style: italic;">No problems selected. Please select at least one challenge.</span>';
            return;
        }

        matched.forEach(p => {
            const chip = document.createElement('div');
            chip.style.cssText = 'display: inline-flex; align-items: center; gap: 8px; padding: 4px 10px; background: rgba(15, 23, 42, 0.85); border: 1px solid rgba(168, 85, 247, 0.35); border-radius: 8px; font-size: 12px;';
            chip.innerHTML = `
                <span style="font-weight: 700; color: #ffffff;">#${p.problemId} ${p.title}</span>
                <span class="badge badge-purple" style="font-size: 9.5px; padding: 2px 6px;">${p.points || 50} pts</span>
            `;
            container.appendChild(chip);
        });
    }

    setModalDueDateOffset(days) {
        const dueInput = document.getElementById('modal-assign-due');
        if (!dueInput) return;
        const d = new Date(Date.now() + days * 86400000);
        const tzOffset = d.getTimezoneOffset() * 60000;
        dueInput.value = (new Date(d - tzOffset)).toISOString().slice(0, 16);
    }

    switchDirectiveMode(mode) {
        const btnAssign = document.getElementById('tab-directive-assignment');
        const btnTask = document.getElementById('tab-directive-task');
        const typeSelect = document.getElementById('modal-assign-type');
        const titleInput = document.getElementById('modal-assign-title');
        const prioritySelect = document.getElementById('modal-assign-priority');

        if (mode === 'TASK') {
            if (btnTask) btnTask.classList.add('active');
            if (btnAssign) btnAssign.classList.remove('active');
            if (typeSelect) typeSelect.value = 'TASK';
            if (prioritySelect) prioritySelect.value = 'URGENT';
            this.fillModalPreset('DAILY_TASK');
        } else {
            if (btnAssign) btnAssign.classList.add('active');
            if (btnTask) btnTask.classList.remove('active');
            if (typeSelect) typeSelect.value = 'ASSIGNMENT';
            if (prioritySelect) prioritySelect.value = 'HIGH';
            this.fillModalPreset('OOP_LAB');
        }
    }

    fillModalPreset(key) {
        const titleEl = document.getElementById('modal-assign-title');
        const typeEl = document.getElementById('modal-assign-type');
        const durationEl = document.getElementById('modal-assign-duration');
        const marksEl = document.getElementById('modal-assign-marks');
        const priorityEl = document.getElementById('modal-assign-priority');
        const instructionsEl = document.getElementById('modal-assign-instructions');
        const select = document.getElementById('modal-assign-problems-select');

        let presetTitle = '';
        let targetIds = [9, 10];

        if (key === 'MIDTERM_EXAM') {
            presetTitle = 'Core Java Midterm Practical Examination';
            if (titleEl) titleEl.value = 'Core Java Midterm Practical Examination: Algorithms & Data Structures';
            if (typeEl) typeEl.value = 'EXAM';
            if (durationEl) durationEl.value = 45;
            if (marksEl) marksEl.value = 100;
            if (priorityEl) priorityEl.value = 'URGENT';
            if (instructionsEl) instructionsEl.value = '1. Strict fullscreen focus monitoring active.\n2. No external libraries or clipboard paste.\n3. Solutions must pass all 4 hidden test suites under 2000ms.\n4. Automated AST Plagiarism Radar scans all submitted code.';
            targetIds = [9, 10];
            this.setModalDueDateOffset(5);
        } else if (key === 'OOP_LAB') {
            presetTitle = 'Unit 1: Classes & Objects Lab Practical';
            if (titleEl) titleEl.value = 'Unit 1: Classes, Constructors & Encapsulation Practical Lab';
            if (typeEl) typeEl.value = 'ASSIGNMENT';
            if (durationEl) durationEl.value = 60;
            if (marksEl) marksEl.value = 50;
            if (priorityEl) priorityEl.value = 'HIGH';
            if (instructionsEl) instructionsEl.value = 'Implement classes with proper encapsulation. Handle boundary conditions and formatted console I/O matching the spec.';
            targetIds = [14, 15];
            this.setModalDueDateOffset(7);
        } else if (key === 'DAILY_TASK') {
            presetTitle = 'Daily Student Task: Array Sorting & Algorithms Sprint';
            if (titleEl) titleEl.value = 'Daily Student Task: Array Sorting & Search Algorithms Sprint';
            if (typeEl) typeEl.value = 'TASK';
            if (durationEl) durationEl.value = 30;
            if (marksEl) marksEl.value = 50;
            if (priorityEl) priorityEl.value = 'URGENT';
            if (instructionsEl) instructionsEl.value = 'Complete the assigned daily challenge. Optimize for O(N log N) time complexity. Ensure edge cases with negative values and duplicates pass.';
            targetIds = [1, 2];
            this.setModalDueDateOffset(1);
        } else if (key === 'DEBUG_TASK') {
            presetTitle = 'Remedial Task: Custom Exception Handling & Resource Safety';
            if (titleEl) titleEl.value = 'Remedial Task: Custom Exception Handling & Resource Leak Prevention';
            if (typeEl) typeEl.value = 'DEBUG';
            if (durationEl) durationEl.value = 45;
            if (marksEl) marksEl.value = 75;
            if (priorityEl) priorityEl.value = 'URGENT';
            if (instructionsEl) instructionsEl.value = 'Identify potential null pointer and arithmetic overflow bugs. Wrap critical file I/O operations in try-with-resources blocks.';
            targetIds = [14, 15];
            this.setModalDueDateOffset(2);
        } else if (key === 'SPEED_QUIZ') {
            presetTitle = 'Unit 3: String Manipulations & Pattern Matching Quiz';
            if (titleEl) titleEl.value = 'Unit 3: Fast String Manipulations & Palindrome Detection Sprint';
            if (typeEl) typeEl.value = 'QUIZ';
            if (durationEl) durationEl.value = 20;
            if (marksEl) marksEl.value = 40;
            if (priorityEl) priorityEl.value = 'HIGH';
            if (instructionsEl) instructionsEl.value = 'Rapid-fire algorithmic sprint. Scores prioritized by execution time and memory footprint.';
            targetIds = [19, 20];
            this.setModalDueDateOffset(3);
        } else if (key === 'EXCEPTIONS_LAB') {
            presetTitle = 'Unit 3: Exception Handling & Multi-Threading Lab';
            if (titleEl) titleEl.value = 'Unit 3: Custom Exceptions, Try-Catch-Finally & Multi-Threading Practical';
            if (typeEl) typeEl.value = 'ASSIGNMENT';
            if (durationEl) durationEl.value = 90;
            if (marksEl) marksEl.value = 100;
            if (priorityEl) priorityEl.value = 'HIGH';
            if (instructionsEl) instructionsEl.value = 'Handle all runtime exceptions cleanly. Ensure thread safety and synchronized block execution where appropriate.';
            targetIds = [14, 19];
            this.setModalDueDateOffset(10);
        }

        if (select) {
            Array.from(select.options).forEach(opt => {
                opt.selected = targetIds.includes(parseInt(opt.value));
            });
            this.updateModalSelectedProblemsPreview();
        }

        this.showToast(`Loaded template: "${presetTitle}"`, 'info');
    }

    setupModalAssignmentCreator() {
        const form = document.getElementById('modal-create-assignment-form');
        const alertBox = document.getElementById('modal-assign-alert');

        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                const title = document.getElementById('modal-assign-title')?.value;
                const type = document.getElementById('modal-assign-type')?.value;
                const priority = document.getElementById('modal-assign-priority')?.value || 'HIGH';
                const target = document.getElementById('modal-assign-target')?.value;
                const due = document.getElementById('modal-assign-due')?.value;
                const duration = document.getElementById('modal-assign-duration')?.value;
                const marks = document.getElementById('modal-assign-marks')?.value;
                const instructions = document.getElementById('modal-assign-instructions')?.value;

                const problemSelect = document.getElementById('modal-assign-problems-select');
                let selectedProblems = [];
                if (problemSelect) {
                    selectedProblems = Array.from(problemSelect.selectedOptions).map(opt => parseInt(opt.value));
                }
                if (selectedProblems.length === 0) selectedProblems = [9, 10];

                try {
                    const submitBtn = form.querySelector('button[type="submit"]');
                    if (submitBtn) {
                        submitBtn.disabled = true;
                        submitBtn.textContent = 'Broadcasting... ⏳';
                    }

                    const res = await fetch('/api/admin/assignments', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            title,
                            type,
                            priority,
                            assignedTo: target,
                            problemIds: selectedProblems,
                            dueDate: due,
                            durationMinutes: duration,
                            maxMarks: marks,
                            instructions
                        })
                    });
                    const data = await res.json();

                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.innerHTML = '<span>🚀 Assign &amp; Broadcast to Student Dashboards</span>';
                    }

                    if (data.success) {
                        const count = data.dispatchedCount || 'all';
                        if (alertBox) {
                            alertBox.style.display = 'block';
                            alertBox.style.background = 'rgba(46, 160, 67, 0.18)';
                            alertBox.style.border = '1px solid rgba(46, 160, 67, 0.5)';
                            alertBox.style.color = '#56d364';
                            alertBox.innerHTML = `<strong>SUCCESS:</strong> "${title}" added and dispatched to ${count} students!`;
                        }
                        this.showToast(`✅ Dispatched "${title}" to ${count} students!`, 'success');
                        setTimeout(() => {
                            this.closeAddAssignmentModal();
                            if (alertBox) alertBox.style.display = 'none';
                            form.reset();
                        }, 1200);
                        this.loadAssignments();
                    } else {
                        alert(data.error || 'Failed to create assignment');
                    }
                } catch (err) {
                    alert('Error creating assignment: ' + err.message);
                }
            });
        }
    }

    async loadAssignments() {
        try {
            const res = await fetch('/api/assignments');
            const assignments = await res.json();
            this.cachedAssignments = assignments;
            this.renderAssignments(assignments);
        } catch (e) {
            console.error('Failed to load assignments:', e);
        }
    }

    filterAssignmentsByType(type) {
        this.currentAssignFilter = type;
        const tabs = ['all', 'exam', 'assignment', 'quiz'];
        tabs.forEach(t => {
            const btn = document.getElementById(`tab-assign-filter-${t}`);
            if (btn) {
                if (t === type.toLowerCase() || (type === 'ALL' && t === 'all')) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            }
        });
        this.renderAssignments(this.cachedAssignments || []);
    }

    searchAssignments(query) {
        this.assignSearchQuery = (query || '').toLowerCase().trim();
        this.renderAssignments(this.cachedAssignments || []);
    }

    renderAssignments(assignments) {
        const tbody = document.getElementById('admin-assignments-table-body');
        const badge = document.getElementById('admin-assignments-count-badge');
        if (!assignments) assignments = [];

        // 1. Calculate KPI Summary metrics
        const totalCount = assignments.length;
        const examCount = assignments.filter(a => a.type === 'EXAM').length;
        const assignmentCount = assignments.filter(a => a.type === 'ASSIGNMENT').length;
        const quizCount = assignments.filter(a => a.type === 'QUIZ').length;
        const totalSubs = assignments.reduce((acc, a) => acc + (a.submissionCount || 0), 0);

        if (badge) badge.textContent = `${totalCount} Assessments Active`;

        const kpiExamActive = document.getElementById('kpi-exam-active');
        const kpiExamAssignments = document.getElementById('kpi-exam-assignments');
        const kpiExamSubs = document.getElementById('kpi-exam-subs');
        const kpiExamTurnout = document.getElementById('kpi-exam-turnout');

        if (kpiExamActive) kpiExamActive.textContent = `${examCount} Active`;
        if (kpiExamAssignments) kpiExamAssignments.textContent = `${assignmentCount} Scheduled`;
        if (kpiExamSubs) kpiExamSubs.textContent = `${totalSubs} Audited`;

        // Turnout calculation across students
        const studentCount = this.cachedStudents && this.cachedStudents.length > 0 ? this.cachedStudents.length : 8;
        const expectedSubs = totalCount * studentCount;
        const turnoutPct = expectedSubs > 0 ? Math.round((totalSubs / expectedSubs) * 100) : 65;
        if (kpiExamTurnout) kpiExamTurnout.textContent = `${Math.min(100, Math.max(turnoutPct, 68))}% Avg`;

        // Update Tab Counts
        const cAll = document.getElementById('count-filter-all');
        const cExam = document.getElementById('count-filter-exam');
        const cAssign = document.getElementById('count-filter-assignment');
        const cQuiz = document.getElementById('count-filter-quiz');
        if (cAll) cAll.textContent = totalCount;
        if (cExam) cExam.textContent = examCount;
        if (cAssign) cAssign.textContent = assignmentCount;
        if (cQuiz) cQuiz.textContent = quizCount;

        if (!tbody) return;
        tbody.innerHTML = '';

        // 2. Filter by Type and Search Query
        let filtered = assignments;
        if (this.currentAssignFilter && this.currentAssignFilter !== 'ALL') {
            filtered = filtered.filter(a => a.type === this.currentAssignFilter);
        }
        if (this.assignSearchQuery) {
            filtered = filtered.filter(a => 
                (a.title && a.title.toLowerCase().includes(this.assignSearchQuery)) ||
                (a.description && a.description.toLowerCase().includes(this.assignSearchQuery)) ||
                (a.category && a.category.toLowerCase().includes(this.assignSearchQuery))
            );
        }

        if (filtered.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 24px;">No matching examinations or assignments found.</td></tr>';
            return;
        }

        filtered.forEach(a => {
            const tr = document.createElement('tr');
            const isExam = a.type === 'EXAM';
            const isQuiz = a.type === 'QUIZ';

            let typeBadge = '<span class="badge badge-blue" style="font-size: 10px; font-weight: 700;">📄 ASSIGNMENT</span>';
            if (isExam) {
                typeBadge = '<span class="badge badge-purple" style="font-size: 10px; font-weight: 700; box-shadow: 0 0 10px rgba(168, 85, 247, 0.4);">🎓 EXAM</span>';
            } else if (isQuiz) {
                typeBadge = '<span class="badge badge-gold" style="font-size: 10px; font-weight: 700; box-shadow: 0 0 10px rgba(245, 158, 11, 0.4);">⚡ QUIZ</span>';
            }

            // Challenge Chips
            const problemChips = (a.problems || []).map(p => {
                let dClass = 'badge-easy';
                if (p.difficulty === 'MEDIUM') dClass = 'badge-medium';
                if (p.difficulty === 'HARD') dClass = 'badge-hard';
                return `<span class="badge ${dClass}" style="font-size: 9.5px; padding: 2px 6px;">#${p.problemId} ${p.title}</span>`;
            }).join(' ') || `<span class="badge badge-medium" style="font-size: 9.5px;">${a.problemIds ? a.problemIds.length : 1} Challenges</span>`;

            // Deadline calculations
            const dueObj = new Date(a.dueDate);
            const now = new Date();
            const diffMs = dueObj - now;
            const diffDays = Math.ceil(diffMs / 86400000);
            let urgencyBadge = '<span class="badge badge-easy" style="font-size: 10px;">🟢 Active</span>';
            if (diffDays <= 0) {
                urgencyBadge = '<span class="badge badge-hard" style="font-size: 10px;">🔴 Closed / Past Due</span>';
            } else if (diffDays <= 2) {
                urgencyBadge = `<span class="badge badge-hard" style="font-size: 10px;">⚠️ Closes in ${diffDays}d</span>`;
            } else {
                urgencyBadge = `<span class="badge badge-easy" style="font-size: 10px;">🟢 Closes in ${diffDays}d</span>`;
            }

            // Completion Turnout Progress
            const completedCount = a.submissionCount || 0;
            const cohortSize = studentCount;
            const percentage = Math.min(100, Math.round((completedCount / cohortSize) * 100));

            tr.innerHTML = `
                <td>
                    <div style="display: flex; flex-direction: column; gap: 4px;">
                        <span style="font-family: var(--font-code); font-size: 12px; font-weight: 700; color: #fff;">#A${a.id}</span>
                        ${typeBadge}
                    </div>
                </td>
                <td>
                    <div style="font-weight: 700; color: #ffffff; font-size: 13.5px; margin-bottom: 3px;">${a.title}</div>
                    <div style="font-size: 11.5px; color: var(--text-secondary); max-width: 320px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">${a.description || 'Continuous academic assessment.'}</div>
                    <div style="margin-top: 4px;">
                        <span style="font-size: 9.5px; font-weight: 700; color: #94a3b8; background: rgba(255,255,255,0.06); padding: 2px 6px; border-radius: 4px; text-transform: uppercase;">${a.category || 'EXAMINATION'}</span>
                    </div>
                </td>
                <td>
                    <div style="display: flex; flex-direction: column; gap: 4px; max-width: 200px;">
                        ${problemChips}
                    </div>
                </td>
                <td>
                    <div style="font-size: 12px; color: #4ade80; font-weight: 600; display: flex; align-items: center; gap: 4px;">
                        <span>👥</span>
                        <span>${a.assignedToLabel || 'All Enrolled Students'}</span>
                    </div>
                    <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">${cohortSize} Monitored Coders</div>
                </td>
                <td>
                    <div style="font-size: 12px; font-weight: 700; color: #ffffff;">⏱️ ${a.durationMinutes} Mins</div>
                    <div style="font-size: 12px; color: #fbbf24; font-weight: 700; margin-top: 2px;">💯 ${a.maxMarks} Marks</div>
                </td>
                <td>
                    <div style="font-size: 12px; color: #f8fafc; font-weight: 600;">${dueObj.toLocaleDateString()}</div>
                    <div style="font-size: 11px; color: var(--text-muted);">${dueObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
                    <div style="margin-top: 4px;">${urgencyBadge}</div>
                </td>
                <td>
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                        <span style="font-size: 11.5px; font-weight: 700; color: #ffffff;">${completedCount} / ${cohortSize} Submitted</span>
                        <span style="font-size: 11px; font-weight: 700; color: #4ade80;">${percentage}%</span>
                    </div>
                    <div style="width: 100%; height: 6px; background: rgba(255, 255, 255, 0.1); border-radius: 3px; overflow: hidden;">
                        <div style="width: ${percentage}%; height: 100%; background: linear-gradient(90deg, #38bdf8 0%, #4ade80 100%);"></div>
                    </div>
                </td>
                <td style="text-align: right;">
                    <div style="display: flex; gap: 6px; justify-content: flex-end; align-items: center;">
                        <button class="btn btn-primary" onclick="adminApp.viewAssignmentRoster(${a.id})" style="font-size: 11px; padding: 5px 11px; font-weight: 700; display: inline-flex; align-items: center; gap: 4px; box-shadow: 0 0 12px rgba(99, 102, 241, 0.3);">
                            <span>📊</span> Grading Roster
                        </button>
                        <button class="btn btn-secondary" onclick="adminApp.pingAssignmentPending(${a.id})" style="font-size: 11px; padding: 5px 8px; color: #fbbf24;" title="Send Urgent Reminder to Pending Students">
                            📢
                        </button>
                        <button class="btn btn-danger" onclick="adminApp.deleteAssignment(${a.id})" style="font-size: 11px; padding: 5px 8px;" title="Revoke Examination">
                            🗑️
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // 3. Synchronize Overview Panel Table
        const overTbody = document.getElementById('overview-assignments-body');
        if (overTbody) {
            overTbody.innerHTML = '';
            if (assignments.length === 0) {
                overTbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 16px;">No active examinations or assignments scheduled.</td></tr>';
            } else {
                assignments.slice(0, 4).forEach(a => {
                    const isExam = a.type === 'EXAM';
                    const typeBadge = `<span class="badge ${isExam ? 'badge-purple' : 'badge-blue'}" style="font-size: 10px;">${isExam ? '🎓 EXAM' : '📄 ASSIGNMENT'}</span>`;
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td><span style="font-family: var(--font-code); font-size: 11px; color: var(--text-muted);">#A${a.id}</span></td>
                        <td>
                            <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 2px;">
                                ${typeBadge}
                                <strong style="color: #fff; font-size: 13px;">${a.title}</strong>
                            </div>
                            <div style="font-size: 11px; color: var(--text-secondary);">${a.problems ? a.problems.length : 1} Problems • ${a.maxMarks} Marks</div>
                        </td>
                        <td><span style="font-size: 11.5px; color: #4ade80;">👥 ${a.assignedToLabel || 'All Enrolled Students'}</span></td>
                        <td><span style="font-size: 11.5px; color: #fbbf24;">📅 ${new Date(a.dueDate).toLocaleDateString()}</span></td>
                        <td>
                            <span class="badge badge-gold" style="font-size: 10px;">${a.submissionCount || 0} / ${studentCount} Submitted</span>
                        </td>
                        <td style="text-align: right;">
                            <button class="btn btn-secondary" onclick="adminApp.viewAssignmentRoster(${a.id})" style="font-size: 11px; padding: 4px 10px; font-weight: 700;">
                                📊 Grade Roster
                            </button>
                        </td>
                    `;
                    overTbody.appendChild(tr);
                });
            }
        }
    }

    async pingAssignmentPending(assignmentId) {
        try {
            const res = await fetch(`/api/admin/assignments/${assignmentId}/remind`, { method: 'POST' });
            const data = await res.json();
            if (data.success) {
                this.showToast(`📢 Urgent deadline notice dispatched to ${data.remindedCount} unsubmitted students!`, 'success');
            } else {
                alert(data.error || 'Failed to dispatch reminder');
            }
        } catch (e) {
            alert('Error: ' + e.message);
        }
    }

    async viewAssignmentRoster(assignmentId) {
        try {
            const res = await fetch(`/api/admin/assignments/${assignmentId}/submissions`);
            const data = await res.json();
            const modal = document.getElementById('admin-assignment-roster-modal');
            if (!modal) return;

            this.activeRosterData = data;
            this.activeAssignmentId = assignmentId;
            this.currentRosterFilter = 'ALL';
            this.rosterSearchQuery = '';

            const a = data.assignment;
            const isExam = a.type === 'EXAM';
            document.getElementById('roster-modal-title').textContent = `${a.title} - Comprehensive Grading Roster`;
            document.getElementById('roster-modal-type').textContent = isExam ? '🎓 EXAM' : (a.type === 'QUIZ' ? '⚡ QUIZ' : '📄 ASSIGNMENT');
            document.getElementById('roster-modal-type').className = `badge ${isExam ? 'badge-purple' : (a.type === 'QUIZ' ? 'badge-gold' : 'badge-blue')}`;
            document.getElementById('roster-modal-meta').textContent = `Cohort: ${a.assignedToLabel || 'All Enrolled Students'} • Duration: ${a.durationMinutes}m • Max Marks: ${a.maxMarks} • Due: ${new Date(a.dueDate).toLocaleString()}`;

            // Populate 6 KPI Summary Stat Boxes
            document.getElementById('roster-stat-total').textContent = `${data.totalStudents} Students`;
            document.getElementById('roster-stat-completed').textContent = data.completedCount;
            document.getElementById('roster-stat-turnout').textContent = `${data.turnoutPercent || 0}% Turnout`;
            document.getElementById('roster-stat-attempted').textContent = data.attemptedCount;
            document.getElementById('roster-stat-pending').textContent = data.pendingCount;
            document.getElementById('roster-stat-avg').textContent = `${data.averageScore} / ${a.maxMarks}`;
            document.getElementById('roster-stat-highest').textContent = `${data.highestScore} / ${a.maxMarks}`;
            document.getElementById('roster-stat-scorer').textContent = data.highestScorer || 'None';

            this.renderRosterTable(data.roster);
            modal.classList.add('active');
        } catch (e) {
            alert('Failed to load roster: ' + e.message);
        }
    }

    renderRosterTable(roster) {
        const tbody = document.getElementById('roster-modal-table-body');
        if (!tbody) return;
        tbody.innerHTML = '';

        if (!roster || roster.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 20px;">No students enrolled in this cohort roster.</td></tr>';
            return;
        }

        let filtered = roster;
        if (this.currentRosterFilter && this.currentRosterFilter !== 'ALL') {
            filtered = filtered.filter(s => s.status === this.currentRosterFilter);
        }
        if (this.rosterSearchQuery) {
            filtered = filtered.filter(s => 
                (s.fullName && s.fullName.toLowerCase().includes(this.rosterSearchQuery)) ||
                (s.username && s.username.toLowerCase().includes(this.rosterSearchQuery)) ||
                (s.rollNumber && s.rollNumber.toLowerCase().includes(this.rosterSearchQuery)) ||
                (s.department && s.department.toLowerCase().includes(this.rosterSearchQuery))
            );
        }

        if (filtered.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 20px;">No students match the current filter or search criteria.</td></tr>';
            return;
        }

        filtered.forEach(stu => {
            const tr = document.createElement('tr');
            let statusBadge = '<span class="badge badge-gold" style="font-size: 10px;">⏳ Inactive / Pending</span>';
            if (stu.status === 'COMPLETED') {
                statusBadge = '<span class="badge badge-easy" style="font-size: 10px;">✅ Submitted (On Time)</span>';
            } else if (stu.status === 'ATTEMPTED') {
                statusBadge = '<span class="badge badge-blue" style="font-size: 10px;">🔄 Testing Code</span>';
            }

            const initial = (stu.fullName || stu.username || 'S')[0].toUpperCase();
            const scoreColor = stu.status === 'COMPLETED' ? '#4ade80' : (stu.status === 'ATTEMPTED' ? '#38bdf8' : 'var(--text-muted)');
            const timeStr = stu.submittedAt ? new Date(stu.submittedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '--';

            tr.innerHTML = `
                <td>
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(99, 102, 241, 0.2); border: 1px solid rgba(99, 102, 241, 0.4); display: flex; align-items: center; justify-content: center; font-weight: 800; color: #fff; font-size: 12px;">
                            ${initial}
                        </div>
                        <div>
                            <div style="font-weight: 700; color: #fff; font-size: 13px;">${stu.fullName}</div>
                            <div style="font-size: 11px; color: var(--text-secondary); display: flex; align-items: center; gap: 6px;">
                                <span>@${stu.username}</span>
                                <span style="color: var(--text-muted);">•</span>
                                <span style="font-family: var(--font-code); color: #38bdf8;">${stu.rollNumber}</span>
                            </div>
                        </div>
                    </div>
                </td>
                <td><span style="font-size: 11.5px; color: var(--text-secondary);">${stu.department}</span></td>
                <td>${statusBadge}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 6px;">
                        <strong style="color: ${scoreColor}; font-size: 13.5px; font-family: var(--font-code);">${stu.score} / ${stu.maxMarks}</strong>
                        <span class="badge ${stu.percentage >= 80 ? 'badge-easy' : (stu.percentage >= 50 ? 'badge-medium' : 'badge-hard')}" style="font-size: 9px; padding: 2px 5px;">${stu.percentage}%</span>
                    </div>
                </td>
                <td>
                    <span style="font-size: 11.5px; color: ${stu.status === 'COMPLETED' ? '#4ade80' : 'var(--text-muted)'}; font-family: var(--font-code);">
                        ${stu.testCasesPassed || '--'}
                    </span>
                </td>
                <td><span style="font-size: 11px; color: var(--text-muted);">${timeStr}</span></td>
                <td><span style="font-size: 11px; font-family: var(--font-code); color: #38bdf8;">${stu.executionTimeMs ? stu.executionTimeMs + 'ms' : '--'}</span></td>
                <td style="text-align: right;">
                    <div style="display: flex; gap: 6px; justify-content: flex-end; align-items: center;">
                        ${stu.sourceCode || stu.submissionId ? `
                            <button type="button" class="btn btn-secondary" onclick="adminApp.viewRosterStudentCode(${stu.userId})" style="font-size: 10.5px; padding: 4px 8px; color: #38bdf8;" title="Audit Exact Code Solution">
                                👁️ Code
                            </button>
                        ` : ''}
                        <button type="button" class="btn btn-secondary" onclick="adminApp.openFeedbackModal(${stu.userId}, '${stu.username}')" style="font-size: 10.5px; padding: 4px 8px;" title="Send Direct Academic Feedback">
                            💬
                        </button>
                        <button type="button" class="btn btn-secondary" onclick="adminApp.adjustRosterStudentScore(${stu.userId}, ${stu.score})" style="font-size: 10.5px; padding: 4px 8px; color: #fbbf24;" title="Adjust or Override Score">
                            ✏️ Marks
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    filterRoster(filterKey) {
        this.currentRosterFilter = filterKey;
        const tabs = ['all', 'submitted', 'attempted', 'pending'];
        tabs.forEach(t => {
            const btn = document.getElementById(`roster-tab-${t}`);
            if (btn) {
                if ((filterKey === 'ALL' && t === 'all') || 
                    (filterKey === 'COMPLETED' && t === 'submitted') ||
                    (filterKey === 'ATTEMPTED' && t === 'attempted') ||
                    (filterKey === 'PENDING' && t === 'pending')) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            }
        });
        if (this.activeRosterData) {
            this.renderRosterTable(this.activeRosterData.roster);
        }
    }

    searchRoster(query) {
        this.rosterSearchQuery = (query || '').toLowerCase().trim();
        if (this.activeRosterData) {
            this.renderRosterTable(this.activeRosterData.roster);
        }
    }

    async remindPendingRosterStudents() {
        if (!this.activeAssignmentId) return;
        await this.pingAssignmentPending(this.activeAssignmentId);
    }

    async extendRosterDeadline() {
        if (!this.activeAssignmentId) return;
        try {
            const res = await fetch(`/api/admin/assignments/${this.activeAssignmentId}/extend-deadline`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ additionalHours: 24 })
            });
            const data = await res.json();
            if (data.success) {
                this.showToast(`Deadline extended by 24h: ${new Date(data.newDueDate).toLocaleString()}`, 'success');
                this.viewAssignmentRoster(this.activeAssignmentId);
                this.loadAssignments();
            }
        } catch (e) {
            alert('Failed to extend deadline');
        }
    }

    viewRosterStudentCode(userId) {
        if (!this.activeRosterData) return;
        const stu = this.activeRosterData.roster.find(s => s.userId === userId);
        if (!stu) return;

        const modal = document.getElementById('admin-code-modal');
        if (!modal) return;

        document.getElementById('code-modal-title').textContent = `${this.activeRosterData.assignment.title} — Solution by ${stu.fullName}`;
        document.getElementById('code-modal-meta').textContent = `@${stu.username} • Roll: ${stu.rollNumber} • Submitted: ${stu.submittedAt ? new Date(stu.submittedAt).toLocaleString() : 'N/A'}`;
        document.getElementById('code-modal-verdict').textContent = stu.status === 'COMPLETED' ? 'ACCEPTED' : (stu.status === 'ATTEMPTED' ? 'ATTEMPTED' : 'PENDING');
        document.getElementById('code-modal-verdict').className = `badge ${stu.status === 'COMPLETED' ? 'badge-easy' : 'badge-hard'}`;
        document.getElementById('code-modal-score').textContent = `Score: ${stu.score} / ${stu.maxMarks} (${stu.percentage}%)`;
        document.getElementById('code-modal-runtime').textContent = `Runtime: ${stu.executionTimeMs || 0} ms • ${stu.testCasesPassed || ''}`;
        document.getElementById('code-modal-source').textContent = stu.sourceCode || '// Source code recorded in sandbox:\n\npublic class Solution {\n    // Implementation successfully executed all test cases\n}';

        modal.classList.add('active');
    }

    async adjustRosterStudentScore(userId, currentScore) {
        const newScoreStr = prompt(`Enter adjusted score (Current: ${currentScore}):`, currentScore);
        if (newScoreStr === null || newScoreStr.trim() === '') return;
        const newScore = parseInt(newScoreStr);
        if (isNaN(newScore) || newScore < 0) {
            alert('Invalid score number');
            return;
        }

        const note = prompt('Optional Evaluator Note for Grade Adjustment (Visible to student):', 'Grade adjustment following lab practical evaluation review');

        try {
            const res = await fetch(`/api/admin/assignments/${this.activeAssignmentId}/adjust-score`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId, newScore, feedbackNote: note })
            });
            const data = await res.json();
            if (data.success) {
                this.showToast(`Score updated to ${newScore} pts!`, 'success');
                this.viewAssignmentRoster(this.activeAssignmentId);
                this.loadAssignments();
            }
        } catch (e) {
            alert('Failed to adjust score: ' + e.message);
        }
    }

    exportAssignmentsCSV() {
        if (!this.cachedAssignments || this.cachedAssignments.length === 0) {
            this.showToast('No assignments to export', 'info');
            return;
        }

        const headers = ['Assessment ID', 'Title', 'Type', 'Category', 'Target Cohort', 'Duration (mins)', 'Max Marks', 'Due Date', 'Submissions Count'];
        const rows = this.cachedAssignments.map(a => [
            `#A${a.id}`,
            `"${(a.title || '').replace(/"/g, '""')}"`,
            a.type,
            a.category || 'EXAMINATION',
            `"${(a.assignedToLabel || 'All Students').replace(/"/g, '""')}"`,
            a.durationMinutes,
            a.maxMarks,
            `"${new Date(a.dueDate).toLocaleString()}"`,
            a.submissionCount || 0
        ]);

        const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement('a');
        link.setAttribute('href', encodedUri);
        link.setAttribute('download', `Examinations_Roster_${new Date().toISOString().slice(0, 10)}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        this.showToast('Examinations roster downloaded as CSV', 'success');
    }

    exportRosterCSV() {
        if (!this.activeRosterData || !this.activeRosterData.roster) {
            this.showToast('No roster data to export', 'info');
            return;
        }

        const a = this.activeRosterData.assignment;
        const headers = ['Roll Number', 'Student Name', 'Username', 'Department', 'Status', 'Score Awarded', 'Max Marks', 'Percentage', 'Test Cases Passed', 'Runtime (ms)', 'Submitted At'];
        const rows = this.activeRosterData.roster.map(s => [
            `"${s.rollNumber}"`,
            `"${(s.fullName || '').replace(/"/g, '""')}"`,
            `"${s.username}"`,
            `"${(s.department || '').replace(/"/g, '""')}"`,
            s.status,
            s.score,
            s.maxMarks,
            `${s.percentage}%`,
            `"${s.testCasesPassed || '--'}"`,
            s.executionTimeMs || '--',
            `"${s.submittedAt ? new Date(s.submittedAt).toLocaleString() : '--'}"`
        ]);

        const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement('a');
        link.setAttribute('href', encodedUri);
        link.setAttribute('download', `Gradebook_${a.title.replace(/[^a-zA-Z0-9]/g, '_')}_${new Date().toISOString().slice(0, 10)}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        this.showToast('Exam gradebook downloaded as CSV', 'success');
    }

    async deleteAssignment(assignmentId) {
        if (!confirm('Are you sure you want to revoke this examination/assignment from all student portals?')) return;
        try {
            await fetch(`/api/admin/assignments/${assignmentId}`, { method: 'DELETE' });
            this.showToast('Examination revoked successfully', 'info');
            this.loadAssignments();
        } catch (e) {
            alert('Failed to delete assignment');
        }
    }

    // =========================================================================
    // STUDENT SURVEILLANCE & SUBMISSIONS
    // =========================================================================

    renderStudents(students) {
        const tbody = document.getElementById('admin-students-body');
        const badge = document.getElementById('admin-student-count-badge');
        if (badge) badge.textContent = `${students.length} Students Monitored`;
        if (!tbody) return;

        tbody.innerHTML = '';
        if (!students || students.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 20px;">No students found matching current filter criteria.</td></tr>';
            return;
        }

        const totalProbs = (this.cachedProblems && this.cachedProblems.length) || 28;

        students.forEach((s, idx) => {
            const tr = document.createElement('tr');
            const rank = s.currentRank || (idx + 1);
            const solved = s.solvedProblems || 0;
            const pct = Math.round((solved / totalProbs) * 100);
            const pts = s.totalPoints || 0;

            let gradeBadge = '';
            if (pts >= 450) {
                gradeBadge = '<span class="badge" style="background: rgba(34, 197, 94, 0.2); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.5); font-weight: 800; font-size: 11px;">A+ (Exemplary)</span>';
            } else if (pts >= 350) {
                gradeBadge = '<span class="badge" style="background: rgba(56, 189, 248, 0.2); color: #38bdf8; border: 1px solid rgba(56, 189, 248, 0.5); font-weight: 800; font-size: 11px;">A (Distinction)</span>';
            } else if (pts >= 250) {
                gradeBadge = '<span class="badge" style="background: rgba(168, 85, 247, 0.2); color: #c084fc; border: 1px solid rgba(168, 85, 247, 0.5); font-weight: 800; font-size: 11px;">B+ (Proficient)</span>';
            } else if (pts >= 150) {
                gradeBadge = '<span class="badge" style="background: rgba(251, 191, 36, 0.2); color: #fbbf24; border: 1px solid rgba(251, 191, 36, 0.5); font-weight: 800; font-size: 11px;">B (Competent)</span>';
            } else if (pts >= 50) {
                gradeBadge = '<span class="badge" style="background: rgba(249, 115, 22, 0.2); color: #fb923c; border: 1px solid rgba(249, 115, 22, 0.5); font-weight: 800; font-size: 11px;">C (Developing)</span>';
            } else {
                gradeBadge = '<span class="badge" style="background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.5); font-weight: 800; font-size: 11px;">F (Remedial)</span>';
            }

            // Daily Streak & Activity Status
            let streakBadge = '';
            const streak = s.streakDays || 1;
            if (streak >= 7) {
                streakBadge = `
                    <div style="display: flex; flex-direction: column; gap: 3px;">
                        <span class="badge" style="background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.5); font-weight: 800; font-size: 11px; width: fit-content;">🔥 ${streak} Days Elite</span>
                        <span style="font-size: 10px; color: #4ade80; font-weight: 600;">⚡ ${s.todayProblemsCount || 2} Solved Today</span>
                    </div>`;
            } else if (streak >= 4) {
                streakBadge = `
                    <div style="display: flex; flex-direction: column; gap: 3px;">
                        <span class="badge" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8; border: 1px solid rgba(56, 189, 248, 0.4); font-weight: 700; font-size: 11px; width: fit-content;">⚡ ${streak} Days Steady</span>
                        <span style="font-size: 10px; color: #38bdf8; font-weight: 600;">⚡ Practiced Today</span>
                    </div>`;
            } else if (streak <= 2) {
                streakBadge = `
                    <div style="display: flex; flex-direction: column; gap: 3px;">
                        <span class="badge" style="background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.5); font-weight: 700; font-size: 11px; width: fit-content;">⚠️ ${streak} Day${streak === 1 ? '' : 's'} Critical</span>
                        <span style="font-size: 10px; color: #f87171; font-weight: 600;">⚠️ Inactive (${s.lastActive || '2d ago'})</span>
                    </div>`;
            } else {
                streakBadge = `
                    <div style="display: flex; flex-direction: column; gap: 3px;">
                        <span class="badge" style="border: 1px solid rgba(255,255,255,0.15); color: #cbd5e1; font-size: 11px; width: fit-content;">🔥 ${streak} Days</span>
                        <span style="font-size: 10px; color: #94a3b8;">Active ${s.lastActive || 'today'}</span>
                    </div>`;
            }

            // Attendance Rate & Academic Standing
            const attPct = s.attendancePct !== undefined ? s.attendancePct : 85;
            let attBarColor = '#22c55e';
            let attStatusText = 'Regular';
            let attStatusColor = '#4ade80';
            if (attPct < 65) {
                attBarColor = '#ef4444';
                attStatusText = 'Deficit';
                attStatusColor = '#f87171';
            } else if (attPct < 80) {
                attBarColor = '#fbbf24';
                attStatusText = 'At-Risk';
                attStatusColor = '#fbbf24';
            }

            const attendanceBadge = `
                <div style="display: flex; align-items: center; gap: 8px;">
                    <div style="width: 50px; height: 6px; background: rgba(255,255,255,0.08); border-radius: 3px; overflow: hidden;">
                        <div style="width: ${attPct}%; height: 100%; background: ${attBarColor};"></div>
                    </div>
                    <div>
                        <div style="font-size: 12px; font-weight: 700; color: ${attStatusColor};">${attPct}%</div>
                        <div style="font-size: 9.5px; color: ${attStatusColor}; font-weight: 600;">${attStatusText}</div>
                    </div>
                </div>`;

            tr.innerHTML = `
                <td>
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <span class="badge" style="border: 1px solid var(--border-color); font-size: 11px;">#${rank}</span>
                        <div class="dash-hero-avatar" style="width: 32px; height: 32px; font-size: 14px;">${s.username.charAt(0).toUpperCase()}</div>
                        <div>
                            <div style="font-weight: 700; color: #fff;">${s.fullName || s.username}</div>
                            <div style="font-size: 11px; color: var(--text-secondary);">@${s.username} • ${s.rollNo || s.email}</div>
                        </div>
                    </div>
                </td>
                <td><span style="font-size: 12px; color: var(--text-secondary);">${s.department || s.dept || 'Computer Science'}</span></td>
                <td>
                    <span class="badge" style="background: rgba(34, 197, 94, 0.15); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.4); font-size: 10px;">
                        <span class="chip-dot" style="background: #22c55e;"></span> ONLINE
                    </span>
                </td>
                <td>${streakBadge}</td>
                <td>${attendanceBadge}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <div style="width: 70px; height: 6px; background: rgba(255,255,255,0.08); border-radius: 3px; overflow: hidden;">
                            <div style="width: ${pct}%; height: 100%; background: #38bdf8;"></div>
                        </div>
                        <span style="font-size: 11px; font-weight: 700; color: #fff;">${solved}/${totalProbs} (${pct}%)</span>
                    </div>
                </td>
                <td><strong style="color: var(--accent-green); font-size: 13px;">${pts} XP</strong></td>
                <td>${gradeBadge}</td>
                <td style="text-align: right;">
                    <div style="display: flex; gap: 6px; justify-content: flex-end;">
                        <button class="btn btn-secondary" onclick="adminApp.sendPracticeReminder(${s.userId}, '${s.username}', this)" style="font-size: 11px; padding: 4px 8px; background: rgba(245, 158, 11, 0.15); border: 1px solid rgba(245, 158, 11, 0.4); color: #fbbf24;" title="Send Daily Practice Streak Reminder Alert">📢 Remind</button>
                        <button class="btn btn-secondary" onclick="adminApp.impersonateStudent(${s.userId})" style="font-size: 11px; padding: 4px 8px; background: rgba(56, 189, 248, 0.15); border: 1px solid rgba(56, 189, 248, 0.4); color: #38bdf8;" title="Shadow Mode: Open Student Arena as this student">🔑 Shadow</button>
                        <button class="btn btn-secondary" onclick="adminApp.inspectStudent(${s.userId})" style="font-size: 11px; padding: 4px 10px; font-weight: 700; background: rgba(168, 85, 247, 0.15); border: 1px solid rgba(168, 85, 247, 0.4); color: #c084fc;" title="Open Full 360° Academic Dossier">🔍 360° Dossier</button>
                        <button class="btn btn-secondary" onclick="adminApp.openFeedbackModal(${s.userId}, '${s.username}')" style="font-size: 11px; padding: 4px 8px;" title="Give Academic Feedback">💬</button>
                        <button class="btn btn-danger" onclick="adminApp.deleteStudent(${s.userId})" style="font-size: 11px; padding: 4px 8px;" title="Delete Profile">🗑️</button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Also populate Overview panel preview (Full Width 1-by-1)
        const overStudentsBody = document.getElementById('overview-students-body');
        if (overStudentsBody) {
            overStudentsBody.innerHTML = '';
            if (!students || students.length === 0) {
                overStudentsBody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 18px;">No student records found.</td></tr>';
            } else {
                students.slice(0, 6).forEach((s, idx) => {
                    const rank = s.currentRank || (idx + 1);
                    const solved = s.solvedProblems || 0;
                    const pct = Math.round((solved / totalProbs) * 100);
                    const pts = s.totalPoints || 0;

                    let gradeBadge = '';
                    if (pts >= 450) {
                        gradeBadge = '<span class="badge" style="background: rgba(34, 197, 94, 0.2); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.5); font-weight: 800; font-size: 11px;">A+ Exemplary</span>';
                    } else if (pts >= 250) {
                        gradeBadge = '<span class="badge" style="background: rgba(56, 189, 248, 0.2); color: #38bdf8; border: 1px solid rgba(56, 189, 248, 0.5); font-weight: 800; font-size: 11px;">A Distinction</span>';
                    } else if (pts >= 100) {
                        gradeBadge = '<span class="badge" style="background: rgba(251, 191, 36, 0.2); color: #fbbf24; border: 1px solid rgba(251, 191, 36, 0.5); font-weight: 800; font-size: 11px;">B Competent</span>';
                    } else {
                        gradeBadge = '<span class="badge" style="background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.5); font-weight: 800; font-size: 11px;">F Remedial</span>';
                    }

                    const streak = s.streakDays || 1;
                    const streakPill = streak >= 5 
                        ? `<span class="badge" style="background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.5); font-weight: 800; font-size: 11px;">🔥 ${streak} Days</span>`
                        : (streak <= 2 
                            ? `<span class="badge" style="background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.5); font-weight: 700; font-size: 11px;">⚠️ ${streak} Day${streak === 1 ? '' : 's'}</span>`
                            : `<span class="badge" style="border: 1px solid rgba(255,255,255,0.15); color: #cbd5e1; font-size: 11px;">🔥 ${streak} Days</span>`);

                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>
                            <span class="badge" style="font-size: 11px; font-weight: 800; border: 1px solid var(--border-color);">#${rank}</span>
                        </td>
                        <td>
                            <div style="display: flex; align-items: center; gap: 10px;">
                                <div class="dash-hero-avatar" style="width: 32px; height: 32px; font-size: 13px;">${s.username.charAt(0).toUpperCase()}</div>
                                <div>
                                    <div style="font-weight: 700; color: #fff; font-size: 13px;">${s.fullName || s.username}</div>
                                    <div style="font-size: 11px; color: var(--text-secondary);">@${s.username} • ${s.rollNo || ''}</div>
                                </div>
                            </div>
                        </td>
                        <td>
                            <span style="font-size: 12px; color: var(--text-secondary);">${s.department || s.dept || 'Computer Science'}</span>
                        </td>
                        <td>${streakPill}</td>
                        <td>
                            <div style="display: flex; align-items: center; gap: 8px;">
                                <div style="width: 70px; height: 6px; background: rgba(255,255,255,0.08); border-radius: 3px; overflow: hidden;">
                                    <div style="width: ${pct}%; height: 100%; background: #38bdf8;"></div>
                                </div>
                                <span style="font-size: 11px; font-weight: 700; color: #fff;">${solved}/${totalProbs} (${pct}%)</span>
                            </div>
                        </td>
                        <td><strong style="color: var(--accent-green); font-size: 13px;">${pts} XP</strong></td>
                        <td>${gradeBadge}</td>
                        <td style="text-align: right;">
                            <div style="display: flex; gap: 6px; justify-content: flex-end;">
                                <button class="btn btn-secondary" onclick="adminApp.inspectStudent(${s.userId})" style="font-size: 11px; padding: 4px 10px; color: #c084fc; border-color: rgba(168, 85, 247, 0.4);" title="Open 360° Academic Dossier">🔍 Dossier</button>
                                <button class="btn btn-secondary" onclick="adminApp.openFeedbackModal(${s.userId}, '${s.username}')" style="font-size: 11px; padding: 4px 8px;" title="Give Academic Feedback">💬</button>
                            </div>
                        </td>
                    `;
                    overStudentsBody.appendChild(tr);
                });
            }
        }
    }

    renderSubmissions(subs) {
        const tbody = document.getElementById('admin-submissions-body');
        const badge = document.getElementById('admin-subs-count-badge');
        if (badge) badge.textContent = `${subs.length} Submissions`;
        if (!tbody) return;

        tbody.innerHTML = '';
        if (!subs || subs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 20px;">No submissions found.</td></tr>';
            return;
        }

        subs.slice(0, 30).forEach(s => {
            const tr = document.createElement('tr');
            const vBadge = s.status === 'ACCEPTED' 
                ? '<span class="badge badge-easy" style="font-size: 10px;">ACCEPTED</span>' 
                : `<span class="badge badge-hard" style="font-size: 10px;">${s.status}</span>`;

            tr.innerHTML = `
                <td><span style="font-family: var(--font-code); font-size: 11px; color: var(--text-muted);">#${s.submissionId}</span></td>
                <td><strong style="color: #fff; font-size: 12px;">@${s.username}</strong></td>
                <td><span style="font-size: 12px; color: var(--text-primary);">${s.problemTitle || ('Challenge #' + s.problemId)}</span></td>
                <td>${vBadge}</td>
                <td><strong style="color: #4ade80;">+${s.score} pts</strong></td>
                <td><span style="font-family: var(--font-code); font-size: 11px; color: #38bdf8;">${s.executionTime || 0}ms</span></td>
                <td><span style="font-size: 11px; color: var(--text-muted);">${new Date(s.submittedAt).toLocaleTimeString()}</span></td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary" onclick="adminApp.viewCodeAudit(${s.submissionId})" style="font-size: 11px; padding: 3px 8px;">
                        🔍 View Code
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Also populate Overview panel preview (Full Width 1-by-1)
        const overSubsBody = document.getElementById('overview-submissions-body');
        if (overSubsBody) {
            overSubsBody.innerHTML = '';
            if (!subs || subs.length === 0) {
                overSubsBody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 18px;">No submissions logged.</td></tr>';
            } else {
                subs.slice(0, 6).forEach(s => {
                    const tr = document.createElement('tr');
                    const vBadge = s.status === 'ACCEPTED' 
                        ? '<span class="badge badge-easy" style="font-size: 10px; font-weight: 700;">ACCEPTED</span>' 
                        : `<span class="badge badge-hard" style="font-size: 10px; font-weight: 700;">${s.status}</span>`;
                    tr.innerHTML = `
                        <td><span style="font-family: var(--font-code); font-size: 11px; color: var(--text-muted);">#${s.submissionId}</span></td>
                        <td><strong style="color: #fff; font-size: 12.5px;">@${s.username}</strong></td>
                        <td><span style="font-size: 12.5px; color: var(--text-primary); font-weight: 600;">${s.problemTitle || ('Challenge #' + s.problemId)}</span></td>
                        <td>${vBadge}</td>
                        <td><strong style="color: #4ade80; font-size: 12.5px;">+${s.score} pts</strong></td>
                        <td><span style="font-family: var(--font-code); font-size: 11px; color: #38bdf8;">${s.executionTime || 0}ms</span></td>
                        <td><span style="font-size: 11px; color: var(--text-muted);">${new Date(s.submittedAt).toLocaleTimeString()}</span></td>
                        <td style="text-align: right;">
                            <button class="btn btn-secondary" onclick="adminApp.viewCodeAudit(${s.submissionId})" style="font-size: 11px; padding: 4px 10px;">
                                🔍 View Code
                            </button>
                        </td>
                    `;
                    overSubsBody.appendChild(tr);
                });
            }
        }
    }

    renderAnnouncements(announcements) {
        const list = document.getElementById('admin-announcements-list');
        if (!list) return;

        list.innerHTML = '';
        if (!announcements || announcements.length === 0) {
            list.innerHTML = '<p style="color: var(--text-muted); font-size: 12px; margin: 0;">No active notices.</p>';
            return;
        }

        announcements.forEach(item => {
            const row = document.createElement('div');
            row.style.cssText = 'display: flex; justify-content: space-between; align-items: center; background: rgba(13, 17, 23, 0.7); border: 1px solid var(--border-color); border-radius: 10px; padding: 12px; flex-wrap: wrap; gap: 10px;';
            row.innerHTML = `
                <div style="display: flex; flex-direction: column; gap: 4px; max-width: 80%;">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <strong style="font-size: 13px; color: #fff;">${item.title}</strong>
                        ${item.pinned ? '<span class="badge badge-gold" style="font-size: 9px; padding: 2px 6px;">📌 PINNED</span>' : ''}
                        <span class="badge badge-purple" style="font-size: 9px; padding: 2px 6px;">${item.category}</span>
                    </div>
                    <p style="font-size: 12px; color: var(--text-secondary); margin: 0; line-height: 1.4;">${item.content}</p>
                </div>
                <button class="btn btn-danger" onclick="adminApp.deleteAnnouncement(${item.id})" style="font-size: 11px; padding: 4px 10px;">Delete</button>
            `;
            list.appendChild(row);
        });
    }

    // =========================================================================
    // PUBLISH CHALLENGE & BROADCAST
    // =========================================================================

    setupProblemCreator() {
        const btn = document.getElementById('admin-btn-create-prob');
        const alertBox = document.getElementById('admin-prob-alert');

        if (btn) {
            btn.addEventListener('click', async () => {
                const title = document.getElementById('admin-prob-title').value;
                const category = document.getElementById('admin-prob-category').value;
                const difficulty = document.getElementById('admin-prob-difficulty').value;
                const points = document.getElementById('admin-prob-pts').value;
                const description = document.getElementById('admin-prob-desc').value;
                const sampleInput = document.getElementById('admin-prob-sample-in').value;
                const sampleOutput = document.getElementById('admin-prob-sample-out').value;

                if (!title || !description) {
                    alertBox.style.display = 'block';
                    alertBox.textContent = '⚠️ Please enter challenge title and description.';
                    return;
                }

                try {
                    const res = await fetch('/api/admin/problems', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ 
                            title, 
                            category, 
                            difficulty: (difficulty || 'EASY').toUpperCase(), 
                            points: parseInt(points) || 100, 
                            description, 
                            sampleInput, 
                            sampleOutput 
                        })
                    });
                    const data = await res.json();
                    if (data.success) {
                        alertBox.style.display = 'block';
                        alertBox.style.background = 'rgba(46, 160, 67, 0.15)';
                        alertBox.style.color = '#56d364';
                        alertBox.textContent = `🚀 Problem "${title}" published live!`;
                        setTimeout(() => { alertBox.style.display = 'none'; }, 4000);
                        document.getElementById('admin-prob-title').value = '';
                        document.getElementById('admin-prob-desc').value = '';
                        this.loadAllData();
                    }
                } catch (e) {
                    alert('Failed to publish problem');
                }
            });
        }
    }

    setupAnnouncementBroadcast() {
        const btn = document.getElementById('btn-broadcast-announcement');
        const alertBox = document.getElementById('admin-announce-alert');

        if (btn) {
            btn.addEventListener('click', async () => {
                const title = document.getElementById('admin-announce-title')?.value;
                const category = document.getElementById('admin-announce-category')?.value;
                const priority = document.getElementById('admin-announce-priority')?.value;
                const content = document.getElementById('admin-announce-content')?.value;
                const pinned = document.getElementById('admin-announce-pinned')?.checked;

                if (!title || !content) {
                    alertBox.style.display = 'block';
                    alertBox.textContent = '⚠️ Please enter title and content.';
                    return;
                }

                try {
                    const res = await fetch('/api/admin/announcements', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ title, category, priority, content, pinned })
                    });
                    const data = await res.json();
                    if (data.success) {
                        alertBox.style.display = 'block';
                        alertBox.style.background = 'rgba(46, 160, 67, 0.15)';
                        alertBox.style.color = '#56d364';
                        alertBox.textContent = '📢 Announcement broadcasted to all students!';
                        setTimeout(() => { alertBox.style.display = 'none'; }, 4000);
                        document.getElementById('admin-announce-title').value = '';
                        document.getElementById('admin-announce-content').value = '';
                        this.loadAllData();
                    }
                } catch (e) {
                    alert('Broadcast error: ' + e.message);
                }
            });
        }
    }

    async deleteAnnouncement(id) {
        if (!confirm('Delete this broadcasted notice?')) return;
        try {
            await fetch(`/api/admin/announcements/${id}`, { method: 'DELETE' });
            this.loadAllData();
        } catch (e) {}
    }

    // =========================================================================
    // MODALS & INSPECTORS
    // =========================================================================

    async inspectStudent(userId) {
        try {
            this.currentInspectedStudentId = userId;
            const res = await fetch(`/api/admin/students/${userId}/full-profile`);
            const data = await res.json();
            const s = data.student || {};
            const perf = data.performance || {};
            const subs = data.submissions || [];
            const assigns = data.assignments || [];
            const feedback = data.feedback || [];

            const modal = document.getElementById('admin-inspect-modal');
            if (!modal) return;

            // Header Elements
            document.getElementById('inspect-modal-avatar').textContent = (s.username || 'S').charAt(0).toUpperCase();
            document.getElementById('inspect-modal-username').textContent = `${s.fullName} (@${s.username})`;
            document.getElementById('inspect-modal-email').textContent = `${s.email} • Roll: ${s.rollNo} • ${s.dept} (${s.semester})`;
            
            const rankBadge = document.getElementById('inspect-modal-rank-badge');
            if (rankBadge) rankBadge.textContent = `#${perf.currentRank || 1} Campus Rank`;

            const statusBadge = document.getElementById('inspect-modal-status-badge');
            if (statusBadge) {
                statusBadge.textContent = s.status || 'ONLINE';
                statusBadge.style.color = s.status === 'ACTIVE_CODING' ? '#38bdf8' : '#4ade80';
            }

            // Tab counts
            const subsCountEl = document.getElementById('dossier-subs-count');
            if (subsCountEl) subsCountEl.textContent = subs.length;
            const assignCountEl = document.getElementById('dossier-assigns-count');
            if (assignCountEl) assignCountEl.textContent = assigns.length;
            const feedbackCountEl = document.getElementById('dossier-feedback-count');
            if (feedbackCountEl) feedbackCountEl.textContent = feedback.length;

            // Tab 1: Overview
            const ptsEl = document.getElementById('inspect-modal-pts');
            if (ptsEl) ptsEl.textContent = `${perf.totalPoints || 0} XP`;

            const gradeEl = document.getElementById('inspect-modal-grade');
            const totalPts = perf.totalPoints || 0;
            let letterGrade = 'Grade: F';
            if (totalPts >= 450) letterGrade = 'Grade: A+ (Exemplary)';
            else if (totalPts >= 350) letterGrade = 'Grade: A (Distinction)';
            else if (totalPts >= 250) letterGrade = 'Grade: B+ (Proficient)';
            else if (totalPts >= 150) letterGrade = 'Grade: B (Competent)';
            else if (totalPts >= 50) letterGrade = 'Grade: C (Developing)';
            if (gradeEl) gradeEl.textContent = letterGrade;

            const totalProbs = (this.cachedProblems && this.cachedProblems.length) || 28;
            const solvedEl = document.getElementById('inspect-modal-solved');
            if (solvedEl) solvedEl.textContent = `${perf.totalSolved || 0} / ${totalProbs}`;

            const diffBreakdown = document.getElementById('inspect-modal-diff-breakdown');
            if (diffBreakdown) diffBreakdown.textContent = `${perf.easySolved || 0} Easy • ${perf.mediumSolved || 0} Med • ${perf.hardSolved || 0} Hard`;

            const accEl = document.getElementById('inspect-modal-acc');
            if (accEl) accEl.textContent = `${perf.successRate || 0}%`;

            const standingEl = document.getElementById('inspect-modal-standing');
            if (standingEl) {
                standingEl.textContent = s.accountStanding || 'EXEMPLARY';
                if (s.accountStanding === 'SUSPENDED' || s.accountStanding === 'FLAGGED_MALPRACTICE') {
                    standingEl.style.color = '#ef4444';
                } else if (s.accountStanding === 'PROBATION') {
                    standingEl.style.color = '#f59e0b';
                } else {
                    standingEl.style.color = '#4ade80';
                }
            }

            // Solved Problems Catalog
            const catalogContainer = document.getElementById('inspect-modal-solved-catalog');
            if (catalogContainer) {
                catalogContainer.innerHTML = '';
                if (!perf.solvedProblemsList || !perf.solvedProblemsList.length) {
                    catalogContainer.innerHTML = '<span style="color: var(--text-muted); font-size: 12px;">No problems officially solved yet.</span>';
                } else {
                    perf.solvedProblemsList.forEach(p => {
                        const pill = document.createElement('span');
                        pill.className = `badge ${p.difficulty === 'EASY' ? 'badge-easy' : (p.difficulty === 'MEDIUM' ? 'badge-blue' : 'badge-hard')}`;
                        pill.style.cssText = 'font-size: 11px; padding: 4px 8px; cursor: pointer;';
                        pill.textContent = `✓ #${p.problemId} ${p.title} (+${p.points} XP)`;
                        catalogContainer.appendChild(pill);
                    });
                }
            }

            // Recent Submissions in Tab 1
            const tbodySubs = document.getElementById('inspect-modal-subs-body');
            if (tbodySubs) {
                tbodySubs.innerHTML = '';
                subs.slice(0, 5).forEach(sub => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td><strong>${sub.problemTitle}</strong></td>
                        <td><span class="badge ${sub.status === 'ACCEPTED' ? 'badge-easy' : 'badge-hard'}" style="font-size: 9px;">${sub.status}</span></td>
                        <td><strong style="color: ${sub.score > 0 ? '#4ade80' : '#ef4444'};">${sub.score > 0 ? '+' : ''}${sub.score} pts</strong></td>
                        <td><span style="font-family: var(--font-code); color: #38bdf8; font-size: 11px;">${sub.executionTime || 12}ms</span></td>
                        <td style="color: var(--text-muted); font-size: 11px;">${new Date(sub.submittedAt).toLocaleTimeString()}</td>
                    `;
                    tbodySubs.appendChild(tr);
                });
            }

            // Tab 2: Full Code Submissions Table
            const fullSubsBody = document.getElementById('dossier-submissions-body');
            if (fullSubsBody) {
                fullSubsBody.innerHTML = '';
                if (!subs.length) {
                    fullSubsBody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 20px; color: var(--text-muted);">No submissions recorded for this student.</td></tr>';
                } else {
                    subs.forEach(sub => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td><span style="font-family: var(--font-code); color: #94a3b8;">#${sub.submissionId}</span></td>
                            <td><strong style="color: #fff;">${sub.problemTitle}</strong></td>
                            <td><span class="badge ${sub.status === 'ACCEPTED' ? 'badge-easy' : 'badge-hard'}" style="font-size: 10px;">${sub.status}</span></td>
                            <td><span style="color: #4ade80; font-weight: 700;">+${sub.score}</span></td>
                            <td><span style="font-family: var(--font-code); color: #38bdf8;">${sub.executionTime || 12}ms</span></td>
                            <td style="font-size: 11px; color: var(--text-muted);">${new Date(sub.submittedAt).toLocaleString()}</td>
                            <td style="text-align: right;">
                                <button class="btn btn-secondary" onclick="adminApp.viewCodeAudit(${sub.submissionId})" style="font-size: 10px; padding: 3px 8px;">
                                    🔍 Code Audit
                                </button>
                            </td>
                        `;
                        fullSubsBody.appendChild(tr);
                    });
                }
            }

            // Tab 3: Assignments & Exams Table
            const assignsBody = document.getElementById('dossier-assignments-body');
            if (assignsBody) {
                assignsBody.innerHTML = '';
                if (!assigns.length) {
                    assignsBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px; color: var(--text-muted);">No examinations or coursework found.</td></tr>';
                } else {
                    assigns.forEach(a => {
                        const tr = document.createElement('tr');
                        let statusPill = '<span class="badge badge-gold" style="font-size: 10px;">⏳ PENDING</span>';
                        if (a.status === 'COMPLETED') {
                            statusPill = '<span class="badge badge-easy" style="font-size: 10px;">✅ COMPLETED</span>';
                        } else if (a.status === 'ATTEMPTED') {
                            statusPill = '<span class="badge badge-blue" style="font-size: 10px;">🔄 IN PROGRESS</span>';
                        }
                        tr.innerHTML = `
                            <td><strong style="color: #fff;">${a.title}</strong></td>
                            <td><span class="badge ${a.type === 'EXAM' ? 'badge-purple' : 'badge-blue'}" style="font-size: 10px;">${a.type}</span></td>
                            <td style="font-size: 11px; color: var(--text-secondary);">${new Date(a.dueDate).toLocaleDateString()}</td>
                            <td>${statusPill}</td>
                            <td><strong style="color: #4ade80;">${a.score} / ${a.maxMarks}</strong></td>
                            <td style="font-size: 11px; color: var(--text-muted);">${a.submittedAt ? new Date(a.submittedAt).toLocaleTimeString() : '--'}</td>
                        `;
                        assignsBody.appendChild(tr);
                    });
                }
            }

            // Tab 4: Faculty Master Controls Form Values
            document.getElementById('dossier-edit-name').value = s.fullName || '';
            document.getElementById('dossier-edit-email').value = s.email || '';
            document.getElementById('dossier-edit-roll').value = s.rollNo || '';
            document.getElementById('dossier-edit-sem').value = s.semester || '';
            const deptSelect = document.getElementById('dossier-edit-dept');
            if (deptSelect) deptSelect.value = s.dept || 'Computer Science & Engineering';

            document.getElementById('dossier-current-pass').textContent = s.password || 'student123';
            const resetPassInp = document.getElementById('dossier-reset-pass');
            if (resetPassInp) resetPassInp.value = '';

            const standingSelect = document.getElementById('dossier-standing-select');
            if (standingSelect) standingSelect.value = s.accountStanding || 'EXEMPLARY';

            // Tab 5: Feedback & Warnings List
            const feedbackList = document.getElementById('dossier-feedback-list');
            if (feedbackList) {
                feedbackList.innerHTML = '';
                if (!feedback.length) {
                    feedbackList.innerHTML = '<div style="color: var(--text-muted); font-size: 12px; padding: 10px;">No administrative feedback or warnings issued yet.</div>';
                } else {
                    feedback.forEach(f => {
                        const item = document.createElement('div');
                        item.style.cssText = 'background: rgba(13, 17, 23, 0.7); border: 1px solid var(--border-color); border-radius: 8px; padding: 12px;';
                        item.innerHTML = `
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                                <span class="badge ${f.badge.includes('Warning') || f.badge.includes('🛑') ? 'badge-hard' : 'badge-gold'}" style="font-size: 10px;">${f.badge}</span>
                                <span style="font-size: 11px; color: var(--text-muted);">${new Date(f.createdAt).toLocaleDateString()}</span>
                            </div>
                            <p style="margin: 0; font-size: 12px; color: #fff; line-height: 1.4;">${f.note}</p>
                            <div style="font-size: 10.5px; color: var(--text-secondary); margin-top: 6px;">Issued by: ${f.instructor}</div>
                        `;
                        feedbackList.appendChild(item);
                    });
                }
            }

            // Always start at Tab 1
            this.switchDossierTab('tab-overview');
            modal.classList.add('active');
        } catch (e) {
            this.showToast('Failed to load full student dossier: ' + e.message, 'error');
        }
    }

    switchDossierTab(tabId) {
        document.querySelectorAll('.dossier-tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
        });
        document.querySelectorAll('.dossier-tab-pane').forEach(pane => {
            pane.classList.remove('active');
        });
        const target = document.getElementById(`dossier-pane-${tabId}`);
        if (target) target.classList.add('active');
    }

    async saveStudentProfile(event) {
        event.preventDefault();
        const userId = this.currentInspectedStudentId;
        if (!userId) return;

        const fullName = document.getElementById('dossier-edit-name').value;
        const email = document.getElementById('dossier-edit-email').value;
        const rollNo = document.getElementById('dossier-edit-roll').value;
        const semester = document.getElementById('dossier-edit-sem').value;
        const dept = document.getElementById('dossier-edit-dept').value;

        try {
            const res = await fetch(`/api/admin/students/${userId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ fullName, email, rollNo, semester, dept })
            });
            const data = await res.json();
            if (data.success) {
                this.showToast('✅ Student profile updated successfully!', 'success');
                this.loadAllData();
                this.inspectStudent(userId);
            }
        } catch (e) {
            this.showToast('Error saving profile: ' + e.message, 'error');
        }
    }

    async executePointAdjustment() {
        const userId = this.currentInspectedStudentId;
        if (!userId) return;

        const action = document.getElementById('dossier-adj-action').value;
        const points = document.getElementById('dossier-adj-pts').value;
        const reason = document.getElementById('dossier-adj-reason').value;

        try {
            const res = await fetch(`/api/admin/students/${userId}/adjust-points`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ action, points, reason })
            });
            const data = await res.json();
            if (data.success) {
                this.showToast(`⚡ Points adjusted: ${action === 'DEDUCT' ? '-' : '+'}${points} XP`, 'success');
                this.loadAllData();
                this.inspectStudent(userId);
            }
        } catch (e) {
            this.showToast('Failed to adjust points: ' + e.message, 'error');
        }
    }

    async resetStudentPasswordDirect() {
        const userId = this.currentInspectedStudentId;
        if (!userId) return;

        const newPass = document.getElementById('dossier-reset-pass').value.trim() || 'student123';
        try {
            const res = await fetch(`/api/admin/students/${userId}/reset-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ newPassword: newPass })
            });
            const data = await res.json();
            if (data.success) {
                document.getElementById('dossier-current-pass').textContent = newPass;
                this.showToast(`🔑 Password reset to: ${newPass}`, 'success');
            }
        } catch (e) {
            this.showToast('Failed to reset password: ' + e.message, 'error');
        }
    }

    async saveStudentStanding() {
        const userId = this.currentInspectedStudentId;
        if (!userId) return;

        const accountStanding = document.getElementById('dossier-standing-select').value;
        let status = 'ONLINE';
        if (accountStanding === 'SUSPENDED') status = 'SUSPENDED';
        else if (accountStanding === 'FLAGGED_MALPRACTICE') status = 'FLAGGED';

        try {
            const res = await fetch(`/api/admin/students/${userId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ accountStanding, status })
            });
            const data = await res.json();
            if (data.success) {
                this.showToast(`🛡️ Student standing updated to: ${accountStanding}`, 'success');
                this.loadAllData();
                this.inspectStudent(userId);
            }
        } catch (e) {
            this.showToast('Failed to update standing: ' + e.message, 'error');
        }
    }

    async dispatchDossierFeedback() {
        const userId = this.currentInspectedStudentId;
        if (!userId) return;

        const badge = document.getElementById('dossier-new-feedback-badge').value;
        const note = document.getElementById('dossier-new-feedback-note').value;
        if (!note.trim()) {
            this.showToast('Please enter note text', 'warning');
            return;
        }

        try {
            const res = await fetch(`/api/admin/students/${userId}/feedback`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ badge, note })
            });
            const data = await res.json();
            if (data.success) {
                document.getElementById('dossier-new-feedback-note').value = '';
                this.showToast('🚀 Notice sent directly to student dashboard!', 'success');
                this.inspectStudent(userId);
            }
        } catch (e) {
            this.showToast('Failed to dispatch feedback: ' + e.message, 'error');
        }
    }

    async impersonateStudent(targetUserId) {
        const userId = targetUserId || this.currentInspectedStudentId;
        if (!userId) return;

        try {
            const res = await fetch(`/api/admin/students/${userId}/impersonate`, { method: 'POST' });
            const data = await res.json();
            if (data.success && data.user) {
                localStorage.setItem('cb_user', JSON.stringify(data.user));
                this.showToast(`🔑 Shadow Mode active: Logged in as @${data.user.username}. Opening Student Arena...`, 'success');
                setTimeout(() => {
                    window.open('/', '_blank');
                }, 800);
            }
        } catch (e) {
            this.showToast('Failed to impersonate student: ' + e.message, 'error');
        }
    }

    openFeedbackModal(userId, username) {
        const modal = document.getElementById('admin-feedback-modal');
        if (!modal) return;
        document.getElementById('feedback-student-id').value = userId;
        document.getElementById('feedback-modal-subtitle').textContent = `Feedback for @${username}`;
        const noteEl = document.getElementById('feedback-note-text');
        if (noteEl) noteEl.value = '';
        modal.classList.add('active');

        const form = document.getElementById('admin-feedback-form');
        form.onsubmit = async (e) => {
            e.preventDefault();
            const badge = document.getElementById('feedback-badge-select').value;
            const note = document.getElementById('feedback-note-text').value;
            await fetch(`/api/admin/students/${userId}/feedback`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ badge, note })
            });
            alert('Feedback sent directly to student dashboard!');
            modal.classList.remove('active');
        };
    }

    viewCodeAudit(submissionId) {
        const sub = this.cachedSubmissions.find(s => s.submissionId === submissionId);
        if (!sub) return;

        const modal = document.getElementById('admin-code-modal');
        if (!modal) return;

        document.getElementById('code-modal-title').textContent = `${sub.problemTitle || 'Challenge'} - Code Audit`;
        document.getElementById('code-modal-meta').textContent = `Submitted by @${sub.username} on ${new Date(sub.submittedAt).toLocaleString()}`;
        document.getElementById('code-modal-verdict').textContent = sub.status;
        document.getElementById('code-modal-verdict').className = `badge ${sub.status === 'ACCEPTED' ? 'badge-easy' : 'badge-hard'}`;
        document.getElementById('code-modal-score').textContent = `Score: ${sub.score} pts`;
        document.getElementById('code-modal-runtime').textContent = `Runtime: ${sub.executionTime || 0} ms`;
        document.getElementById('code-modal-source').textContent = sub.sourceCode || '// No source code recorded';

        modal.classList.add('active');
    }

    async deleteStudent(userId) {
        if (!confirm('Are you sure you want to delete this student profile and all associated test records?')) return;
        try {
            await fetch(`/api/admin/students/${userId}`, { method: 'DELETE' });
            this.showToast('Student record deleted', 'info');
            this.loadAllData();
        } catch (e) {}
    }

    // =========================================================================
    // GRADEBOOK EXPORT & FILTERS
    // =========================================================================

    exportGradebookCSV() {
        const students = this.cachedStudents || [];
        if (!students.length) {
            this.showToast('No student records available to export', 'error');
            return;
        }

        const headers = ['Rank', 'Full Name', 'Username', 'Email', 'Department', 'Solved Challenges', 'Total Course Points (XP)', 'Academic Grade', 'Surveillance Status'];
        const csvRows = [headers.join(',')];

        const totalProbs = (this.cachedProblems && this.cachedProblems.length) || 28;

        students.forEach((s, idx) => {
            const rank = s.currentRank || (idx + 1);
            const pts = s.totalPoints || 0;
            let letterGrade = 'F (Remedial)';
            if (pts >= 450) letterGrade = 'A+ (Exemplary)';
            else if (pts >= 350) letterGrade = 'A (Distinction)';
            else if (pts >= 250) letterGrade = 'B+ (Proficient)';
            else if (pts >= 150) letterGrade = 'B (Competent)';
            else if (pts >= 50) letterGrade = 'C (Developing)';

            const row = [
                rank,
                `"${(s.fullName || s.username).replace(/"/g, '""')}"`,
                `"${(s.username || '').replace(/"/g, '""')}"`,
                `"${(s.email || '').replace(/"/g, '""')}"`,
                `"${(s.department || s.dept || 'Computer Science & Engineering').replace(/"/g, '""')}"`,
                `"${s.solvedProblems || 0} / ${totalProbs}"`,
                pts,
                `"${letterGrade}"`,
                '"ACTIVE / ONLINE"'
            ];
            csvRows.push(row.join(','));
        });

        const csvContent = csvRows.join('\r\n');
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.setAttribute('href', url);
        link.setAttribute('download', `CSE201_Java_Class_Gradebook_${new Date().getFullYear()}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        this.showToast('📥 Gradebook CSV exported successfully!', 'success');
    }

    toggleHonorRollFilter() {
        this.isHonorRollOnly = !this.isHonorRollOnly;
        const btn = document.getElementById('btn-filter-honor-roll');
        if (btn) {
            if (this.isHonorRollOnly) {
                btn.style.background = 'rgba(251, 191, 36, 0.25)';
                btn.style.borderColor = '#fbbf24';
                btn.style.color = '#fbbf24';
                btn.innerHTML = '⭐ Filter: Honor Roll (Active)';
            } else {
                btn.style.background = '';
                btn.style.borderColor = '';
                btn.style.color = '';
                btn.innerHTML = '⭐ Honor Roll (XP ≥ 300)';
            }
        }
        this.applyAllStudentFilters();
    }

    applyAllStudentFilters() {
        const searchInput = document.getElementById('admin-student-search');
        const deptFilter = document.getElementById('admin-dept-filter');
        const q = (searchInput?.value || '').toLowerCase().trim();
        const d = deptFilter?.value || 'ALL';

        let list = this.cachedStudents || [];

        // 1. Department Filter
        if (d !== 'ALL') {
            list = list.filter(s => {
                const studentDept = (s.dept || s.department || '').toLowerCase();
                return studentDept.includes(d.toLowerCase());
            });
        }

        // 2. Search Query
        if (q) {
            list = list.filter(s => 
                (s.username && s.username.toLowerCase().includes(q)) || 
                (s.fullName && s.fullName.toLowerCase().includes(q)) ||
                (s.rollNo && s.rollNo.toLowerCase().includes(q))
            );
        }

        // 3. Honor Roll Filter
        if (this.isHonorRollOnly) {
            list = list.filter(s => (s.totalPoints || 0) >= 300);
        }

        // 4. Streak Filter
        if (this.currentStreakFilter === 'HIGH') {
            list = list.filter(s => (s.streakDays || 0) >= 5);
        } else if (this.currentStreakFilter === 'LOW') {
            list = list.filter(s => (s.streakDays || 0) <= 2);
        } else if (this.currentStreakFilter === 'TODAY') {
            list = list.filter(s => Boolean(s.practicedToday));
        }

        this.renderStudents(list);
    }

    filterByStreak(type) {
        this.currentStreakFilter = type;
        const btnMap = {
            'ALL': 'btn-streak-all',
            'HIGH': 'btn-streak-high',
            'LOW': 'btn-streak-low',
            'TODAY': 'btn-streak-today'
        };

        Object.keys(btnMap).forEach(key => {
            const btn = document.getElementById(btnMap[key]);
            if (btn) btn.classList.toggle('active', key === type);
        });

        this.applyAllStudentFilters();
    }

    renderEngagementAnalytics(data) {
        if (!data) return;
        const summary = data.summary || {};

        // 1. Update 4 Attention & Streak Telemetry KPIs
        const elTopStreakVal = document.getElementById('metric-top-streak-val');
        const elTopStreakStudent = document.getElementById('metric-top-streak-student');
        const elAttVal = document.getElementById('metric-cohort-attendance-val');
        const elAttSub = document.getElementById('metric-cohort-attendance-sub');
        const elPracticedVal = document.getElementById('metric-practiced-today-val');
        const elLowStreakVal = document.getElementById('metric-low-streak-val');

        if (elTopStreakVal) elTopStreakVal.textContent = `${summary.maxStreakDays || 0} Days`;
        if (elTopStreakStudent) {
            const topS = summary.maxStreakStudent;
            elTopStreakStudent.textContent = topS ? `${topS.fullName || topS.username} (@${topS.username})` : 'None';
        }
        if (elAttVal) elAttVal.textContent = `${summary.dailyAttendanceRate || 85}%`;
        if (elAttSub) elAttSub.textContent = `${summary.attendedTodayCount || 0} / ${summary.cohortSize || 8} In Regular Standing (≥ 65%)`;
        if (elPracticedVal) elPracticedVal.textContent = `${summary.practicedTodayCount || 0} Students`;
        if (elLowStreakVal) elLowStreakVal.textContent = `${summary.lowestStreakCount || 0} Students`;

        // 2. Render Highest Streaks & Daily Practice Leaders Table (Full Width 1-by-1)
        const highTbody = document.getElementById('highest-streaks-table-body');
        if (highTbody) {
            highTbody.innerHTML = '';
            const highestList = data.highestStreaks || [];
            if (highestList.length === 0) {
                highTbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 18px;">No high streak records found.</td></tr>';
            } else {
                highestList.forEach((s, idx) => {
                    const tr = document.createElement('tr');
                    const medal = idx === 0 ? '🥇 #1' : (idx === 1 ? '🥈 #2' : (idx === 2 ? '🥉 #3' : `⭐ #${idx + 1}`));
                    const actBadge = s.practicedToday 
                        ? `<div style="font-size: 11.5px; font-weight: 700; color: #4ade80;">✅ ${s.todayProblemsCount || 2} Problems Solved Today</div><div style="font-size: 10.5px; color: #94a3b8;">Active ${s.lastActive}</div>`
                        : `<div style="font-size: 11.5px; color: #94a3b8;">⏳ Idle Today</div><div style="font-size: 10px; color: #64748b;">Last active: ${s.lastActive}</div>`;

                    const attPct = s.attendancePct || 95;
                    const attendancePill = `
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <div style="width: 50px; height: 6px; background: rgba(255,255,255,0.08); border-radius: 3px; overflow: hidden;">
                                <div style="width: ${attPct}%; height: 100%; background: #22c55e;"></div>
                            </div>
                            <div>
                                <span style="font-weight: 700; color: #4ade80; font-size: 12px;">${attPct}%</span>
                                <span class="badge" style="font-size: 9px; padding: 1px 4px; background: rgba(34,197,94,0.15); color: #4ade80;">Regular</span>
                            </div>
                        </div>`;

                    tr.innerHTML = `
                        <td>
                            <span class="badge" style="font-size: 11px; font-weight: 800; background: rgba(245, 158, 11, 0.15); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.4);">
                                ${medal}
                            </span>
                        </td>
                        <td>
                            <div style="display: flex; align-items: center; gap: 10px;">
                                <div class="dash-hero-avatar" style="width: 32px; height: 32px; font-size: 13px;">${s.username.charAt(0).toUpperCase()}</div>
                                <div>
                                    <div style="font-weight: 700; color: #fff; font-size: 13px;">${s.fullName || s.username}</div>
                                    <div style="font-size: 11px; color: var(--text-secondary);">@${s.username}</div>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div style="font-size: 12px; color: #e2e8f0; font-weight: 600;">${s.department || 'Computer Science'}</div>
                            <div style="font-size: 11px; color: #94a3b8;">${s.rollNo || ''}</div>
                        </td>
                        <td>
                            <span class="badge" style="background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.5); font-weight: 800; font-size: 12px; padding: 4px 10px;">
                                🔥 ${s.streakDays} Days Elite
                            </span>
                        </td>
                        <td>${actBadge}</td>
                        <td>${attendancePill}</td>
                        <td>
                            <strong style="color: var(--accent-green); font-size: 13px;">${s.totalPoints || 0} XP</strong>
                        </td>
                        <td style="text-align: right;">
                            <div style="display: flex; gap: 6px; justify-content: flex-end;">
                                <button class="btn btn-secondary" onclick="adminApp.openFeedbackModal(${s.userId}, '${s.username}')" style="font-size: 11px; padding: 4px 10px; border-color: rgba(245, 158, 11, 0.4); color: #fbbf24;" title="Award Commendation Notice">
                                    ⭐ Commend
                                </button>
                                <button class="btn btn-secondary" onclick="adminApp.inspectStudent(${s.userId})" style="font-size: 11px; padding: 4px 10px; color: #c084fc; border-color: rgba(168, 85, 247, 0.4);" title="Open 360° Academic Dossier">
                                    🔍 360° Dossier
                                </button>
                            </div>
                        </td>
                    `;
                    highTbody.appendChild(tr);
                });
            }
        }

        // 3. Render Lowest Streaks & Faculty Intervention Watchlist Table (Full Width 1-by-1)
        const lowTbody = document.getElementById('lowest-streaks-table-body');
        if (lowTbody) {
            lowTbody.innerHTML = '';
            const lowestList = data.lowestStreaks || [];
            if (lowestList.length === 0) {
                lowTbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #4ade80; padding: 18px;">✅ No students currently at risk. Excellent cohort attention!</td></tr>';
            } else {
                lowestList.forEach(s => {
                    const tr = document.createElement('tr');
                    const attPct = s.attendancePct || 52;
                    const attDeficitColor = attPct < 65 ? '#f87171' : '#fbbf24';
                    const attBarBg = attPct < 65 ? '#ef4444' : '#f59e0b';
                    const attLabel = attPct < 65 ? 'Critical Deficit' : 'At-Risk Standing';

                    const attPill = `
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <div style="width: 50px; height: 6px; background: rgba(255,255,255,0.08); border-radius: 3px; overflow: hidden;">
                                <div style="width: ${attPct}%; height: 100%; background: ${attBarBg};"></div>
                            </div>
                            <div>
                                <span style="font-size: 12px; font-weight: 700; color: ${attDeficitColor};">${attPct}%</span>
                                <div style="font-size: 9.5px; color: ${attDeficitColor}; font-weight: 600;">${attLabel}</div>
                            </div>
                        </div>`;

                    tr.innerHTML = `
                        <td>
                            <div style="display: flex; align-items: center; gap: 10px;">
                                <div class="dash-hero-avatar" style="width: 32px; height: 32px; font-size: 13px; background: rgba(239, 68, 68, 0.2); border-color: rgba(239, 68, 68, 0.4); color: #f87171;">
                                    ${s.username.charAt(0).toUpperCase()}
                                </div>
                                <div>
                                    <div style="font-weight: 700; color: #fff; font-size: 13px;">${s.fullName || s.username}</div>
                                    <div style="font-size: 11px; color: var(--text-secondary);">@${s.username}</div>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div style="font-size: 12px; color: #e2e8f0; font-weight: 600;">${s.department || 'Computer Science'}</div>
                            <div style="font-size: 11px; color: #94a3b8;">${s.rollNo || ''}</div>
                        </td>
                        <td>
                            <span class="badge" style="background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.5); font-weight: 800; font-size: 11px; padding: 4px 8px;">
                                ⚠️ ${s.streakDays} Day${s.streakDays === 1 ? '' : 's'} Critical
                            </span>
                        </td>
                        <td>
                            <div style="font-size: 12px; color: #f87171; font-weight: 700;">⚠️ ${s.lastActive}</div>
                            <div style="font-size: 10px; color: #94a3b8;">Missed Daily Session</div>
                        </td>
                        <td>${attPill}</td>
                        <td>
                            <span class="badge" style="background: rgba(255, 255, 255, 0.05); color: #94a3b8; border: 1px solid rgba(255, 255, 255, 0.1); font-size: 11px;">
                                ${s.status || 'IDLE'} • Inactive
                            </span>
                        </td>
                        <td style="text-align: right;">
                            <div style="display: flex; gap: 6px; justify-content: flex-end;">
                                <button class="btn btn-danger" onclick="adminApp.sendPracticeReminder(${s.userId}, '${s.username}', this)" style="font-size: 11.5px; padding: 6px 14px; font-weight: 800; white-space: nowrap; box-shadow: 0 0 14px rgba(239, 68, 68, 0.3);">
                                    📢 Dispatch Practice Reminder
                                </button>
                                <button class="btn btn-secondary" onclick="adminApp.inspectStudent(${s.userId})" style="font-size: 11px; padding: 4px 8px; color: #c084fc;" title="Open 360° Academic Dossier">
                                    🔍 Dossier
                                </button>
                            </div>
                        </td>
                    `;
                    lowTbody.appendChild(tr);
                });
            }
        }
    }

    async refreshEngagementAnalytics() {
        try {
            const res = await fetch('/api/admin/engagement-analytics');
            if (res.ok) {
                const data = await res.json();
                this.cachedAnalytics = data;
                this.renderEngagementAnalytics(data);
                this.showToast('🔥 Student attention, attendance & streak radar updated live.', 'success');
            }
        } catch (e) {
            console.error('Failed to refresh engagement analytics:', e);
            this.showToast('Failed to refresh telemetry: ' + e.message, 'error');
        }
    }

    async sendPracticeReminder(userId, username, btnEl) {
        try {
            if (btnEl) {
                btnEl.disabled = true;
                btnEl.dataset.originalText = btnEl.textContent;
                btnEl.textContent = 'Sending...';
            }
            const res = await fetch(`/api/admin/students/${userId}/remind-practice`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            const data = await res.json();
            if (res.ok) {
                this.showToast(`📢 Practice streak reminder dispatched directly to @${username}'s dashboard!`, 'success');
                if (btnEl) {
                    btnEl.textContent = 'Dispatched ✔️';
                    btnEl.style.background = 'rgba(34, 197, 94, 0.2)';
                    btnEl.style.borderColor = 'rgba(34, 197, 94, 0.5)';
                    btnEl.style.color = '#4ade80';
                    setTimeout(() => {
                        btnEl.disabled = false;
                        btnEl.textContent = btnEl.dataset.originalText || '📢 Send Reminder';
                        btnEl.style.background = '';
                        btnEl.style.borderColor = '';
                        btnEl.style.color = '';
                    }, 4000);
                }
            } else {
                this.showToast(`❌ Failed to dispatch reminder: ${data.error || 'Server error'}`, 'error');
                if (btnEl) {
                    btnEl.disabled = false;
                    btnEl.textContent = btnEl.dataset.originalText || '📢 Send Reminder';
                }
            }
        } catch (e) {
            console.error('Error sending practice reminder:', e);
            this.showToast(`⚠️ Network error: ${e.message}`, 'error');
            if (btnEl) {
                btnEl.disabled = false;
                btnEl.textContent = btnEl.dataset.originalText || '📢 Send Reminder';
            }
        }
    }

    // =========================================================================
    // CURRICULUM MASTERY HEATMAP
    // =========================================================================

    async loadCurriculumStats() {
        try {
            const res = await fetch('/api/admin/curriculum-stats');
            const data = await res.json();
            this.cachedCurriculum = data;
            this.renderCurriculum(data);
        } catch (e) {
            console.error('Failed to load curriculum:', e);
        }
    }

    renderCurriculum(curriculum) {
        const container = document.getElementById('curriculum-heatmap-container');
        if (!container) return;

        if (!curriculum || !curriculum.length) {
            container.innerHTML = '<div style="padding: 16px; color: var(--text-muted); text-align: center;">No syllabus data available.</div>';
            return;
        }

        container.innerHTML = '';
        curriculum.forEach(unit => {
            const card = document.createElement('div');
            card.className = 'curriculum-card';

            let statusBadge = '<span class="badge" style="background: rgba(34, 197, 94, 0.15); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.4); font-size: 10px;">MASTERED</span>';
            let barColor = '#22c55e';
            if (unit.status === 'NEEDS_REMEDIAL' || unit.passRate < 60) {
                statusBadge = '<span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.4); font-size: 10px;">NEEDS REMEDIAL</span>';
                barColor = '#ef4444';
            } else if (unit.passRate < 80) {
                statusBadge = '<span class="badge" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8; border: 1px solid rgba(56, 189, 248, 0.4); font-size: 10px;">ON TRACK</span>';
                barColor = '#38bdf8';
            }

            card.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                    <span class="badge badge-purple" style="font-size: 10px;">${unit.code.replace(/_/g, ' ')}</span>
                    ${statusBadge}
                </div>
                <div style="font-weight: 700; color: #ffffff; font-size: 13.5px; margin-bottom: 4px;">${unit.name}</div>
                <p style="font-size: 11.5px; color: var(--text-secondary); margin: 0 0 12px 0; line-height: 1.4;">${unit.desc}</p>
                <div style="margin-bottom: 6px; display: flex; justify-content: space-between; font-size: 11px;">
                    <span style="color: var(--text-muted);">Cohort Pass Rate</span>
                    <strong style="color: #fff;">${unit.passRate}%</strong>
                </div>
                <div class="curriculum-progress-track">
                    <div class="curriculum-progress-bar" style="width: ${unit.passRate}%; background: ${barColor};"></div>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 10px; font-size: 11px; color: var(--text-muted);">
                    <span>${unit.totalProblems} Problems</span>
                    <span>${unit.submissionsCount} Evaluated</span>
                </div>
            `;
            container.appendChild(card);
        });
    }

    // =========================================================================
    // OPENJDK 21 SANDBOX ENGINE & TELEMETRY
    // =========================================================================

    async loadSandboxHealth() {
        try {
            const res = await fetch('/api/admin/sandbox-health');
            const data = await res.json();
            this.cachedSandbox = data;
            this.renderSandboxHealth(data);
            this.showToast('🖥️ Sandbox engine pulse refreshed', 'info');
        } catch (e) {
            console.error('Failed to load sandbox health:', e);
        }
    }

    renderSandboxHealth(data) {
        if (!data) return;
        const elEngine = document.getElementById('sandbox-engine');
        const elPods = document.getElementById('sandbox-pods');
        const elHeap = document.getElementById('sandbox-heap');
        const elKills = document.getElementById('sandbox-kills');
        const elLatency = document.getElementById('sandbox-latency');

        if (elEngine) elEngine.textContent = data.engine || 'OpenJDK 21.0.2 HotSpot';
        if (elPods) elPods.textContent = `${data.activePods || 4} Pods Active`;
        if (elHeap) elHeap.textContent = `${data.heapUsedMb || 48} MB`;
        if (elKills) elKills.textContent = `${data.infiniteLoopKills || 0} Loop Kills`;
        if (elLatency) elLatency.textContent = `${data.avgLatencyMs || 38} ms`;
    }

    // =========================================================================
    // PLAGIARISM & ACADEMIC INTEGRITY RADAR
    // =========================================================================

    async loadPlagiarismData() {
        try {
            const res = await fetch('/api/admin/plagiarism');
            const data = await res.json();
            this.cachedPlagiarism = data;
            this.renderPlagiarism(data);
        } catch (e) {
            console.error('Failed to load plagiarism pairs:', e);
        }
    }

    renderPlagiarism(pairs) {
        const tbody = document.getElementById('admin-plagiarism-table-body');
        const badge = document.getElementById('plagiarism-pairs-count-badge');
        const kpiFlagged = document.getElementById('kpi-plag-flagged');

        if (!tbody) return;

        if (badge) badge.textContent = `${pairs.length} Flagged Collision${pairs.length === 1 ? '' : 's'}`;
        if (kpiFlagged) kpiFlagged.textContent = pairs.length;

        tbody.innerHTML = '';
        if (!pairs || !pairs.length) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 24px; color: #22c55e;">✅ Clean Radar: No anomalous code collisions detected above threshold!</td></tr>';
            return;
        }

        pairs.forEach(p => {
            const tr = document.createElement('tr');

            let riskBadge = '<span class="badge badge-hard" style="font-size: 10px;">HIGH RISK</span>';
            let barColor = '#ef4444';
            if (p.riskLevel === 'MEDIUM') {
                riskBadge = '<span class="badge badge-gold" style="font-size: 10px;">MEDIUM RISK</span>';
                barColor = '#f59e0b';
            } else if (p.riskLevel === 'LOW') {
                riskBadge = '<span class="badge badge-easy" style="font-size: 10px;">LOW RISK</span>';
                barColor = '#10b981';
            }

            tr.innerHTML = `
                <td>
                    <div style="font-weight: 700; color: #ffffff;">${p.problemTitle}</div>
                    <div style="font-size: 11px; color: var(--text-secondary);">PID #${p.problemId}</div>
                </td>
                <td>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="color: #38bdf8; font-weight: 700;">@${p.studentA.username}</span>
                        <span style="color: var(--text-muted); font-size: 11px;">vs</span>
                        <span style="color: #c084fc; font-weight: 700;">@${p.studentB.username}</span>
                    </div>
                </td>
                <td>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <div style="width: 70px; height: 6px; background: rgba(255,255,255,0.08); border-radius: 3px; overflow: hidden;">
                            <div style="width: ${p.similarityPct}%; height: 100%; background: ${barColor};"></div>
                        </div>
                        <span style="font-size: 12px; font-weight: 800; color: ${barColor};">${p.similarityPct}%</span>
                    </div>
                </td>
                <td>${riskBadge}</td>
                <td><span style="font-family: var(--font-code); font-size: 12px; color: #94a3b8;">${p.matchedLines} n-grams</span></td>
                <td><span style="font-size: 11px; color: var(--text-muted);">${new Date(p.scannedAt).toLocaleTimeString()}</span></td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary" onclick="adminApp.openPlagiarismDiff('${p.id}')" style="font-size: 11px; padding: 4px 10px; background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.4); color: #fca5a5;">
                        🔍 Side-by-Side Diff
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    openPlagiarismDiff(pairId) {
        const pair = (this.cachedPlagiarism || []).find(p => p.id === pairId);
        if (!pair) return;

        window.currentDiffPairId = pairId;
        const modal = document.getElementById('admin-diff-modal');
        if (!modal) return;

        document.getElementById('diff-modal-title').textContent = `${pair.problemTitle} — Plagiarism Overlap`;
        document.getElementById('diff-modal-meta').textContent = `Pairwise comparison between @${pair.studentA.username} and @${pair.studentB.username}`;
        
        const riskBadge = document.getElementById('diff-modal-risk-badge');
        if (riskBadge) {
            riskBadge.textContent = `${pair.riskLevel} RISK (${pair.similarityPct}%)`;
            riskBadge.className = `badge ${pair.riskLevel === 'HIGH' ? 'badge-hard' : 'badge-gold'}`;
        }

        const simBar = document.getElementById('diff-modal-sim-bar');
        const simText = document.getElementById('diff-modal-sim-text');
        if (simBar) {
            simBar.style.width = `${pair.similarityPct}%`;
            simBar.style.background = pair.riskLevel === 'HIGH' ? '#ef4444' : '#f59e0b';
        }
        if (simText) {
            simText.textContent = `${pair.similarityPct}% Match`;
            simText.style.color = pair.riskLevel === 'HIGH' ? '#ef4444' : '#f59e0b';
        }

        const tokensCount = document.getElementById('diff-modal-tokens-count');
        if (tokensCount) tokensCount.textContent = `${pair.matchedLines} Overlapping AST Token Sequences`;

        document.getElementById('diff-user-a').textContent = `@${pair.studentA.username}`;
        document.getElementById('diff-sub-a').textContent = ` (Submission #${pair.studentA.submissionId})`;
        document.getElementById('diff-code-a').textContent = pair.studentA.sourceCode || '// No source code recorded';

        document.getElementById('diff-user-b').textContent = `@${pair.studentB.username}`;
        document.getElementById('diff-sub-b').textContent = ` (Submission #${pair.studentB.submissionId})`;
        document.getElementById('diff-code-b').textContent = pair.studentB.sourceCode || '// No source code recorded';

        modal.classList.add('active');
    }

    async issuePlagiarismWarning(pairId) {
        const pair = (this.cachedPlagiarism || []).find(p => p.id === pairId);
        if (!pair) return;

        try {
            await Promise.all([
                fetch(`/api/admin/students/${pair.studentA.userId}/feedback`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        badge: '🛑 Academic Integrity Warning',
                        note: `Code collision (${pair.similarityPct}% overlap) detected in "${pair.problemTitle}". Please ensure all coursework is authored independently.`
                    })
                }),
                fetch(`/api/admin/students/${pair.studentB.userId}/feedback`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        badge: '🛑 Academic Integrity Warning',
                        note: `Code collision (${pair.similarityPct}% overlap) detected in "${pair.problemTitle}". Please ensure all coursework is authored independently.`
                    })
                })
            ]);

            document.getElementById('admin-diff-modal').classList.remove('active');
            this.showToast('⚠️ Formal academic integrity warnings dispatched to both students', 'warning');
        } catch (e) {
            alert('Failed to send warnings: ' + e.message);
        }
    }

    dismissPlagiarismFlag(pairId) {
        this.cachedPlagiarism = (this.cachedPlagiarism || []).filter(p => p.id !== pairId);
        this.renderPlagiarism(this.cachedPlagiarism);
        const modal = document.getElementById('admin-diff-modal');
        if (modal) modal.classList.remove('active');
        this.showToast('✓ Flag dismissed as authorized similarity', 'success');
    }

    runRealTimePlagiarismScan() {
        this.showToast('⚡ Running real-time AST token scan across submissions...', 'info');
        setTimeout(() => {
            this.loadPlagiarismData();
            this.showToast('✓ Plagiarism radar scan complete', 'success');
        }, 600);
    }

    // =========================================================================
    // TOAST SYSTEM
    // =========================================================================

    showToast(message, type = 'info') {
        const container = document.getElementById('admin-toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = 'admin-toast';

        let icon = 'ℹ️';
        let border = 'rgba(99, 102, 241, 0.4)';
        if (type === 'success') {
            icon = '✅';
            border = 'rgba(34, 197, 94, 0.5)';
        } else if (type === 'warning') {
            icon = '⚠️';
            border = 'rgba(245, 158, 11, 0.5)';
        } else if (type === 'error') {
            icon = '❌';
            border = 'rgba(239, 68, 68, 0.5)';
        }

        toast.style.borderColor = border;
        toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.transition = 'all 0.3s ease';
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(50px)';
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    }

    setupModalDismissal() {
        document.querySelectorAll('.modal-overlay').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.classList.remove('active');
                }
            });
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                document.querySelectorAll('.modal-overlay.active').forEach(modal => {
                    modal.classList.remove('active');
                });
                this.closeInspectCodeModal();
            }
        });
    }

    // =========================================================================
    // REAL-TIME STUDENT MOMENTS & LIVE ACTIVITY STREAM
    // =========================================================================
    async loadStudentMoments() {
        try {
            const res = await fetch('/api/admin/student-moments');
            if (!res.ok) return;
            const data = await res.json();
            this.cachedMoments = data.moments || [];
            this.cachedActiveStudents = data.onlineStudents || [];

            this.renderStudentMoments();
            this.renderActiveStudentsStrip();
        } catch (e) {
            console.error('Failed to load live student moments:', e);
        }
    }

    renderActiveStudentsStrip() {
        const strip = document.getElementById('moments-students-strip');
        const countBadge = document.getElementById('moments-active-count');
        if (!strip) return;

        const students = this.cachedActiveStudents || [];
        if (countBadge) {
            const activeCount = students.filter(s => s.status === 'ACTIVE_CODING').length;
            countBadge.innerHTML = `<span style="color:#22c55e;">● ${activeCount} Active Coding</span> &nbsp;•&nbsp; <span style="color:#38bdf8;">${students.length} Enrolled Online</span>`;
        }

        if (!students.length) {
            strip.innerHTML = '<span style="color:var(--text-muted);font-size:12px;">No students currently tracked</span>';
            return;
        }

        strip.innerHTML = students.map(s => `
            <div style="background: rgba(15, 23, 42, 0.85); border: 1px solid rgba(255,255,255,0.08); border-radius: 8px; padding: 8px 12px; display: flex; align-items: center; gap: 8px; flex-shrink: 0; min-width: 180px;">
                <div style="width: 30px; height: 30px; border-radius: 7px; background: linear-gradient(135deg, #6366f1, #a855f7); color: #fff; font-weight: 800; display: flex; align-items: center; justify-content: center; font-size: 13px;">
                    ${s.fullName ? s.fullName[0].toUpperCase() : 'S'}
                </div>
                <div style="overflow: hidden; flex: 1;">
                    <div style="font-size: 12px; font-weight: 700; color: #fff; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">${this.escapeHtml(s.fullName || s.username)}</div>
                    <div style="font-size: 10.5px; color: ${s.status === 'ACTIVE_CODING' ? '#4ade80' : '#38bdf8'}; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">
                        ${s.status === 'ACTIVE_CODING' ? '⚡ ' + this.escapeHtml(s.currentActivity || 'Active Coding') : '🟢 Online in Arena'}
                    </div>
                </div>
            </div>
        `).join('');
    }

    renderStudentMoments() {
        const containers = [
            document.getElementById('moments-feed-container'),
            document.getElementById('moments-feed-container-panel')
        ];

        const moments = this.cachedMoments || [];
        const filter = this.currentMomentsFilter || 'ALL';

        const filtered = moments.filter(m => {
            if (filter === 'ALL') return true;
            if (filter === 'RAN_CODE') return m.action && m.action.startsWith('RAN_CODE');
            if (filter === 'ERROR') return m.statusType === 'danger' || m.statusType === 'warning' || (m.status && m.status.includes('ERROR'));
            if (filter === 'SUBMISSION') return m.action === 'SUBMITTED_CODE';
            return true;
        });

        containers.forEach(container => {
            if (!container) return;

            if (!filtered.length) {
                container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 24px; font-size: 13px;">No student moments recorded for this filter.</div>`;
                return;
            }

            container.innerHTML = filtered.map(m => {
                const badgeColor = m.statusType === 'success' ? '#22c55e' : (m.statusType === 'danger' ? '#ef4444' : (m.statusType === 'warning' ? '#f59e0b' : '#38bdf8'));
                const badgeBg = m.statusType === 'success' ? 'rgba(34, 197, 94, 0.15)' : (m.statusType === 'danger' ? 'rgba(239, 68, 68, 0.15)' : (m.statusType === 'warning' ? 'rgba(245, 158, 11, 0.15)' : 'rgba(56, 189, 248, 0.15)'));
                const icon = m.statusType === 'success' ? '✅' : (m.statusType === 'danger' ? '⛔' : (m.statusType === 'warning' ? '⚠️' : 'ℹ️'));
                const timeAgo = this.formatRelativeTime(m.timestamp);

                return `
                    <div style="background: rgba(15, 23, 42, 0.78); border: 1px solid rgba(255,255,255,0.06); border-radius: 8px; padding: 12px 14px; display: flex; align-items: center; justify-content: space-between; gap: 12px; transition: all 0.15s ease;" onmouseover="this.style.background='rgba(30, 41, 59, 0.9)'" onmouseout="this.style.background='rgba(15, 23, 42, 0.78)'">
                        <div style="display: flex; align-items: center; gap: 12px; min-width: 0; flex: 1;">
                            <div style="width: 36px; height: 36px; border-radius: 8px; background: linear-gradient(135deg, #4f46e5, #9333ea); color: #fff; font-weight: 800; display: flex; align-items: center; justify-content: center; font-size: 14px; flex-shrink: 0; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);">
                                ${m.avatar || 'S'}
                            </div>
                            <div style="min-width: 0; flex: 1;">
                                <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                                    <strong style="font-size: 13.5px; color: #fff;">${this.escapeHtml(m.studentName || m.username)}</strong>
                                    <span style="font-size: 11px; color: #94a3b8; font-family: var(--font-code); background: rgba(255,255,255,0.05); padding: 1px 6px; border-radius: 4px;">${this.escapeHtml(m.rollNo || '')}</span>
                                    <span style="font-size: 11px; color: #64748b;">•</span>
                                    <span style="font-size: 12px; color: #c084fc; font-weight: 600;">${this.escapeHtml(m.problemTitle || 'Coding Arena')}</span>
                                </div>
                                <div style="font-size: 12px; color: var(--text-secondary); margin-top: 4px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                                    <span style="display: inline-flex; align-items: center; gap: 4px; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; color: ${badgeColor}; background: ${badgeBg}; border: 1px solid ${badgeColor}33;">
                                        <span>${icon}</span>
                                        <span>${this.escapeHtml(m.statusText || m.status)}</span>
                                    </span>
                                    <span style="color: #cbd5e1; font-size: 12px;">${this.escapeHtml(m.details || '')}</span>
                                </div>
                            </div>
                        </div>
                        <div style="display: flex; align-items: center; gap: 12px; flex-shrink: 0;">
                            <span style="font-size: 11.5px; color: #94a3b8; font-family: var(--font-code);">${timeAgo}</span>
                            ${m.codeSnippet ? `
                                <button onclick="adminApp.inspectStudentCode(${m.id})" class="btn btn-secondary" style="font-size: 11px; padding: 5px 12px; border-color: rgba(99, 102, 241, 0.45); color: #c7d2fe; font-weight: 600;" title="Inspect exact code entered by student">
                                    👁️ View Code
                                </button>
                            ` : ''}
                        </div>
                    </div>
                `;
            }).join('');
        });
    }

    formatRelativeTime(isoStr) {
        if (!isoStr) return 'Just now';
        const diffMs = Date.now() - new Date(isoStr).getTime();
        const sec = Math.floor(diffMs / 1000);
        if (sec < 45) return 'Just now';
        const min = Math.floor(sec / 60);
        if (min < 60) return `${min}m ago`;
        const hrs = Math.floor(min / 60);
        if (hrs < 24) return `${hrs}h ago`;
        return `${Math.floor(hrs / 24)}d ago`;
    }

    filterMoments(filter) {
        this.currentMomentsFilter = filter;
        const s1 = document.getElementById('moments-filter-select');
        const s2 = document.getElementById('moments-filter-select-panel');
        if (s1) s1.value = filter;
        if (s2) s2.value = filter;
        this.renderStudentMoments();
    }

    inspectStudentCode(momentId) {
        const moment = (this.cachedMoments || []).find(m => m.id === momentId);
        if (!moment) return;

        const modal = document.getElementById('modal-inspect-student-code');
        const title = document.getElementById('inspect-modal-title');
        const meta = document.getElementById('inspect-modal-meta');
        const statusBox = document.getElementById('inspect-modal-status-box');
        const codePre = document.getElementById('inspect-modal-code-pre');
        const ts = document.getElementById('inspect-modal-timestamp');

        if (title) title.textContent = `Student Code: ${moment.studentName || moment.username}`;
        if (meta) meta.textContent = `${moment.rollNo || ''} • ${moment.dept || ''} • ${moment.problemTitle || 'Arena'}`;
        if (statusBox) {
            const color = moment.statusType === 'success' ? '#4ade80' : (moment.statusType === 'danger' ? '#f87171' : '#fbbf24');
            statusBox.innerHTML = `<strong>Action:</strong> <span style="color:#c084fc;">${this.escapeHtml(moment.actionLabel || moment.action)}</span> &nbsp;|&nbsp; <strong>Status:</strong> <span style="color: ${color}; font-weight: 700;">${this.escapeHtml(moment.statusText || moment.status)}</span><div style="margin-top:4px;color:#94a3b8;font-size:12px;">${this.escapeHtml(moment.details || '')}</div>`;
        }
        if (codePre) codePre.textContent = moment.codeSnippet || '// No code snippet recorded for this action';
        if (ts) ts.textContent = `Timestamp: ${new Date(moment.timestamp).toLocaleTimeString()} (${this.formatRelativeTime(moment.timestamp)})`;

        if (modal) modal.style.display = 'flex';
    }

    closeInspectCodeModal() {
        const modal = document.getElementById('modal-inspect-student-code');
        if (modal) modal.style.display = 'none';
    }
}

// Global instance
const adminApp = new AdminPortalApp();
