/* global SockJS, Stomp - Single global meeting (no room IDs) */

function $(id) { return document.getElementById(id); }

// Expose the local mic stream for STT streaming.
let meetingLocalStream = null;

function nowTime() {
  return new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

function hasMediaSupport() {
  return !!(
    typeof navigator !== "undefined" &&
    navigator.mediaDevices &&
    typeof navigator.mediaDevices.getUserMedia === "function"
  );
}

function mediaSupportErrorMessage() {
  return "Camera/microphone is blocked on this page. Open using HTTPS (or localhost), then allow permissions and try again.";
}

function appendChatMessage({ self, sender, text, fileName, fileUrl, timeLabel }) {
  const chatMessages = $("chatMessages");
  if (!chatMessages) return;

  const msg = document.createElement("div");
  msg.classList.add("message");
  msg.classList.add(self ? "sent" : "received");

  if (fileUrl) {
    const a = document.createElement("a");
    a.href = fileUrl;
    a.target = "_blank";
    a.rel = "noopener";
    a.textContent = fileName ? "📄 " + fileName : "📄 Shared document";
    msg.appendChild(a);
  } else {
    msg.textContent = text || "";
  }

  const meta = document.createElement("div");
  meta.style.opacity = "0.7";
  meta.style.fontSize = "11px";
  meta.style.marginTop = "4px";
  meta.textContent = (sender || "unknown") + " • " + (timeLabel || nowTime());
  msg.appendChild(meta);

  chatMessages.appendChild(msg);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

async function fetchRtcConfiguration() {
  const res = await fetch("/meeting/rtc-config");
  if (!res.ok) throw new Error("Could not load WebRTC configuration");
  return res.json();
}

async function buildPc() {
  const cfg = await fetchRtcConfiguration();
  const iceServers = cfg.iceServers || [
    { urls: "stun:stun.l.google.com:19302" },
    { urls: "stun:stun1.l.google.com:19302" }
  ];
  return new RTCPeerConnection({ iceServers: iceServers });
}

async function startMeeting({ role }) {
  const startBtn = $("startBtn");
  const endBtn = $("endBtn");
  const mainVideoEl = $("mainVideoEl");
  const chatInput = $("chatInput");
  const sendBtn = $("sendBtn");
  const docInput = $("docInput");
  const sharedDoc = $("sharedDoc");
  const docName = $("docName");
  const participantsGrid = $("participantsGrid");
  const meetingStatusText = $("meetingStatusText");
  const typingIndicator = $("typingIndicator");
  const cameraSelect = $("cameraSelect");
  const micSelect = $("micSelect");
  const screenShareBtn = $("screenShareBtn");
  const fullScreenBtn = $("fullScreenBtn");
  const speakerBtn = $("speakerBtn");
  const meetingWrapper = document.querySelector(".meeting-wrapper");
  const chatSection = document.querySelector(".chat-section");
  const fsChatToggleBtn = $("fsChatToggleBtn");
  const bookingIdInput = $("meetingBookingId");
  const transcriptBox = $("liveTranscriptBox");
  const transcriptText = $("liveTranscriptText");
  const transcriptIssues = $("liveTranscriptIssues");
  const commScoreValue = $("commScoreValue");
  const commScoreBar = $("commScoreBar");
  const commStatus = $("commStatus");
  const adminBehInput = $("adminBehavioralScore");
  const adminBehSaveBtn = $("adminBehavioralSaveBtn");
  const adminBehStatus = $("adminBehavioralStatus");
  const adminStartBookingSelect = $("adminStartBookingSelect");
  const adminMeetingMode = $("adminMeetingMode");
  const adminStartBookingHint = $("adminStartBookingHint");
  const adminSlotSummary = $("adminSlotSummary");
  const refreshBookingSelectBtn = $("refreshBookingSelectBtn");

  const storageKey = "meetingClientId_session";
  let myClientId = sessionStorage.getItem(storageKey);
  if (!myClientId) {
    myClientId = (typeof crypto !== "undefined" && crypto.randomUUID)
      ? crypto.randomUUID()
      : (Math.random().toString(16).slice(2) + Date.now().toString(16));
    sessionStorage.setItem(storageKey, myClientId);
  }

  let myName = role === "admin" ? "Admin" : "User";
  let myUserId = null;
  try {
    const u = JSON.parse(sessionStorage.getItem("user") || localStorage.getItem("user") || "null");
    if (role !== "admin" && u) {
      myName = u.username || u.email || "User";
      myUserId = u.id || null;
    }
  } catch (e) {}

  let stomp = null;
  let localStream = null;
  let screenStream = null;
  let started = false;
  let lastPresenceList = [];
  const peers = {};
  let adminClientId = null;
  let typingTimeout = null;
  let lastTypingSent = 0;
  let meetingStartTs = null;
  let commPollTimer = null;
  let transcriptPollTimer = null;
  let speechActivityTimer = null;
  let lastTranscriptAt = 0;
  let speakerEnabled = true;
  let currentTranscriptRaw = "";
  let currentMeetingMode = "SCHEDULED";
  let adminSlotMeta = {};

  function getBookingIdFromUiOrStorage() {
    try {
      const v = bookingIdInput && bookingIdInput.value ? String(bookingIdInput.value).trim() : "";
      if (v) return Number(v);
    } catch (e) {}
    return guessBookingId();
  }

  function persistBookingId(id) {
    if (!id || !Number.isFinite(id)) return;
    try {
      localStorage.setItem("currentBookingId", String(id));
    } catch (e) {}
    try {
      sessionStorage.setItem("currentBookingId", String(id));
    } catch (e) {}
    try {
      if (bookingIdInput) bookingIdInput.value = String(id);
    } catch (e) {}
    try {
      if (adminStartBookingSelect && !adminStartBookingSelect.value) adminStartBookingSelect.value = String(id);
    } catch (e) {}
  }

  function getMeetingModeFromUi() {
    if (!adminMeetingMode) return currentMeetingMode || "SCHEDULED";
    const v = String(adminMeetingMode.value || "SCHEDULED").toUpperCase();
    return v === "NORMAL" ? "NORMAL" : "SCHEDULED";
  }

  function isNormalMode() {
    return String(currentMeetingMode || "").toUpperCase() === "NORMAL";
  }

  function syncAdminModeUi() {
    if (!adminMeetingMode || role !== "admin") return;
    const scheduled = getMeetingModeFromUi() === "SCHEDULED";
    if (adminStartBookingSelect) adminStartBookingSelect.disabled = !scheduled;
    if (adminSlotSummary) adminSlotSummary.style.display = scheduled ? "" : "none";
    if (adminStartBookingHint && !scheduled) {
      adminStartBookingHint.textContent = "Normal mode: no slot required";
    }
  }

  async function resolveUserBookingForSlot(slotId, userId) {
    if (!slotId || !userId) return null;
    try {
      const res = await fetch("/api/interviews/bookings/user/" + encodeURIComponent(String(userId)));
      if (!res.ok) return null;
      const rows = await res.json();
      const found = (rows || []).find(function (b) {
        return Number(b.slotId || 0) === Number(slotId) && String(b.status || "").toUpperCase() === "BOOKED";
      });
      return found && found.id ? Number(found.id) : null;
    } catch (e) {
      return null;
    }
  }

  function applyModeBehaviorUi() {
    const normal = isNormalMode();
    if (transcriptBox) transcriptBox.style.display = normal ? "none" : "";
    if (adminBehSaveBtn) adminBehSaveBtn.style.display = normal ? "none" : "";
    if (adminBehInput) adminBehInput.disabled = normal;
    if (adminBehStatus && normal) adminBehStatus.textContent = "Normal mode: behavioral scoring disabled";
    if (commStatus && normal) commStatus.textContent = "Normal mode (no tests/scoring)";
  }

  async function loadAdminBookingOptions() {
    if (!adminStartBookingSelect) return;
    if (adminStartBookingHint) adminStartBookingHint.textContent = "Loading slots...";
    try {
      const slotsRes = await fetch("/api/admin/interviews/slots");
      if (!slotsRes.ok) throw new Error("Unable to load slots");
      const bookingsRes = await fetch("/api/admin/interviews/bookings");
      if (!bookingsRes.ok) throw new Error("Unable to load bookings");
      const slots = await slotsRes.json();
      const rows = await bookingsRes.json();
      const bookedRows = (rows || []).filter(function (b) { return String(b.status || "").toUpperCase() === "BOOKED"; });
      adminSlotMeta = {};
      bookedRows.forEach(function (b) {
        const sid = Number(b.slotId || 0);
        if (!sid) return;
        if (!adminSlotMeta[sid]) adminSlotMeta[sid] = { users: [], bookedCount: 0 };
        adminSlotMeta[sid].bookedCount += 1;
        adminSlotMeta[sid].users.push((b.username || "User") + (b.email ? " <" + b.email + ">" : ""));
      });
      const opts = ['<option value="">Select scheduled slot...</option>'].concat(
        (slots || []).map(function (s) {
          const sid = Number(s.id || 0);
          const meta = adminSlotMeta[sid] || { bookedCount: 0, users: [] };
          const when = s.scheduledAt ? new Date(s.scheduledAt).toLocaleString() : "";
          const text = "#" + sid + " - " + (s.title || "Slot") + " - Booked: " + Number(meta.bookedCount || s.bookedCount || 0) + (when ? " - " + when : "");
          return '<option value="' + sid + '">' + text + "</option>";
        }).filter(Boolean)
      );
      adminStartBookingSelect.innerHTML = opts.join("");
      if (adminStartBookingHint) adminStartBookingHint.textContent = (slots || []).length ? ((slots || []).length + " slots available") : "No slots found";
    } catch (e) {
      if (adminStartBookingHint) adminStartBookingHint.textContent = "Failed to load slots";
    }
    if (adminStartBookingSelect) {
      const sid = Number(adminStartBookingSelect.value || 0);
      const meta = adminSlotMeta[sid];
      if (adminSlotSummary) {
        if (sid && meta) {
          adminSlotSummary.style.display = "";
          adminSlotSummary.innerHTML =
            "<strong>Scheduled Slot Summary:</strong> " +
            Number(meta.bookedCount || 0) +
            " booked user(s)<br/>" +
            "<div style='margin-top:6px;max-height:84px;overflow:auto'>" +
            meta.users.map(function (u) { return "• " + u; }).join("<br/>") +
            "</div>";
        } else {
          adminSlotSummary.style.display = "none";
          adminSlotSummary.innerHTML = "";
        }
      }
    }
  }

  function setStatus(text) {
    if (meetingStatusText) meetingStatusText.textContent = text || "";
  }

  function clearChatUi() {
    const cm = $("chatMessages");
    if (cm) cm.innerHTML = "";
    const ti = $("typingIndicator");
    if (ti) ti.textContent = "";
    const sd = $("sharedDoc");
    if (sd) sd.style.display = "none";
    const dn = $("docName");
    if (dn) dn.textContent = "";
  }

  function setStartedUi(on) {
    started = on;
    if (endBtn) endBtn.disabled = !on;
    if (startBtn) startBtn.disabled = on;
    const videoArea = $("videoArea");
    if (videoArea) videoArea.classList.toggle("active", !!on);
  }

  function isFullscreen() {
    return !!(document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement);
  }

  async function enterFullscreen() {
    const el = meetingWrapper || document.documentElement;
    try {
      if (el.requestFullscreen) await el.requestFullscreen();
      else if (el.webkitRequestFullscreen) await el.webkitRequestFullscreen();
      else if (el.mozRequestFullScreen) await el.mozRequestFullScreen();
      else if (el.msRequestFullscreen) await el.msRequestFullscreen();
    } catch (e) {}
  }

  async function exitFullscreen() {
    try {
      if (document.exitFullscreen) await document.exitFullscreen();
      else if (document.webkitExitFullscreen) await document.webkitExitFullscreen();
      else if (document.mozCancelFullScreen) await document.mozCancelFullScreen();
      else if (document.msExitFullscreen) await document.msExitFullscreen();
    } catch (e) {}
  }

  function syncFullscreenUi() {
    const on = isFullscreen();
    try {
      document.body.classList.toggle("meeting-fullscreen", on);
    } catch (e) {}
    if (!on && chatSection) {
      chatSection.classList.remove("is-collapsed");
    }
    if (fsChatToggleBtn && chatSection) {
      fsChatToggleBtn.textContent = chatSection.classList.contains("is-collapsed") ? "💬" : "✕";
      fsChatToggleBtn.title = chatSection.classList.contains("is-collapsed") ? "Open chat panel" : "Close chat panel";
      fsChatToggleBtn.setAttribute("aria-label", fsChatToggleBtn.title);
    }
    if (fullScreenBtn) setControlLabel(fullScreenBtn, on ? "Exit full" : "Fullscreen");
  }

  function setControlLabel(btn, label) {
    if (!btn) return;
    const labelEl = btn.querySelector(".control-label");
    if (labelEl) labelEl.textContent = label || "";
    try {
      btn.setAttribute("aria-label", label || "");
    } catch (e) {}
  }

  function setControlState(btn, isOn, onLabel, offLabel) {
    if (!btn) return;
    const on = !!isOn;
    btn.classList.toggle("is-on", on);
    btn.classList.toggle("is-off", !on);
    btn.setAttribute("aria-pressed", on ? "true" : "false");
    setControlLabel(btn, on ? onLabel : offLabel);
  }

  function analyzeTranscriptIssues(text) {
    const t = String(text || "").trim();
    if (!t) return [];
    const issues = [];
    if (/^[a-z]/.test(t)) issues.push("Start sentence with a capital letter.");
    if (!/[.!?]$/.test(t)) issues.push("Add ending punctuation to improve clarity.");
    const repeated = t.match(/\b(\w+)\s+\1\b/gi);
    if (repeated && repeated.length) issues.push("Repeated words detected: " + repeated.slice(0, 3).join(", "));
    const fillerHits = (t.toLowerCase().match(/\b(um|uh|like|you know|basically|actually)\b/g) || []).length;
    if (fillerHits >= 3) issues.push("High filler-word usage detected.");
    return issues;
  }

  function renderTranscriptIssues(text) {
    if (!transcriptIssues) return;
    const items = analyzeTranscriptIssues(text);
    if (!items.length) {
      transcriptIssues.innerHTML = "<div style='color:#16a34a'>No major transcript issues detected.</div>";
      return;
    }
    transcriptIssues.innerHTML = items.map(function (x) {
      return "<div style='margin:2px 0;color:#b45309'>• " + String(x) + "</div>";
    }).join("");
  }

  function sendControlCommand(actionType, mode, target) {
    if (!stomp || role !== "admin") return;
    if (!target || !target.clientId) return;
    const payload = {
      actionType: actionType,
      mode: mode,
      targetClientId: target.clientId,
      targetUserId: target.userId || null,
      adminClientId: myClientId,
      adminUserId: myUserId || null,
      reason: "Moderation",
      ts: new Date().toISOString()
    };
    stomp.send("/app/meeting/control", {}, JSON.stringify(payload));
  }

  function sendControlAck(command, accepted, applied, message) {
    if (!stomp || !command) return;
    const ack = {
      actionType: command.actionType,
      mode: command.mode,
      targetClientId: command.targetClientId,
      targetUserId: command.targetUserId,
      adminClientId: command.adminClientId,
      adminUserId: command.adminUserId,
      accepted: !!accepted,
      applied: !!applied,
      message: message || "",
      ts: new Date().toISOString()
    };
    stomp.send("/app/meeting/control/ack", {}, JSON.stringify(ack));
  }

  function applyLocalControlAction(actionType) {
    if (!localStream) return false;
    const a = String(actionType || "").toLowerCase();
    if (a === "mute_mic" || a === "unmute_mic") {
      const at = localStream.getAudioTracks()[0];
      if (!at) return false;
      at.enabled = a === "unmute_mic";
      const audioBtn = $("audioBtn");
      if (audioBtn) setControlState(audioBtn, at.enabled, "Mic on", "Mic off");
      return true;
    }
    if (a === "camera_off" || a === "camera_on") {
      const vt = localStream.getVideoTracks()[0];
      if (!vt) return false;
      vt.enabled = a === "camera_on";
      const videoBtn = $("videoBtn");
      if (videoBtn) setControlLabel(videoBtn, vt.enabled ? "Cam on" : "Cam off");
      return true;
    }
    return false;
  }

  function applySpeakerState() {
    if (mainVideoEl) {
      const isLocalMain = mainVideoEl.getAttribute("data-is-local") === "1";
      mainVideoEl.muted = isLocalMain || !speakerEnabled;
    }
    if (participantsGrid) {
      participantsGrid.querySelectorAll("video").forEach(function (v) {
        if (!v) return;
        if (v.getAttribute("data-is-local") === "1") {
          v.muted = true;
        } else {
          v.muted = !speakerEnabled;
        }
      });
    }
    if (speakerBtn) setControlState(speakerBtn, speakerEnabled, "Speaker on", "Speaker off");
  }

  function meetingHeaders(isJson) {
    const h = {};
    if (isJson) h["Content-Type"] = "application/json";
    if (role === "admin") {
      const t = localStorage.getItem("adminMeetingToken");
      if (t) h["X-Meeting-Admin-Token"] = t;
    }
    return h;
  }

  async function ensureMedia() {
    if (!hasMediaSupport()) {
      throw new Error(mediaSupportErrorMessage());
    }
    if (localStream) return localStream;
    const videoDeviceId = cameraSelect && cameraSelect.value ? { exact: cameraSelect.value } : true;
    const audioDeviceId = micSelect && micSelect.value ? { exact: micSelect.value } : true;
    try {
      localStream = await navigator.mediaDevices.getUserMedia({
        video: videoDeviceId,
        audio: audioDeviceId
      });
    } catch (err) {
      const msg =
        err && err.name === "NotAllowedError"
          ? "Camera/microphone permission denied. Allow access in the browser address bar and try again."
          : err && err.name === "NotFoundError"
            ? "No camera or microphone found."
            : (err && err.message) || "Could not access camera/microphone.";
      throw new Error(msg);
    }
    meetingLocalStream = localStream;
    return localStream;
  }

  async function populateDeviceSelectors() {
    if (!cameraSelect && !micSelect) return;
    if (!hasMediaSupport()) return;
    try {
      const devs = await navigator.mediaDevices.enumerateDevices();
      if (cameraSelect) {
        cameraSelect.innerHTML = "";
        devs
          .filter(function (d) { return d.kind === "videoinput"; })
          .forEach(function (d) {
            const o = document.createElement("option");
            o.value = d.deviceId;
            o.textContent = d.label || "Camera " + (cameraSelect.options.length + 1);
            cameraSelect.appendChild(o);
          });
      }
      if (micSelect) {
        micSelect.innerHTML = "";
        devs
          .filter(function (d) { return d.kind === "audioinput"; })
          .forEach(function (d) {
            const o = document.createElement("option");
            o.value = d.deviceId;
            o.textContent = d.label || "Mic " + (micSelect.options.length + 1);
            micSelect.appendChild(o);
          });
      }
    } catch (e) {}
  }

  async function ensureLocalReady() {
    await populateDeviceSelectors();
    const stream = await ensureMedia();
    if (lastPresenceList && lastPresenceList.length) renderParticipants(lastPresenceList);
    setMainStream(stream, true);
    return stream;
  }

  function shouldOfferTo(remoteClientId) {
    return String(myClientId) < String(remoteClientId);
  }

  async function createPeer(remote) {
    const rid = remote && remote.clientId;
    if (!rid || rid === myClientId) return null;
    if (peers[rid]) return peers[rid];

    const stream = await ensureLocalReady();
    const pc = await buildPc();
    const state = { pc: pc, remoteStream: null, pendingIce: [] };
    peers[rid] = state;

    stream.getTracks().forEach(function (t) {
      pc.addTrack(t, stream);
    });

    pc.ontrack = function (evt) {
      const rs = evt.streams[0];
      if (!rs) return;
      state.remoteStream = rs;
      renderParticipants(lastPresenceList || []);

      if (adminClientId && rid === adminClientId) {
        setMainStream(rs, false);
      } else if (role === "admin" && mainVideoEl && !mainVideoEl.srcObject) {
        setMainStream(rs, false);
      }
    };

    pc.onconnectionstatechange = function () {
      const stConn = pc.connectionState;
      if (stConn === "failed" || stConn === "disconnected" || stConn === "closed") {
        try { pc.close(); } catch (e) {}
        if (peers[rid]) delete peers[rid];
        renderParticipants(lastPresenceList || []);
        refreshMainVideoCandidate();
      }
    };

    pc.onicecandidate = function (evt) {
      if (!evt.candidate) return;
      stomp.send("/app/meeting/signal", {}, JSON.stringify({
        type: "signal",
        sender: role,
        senderName: myName,
        senderId: myUserId,
        kind: "ice",
        toClientId: rid,
        fromClientId: myClientId,
        payload: evt.candidate
      }));
    };

    return state;
  }

  async function ensureMeshPeers(list) {
    if (!list || !list.length) return;

    adminClientId = (list.find(function (p) { return (p.role || "").toLowerCase() === "admin"; }) || {}).clientId || null;

    for (const p of list) {
      if (!p || !p.clientId || p.clientId === myClientId) continue;
      await createPeer(p);
    }

    for (const p of list) {
      if (!p || !p.clientId || p.clientId === myClientId) continue;
      const rid = p.clientId;
      if (!shouldOfferTo(rid)) continue;
      const st = peers[rid];
      if (!st || !st.pc) continue;
      if (st.pc.signalingState !== "stable") continue;
      const offer = await st.pc.createOffer();
      await st.pc.setLocalDescription(offer);
      stomp.send("/app/meeting/signal", {}, JSON.stringify({
        type: "signal",
        sender: role,
        senderName: myName,
        senderId: myUserId,
        kind: "offer",
        toClientId: rid,
        fromClientId: myClientId,
        payload: st.pc.localDescription
      }));
    }
  }

  async function handleSignal(msg) {
    const from = msg.fromClientId;
    if (!from) return;

    const remote =
      (lastPresenceList || []).find(function (p) { return p && p.clientId === from; }) ||
      { clientId: from, role: "user", name: "User" };
    const st = await createPeer(remote);
    if (!st) return;
    const pc = st.pc;

    if (msg.kind === "offer") {
      await pc.setRemoteDescription(new RTCSessionDescription(msg.payload));
      if (st.pendingIce.length) {
        for (const c of st.pendingIce) {
          try {
            await pc.addIceCandidate(new RTCIceCandidate(c));
          } catch (e) {}
        }
        st.pendingIce = [];
      }
      const answer = await pc.createAnswer();
      await pc.setLocalDescription(answer);
      stomp.send("/app/meeting/signal", {}, JSON.stringify({
        type: "signal",
        sender: role,
        senderName: myName,
        senderId: myUserId,
        kind: "answer",
        toClientId: from,
        fromClientId: myClientId,
        payload: pc.localDescription
      }));
    } else if (msg.kind === "answer") {
      await pc.setRemoteDescription(new RTCSessionDescription(msg.payload));
      if (st.pendingIce.length) {
        for (const c of st.pendingIce) {
          try {
            await pc.addIceCandidate(new RTCIceCandidate(c));
          } catch (e) {}
        }
        st.pendingIce = [];
      }
    } else if (msg.kind === "ice") {
      if (!msg.payload) return;
      if (!pc.remoteDescription) {
        st.pendingIce.push(msg.payload);
        return;
      }
      await pc.addIceCandidate(new RTCIceCandidate(msg.payload));
    }
  }

  function setMainStream(stream, isLocal) {
    if (!mainVideoEl) return;
    mainVideoEl.srcObject = stream || null;
    mainVideoEl.muted = !!isLocal || !speakerEnabled;
    mainVideoEl.setAttribute("data-is-local", isLocal ? "1" : "0");
    mainVideoEl.play().catch(function () {
      try {
        mainVideoEl.style.cursor = "pointer";
        mainVideoEl.title = "Click to play";
      } catch (e) {}
    });
  }

  function refreshMainVideoCandidate() {
    if (!mainVideoEl) return;
    const current = mainVideoEl.srcObject;
    const aliveCurrent = current && current.getTracks && current.getTracks().some(function (t) { return t.readyState === "live"; });
    if (aliveCurrent) return;
    let candidate = null;
    if (adminClientId && peers[adminClientId] && peers[adminClientId].remoteStream) {
      candidate = peers[adminClientId].remoteStream;
    }
    if (!candidate) {
      const remotePeerKey = Object.keys(peers).find(function (k) {
        const s = peers[k] && peers[k].remoteStream;
        return !!(s && s.getTracks && s.getTracks().some(function (t) { return t.readyState === "live"; }));
      });
      if (remotePeerKey) candidate = peers[remotePeerKey].remoteStream;
    }
    if (candidate) setMainStream(candidate, false);
    else if (localStream) setMainStream(localStream, true);
  }

  if (mainVideoEl) {
    mainVideoEl.addEventListener("click", function () {
      mainVideoEl.play().catch(function () {});
    });
  }

  function renderParticipantListText(list) {
    const el = $("participantListText");
    if (!el) return;
    if (!list || !list.length) {
      el.innerHTML = "<p>No participants yet.</p>";
      return;
    }
    el.innerHTML = list
      .map(function (p) {
        const nm = (p.name || p.role || "?") + (p.userId ? " (id " + p.userId + ")" : "");
        const rl = (p.role || "").toUpperCase();
        return "<p><strong>" + rl + "</strong> — " + nm + "</p>";
      })
      .join("");
  }

  function loadChatHistory() {
    const chatMessages = $("chatMessages");
    if (chatMessages) chatMessages.innerHTML = "";
    return fetch("/meeting/chat/history?limit=100")
      .then(function (r) { return r.json(); })
      .then(function (items) {
        if (!chatMessages || !Array.isArray(items)) return;
        items.forEach(function (item) {
          const who = (item.senderName || item.senderRole || "?") + (item.senderUserId ? " (" + item.senderUserId + ")" : "");
          const tl = item.ts ? new Date(item.ts).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : null;
          appendChatMessage({
            self: item.fromClientId === myClientId,
            sender: who,
            text: item.text,
            fileName: item.fileName,
            fileUrl: item.fileUrl,
            timeLabel: tl
          });
        });
      })
      .catch(function () {});
  }

  function connectWs() {
    return new Promise(function (resolve, reject) {
      setStatus("Connecting to chat…");
      const socket = new SockJS("/ws");
      stomp = Stomp.over(socket);
      stomp.debug = null;
      stomp.connect(
        {},
        function () {
          stomp.subscribe("/topic/meeting/presence", function (frame) {
            try {
              const list = JSON.parse(frame.body) || [];
              const uniq = [];
              const seen = new Set();
              list.forEach(function (p) {
                if (!p || !p.clientId) return;
                const k = String(p.clientId);
                if (seen.has(k)) return;
                seen.add(k);
                uniq.push(p);
              });
              lastPresenceList = uniq;
              renderParticipants(uniq);
              renderParticipantListText(uniq);
              if (started) ensureMeshPeers(uniq).catch(function () {});
            } catch (e) {}
          });

          stomp.subscribe("/topic/meeting/signal", async function (frame) {
            const msg = JSON.parse(frame.body);
            if (!msg || !msg.type) return;
            if (msg.fromClientId && msg.fromClientId === myClientId) return;
            if (msg.toClientId && msg.toClientId !== myClientId) return;

            if (msg.kind === "end") {
              cleanup(false);
              alert("Meeting ended.");
              return;
            }

            try {
              await handleSignal(msg);
            } catch (e) {}
          });

          stomp.subscribe("/topic/meeting/chat", function (frame) {
            const msg = JSON.parse(frame.body);
            if (!msg) return;
            if (msg.fromClientId && msg.fromClientId === myClientId) return;

            const p = msg.payload || {};
            const who = (msg.senderName ? msg.senderName : msg.sender) + (msg.senderId ? " (" + msg.senderId + ")" : "");
            appendChatMessage({
              self: false,
              sender: who,
              text: p.text,
              fileName: p.fileName,
              fileUrl: p.fileUrl
            });
          });

          stomp.subscribe("/topic/meeting/typing", function (frame) {
            try {
              const t = JSON.parse(frame.body);
              if (!t || t.fromClientId === myClientId) return;
              if (!typingIndicator) return;
              if (t.typing) {
                typingIndicator.textContent = (t.senderName || "Someone") + " is typing…";
                clearTimeout(typingTimeout);
                typingTimeout = setTimeout(function () {
                  typingIndicator.textContent = "";
                }, 2500);
              }
            } catch (e) {}
          });

          stomp.subscribe("/topic/meeting/control", function (frame) {
            try {
              const cmd = JSON.parse(frame.body || "{}");
              if (!cmd || !cmd.targetClientId) return;
              if (String(cmd.targetClientId) !== String(myClientId)) return;
              const actionType = String(cmd.actionType || "");
              const mode = String(cmd.mode || "request").toLowerCase();
              if (mode === "force") {
                const applied = applyLocalControlAction(actionType);
                sendControlAck(cmd, true, applied, applied ? "Applied by force." : "Could not apply action.");
                return;
              }
              const msg = "Admin request: " + actionType.split("_").join(" ") + ". Allow?";
              const ok = window.confirm(msg);
              if (!ok) {
                sendControlAck(cmd, false, false, "User rejected request.");
                return;
              }
              const applied = applyLocalControlAction(actionType);
              sendControlAck(cmd, true, applied, applied ? "User accepted and applied." : "Accepted but action unavailable.");
            } catch (e) {}
          });

          stomp.subscribe("/topic/meeting/control/ack", function (frame) {
            try {
              const ack = JSON.parse(frame.body || "{}");
              if (!ack || !ack.adminClientId) return;
              if (String(ack.adminClientId) !== String(myClientId)) return;
              if (typingIndicator) {
                const who = ack.targetUserId ? ("User " + ack.targetUserId) : "User";
                typingIndicator.textContent = who + ": " + (ack.message || (ack.applied ? "Applied." : "Not applied."));
                clearTimeout(typingTimeout);
                typingTimeout = setTimeout(function () { if (typingIndicator) typingIndicator.textContent = ""; }, 2800);
              }
            } catch (e) {}
          });

          // Bind live STT + score push only for scheduled mode.
          const liveBid = getBookingIdFromUiOrStorage();
          if (!isNormalMode() && liveBid) {
            try {
              stomp.subscribe("/topic/meeting/stt/" + liveBid, function (frame) {
                const msg = JSON.parse(frame.body || "{}");
                if (transcriptBox) transcriptBox.style.display = "";
                currentTranscriptRaw = String(msg.fullTranscript || msg.partialTranscript || "");
                if (transcriptText) transcriptText.textContent = currentTranscriptRaw || "(listening...)";
                renderTranscriptIssues(currentTranscriptRaw);
                lastTranscriptAt = Date.now();
                const segs = Array.isArray(msg.segments) ? msg.segments : [];
                if (segs.length && transcriptText) {
                  const lines = segs.slice(-6).map(function (s) {
                    const sp = s.speaker ? "[" + s.speaker + "] " : "";
                    const cf = s.confidence != null ? " (" + Number(s.confidence).toFixed(2) + ")" : "";
                    return sp + (s.text || "") + cf;
                  });
                  if (lines.join("").trim()) transcriptText.textContent = lines.join("\n");
                }
              });
              stomp.subscribe("/topic/meeting/score/" + liveBid, function (frame) {
                const msg = JSON.parse(frame.body || "{}");
                const v = Number(msg.communicationScore || 0);
                const n = Number.isFinite(v) ? Math.max(0, Math.min(100, v)) : 0;
                if (commScoreValue) commScoreValue.textContent = n.toFixed(2) + "/100";
                if (commScoreBar) commScoreBar.style.width = n + "%";
                if (commStatus) commStatus.textContent = "Live";
              });
            } catch (e) {}
          }

          stomp.send(
            "/app/meeting/presence/join",
            {},
            JSON.stringify({
              clientId: myClientId,
              role: role,
              name: myName,
              userId: myUserId
            })
          );

          loadChatHistory().finally(function () {
            setStatus("");
            resolve();
          });
        },
        function (err) {
          reject(err);
        }
      );
    });
  }

  function getOrCreateTile(p) {
    if (!participantsGrid) return null;
    const clientId = p.clientId || "";
    if (!clientId) return null;

    let tile = participantsGrid.querySelector('[data-client-id="' + clientId + '"]');
    if (tile) return tile;

    tile = document.createElement("div");
    tile.className = "participant connected";
    tile.setAttribute("data-client-id", clientId);

    const badge = document.createElement("div");
    badge.className = "status-badge active";
    badge.textContent = "ACTIVE";

    const title = document.createElement("span");
    title.className = "participant-title";
    title.textContent = (p.role || "USER").toUpperCase();

    const vidWrap = document.createElement("div");
    vidWrap.className = "participant-video-wrap";

    const v = document.createElement("video");
    v.autoplay = true;
    v.playsInline = true;
    v.muted = true;
    v.style.width = "100%";
    v.style.height = "100%";
    v.style.objectFit = "cover";
    vidWrap.appendChild(v);

    const nm = document.createElement("div");
    nm.className = "participant-name";

    const actions = document.createElement("div");
    actions.className = "participant-actions";
    actions.innerHTML = [
      '<button type="button" data-action="mute_mic" data-mode="request">Req Mic Off</button>',
      '<button type="button" data-action="mute_mic" data-mode="force">Force Mic Off</button>',
      '<button type="button" data-action="camera_off" data-mode="request">Req Cam Off</button>',
      '<button type="button" data-action="camera_off" data-mode="force">Force Cam Off</button>'
    ].join("");
    actions.querySelectorAll("button").forEach(function (btn) {
      btn.addEventListener("click", function (ev) {
        ev.preventDefault();
        ev.stopPropagation();
        if (role !== "admin") return;
        const actionType = btn.getAttribute("data-action") || "";
        const mode = btn.getAttribute("data-mode") || "request";
        const target = {
          clientId: clientId,
          userId: p.userId || null
        };
        sendControlCommand(actionType, mode, target);
      });
    });

    tile.appendChild(badge);
    tile.appendChild(title);
    tile.appendChild(vidWrap);
    tile.appendChild(nm);
    tile.appendChild(actions);
    participantsGrid.appendChild(tile);

    return tile;
  }

  function setTileMeta(tile, p) {
    if (!tile) return;
    const title = tile.querySelector(".participant-title");
    const nm = tile.querySelector(".participant-name");
    if (title) title.textContent = (p.role || "USER").toUpperCase();
    if (nm)
      nm.textContent =
        (p.name || (p.role === "admin" ? "Admin" : "User")) + (p.userId ? " (" + p.userId + ")" : "");
    const actions = tile.querySelector(".participant-actions");
    const canModerate = role === "admin" && p && p.clientId && p.clientId !== myClientId && String((p.role || "").toLowerCase()) !== "admin";
    if (actions) actions.style.display = canModerate ? "grid" : "none";
  }

  function setTileStreamByClientId(clientId, stream, mute, onClickToMain) {
    if (!participantsGrid) return;
    const tile = participantsGrid.querySelector('[data-client-id="' + clientId + '"]');
    if (!tile) return;
    const v = tile.querySelector("video");
    if (v && stream && v.srcObject !== stream) {
      v.srcObject = stream;
      v.muted = !!mute;
      v.setAttribute("data-is-local", mute ? "1" : "0");
      v.play().catch(function () {});
    }
    if (onClickToMain) {
      tile.onclick = onClickToMain;
    }
  }

  function renderParticipants(list) {
    if (!participantsGrid) return;
    const dedupMap = new Map();
    (list || []).forEach(function (p) {
      if (!p || !p.clientId) return;
      const key = String(p.clientId);
      dedupMap.set(key, p);
    });
    list = Array.from(dedupMap.values());

    const keep = {};
    (list || []).forEach(function (p) {
      if (p && p.clientId) keep[p.clientId] = true;
    });
    participantsGrid.querySelectorAll("[data-client-id]").forEach(function (el) {
      const id = el.getAttribute("data-client-id");
      if (!keep[id]) el.remove();
    });

    if (!list || list.length === 0) return;

    list = list.slice().sort(function (a, b) {
      const ar = (a.role || "").toLowerCase();
      const br = (b.role || "").toLowerCase();
      if (ar === br) return 0;
      if (ar === "admin") return -1;
      if (br === "admin") return 1;
      return 0;
    });

    list.forEach(function (p) {
      const tile = getOrCreateTile(p);
      setTileMeta(tile, p);

      if (p.clientId === myClientId && localStream) {
        setTileStreamByClientId(p.clientId, localStream, true, function () {
          setMainStream(localStream, true);
        });
      } else if (peers[p.clientId] && peers[p.clientId].remoteStream) {
        const s = peers[p.clientId].remoteStream;
        setTileStreamByClientId(p.clientId, s, !speakerEnabled, function () {
          setMainStream(s, false);
        });
      }
    });
    applySpeakerState();
    refreshMainVideoCandidate();
  }

  async function replaceVideoTrackOnAllPeers(newVideoTrack) {
    if (!localStream) return;
    const old = localStream.getVideoTracks()[0];
    if (old) {
      localStream.removeTrack(old);
      try {
        old.stop();
      } catch (e) {}
    }
    localStream.addTrack(newVideoTrack);
    newVideoTrack.onended = function () {
      stopScreenShare().catch(function () {});
    };

    Object.keys(peers).forEach(function (k) {
      const pc = peers[k].pc;
      if (!pc) return;
      const sender = pc.getSenders().find(function (s) {
        return s.track && s.track.kind === "video";
      });
      if (sender) sender.replaceTrack(newVideoTrack);
    });

    // Prevent black/old frame in main/local tiles after swapping camera/screen track.
    renderParticipants(lastPresenceList || []);
    setMainStream(localStream, true);
  }

  async function stopScreenShare() {
    if (screenStream) {
      screenStream.getTracks().forEach(function (t) {
        try {
          t.stop();
        } catch (e) {}
      });
      screenStream = null;
    }
    try {
      if (!hasMediaSupport()) {
        throw new Error(mediaSupportErrorMessage());
      }
      const cam = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
      const nv = cam.getVideoTracks()[0];
      if (localStream && nv) {
        const oldV = localStream.getVideoTracks()[0];
        if (oldV) {
          localStream.removeTrack(oldV);
          try {
            oldV.stop();
          } catch (e) {}
        }
        localStream.addTrack(nv);
      }
      const v = localStream ? localStream.getVideoTracks()[0] : null;
      Object.keys(peers).forEach(function (k) {
        const pc = peers[k].pc;
        if (!pc) return;
        const sender = pc.getSenders().find(function (s) {
          return s.track && s.track.kind === "video";
        });
        if (sender && v) sender.replaceTrack(v);
      });
      renderParticipants(lastPresenceList || []);
      setMainStream(localStream, true);
    } catch (e) {
      console.warn(e);
      // If restoring camera fails, keep user informed instead of silent black screen.
      alert("Could not restore camera after screen share. Re-select camera or rejoin meeting.");
    }
    setControlLabel(screenShareBtn, "Share screen");
  }

  async function doStart() {
    if (role !== "admin") {
      setStatus("Checking meeting status…");
      const stRes = await fetch("/meeting/status");
      const st = await stRes.json();
      currentMeetingMode = String(st.mode || "SCHEDULED").toUpperCase();
      if (!st.active) {
        setStatus("");
        alert("The meeting is not active yet. Wait until the host starts it from the admin dashboard, then try JOIN again.");
        return;
      }
      if (currentMeetingMode === "SCHEDULED") {
        const allowedIds = Array.isArray(st.allowedUserIds) ? st.allowedUserIds.map(Number) : [];
        if (myUserId && allowedIds.length && !allowedIds.includes(Number(myUserId))) {
          setStatus("");
          alert("This scheduled meeting is for another slot audience.");
          return;
        }
        const myBooking = await resolveUserBookingForSlot(Number(st.slotId || 0), Number(myUserId || 0));
        if (myBooking) persistBookingId(Number(myBooking));
        else setStatus("Joined scheduled meeting (no personal booking mapping found for tests)");
      } else {
        try { localStorage.removeItem("currentBookingId"); } catch (e) {}
        try { sessionStorage.removeItem("currentBookingId"); } catch (e) {}
      }
      applyModeBehaviorUi();
    } else {
      currentMeetingMode = getMeetingModeFromUi();
      applyModeBehaviorUi();
    }

    setStartedUi(true);
    setStatus("Connecting…");

    try {
      await connectWs();
      setStatus("Requesting camera and microphone…");
      await ensureLocalReady();

      // Scheduled mode only: STT + scoring/test pipeline.
      const bid = getBookingIdFromUiOrStorage();
      if (!isNormalMode() && bid && Number.isFinite(bid)) {
        persistBookingId(bid);
      }
      if (!isNormalMode()) {
        try {
          startSttStreaming(role);
        } catch (e) {
          console.warn("STT stream init failed", e);
        }
        meetingStartTs = Date.now();
        startTranscriptPolling();
        startCommunicationPolling();
      }

      if (role === "admin") {
        const token = localStorage.getItem("adminMeetingToken");
        if (!token) {
          cleanup(false);
          alert("Missing meeting credentials. Please log out of admin and log in again.");
          return;
        }
        let startUrl = "/meeting/start?mode=" + encodeURIComponent(currentMeetingMode);
        if (currentMeetingMode === "SCHEDULED") {
          const selectedSlotId = adminStartBookingSelect && adminStartBookingSelect.value ? Number(adminStartBookingSelect.value) : 0;
          if (!selectedSlotId) {
            cleanup(false);
            alert("Select a scheduled slot before starting scheduled meeting.");
            return;
          }
          startUrl += "&slotId=" + encodeURIComponent(String(selectedSlotId));
        }
        const res2 = await fetch(startUrl, { method: "POST", headers: meetingHeaders(false) });
        if (!res2.ok) {
          const err = await res2.json().catch(function () { return {}; });
          cleanup(false);
          alert("Could not start meeting on server: " + (err.error || res2.status));
          return;
        }
      }

      await ensureMeshPeers(lastPresenceList || []);
      setStatus("");
    } catch (e) {
      cleanup(false);
      alert(e.message || "Start failed");
    }
  }

  function cleanup(sendEnd) {
    setStartedUi(false);
    setStatus("");
    clearChatUi();

    try {
      if (sendEnd) {
        if (role === "admin") {
          if (stomp) stomp.send("/app/meeting/end", {}, JSON.stringify({ sender: role, fromClientId: myClientId }));
          const token = localStorage.getItem("adminMeetingToken");
          if (token) {
            fetch("/meeting/end", {
              method: "POST",
              headers: { "X-Meeting-Admin-Token": token }
            }).catch(function () {});
          }
        }
      }
    } catch (e) {}

    Object.keys(peers).forEach(function (k) {
      try {
        if (peers[k].pc) peers[k].pc.close();
      } catch (e) {}
      delete peers[k];
    });

    if (mainVideoEl) mainVideoEl.srcObject = null;

    if (screenStream) {
      screenStream.getTracks().forEach(function (t) {
        try {
          t.stop();
        } catch (e) {}
      });
      screenStream = null;
    }

    if (localStream) {
      localStream.getTracks().forEach(function (t) {
        t.stop();
      });
    }
    localStream = null;
    meetingLocalStream = null;

    try {
      stopSttStreaming();
    } catch (e) {}

    stopCommunicationPolling();
    stopTranscriptPolling();

    try {
      if (stomp) stomp.disconnect(function () {});
    } catch (e) {}
    stomp = null;
  }

  if (startBtn) {
    startBtn.addEventListener("click", function () {
      doStart().catch(function (e) {
        alert(e.message || "Start failed");
      });
    });
  }
  if (endBtn) {
    endBtn.addEventListener("click", function () {
      if (role === "admin") {
        // Finalize transcript + compute final scores + publish PDF report + email
        finalizeMeetingArtifacts(true).finally(function () {
          cleanup(true);
        });
      }
      else {
        finalizeMeetingArtifacts(false).finally(function () {
          cleanup(false);
          window.location.href = "/user/user.html";
        });
      }
    });
  }

  if (adminBehSaveBtn) {
    adminBehSaveBtn.addEventListener("click", function () {
      if (role !== "admin") return;
      if (isNormalMode()) {
        if (adminBehStatus) adminBehStatus.textContent = "Normal mode: behavioral score is disabled.";
        return;
      }
      const bid = getBookingIdFromUiOrStorage();
      if (!bid) {
        if (adminBehStatus) adminBehStatus.textContent = "Booking ID is required.";
        return;
      }
      const v = adminBehInput && adminBehInput.value !== "" ? Number(adminBehInput.value) : null;
      if (v == null || !Number.isFinite(v) || v < 0 || v > 100) {
        if (adminBehStatus) adminBehStatus.textContent = "Behavioral score must be 0-100.";
        return;
      }
      if (adminBehStatus) adminBehStatus.textContent = "Saving Behavioral…";
      fetch("/api/interview-upgrade/communication/score", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ bookingId: bid, behavioralScore: v })
      })
        .then(function (r) { return r.json(); })
        .then(function () {
          if (adminBehStatus) adminBehStatus.textContent = "Behavioral saved.";
        })
        .catch(function () {
          if (adminBehStatus) adminBehStatus.textContent = "Behavioral save failed.";
        });
    });
  }

  var audioBtn = $("audioBtn");
  var videoBtn = $("videoBtn");
  if (audioBtn) {
    audioBtn.addEventListener("click", function () {
      if (!localStream) return;
      var audioTrack = localStream.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        setControlState(audioBtn, audioTrack.enabled, "Mic on", "Mic off");
      }
    });
    setControlState(audioBtn, true, "Mic on", "Mic off");
  }
  if (speakerBtn) {
    speakerBtn.addEventListener("click", function () {
      speakerEnabled = !speakerEnabled;
      applySpeakerState();
      setControlState(speakerBtn, speakerEnabled, "Speaker on", "Speaker off");
    });
    setControlState(speakerBtn, true, "Speaker on", "Speaker off");
  }
  if (videoBtn) {
    videoBtn.addEventListener("click", function () {
      if (!localStream) return;
      var videoTrack = localStream.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled;
        setControlLabel(videoBtn, videoTrack.enabled ? "Cam on" : "Cam off");
      }
    });
    setControlLabel(videoBtn, "Cam on");
  }

  if (screenShareBtn) {
    screenShareBtn.addEventListener("click", async function () {
      if (!started || !stomp) {
        alert("Join the meeting first.");
        return;
      }
      if (!hasMediaSupport()) {
        alert(mediaSupportErrorMessage());
        return;
      }
      try {
        if (screenStream) {
          await stopScreenShare();
          return;
        }
        screenStream = await navigator.mediaDevices.getDisplayMedia({ video: true, audio: true });
        const vt = screenStream.getVideoTracks()[0];
        if (vt) await replaceVideoTrackOnAllPeers(vt);
        setControlLabel(screenShareBtn, "Stop share");
      } catch (e) {
        alert((e && e.name === "NotAllowedError")
          ? "Screen share was blocked/cancelled."
          : (e.message || "Screen share failed"));
      }
    });
  }

  if (fullScreenBtn) {
    fullScreenBtn.addEventListener("click", function () {
      if (!isFullscreen()) enterFullscreen().then(syncFullscreenUi);
      else exitFullscreen().then(syncFullscreenUi);
    });
    setControlLabel(fullScreenBtn, "Fullscreen");
  }

  if (fsChatToggleBtn && chatSection) {
    fsChatToggleBtn.addEventListener("click", function () {
      if (!isFullscreen()) return;
      chatSection.classList.toggle("is-collapsed");
      fsChatToggleBtn.textContent = chatSection.classList.contains("is-collapsed") ? "💬" : "✕";
      fsChatToggleBtn.title = chatSection.classList.contains("is-collapsed") ? "Open chat panel" : "Close chat panel";
      fsChatToggleBtn.setAttribute("aria-label", fsChatToggleBtn.title);
    });
  }

  document.addEventListener("fullscreenchange", syncFullscreenUi);
  document.addEventListener("webkitfullscreenchange", syncFullscreenUi);
  document.addEventListener("mozfullscreenchange", syncFullscreenUi);
  document.addEventListener("MSFullscreenChange", syncFullscreenUi);

  if (micSelect) {
    micSelect.addEventListener("change", async function () {
      if (!started || screenStream) return;
      if (!hasMediaSupport()) {
        alert(mediaSupportErrorMessage());
        return;
      }
      try {
        const m = micSelect.value;
        const newS = await navigator.mediaDevices.getUserMedia({
          video: false,
          audio: m ? { deviceId: { exact: m } } : true
        });
        const na = newS.getAudioTracks()[0];
        if (!localStream || !na) return;
        localStream.getAudioTracks().forEach(function (t) {
          localStream.removeTrack(t);
          try {
            t.stop();
          } catch (e) {}
        });
        localStream.addTrack(na);
        Object.keys(peers).forEach(function (k) {
          const sender = peers[k].pc.getSenders().find(function (s) {
            return s.track && s.track.kind === "audio";
          });
          if (sender) sender.replaceTrack(na);
        });
      } catch (e) {
        alert(e.message || "Could not switch microphone");
      }
    });
  }

  if (cameraSelect) {
    cameraSelect.addEventListener("change", async function () {
      if (!started || screenStream) return;
      if (!hasMediaSupport()) {
        alert(mediaSupportErrorMessage());
        return;
      }
      try {
        const v = cameraSelect.value;
        const newS = await navigator.mediaDevices.getUserMedia({
          video: v ? { deviceId: { exact: v } } : true,
          audio: false
        });
        const nv = newS.getVideoTracks()[0];
        if (!localStream || !nv) return;
        localStream.getVideoTracks().forEach(function (t) {
          localStream.removeTrack(t);
          try {
            t.stop();
          } catch (e) {}
        });
        localStream.addTrack(nv);
        Object.keys(peers).forEach(function (k) {
          const sender = peers[k].pc.getSenders().find(function (s) {
            return s.track && s.track.kind === "video";
          });
          if (sender) sender.replaceTrack(nv);
        });
        renderParticipants(lastPresenceList || []);
        setMainStream(localStream, true);
      } catch (e) {
        alert(e.message || "Could not switch camera");
      }
    });
  }

  if (sendBtn && chatInput) {
    const sendChat = function () {
      const text = chatInput.value.trim();
      if (!text || !stomp) return;
      chatInput.value = "";

      const whoMe = myName + (myUserId ? " (" + myUserId + ")" : "");
      appendChatMessage({ self: true, sender: whoMe, text: text });
      stomp.send(
        "/app/meeting/chat",
        {},
        JSON.stringify({
          sender: role,
          senderName: myName,
          senderId: myUserId,
          fromClientId: myClientId,
          payload: { text: text }
        })
      );
    };

    sendBtn.addEventListener("click", sendChat);
    chatInput.addEventListener("keypress", function (e) {
      if (e.key === "Enter") sendChat();
    });

    chatInput.addEventListener("input", function () {
      if (!stomp) return;
      const now = Date.now();
      if (now - lastTypingSent < 1500) return;
      lastTypingSent = now;
      stomp.send(
        "/app/meeting/typing",
        {},
        JSON.stringify({
          fromClientId: myClientId,
          senderName: myName,
          role: role,
          typing: true
        })
      );
    });
  }

  if (docInput) {
    docInput.addEventListener("change", async function () {
      if (!docInput.files || docInput.files.length === 0) return;
      if (!stomp || !started) {
        alert("Start/Join meeting before sharing documents.");
        docInput.value = "";
        return;
      }

      const file = docInput.files[0];
      const fd = new FormData();
      fd.append("file", file);

      const res = await fetch("/meeting/upload", { method: "POST", body: fd });
      if (!res.ok) {
        alert("Upload failed");
        docInput.value = "";
        return;
      }
      const meta = await res.json();

      if (docName) docName.textContent = meta.fileName || file.name;
      if (sharedDoc) sharedDoc.style.display = "block";

      const whoMe = myName + (myUserId ? " (" + myUserId + ")" : "");
      appendChatMessage({ self: true, sender: whoMe, fileName: meta.fileName, fileUrl: meta.fileUrl });
      stomp.send(
        "/app/meeting/chat",
        {},
        JSON.stringify({
          sender: role,
          senderName: myName,
          senderId: myUserId,
          fromClientId: myClientId,
          payload: { fileName: meta.fileName, fileUrl: meta.fileUrl }
        })
      );

      docInput.value = "";
    });
  }

  if (refreshBookingSelectBtn) {
    refreshBookingSelectBtn.addEventListener("click", function () {
      loadAdminBookingOptions().catch(function () {});
    });
  }
  if (adminMeetingMode) {
    adminMeetingMode.addEventListener("change", function () {
      currentMeetingMode = getMeetingModeFromUi();
      syncAdminModeUi();
      applyModeBehaviorUi();
    });
  }
  if (adminStartBookingSelect) {
    adminStartBookingSelect.addEventListener("change", function () {
      const sid = Number(adminStartBookingSelect.value || 0);
      const meta = adminSlotMeta[sid];
      if (adminSlotSummary) {
        if (sid && meta) {
          adminSlotSummary.style.display = "";
          adminSlotSummary.innerHTML =
            "<strong>Scheduled Slot Summary:</strong> " +
            Number(meta.bookedCount || 0) +
            " booked user(s)<br/>" +
            "<div style='margin-top:6px;max-height:84px;overflow:auto'>" +
            meta.users.map(function (u) { return "• " + u; }).join("<br/>") +
            "</div>";
        } else {
          adminSlotSummary.style.display = "none";
          adminSlotSummary.innerHTML = "";
        }
      }
    });
  }
  if (adminStartBookingSelect && role === "admin") {
    loadAdminBookingOptions().catch(function () {});
  }

  wireChatTabs();
  setStartedUi(false);
  if (!hasMediaSupport()) {
    setStatus("Camera is unavailable on insecure HTTP. Use HTTPS (or localhost) for video call.");
  }
  syncFullscreenUi();
  applySpeakerState();
  if (role === "admin") {
    currentMeetingMode = getMeetingModeFromUi();
    syncAdminModeUi();
  }
  applyModeBehaviorUi();

  // Pre-fill booking id if it was set from appointments/dashboard earlier.
  try {
    const bid = guessBookingId();
    if (bid && bookingIdInput && !bookingIdInput.value) bookingIdInput.value = String(bid);
  } catch (e) {}

  function stopCommunicationPolling() {
    if (commPollTimer) clearInterval(commPollTimer);
    commPollTimer = null;
    if (speechActivityTimer) clearInterval(speechActivityTimer);
    speechActivityTimer = null;
  }

  function stopTranscriptPolling() {
    if (transcriptPollTimer) clearInterval(transcriptPollTimer);
    transcriptPollTimer = null;
  }

  function startTranscriptPolling() {
    stopTranscriptPolling();
    if (!transcriptText) return;
    transcriptPollTimer = setInterval(function () {
      const bid = getBookingIdFromUiOrStorage();
      if (!bid) return;
      fetch("/api/meeting/" + bid + "/stt/finalize", { method: "POST" })
        .then(function (r) { return r.json(); })
        .then(function (d) {
          const t = (d && d.transcriptText) ? String(d.transcriptText) : "";
          currentTranscriptRaw = t;
          transcriptText.textContent = t ? t : "(listening…)";
          renderTranscriptIssues(currentTranscriptRaw);
          if (transcriptBox) transcriptBox.style.display = "";
        })
        .catch(function () {});
    }, 5000);
  }

  function startCommunicationPolling() {
    stopCommunicationPolling();
    if (!commScoreValue || !commScoreBar) return;
    commPollTimer = setInterval(function () {
      const bid = getBookingIdFromUiOrStorage();
      if (!bid) return;
      const elapsedSec = meetingStartTs ? Math.max(10, Math.floor((Date.now() - meetingStartTs) / 1000)) : 60;
      fetch("/api/meeting/" + bid + "/stt/finalize", { method: "POST" })
        .then(function (r) { return r.json(); })
        .then(function (d) {
          const t = (d && d.transcriptText) ? String(d.transcriptText) : "";
          if (!t.trim()) {
            if (commStatus) commStatus.textContent = "Waiting for transcript…";
            return;
          }
          if (commStatus) commStatus.textContent = "Scoring…";
          return fetch("/api/interview-upgrade/communication/score", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              bookingId: bid,
              transcriptText: t,
              speakingDurationSeconds: elapsedSec
            })
          }).then(function (r2) { return r2.json(); });
        })
        .then(function (scoreResp) {
          if (!scoreResp || !scoreResp.communicationScore) return;
          const v = Number(scoreResp.communicationScore);
          const n = Number.isFinite(v) ? Math.max(0, Math.min(100, v)) : 0;
          commScoreValue.textContent = n.toFixed(2) + "/100";
          commScoreBar.style.width = n + "%";
          if (commStatus) {
            const fillers = Number(scoreResp.fillerWordsCount || 0);
            const wpm = Number(scoreResp.speakingSpeedWpm || 0);
            if (fillers > 10) commStatus.textContent = "Live: reduce filler words";
            else if (wpm > 190) commStatus.textContent = "Live: speaking too fast";
            else if (wpm > 0 && wpm < 90) commStatus.textContent = "Live: speak slightly faster";
            else commStatus.textContent = "Live: good pace";
          }
        })
        .catch(function () {
          if (commStatus) commStatus.textContent = "Scoring failed";
        });
    }, 6000);
    if (speechActivityTimer) clearInterval(speechActivityTimer);
    speechActivityTimer = setInterval(function () {
      if (!commStatus || !started) return;
      const idleMs = Date.now() - (lastTranscriptAt || 0);
      if (!lastTranscriptAt || idleMs > 9000) {
        commStatus.textContent = "No speech detected (listening...)";
      }
    }, 2000);
  }

  function finalizeMeetingArtifacts(publishFinalReport) {
    if (isNormalMode()) return Promise.resolve();
    const bid = getBookingIdFromUiOrStorage();
    if (!bid) return Promise.resolve();
    const elapsedSec = meetingStartTs ? Math.max(10, Math.floor((Date.now() - meetingStartTs) / 1000)) : 60;
    if (commStatus) commStatus.textContent = "Finalizing…";
    return fetch("/api/meeting/" + bid + "/stt/finalize", { method: "POST" })
      .then(function (r) { return r.json(); })
      .then(function (d) {
        const t = (d && d.transcriptText) ? String(d.transcriptText) : "";
        const payload = { bookingId: bid, transcriptText: t, speakingDurationSeconds: elapsedSec };
        if (role === "admin" && adminBehInput && adminBehInput.value !== "") {
          const bv = Number(adminBehInput.value);
          if (Number.isFinite(bv) && bv >= 0 && bv <= 100) payload.behavioralScore = bv;
        }
        return fetch("/api/interview-upgrade/communication/score", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        }).then(function (r2) { return r2.json(); });
      })
      .then(function () {
        if (!publishFinalReport) return null;
        // Publish + save PDF report, and email users their rank
        return fetch("/api/interview-upgrade/report/final/publish?sendEmails=true", { method: "POST" })
          .then(function (r3) { return r3.json(); })
          .then(function () { return null; });
      })
      .catch(function () {})
      .finally(function () {
        if (commStatus) commStatus.textContent = "Finalized";
      });
  }
}

function wireChatTabs() {
  const chatPanel = $("chatTabPanel");
  const partPanel = $("participantTabPanel");
  if (chatPanel) chatPanel.style.display = "";
  if (partPanel) partPanel.style.display = "";
}

// --- Live STT streaming (audio -> backend) ---
let sttRecorder = null;
let sttStreaming = false;
let sttBookingId = null;
let sttChunkSeq = 0;
let sttUploadQueue = [];
let sttUploading = false;

function guessBookingId() {
  try {
    const b = sessionStorage.getItem("currentBookingId") || localStorage.getItem("currentBookingId");
    if (b) return Number(b);
  } catch (e) {}
  return null;
}

function startSttStreaming(role) {
  if (!hasMediaSupport()) return;
  const bookingId = guessBookingId();
  if (!bookingId) return;
  const stream = meetingLocalStream;
  if (!stream) return;
  if (sttStreaming) return;
  sttStreaming = true;
  sttBookingId = bookingId;
  sttChunkSeq = 0;
  try {
    sttRecorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
  } catch (e) {
    console.warn("MediaRecorder not available for STT", e);
    sttStreaming = false;
    return;
  }
  sttRecorder.addEventListener("dataavailable", function (evt) {
    if (!evt.data || !evt.data.size || !sttStreaming || !sttBookingId) return;
    sttChunkSeq += 1;
    sttUploadQueue.push({
      seq: sttChunkSeq,
      data: evt.data,
      retryCount: 0,
      nextTryAt: Date.now()
    });
    pumpSttQueue();
  });
  sttRecorder.start(4000); // 4s chunks
}

function stopSttStreaming() {
  sttStreaming = false;
  sttBookingId = null;
  if (sttRecorder) {
    try {
      sttRecorder.stop();
    } catch (e) {}
  }
  sttRecorder = null;
  sttUploadQueue = [];
  sttUploading = false;
}

function pumpSttQueue() {
  if (sttUploading || !sttStreaming || !sttBookingId) return;
  if (!sttUploadQueue.length) return;
  const item = sttUploadQueue[0];
  if (Date.now() < item.nextTryAt) {
    setTimeout(pumpSttQueue, Math.max(200, item.nextTryAt - Date.now()));
    return;
  }
  sttUploading = true;
  const fd = new FormData();
  fd.append("audio", item.data, "chunk.webm");
  fd.append("clientId", "browser-client");
  fd.append("chunkSeq", String(item.seq));
  fetch("/api/meeting/" + sttBookingId + "/stt/chunk", { method: "POST", body: fd })
    .then(function (r) {
      if (!r.ok) throw new Error("upload_failed");
      return r.json();
    })
    .then(function () {
      sttUploadQueue.shift();
    })
    .catch(function () {
      item.retryCount += 1;
      const backoff = Math.min(12000, 500 * Math.pow(2, item.retryCount));
      item.nextTryAt = Date.now() + backoff;
    })
    .finally(function () {
      sttUploading = false;
      if (sttUploadQueue.length) setTimeout(pumpSttQueue, 200);
    });
}

window.MeetingApp = { startMeeting };
