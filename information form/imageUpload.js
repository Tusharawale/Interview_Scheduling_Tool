const imageInput = document.getElementById('imageUpload');
const profileImage = document.getElementById('profileImage');

imageInput.addEventListener('change', (event) => {
  const file = event.target.files[0];
  if (file) {
    const imageURL = URL.createObjectURL(file);
    profileImage.src = imageURL;
  }
});

