/**
 * Live preview for profile photo when user picks a file.
 */
document.addEventListener('DOMContentLoaded', () => {
  const fileInput = document.getElementById('profilePhoto');
  const preview = document.getElementById('editProfilePreview');
  if (!fileInput || !preview) return;

  fileInput.addEventListener('change', (event) => {
    const file = event.target.files && event.target.files[0];
    if (file) {
      preview.src = URL.createObjectURL(file);
    }
  });
});
