/**
 * + Add Experience → uses main profile script.
 */
document.addEventListener('DOMContentLoaded', () => {
  const addBtn = document.getElementById('addExperienceBtn');
  if (!addBtn) return;
  addBtn.addEventListener('click', () => {
    if (typeof window.addExperienceRow === 'function') {
      window.addExperienceRow();
    }
  });
});
