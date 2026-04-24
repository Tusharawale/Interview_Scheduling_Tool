/**
 * + button → add skill row (saved via user-profile-edit.js).
 */
document.addEventListener('DOMContentLoaded', () => {
  const addBtn = document.getElementById('addSkillBtn');
  if (!addBtn) return;
  addBtn.addEventListener('click', () => {
    if (typeof window.addSkillRow === 'function') {
      window.addSkillRow();
    }
  });
});
