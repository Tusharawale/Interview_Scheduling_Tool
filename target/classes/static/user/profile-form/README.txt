Information form — profile edit (no Tailwind)
==============================================
Files:
  form2.html              — main Edit Profile page (sidebar + all sections)
  form.css                — custom styles only
  college.js              — college → branch
  education.js            — course → semester list
  semesterFields.js       — semester-wise marks / CGPA / file + Save
  programmingLanguages.js — "+ Add Programming Language" → main script
  skills.js               — "+" skills → main script
  experience.js           — "+ Add Experience" → main script
  imageUpload.js          — profile photo preview

API logic lives in: ../User-js/user-profile-edit.js

user-profile-edit.html (project root) redirects here so old links still work.

If you move or rename this folder, update:
  - static/user-profile-edit.html redirect URL
  - form2.html script/link paths (../User-js/ and ../User-css/)
