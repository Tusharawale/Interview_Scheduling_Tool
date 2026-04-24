let currentUser = null;
let profileData = null;

function getCurrentUserId() {
    if (!currentUser) return null;
    const raw = currentUser.id != null ? currentUser.id : currentUser.userId;
    if (raw === undefined || raw === null || raw === '') return null;
    const n = Number(raw);
    return Number.isFinite(n) && n > 0 ? n : null;
}

function escapeAttr(s) {
    if (s == null) return '';
    return String(s).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function isDocImagePath(path) {
    if (!path) return false;
    const p = path.toLowerCase();
    return p.endsWith('.png') || p.endsWith('.jpg') || p.endsWith('.jpeg') || p.endsWith('.gif') || p.endsWith('.webp');
}

function isDocPdfPath(path) {
    return path && String(path).toLowerCase().endsWith('.pdf');
}

function openDocPreviewModal(url, kind) {
    const modal = document.getElementById('docPreviewModal');
    const body = document.getElementById('docPreviewBody');
    if (!modal || !body) {
        window.open(url, '_blank', 'noopener,noreferrer');
        return;
    }
    modal.classList.add('is-open');
    modal.style.display = 'flex';
    if (kind === 'image') {
        body.innerHTML = '<img src="' + escapeAttr(url) + '" alt="" style="max-width:100%;max-height:75vh;display:block;margin:0 auto"/>';
    } else if (kind === 'pdf') {
        body.innerHTML = '<iframe title="PDF" src="' + escapeAttr(url) + '" style="width:100%;min-height:70vh;border:1px solid #ddd;border-radius:4px"></iframe>';
    } else {
        body.innerHTML = '<p><a href="' + escapeAttr(url) + '" target="_blank" rel="noopener">Open file in new tab</a></p>';
    }
}

function closeDocPreviewModal() {
    const modal = document.getElementById('docPreviewModal');
    const body = document.getElementById('docPreviewBody');
    if (modal) {
        modal.classList.remove('is-open');
        modal.style.display = 'none';
    }
    if (body) body.innerHTML = '';
}
const DEFAULT_PROFILE_IMG = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120"><rect width="100%25" height="100%25" fill="%23e9ecef"/><circle cx="60" cy="45" r="22" fill="%23adb5bd"/><rect x="28" y="76" width="64" height="30" rx="15" fill="%23adb5bd"/></svg>';

async function parseApiError(res) {
    const txt = await res.text();
    try {
        const json = JSON.parse(txt);
        return json.message || json.error || txt || ('HTTP ' + res.status);
    } catch (_) {
        return txt || ('HTTP ' + res.status);
    }
}

function escapeHtml(s) {
    if (s == null) return '';
    return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/** Normalize API date for HTML date inputs (yyyy-MM-dd). */
function dateInputValue(v) {
    if (v == null || v === '') return '';
    if (typeof v === 'string') return v.length >= 10 ? v.slice(0, 10) : v;
    return '';
}

function toNumberOrNull(v) {
    if (v == null || v === '') return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
}

function computeCgpaFromMarks(totalMarks, marksObtained) {
    if (!totalMarks || totalMarks <= 0 || marksObtained == null || marksObtained < 0) return null;
    const cgpa = (marksObtained / totalMarks) * 10;
    return Math.max(0, Math.min(10, cgpa)).toFixed(1);
}

/** Read current values from a row so Save works even if the user did not blur the field (onchange not fired). */
function readSkillRowFromDom(i) {
    const row = document.querySelector('#skillRows .pl-row[data-idx="' + i + '"]');
    const base = skillRows[i] || {};
    if (!row) return { ...base };
    const nameEl = row.querySelector('input.skill-name-input') || row.querySelector('input');
    const levelEl = row.querySelector('select.skill-level-select');
    return {
        id: base.id,
        skillName: (nameEl && nameEl.value != null ? nameEl.value : base.skillName || '').trim(),
        skillLevel: (levelEl && levelEl.value != null ? levelEl.value : base.skillLevel || '').trim()
    };
}

function readCertRowFromDom(i) {
    const row = document.querySelector('#certRows .pl-row[data-idx="' + i + '"]');
    const base = certRows[i] || {};
    if (!row) return { ...base };
    const ins = row.querySelectorAll('input:not([type="file"])');
    return {
        id: base.id,
        certificateName: ins[0] ? ins[0].value : (base.certificateName || ''),
        issuer: ins[1] ? ins[1].value : (base.issuer || ''),
        issueDate: ins[2] ? ins[2].value : (base.issueDate || '')
    };
}

function readPlRowFromDom(i) {
    const row = document.querySelector('#languageContainer .pl-lang-block[data-idx="' + i + '"]') ||
        document.querySelector('#plRows .pl-row[data-idx="' + i + '"]');
    const base = plRows[i] || {};
    if (!row) return { ...base };
    const sel = row.querySelector('select.pl-lang-select') || row.querySelector('select');
    const companyEl = row.querySelector('input.pl-lang-company') || row.querySelector('input.pl-lang-text');
    const levelEl = row.querySelector('select.pl-lang-level-select');
    return {
        id: base.id,
        languageName: sel ? sel.value : (base.languageName || ''),
        certificateCompany: companyEl ? companyEl.value : (base.certificateCompany || ''),
        proficiencyLevel: levelEl ? levelEl.value : (base.proficiencyLevel || '')
    };
}

function readEduRowFromDom(i) {
    const row = document.querySelector('#educationRows .edu-row[data-idx="' + i + '"]');
    const base = educationRows[i] || {};
    if (!row) return { ...base };
    const ins = row.querySelectorAll('input:not([type="file"])');
    return {
        ...base,
        collegeName: ins[0] ? ins[0].value : '',
        branch: ins[1] ? ins[1].value : '',
        semester: ins[2] ? ins[2].value : '',
        totalMarks: ins[3] ? ins[3].value : '',
        marksObtained: ins[4] ? ins[4].value : '',
        cgpa: ins[5] ? ins[5].value : '',
        startYear: ins[6] ? ins[6].value : '',
        endYear: ins[7] ? ins[7].value : ''
    };
}

function readExpRowFromDom(i) {
    const row = document.querySelector('#experienceRows .exp-row[data-idx="' + i + '"]');
    const base = experienceRows[i] || {};
    if (!row) return { ...base };
    const ins = row.querySelectorAll('input:not([type="file"])');
    return {
        ...base,
        companyName: ins[0] ? ins[0].value : '',
        jobRole: ins[1] ? ins[1].value : '',
        startDate: ins[2] ? ins[2].value : '',
        endDate: ins[3] ? ins[3].value : ''
    };
}

function renderSkillsSummary() {
    const el = document.getElementById('skillsContainer');
    if (!el || !profileData?.skills) { if (el) el.innerHTML = ''; return; }
    el.innerHTML = profileData.skills.map(s =>
        `<span class="skill-chip">${escapeHtml(s.skillName || '')}${s.skillLevel ? ' (' + escapeHtml(s.skillLevel) + ')' : ''}</span>`
    ).join('');
}

async function initEditPage() {
    currentUser = JSON.parse(sessionStorage.getItem('user') || localStorage.getItem('user') || 'null');
    if (!currentUser) { window.location.href = '/login.html'; return; }
    if (getCurrentUserId() == null) {
        alert('Your session is missing a valid user id. Please log in again.');
        window.location.href = '/login.html';
        return;
    }
    await loadProfile();
    renderEducationRows();
    renderExperienceRows();
    renderPlRows();
    renderSkillRows();
    renderCertRows();
    renderDocList();
    renderSkillsSummary();
}

async function loadProfile() {
    const userId = getCurrentUserId();
    if (userId == null) {
        window.location.href = '/login.html';
        return;
    }
    const res = await fetch('/api/users/' + userId + '/profile');
    if (!res.ok) {
        alert('Could not load profile: ' + await parseApiError(res));
        return;
    }
    profileData = await res.json();
    applyProfileToForm();
}

function applyProfileToForm() {
    if (!profileData) return;
    if (profileData.profile) {
        const p = profileData.profile;
        const preview = document.getElementById('editProfilePreview');
        if (preview) {
            if (p.profileImage) {
                let u = '/api/files/' + p.profileImage;
                if (window.__profilePhotoCacheBust) u += '?cb=' + window.__profilePhotoCacheBust;
                preview.src = u;
            } else {
                preview.src = DEFAULT_PROFILE_IMG;
            }
        }
        const fn = document.getElementById('firstName');
        if (fn) fn.value = p.firstName || '';
        const mid = document.getElementById('middleName');
        if (mid) mid.value = p.middleName || '';
        const ln = document.getElementById('lastName');
        if (ln) ln.value = p.lastName || '';
        const ph = document.getElementById('contactNumber');
        if (ph) ph.value = p.contactNumber || '';
        const g = document.getElementById('gender');
        if (g) g.value = p.gender || '';
        const dobEl = document.getElementById('dob');
        if (dobEl) dobEl.value = dateInputValue(p.dob);
        const c = document.getElementById('country');
        if (c) c.value = p.country || '';
        const st = document.getElementById('state');
        if (st) st.value = p.state || '';
        const ct = document.getElementById('city');
        if (ct) ct.value = p.city || '';
        const li = document.getElementById('linkedinUrl');
        if (li) li.value = p.linkedinUrl || '';
        const gh = document.getElementById('githubUrl');
        if (gh) gh.value = p.githubUrl || '';
        const emailEl = document.getElementById('email');
        if (emailEl) emailEl.value = (currentUser && currentUser.email) ? currentUser.email : '';
        const collegeSel = document.getElementById('collegeSelect');
        if (collegeSel && p.currentCollegeCode) {
            collegeSel.value = p.currentCollegeCode;
            collegeSel.dispatchEvent(new Event('change', { bubbles: true }));
        }
        setTimeout(function () {
            const branchSel = document.getElementById('branchSelect');
            if (branchSel && p.currentBranch) {
                branchSel.value = p.currentBranch;
            }
            const eduSel = document.getElementById('educationSelect');
            if (eduSel && p.currentCourse) {
                eduSel.value = p.currentCourse;
                eduSel.dispatchEvent(new Event('change', { bubbles: true }));
            }
        }, 0);
    } else {
        const preview = document.getElementById('editProfilePreview');
        if (preview) preview.src = DEFAULT_PROFILE_IMG;
    }
}

async function saveProfile() {
    const userId = getCurrentUserId();
    if (userId == null) {
        alert('Not logged in.');
        window.location.href = '/login.html';
        return;
    }
    const midEl = document.getElementById('middleName');
    const req = {
        firstName: document.getElementById('firstName')?.value ?? '',
        middleName: midEl ? midEl.value : null,
        lastName: document.getElementById('lastName')?.value ?? '',
        contactNumber: document.getElementById('contactNumber')?.value ?? '',
        gender: document.getElementById('gender')?.value ?? '',
        dob: document.getElementById('dob')?.value || null,
        country: document.getElementById('country')?.value ?? '',
        state: document.getElementById('state')?.value ?? '',
        city: document.getElementById('city')?.value ?? '',
        linkedinUrl: document.getElementById('linkedinUrl')?.value ?? '',
        githubUrl: document.getElementById('githubUrl')?.value ?? '',
        currentCollegeCode: document.getElementById('collegeSelect')?.value || null,
        currentBranch: document.getElementById('branchSelect')?.value || null,
        currentCourse: document.getElementById('educationSelect')?.value || null
    };
    const res = await fetch('/api/users/' + userId + '/profile', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(req)
    });
    if (res.ok) {
        const emailEl = document.getElementById('email');
        const newEmail = (emailEl?.value || '').trim();
        const currentEmail = ((currentUser && currentUser.email) || '').trim();
        if (newEmail && newEmail !== currentEmail) {
            const emailRes = await fetch('/api/users/' + userId + '/email', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: newEmail })
            });
            if (!emailRes.ok) {
                alert('Profile saved, but email update failed: ' + await parseApiError(emailRes));
            } else {
                if (currentUser) {
                    currentUser.email = newEmail;
                    sessionStorage.setItem('user', JSON.stringify(currentUser));
                    localStorage.setItem('user', JSON.stringify(currentUser));
                }
            }
        }
        profileData = await res.json();
        applyProfileToForm();
        renderEducationRows();
        renderExperienceRows();
        renderPlRows();
        renderSkillRows();
        renderCertRows();
        renderDocList();
        renderSkillsSummary();
        alert('Profile saved. Open the dashboard to see the latest details.');
    } else {
        alert('Failed: ' + await parseApiError(res));
    }
}

async function uploadProfilePhoto() {
    const userId = getCurrentUserId();
    if (userId == null) {
        window.location.href = '/login.html';
        return;
    }
    const file = document.getElementById('profilePhoto').files[0];
    if (!file) { alert('Select a file'); return; }
    const allowed = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!allowed.includes((file.type || '').toLowerCase())) {
        alert('Only JPG, JPEG, PNG, WEBP allowed for profile photo');
        return;
    }
    const fd = new FormData();
    fd.append('file', file);
    const res = await fetch('/api/users/' + userId + '/profile/photo', { method: 'POST', body: fd });
    if (res.ok) {
        window.__profilePhotoCacheBust = Date.now();
        try {
            const j = await res.json();
            const preview = document.getElementById('editProfilePreview');
            if (preview && j && j.url) preview.src = j.url + '?cb=' + window.__profilePhotoCacheBust;
        } catch (_) { /* non-JSON */ }
        alert('Photo uploaded');
        await loadProfile();
    } else alert('Failed: ' + await parseApiError(res));
}

let educationRows = [];
function renderEducationRows() {
    const host = document.getElementById('educationRows');
    if (!host) return;
    const list = profileData?.education || [];
    educationRows = list.length ? list.map(e => ({ ...e, startYear: e.startYear, endYear: e.endYear })) : [{}];
    const html = educationRows.map((r, i) => `
        <div class="edu-row" data-idx="${i}">
            <input placeholder="College" value="${r.collegeName || ''}" onchange="eduChange(${i},'collegeName',this.value)">
            <input placeholder="Branch" value="${r.branch || ''}" onchange="eduChange(${i},'branch',this.value)">
            <input type="number" placeholder="Sem" value="${r.semester || ''}" onchange="eduChange(${i},'semester',this.value)">
            <input type="number" placeholder="Total Marks" value="${r.totalMarks || ''}" onchange="eduChange(${i},'totalMarks',this.value)">
            <input type="number" placeholder="Marks Got" value="${r.marksObtained || ''}" onchange="eduChange(${i},'marksObtained',this.value)">
            <input placeholder="CGPA" value="${r.cgpa || ''}" onchange="eduChange(${i},'cgpa',this.value)">
            <input type="number" placeholder="Start Yr" value="${r.startYear || ''}" onchange="eduChange(${i},'startYear',this.value)">
            <input type="number" placeholder="End Yr" value="${r.endYear || ''}" onchange="eduChange(${i},'endYear',this.value)">
            <input type="file" id="eduFile${i}" accept=".pdf,.jpg,.jpeg,.png,.webp,.doc,.docx">
            ${r.documentPath ? `<a class="btn btn-orange" href="/api/files/${escapeAttr(r.documentPath)}" target="_blank" rel="noopener">View doc</a>` : ''}
            <button type="button" class="btn btn-orange" onclick="saveEducation(${i})">Save</button>
            ${r.id ? `<button type="button" class="btn btn-danger" onclick="delEducation(${r.id})">Delete</button>` : ''}
        </div>
    `).join('');
    host.innerHTML = html;
}
function eduChange(i, k, v) { if (!educationRows[i]) educationRows[i] = {}; educationRows[i][k] = v; }
function addEducationRow() {
    if (!profileData.education) profileData.education = [];
    profileData.education.push({});
    renderEducationRows();
}
async function saveEducation(i) {
    const userId = getCurrentUserId();
    const r = readEduRowFromDom(i);
    educationRows[i] = r;
    const fd = new FormData();
    const fileInp = document.getElementById('eduFile' + i);
    if (fileInp?.files[0]) fd.append('file', fileInp.files[0]);
    if (r.id) fd.append('id', r.id);
    if (r.collegeName) fd.append('collegeName', r.collegeName);
    if (r.branch) fd.append('branch', r.branch);
    if (r.semester) fd.append('semester', r.semester);
    if (r.totalMarks) fd.append('totalMarks', r.totalMarks);
    if (r.marksObtained) fd.append('marksObtained', r.marksObtained);
    if (r.cgpa) fd.append('cgpa', r.cgpa);
    if (r.startYear) fd.append('startYear', r.startYear);
    if (r.endYear) fd.append('endYear', r.endYear);
    const eduSel = document.getElementById('educationSelect');
    if (eduSel?.value) {
        const levelMap = { btech: 'B.Tech', diploma: 'Diploma', mtech: 'M.Tech' };
        fd.append('educationLevel', levelMap[eduSel.value] || eduSel.value);
    }
    const res = await fetch('/api/users/' + userId + '/education', { method: 'POST', body: fd });
    if (res.ok) { profileData = await res.json(); renderEducationRows(); } else alert('Failed: ' + await parseApiError(res));
}
async function delEducation(id) {
    const userId = getCurrentUserId();
    if (!confirm('Delete?')) return;
    const res = await fetch('/api/users/' + userId + '/education/' + id, { method: 'DELETE' });
    if (res.ok) { profileData = await res.json(); renderEducationRows(); } else alert('Failed: ' + await parseApiError(res));
}

let experienceRows = [];
function renderExperienceRows() {
    const host = document.getElementById('experienceRows');
    if (!host) return;
    const list = profileData?.experience || [];
    experienceRows = list.length ? list.map(e => ({ ...e })) : [{}];
    const html = experienceRows.map((r, i) => `
        <div class="exp-row" data-idx="${i}">
            <input placeholder="Company" value="${r.companyName || ''}" onchange="expChange(${i},'companyName',this.value)">
            <input placeholder="Job Role" value="${r.jobRole || ''}" onchange="expChange(${i},'jobRole',this.value)">
            <input type="date" placeholder="Start" value="${dateInputValue(r.startDate)}" onchange="expChange(${i},'startDate',this.value)">
            <input type="date" placeholder="End" value="${dateInputValue(r.endDate)}" onchange="expChange(${i},'endDate',this.value)">
            <input type="file" id="expFile${i}" accept=".pdf,.jpg,.jpeg,.png,.webp,.doc,.docx">
            ${r.documentPath ? `<a class="btn btn-orange" href="/api/files/${escapeAttr(r.documentPath)}" target="_blank" rel="noopener">View doc</a>` : ''}
            <button type="button" class="btn btn-orange" onclick="saveExperience(${i})">Save</button>
            ${r.id ? `<button type="button" class="btn btn-danger" onclick="delExperience(${r.id})">Delete</button>` : ''}
        </div>
    `).join('');
    host.innerHTML = html;
}
function expChange(i, k, v) { if (!experienceRows[i]) experienceRows[i] = {}; experienceRows[i][k] = v; }
function addExperienceRow() {
    if (!profileData.experience) profileData.experience = [];
    profileData.experience.push({});
    renderExperienceRows();
}
async function saveExperience(i) {
    const userId = getCurrentUserId();
    const r = readExpRowFromDom(i);
    experienceRows[i] = r;
    const fd = new FormData();
    const fileInp = document.getElementById('expFile' + i);
    if (fileInp?.files[0]) fd.append('file', fileInp.files[0]);
    if (r.id) fd.append('id', r.id);
    if (r.companyName) fd.append('companyName', r.companyName);
    if (r.jobRole) fd.append('jobRole', r.jobRole);
    if (r.startDate) fd.append('startDate', r.startDate);
    if (r.endDate) fd.append('endDate', r.endDate);
    const res = await fetch('/api/users/' + userId + '/experience', { method: 'POST', body: fd });
    if (res.ok) { profileData = await res.json(); renderExperienceRows(); } else alert('Failed: ' + await parseApiError(res));
}
async function delExperience(id) {
    const userId = getCurrentUserId();
    if (!confirm('Delete?')) return;
    const res = await fetch('/api/users/' + userId + '/experience/' + id, { method: 'DELETE' });
    if (res.ok) { profileData = await res.json(); renderExperienceRows(); } else alert('Failed: ' + await parseApiError(res));
}

const PROGRAMMING_LANGUAGES_LIST = [
    'C', 'C++', 'C#', 'Java', 'Python', 'JavaScript', 'TypeScript', 'Kotlin', 'Go', 'Rust', 'Swift',
    'Ruby', 'PHP', 'R', 'Dart', 'Scala', 'Haskell', 'MATLAB', 'Shell/Bash', 'PowerShell', 'SQL',
    'HTML/CSS', 'React', 'Node.js', 'Vue.js', 'Angular', '.NET', 'Spring Boot', 'Django', 'Flask'
];
let plRows = [];
function onPlCertFileChange(i, input) {
    const h = document.getElementById('plFileHint' + i);
    if (!h || !input) return;
    const f = input.files && input.files[0];
    if (!f) {
        h.textContent = '';
        return;
    }
    if (f.type === 'application/pdf' || (f.name && f.name.toLowerCase().endsWith('.pdf'))) {
        h.textContent = 'Selected: ' + f.name;
    } else {
        h.textContent = 'Only PDF files are allowed for this certificate.';
        input.value = '';
    }
}
function renderPlRows() {
    const host = document.getElementById('languageContainer') || document.getElementById('plRows');
    if (!host) return;
    const list = profileData?.programmingLanguages || [];
    plRows = list.length ? list.map(p => ({ ...p })) : [{}];
    const html = plRows.map((r, i) => {
        const path = r.certificateFile || '';
        const low = String(path).toLowerCase();
        const isPdf = low.endsWith('.pdf');
        const fileUrl = path ? ('/api/files/' + path) : '';
        let viewBtns = '';
        if (path && isPdf) {
            viewBtns =
                '<button type="button" class="btn btn-orange" onclick="openDocPreviewModal(' +
                JSON.stringify(fileUrl) +
                ',\'pdf\')">Preview PDF</button>' +
                '<a class="btn btn-orange" href="' +
                escapeAttr(fileUrl) +
                '" target="_blank" rel="noopener">Open</a>';
        } else if (path) {
            viewBtns =
                '<a class="btn btn-orange" href="' + escapeAttr(fileUrl) + '" target="_blank" rel="noopener">View file</a>';
        }
        const opts = PROGRAMMING_LANGUAGES_LIST.map(function (l) {
            return (
                '<option value="' +
                escapeAttr(l) +
                '"' +
                (r.languageName === l ? ' selected' : '') +
                '>' +
                escapeHtml(l) +
                '</option>'
            );
        }).join('');
        return (
            '<div class="pl-lang-block" data-idx="' +
            i +
            '">' +
            '<div class="pl-lang-grid">' +
            '<select class="pl-lang-select" aria-label="Programming language" onchange="plChange(' +
            i +
            ',\'languageName\',this.value)">' +
            '<option value="">Select programming language</option>' +
            opts +
            '</select>' +
            '<input class="pl-lang-company pl-lang-text" type="text" placeholder="Certificate company / issuer" value="' +
            escapeAttr(r.certificateCompany || '') +
            '" onchange="plChange(' +
            i +
            ',\'certificateCompany\',this.value)"/>' +
            '<select class="pl-lang-level-select" aria-label="Proficiency level" onchange="plChange(' +
            i +
            ',\'proficiencyLevel\',this.value)">' +
            '<option value="Beginner"' +
            (String(r.proficiencyLevel || '').toLowerCase() === 'beginner' ? ' selected' : '') +
            '>Beginner</option>' +
            '<option value="Intermediate"' +
            (String(r.proficiencyLevel || '').toLowerCase() === 'intermediate' ? ' selected' : '') +
            '>Intermediate</option>' +
            '<option value="Advanced"' +
            (String(r.proficiencyLevel || '').toLowerCase() === 'advanced' ? ' selected' : '') +
            '>Advanced</option>' +
            '</select>' +
            '<input type="file" id="plFile' +
            i +
            '" class="pl-lang-file-input" accept=".pdf,application/pdf" style="display:none" onchange="onPlCertFileChange(' +
            i +
            ',this)"/>' +
            '<button type="button" class="btn btn-pl-cert" onclick="document.getElementById(\'plFile' +
            i +
            '\').click()">Upload certificate (PDF)</button>' +
            viewBtns +
            '<button type="button" class="btn btn-pl-save" onclick="savePl(' +
            i +
            ')">Save</button>' +
            (r.id
                ? '<button type="button" class="btn btn-danger" onclick="delPl(' + r.id + ')">Delete</button>'
                : '') +
            '</div>' +
            '<small class="pl-lang-file-hint" id="plFileHint' +
            i +
            '"></small>' +
            '</div>'
        );
    }).join('');
    host.innerHTML = html;
}
function plChange(i, k, v) { if (!plRows[i]) plRows[i] = {}; plRows[i][k] = v; }
function addPlRow() {
    if (!profileData.programmingLanguages) profileData.programmingLanguages = [];
    profileData.programmingLanguages.push({});
    renderPlRows();
}
async function savePl(i) {
    const userId = getCurrentUserId();
    const r = readPlRowFromDom(i);
    plRows[i] = r;
    if (!r.languageName || !String(r.languageName).trim()) {
        alert('Select a programming language.');
        return;
    }
    const fd = new FormData();
    const fileInp = document.getElementById('plFile' + i);
    const f = fileInp && fileInp.files && fileInp.files[0];
    if (f) {
        const okPdf = f.type === 'application/pdf' || (f.name && f.name.toLowerCase().endsWith('.pdf'));
        if (!okPdf) {
            alert('Programming language certificate must be a PDF file.');
            return;
        }
        fd.append('file', f);
    }
    if (r.id) fd.append('id', r.id);
    if (r.languageName) fd.append('languageName', r.languageName);
    if (r.certificateCompany) fd.append('certificateCompany', r.certificateCompany);
    if (r.proficiencyLevel) fd.append('proficiencyLevel', r.proficiencyLevel);
    const res = await fetch('/api/users/' + userId + '/programming-languages', { method: 'POST', body: fd });
    if (res.ok) { profileData = await res.json(); renderPlRows(); } else alert('Failed: ' + await parseApiError(res));
}
async function delPl(id) {
    const userId = getCurrentUserId();
    if (!confirm('Delete?')) return;
    const res = await fetch('/api/users/' + userId + '/programming-languages/' + id, { method: 'DELETE' });
    if (res.ok) { profileData = await res.json(); renderPlRows(); } else alert('Failed: ' + await parseApiError(res));
}

let skillRows = [];
function renderSkillRows() {
    const host = document.getElementById('skillRows');
    if (!host) return;
    const list = profileData?.skills || [];
    skillRows = list.length ? list.map(s => ({ ...s })) : [{}];
    const html = skillRows.map((r, i) => `
        <div class="pl-row" data-idx="${i}">
            <input class="skill-name-input" placeholder="Skill Name" value="${r.skillName || ''}" onchange="skillChange(${i},'skillName',this.value)">
            <select class="skill-level-select" aria-label="Skill level" onchange="skillChange(${i},'skillLevel',this.value)">
              <option value="Beginner" ${String(r.skillLevel || '').toLowerCase() === 'beginner' ? 'selected' : ''}>Beginner</option>
              <option value="Intermediate" ${String(r.skillLevel || '').toLowerCase() === 'intermediate' ? 'selected' : ''}>Intermediate</option>
              <option value="Advanced" ${String(r.skillLevel || '').toLowerCase() === 'advanced' ? 'selected' : ''}>Advanced</option>
            </select>
            <button type="button" class="btn btn-orange" onclick="saveSkill(${i})">Save</button>
            ${r.id ? `<button type="button" class="btn btn-danger" onclick="delSkill(${r.id})">Delete</button>` : ''}
        </div>
    `).join('');
    host.innerHTML = html;
}
function skillChange(i, k, v) { if (!skillRows[i]) skillRows[i] = {}; skillRows[i][k] = v; }
function addSkillRow() {
    if (!profileData.skills) profileData.skills = [];
    profileData.skills.push({});
    renderSkillRows();
}
async function saveSkill(i) {
    const userId = getCurrentUserId();
    const r = readSkillRowFromDom(i);
    skillRows[i] = r;
    if (!r.skillName || !String(r.skillName).trim()) {
        alert('Enter a skill name.');
        return;
    }
    const fd = new FormData();
    if (r.id) fd.append('id', r.id);
    fd.append('skillName', r.skillName);
    if (r.skillLevel) fd.append('skillLevel', r.skillLevel);
    const res = await fetch('/api/users/' + userId + '/skills', { method: 'POST', body: fd });
    if (res.ok) { profileData = await res.json(); renderSkillRows(); renderSkillsSummary(); } else alert('Failed: ' + await parseApiError(res));
}
async function delSkill(id) {
    const userId = getCurrentUserId();
    if (!confirm('Delete?')) return;
    const res = await fetch('/api/users/' + userId + '/skills/' + id, { method: 'DELETE' });
    if (res.ok) { profileData = await res.json(); renderSkillRows(); renderSkillsSummary(); } else alert('Failed: ' + await parseApiError(res));
}

let certRows = [];
function renderCertRows() {
    const host = document.getElementById('certRows');
    if (!host) return;
    const list = profileData?.certificates || [];
    certRows = list.length ? list.map(c => ({ ...c })) : [{}];
    const html = certRows.map((r, i) => `
        <div class="pl-row" data-idx="${i}">
            <input placeholder="Certificate Name" value="${r.certificateName || ''}" onchange="certChange(${i},'certificateName',this.value)">
            <input placeholder="Issuer" value="${r.issuer || ''}" onchange="certChange(${i},'issuer',this.value)">
            <input type="date" placeholder="Issue Date" value="${dateInputValue(r.issueDate)}" onchange="certChange(${i},'issueDate',this.value)">
            <input type="file" id="certFile${i}" accept=".pdf,.jpg,.jpeg,.png,.webp,.doc,.docx">
            ${r.certificateFile ? `<a class="btn btn-orange" href="/api/files/${escapeAttr(r.certificateFile)}" target="_blank" rel="noopener">View file</a>` : ''}
            <button type="button" class="btn" onclick="saveCert(${i})">Save</button>
            ${r.id ? `<button type="button" class="btn btn-danger" onclick="delCert(${r.id})">Delete</button>` : ''}
        </div>
    `).join('');
    host.innerHTML = html;
}
function certChange(i, k, v) { if (!certRows[i]) certRows[i] = {}; certRows[i][k] = v; }
function addCertRow() {
    if (!profileData.certificates) profileData.certificates = [];
    profileData.certificates.push({});
    renderCertRows();
}
async function saveCert(i) {
    const userId = getCurrentUserId();
    const r = readCertRowFromDom(i);
    certRows[i] = r;
    const fileInp = document.getElementById('certFile' + i);
    const hasFile = fileInp && fileInp.files && fileInp.files[0];
    let certName = (r.certificateName && String(r.certificateName).trim()) || '';
    if (!certName && hasFile) {
        certName = fileInp.files[0].name.replace(/\.[^.]+$/, '') || 'Certificate';
    }
    if (!certName && !r.id) {
        alert('Enter a certificate name or attach a file.');
        return;
    }
    const fd = new FormData();
    if (hasFile) fd.append('file', fileInp.files[0]);
    if (r.id) fd.append('id', r.id);
    if (certName) fd.append('certificateName', certName);
    if (r.issuer) fd.append('issuer', r.issuer);
    if (r.issueDate) fd.append('issueDate', r.issueDate);
    const res = await fetch('/api/users/' + userId + '/certificates', { method: 'POST', body: fd });
    if (res.ok) { profileData = await res.json(); renderCertRows(); } else alert('Failed: ' + await parseApiError(res));
}
async function delCert(id) {
    const userId = getCurrentUserId();
    if (!confirm('Delete?')) return;
    const res = await fetch('/api/users/' + userId + '/certificates/' + id, { method: 'DELETE' });
    if (res.ok) { profileData = await res.json(); renderCertRows(); } else alert('Failed: ' + await parseApiError(res));
}

function renderDocList() {
    const host = document.getElementById('docList');
    if (!host) return;
    const list = profileData?.documents || [];
    const html = list.map(d => {
        const path = d.filePath || '';
        const url = '/api/files/' + path;
        const safeName = escapeHtml(d.documentName || 'Document');
        let previewBtn = '';
        if (isDocImagePath(path)) {
            previewBtn = '<button type="button" class="btn btn-orange" onclick="openDocPreviewModal(' + JSON.stringify(url) + ',\'image\')">Preview</button>';
        } else if (isDocPdfPath(path)) {
            previewBtn = '<button type="button" class="btn btn-orange" onclick="openDocPreviewModal(' + JSON.stringify(url) + ',\'pdf\')">Preview</button>';
        }
        return '<div class="doc-list-item">' +
            '<span>' + safeName + '</span>' +
            previewBtn +
            '<a href="' + escapeAttr(url) + '" target="_blank" rel="noopener" class="btn">Open</a>' +
            '<button type="button" class="btn btn-danger" onclick="delDocument(' + d.id + ')">Delete</button>' +
            '</div>';
    }).join('');
    host.innerHTML = html || '<p>No documents yet.</p>';
}
async function uploadDocument() {
    const userId = getCurrentUserId();
    if (userId == null) {
        window.location.href = '/login.html';
        return;
    }
    const file = document.getElementById('docFile').files[0];
    if (!file) { alert('Select a file'); return; }
    const ext = (file.name.split('.').pop() || '').toLowerCase();
    if (!['pdf', 'jpg', 'jpeg', 'png', 'gif', 'webp', 'doc', 'docx'].includes(ext)) {
        alert('Allowed: PDF, images (jpg, png, gif, webp), Word (doc, docx)');
        return;
    }
    const fd = new FormData();
    fd.append('file', file);
    fd.append('documentName', document.getElementById('docName').value || file.name);
    const res = await fetch('/api/users/' + userId + '/documents', { method: 'POST', body: fd });
    if (res.ok) {
        profileData = await res.json();
        renderDocList();
        const df = document.getElementById('docFile');
        const dn = document.getElementById('docName');
        if (df) df.value = '';
        if (dn) dn.value = '';
    } else alert('Failed: ' + await parseApiError(res));
}
async function delDocument(id) {
    const userId = getCurrentUserId();
    if (!confirm('Delete?')) return;
    const res = await fetch('/api/users/' + userId + '/documents/' + id, { method: 'DELETE' });
    if (res.ok) { profileData = await res.json(); renderDocList(); } else alert('Failed: ' + await parseApiError(res));
}

/** Semester-wise save (Qualification section in profile-form/form2.html) */
async function saveSemesterEducation(sem) {
    const userId = getCurrentUserId();
    if (userId == null) {
        window.location.href = '/login.html';
        return;
    }
    const collegeEl = document.getElementById('collegeSelect');
    const branchEl = document.getElementById('branchSelect');
    const eduEl = document.getElementById('educationSelect');
    if (!collegeEl?.value || !branchEl?.value || !eduEl?.value) {
        alert('Select college, branch, and course (B.Tech / Diploma / M.Tech) first.');
        return;
    }
    const collegeName = collegeEl.options[collegeEl.selectedIndex].text.trim();
    const branch = branchEl.options[branchEl.selectedIndex].text.trim();
    const levelMap = { btech: 'B.Tech', diploma: 'Diploma', mtech: 'M.Tech' };
    const educationLevel = levelMap[eduEl.value] || eduEl.value;

    const totalEl = document.getElementById('eduSemTotal' + sem);
    const marksEl = document.getElementById('eduSemMarks' + sem);
    const cgpaEl = document.getElementById('eduSemCgpa' + sem);
    const fileInp = document.getElementById('eduSemFile' + sem);
    const totalMarks = toNumberOrNull(totalEl?.value);
    const marksObtained = toNumberOrNull(marksEl?.value);

    if (totalMarks != null && totalMarks <= 0) {
        alert('Total marks must be greater than 0.');
        return;
    }
    if (marksObtained != null && marksObtained < 0) {
        alert('Marks obtained cannot be negative.');
        return;
    }
    if (totalMarks != null && marksObtained != null && marksObtained > totalMarks) {
        alert('Marks obtained cannot be more than total marks.');
        return;
    }
    if (cgpaEl && totalMarks != null && marksObtained != null) {
        cgpaEl.value = computeCgpaFromMarks(totalMarks, marksObtained);
    }

    const edu = profileData?.education || [];
    const existingBySem = edu.find(e => Number(e.semester) === Number(sem));

    const fd = new FormData();
    if (existingBySem?.id) fd.append('id', existingBySem.id);
    fd.append('collegeName', collegeName);
    fd.append('branch', branch);
    fd.append('educationLevel', educationLevel);
    fd.append('semester', String(sem));
    if (totalEl?.value) fd.append('totalMarks', totalEl.value);
    if (marksEl?.value) fd.append('marksObtained', marksEl.value);
    if (cgpaEl?.value) fd.append('cgpa', cgpaEl.value);
    if (fileInp?.files?.[0]) fd.append('file', fileInp.files[0]);

    const res = await fetch('/api/users/' + userId + '/education', { method: 'POST', body: fd });
    if (res.ok) {
        profileData = await res.json();
        alert('Semester ' + sem + ' saved');
        renderEducationRows();
        renderSkillsSummary();
    } else {
        alert('Failed: ' + await parseApiError(res));
    }
}

window.saveSemesterEducation = saveSemesterEducation;
window.addEducationRow = addEducationRow;
window.addExperienceRow = addExperienceRow;
window.addPlRow = addPlRow;
window.addSkillRow = addSkillRow;
window.addCertRow = addCertRow;
window.saveProfile = saveProfile;
window.uploadProfilePhoto = uploadProfilePhoto;
window.uploadDocument = uploadDocument;
window.openDocPreviewModal = openDocPreviewModal;
window.closeDocPreviewModal = closeDocPreviewModal;
window.onPlCertFileChange = onPlCertFileChange;

function logout() {
    sessionStorage.removeItem('user');
    localStorage.removeItem('user');
    window.location.href = '/login.html';
}
window.logout = logout;
