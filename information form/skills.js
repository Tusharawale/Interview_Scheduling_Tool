document.getElementById("addSkillBtn").addEventListener("click", () => {
  const skill = prompt("Enter your skill:");

  if (skill && skill.trim() !== "") {
    const skillSpan = document.createElement("span");
    skillSpan.className = "inline-block bg-amber-400 rounded-md px-5 py-2 text-sm font-semibold shadow-md";
    skillSpan.textContent = skill.trim();

    document.getElementById("skillsContainer").appendChild(skillSpan);
  }
});
