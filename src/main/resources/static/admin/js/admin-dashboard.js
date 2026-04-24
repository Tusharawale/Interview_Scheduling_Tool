async function loadAppointments(){

    if(!localStorage.getItem('admin')){
        window.location.href='/admin/admin-login.html'
        return
    }

    try{

        const res = await fetch('/api/admin/interviews/bookings')

        const bookings = await res.json()

        const tbody = document.getElementById('appointmentBody')

        tbody.innerHTML=''

        if(bookings.length===0){
            tbody.innerHTML='<tr><td colspan="7">No appointments found</td></tr>'
            return
        }

        bookings.forEach(b=>{

            const tr=document.createElement('tr')

            const when=new Date(b.scheduledAt).toLocaleString()
            const uid = Number(b.userId || (b.user && b.user.id) || (b.user && b.user.userId) || 0)

            const bookingId = Number(b.id||0)
            const safeUsername = (b.username||'')
            const safeEmail = (b.email||'')
            tr.innerHTML=`

<td>${b.id}</td>
<td>${b.slotTitle}</td>
<td>${when}</td>
<td>${b.username}</td>
<td>${b.email}</td>
<td>${b.status}</td>
<td><button class="create-btn" style="background:#3498db" onclick="openAppointmentUserInfoModal(${uid}, ${JSON.stringify(safeUsername)}, ${bookingId}, ${JSON.stringify(safeEmail)})">View User Info</button></td>

`

            tbody.appendChild(tr)

        })

    }catch(e){

        alert("Failed to load appointments")

    }

}

let appointmentModalCharts = []

function adminEscapeHtml(v){
    if(v==null) return ''
    return String(v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')
}

function destroyAppointmentModalCharts(){
    ;(appointmentModalCharts||[]).forEach(c=>{ try{ c.destroy() }catch(_e){} })
    appointmentModalCharts=[]
}

async function openAppointmentUserInfoModal(userId, username, bookingId, email){
    const modal=document.getElementById('appointmentUserInfoModal')
    const content=document.getElementById('appointmentUserInfoContent')
    if(!modal||!content||!userId) return
    modal.style.display='flex'
    content.innerHTML='<p>Loading...</p>'
    destroyAppointmentModalCharts()
    try{
        const [profileRes,analyticsRes]=await Promise.all([
            fetch('/api/users/'+userId+'/profile'),
            fetch('/api/users/'+userId+'/profile/analytics')
        ])
        const data=await profileRes.json()
        const analytics=analyticsRes.ok?await analyticsRes.json():null
        if(!profileRes.ok) throw new Error(data.message||'Failed to load')

        const p=data.profile||{}
        const profileImg=p.profileImage?('/api/files/'+p.profileImage):'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120"><rect width="100%25" height="100%25" fill="%23e9ecef"/><circle cx="60" cy="45" r="22" fill="%23adb5bd"/><rect x="28" y="76" width="64" height="30" rx="15" fill="%23adb5bd"/></svg>'
        const fullName=[p.firstName,p.middleName,p.lastName].filter(Boolean).join(' ')||username||('User '+userId)
        const location=[p.city,p.state,p.country].filter(Boolean).join(', ')||'Location not set'
        const programme=({btech:'B.Tech',diploma:'Diploma',mtech:'M.Tech'}[p.currentCourse]||p.currentCourse||'Not set')
        const edu=(data.education||[])
        const skills=(data.skills||[])
        const pl=(data.programmingLanguages||[])
        const exp=(data.experience||[])
        const certs=(data.certificates||[])
        const docs=(data.documents||[])

        let html=''
        html+='<div style="display:flex;gap:16px;align-items:center;margin-bottom:14px">'
        html+='<img src="'+profileImg+'" alt="Profile" style="width:120px;height:120px;border-radius:50%;border:3px solid #ddd;object-fit:cover;background:#f1f2f6"/>'
        html+='<div><h3 style="margin:0">'+adminEscapeHtml(fullName)+'</h3>'
        html+='<div style="margin-top:6px;color:#666">'+adminEscapeHtml(location)+'</div>'
        html+='<div style="margin-top:6px;color:#555">ID '+userId+' • '+adminEscapeHtml(programme)+'</div></div>'
        html+='</div>'
        html+='<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:12px;margin-top:8px">'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Email</strong><div style="margin-top:6px">'+adminEscapeHtml(email||'Not set')+'</div></div>'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Phone</strong><div style="margin-top:6px">'+adminEscapeHtml(p.contactNumber||'Not set')+'</div></div>'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Gender / DOB</strong><div style="margin-top:6px">'+adminEscapeHtml((p.gender||'NA')+' / '+(p.dob||'NA'))+'</div></div>'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Links</strong><div style="margin-top:6px;display:grid;gap:6px">'+
            '<div><span style="color:#666">LinkedIn:</span> '+adminEscapeHtml(p.linkedinUrl||'')+'</div>'+
            '<div><span style="color:#666">GitHub:</span> '+adminEscapeHtml(p.githubUrl||'')+'</div>'+
        '</div></div>'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Sections</strong><div style="margin-top:6px">Education: '+edu.length+', Experience: '+exp.length+', Skills: '+(skills.length+pl.length)+', Files: '+(certs.length+docs.length)+'</div></div>'
        html+='</div>'

        if(bookingId){
            html+='<h4 style="margin:16px 0 8px">Interview Evaluation (Admin)</h4>'
            html+='<div style="background:#fff;border:1px solid #ececec;border-radius:12px;padding:12px">'
            html+='<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:10px;align-items:end">'
            html+='<div><div style="font-size:13px;color:#444;margin-bottom:6px">Speaking duration (seconds)</div><input id="apptSpeakDur" type="number" min="10" step="1" value="60" style="width:100%;padding:10px 12px;border:1px solid #dadada;border-radius:10px"/></div>'
            html+='<div><div style="font-size:13px;color:#444;margin-bottom:6px">Behavioral score (0-100)</div><input id="apptBehScore" type="number" min="0" max="100" step="1" placeholder="e.g. 70" style="width:100%;padding:10px 12px;border:1px solid #dadada;border-radius:10px"/></div>'
            html+='<div><div style="font-size:13px;color:#444;margin-bottom:6px">Technical score (0-100)</div><input id="apptTechScore" type="number" min="0" max="100" step="1" placeholder="optional" style="width:100%;padding:10px 12px;border:1px solid #dadada;border-radius:10px"/></div>'
            html+='<div><button id="apptEvalBtn" class="create-btn" type="button" style="width:100%;background:#6c5ce7" onclick="submitAppointmentInterviewEvaluation('+bookingId+','+userId+','+JSON.stringify(username||'')+','+JSON.stringify(email||'')+')">Save + Calculate</button></div>'
            html+='</div>'
            html+='<div style="margin-top:10px">'
            html+='<div style="font-size:13px;color:#444;margin-bottom:6px">Transcript text (paste speech-to-text)</div>'
            html+='<textarea id="apptTranscript" rows="5" placeholder="Paste transcript here..." style="width:100%;padding:10px 12px;border:1px solid #dadada;border-radius:10px;box-sizing:border-box"></textarea>'
            html+='</div>'
            html+='<div id="apptEvalStatus" style="margin-top:8px;font-size:13px;color:#666"></div>'
            html+='</div>'
        }

        if(analytics){
            html+='<h4 style="margin:16px 0 8px">Interview Scorecards</h4>'
            html+='<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:12px">'
            html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Communication</strong><div style="margin-top:6px;font-size:18px"><b>'+num(analytics.communicationScore)+'</b>/100</div></div>'
            html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Technical</strong><div style="margin-top:6px;font-size:18px"><b>'+num(analytics.technicalScore)+'</b>/100</div></div>'
            html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Behavioral</strong><div style="margin-top:6px;font-size:18px"><b>'+num(analytics.behavioralScore)+'</b>/100</div></div>'
            html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Final</strong><div style="margin-top:6px;font-size:18px"><b>'+num(analytics.interviewFinalScore)+'</b>/100</div></div>'
            html+='</div>'
        }

        html+='<h4 style="margin:16px 0 8px">Profile Details</h4>'
        html+='<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:12px">'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Education</strong><div style="margin-top:8px">'+
            (edu.length?('<div style="display:grid;gap:8px">'+edu.map(e=>'<div style="border:1px solid #f0f0f0;border-radius:8px;padding:10px">'+
                '<div><b>'+adminEscapeHtml(e.educationLevel||'')+'</b> • '+adminEscapeHtml(e.collegeName||'')+'</div>'+
                '<div style="color:#666;font-size:13px;margin-top:4px">'+adminEscapeHtml([e.branch,e.startYear&&e.endYear?(e.startYear+'-'+e.endYear):''].filter(Boolean).join(' • '))+'</div>'+
                (e.totalMarks!=null?('<div style="color:#444;font-size:13px;margin-top:4px">Marks: '+adminEscapeHtml(String(e.marksObtained||0))+' / '+adminEscapeHtml(String(e.totalMarks||0))+' • CGPA: '+adminEscapeHtml(String(e.cgpa||''))+'</div>'):'')+
                (e.documentPath?('<div style="margin-top:6px"><a href="/api/files/'+encodeURIComponent(e.documentPath)+'" target="_blank">View document</a></div>'):'')+
            '</div>').join('')+'</div>'):'<div style="color:#666">No education added.</div>')+
        '</div></div>'

        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Experience</strong><div style="margin-top:8px">'+
            (exp.length?('<div style="display:grid;gap:8px">'+exp.map(x=>'<div style="border:1px solid #f0f0f0;border-radius:8px;padding:10px">'+
                '<div><b>'+adminEscapeHtml(x.companyName||'')+'</b> • '+adminEscapeHtml(x.jobRole||'')+'</div>'+
                '<div style="color:#666;font-size:13px;margin-top:4px">'+adminEscapeHtml([x.startDate,x.endDate].filter(Boolean).join(' to ')||'')+'</div>'+
                (x.description?('<div style="color:#444;font-size:13px;margin-top:6px">'+adminEscapeHtml(x.description)+'</div>'):'')+
                (x.documentPath?('<div style="margin-top:6px"><a href="/api/files/'+encodeURIComponent(x.documentPath)+'" target="_blank">View document</a></div>'):'')+
            '</div>').join('')+'</div>'):'<div style="color:#666">No experience added.</div>')+
        '</div></div>'
        html+='</div>'

        html+='<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:12px;margin-top:12px">'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Skills</strong><div style="margin-top:8px">'+
            (skills.length?('<div style="display:flex;flex-wrap:wrap;gap:8px">'+skills.map(s=>'<span style="background:#f5f6ff;border:1px solid #e8eaff;padding:6px 10px;border-radius:999px;font-size:13px">'+adminEscapeHtml((s.skillName||'Skill')+' ('+(s.skillLevel||'')+')')+'</span>').join('')+'</div>'):'<div style="color:#666">No skills added.</div>')+
        '</div></div>'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Programming Languages</strong><div style="margin-top:8px">'+
            (pl.length?('<div style="display:flex;flex-wrap:wrap;gap:8px">'+pl.map(s=>'<span style="background:#f0fffb;border:1px solid #ddfff5;padding:6px 10px;border-radius:999px;font-size:13px">'+adminEscapeHtml((s.languageName||'Lang')+' ('+(s.proficiencyLevel||'')+')')+'</span>').join('')+'</div>'):'<div style="color:#666">No languages added.</div>')+
        '</div></div>'
        html+='</div>'

        html+='<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:12px;margin-top:12px">'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Certificates</strong><div style="margin-top:8px">'+
            (certs.length?('<div style="display:grid;gap:8px">'+certs.map(c=>'<div style="border:1px solid #f0f0f0;border-radius:8px;padding:10px">'+
                '<div><b>'+adminEscapeHtml(c.certificateName||'Certificate')+'</b></div>'+
                '<div style="color:#666;font-size:13px;margin-top:4px">'+adminEscapeHtml([c.issuer,c.issueDate].filter(Boolean).join(' • '))+'</div>'+
                (c.certificateFile?('<div style="margin-top:6px"><a href="/api/files/'+encodeURIComponent(c.certificateFile)+'" target="_blank">View file</a></div>'):'')+
            '</div>').join('')+'</div>'):'<div style="color:#666">No certificates uploaded.</div>')+
        '</div></div>'
        html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><strong>Documents</strong><div style="margin-top:8px">'+
            (docs.length?('<div style="display:grid;gap:8px">'+docs.map(d=>'<div style="border:1px solid #f0f0f0;border-radius:8px;padding:10px">'+
                '<div><b>'+adminEscapeHtml(d.documentName||'Document')+'</b></div>'+
                (d.filePath?('<div style="margin-top:6px"><a href="/api/files/'+encodeURIComponent(d.filePath)+'" target="_blank">View file</a></div>'):'')+
            '</div>').join('')+'</div>'):'<div style="color:#666">No documents uploaded.</div>')+
        '</div></div>'
        html+='</div>'

        if(analytics){
            html+='<h4 style="margin:16px 0 8px">Analytics</h4>'
            html+='<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:12px">'
            html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><canvas id="appointmentUserSectionsChart" height="180"></canvas></div>'
            html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px"><canvas id="appointmentUserSkillsChart" height="180"></canvas></div>'
            html+='<div style="background:#fff;border:1px solid #ececec;border-radius:10px;padding:12px;grid-column:1/-1"><canvas id="appointmentUserRadarChart" height="180"></canvas></div>'
            html+='</div>'
        }
        content.innerHTML=html

        if(analytics&&window.Chart){
            const secCtx=document.getElementById('appointmentUserSectionsChart')
            const skillCtx=document.getElementById('appointmentUserSkillsChart')
            const radarCtx=document.getElementById('appointmentUserRadarChart')
            if(secCtx&&(analytics.sectionCounts||[]).length){
                appointmentModalCharts.push(new Chart(secCtx,{type:'bar',data:{labels:analytics.sectionCounts.map(x=>x.name),datasets:[{label:'Count',data:analytics.sectionCounts.map(x=>x.value),backgroundColor:'#6c5ce7'}]},options:{plugins:{legend:{display:false},title:{display:true,text:'Profile depth'}}}}))
            }
            if(skillCtx&&(analytics.skillLevelDistribution||[]).length){
                appointmentModalCharts.push(new Chart(skillCtx,{type:'doughnut',data:{labels:analytics.skillLevelDistribution.map(x=>x.name),datasets:[{data:analytics.skillLevelDistribution.map(x=>x.value),backgroundColor:['#6c5ce7','#00b894','#fdcb6e','#e17055']}]},options:{plugins:{title:{display:true,text:'Skill level'}}}}))
            }
            if(radarCtx&&analytics.radarCompare&&analytics.radarCompare.labels){
                appointmentModalCharts.push(new Chart(radarCtx,{type:'radar',data:{labels:analytics.radarCompare.labels,datasets:[{label:'User',data:analytics.radarCompare.userScores||[],borderColor:'#6c5ce7',backgroundColor:'rgba(108,92,231,0.2)'},{label:'Cohort',data:analytics.radarCompare.cohortScores||[],borderColor:'#b2bec3',backgroundColor:'rgba(178,190,195,0.12)'}]},options:{plugins:{title:{display:true,text:'User vs cohort'}}}}))
            }
        }
    }catch(e){
        content.innerHTML='<p style="color:red">'+adminEscapeHtml(e.message)+'</p>'
    }
}

function closeAppointmentUserInfoModal(){
    const modal=document.getElementById('appointmentUserInfoModal')
    if(modal) modal.style.display='none'
    destroyAppointmentModalCharts()
}

async function submitAppointmentInterviewEvaluation(bookingId,userId,username,email){
    const btn=document.getElementById('apptEvalBtn')
    const status=document.getElementById('apptEvalStatus')
    const transcriptEl=document.getElementById('apptTranscript')
    const durEl=document.getElementById('apptSpeakDur')
    const behEl=document.getElementById('apptBehScore')
    const techEl=document.getElementById('apptTechScore')
    if(!bookingId||!userId) return
    if(status) status.textContent='Saving...'
    if(btn) btn.disabled=true
    try{
        const payload={
            bookingId:Number(bookingId),
            transcriptText: transcriptEl?String(transcriptEl.value||''):'',
            speakingDurationSeconds: durEl?Number(durEl.value||60):60
        }
        const beh=behEl&&behEl.value!==''?Number(behEl.value):null
        const tech=techEl&&techEl.value!==''?Number(techEl.value):null
        if(beh!=null) payload.behavioralScore=beh
        if(tech!=null) payload.technicalScore=tech
        await apiJson('/api/interview-upgrade/communication/score',{
            method:'POST',
            headers:{'Content-Type':'application/json'},
            body:JSON.stringify(payload)
        })
        if(status) status.textContent='Saved. Scores updated.'
        await openAppointmentUserInfoModal(Number(userId), username, Number(bookingId), email)
    }catch(e){
        if(status) status.textContent='Save failed: '+(e.message||'Error')
    }finally{
        if(btn) btn.disabled=false
    }
}

function goDashboard(){
    window.location.href='admin.html'
}
function logout(){

    localStorage.removeItem('admin')
    localStorage.removeItem('adminMeetingToken')
    window.location.href='/admin/admin-login.html'

}

async function loadAdminSettingsPage(){
    if(!localStorage.getItem('admin')){
        window.location.href='/admin/admin-login.html'
        return
    }
    await Promise.all([loadRankingWeights(), loadRankingBoard(), loadCodingChallenges(), checkJudge0Status(), loadFinalReport()])
}

async function loadRankingWeights(){
    try{
        const res=await fetch('/api/interview-upgrade/ranking/weights')
        if(!res.ok) return
        const w=await res.json()
        document.getElementById('wCommunication').value=w.communicationWeight??35
        document.getElementById('wTechnical').value=w.technicalWeight??40
        document.getElementById('wBehavioral').value=w.behavioralWeight??15
        document.getElementById('wProfile').value=w.profileWeight??10
    }catch(_e){}
}

async function saveRankingWeights(event){
    event.preventDefault()
    const btn=event.target.querySelector('button[type="submit"]')
    if(btn) btn.disabled=true
    const payload={
        communicationWeight:parseFloat(document.getElementById('wCommunication').value||'0'),
        technicalWeight:parseFloat(document.getElementById('wTechnical').value||'0'),
        behavioralWeight:parseFloat(document.getElementById('wBehavioral').value||'0'),
        profileWeight:parseFloat(document.getElementById('wProfile').value||'0')
    }
    try{
        const data=await apiJson('/api/interview-upgrade/ranking/weights',{
            method:'PUT',
            headers:{'Content-Type':'application/json'},
            body:JSON.stringify(payload)
        })
        if(!data) return
        alert('Ranking weights saved')
        await loadRankingWeights()
        await loadRankingBoard()
        await loadFinalReport()
    }catch(e){
        alert(e.message||'Failed to save ranking weights')
    }finally{
        if(btn) btn.disabled=false
    }
}

async function loadRankingBoard(){
    const body=document.getElementById('rankingBody')
    if(!body) return
    body.innerHTML='<tr><td colspan="6">Loading...</td></tr>'
    try{
        const res=await fetch('/api/interview-upgrade/ranking')
        if(!res.ok){
            body.innerHTML='<tr><td colspan="6">Could not load ranking.</td></tr>'
            return
        }
        const data=await res.json()
        const rows=data.candidates||[]
        if(!rows.length){
            body.innerHTML='<tr><td colspan="6">No ranking data yet.</td></tr>'
            return
        }
        body.innerHTML=rows.map(r=>'<tr>'+
            '<td>'+(r.displayName||r.username||('User '+r.userId))+'</td>'+
            '<td>'+num(r.communicationScore)+'</td>'+
            '<td>'+num(r.technicalScore)+'</td>'+
            '<td>'+num(r.behavioralScore)+'</td>'+
            '<td>'+num(r.profileScore)+'</td>'+
            '<td><strong>'+num(r.finalScore)+'</strong></td>'+
            '</tr>').join('')
    }catch(_e){
        body.innerHTML='<tr><td colspan="6">Could not load ranking.</td></tr>'
    }
}

async function createCodingChallenge(event){
    event.preventDefault()
    const btn=event.target.querySelector('button[type="submit"]')
    if(btn) btn.disabled=true
    const payload={
        title:document.getElementById('challengeTitle').value,
        description:document.getElementById('challengeDescription').value,
        language:document.getElementById('challengeLanguage').value,
        expectedOutput:document.getElementById('challengeExpectedOutput').value,
        starterCode:document.getElementById('challengeStarterCode').value,
        active:true
    }
    try{
        await apiJson('/api/interview-upgrade/coding/challenges',{
            method:'POST',
            headers:{'Content-Type':'application/json'},
            body:JSON.stringify(payload)
        })
        event.target.reset()
        await loadCodingChallenges()
        await loadFinalReport()
    }catch(e){
        alert(e.message||'Failed to create challenge')
    }finally{
        if(btn) btn.disabled=false
    }
}

async function loadCodingChallenges(){
    const host=document.getElementById('challengeList')
    if(!host) return
    try{
        const res=await fetch('/api/interview-upgrade/coding/challenges/all')
        if(!res.ok){
            host.innerHTML='<p>Could not load challenges.</p>'
            return
        }
        const rows=await res.json()
        if(!rows.length){
            host.innerHTML='<p>No challenges yet.</p>'
            return
        }
        host.innerHTML=rows.map(c=>'<div style="padding:10px;border:1px solid #e6e6e6;border-radius:8px;margin-bottom:8px">'+
            '<strong>'+escapeHtmlSimple(c.title||'Challenge')+'</strong> '+
            '<span style="font-size:12px;color:#666">('+escapeHtmlSimple(c.language||'')+')</span>'+
            '<div style="font-size:13px;color:#444;margin-top:4px">'+escapeHtmlSimple(c.description||'')+'</div>'+
            '</div>').join('')
    }catch(_e){
        host.innerHTML='<p>Could not load challenges.</p>'
    }
}

async function checkJudge0Status(){
    const el=document.getElementById('judge0StatusText')
    if(el) el.textContent='Checking...'
    try{
        const res=await fetch('/api/interview-upgrade/coding/judge0/status')
        if(!res.ok){
            if(el) el.textContent='Judge0 status check failed.'
            return
        }
        const data=await res.json()
        if(!el) return
        if(!data.enabled){
            el.textContent='Not configured (fallback evaluator active).'
            return
        }
        el.textContent=data.reachable
            ? 'Connected: '+(data.url||'Judge0')
            : 'Configured but not reachable: '+(data.message||'Error')
    }catch(_e){
        if(el) el.textContent='Judge0 status check failed.'
    }
}

function num(v){
    if(v==null||v==='') return '0.00'
    const n=Number(v)
    return Number.isFinite(n)?n.toFixed(2):'0.00'
}

function escapeHtmlSimple(s){
    if(s==null) return ''
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')
}

let finalReportData=null
let reportCharts=[]

async function loadFinalReport(){
    const status=document.getElementById('finalReportStatus')
    if(status) status.textContent='Generating report...'
    try{
        finalReportData=await apiJson('/api/interview-upgrade/report/final')
        renderFinalReport(finalReportData)
        if(status) status.textContent='Report updated'
    }catch(e){
        if(status) status.textContent='Report failed: '+(e.message||'Error')
    }
}

async function publishFinalReport(){
    const status=document.getElementById('finalReportStatus')
    const sendEmails=!!(document.getElementById('sendReportEmails') && document.getElementById('sendReportEmails').checked)
    if(status) status.textContent='Publishing report...'
    try{
        const query='?sendEmails='+(sendEmails?'true':'false')
        const data=await apiJson('/api/interview-upgrade/report/final/publish'+query,{method:'POST'})
        await loadFinalReport()
        if(status){
            const pdfPath=data.reportPdfPath||'reports/final-report.pdf'
            status.textContent='Published PDF: '+pdfPath+'; emailed '+(data.emailedUsers||0)+' users.'
        }
    }catch(e){
        if(status) status.textContent='Publish failed: '+(e.message||'Error')
    }
}

function renderFinalReport(data){
    const kpi=document.getElementById('finalReportKpis')
    const top=document.getElementById('finalReportTopCandidates')
    if(!kpi||!top||!data) return
    kpi.innerHTML=[
        card('Users', data.totalUsers),
        card('Interview Sessions', data.totalInterviewSessions),
        card('Coding Submissions', data.totalCodingSubmissions),
        card('Avg Communication', num(data.averageCommunication)),
        card('Avg Technical', num(data.averageTechnical)),
        card('Avg Behavioral', num(data.averageBehavioral)),
        card('Avg Final Score', num(data.averageFinal)),
        card('Accepted Submissions', data.acceptedSubmissions)
    ].join('')
    const rows=(data.topCandidates||[]).slice(0,10)
    top.innerHTML='<h4 style="margin:0 0 8px">Top Candidates</h4>'+(rows.length
        ? '<table><thead><tr><th>User</th><th>Final</th><th>Comm</th><th>Tech</th><th>Behav</th></tr></thead><tbody>'+
            rows.map(r=>'<tr><td>'+escapeHtmlSimple(r.displayName||r.username||('User '+r.userId))+'</td><td><strong>'+num(r.finalScore)+'</strong></td><td>'+num(r.communicationScore)+'</td><td>'+num(r.technicalScore)+'</td><td>'+num(r.behavioralScore)+'</td></tr>').join('')+
          '</tbody></table>'
        : '<p>No candidates yet.</p>')
    renderReportCharts(data)
}

function renderReportCharts(data){
    reportCharts.forEach(c=>{ try{ c.destroy() }catch(_e){} })
    reportCharts=[]
    if(!window.Chart) return
    const scoreCtx=document.getElementById('reportScoreBandChart')
    const codingCtx=document.getElementById('reportCodingStatusChart')
    if(scoreCtx){
        const bands=data.finalScoreBands||[]
        reportCharts.push(new Chart(scoreCtx,{
            type:'bar',
            data:{labels:bands.map(x=>x.name),datasets:[{label:'Candidates',data:bands.map(x=>x.value),backgroundColor:'#6c5ce7'}]},
            options:{plugins:{legend:{display:false},title:{display:true,text:'Final Score Bands'}}}
        }))
    }
    if(codingCtx){
        const statuses=data.codingStatusDistribution||[]
        reportCharts.push(new Chart(codingCtx,{
            type:'doughnut',
            data:{labels:statuses.map(x=>x.name),datasets:[{data:statuses.map(x=>x.value),backgroundColor:['#00b894','#fdcb6e','#e17055','#6c5ce7','#74b9ff']}]},
            options:{plugins:{title:{display:true,text:'Coding Submission Status'}}}
        }))
    }
}

async function downloadFinalReportJson(){
    const status=document.getElementById('finalReportStatus')
    if(status) status.textContent='Preparing PDF...'
    try{
        const data=await apiJson('/api/interview-upgrade/report/final/publish?sendEmails=false',{method:'POST'})
        const pdfPath=data&&data.reportPdfPath?String(data.reportPdfPath):''
        if(!pdfPath){ throw new Error('PDF path missing from server response') }
        window.open('/files/'+pdfPath,'_blank')
        if(status) status.textContent='PDF ready: '+pdfPath
    }catch(e){
        if(status) status.textContent='PDF export failed: '+(e.message||'Error')
        else alert('PDF export failed: '+(e.message||'Error'))
    }
}

function card(title,value){
    return '<div style="background:#fff;border:1px solid #ececec;border-radius:8px;padding:10px"><div style="font-size:12px;color:#666">'+escapeHtmlSimple(String(title))+'</div><div style="font-size:22px;font-weight:800">'+escapeHtmlSimple(String(value))+'</div></div>'
}

async function apiJson(url,options){
    const res=await fetch(url,options)
    const text=await res.text()
    let data=null
    try{ data=text?JSON.parse(text):null }catch(_e){}
    if(!res.ok){
        const msg=(data&& (data.message||data.error)) || text || ('HTTP '+res.status)
        throw new Error(msg)
    }
    return data
}