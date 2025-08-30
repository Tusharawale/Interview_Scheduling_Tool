# Interview Scheduling Tool


<img width="353" height="353" alt="image" src="https://github.com/user-attachments/assets/3f4a29e1-cad3-4c42-a9d8-fdde93363a26" />


![Project Banner](https://img.shields.io/badge/Interview%20Scheduling%20Tool-blueviolet?style=for-the-badge&logo=appveyor)

---

## 📖 Project Overview

The **Interview Scheduling Tool** is a comprehensive web application designed to simplify and automate the process of scheduling interviews for organizations. It enables candidates and interviewers to coordinate schedules effortlessly, manage slots, and reduce manual overhead.

Key functionalities include:

- Scheduling, rescheduling, and canceling interview slots
- Viewing and managing interview calendars
- Generating insightful reports for data-driven decisions
- Seamless data analysis integration

---

## 🛠️ Technologies Used

| Layer         | Technology             | Logo                                            |
|---------------|------------------------|------------------------------------------------|
| Frontend      | HTML, CSS, JavaScript  | ![HTML5](https://cdn.jsdelivr.net/gh/devicons/devicon/icons/html5/html5-original.svg) ![CSS3](https://cdn.jsdelivr.net/gh/devicons/devicon/icons/css3/css3-original.svg) ![JavaScript](https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg) |
| Backend       | Java Spring Boot       | ![Java](https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg) ![Spring Boot](.svg) |
| Database      | SQL                    | ![SQL](https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg) |
| Data Analysis | Power BI               | ![Power BI](.svg) |

---

## 🚀 Features

- **Interactive UI:** Responsive and clean frontend with HTML5, CSS3, and vanilla JavaScript
- **Robust Backend:** Built on Java Spring Boot for scalable API management and business logic
- **Secure Data Storage:** SQL database (MySQL/PostgreSQL) for reliable and consistent data handling
- **Data Insights:** Power BI dashboards integrated for real-time data visualization and reporting
- **Collaboration Ready:** Supports multiple user roles with permission management

---

## 🔧 Getting Started

### Prerequisites

- Java Development Kit (JDK 11 or above)
- Maven or Gradle for dependency management
- SQL database installed (MySQL, PostgreSQL, or other compatible DB)
- Power BI Desktop (for data visualization)
- Git (for cloning repo)

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Tusharawale/Interview_Scheduling_Tool.git
   cd Interview_Scheduling_Tool


   
#### Working ####
/* === tailwind-base.css === */

/* Root variables and resets */
:host,
html {
  line-height: 1.5;
  -webkit-text-size-adjust: 100%;
  -moz-tab-size: 4;
  tab-size: 4;
  font-family: ui-sans-serif, system-ui, sans-serif, "Apple Color Emoji",
    "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
  font-feature-settings: normal;
  font-variation-settings: normal;
  -webkit-tap-highlight-color: transparent;
}

/* Box sizing and border resets */
*,
::before,
::after {
  box-sizing: border-box;
  border-width: 0;
  border-style: solid;
  border-color: #e5e7eb; /* Tailwind gray-200 */
}

/* Content placeholder for pseudo-elements */
::before,
::after {
  --tw-content: '';
}

/* Utility variables (Tailwind internal CSS custom props) */
*,
::before,
::after {
  --tw-border-spacing-x: 0;
  --tw-border-spacing-y: 0;
  --tw-translate-x: 0;
  --tw-translate-y: 0;
  --tw-rotate: 0;
  --tw-skew-x: 0;
  --tw-skew-y: 0;
  --tw-scale-x: 1;
  --tw-scale-y: 1;
  --tw-pan-x: ;
  --tw-pan-y: ;
  --tw-pinch-zoom: ;
  --tw-scroll-snap-strictness: proximity;
  --tw-gradient-from-position: ;
  --tw-gradient-via-position: ;
  --tw-gradient-to-position: ;
  --tw-ordinal: ;
  --tw-slashed-zero: ;
  --tw-numeric-figure: ;
  --tw-numeric-spacing: ;
  --tw-numeric-fraction: ;
  --tw-ring-inset: ;
  --tw-ring-offset-width: 0px;
  --tw-ring-offset-color: #fff;
  --tw-ring-color: rgb(59 130 246 / 0.5);
  --tw-ring-offset-shadow: 0 0 #0000;
  --tw-ring-shadow: 0 0 #0000;
  --tw-shadow: 0 0 #0000;
  --tw-shadow-colored: 0 0 #0000;
  --tw-blur: ;
  --tw-brightness: ;
  --tw-contrast: ;
  --tw-grayscale: ;
  --tw-hue-rotate: ;
  --tw-invert: ;
  --tw-saturate: ;
  --tw-sepia: ;
  --tw-drop-shadow: ;
  --tw-backdrop-blur: ;
  --tw-backdrop-brightness: ;
  --tw-backdrop-contrast: ;
  --tw-backdrop-grayscale: ;
  --tw-backdrop-hue-rotate: ;
  --tw-backdrop-invert: ;
  --tw-backdrop-opacity: ;
  --tw-backdrop-saturate: ;
  --tw-backdrop-sepia: ;
  --tw-contain-size: ;
  --tw-contain-layout: ;
  --tw-contain-paint: ;
  --tw-contain-style: ;
}

/* Optional: if you want to match HTML tag rendering */
html {
  display: block;
}

<html lang="en">
 <head>
  <meta charset="utf-8"/>
  <meta content="width=device-width, initial-scale=1" name="viewport"/>
  <title>
   Form
  </title>
  <script src="https://cdn.tailwindcss.com">
  </script>
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css" rel="stylesheet"/>
  <style>
   /* Custom arrow for select */
  select {
    -webkit-appearance: none;
    -moz-appearance: none;
    appearance: none;
    background-image: url("data:image/svg+xml,%3csvg fill='%23343a40' height='10' viewBox='0 0 24 24' width='10' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M7 10l5 5 5-5z'/%3e%3c/svg%3e");
    background-repeat: no-repeat;
    background-position: right 0.75rem center;
    background-size: 10px 10px;
    padding-right: 2.5rem;
    transition: background-color 0.3s ease;
  }
  select:focus {
    outline: none;
    background-color: #fef3c7; /* Tailwind amber-100 */
  }
  input:focus, button:focus, select:focus, textarea:focus {
    outline: 2px solid #fbbf24; /* Tailwind amber-400 */
    outline-offset: 2px;
  }
  </style>
 </head>
 <body class="bg-gradient-to-r from-gray-900 via-gray-800 to-gray-900 text-white font-sans p-6 min-h-screen">
  <form class="max-w-full mx-auto space-y-10">
   <div class="flex flex-col md:flex-row md:space-x-8">
    <div class="flex flex-col items-center bg-gradient-to-b from-indigo-700 to-indigo-900 rounded-lg w-40 p-6 shrink-0 shadow-lg">
     <img alt="Placeholder profile image, circular shape, gray background" class="rounded-full mb-5 border-4 border-amber-400 shadow-lg" height="80" src="https://storage.googleapis.com/a1aa/image/119107b4-10c6-4507-aede-99e8720759bd.jpg" width="80"/>
     <button type="button"
        onclick="document.getElementById('imageUpload').click();"
        class="bg-amber-400 hover:bg-amber-500 text-gray-900 font-semibold text-xs rounded-full px-5 py-2 shadow-md transition">
  UPLOAD IMAGE
</button>
<input type="file" id="imageUpload" accept="image/*" style="display: none;">


    </div>
    <div class="flex-1 space-y-5">
     <h2 class="font-extrabold text-2xl tracking-wide text-amber-400 drop-shadow-lg">
      INFORMATION
     </h2>
     <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
      <input class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner" placeholder="First NAME" type="text"/>
      <input class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner" placeholder="Meddel Name" type="text"/>
      <input class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner" placeholder="LASTE NAME" type="text"/>
     </div>
     <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <input class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner" placeholder="Email id" type="email"/>
     <input 
  class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner focus:outline-none focus:ring-2 focus:ring-blue-500" 
  placeholder="Contact Number" 
  type="tel" 
  pattern="[0-9]{10}" 
  maxlength="10"
  inputmode="numeric" 
  title="Please enter a 10-digit contact number"
/>

     </div>
     <div class="grid grid-cols-1 sm:grid-cols-5 gap-4">
      <select aria-label="GENDER" class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner">
  <option disabled selected>GENDER</option>
  <option value="male">Male</option>
  <option value="female">Female</option>
  <option value="other">Other</option>
  <option value="prefer-not-to-say">Prefer not to say</option>
</select>

      
<input type="date" 
       id="dob"
       name="dob"
       class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner w-full" placeholder="Date of Barth" />

      </select>
     <select aria-label="Select your country" class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner w-full">
  <option disabled selected>SELECT YOUR COUNTRY</option>
  <option value="india">India</option>
  <option value="usa">United States</option>
  <option value="uk">United Kingdom</option>
  <option value="canada">Canada</option>
  <option value="australia">Australia</option>
  <option value="germany">Germany</option>
  <option value="france">France</option>
  <option value="japan">Japan</option>
  <option value="china">China</option>
  <option value="brazil">Brazil</option>
  <option value="south-africa">South Africa</option>
</select>

      </select>
      <select aria-label="State" class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner w-full">
  <option disabled selected>STATE</option>
  <option value="andhra-pradesh">Andhra Pradesh</option>
  <option value="arunachal-pradesh">Arunachal Pradesh</option>
  <option value="assam">Assam</option>
  <option value="bihar">Bihar</option>
  <option value="chhattisgarh">Chhattisgarh</option>
  <option value="goa">Goa</option>
  <option value="gujarat">Gujarat</option>
  <option value="haryana">Haryana</option>
  <option value="himachal-pradesh">Himachal Pradesh</option>
  <option value="jharkhand">Jharkhand</option>
  <option value="karnataka">Karnataka</option>
  <option value="kerala">Kerala</option>
  <option value="madhya-pradesh">Madhya Pradesh</option>
  <option value="maharashtra">Maharashtra</option>
  <option value="manipur">Manipur</option>
  <option value="meghalaya">Meghalaya</option>
  <option value="mizoram">Mizoram</option>
  <option value="nagaland">Nagaland</option>
  <option value="odisha">Odisha</option>
  <option value="punjab">Punjab</option>
  <option value="rajasthan">Rajasthan</option>
  <option value="sikkim">Sikkim</option>
  <option value="tamil-nadu">Tamil Nadu</option>
  <option value="telangana">Telangana</option>
  <option value="tripura">Tripura</option>
  <option value="uttar-pradesh">Uttar Pradesh</option>
  <option value="uttarakhand">Uttarakhand</option>
  <option value="west-bengal">West Bengal</option>
  <option value="andaman-nicobar">Andaman and Nicobar Islands</option>
  <option value="chandigarh">Chandigarh</option>
  <option value="dadra-nagar-haveli">Dadra and Nagar Haveli and Daman & Diu</option>
  <option value="delhi">Delhi</option>
  <option value="jammu-kashmir">Jammu and Kashmir</option>
  <option value="ladakh">Ladakh</option>
  <option value="lakshadweep">Lakshadweep</option>
  <option value="puducherry">Puducherry</option>
</select>

      <select aria-label="City" class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner w-full">
  <option disabled selected>CITY</option>
  <option value="mumbai">Mumbai</option>
  <option value="nagpur">Nagpur</option>
  <option value="pune">Pune</option>
  <option value="nashik">Nashik</option>
  <option value="aurangabad">Aurangabad</option>
  <option value="solapur">Solapur</option>
  <option value="amravati">Amravati</option>
  <option value="nanded">Nanded</option>
  <option value="kolhapur">Kolhapur</option>
  <option value="thane">Thane</option>
</select>

     </div>
     <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
     <input 
  class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner focus:outline-none focus:ring-2 focus:ring-blue-500" 
  placeholder="LinkedIn Profile URL" 
  type="url" 
  name="linkedin" 
  aria-label="LinkedIn Profile URL" 
/>

<input 
  class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold placeholder-gray-500 shadow-inner focus:outline-none focus:ring-2 focus:ring-blue-500" 
  placeholder="GitHub Profile URL" 
  type="url" 
  name="github" 
  aria-label="GitHub Profile URL" 
/>

     </div>
    </div>
   </div>
   <div>
    <!-- Qualification Section -->
<h2 class="font-extrabold text-2xl tracking-wide text-amber-400 drop-shadow-lg mb-5">
  QUALIFICATION
</h2>
<div class="grid grid-cols-1 sm:grid-cols-5 gap-4 mb-5">
  <!-- Engineering College -->
  <select id="collegeSelect" class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner w-full">
    <option disabled selected>SELECT ENGINEERING COLLEGE</option>
    <option value="vnit">VNIT</option>
    <option value="iiit">IIIT Nagpur</option>
    <option value="rcoem">RCOEM</option>
    <option value="gcoen">GCOEN</option>
    <option value="ghrce">GHRCE</option>
    <option value="ycce">YCCE</option>
    <option value="lit">LIT</option>
    <option value="sbjitmr">SBJITMR</option>
    <option value="cummins">Cummins</option>
    <option value="kits">KITS Ramtek</option>
    <option value="kdkce">KDKCE</option>
    <option value="jit">JIT</option>
    <option value="svpcet">SVPCET</option>
    <option value="pce">PCE</option>
    <option value="itgcoe">ITMCOE</option>
    <option value="anjuman">Anjuman</option>
    <option value="ramdeobaba-univ">Ramdeobaba University</option>
  </select>

  <!-- Branch (Dynamic) -->
  <select id="branchSelect" class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner mt-4">
    <option disabled selected>SELECT YOUR BRANCH</option>
  </select>

  <!-- Current Education -->
  <select id="educationSelect" class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner mt-4">
    <option disabled selected>SELECT CURRENT EDUCATION</option>
    <option value="btech">B.Tech</option>
    <option value="diploma">Diploma</option>
    <option value="mtech">M.Tech</option>
  </select>

  <!-- Semester (Dynamic dropdown) -->
  <select id="semesterSelect" class="bg-gray-200 text-gray-900 rounded-lg py-2 px-3 text-sm font-semibold shadow-inner mt-4">
    <option disabled selected>SEMESTER</option>
  </select>
</div>

<!-- ⬇️ Where generated semester fields will go -->
<div id="semesterFields" class="grid grid-cols-1 sm:grid-cols-10 gap-4 mt-6"></div>

    
     
     
    </div>
   </div>
   <div>
    <h2 class="font-extrabold text-2xl tracking-wide text-amber-400 drop-shadow-lg mb-5">
  PROGRAMMING LANGUAGES
</h2>

<!-- ADD BUTTON -->
<button
  id="addLanguageBtn"
  type="button"
  class="mt-4 bg-amber-500 hover:bg-amber-600 text-white rounded-full px-6 py-2 text-sm font-bold shadow-lg transition"
>
  + Add Programming Language
</button>

<!-- Languages Container -->
<div id="languageContainer" class="grid grid-cols-1 gap-4 mt-6"></div>


<!-- Container for language blocks -->
<div id="languageContainer">
  <div class="grid grid-cols-1 sm:grid-cols-6 gap-4 mb-5 max-w-full">
  
   
  </div>
</div>

    </div>
   </div>
   <div>
  <h2 class="font-extrabold text-2xl tracking-wide text-amber-400 drop-shadow-lg mb-5 flex items-center justify-between">
  SKILLS
  <button id="addSkillBtn" class="bg-amber-400 hover:bg-amber-500 text-gray-900 rounded-full p-2 shadow-md text-xl font-bold">
    +
  </button>
</h2>

<div id="skillsContainer" aria-label="Skills area" class="bg-gray-100 rounded-lg p-6 min-h-[120px] relative text-gray-900 shadow-inner flex flex-wrap gap-2">
  <span class="inline-block bg-amber-400 rounded-md px-5 py-2 text-sm font-semibold shadow-md">
    Example Skill
  </span>
</div>

    </div>
   </div>
   <div>
   <h2 class="font-extrabold text-2xl tracking-wide text-amber-400 drop-shadow-lg mb-5">
  EXPERIENCE
</h2>

<!-- Add Experience Button --

< Container for all experience entries -->
<div id="experienceContainer" class="space-y-6"></div>

  </div>
</div>

<!-- Add Experience Button -->
<button id="addExperienceBtn" class="mt-4 bg-amber-500 hover:bg-amber-600 text-white rounded-full px-6 py-2 text-sm font-bold shadow-lg transition" type="button">
  + Add Experience
</button>

   </div>
  </form>
  
<script src="college.js"></script>          <!-- Handles college → branch -->
<script src="education.js"></script>        <!-- Handles education → semester dropdown -->
<script src="semesterFields.js"></script> 
<script src="programmingLanguages.js"></script> <!-- Generates input blocks per semester -->
<script src="skills.js"></script>
<script src="experience.js"></script>
<script src="imageUpload.js"></script>


 </body>
</html>

