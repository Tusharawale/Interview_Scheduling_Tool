let fixCalYear;
let fixCalMonth; // 1-12
let fixCalSummary = [];
let fixModalCharts = [];

function initFixAppointmentPage() {
  if (!localStorage.getItem("admin")) {
    window.location.href = "/admin/admin-login.html";
    return;
  }
  const now = new Date();
  fixCalYear = now.getFullYear();
  fixCalMonth = now.getMonth() + 1;
  loadCalendarMonth();
}

function prevMonth() {
  fixCalMonth -= 1;
  if (fixCalMonth < 1) {
    fixCalMonth = 12;
    fixCalYear -= 1;
  }
  loadCalendarMonth();
}

function nextMonth() {
  fixCalMonth += 1;
  if (fixCalMonth > 12) {
    fixCalMonth = 1;
    fixCalYear += 1;
  }
  loadCalendarMonth();
}

async function loadCalendarMonth() {
  const res = await fetch("/api/admin/interviews/calendar/summary?year=" + fixCalYear + "&month=" + fixCalMonth);
  fixCalSummary = res.ok ? await res.json() : [];
  renderCalendar();
}

function renderCalendar() {
  const label = document.getElementById("calendarMonthLabel");
  if (label) {
    const dt = new Date(fixCalYear, fixCalMonth - 1, 1);
    label.textContent = dt.toLocaleString([], { month: "long", year: "numeric" });
  }
  const host = document.getElementById("calendarGrid");
  if (!host) return;
  host.innerHTML = "";

  ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].forEach(function (d) {
    const h = document.createElement("div");
    h.textContent = d;
    h.style.fontWeight = "700";
    h.style.padding = "6px 8px";
    host.appendChild(h);
  });

  const firstDay = new Date(fixCalYear, fixCalMonth - 1, 1).getDay();
  const daysInMonth = new Date(fixCalYear, fixCalMonth, 0).getDate();
  const byDate = {};
  (fixCalSummary || []).forEach(function (r) {
    byDate[r.date] = r;
  });

  for (let i = 0; i < firstDay; i++) {
    const blank = document.createElement("div");
    blank.style.minHeight = "72px";
    host.appendChild(blank);
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const iso = fixCalYear + "-" + String(fixCalMonth).padStart(2, "0") + "-" + String(d).padStart(2, "0");
    const row = byDate[iso] || { slotCount: 0, bookedCount: 0 };
    const cell = document.createElement("button");
    cell.type = "button";
    cell.style.minHeight = "72px";
    cell.style.border = "1px solid #e6e6e6";
    cell.style.borderRadius = "8px";
    cell.style.background = "#fff";
    cell.style.cursor = "pointer";
    cell.style.padding = "8px";
    cell.style.textAlign = "left";
    cell.innerHTML =
      "<div style='font-weight:600'>" + d + "</div>" +
      "<div style='margin-top:6px;font-size:12px;color:#444'>Slots: " + (row.slotCount || 0) + "</div>" +
      "<div style='font-size:12px;color:#444'>Booked: " + (row.bookedCount || 0) + "</div>";
    if ((row.bookedCount || 0) > 0) {
      cell.style.borderColor = "#00b894";
      cell.style.boxShadow = "inset 0 0 0 1px #00b894";
    } else if ((row.slotCount || 0) > 0) {
      cell.style.borderColor = "#74b9ff";
      cell.style.boxShadow = "inset 0 0 0 1px #74b9ff";
    }
    cell.addEventListener("click", function () {
      loadBookingsForDate(iso);
    });
    host.appendChild(cell);
  }
}

async function loadBookingsForDate(isoDate) {
  const heading = document.getElementById("selectedDateHeading");
  const body = document.getElementById("dateBookingBody");
  if (heading) heading.textContent = "Bookings for " + isoDate;
  if (!body) return;
  body.innerHTML = "<tr><td colspan='7'>Loading...</td></tr>";
  const res = await fetch("/api/admin/interviews/calendar/date/" + isoDate);
  const rows = res.ok ? await res.json() : [];
  if (!rows.length) {
    body.innerHTML = "<tr><td colspan='7'>No bookings found on this date.</td></tr>";
    return;
  }
  body.innerHTML = rows.map(function (b) {
    return "<tr>" +
      "<td>" + b.id + "</td>" +
      "<td>" + escapeHtml(b.slotTitle || "") + "</td>" +
      "<td>" + new Date(b.scheduledAt).toLocaleString() + "</td>" +
      "<td>" + escapeHtml(b.username || "") + " (#" + b.userId + ")</td>" +
      "<td>" + escapeHtml(b.email || "") + "</td>" +
      "<td>" + escapeHtml(b.status || "") + "</td>" +
      "<td style='display:flex;gap:8px;flex-wrap:wrap'><button class='create-btn' onclick='openEmailComposer(" +
      JSON.stringify(b.email || "") + "," +
      JSON.stringify(b.username || "") + "," +
      JSON.stringify(isoDate) + "," +
      JSON.stringify(b.slotTitle || "") +
      ")'>Email</button>" +
      "<button class='create-btn' style='background:#3498db' onclick='openFixUserInfoModal(" + Number(b.userId || 0) + "," + JSON.stringify(b.username || "") + ")'>View User Info</button></td>" +
      "</tr>";
  }).join("");
}

function openEmailComposer(email, username, date, slotTitle) {
  if (typeof window.openAdminEmailComposer === "function") {
    window.openAdminEmailComposer(email, username, date, slotTitle);
    return;
  }
  alert("Email composer will open here for: " + (username || "User") + " (" + (email || "no-email") + ")");
}

function openAdminEmailComposer(email, username, date, slotTitle) {
  const modal = document.getElementById("emailComposerModal");
  if (!modal) return;
  modal.style.display = "flex";
  const to = document.getElementById("emailTo");
  const subject = document.getElementById("emailSubject");
  const body = document.getElementById("emailBody");
  if (to) to.value = email || "";
  if (subject) subject.value = "Interview follow-up: " + (slotTitle || "Interview slot");
  if (body) {
    body.value =
      "Hello " + (username || "Candidate") + ",\n\n" +
      "This is regarding your interview booking on " + (date || "") + ".\n" +
      "Slot: " + (slotTitle || "") + "\n\n" +
      "Regards,\nAdmin Team";
  }
}

function closeAdminEmailComposer() {
  const modal = document.getElementById("emailComposerModal");
  if (modal) modal.style.display = "none";
}

async function sendAdminEmail(event) {
  event.preventDefault();
  const fd = new FormData();
  fd.append("toEmail", document.getElementById("emailTo").value || "");
  fd.append("subject", document.getElementById("emailSubject").value || "");
  fd.append("messageBody", document.getElementById("emailBody").value || "");
  const f = document.getElementById("emailFile");
  if (f && f.files && f.files[0]) fd.append("file", f.files[0]);

  const res = await fetch("/api/admin/email/send", { method: "POST", body: fd });
  if (!res.ok) {
    alert("Email send failed.");
    return;
  }
  alert("Email sent.");
  const form = document.getElementById("adminEmailForm");
  if (form) form.reset();
  closeAdminEmailComposer();
}

function escapeHtml(v) {
  if (v == null) return "";
  return String(v)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

async function openFixUserInfoModal(userId, username) {
  const modal = document.getElementById("fixUserInfoModal");
  const content = document.getElementById("fixUserInfoContent");
  if (!modal || !content || !userId) return;
  modal.style.display = "flex";
  content.innerHTML = "<p>Loading...</p>";
  destroyFixModalCharts();
  try {
    const [profileRes, analyticsRes] = await Promise.all([
      fetch("/api/users/" + userId + "/profile"),
      fetch("/api/users/" + userId + "/profile/analytics")
    ]);
    const data = await profileRes.json();
    const analytics = analyticsRes.ok ? await analyticsRes.json() : null;
    if (!profileRes.ok) throw new Error(data.message || "Failed to load");
    const p = data.profile || {};
    const profileImg = p.profileImage
      ? ("/api/files/" + p.profileImage)
      : "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='120' height='120'><rect width='100%25' height='100%25' fill='%23e9ecef'/><circle cx='60' cy='45' r='22' fill='%23adb5bd'/><rect x='28' y='76' width='64' height='30' rx='15' fill='%23adb5bd'/></svg>";
    const fullName = [p.firstName, p.middleName, p.lastName].filter(Boolean).join(" ") || username || ("User " + userId);
    const location = [p.city, p.state, p.country].filter(Boolean).join(", ") || "Location not set";
    const programme = ({ btech: "B.Tech", diploma: "Diploma", mtech: "M.Tech" }[p.currentCourse] || p.currentCourse || "Not set");
    const edu = data.education || [];
    const skills = data.skills || [];
    const pl = data.programmingLanguages || [];
    const exp = data.experience || [];
    const certs = data.certificates || [];
    const docs = data.documents || [];

    let html = "";
    html += "<div class='admin-user-hero'>";
    html += "<img class='admin-user-avatar' src='" + profileImg + "' alt='Profile'/>";
    html += "<div><h3 style='margin:0'>" + escapeHtml(fullName) + "</h3>";
    html += "<div style='margin-top:6px;color:#666'>" + escapeHtml(location) + "</div>";
    html += "<div style='margin-top:6px;color:#555'>ID " + userId + " • " + escapeHtml(programme) + "</div></div>";
    html += "</div>";

    html += "<div class='admin-user-card-grid'>";
    html += "<div class='admin-user-card'><strong>Email</strong><div style='margin-top:6px'>" + escapeHtml(data.email || "") + "</div></div>";
    html += "<div class='admin-user-card'><strong>Phone</strong><div style='margin-top:6px'>" + escapeHtml(p.contactNumber || "Not set") + "</div></div>";
    html += "<div class='admin-user-card'><strong>Gender / DOB</strong><div style='margin-top:6px'>" + escapeHtml((p.gender || "NA") + " / " + (p.dob || "NA")) + "</div></div>";
    html += "<div class='admin-user-card'><strong>Links</strong><div style='margin-top:6px'>" + (p.linkedinUrl ? "<a href='" + escapeHtml(p.linkedinUrl) + "' target='_blank'>LinkedIn</a> " : "") + (p.githubUrl ? "<a href='" + escapeHtml(p.githubUrl) + "' target='_blank'>GitHub</a>" : "") + "</div></div>";
    html += "<div class='admin-user-card'><strong>Sections</strong><div style='margin-top:6px'>Education: " + edu.length + ", Experience: " + exp.length + ", Skills: " + (skills.length + pl.length) + ", Files: " + (certs.length + docs.length) + "</div></div>";
    html += "</div>";

    if (analytics) {
      html += "<h4 style='margin:16px 0 8px'>Analytics</h4>";
      html += "<div class='admin-user-card-grid'>";
      html += "<div class='admin-user-card'><canvas id='fixUserSectionsChart' height='180'></canvas></div>";
      html += "<div class='admin-user-card'><canvas id='fixUserSkillsChart' height='180'></canvas></div>";
      html += "<div class='admin-user-card' style='grid-column:1/-1'><canvas id='fixUserRadarChart' height='180'></canvas></div>";
      html += "</div>";
    }

    if (edu.length) {
      html += "<h4 style='margin:16px 0 8px'>Education Details</h4><div class='admin-user-card-grid'>";
      edu.slice(0, 20).forEach(function (e) {
        html += "<div class='admin-user-card'><div><strong>" + escapeHtml(e.collegeName || "College") + "</strong></div>" +
          "<div style='font-size:13px;margin-top:4px'>" + escapeHtml((e.educationLevel || "") + " " + (e.branch || "")) + "</div>" +
          "<div style='font-size:12px;color:#666;margin-top:4px'>Sem " + escapeHtml(e.semester || "-") + " | CGPA " + escapeHtml(e.cgpa || "-") + "</div></div>";
      });
      html += "</div>";
    }

    if (exp.length) {
      html += "<h4 style='margin:16px 0 8px'>Experience Details</h4><div class='admin-user-card-grid'>";
      exp.slice(0, 20).forEach(function (e) {
        html += "<div class='admin-user-card'><div><strong>" + escapeHtml(e.companyName || "Company") + "</strong></div>" +
          "<div style='font-size:13px;margin-top:4px'>" + escapeHtml(e.jobRole || "") + "</div>" +
          "<div style='font-size:12px;color:#666;margin-top:4px'>" + escapeHtml((e.startDate || "") + " - " + (e.endDate || "Present")) + "</div></div>";
      });
      html += "</div>";
    }

    if (skills.length || pl.length) {
      html += "<h4 style='margin:16px 0 8px'>Skills & Languages</h4>";
      html += "<div style='display:flex;flex-wrap:wrap;gap:6px'>" +
        skills.map(function (x) { return "<span style='display:inline-block;padding:4px 10px;border-radius:999px;background:#eef2ff;color:#4a4a8a;font-size:12px'>" + escapeHtml((x.skillName || "") + " (" + (x.skillLevel || "NA") + ")") + "</span>"; }).join("") +
        pl.map(function (x) { return "<span style='display:inline-block;padding:4px 10px;border-radius:999px;background:#e8fff8;color:#007a5a;font-size:12px'>" + escapeHtml((x.languageName || "") + " (" + (x.proficiencyLevel || "NA") + ")") + "</span>"; }).join("") +
        "</div>";
    }

    if (certs.length || docs.length) {
      html += "<h4 style='margin:16px 0 8px'>Certificates & Documents</h4><div class='admin-user-card-grid'>";
      certs.slice(0, 12).forEach(function (c) {
        html += "<div class='admin-user-card'><strong>" + escapeHtml(c.certificateName || "Certificate") + "</strong><div style='font-size:12px;color:#666;margin-top:4px'>" + escapeHtml(c.issuer || "") + "</div></div>";
      });
      docs.slice(0, 12).forEach(function (d) {
        html += "<div class='admin-user-card'><strong>" + escapeHtml(d.documentName || "Document") + "</strong></div>";
      });
      html += "</div>";
    }
    content.innerHTML = html;

    if (analytics && window.Chart) {
      const secCtx = document.getElementById("fixUserSectionsChart");
      const skillCtx = document.getElementById("fixUserSkillsChart");
      const radarCtx = document.getElementById("fixUserRadarChart");
      if (secCtx && (analytics.sectionCounts || []).length) {
        fixModalCharts.push(new Chart(secCtx, {
          type: "bar",
          data: { labels: analytics.sectionCounts.map(function (x) { return x.name; }), datasets: [{ label: "Count", data: analytics.sectionCounts.map(function (x) { return x.value; }), backgroundColor: "#6c5ce7" }] },
          options: { plugins: { legend: { display: false }, title: { display: true, text: "Profile depth" } } }
        }));
      }
      if (skillCtx && (analytics.skillLevelDistribution || []).length) {
        fixModalCharts.push(new Chart(skillCtx, {
          type: "doughnut",
          data: { labels: analytics.skillLevelDistribution.map(function (x) { return x.name; }), datasets: [{ data: analytics.skillLevelDistribution.map(function (x) { return x.value; }), backgroundColor: ["#6c5ce7", "#00b894", "#fdcb6e", "#e17055"] }] },
          options: { plugins: { title: { display: true, text: "Skill level distribution" } } }
        }));
      }
      if (radarCtx && analytics.radarCompare && analytics.radarCompare.labels) {
        fixModalCharts.push(new Chart(radarCtx, {
          type: "radar",
          data: {
            labels: analytics.radarCompare.labels,
            datasets: [
              { label: "User", data: analytics.radarCompare.userScores || [], borderColor: "#6c5ce7", backgroundColor: "rgba(108,92,231,0.2)" },
              { label: "Cohort", data: analytics.radarCompare.cohortScores || [], borderColor: "#b2bec3", backgroundColor: "rgba(178,190,195,0.12)" }
            ]
          },
          options: { plugins: { title: { display: true, text: "User vs cohort" } } }
        }));
      }
    }
  } catch (e) {
    content.innerHTML = "<p style='color:red'>" + escapeHtml(e.message) + "</p>";
  }
}

function closeFixUserInfoModal() {
  const modal = document.getElementById("fixUserInfoModal");
  if (modal) modal.style.display = "none";
  destroyFixModalCharts();
}

function destroyFixModalCharts() {
  (fixModalCharts || []).forEach(function (c) {
    try { c.destroy(); } catch (_e) {}
  });
  fixModalCharts = [];
}

window.openAdminEmailComposer = openAdminEmailComposer;
window.closeAdminEmailComposer = closeAdminEmailComposer;
window.sendAdminEmail = sendAdminEmail;
window.openFixUserInfoModal = openFixUserInfoModal;
window.closeFixUserInfoModal = closeFixUserInfoModal;
