document.addEventListener("DOMContentLoaded", () => {
  const addBtn = document.getElementById("addLanguageBtn");
  const container = document.getElementById("languageContainer");
  let langIndex = 1;

  const programmingLanguages = [
    "C", "C++", "Java", "Python", "JavaScript",
    "TypeScript", "Kotlin", "Go", "Rust", "Swift", "Ruby", "PHP"
  ];

  addBtn.addEventListener("click", () => {
    const id = `certUpload${langIndex}`;
    const fileId = `fileName${langIndex}`;

    const block = document.createElement("div");
    block.className = "grid grid-cols-1 sm:grid-cols-6 gap-4 max-w-full";

    block.innerHTML = `
      <select class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-[9px] font-semibold shadow-inner col-span-2">
        <option disabled selected>SELECT THE PROGRAMMING LANGUAGE</option>
        ${programmingLanguages.map(lang => `<option value="${lang.toLowerCase()}">${lang}</option>`).join("")}
      </select>

      <input class="bg-gray-100 text-gray-900 rounded-lg py-2 px-3 text-[9px] font-semibold placeholder-gray-600 shadow-inner col-span-3"
             placeholder="ENTER THE CERTIFICATE COMPANY NAME" type="text"/>

      <!-- Hidden file input (only PDF) -->
      <input type="file" id="${id}" accept=".pdf" style="display: none;" />

      <!-- Upload Button -->
      <button type="button"
              onclick="document.getElementById('${id}').click();"
              class="bg-amber-400 hover:bg-amber-500 text-gray-900 rounded-lg py-2 px-3 text-[9px] font-semibold shadow-md transition">
        UPLOAD CERTIFICATE
      </button>

      <!-- File name preview -->
      <small id="${fileId}" class="text-xs text-gray-400 col-span-6 italic mt-1 block"></small>
    `;

    container.appendChild(block);

    // Handle file name preview
    const fileInput = document.getElementById(id);
    fileInput.addEventListener("change", (e) => {
      const file = e.target.files[0];
      const fileNameTag = document.getElementById(fileId);

      if (file && file.type === "application/pdf") {
        fileNameTag.textContent = `Uploaded: ${file.name}`;
      } else {
        fileNameTag.textContent = "Only PDF files are allowed!";
        fileInput.value = ""; // reset
      }
    });

    langIndex++;
  });
});
