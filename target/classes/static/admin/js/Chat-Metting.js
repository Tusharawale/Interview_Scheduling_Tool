/* ===== ELEMENTS ===== */
const chatMessages = document.getElementById("chatMessages");
const chatInput = document.getElementById("chatInput");
const sendBtn = document.getElementById("sendBtn");
const docInput = document.getElementById("docInput");
const sharedDoc = document.getElementById("sharedDoc");
const docName = document.getElementById("docName");

/* ===== SEND MESSAGE ===== */
function sendMessage() {
    const text = chatInput.value.trim();
    if (text === "") return;

    const msg = document.createElement("div");
    msg.classList.add("message", "sent");
    msg.textContent = text;

    chatMessages.appendChild(msg);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    chatInput.value = "";
}

/* CLICK SEND */
sendBtn.addEventListener("click", sendMessage);

/* ENTER KEY */
chatInput.addEventListener("keypress", function(e) {
    if (e.key === "Enter") {
        sendMessage();
    }
});

/* FILE SHARE */
docInput.addEventListener("change", function() {
    if (docInput.files.length > 0) {
        const file = docInput.files[0];
        docName.textContent = file.name;
        sharedDoc.style.display = "block";
    }
});

/* DEMO: RECEIVE MESSAGE (simulate admin/user) */
setTimeout(() => {
    const msg = document.createElement("div");
    msg.classList.add("message", "received");
    msg.textContent = "Hello from Admin!";
    chatMessages.appendChild(msg);
}, 2000);
