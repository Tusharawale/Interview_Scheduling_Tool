document.addEventListener("DOMContentLoaded", () => {
  const educationSelect = document.getElementById("educationSelect");
  const semesterFields = document.getElementById("semesterFields");

  const semesterCount = {
    btech: 8,
    diploma: 6,
    mtech: 4
  };

  educationSelect.addEventListener("change", () => {
    const total = semesterCount[educationSelect.value] || 0;
    semesterFields.innerHTML = ""; // Clear previous blocks

    for (let i = 1; i <= total; i++) {
      // Unique IDs for file inputs
      const inputId = `uploadInput${i}`;

      semesterFields.innerHTML += `
        <div class="contents">
          <select class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner col-span-2">
            <option>Semester ${i}</option>
          </select>

          <input class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner col-span-2"
                 placeholder="TOTAL MARKS" type="number" step="0.01" min="0"/>

          <input class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner col-span-2"
                 placeholder="TOTAL MARKS OBTAIN" type="number" step="0.01" min="0"/>

          <input class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner col-span-2"
                 placeholder="CGPA" type="number" step="0.01" min="0"/>

          <!-- Hidden file input -->
          <input type="file" id="${inputId}" style="display: none;" />

          <!-- Upload Button -->
          <button onclick="document.getElementById('${inputId}').click();"
                  class="bg-amber-400 hover:bg-amber-500 text-gray-900 rounded-lg py-2 px-3 text-xs font-semibold shadow-md col-span-2 transition"
                  type="button">
            UPLOAD DOCUMENT
          </button>
        </div>
      `;
    }
  });
});
