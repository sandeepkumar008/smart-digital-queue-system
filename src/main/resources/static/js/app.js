/**
 * Q-Flow AI - Client Application Logic
 * Integrates REST APIs, Live Display Polling, Web Speech Audio Announcements, and Chart.js Visualizations
 */

// Global State
const state = {
    activeTab: 'kiosk',
    departments: [],
    counters: [],
    selectedDeptCode: null,
    selectedCounterId: 1,
    voiceEnabled: true,
    lastAnnouncedToken: null,
    serviceTimerInterval: null,
    serviceSecondsElapsed: 0,
    charts: {
        arrivals: null,
        wait: null,
        dayTrends: null
    }
};

// Initialize Application on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initClock();
    initPrioritySelector();
    initVoiceToggle();
    loadInitialData();

    // Start background sync
    setInterval(syncLiveBoard, 3000);
    setInterval(updateSystemMetrics, 6000);
});

/* ==========================================================================
   Navigation & View Switching
   ========================================================================== */
function initNavigation() {
    const tabs = document.querySelectorAll('.nav-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const tabName = tab.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    state.activeTab = tabName;

    document.querySelectorAll('.nav-tab').forEach(t => {
        t.classList.toggle('active', t.getAttribute('data-tab') === tabName);
    });

    document.querySelectorAll('.tab-view').forEach(view => {
        view.classList.toggle('active', view.id === `view-${tabName}`);
    });

    // Refresh view specific data on switch
    if (tabName === 'display') {
        syncLiveBoard();
    } else if (tabName === 'operator') {
        loadOperatorView();
    } else if (tabName === 'prediction') {
        loadPredictionData();
    } else if (tabName === 'database') {
        loadDatabaseAudit();
    }
}

function initClock() {
    const updateTime = () => {
        const now = new Date();
        const timeStr = now.toLocaleTimeString('en-US', { hour12: true, hour: '2-digit', minute: '2-digit', second: '2-digit' });
        const kioskClock = document.getElementById('kioskClock');
        const displayClock = document.getElementById('displayBoardClock');
        if (kioskClock) kioskClock.textContent = timeStr;
        if (displayClock) displayClock.textContent = timeStr;
    };
    updateTime();
    setInterval(updateTime, 1000);
}

function initVoiceToggle() {
    const btn = document.getElementById('voiceToggleBtn');
    const icon = document.getElementById('voiceIcon');
    const label = document.getElementById('voiceLabel');

    btn.addEventListener('click', () => {
        state.voiceEnabled = !state.voiceEnabled;
        if (state.voiceEnabled) {
            btn.classList.remove('muted');
            icon.className = 'bi bi-volume-up-fill';
            label.textContent = 'Voice ON';
            showToast('Voice announcements enabled', 'info');
        } else {
            btn.classList.add('muted');
            icon.className = 'bi bi-volume-mute-fill';
            label.textContent = 'Voice Muted';
            showToast('Voice announcements muted', 'warning');
        }
    });
}

/* ==========================================================================
   Data Fetching & Bootstrapping
   ========================================================================== */
async function loadInitialData() {
    try {
        const [deptsRes, countersRes] = await Promise.all([
            fetch('/api/queue/departments'),
            fetch('/api/queue/counters')
        ]);

        state.departments = await deptsRes.json();
        state.counters = await countersRes.json();

        renderKioskDepartments();
        populateCounterSelector();
        populatePredictionDeptSelector();
        updateSystemMetrics();
        syncLiveBoard();
    } catch (err) {
        console.error('Error loading initial metadata:', err);
        showToast('Error connecting to backend database', 'error');
    }
}

async function updateSystemMetrics() {
    try {
        const res = await fetch('/api/admin/stats');
        const stats = await res.json();

        document.getElementById('stripWaitingCount').textContent = stats.waitingCount;
        document.getElementById('stripServingCount').textContent = stats.servingCount;
        document.getElementById('stripCompletedCount').textContent = stats.completedCount;
        document.getElementById('stripAvgWait').textContent = `${stats.avgWaitTimeMinutes}m`;
        document.getElementById('stripActiveCounters').textContent = `${stats.onlineCounters}/${stats.totalCounters}`;

        // Also update Database tab counts if present
        const dbWaiting = document.getElementById('dbCountTickets');
        if (dbWaiting) dbWaiting.textContent = stats.totalTicketsToday;
    } catch (err) {
        // Silently handle transient network pauses
    }
}

/* ==========================================================================
   TAB 1: KIOSK LOGIC
   ========================================================================== */
function renderKioskDepartments() {
    const container = document.getElementById('kioskDepartmentsList');
    if (!container) return;

    container.innerHTML = '';
    state.departments.forEach((dept, index) => {
        const card = document.createElement('div');
        card.className = `dept-select-card ${index === 0 ? 'active' : ''}`;
        card.dataset.code = dept.code;
        card.dataset.name = dept.name;
        card.dataset.wait = dept.avgServiceMinutes;

        card.innerHTML = `
            <div class="dept-left">
                <div class="dept-icon-box" style="background: ${dept.colorHex || '#2563eb'}">
                    <i class="bi ${dept.icon || 'bi-building'}"></i>
                </div>
                <div class="dept-info">
                    <h4>${dept.name}</h4>
                    <p>${dept.description || 'Standard counter service'}</p>
                </div>
            </div>
            <div class="dept-right">
                <span class="dept-tag">${dept.code}</span>
            </div>
        `;

        card.addEventListener('click', () => {
            document.querySelectorAll('.dept-select-card').forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            state.selectedDeptCode = dept.code;
            updateKioskSummary(dept);
        });

        container.appendChild(card);
    });

    if (state.departments.length > 0) {
        state.selectedDeptCode = state.departments[0].code;
        updateKioskSummary(state.departments[0]);
    }
}

function initPrioritySelector() {
    const cards = document.querySelectorAll('.priority-card');
    cards.forEach(card => {
        card.addEventListener('click', () => {
            cards.forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            const radio = card.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        });
    });
}

function updateKioskSummary(dept) {
    document.getElementById('summaryDeptName').textContent = dept.name;
    document.getElementById('summaryEstWait').textContent = `~${dept.avgServiceMinutes} mins`;
}

async function issueNewToken() {
    if (!state.selectedDeptCode) {
        showToast('Please select a department first', 'warning');
        return;
    }

    const nameInput = document.getElementById('custNameInput');
    const phoneInput = document.getElementById('custPhoneInput');
    const priorityRadio = document.querySelector('input[name="priorityLevel"]:checked');

    const payload = {
        departmentCode: state.selectedDeptCode,
        customerName: nameInput.value.trim() || 'Walk-in Guest',
        customerPhone: phoneInput.value.trim() || '',
        priority: priorityRadio ? priorityRadio.value : 'REGULAR'
    };

    const submitBtn = document.getElementById('btnIssueTicket');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-arrow-repeat spin"></i> Issuing Token...';

    try {
        const res = await fetch('/api/queue/ticket', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error('Failed to issue token');
        const ticket = await res.json();

        // Show ticket modal
        displayTicketModal(ticket);
        showToast(`Token ${ticket.tokenNumber} issued successfully!`, 'success');

        // Reset form
        nameInput.value = '';
        phoneInput.value = '';

        // Refresh counts
        updateSystemMetrics();
        syncLiveBoard();
    } catch (err) {
        console.error(err);
        showToast('Error issuing digital token', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="bi bi-printer"></i> <span>Print Digital Token</span>';
    }
}

function displayTicketModal(ticket) {
    document.getElementById('modalDeptName').textContent = ticket.departmentName;
    document.getElementById('modalTokenNumber').textContent = ticket.tokenNumber;
    document.getElementById('modalCustName').textContent = ticket.customerName || 'Walk-in Guest';
    document.getElementById('modalAhead').textContent = ticket.ticketsAhead;
    document.getElementById('modalWait').textContent = `~${ticket.estimatedWaitMinutes} mins`;
    document.getElementById('modalPriorityPill').textContent = ticket.priorityLabel || ticket.priority;

    const dateStr = new Date().toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
    const timeStr = new Date().toLocaleTimeString('en-US', { hour12: true, hour: '2-digit', minute: '2-digit' });
    document.getElementById('modalIssuedTime').textContent = `${dateStr}, ${timeStr}`;

    const modal = document.getElementById('ticketModal');
    modal.classList.add('show');
}

function closeTicketModal() {
    document.getElementById('ticketModal').classList.remove('show');
}

function trackIssuedTicket() {
    const token = document.getElementById('modalTokenNumber').textContent;
    closeTicketModal();
    switchTab('track');
    document.getElementById('trackTokenInput').value = token;
    trackEnteredToken();
}

/* ==========================================================================
   TAB 2: LIVE PUBLIC DISPLAY BOARD LOGIC
   ========================================================================== */
async function syncLiveBoard() {
    try {
        const res = await fetch('/api/queue/board');
        if (!res.ok) return;
        const board = await res.json();

        // Handle Last Called Ticket Announcement & Sound
        if (board.lastCalledTicket) {
            const currentToken = board.lastCalledTicket.tokenNumber;
            const counterName = board.lastCalledTicket.counterName || `Counter ${board.lastCalledTicket.counterNumber}`;

            document.getElementById('heroToken').textContent = currentToken;
            document.getElementById('heroCounter').textContent = counterName.toUpperCase();
            document.getElementById('heroDept').textContent = board.lastCalledTicket.departmentName;

            if (state.lastAnnouncedToken !== currentToken) {
                state.lastAnnouncedToken = currentToken;
                announceVoiceCall(currentToken, counterName);
                flashNowCalledHero();
            }
        }

        // Render Active Counters
        renderDisplayCounters(board.activeCounters || []);

        // Render Next Up in Queue
        renderDisplayNextUp(board.nextUpTickets || []);

        // Render Department Waiting Summary
        renderDisplayDeptSummary(board.waitingCountByDepartment || {});
    } catch (err) {
        // Silently catch
    }
}

function renderDisplayCounters(counters) {
    const container = document.getElementById('displayCountersGrid');
    if (!container) return;

    container.innerHTML = '';
    counters.forEach(c => {
        const card = document.createElement('div');
        card.className = 'counter-tv-card';
        card.style.setProperty('--card-color', c.departmentColor || '#3b82f6');

        card.innerHTML = `
            <div class="tv-c-number">${c.counterName}</div>
            <div class="tv-c-dept">${c.departmentName}</div>
            <div class="tv-c-token">${c.currentToken || '--'}</div>
            <span class="tv-c-status status-${c.status}">${c.status}</span>
        `;
        container.appendChild(card);
    });
}

function renderDisplayNextUp(tickets) {
    const container = document.getElementById('displayNextUpList');
    if (!container) return;

    if (tickets.length === 0) {
        container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.88rem; padding: 10px;">Queue is currently clear. No pending tickets.</div>';
        return;
    }

    container.innerHTML = '';
    tickets.forEach(t => {
        const pill = document.createElement('div');
        pill.className = 'next-token-pill';
        pill.innerHTML = `
            <span class="pill-token">${t.tokenNumber}</span>
            <span class="pill-dept">${t.departmentCode} • ~${t.estimatedWaitMinutes}m</span>
        `;
        container.appendChild(pill);
    });
}

function renderDisplayDeptSummary(deptMap) {
    const container = document.getElementById('displayDeptSummaryList');
    if (!container) return;

    container.innerHTML = '';
    for (const [deptName, count] of Object.entries(deptMap)) {
        const row = document.createElement('div');
        row.className = 'dept-load-row';
        row.innerHTML = `
            <span class="dept-load-name">${deptName}</span>
            <span class="dept-load-badge">${count} Waiting</span>
        `;
        container.appendChild(row);
    }
}

function flashNowCalledHero() {
    const hero = document.getElementById('nowCalledHero');
    if (hero) {
        hero.classList.add('calling-anim');
        setTimeout(() => hero.classList.remove('calling-anim'), 6000);
    }
}

function announceVoiceCall(token, counter) {
    if (!state.voiceEnabled) return;

    playChimeBeep();

    if ('speechSynthesis' in window) {
        // Formulate clear, realistic airport/bank style announcement
        const phrase = `Token ${formatTokenForSpeech(token)}, please proceed to ${counter}`;
        const utterance = new SpeechSynthesisUtterance(phrase);
        utterance.rate = 0.92;
        utterance.pitch = 1.05;
        window.speechSynthesis.speak(utterance);
    }
}

function formatTokenForSpeech(token) {
    // e.g. "ACC-003" -> "A C C, zero zero three"
    const parts = token.split('-');
    if (parts.length === 2) {
        const letters = parts[0].split('').join(' ');
        return `${letters}, number ${parts[1]}`;
    }
    return token;
}

function playChimeBeep() {
    try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();

        osc.type = 'sine';
        osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
        osc.frequency.setValueAtTime(880.00, ctx.currentTime + 0.15); // A5

        gain.gain.setValueAtTime(0.2, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.45);

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.start();
        osc.stop(ctx.currentTime + 0.45);
    } catch (e) {
        // AudioContext restricted before first user gesture
    }
}

/* ==========================================================================
   TAB 3: COUNTER OPERATOR DESK LOGIC
   ========================================================================== */
function populateCounterSelector() {
    const select = document.getElementById('activeCounterSelect');
    if (!select) return;

    select.innerHTML = '';
    state.counters.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = `${c.counterName} - ${c.department.name} (${c.operatorName})`;
        select.appendChild(opt);
    });

    if (state.counters.length > 0) {
        state.selectedCounterId = state.counters[0].id;
    }
}

function onCounterSwitch() {
    const select = document.getElementById('activeCounterSelect');
    state.selectedCounterId = parseInt(select.value, 10);
    loadOperatorView();
}

async function loadOperatorView() {
    const counter = state.counters.find(c => c.id === state.selectedCounterId);
    if (!counter) return;

    document.getElementById('opCounterName').textContent = counter.counterName;
    document.getElementById('opDeptName').textContent = counter.department.name;
    document.getElementById('opStaffName').innerHTML = `<i class="bi bi-person-circle"></i> ${counter.operatorName}`;

    // Update status buttons
    updateCounterModeButtons(counter.status);

    // Fetch live board to check if currently serving
    const boardRes = await fetch('/api/queue/board');
    const board = await boardRes.json();
    const activeDisplay = (board.activeCounters || []).find(c => c.counterNumber === counter.counterNumber);

    if (activeDisplay && activeDisplay.currentToken && activeDisplay.currentToken !== '--') {
        document.getElementById('opCurrentToken').textContent = activeDisplay.currentToken;
        document.getElementById('opCustomerName').textContent = activeDisplay.customerName || 'Customer';
        document.getElementById('opPriorityTag').textContent = 'Currently in session';
        startServiceTimer();
    } else {
        document.getElementById('opCurrentToken').textContent = '--';
        document.getElementById('opCustomerName').textContent = 'No customer currently called';
        document.getElementById('opPriorityTag').textContent = 'Click "Call Next Customer" to begin';
        stopServiceTimer();
    }

    // Fetch department's waiting queue
    const waitingRes = await fetch(`/api/queue/waiting?departmentId=${counter.department.id}`);
    const waitingList = await waitingRes.json();

    document.getElementById('opWaitingQueueCount').textContent = waitingList.length;
    renderOperatorWaitingTable(waitingList);
}

function renderOperatorWaitingTable(tickets) {
    const tbody = document.getElementById('opWaitingTableBody');
    if (!tbody) return;

    if (tickets.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:var(--text-muted); padding:20px;">No customers currently waiting in this department.</td></tr>';
        return;
    }

    tbody.innerHTML = '';
    tickets.forEach((t, idx) => {
        const tr = document.createElement('tr');
        const pillClass = t.priority === 'VIP_URGENT' ? 'pill-VIP' : (t.priority === 'SENIOR_CITIZEN' ? 'pill-SENIOR' : 'pill-REGULAR');
        const waitMins = Math.max(1, Math.round((new Date() - new Date(t.issuedAt)) / 60000));

        tr.innerHTML = `
            <td class="token-cell">${t.tokenNumber}</td>
            <td>${t.customerName || 'Walk-in'}</td>
            <td><span class="priority-badge-pill ${pillClass}">${t.priority}</span></td>
            <td>~${waitMins} min</td>
            <td>#${idx + 1}</td>
        `;
        tbody.appendChild(tr);
    });
}

function startServiceTimer() {
    stopServiceTimer();
    state.serviceSecondsElapsed = 0;
    state.serviceTimerInterval = setInterval(() => {
        state.serviceSecondsElapsed++;
        const mins = Math.floor(state.serviceSecondsElapsed / 60).toString().padStart(2, '0');
        const secs = (state.serviceSecondsElapsed % 60).toString().padStart(2, '0');
        const el = document.getElementById('opServiceTimer');
        if (el) el.textContent = `${mins}:${secs}`;
    }, 1000);
}

function stopServiceTimer() {
    if (state.serviceTimerInterval) {
        clearInterval(state.serviceTimerInterval);
        state.serviceTimerInterval = null;
    }
    const el = document.getElementById('opServiceTimer');
    if (el) el.textContent = '00:00';
}

async function operatorCallNext() {
    try {
        const res = await fetch(`/api/queue/counter/${state.selectedCounterId}/call-next`, { method: 'POST' });
        const ticket = await res.json();

        if (!ticket) {
            showToast('No more waiting customers in queue!', 'info');
            loadOperatorView();
            return;
        }

        showToast(`Called Token ${ticket.tokenNumber}`, 'success');
        loadOperatorView();
        syncLiveBoard();
        updateSystemMetrics();
    } catch (err) {
        showToast('Error calling next ticket', 'error');
    }
}

async function operatorRecall() {
    try {
        const res = await fetch(`/api/queue/counter/${state.selectedCounterId}/recall`, { method: 'POST' });
        const ticket = await res.json();
        if (ticket) {
            showToast(`Re-announced Token ${ticket.tokenNumber}`, 'info');
            announceVoiceCall(ticket.tokenNumber, ticket.counterName || `Counter ${ticket.counterNumber}`);
            flashNowCalledHero();
        } else {
            showToast('No customer currently active to recall', 'warning');
        }
    } catch (err) {
        showToast('Error recalling customer', 'error');
    }
}

async function operatorComplete() {
    try {
        const res = await fetch(`/api/queue/counter/${state.selectedCounterId}/complete`, { method: 'POST' });
        const ticket = await res.json();

        if (ticket) {
            showToast(`Service completed for ${ticket.tokenNumber} (Duration: ${ticket.actualServiceMinutes}m)`, 'success');
        }
        loadOperatorView();
        syncLiveBoard();
        updateSystemMetrics();
    } catch (err) {
        showToast('Error completing ticket', 'error');
    }
}

async function operatorNoShow() {
    try {
        const res = await fetch(`/api/queue/counter/${state.selectedCounterId}/no-show`, { method: 'POST' });
        const ticket = await res.json();
        if (ticket) {
            showToast(`Marked ${ticket.tokenNumber} as No-Show`, 'warning');
        }
        loadOperatorView();
        syncLiveBoard();
        updateSystemMetrics();
    } catch (err) {
        showToast('Error marking no-show', 'error');
    }
}

async function setCounterStatus(status) {
    try {
        const res = await fetch(`/api/queue/counter/${state.selectedCounterId}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        const updated = await res.json();

        // Update local state
        const c = state.counters.find(x => x.id === state.selectedCounterId);
        if (c) c.status = updated.status;

        updateCounterModeButtons(updated.status);
        showToast(`Counter set to ${updated.status}`, 'info');
        syncLiveBoard();
        updateSystemMetrics();
    } catch (err) {
        showToast('Error updating counter mode', 'error');
    }
}

function updateCounterModeButtons(status) {
    document.getElementById('btnStatusAvailable').classList.toggle('active', status === 'AVAILABLE');
    document.getElementById('btnStatusBreak').classList.toggle('active', status === 'ON_BREAK');
    document.getElementById('btnStatusOffline').classList.toggle('active', status === 'OFFLINE');
}

/* ==========================================================================
   TAB 4: TRACK MY TOKEN LOGIC
   ========================================================================== */
async function trackEnteredToken() {
    const input = document.getElementById('trackTokenInput');
    const token = input.value.trim().toUpperCase();
    if (!token) {
        showToast('Please enter a valid token number', 'warning');
        return;
    }

    const container = document.getElementById('trackerResultContainer');
    container.innerHTML = '<div class="empty-state"><i class="bi bi-arrow-repeat spin"></i><p>Locating token in database...</p></div>';

    try {
        const res = await fetch(`/api/queue/track/${token}`);
        if (!res.ok) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="bi bi-question-circle text-rose"></i>
                    <p>Token <strong>${token}</strong> was not found in active records.</p>
                </div>
            `;
            return;
        }

        const ticket = await res.json();
        renderTrackerCard(ticket);
    } catch (err) {
        showToast('Error tracking ticket', 'error');
    }
}

function renderTrackerCard(t) {
    const container = document.getElementById('trackerResultContainer');
    const isWaiting = t.status === 'WAITING';
    const isServing = t.status === 'SERVING';
    const isCompleted = t.status === 'COMPLETED';

    container.innerHTML = `
        <div class="track-result-card">
            <div class="track-header-top">
                <div>
                    <div class="track-token-giant">${t.tokenNumber}</div>
                    <span style="color:var(--text-secondary); font-size:0.9rem;">${t.departmentName}</span>
                </div>
                <span class="track-status-pill status-${t.status}">${t.statusDescription}</span>
            </div>

            <!-- Stepper Progress -->
            <div class="track-stepper">
                <div class="step-node completed">
                    <div class="node-icon"><i class="bi bi-check-lg"></i></div>
                    <span class="node-label">Issued</span>
                </div>
                <div class="step-node ${isWaiting ? 'current' : (isServing || isCompleted ? 'completed' : '')}">
                    <div class="node-icon"><i class="bi bi-hourglass-split"></i></div>
                    <span class="node-label">In Queue</span>
                </div>
                <div class="step-node ${isServing ? 'current' : (isCompleted ? 'completed' : '')}">
                    <div class="node-icon"><i class="bi bi-bell"></i></div>
                    <span class="node-label">Called</span>
                </div>
                <div class="step-node ${isCompleted ? 'completed' : ''}">
                    <div class="node-icon"><i class="bi bi-check-circle"></i></div>
                    <span class="node-label">Done</span>
                </div>
            </div>

            <div class="track-metrics-grid">
                <div class="t-metric-box">
                    <span class="label">People Ahead in Line</span>
                    <span class="val text-amber">${isWaiting ? t.ticketsAhead : (isServing ? 'NOW SERVING' : '0')}</span>
                </div>
                <div class="t-metric-box">
                    <span class="label">Assigned Service Desk</span>
                    <span class="val text-blue">${t.counterName || (isWaiting ? 'Pending...' : '--')}</span>
                </div>
                <div class="t-metric-box">
                    <span class="label">Est. Remaining Wait</span>
                    <span class="val text-emerald">${isWaiting ? `~${t.estimatedWaitMinutes}m` : (isServing ? 'In Progress' : 'Completed')}</span>
                </div>
                <div class="t-metric-box">
                    <span class="label">Priority Class</span>
                    <span class="val text-purple">${t.priorityLabel || t.priority}</span>
                </div>
            </div>
        </div>
    `;
}

/* ==========================================================================
   TAB 5: CROWD PREDICTION & HEATMAP
   ========================================================================== */
function populatePredictionDeptSelector() {
    const select = document.getElementById('predictDeptSelect');
    if (!select) return;

    state.departments.forEach(dept => {
        const opt = document.createElement('option');
        opt.value = dept.id;
        opt.textContent = dept.name;
        select.appendChild(opt);
    });
}

async function loadPredictionData() {
    const daySelect = document.getElementById('predictDaySelect');
    const deptSelect = document.getElementById('predictDeptSelect');

    const day = daySelect ? daySelect.value : new Date().getDay() || 7;
    const deptId = deptSelect ? deptSelect.value : '';

    const url = `/api/prediction/forecast?day=${day}${deptId ? `&departmentId=${deptId}` : ''}`;

    try {
        const [forecastRes, trendsRes] = await Promise.all([
            fetch(url),
            fetch('/api/prediction/day-trends')
        ]);

        const forecast = await forecastRes.json();
        const trends = await trendsRes.json();

        // Populate Alert Insights
        document.getElementById('predPeakWindow').textContent = forecast.peakHourWindow || 'N/A';
        document.getElementById('predLowestWindow').textContent = forecast.lowestCrowdWindow || 'N/A';
        document.getElementById('predTotalCrowd').textContent = `${forecast.totalPredictedDailyCrowd} visitors`;
        document.getElementById('predAvgDailyWait').textContent = `${forecast.avgDailyPredictedWaitMinutes}m`;
        document.getElementById('recAlertNotice').textContent = forecast.crowdAlertNotice || '';

        // Populate Recommendation Pills
        const pillsContainer = document.getElementById('recSlotsPills');
        pillsContainer.innerHTML = '';
        (forecast.bestVisitSlots || []).forEach(slot => {
            const pill = document.createElement('span');
            pill.className = 'rec-slot-badge';
            pill.innerHTML = `<i class="bi bi-clock-fill"></i> ${slot}`;
            pillsContainer.appendChild(pill);
        });

        // Render Charts
        renderArrivalsChart(forecast.hourlyPredictions || []);
        renderWaitChart(forecast.hourlyPredictions || []);
        renderDayTrendsChart(trends || []);

        // Render Detailed Hourly Table
        renderHourlyPredictionTable(forecast.hourlyPredictions || []);
    } catch (err) {
        console.error('Error loading prediction data:', err);
        showToast('Error generating AI crowd forecast', 'error');
    }
}

function renderArrivalsChart(hourlyData) {
    const ctx = document.getElementById('hourlyArrivalsChart');
    if (!ctx) return;

    if (state.charts.arrivals) {
        state.charts.arrivals.destroy();
    }

    const labels = hourlyData.map(h => h.timeLabel);
    const arrivals = hourlyData.map(h => h.predictedArrivals);
    const counters = hourlyData.map(h => h.recommendedCounters);

    state.charts.arrivals = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Predicted Visitor Arrivals',
                    data: arrivals,
                    backgroundColor: 'rgba(59, 130, 246, 0.65)',
                    borderColor: '#3b82f6',
                    borderWidth: 2,
                    borderRadius: 6,
                    yAxisID: 'y'
                },
                {
                    label: 'Recommended Counters to Open',
                    data: counters,
                    type: 'line',
                    borderColor: '#f59e0b',
                    backgroundColor: '#f59e0b',
                    borderWidth: 3,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    tension: 0.3,
                    yAxisID: 'y1'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: '#94a3b8', font: { family: 'Plus Jakarta Sans', size: 12 } } }
            },
            scales: {
                x: { ticks: { color: '#64748b' }, grid: { color: 'rgba(255,255,255,0.05)' } },
                y: {
                    type: 'linear',
                    position: 'left',
                    title: { display: true, text: 'Visitors / Hour', color: '#60a5fa' },
                    ticks: { color: '#64748b' },
                    grid: { color: 'rgba(255,255,255,0.05)' }
                },
                y1: {
                    type: 'linear',
                    position: 'right',
                    title: { display: true, text: 'Active Desks', color: '#f59e0b' },
                    ticks: { color: '#f59e0b', stepSize: 1 },
                    grid: { drawOnChartArea: false }
                }
            }
        }
    });
}

function renderWaitChart(hourlyData) {
    const ctx = document.getElementById('hourlyWaitChart');
    if (!ctx) return;

    if (state.charts.wait) {
        state.charts.wait.destroy();
    }

    const labels = hourlyData.map(h => h.timeLabel);
    const waits = hourlyData.map(h => h.predictedWaitMinutes);

    state.charts.wait = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Estimated Waiting Time (Minutes)',
                data: waits,
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.15)',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: hourlyData.map(h => h.congestionColor || '#10b981'),
                pointRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: '#94a3b8' } }
            },
            scales: {
                x: { ticks: { color: '#64748b' }, grid: { color: 'rgba(255,255,255,0.05)' } },
                y: {
                    title: { display: true, text: 'Minutes Wait', color: '#34d399' },
                    ticks: { color: '#64748b' },
                    grid: { color: 'rgba(255,255,255,0.05)' }
                }
            }
        }
    });
}

function renderDayTrendsChart(trends) {
    const ctx = document.getElementById('dayTrendsChart');
    if (!ctx) return;

    if (state.charts.dayTrends) {
        state.charts.dayTrends.destroy();
    }

    const labels = trends.map(t => t.dayName);
    const footfalls = trends.map(t => t.estimatedDailyFootfall);
    const waitTimes = trends.map(t => t.avgWaitMinutes);

    state.charts.dayTrends = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Est. Total Day Footfall',
                    data: footfalls,
                    backgroundColor: 'rgba(99, 102, 241, 0.7)',
                    borderColor: '#6366f1',
                    borderRadius: 6,
                    yAxisID: 'y'
                },
                {
                    label: 'Avg Day Wait Time (Min)',
                    data: waitTimes,
                    type: 'line',
                    borderColor: '#ec4899',
                    backgroundColor: '#ec4899',
                    borderWidth: 2,
                    pointRadius: 4,
                    yAxisID: 'y1'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: '#94a3b8' } }
            },
            scales: {
                x: { ticks: { color: '#64748b' }, grid: { color: 'rgba(255,255,255,0.05)' } },
                y: {
                    position: 'left',
                    title: { display: true, text: 'Total Footfall', color: '#a5b4fc' },
                    ticks: { color: '#64748b' },
                    grid: { color: 'rgba(255,255,255,0.05)' }
                },
                y1: {
                    position: 'right',
                    title: { display: true, text: 'Avg Wait (min)', color: '#f472b6' },
                    ticks: { color: '#f472b6' },
                    grid: { drawOnChartArea: false }
                }
            }
        }
    });
}

function renderHourlyPredictionTable(hourlyList) {
    const tbody = document.getElementById('hourlyTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';
    hourlyList.forEach(h => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="font-family:var(--font-mono); font-weight:700; color:#93c5fd;">${h.timeLabel}</td>
            <td><strong>${h.predictedArrivals}</strong> visitors</td>
            <td>~${h.predictedWaitMinutes} mins</td>
            <td>
                <span class="congestion-pill" style="background:${h.congestionColor}20; color:${h.congestionColor}; border:1px solid ${h.congestionColor}50;">
                    ${h.congestionLevel}
                </span>
            </td>
            <td><strong class="text-amber">${h.recommendedCounters}</strong> Active Desks</td>
            <td><span class="text-emerald">${Math.round(h.confidenceScore * 100)}%</span> (High)</td>
        `;
        tbody.appendChild(tr);
    });
}

/* ==========================================================================
   TAB 6: DATABASE & SYSTEM OPS LOGIC
   ========================================================================== */
function openH2Console() {
    window.open('/h2-console', '_blank');
}

async function loadDatabaseAudit() {
    try {
        const [statsRes, boardRes] = await Promise.all([
            fetch('/api/admin/stats'),
            fetch('/api/queue/board')
        ]);

        const stats = await statsRes.json();
        const board = await boardRes.json();

        document.getElementById('dbCountDept').textContent = stats.totalDepartments;
        document.getElementById('dbCountCounter').textContent = stats.totalCounters;
        document.getElementById('dbCountTickets').textContent = stats.totalTicketsToday;

        const tbody = document.getElementById('dbAuditTableBody');
        tbody.innerHTML = '';

        // Combine serving and waiting tickets for audit display
        const combined = [...(board.currentlyServing || []), ...(board.nextUpTickets || [])];

        if (combined.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; color:var(--text-muted); padding:20px;">No queue tickets in database.</td></tr>';
            return;
        }

        combined.forEach(t => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td style="font-family:var(--font-mono); color:var(--text-muted);">#${t.id}</td>
                <td class="token-cell">${t.tokenNumber}</td>
                <td>${t.departmentName}</td>
                <td>${t.customerName || 'Walk-in'}</td>
                <td><span class="priority-badge-pill pill-${t.priority === 'VIP_URGENT' ? 'VIP' : (t.priority === 'SENIOR_CITIZEN' ? 'SENIOR' : 'REGULAR')}">${t.priority}</span></td>
                <td><span class="tv-c-status status-${t.status}">${t.status}</span></td>
                <td>${t.counterName || '--'}</td>
                <td style="font-family:var(--font-mono); font-size:0.78rem;">${t.issuedAt ? t.issuedAt.substring(11, 19) : '--'}</td>
                <td>${t.actualWaitMinutes ? `${t.actualWaitMinutes}m` : `~${t.estimatedWaitMinutes}m`}</td>
                <td>${t.actualServiceMinutes ? `${t.actualServiceMinutes}m` : '--'}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showToast('Error refreshing database audit log', 'error');
    }
}

async function reseedDatabase() {
    if (!confirm('Are you sure you want to reset and re-seed the database with fresh demo records and 30-day crowd history?')) {
        return;
    }

    try {
        const res = await fetch('/api/admin/reseed', { method: 'POST' });
        const result = await res.json();
        showToast(result.message || 'Database successfully reset!', 'success');
        loadInitialData();
        loadDatabaseAudit();
    } catch (err) {
        showToast('Error re-seeding database', 'error');
    }
}

/* ==========================================================================
   Toast Notification Helper
   ========================================================================== */
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let icon = 'bi-info-circle-fill';
    if (type === 'success') icon = 'bi-check-circle-fill';
    else if (type === 'warning') icon = 'bi-exclamation-triangle-fill';
    else if (type === 'error') icon = 'bi-x-circle-fill';

    toast.innerHTML = `<i class="bi ${icon}"></i> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}
