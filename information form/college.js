document.addEventListener("DOMContentLoaded", () => {
  const collegeSelect = document.getElementById("collegeSelect");
  const branchSelect = document.getElementById("branchSelect");

  const collegeBranches = {
    vnit: ["CSE", "ECE", "EEE", "Mechanical", "Civil"],
    gcoen: ["Computer Tech", "IT", "Electrical", "EXTC"],
    rcoem: ["CSE", "AI/ML", "Electronics", "Civil"],
    ghrce: ["IT", "AI-DS", "Mech", "EXTC"],
    ycce: ["Mech", "Civil", "AI-ML", "IT"],
    pce: ["CSE", "IT", "AI-DS", "C.Tech"],
    default: ["CSE", "IT", "Mechanical", "Civil"]
  };

  collegeSelect.addEventListener("change", () => {
    const selectedCollege = collegeSelect.value;
    const branches = collegeBranches[selectedCollege] || collegeBranches["default"];

    branchSelect.innerHTML = `<option disabled selected>SELECT YOUR BRANCH</option>`;
    branches.forEach(branch => {
      const option = document.createElement("option");
      option.value = branch.toLowerCase().replace(/\s+/g, "-");
      option.textContent = branch;
      branchSelect.appendChild(option);
    });
  });
});
