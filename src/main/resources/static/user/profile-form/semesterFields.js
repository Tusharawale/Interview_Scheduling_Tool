/**
 * Semester-wise result rows (marks, CGPA, document) — no Tailwind.
 * Requires: #educationSelect, #semesterFields
 * Calls: window.saveSemesterEducation(semesterNumber)
 */
document.addEventListener('DOMContentLoaded', () => {
  const educationSelect = document.getElementById('educationSelect');
  const semesterFields = document.getElementById('semesterFields');
  if (!educationSelect || !semesterFields) return;

  const semesterCount = {
    btech: 8,
    diploma: 6,
    mtech: 4
  };

  educationSelect.addEventListener('change', () => {
    const total = semesterCount[educationSelect.value] || 0;
    semesterFields.innerHTML = '';

    const parts = [];
    for (let i = 1; i <= total; i++) {
      parts.push(`
        <div class="semester-row">
          <div class="sem-label">Semester ${i}</div>
          <input type="number" id="eduSemTotal${i}" placeholder="Total marks" min="0" step="1"/>
          <input type="number" id="eduSemMarks${i}" placeholder="Marks obtained" min="0" step="1"/>
          <input type="text" id="eduSemCgpa${i}" placeholder="CGPA" inputmode="decimal" readonly/>
          <input type="file" id="eduSemFile${i}" accept=".pdf,.jpg,.jpeg,.png,.webp,.doc,.docx" style="display:none"/>
          <button type="button" class="btn btn-orange" onclick="document.getElementById('eduSemFile${i}').click()">Choose file</button>
          <button type="button" class="btn" onclick="saveSemesterEducation(${i})">Save semester</button>
        </div>
      `);
    }
    semesterFields.innerHTML = parts.join('');

    const toNumberOrNull = (v) => {
      if (v == null || v === '') return null;
      const n = Number(v);
      return Number.isFinite(n) ? n : null;
    };
    const formatCgpa = (totalMarks, marksObtained) => {
      if (!totalMarks || totalMarks <= 0 || marksObtained == null || marksObtained < 0) return '';
      return Math.max(0, Math.min(10, (marksObtained / totalMarks) * 10)).toFixed(1);
    };

    const wireValidation = (semNo) => {
      const totalEl = document.getElementById('eduSemTotal' + semNo);
      const marksEl = document.getElementById('eduSemMarks' + semNo);
      const cgpaEl = document.getElementById('eduSemCgpa' + semNo);
      if (!totalEl || !marksEl || !cgpaEl) return;

      const sync = () => {
        const totalMarks = toNumberOrNull(totalEl.value);
        const marksObtained = toNumberOrNull(marksEl.value);
        if (totalMarks != null && totalMarks > 0) marksEl.max = String(totalMarks);
        if (totalMarks != null && marksObtained != null && marksObtained > totalMarks) {
          marksEl.value = String(totalMarks);
        }
        const safeMarks = toNumberOrNull(marksEl.value);
        cgpaEl.value = formatCgpa(totalMarks, safeMarks);
      };

      totalEl.addEventListener('input', sync);
      marksEl.addEventListener('input', sync);
      sync();
    };

    for (let i = 1; i <= total; i++) wireValidation(i);
  });
});
