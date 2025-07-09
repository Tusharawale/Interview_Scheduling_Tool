document.addEventListener("DOMContentLoaded", () => {
  const educationSelect = document.getElementById("educationSelect");
  const semesterSelect = document.getElementById("semesterSelect");

  const educationSemesters = {
    btech: ["1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"],
    diploma: ["1st", "2nd", "3rd", "4th", "5th", "6th"],
    mtech: ["1st", "2nd", "3rd", "4th"]
  };

  educationSelect.addEventListener("change", () => {
    const selected = educationSelect.value;
    const semesters = educationSemesters[selected] || [];

    semesterSelect.innerHTML = `<option disabled selected>SELECT SEMESTER</option>`;
    semesters.forEach(sem => {
      const option = document.createElement("option");
      option.value = sem.toLowerCase();
      option.textContent = sem;
      semesterSelect.appendChild(option);
    });
  });
});
