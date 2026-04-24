let currentUser=null;
let userBookings=[];
let interviewSlots=[];
const DEFAULT_PROFILE_IMG = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120"><rect width="100%25" height="100%25" fill="%23e9ecef"/><circle cx="60" cy="45" r="22" fill="%23adb5bd"/><rect x="28" y="76" width="64" height="30" rx="15" fill="%23adb5bd"/></svg>';

function escapeHtml(s){
    if(s==null) return '';
    const d=document.createElement('div');
    d.textContent=String(s);
    return d.innerHTML;
}

function safeHttpUrl(url){
    const s=(url||'').toString().trim();
    if(!s) return '';
    try{
        const u=new URL(s, window.location.origin);
        if(u.protocol!=='http:' && u.protocol!=='https:') return '';
        return u.toString();
    }catch(e){
        return '';
    }
}

async function loadUser(){

    const user=JSON.parse(sessionStorage.getItem('user') || localStorage.getItem('user') || 'null');

    if(!user){
        window.location.href='/login.html';
        return;
    }

    currentUser=user;

    const nameEl=document.getElementById("name");
    if(nameEl){ nameEl.textContent=user.username; }
    const idEl=document.getElementById("id");
    if(idEl){ idEl.textContent=user.id; }
    const emailEl=document.getElementById("email");
    if(emailEl){ emailEl.textContent=user.email; }
    const verifiedEl=document.getElementById("verified");
    if(verifiedEl){ verifiedEl.textContent=user.verified?"Verified":"Not Verified"; }

    await Promise.all([loadInterviewData(), loadProfileData()]);
}

async function loadUserProfile(){
    const user=JSON.parse(sessionStorage.getItem('user') || localStorage.getItem('user') || 'null');
    if(!user){
        window.location.href='/login.html';
        return;
    }
    currentUser=user;
    await loadProfileData();
}

function getDashboardUserId(){
    if(!currentUser||currentUser.id==null||currentUser.id==='') return null;
    const n=Number(currentUser.id);
    return Number.isFinite(n)&&n>0?n:null;
}

async function loadProfileData(){
    const uid=getDashboardUserId();
    if(uid==null) return;
    try {
        const res=await fetch('/api/users/'+uid+'/profile');
        if(!res.ok) return;
        const data=await res.json();

        // Profile
        const p=data.profile;
        const profileTopImage=document.getElementById('profileTopImage');
        if (profileTopImage) {
            let imgUrl = p && p.profileImage ? ('/api/files/' + p.profileImage) : DEFAULT_PROFILE_IMG;
            if (p && p.profileImage && window.__profilePhotoCacheBust) {
                imgUrl += '?cb=' + window.__profilePhotoCacheBust;
            }
            profileTopImage.src = imgUrl;
        }
        const profileEl=document.getElementById('profileContent');
        const profileSection=document.getElementById('profileSection');
        const courseLabels={btech:'B.Tech',diploma:'Diploma',mtech:'M.Tech'};
        const courseLabel=p&&p.currentCourse?(courseLabels[p.currentCourse]||p.currentCourse):'';

        if(p && (p.firstName||p.lastName||p.contactNumber||p.country||p.city||p.gender||p.dob||p.linkedinUrl||p.githubUrl||p.currentCourse)){
            const linkedin = safeHttpUrl(p.linkedinUrl);
            const github = safeHttpUrl(p.githubUrl);
            profileEl.innerHTML=`
                <table>
                    ${p.firstName||p.lastName?`<tr><th>Full Name</th><td>${escapeHtml([p.firstName,p.middleName,p.lastName].filter(Boolean).join(' '))}</td></tr>`:''}
                    ${courseLabel?`<tr><th>Programme</th><td>${escapeHtml(courseLabel)}</td></tr>`:''}
                    ${p.contactNumber?`<tr><th>Contact</th><td>${escapeHtml(p.contactNumber)}</td></tr>`:''}
                    ${p.gender?`<tr><th>Gender</th><td>${escapeHtml(p.gender)}</td></tr>`:''}
                    ${p.dob?`<tr><th>Date of Birth</th><td>${escapeHtml(p.dob)}</td></tr>`:''}
                    ${p.country?`<tr><th>Country</th><td>${escapeHtml(p.country)}</td></tr>`:''}
                    ${p.state?`<tr><th>State</th><td>${escapeHtml(p.state)}</td></tr>`:''}
                    ${p.city?`<tr><th>City</th><td>${escapeHtml(p.city)}</td></tr>`:''}
                    ${linkedin?`<tr><th>LinkedIn</th><td><a href="${linkedin}" target="_blank" rel="noopener">${escapeHtml(linkedin)}</a></td></tr>`:''}
                    ${github?`<tr><th>GitHub</th><td><a href="${github}" target="_blank" rel="noopener">${escapeHtml(github)}</a></td></tr>`:''}
                </table>
                ${(linkedin||github)?`
                  <div class="social-webviews">
                    ${linkedin?`
                      <div class="webview-card">
                        <div class="webview-header">
                          <div class="webview-title">LinkedIn web-view</div>
                          <a class="webview-open" href="${linkedin}" target="_blank" rel="noopener">Open</a>
                        </div>
                        <iframe src="${linkedin}" sandbox="allow-scripts allow-forms allow-popups allow-same-origin" referrerpolicy="no-referrer"></iframe>
                        <div class="webview-fallback">If this shows blank, LinkedIn blocks iframe embedding. Use the Open button.</div>
                      </div>
                    `:''}
                    ${github?`
                      <div class="webview-card">
                        <div class="webview-header">
                          <div class="webview-title">GitHub web-view</div>
                          <a class="webview-open" href="${github}" target="_blank" rel="noopener">Open</a>
                        </div>
                        <iframe src="${github}" sandbox="allow-scripts allow-forms allow-popups allow-same-origin" referrerpolicy="no-referrer"></iframe>
                        <div class="webview-fallback">If this shows blank, GitHub blocks iframe embedding. Use the Open button.</div>
                      </div>
                    `:''}
                  </div>
                `:''}
            `;
        } else if(profileEl){
            profileEl.innerHTML='<p>No profile data yet. Complete your profile to see your information here.</p>';
        }

        // Education - card view (grouped by college + branch + course level)
        const edu = data.education || [];
        const eduEl = document.getElementById('educationContent');
        const eduSection = document.getElementById('educationSection');
        if (edu.length > 0 && eduEl) {
            function levelWeight(lvl) {
                const s = (lvl || '').toString().trim().toLowerCase();
                const n = s.replace(/[^a-z0-9]/g, '');
                if (n.includes('mtech')) return 3;
                if (n.includes('btech')) return 2;
                if (n.includes('diploma')) return 1;
                return 0;
            }

            function levelLabel(lvl) {
                const s = (lvl || '').toString().trim();
                const n = s.toLowerCase().replace(/[^a-z0-9]/g, '');
                if (n.includes('mtech')) return 'M.Tech';
                if (n.includes('btech')) return 'B.Tech';
                if (n.includes('diploma')) return 'Diploma';
                return s || 'Education';
            }

            const groups = new Map();
            edu.forEach((e) => {
                const collegeName = e.collegeName || 'Education';
                const branch = e.branch || '';
                const educationLevel = e.educationLevel || '';
                const key = `${collegeName}__${branch}__${educationLevel}`;

                const g = groups.get(key) || { collegeName, branch, educationLevel, items: [] };
                g.items.push(e);
                groups.set(key, g);
            });

            const groupList = Array.from(groups.values()).sort((a, b) => {
                const wA = levelWeight(a.educationLevel);
                const wB = levelWeight(b.educationLevel);
                if (wA !== wB) return wB - wA; // importance: M.Tech > B.Tech > Diploma
                return (a.collegeName || '').localeCompare(b.collegeName || '');
            });

            eduEl.innerHTML =
                '<div class="card-grid">' +
                groupList
                    .map((g) => {
                        const w = levelWeight(g.educationLevel);
                        const bg =
                            w === 3
                                ? 'linear-gradient(135deg,#667eea,#764ba2)'
                                : w === 2
                                    ? 'linear-gradient(135deg,#11998e,#38ef7d)'
                                    : w === 1
                                        ? 'linear-gradient(135deg,#4facfe,#00f2fe)'
                                        : 'linear-gradient(135deg,#6c5ce7,#00cec9)';

                        const sortedItems = g.items
                            .slice()
                            .sort((x, y) => Number(x.semester ?? 999) - Number(y.semester ?? 999));

                        function rowHtml(x){
                            const sem = x.semester != null ? x.semester : '-';
                            const cgpaPart = x.cgpa != null ? `CGPA ${escapeHtml(x.cgpa)}` : '';
                            let marksPart = '';
                            const total = x.totalMarks;
                            const got = x.marksObtained;
                            if (total != null && got != null && Number(total) > 0) {
                                const pct = Math.round((Number(got) / Number(total)) * 100);
                                marksPart = ` | ${pct}%`;
                            }
                            const docLink = x.documentPath
                                ? `<a href="/api/files/${escapeHtml(x.documentPath)}" target="_blank" rel="noopener" class="view-btn edu-sem-link">View PDF</a>`
                                : '';
                            return `<div class="edu-sem-row">
                                <div class="edu-sem-meta">Sem ${escapeHtml(sem)} ${cgpaPart ? `• ${cgpaPart}` : ''}${marksPart}</div>
                                ${docLink}
                            </div>`;
                        }

                        const semHtmlFull = sortedItems
                            .map((x) => {
                                return rowHtml(x);
                            })
                            .join('');

                        const semHtmlPreview = sortedItems.slice(0, 2).map(rowHtml).join('');
                        const totalSems = sortedItems.filter(x => x.semester != null).length;
                        const docsCount = sortedItems.filter(x => !!x.documentPath).length;
                        const cgpas = sortedItems.map(x => x.cgpa != null ? Number(x.cgpa) : null).filter(v => v!=null && Number.isFinite(v));
                        const bestCgpa = cgpas.length ? Math.max.apply(null, cgpas) : null;

                        return `<details class="edu-details">
                            <summary>
                              <div class="card education-card" style="background:${bg}">
                                <div class="edu-summary">
                                  <div class="edu-card-title">📚 ${escapeHtml(g.collegeName || 'Education')}</div>
                                  <div class="edu-card-subtitle">${escapeHtml(levelLabel(g.educationLevel))}${g.branch ? ' • ' + escapeHtml(g.branch) : ''}</div>
                                  <div class="edu-sem-list">${semHtmlPreview}</div>
                                  <div class="edu-open-hint">Click to open • ${totalSems || sortedItems.length} semesters • ${docsCount} PDFs${bestCgpa!=null?` • Best CGPA ${bestCgpa}`:''}</div>
                                </div>
                              </div>
                            </summary>
                            <div class="card education-card" style="background:${bg};margin-top:10px">
                              <div class="edu-card-title">📚 ${escapeHtml(g.collegeName || 'Education')}</div>
                              <div class="edu-card-subtitle">${escapeHtml(levelLabel(g.educationLevel))}${g.branch ? ' • ' + escapeHtml(g.branch) : ''}</div>
                              <div class="edu-sem-list">${semHtmlFull}</div>
                            </div>
                        </details>`;
                    })
                    .join('') +
                '</div>';
        } else if (eduSection) {
            eduSection.style.display = 'none';
        }

        // Experience
        const exp=data.experience||[];
        const expEl=document.getElementById('experienceContent');
        const expSection=document.getElementById('experienceSection');
        if(exp.length>0 && expEl){
            expEl.innerHTML='<div class="card-grid">'+exp.map((e,i)=>{
                return `<div class="card" style="background:linear-gradient(135deg,#f093fb,#f5576c)">
                    <div><strong>${e.companyName||'Company'}</strong></div>
                    <div style="font-size:12px">${e.jobRole||''} | ${e.startDate||''} - ${e.endDate||'Present'}</div>
                    ${e.documentPath?`<a href="/api/files/${e.documentPath}" target="_blank" class="view-btn">View Letter</a>`:''}
                </div>`;
            }).join('')+'</div>';
        } else if(expSection){
            expSection.style.display='none';
        }

        // Skills & Programming Languages - card view
        const skills=data.skills||[];
        const pl=data.programmingLanguages||[];
        const skillsEl=document.getElementById('skillsContent');
        const skillsSection=document.getElementById('skillsSection');
        if((skills.length>0 || pl.length>0) && skillsEl){
            let html='';
            if(pl.length>0){
                const plColors=['#f1c40f','#3498db','#e74c3c','#00d2ff','#38ef7d','#9b59b6'];
                html+='<div class="card-grid">'+pl.map((x,i)=>`<div class="card" style="background:${plColors[i%plColors.length]}">
                    <div><strong>${x.languageName||'Lang'}</strong></div>
                    <div style="font-size:12px">${x.certificateCompany||x.proficiencyLevel||''}</div>
                    ${x.certificateFile?`<a href="/api/files/${x.certificateFile}" target="_blank" class="view-btn">VIEW</a>`:''}
                </div>`).join('')+'</div>';
            }
            if(skills.length>0) html+='<p style="margin-top:15px"><strong>Skills:</strong> '+skills.map(x=>`${x.skillName} (${x.skillLevel||''})`).join(', ')+'</p>';
            skillsEl.innerHTML=html;
        } else if(skillsSection){
            skillsSection.style.display='none';
        }

        // Certificates - card view
        const certs=data.certificates||[];
        const certEl=document.getElementById('certificatesContent');
        const certSection=document.getElementById('certificatesSection');
        if(certs.length>0 && certEl){
            certEl.innerHTML='<div class="card-grid">'+certs.map(c=>`<div class="card" style="background:linear-gradient(135deg,#667eea,#764ba2)">
                <div><strong>${c.certificateName||'Certificate'}</strong></div>
                <div style="font-size:12px">${c.issuer||''} | ${c.issueDate||''}</div>
                ${c.certificateFile?`<a href="/api/files/${c.certificateFile}" target="_blank" class="view-btn">View Certificate</a>`:''}
            </div>`).join('')+'</div>';
        } else if(certSection){
            certSection.style.display='none';
        }

        // Documents - card view
        const docs=data.documents||[];
        const docEl=document.getElementById('documentsContent');
        const docSection=document.getElementById('documentsSection');
        if(docs.length>0 && docEl){
            docEl.innerHTML='<div class="card-grid">'+docs.map(d=>{
                const fp=d.filePath||''
                const low=fp.toLowerCase()
                const url='/api/files/'+fp
                const isImg=low.endsWith('.png')||low.endsWith('.jpg')||low.endsWith('.jpeg')||low.endsWith('.gif')||low.endsWith('.webp')
                let thumb=isImg?`<img src="${url}" alt="" style="width:100%;max-height:100px;object-fit:contain;margin:8px 0;border-radius:4px"/>`:''
                return `<div class="card" style="background:linear-gradient(135deg,#4facfe,#00f2fe)">
                <div><strong>${d.documentName||'Document'}</strong></div>
                ${thumb}
                <a href="${url}" target="_blank" rel="noopener" class="view-btn">View / Download</a>
            </div>`
            }).join('')+'</div>';
        } else if(docSection){
            docSection.style.display='none';
        }

        if(window.ProfileAnalyticsUI){
            const uid=getDashboardUserId();
            if(uid!=null) ProfileAnalyticsUI.mountUserCharts(uid,'');
        }
        await loadLatestPosition();
    } catch(e) {
        ['profileSection','educationSection','experienceSection','skillsSection','certificatesSection','documentsSection','analyticsSection','positionSection'].forEach(id=>{
            const el=document.getElementById(id);
            if(el) el.style.display='none';
        });
        if(window.ProfileAnalyticsUI) ProfileAnalyticsUI.dispose();
    }
}

async function loadLatestPosition(){
    const uid=getDashboardUserId();
    const content=document.getElementById('positionContent');
    const section=document.getElementById('positionSection');
    if(!content||!section||uid==null) return;
    try{
        const res=await fetch('/api/interview-upgrade/report/final/user/'+uid);
        const txt=await res.text();
        let data={}; try{ data=txt?JSON.parse(txt):{} }catch(_e){}
        if(!res.ok){
            content.innerHTML='<p style="color:#666">No published report yet. Ask admin to publish final report.</p>';
            return;
        }
        if(!data.position){
            content.innerHTML='<p style="color:#666">Your position is not available in latest report.</p>';
            return;
        }
        content.innerHTML='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px;display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:10px">'+
            '<div><div style="font-size:12px;color:#666">Rank</div><div style="font-size:24px;font-weight:800;color:#6c5ce7">#'+escapeHtml(String(data.position))+'</div></div>'+
            '<div><div style="font-size:12px;color:#666">Total Candidates</div><div style="font-size:20px;font-weight:700">'+escapeHtml(String(data.totalCandidates||0))+'</div></div>'+
            '<div><div style="font-size:12px;color:#666">Final Score</div><div style="font-size:20px;font-weight:700">'+Number(data.finalScore||0).toFixed(2)+'/100</div></div>'+
            '<div><div style="font-size:12px;color:#666">Report Time</div><div style="font-size:14px;font-weight:600">'+escapeHtml(String(data.generatedAt||''))+'</div></div>'+
            '</div>';
    }catch(_e){
        content.innerHTML='<p style="color:#666">Could not load your latest position.</p>';
    }
}


async function loadInterviewData(){

    const slotsEl=document.getElementById("slots");
    if(!slotsEl) return;
    const uid=getDashboardUserId();
    if(uid==null) return;

    const [slotsRes,bookingsRes]=await Promise.all([
        fetch('/api/interviews/slots'),
        fetch('/api/interviews/bookings/user/'+uid)
    ]);

    interviewSlots=await slotsRes.json();
    userBookings=await bookingsRes.json();

    renderSlots();

}


function renderSlots(){

    const container=document.getElementById("slots");
    if(!container) return;

    container.innerHTML="";

    if(interviewSlots.length===0){
        container.innerHTML="<p>No interview slots available</p>";
        return;
    }

    const bookingMap={};

    userBookings.forEach(b=>{
        bookingMap[b.slotId]=b;
    });

    interviewSlots.forEach(slot=>{

        const booking=bookingMap[slot.id];

        const row=document.createElement("tr");

        const when=new Date(slot.scheduledAt).toLocaleString();

        row.innerHTML=`

<td>${slot.id}</td>
<td>${slot.title}</td>
<td>${when}</td>
<td>${slot.durationMinutes} min</td>
<td>${slot.capacity}</td>

<td>

${
            booking && booking.status==="BOOKED"
                ? `<button class="delete" onclick="cancelBooking(${booking.id})">Cancel</button>`
                : `<button class="create-btn" onclick="bookSlot(${slot.id})">Book</button>`
        }

</td>

`;

        container.appendChild(row);

    });

}


async function bookSlot(slotId){

    const res=await fetch('/api/interviews/slots/'+slotId+'/book',{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({userId:currentUser.id})
    });

    if(res.ok){
        loadInterviewData();
    }else{
        alert("Booking failed");
    }

}


async function cancelBooking(bookingId){

    const res=await fetch('/api/interviews/bookings/'+bookingId+'/cancel',{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({userId:currentUser.id})
    });

    if(res.ok){
        loadInterviewData();
    }else{
        alert("Cancel failed");
    }

}


function logout(){

    sessionStorage.removeItem("user");
    localStorage.removeItem("user");

    window.location.href="/login.html";

}