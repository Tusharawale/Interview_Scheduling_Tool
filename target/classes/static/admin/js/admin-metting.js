const startBtn = document.getElementById("startBtn");
const endBtn = document.getElementById("endBtn");
const videoArea = document.getElementById("videoArea");
const localVideo = document.getElementById("localVideo");

startBtn.onclick = () => {
    videoArea.classList.add("active");
    localVideo.style.display = "flex";
    startBtn.disabled = true;
    endBtn.disabled = false;

    connectUser(1);
    setTimeout(() => connectUser(2), 1000);
    setTimeout(() => connectUser(3), 2000);
};

endBtn.onclick = () => {
    videoArea.classList.remove("active");
    localVideo.style.display = "none";
    startBtn.disabled = false;
    endBtn.disabled = true;

    document.querySelectorAll(".participant").forEach(p => {
        p.classList.remove("connected");
        p.querySelector(".status-badge").classList.remove("active");
    });
};

function connectUser(id) {
    let user = document.querySelector(`[data-user="${id}"]`);
    if (user) {
        user.classList.add("connected");
        user.querySelector(".status-badge").classList.add("active");
    }
}

/* CHAT */
const chatInput = document.getElementById("chatInput");
const sendBtn = document.getElementById("sendBtn");
const chatMessages = document.getElementById("chatMessages");

sendBtn.onclick = sendMessage;

function sendMessage() {
    let text = chatInput.value.trim();
    if (!text) return;

    let msg = document.createElement("div");
    msg.className = "message";
    msg.innerHTML = "<b>You:</b> " + text;

    chatMessages.appendChild(msg);
    chatInput.value = "";
}

/* FILE */
document.getElementById("docInput").onchange = (e) => {
    let file = e.target.files[0];
    if (!file) return;

    document.getElementById("sharedDoc").style.display = "block";
    document.getElementById("docName").innerText = file.name;
};