// Enhanced appointment data with more dates
const appointmentsData = {
  "2024-12-14": [
    {
      id: 1,
      name: "John Smith",
      position: "Frontend Developer",
      education: "Computer Science, MIT",
      linkedin: "https://linkedin.com/in/johnsmith",
      github: "https://github.com/johnsmith",
      time: "09:30 AM",
      email: "john.smith@email.com",
      phone: "+1-555-0123",
    },
  ],
  "2024-12-15": [
    {
      id: 2,
      name: "Sarah Johnson",
      position: "UX Designer",
      education: "Design, Stanford University",
      linkedin: "https://linkedin.com/in/sarahjohnson",
      github: "https://github.com/sarahjohnson",
      time: "11:00 AM",
      email: "sarah.johnson@email.com",
      phone: "+1-555-0124",
    },
  ],
  "2024-12-16": [
    {
      id: 3,
      name: "Michael Brown",
      position: "Full Stack Developer",
      education: "Software Engineering, UC Berkeley",
      linkedin: "https://linkedin.com/in/michaelbrown",
      github: "https://github.com/michaelbrown",
      time: "10:30 AM",
      email: "michael.brown@email.com",
      phone: "+1-555-0125",
    },
    {
      id: 4,
      name: "Emily Davis",
      position: "Data Scientist",
      education: "Data Science, Harvard",
      linkedin: "https://linkedin.com/in/emilydavis",
      github: "https://github.com/emilydavis",
      time: "02:15 PM",
      email: "emily.davis@email.com",
      phone: "+1-555-0126",
    },
    {
      id: 5,
      name: "David Wilson",
      position: "Backend Developer",
      education: "Computer Engineering, CMU",
      linkedin: "https://linkedin.com/in/davidwilson",
      github: "https://github.com/davidwilson",
      time: "03:45 PM",
      email: "david.wilson@email.com",
      phone: "+1-555-0127",
    },
    {
      id: 6,
      name: "Lisa Anderson",
      position: "Product Manager",
      education: "Business Administration, Wharton",
      linkedin: "https://linkedin.com/in/lisaanderson",
      github: "https://github.com/lisaanderson",
      time: "04:30 PM",
      email: "lisa.anderson@email.com",
      phone: "+1-555-0128",
    },
  ],
  "2024-12-17": [
    {
      id: 7,
      name: "Robert Taylor",
      position: "DevOps Engineer",
      education: "Systems Engineering, Georgia Tech",
      linkedin: "https://linkedin.com/in/roberttaylor",
      github: "https://github.com/roberttaylor",
      time: "01:00 PM",
      email: "robert.taylor@email.com",
      phone: "+1-555-0129",
    },
  ],
  "2024-12-19": [
    {
      id: 8,
      name: "Jennifer Martinez",
      position: "Mobile Developer",
      education: "Computer Science, UCLA",
      linkedin: "https://linkedin.com/in/jennifermartinez",
      github: "https://github.com/jennifermartinez",
      time: "10:00 AM",
      email: "jennifer.martinez@email.com",
      phone: "+1-555-0130",
    },
  ],
  "2024-12-22": [
    {
      id: 9,
      name: "Alex Thompson",
      position: "Software Architect",
      education: "Computer Science, Stanford",
      linkedin: "https://linkedin.com/in/alexthompson",
      github: "https://github.com/alexthompson",
      time: "02:30 PM",
      email: "alex.thompson@email.com",
      phone: "+1-555-0131",
    },
  ],
  "2024-12-25": [
    {
      id: 10,
      name: "Maria Garcia",
      position: "QA Engineer",
      education: "Software Testing, UC San Diego",
      linkedin: "https://linkedin.com/in/mariagarcia",
      github: "https://github.com/mariagarcia",
      time: "09:00 AM",
      email: "maria.garcia@email.com",
      phone: "+1-555-0132",
    },
  ],
}

// Calendar state
let currentDate = new Date()
let selectedDate = new Date(2024, 11, 16) // December 16, 2024
const today = new Date()

// Month names
const monthNames = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
]

// Initialize the application
document.addEventListener("DOMContentLoaded", () => {
  generateCalendar()
  loadAppointments(formatDate(selectedDate))
  setupEventListeners()
  updateCalendarStatus()
  addTodayButton()
})

// Setup event listeners
function setupEventListeners() {
  // Calendar navigation
  document.getElementById("prevMonth").addEventListener("click", () => {
    currentDate.setMonth(currentDate.getMonth() - 1)
    generateCalendar()
  })

  document.getElementById("nextMonth").addEventListener("click", () => {
    currentDate.setMonth(currentDate.getMonth() + 1)
    generateCalendar()
  })

  // Calendar cell interactions
  const calendarCells = document.querySelectorAll(".calendar-cell")
  calendarCells.forEach((cell) => {
    cell.addEventListener("click", function () {
      const status = this.dataset.status
      showCalendarCellInfo(status)
    })
  })

  // Modal close
  const modal = document.getElementById("appointmentModal")
  const closeBtn = document.querySelector(".close")

  closeBtn.addEventListener("click", () => {
    modal.style.display = "none"
  })

  window.addEventListener("click", (event) => {
    if (event.target === modal) {
      modal.style.display = "none"
    }
  })

  // Keyboard navigation
  document.addEventListener("keydown", (event) => {
    if (event.key === "ArrowLeft") {
      // Navigate to previous day
      const newDate = new Date(selectedDate)
      newDate.setDate(newDate.getDate() - 1)
      selectDate(newDate)
    } else if (event.key === "ArrowRight") {
      // Navigate to next day
      const newDate = new Date(selectedDate)
      newDate.setDate(newDate.getDate() + 1)
      selectDate(newDate)
    } else if (event.key === "ArrowUp") {
      // Navigate to previous week
      const newDate = new Date(selectedDate)
      newDate.setDate(newDate.getDate() - 7)
      selectDate(newDate)
    } else if (event.key === "ArrowDown") {
      // Navigate to next week
      const newDate = new Date(selectedDate)
      newDate.setDate(newDate.getDate() + 7)
      selectDate(newDate)
    } else if (event.key === "Escape") {
      document.getElementById("appointmentModal").style.display = "none"
    }
  })
}

// Generate calendar
function generateCalendar() {
  const year = currentDate.getFullYear()
  const month = currentDate.getMonth()

  // Update month header
  document.getElementById("currentMonth").textContent = `${monthNames[month]} ${year}`

  // Get first day of month and number of days
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const daysInMonth = lastDay.getDate()
  const startingDayOfWeek = firstDay.getDay()

  // Get previous month's last days
  const prevMonth = new Date(year, month, 0)
  const daysInPrevMonth = prevMonth.getDate()

  const calendarDates = document.getElementById("calendarDates")
  calendarDates.innerHTML = ""

  // Add previous month's trailing days
  for (let i = startingDayOfWeek - 1; i >= 0; i--) {
    const day = daysInPrevMonth - i
    const dateElement = createDateElement(day, true, new Date(year, month - 1, day))
    calendarDates.appendChild(dateElement)
  }

  // Add current month's days
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    const dateElement = createDateElement(day, false, date)
    calendarDates.appendChild(dateElement)
  }

  // Add next month's leading days
  const totalCells = calendarDates.children.length
  const remainingCells = 42 - totalCells // 6 rows × 7 days
  for (let day = 1; day <= remainingCells; day++) {
    const dateElement = createDateElement(day, true, new Date(year, month + 1, day))
    calendarDates.appendChild(dateElement)
  }
}

// Create date element
function createDateElement(day, isOtherMonth, date) {
  const dateElement = document.createElement("div")
  dateElement.className = "calendar-date"
  dateElement.textContent = day

  if (isOtherMonth) {
    dateElement.classList.add("other-month")
  }

  // Check if it's today
  if (isSameDate(date, today)) {
    dateElement.classList.add("today")
  }

  // Check if it's selected
  if (isSameDate(date, selectedDate)) {
    dateElement.classList.add("selected")
  }

  // Check if it has appointments
  const dateKey = formatDate(date)
  if (appointmentsData[dateKey] && appointmentsData[dateKey].length > 0) {
    dateElement.classList.add("has-appointments")

    // Add appointment indicator
    const indicator = document.createElement("div")
    indicator.className = "appointment-indicator"
    indicator.textContent = appointmentsData[dateKey].length
    dateElement.appendChild(indicator)
  }

  // Add click event
  dateElement.addEventListener("click", () => {
    if (!isOtherMonth) {
      selectDate(date)
    } else {
      // Navigate to other month and select date
      currentDate = new Date(date)
      selectedDate = new Date(date)
      generateCalendar()
      loadAppointments(formatDate(selectedDate))
    }
  })

  return dateElement
}

// Select a date
function selectDate(date) {
  selectedDate = new Date(date)
  generateCalendar()
  loadAppointments(formatDate(selectedDate))
}

// Load appointments for selected date
function loadAppointments(dateKey) {
  const appointmentList = document.getElementById("appointmentList")
  const appointments = appointmentsData[dateKey] || []

  if (appointments.length === 0) {
    appointmentList.innerHTML = `
      <div style="text-align: center; color: #666; padding: 40px;">
        <h3>No appointments scheduled</h3>
        <p>Selected date: ${formatDateDisplay(selectedDate)}</p>
      </div>`
    return
  }

  appointmentList.innerHTML = appointments
    .map(
      (appointment) => `
        <div class="appointment-card">
            <div class="appointment-time">Appointment time: ${appointment.time}</div>
            <div class="profile-pic">Profile pic</div>
            <div class="appointment-info">
                <div class="employee-name">${appointment.name}</div>
                <div class="job-position">${appointment.position}</div>
                <div class="education">${appointment.education}</div>
                <div class="links">
                    <a href="${appointment.linkedin}" target="_blank">Link of LINKDIN</a>
                    <a href="${appointment.github}" target="_blank">Link of gitHub</a>
                </div>
            </div>
            <div class="appointment-actions">
                <button class="btn btn-view" onclick="viewProfile(${appointment.id})">View profile</button>
                <button class="btn btn-email" onclick="sendEmail('${appointment.email}')">Email</button>
                <button class="btn btn-call" onclick="makeCall('${appointment.phone}')">Call</button>
            </div>
        </div>
    `,
    )
    .join("")
}

// Utility functions
function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, "0")
  const day = String(date.getDate()).padStart(2, "0")
  return `${year}-${month}-${day}`
}

function formatDateDisplay(date) {
  const options = {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  }
  return date.toLocaleDateString("en-US", options)
}

function isSameDate(date1, date2) {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  )
}

// View profile function
function viewProfile(appointmentId) {
  const appointment = findAppointmentById(appointmentId)
  if (!appointment) return

  const modalContent = document.getElementById("modalContent")
  modalContent.innerHTML = `
        <h2>${appointment.name}</h2>
        <div style="margin: 20px 0;">
            <p><strong>Position:</strong> ${appointment.position}</p>
            <p><strong>Education:</strong> ${appointment.education}</p>
            <p><strong>Email:</strong> ${appointment.email}</p>
            <p><strong>Phone:</strong> ${appointment.phone}</p>
            <p><strong>Appointment Time:</strong> ${appointment.time}</p>
            <p><strong>Date:</strong> ${formatDateDisplay(selectedDate)}</p>
        </div>
        <div style="margin-top: 20px;">
            <a href="${appointment.linkedin}" target="_blank" style="margin-right: 15px; color: #0077b5;">LinkedIn Profile</a>
            <a href="${appointment.github}" target="_blank" style="color: #333;">GitHub Profile</a>
        </div>
    `

  document.getElementById("appointmentModal").style.display = "block"
}

// Send email function
function sendEmail(email) {
  window.location.href = `mailto:${email}?subject=Interview Follow-up&body=Dear candidate,%0D%0A%0D%0AThank you for your interest in our position.%0D%0A%0D%0ABest regards`
}

// Make call function
function makeCall(phone) {
  if (navigator.userAgent.match(/(iPhone|iPod|Android|BlackBerry|IEMobile)/)) {
    window.location.href = `tel:${phone}`
  } else {
    alert(`Call ${phone}\n\nNote: This feature works best on mobile devices.`)
  }
}

// Find appointment by ID
function findAppointmentById(id) {
  for (const date in appointmentsData) {
    const appointment = appointmentsData[date].find((app) => app.id === id)
    if (appointment) return appointment
  }
  return null
}

// Show calendar cell information
function showCalendarCellInfo(status) {
  const statusMessages = {
    occupied: "This time slot is occupied",
    busy: "This time slot is busy",
    meeting: "Meeting scheduled",
    available: "This time slot is available",
    empty: "No appointments scheduled",
  }

  const message = statusMessages[status] || "Unknown status"
  alert(message)
}

// Update calendar status based on appointments
function updateCalendarStatus() {
  const cells = document.querySelectorAll(".calendar-cell")

  // Add some random availability for demonstration
  const availableSlots = [8, 12, 15, 20]
  availableSlots.forEach((index) => {
    if (cells[index]) {
      cells[index].classList.add("available")
      cells[index].dataset.status = "available"
    }
  })
}

// Quick date navigation functions
function goToToday() {
  currentDate = new Date()
  selectedDate = new Date()
  generateCalendar()
  loadAppointments(formatDate(selectedDate))
}

// Add today button functionality
function addTodayButton() {
  const calendarHeader = document.querySelector(".calendar-header")
  const todayBtn = document.createElement("button")
  todayBtn.textContent = "Today"
  todayBtn.className = "btn btn-view"
  todayBtn.style.fontSize = "12px"
  todayBtn.style.padding = "5px 10px"
  todayBtn.onclick = goToToday
  calendarHeader.appendChild(todayBtn)
}
