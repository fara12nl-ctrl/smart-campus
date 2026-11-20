// Smart Campus - web dashboard (vanilla JS, talks to the Spring Boot REST API)

const state = {
  token: localStorage.getItem('sc_token') || null,
  user: JSON.parse(localStorage.getItem('sc_user') || 'null'),
  view: 'dashboard',
  entity: null,
  rows: [],
};

const ENTITIES = {
  students: {
    title: 'Students',
    sub: 'Add, view, search and update student details',
    api: '/api/students',
    columns: [
      ['id', 'ID'], ['rollNo', 'Roll No'], ['name', 'Name'], ['email', 'Email'],
      ['phone', 'Phone'], ['department', 'Department'], ['year', 'Year'],
    ],
    fields: [
      { key: 'rollNo', label: 'Roll No', type: 'text', required: true },
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'email', label: 'Email', type: 'email', required: true },
      { key: 'phone', label: 'Phone', type: 'text' },
      { key: 'department', label: 'Department', type: 'text', required: true },
      { key: 'year', label: 'Year', type: 'number', required: true },
    ],
  },
  faculty: {
    title: 'Faculty',
    sub: 'Add, view, search and update faculty details',
    api: '/api/faculty',
    columns: [
      ['id', 'ID'], ['empId', 'Emp ID'], ['name', 'Name'], ['email', 'Email'],
      ['phone', 'Phone'], ['department', 'Department'], ['designation', 'Designation'],
    ],
    fields: [
      { key: 'empId', label: 'Employee ID', type: 'text', required: true },
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'email', label: 'Email', type: 'email', required: true },
      { key: 'phone', label: 'Phone', type: 'text' },
      { key: 'department', label: 'Department', type: 'text', required: true },
      { key: 'designation', label: 'Designation', type: 'text', required: true },
    ],
  },
  courses: {
    title: 'Courses',
    sub: 'Manage course information and faculty assignments',
    api: '/api/courses',
    columns: [
      ['id', 'ID'], ['code', 'Code'], ['title', 'Title'], ['credits', 'Credits'],
      ['semester', 'Semester'], ['facultyName', 'Faculty'],
    ],
    fields: [
      { key: 'code', label: 'Course Code', type: 'text', required: true },
      { key: 'title', label: 'Title', type: 'text', required: true },
      { key: 'credits', label: 'Credits', type: 'number' },
      { key: 'semester', label: 'Semester', type: 'number', required: true },
      { key: 'facultyId', label: 'Faculty ID (optional)', type: 'number' },
    ],
  },
  resources: {
    title: 'Classrooms & Labs',
    sub: 'Manage classrooms, laboratories and their capacity',
    api: '/api/resources',
    columns: [
      ['id', 'ID'], ['roomNo', 'Room No'], ['type', 'Type'], ['capacity', 'Capacity'], ['building', 'Building'],
    ],
    fields: [
      { key: 'roomNo', label: 'Room No', type: 'text', required: true },
      { key: 'type', label: 'Type', type: 'select', options: ['CLASSROOM', 'LAB'] },
      { key: 'capacity', label: 'Capacity', type: 'number', required: true },
      { key: 'building', label: 'Building', type: 'text', required: true },
    ],
  },
  equipment: {
    title: 'Equipment',
    sub: 'Track labs equipment and availability status',
    api: '/api/equipment',
    columns: [
      ['id', 'ID'], ['assetTag', 'Asset Tag'], ['name', 'Name'], ['category', 'Category'],
      ['roomId', 'Room ID'], ['status', 'Status'],
    ],
    fields: [
      { key: 'assetTag', label: 'Asset Tag', type: 'text', required: true },
      { key: 'name', label: 'Name', type: 'text', required: true },
      { key: 'category', label: 'Category', type: 'text', required: true },
      { key: 'roomId', label: 'Room ID (optional)', type: 'number' },
      { key: 'status', label: 'Status', type: 'select', options: ['AVAILABLE', 'IN_USE', 'MAINTENANCE'] },
    ],
  },
  events: {
    title: 'Campus Events',
    sub: 'Manage events and campus activities',
    api: '/api/events',
    columns: [
      ['id', 'ID'], ['title', 'Title'], ['eventDate', 'Date'], ['venue', 'Venue'], ['organizer', 'Organizer'],
    ],
    fields: [
      { key: 'title', label: 'Title', type: 'text', required: true },
      { key: 'eventDate', label: 'Date & Time', type: 'datetime-local', required: true },
      { key: 'venue', label: 'Venue', type: 'text', required: true },
      { key: 'organizer', label: 'Organizer', type: 'text', required: true },
      { key: 'description', label: 'Description', type: 'textarea', full: true },
    ],
  },
  bookings: {
    title: 'Resource Booking',
    sub: 'Check availability and book rooms / equipment without conflicts',
    api: '/api/bookings',
    editable: false,
    columns: [
      ['id', 'ID'], ['resourceType', 'Resource'], ['resourceId', 'Resource ID'], ['bookedBy', 'Booked By'],
      ['purpose', 'Purpose'], ['startTime', 'Start'], ['endTime', 'End'], ['status', 'Status'],
    ],
    fields: [
      { key: 'resourceType', label: 'Resource Type', type: 'select', options: ['ROOM', 'EQUIPMENT'] },
      { key: 'resourceId', label: 'Resource ID', type: 'number', required: true },
      { key: 'purpose', label: 'Purpose', type: 'text', required: true, full: true },
      { key: 'startTime', label: 'Start Time', type: 'datetime-local', required: true },
      { key: 'endTime', label: 'End Time', type: 'datetime-local', required: true },
    ],
  },
};

// ---------- helpers ----------
const $ = (id) => document.getElementById(id);
const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const prettyDate = (v) => (v ? String(v).replace('T', ' ').slice(0, 16) : '');

let toastTimer;
function toast(message, isError) {
  const t = $('toast');
  t.textContent = message;
  t.className = 'toast' + (isError ? ' err' : '');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.add('hidden'), 3200);
}

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (state.token) headers['Authorization'] = 'Bearer ' + state.token;
  const res = await fetch(path, { ...options, headers });
  let body = null;
  try { body = await res.json(); } catch { }
  if (res.status === 401 && state.token) {
    doLogout();
    throw new Error('Session expired, please login again');
  }
  if (!res.ok || (body && body.success === false)) {
    throw new Error((body && body.message) || ('HTTP ' + res.status));
  }
  return body ? body.data : null;
}

// ---------- auth ----------
$('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = e.target.querySelector('button');
  btn.disabled = true;
  try {
    const data = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username: $('login-username').value, password: $('login-password').value }),
    });
    state.token = data.token;
    state.user = data.user;
    localStorage.setItem('sc_token', state.token);
    localStorage.setItem('sc_user', JSON.stringify(state.user));
    enterApp();
  } catch (err) {
    const el = $('login-error');
    el.textContent = err.message;
    el.classList.remove('hidden');
  } finally {
    btn.disabled = false;
  }
});

function doLogout() {
  state.token = null;
  state.user = null;
  localStorage.removeItem('sc_token');
  localStorage.removeItem('sc_user');
  $('app').classList.add('hidden');
  $('login-screen').classList.remove('hidden');
  $('login-password').value = '';
}

$('logout-btn').addEventListener('click', async () => {
  try { await api('/api/auth/logout', { method: 'POST' }); } catch { }
  doLogout();
});

function enterApp() {
  $('login-screen').classList.add('hidden');
  $('app').classList.remove('hidden');
  $('user-chip').innerHTML = `<strong>${esc(state.user.displayName)}</strong>${esc(state.user.role)}`;
  switchView('dashboard');
}

// ---------- navigation ----------
$('nav').addEventListener('click', (e) => {
  const btn = e.target.closest('button');
  if (btn) switchView(btn.dataset.view);
});

function switchView(view) {
  state.view = view;
  document.querySelectorAll('#nav button').forEach((b) => b.classList.toggle('active', b.dataset.view === view));
  $('view-dashboard').classList.add('hidden');
  $('view-crud').classList.add('hidden');
  $('view-demo').classList.add('hidden');
  if (view === 'dashboard') {
    $('page-title').textContent = 'Dashboard';
    $('page-sub').textContent = `Welcome back, ${state.user.displayName}`;
    $('view-dashboard').classList.remove('hidden');
    renderDashboard().catch((err) => toast(err.message, true));
  } else if (view === 'demo') {
    $('page-title').textContent = 'Thread Demo';
    $('page-sub').textContent = 'Multithreading + synchronization proof';
    $('view-demo').classList.remove('hidden');
    renderDemo();
  } else {
    const cfg = ENTITIES[view];
    state.entity = view;
    $('page-title').textContent = cfg.title;
    $('page-sub').textContent = cfg.sub;
    $('search-box').value = '';
    $('add-btn').classList.toggle('hidden', false);
    $('view-crud').classList.remove('hidden');
    loadRows();
  }
}

// ---------- dashboard ----------
async function renderDashboard() {
  const [stats, bookings, events] = await Promise.all([
    api('/api/dashboard'),
    api('/api/bookings?status=CONFIRMED'),
    api('/api/events'),
  ]);
  const today = new Date().toISOString().slice(0, 10);
  const todays = bookings.filter((b) => (b.startTime || '').slice(0, 10) === today);
  const upcoming = events
    .filter((e) => (e.eventDate || '').slice(0, 10) >= today)
    .sort((a, b) => a.eventDate.localeCompare(b.eventDate))
    .slice(0, 5);

  const cards = [
    ['Students', stats.students], ['Faculty', stats.faculty], ['Courses', stats.courses],
    ['Rooms', stats.rooms], ['Equipment', stats.equipment], ['Events', stats.events],
  ].map(([label, value]) => `
    <div class="stat-card"><div class="label">${label}</div><div class="value accent">${value}</div></div>`).join('');

  const schedule = todays.length
    ? todays.map((b) => `
      <div class="list-item">
        <div><div>${esc(b.purpose || 'Booking')} <span class="t">(${esc(b.resourceType)} #${b.resourceId})</span></div>
        <div class="t">${prettyDate(b.startTime)} - ${prettyDate(b.endTime)}</div></div>
        <span class="badge good">CONFIRMED</span>
      </div>`).join('')
    : '<div class="empty">No bookings today - calm day ahead</div>';

  const eventList = upcoming.length
    ? upcoming.map((e) => `
      <div class="list-item">
        <div><div>${esc(e.title)}</div><div class="t">${esc(e.venue || '')}</div></div>
        <span class="badge">${prettyDate(e.eventDate).slice(0, 10)}</span>
      </div>`).join('')
    : '<div class="empty">No upcoming events</div>';

  const pct = stats.attendancePercent ?? 0;
  const C = 2 * Math.PI * 52;
  const dash = (pct / 100) * C;

  const quick = Object.keys(ENTITIES).map((k) =>
    `<button data-go="${k}"><span class="icon">&#9679;</span>${ENTITIES[k].title}</button>`).join('');

  $('view-dashboard').innerHTML = `
    <div class="stat-grid">${cards}</div>
    <div class="dash-grid">
      <div>
        <div class="card"><h3>Today's Schedule</h3><div class="list">${schedule}</div></div>
        <div class="card"><h3>Upcoming Events</h3><div class="list">${eventList}</div></div>
      </div>
      <div>
        <div class="card">
          <h3>Attendance</h3>
          <div class="ring-wrap">
            <svg class="ring" width="120" height="120" viewBox="0 0 120 120">
              <defs>
                <linearGradient id="ringGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#c084fc"/>
                  <stop offset="100%" stop-color="#6d28d9"/>
                </linearGradient>
              </defs>
              <circle class="track" cx="60" cy="60" r="52"/>
              <circle class="bar" cx="60" cy="60" r="52"
                stroke-dasharray="${dash.toFixed(1)} ${(C - dash).toFixed(1)}"/>
            </svg>
            <div>
              <div class="ring-label">${pct}%</div>
              <div class="ring-sub">Today's attendance<br>${stats.todaysBookings} bookings today</div>
            </div>
          </div>
        </div>
        <div class="card">
          <h3>Quick Access</h3>
          <div class="quick-grid">${quick}</div>
        </div>
      </div>
    </div>`;

  $('view-dashboard').querySelectorAll('[data-go]').forEach((btn) =>
    btn.addEventListener('click', () => switchView(btn.dataset.go)));
}

// ---------- generic CRUD ----------
async function loadRows(q) {
  const cfg = ENTITIES[state.entity];
  try {
    const url = q ? `${cfg.api}?q=${encodeURIComponent(q)}` : cfg.api;
    state.rows = await api(url);
    renderTable();
  } catch (err) {
    toast(err.message, true);
  }
}

function cellValue(row, key) {
  const v = row[key];
  if (v == null || v === '') return '<span class="muted">-</span>';
  if (key === 'status') {
    const cls = v === 'CONFIRMED' || v === 'AVAILABLE' ? 'good' : v === 'CANCELLED' || v === 'MAINTENANCE' ? 'bad' : 'warn';
    return `<span class="badge ${cls}">${esc(v)}</span>`;
  }
  if (key === 'startTime' || key === 'endTime' || key === 'eventDate') return esc(prettyDate(v));
  return esc(v);
}

function renderTable() {
  const cfg = ENTITIES[state.entity];
  if (!state.rows.length) {
    $('table-wrap').innerHTML = '<div class="empty">No records found</div>';
    return;
  }
  const head = cfg.columns.map(([, label]) => `<th>${label}</th>`).join('');
  const rows = state.rows.map((row) => {
    const cells = cfg.columns.map(([key]) => `<td>${cellValue(row, key)}</td>`).join('');
    const cancelBtn = state.entity === 'bookings' && row.status === 'CONFIRMED'
      ? `<button class="btn small danger" data-cancel="${row.id}">Cancel</button>` : '';
    const editBtn = cfg.editable === false ? '' : `<button class="btn small" data-edit="${row.id}">Edit</button>`;
    return `<tr>${cells}<td><div class="row-actions">${editBtn}${cancelBtn}
      <button class="btn small danger" data-del="${row.id}">Delete</button></div></td></tr>`;
  }).join('');

  $('table-wrap').innerHTML = `<table><thead><tr>${head}<th>Actions</th></tr></thead><tbody>${rows}</tbody></table>`;
}

$('table-wrap').addEventListener('click', async (e) => {
  const editId = e.target.dataset.edit;
  const delId = e.target.dataset.del;
  const cancelId = e.target.dataset.cancel;
  const cfg = ENTITIES[state.entity];
  try {
    if (editId) {
      const row = state.rows.find((r) => String(r.id) === editId);
      openForm(row);
    } else if (delId) {
      if (!confirm('Delete this record?')) return;
      await api(`${cfg.api}/${delId}`, { method: 'DELETE' });
      toast('Deleted');
      loadRows($('search-box').value.trim());
    } else if (cancelId) {
      await api(`${cfg.api}/${cancelId}/cancel`, { method: 'PUT' });
      toast('Booking cancelled');
      loadRows($('search-box').value.trim());
    }
  } catch (err) {
    toast(err.message, true);
  }
});

$('add-btn').addEventListener('click', () => openForm(null));

let searchTimer;
$('search-box').addEventListener('input', () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => loadRows($('search-box').value.trim()), 250);
});

// ---------- form modal ----------
function openForm(row) {
  const cfg = ENTITIES[state.entity];
  const isEdit = !!row;
  const inputs = cfg.fields.map((f) => {
    const val = row ? (row[f.key] ?? '') : '';
    let input;
    if (f.type === 'select') {
      input = `<select name="${f.key}">${f.options.map((o) =>
        `<option value="${o}" ${String(val) === o ? 'selected' : ''}>${o}</option>`).join('')}</select>`;
    } else if (f.type === 'textarea') {
      input = `<textarea name="${f.key}" rows="3">${esc(val)}</textarea>`;
    } else if (f.type === 'datetime-local') {
      const v = String(val).slice(0, 16);
      input = `<input type="datetime-local" name="${f.key}" value="${esc(v)}" ${f.required ? 'required' : ''}>`;
    } else {
      input = `<input type="${f.type}" name="${f.key}" value="${esc(val)}" ${f.required ? 'required' : ''}>`;
    }
    return `<label class="${f.full ? 'full' : ''}">${f.label}${input}</label>`;
  }).join('');

  $('modal-root').innerHTML = `
    <div class="modal-backdrop" id="modal-bg">
      <div class="modal">
        <h3>${isEdit ? 'Edit' : 'Add'} ${cfg.title.replace(/s$/, '')}</h3>
        <form id="modal-form">
          <div class="form-grid">${inputs}</div>
          <div class="modal-actions">
            <button type="button" class="btn ghost" id="modal-cancel">Cancel</button>
            <button type="submit" class="btn primary">${isEdit ? 'Save Changes' : 'Create'}</button>
          </div>
        </form>
      </div>
    </div>`;

  const close = () => { $('modal-root').innerHTML = ''; };
  $('modal-cancel').addEventListener('click', close);
  $('modal-bg').addEventListener('click', (e) => { if (e.target.id === 'modal-bg') close(); });

  $('modal-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = {};
    new FormData(e.target).forEach((v, k) => {
      if (v === '' || v == null) return;
      const field = cfg.fields.find((f) => f.key === k);
      body[k] = field && field.type === 'number' ? Number(v) : v;
    });
    try {
      if (isEdit) {
        await api(`${cfg.api}/${row.id}`, { method: 'PUT', body: JSON.stringify(body) });
        toast('Updated');
      } else {
        await api(cfg.api, { method: 'POST', body: JSON.stringify(body) });
        toast('Created');
      }
      close();
      loadRows($('search-box').value.trim());
    } catch (err) {
      toast(err.message, true);
    }
  });
}

// ---------- thread demo ----------
function renderDemo() {
  const tomorrow = new Date(Date.now() + 86400000);
  const pad = (n) => String(n).padStart(2, '0');
  const iso = (h) => `${tomorrow.getFullYear()}-${pad(tomorrow.getMonth() + 1)}-${pad(tomorrow.getDate())}T${pad(h)}:00`;
  $('view-demo').innerHTML = `
    <div class="card">
      <h3>Multithreading + Synchronization Demo</h3>
      <p class="muted" style="font-size:13px;margin-bottom:16px">
        N threads ek hi slot ke liye ek saath booking try karte hain. BookingService.book()
        synchronized hai, isliye sirf ek booking succeed hoti hai aur baaki conflict uthate hain.
      </p>
      <form id="race-form" class="form-grid">
        <label>Resource Type
          <select name="type"><option value="ROOM">ROOM</option><option value="EQUIPMENT">EQUIPMENT</option></select>
        </label>
        <label>Resource ID<input type="number" name="id" value="1" required></label>
        <label>Threads<input type="number" name="threads" value="5" min="2" max="20"></label>
        <label>Slot Start<input type="datetime-local" name="start" value="${iso(10)}"></label>
        <label>Slot End<input type="datetime-local" name="end" value="${iso(11)}"></label>
        <label class="full">&nbsp;<button class="btn primary" type="submit">Run Race</button></label>
      </form>
      <div id="race-result" style="margin-top:16px"></div>
    </div>`;

  $('race-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = new FormData(e.target);
    const q = new URLSearchParams({
      type: f.get('type'), id: f.get('id'), threads: f.get('threads'),
      start: f.get('start') + ':00', end: f.get('end') + ':00',
    });
    try {
      const r = await api('/api/demo/race?' + q.toString());
      $('race-result').innerHTML = `
        <div class="list">
          <div class="list-item"><span>Attempts</span><strong>${r.attempts}</strong></div>
          <div class="list-item"><span>Successful bookings</span><span class="badge good">${r.successes}</span></div>
          <div class="list-item"><span>Conflicts (rejected)</span><span class="badge bad">${r.conflicts}</span></div>
          <div class="list-item"><span>Duration</span><strong>${r.durationMs} ms</strong></div>
          <div class="list-item"><span>Winner</span><span class="badge">${esc((r.winners || []).join(', ') || '-')}</span></div>
        </div>`;
      toast('Race finished');
    } catch (err) {
      toast(err.message, true);
    }
  });
}

// ---------- boot ----------
function tick() {
  $('clock').textContent = new Date().toLocaleString();
}
tick();
setInterval(tick, 1000);

(async function boot() {
  if (!state.token) {
    $('login-screen').classList.remove('hidden');
    return;
  }
  try {
    state.user = await api('/api/auth/me');
    enterApp();
  } catch {
    doLogout();
  }
})();
