document.getElementById("messageForm").addEventListener("submit", function (e) {
  e.preventDefault();
  sendMessage();
});

document.getElementById("attachButton").addEventListener("click", function () {
  document.getElementById("fileInput").click();
});

document.getElementById("fileInput").addEventListener("change", function (e) {
  const file = e.target.files[0];
  if (file) sendFile(file);
});

function sendMessage() {
  const input = document.getElementById("messageInput");
  const messageText = input.value.trim();
  if (messageText === "") return;

  const formattedTime = getFormattedTime();

  const messageHTML = `
    <div class="flex justify-end">
      <div class="bg-purple-600 text-white rounded-xl px-4 py-2 max-w-[70%]">
        <p>${messageText}</p>
        <span class="block text-right text-[10px] mt-1">${formattedTime}</span>
      </div>
    </div>
  `;

  appendToChat(messageHTML);
  input.value = "";
}

function sendFile(file) {
  const fileName = file.name;
  const fileURL = URL.createObjectURL(file);
  const formattedTime = getFormattedTime();

  let filePreviewHTML;

  if (file.type.startsWith("image/")) {
    filePreviewHTML = `<img src="${fileURL}" class="max-w-[200px] rounded-lg" />`;
  } else {
    filePreviewHTML = `<a href="${fileURL}" download class="text-blue-600 underline">${fileName}</a>`;
  }

  const messageHTML = `
    <div class="flex justify-end">
      <div class="bg-purple-600 text-white rounded-xl px-4 py-2 max-w-[70%]">
        ${filePreviewHTML}
        <span class="block text-right text-[10px] mt-1">${formattedTime}</span>
      </div>
    </div>
  `;

  appendToChat(messageHTML);

  // Reset file input
  document.getElementById("fileInput").value = "";
}

function appendToChat(html) {
  const chatContainer = document.getElementById("chatContainer");
  chatContainer.innerHTML += html;
  chatContainer.scrollTop = chatContainer.scrollHeight;
}

function getFormattedTime() {
  const now = new Date();
  const hours = now.getHours();
  const minutes = now.getMinutes().toString().padStart(2, "0");
  const ampm = hours >= 12 ? "PM" : "AM";
  return `${(hours % 12) || 12}:${minutes} ${ampm}`;
}
