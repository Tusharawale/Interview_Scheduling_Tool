/**
 * + Add Programming Language → uses main profile script (plRows).
 */
document.addEventListener('DOMContentLoaded', () => {
  const addBtn = document.getElementById('addLanguageBtn');
  if (!addBtn) return;
  addBtn.addEventListener('click', () => {
    if (typeof window.addPlRow === 'function') {
      window.addPlRow();
    }
  });
});
