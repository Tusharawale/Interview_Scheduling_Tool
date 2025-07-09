document.addEventListener("DOMContentLoaded", () => {
  const experienceContainer = document.getElementById("experienceContainer");
  const addExperienceBtn = document.getElementById("addExperienceBtn");
  let experienceIndex = 1;

  addExperienceBtn.addEventListener("click", () => {
    const fileId = `expFile${experienceIndex}`;
    const fileLabelId = `expLabel${experienceIndex}`;

    const experienceBlock = document.createElement("div");
    experienceBlock.className = "experience-entry grid grid-cols-1 sm:grid-cols-6 gap-4 max-w-full";

    experienceBlock.innerHTML = `
      <input class="bg-gray-100 text-gray-900 rounded-lg py-3 px-4 text-xl font-extrabold placeholder-gray-600 shadow-inner col-span-3"
             placeholder="COMPANY NAME" type="text"/>

      <input class="bg-gray-100 text-gray-900 rounded-lg py-3 px-4 text-xs font-semibold placeholder-gray-600 shadow-inner col-span-1"
             placeholder="START DATE" type="date"/>

      <input class="bg-gray-100 text-gray-900 rounded-lg py-3 px-4 text-xs font-semibold placeholder-gray-600 shadow-inner col-span-1"
             placeholder="END DATE" type="date"/>

      <!-- Hidden PDF Input -->
      <input type="file" id="${fileId}" accept=".pdf" style="display:none" />

      <!-- Upload Button -->
      <button type="button"
              onclick="document.getElementById('${fileId}').click();"
              class="bg-amber-400 hover:bg-amber-500 text-gray-900 rounded-full py-2 px-8 text-sm font-semibold col-span-1 shadow-md transition">
        UPLOAD LETTER
      </button>

      <!-- File name -->
      <small id="${fileLabelId}" class="text-xs text-gray-400 italic col-span-6 mt-1 block"></small>
    `;

    experienceContainer.appendChild(experienceBlock);

    const fileInput = document.getElementById(fileId);
    const fileLabel = document.getElementById(fileLabelId);

    fileInput.addEventListener("change", (e) => {
      const file = e.target.files[0];
      if (file && file.type === "application/pdf") {
        fileLabel.textContent = `Uploaded: ${file.name}`;
      } else {
        fileLabel.textContent = "❌ Only PDF files allowed!";
        fileInput.value = "";
      }
    });

    experienceIndex++;
  });
});
