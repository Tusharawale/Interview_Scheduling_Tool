// Programming languages data
const programmingLanguages = [
    { name: "JavaScript", color: "javascript", icon: "🌐", textColor: "black" },
    { name: "Python", color: "python", icon: "💻", textColor: "white" },
    { name: "Java", color: "java", icon: "⚙️", textColor: "white" },
    { name: "React", color: "react", icon: "🌐", textColor: "white" },
    { name: "Node.js", color: "nodejs", icon: "🖥️", textColor: "white" },
    { name: "SQL", color: "sql", icon: "🗄️", textColor: "white" },
];

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    generateQualificationCertificates();
    generateProgrammingCertificates();
});

// Generate qualification certificates
function generateQualificationCertificates() {
    const grid = document.getElementById('qualificationGrid');
    
    for (let i = 0; i < 9; i++) {
        const card = document.createElement('div');
        card.className = 'certificate-card';
        
        card.innerHTML = `
            <div class="certificate-info">
                <div>🎓 Institution</div>
                <div>📚 Course</div>
                <div>⏰ Duration</div>
                <div>🏆 Grade</div>
            </div>
            <button class="btn btn-certificate" onclick="viewCertificate(${i})">
                View Certificate
            </button>
        `;
        
        grid.appendChild(card);
    }
}

// Generate programming language certificates
function generateProgrammingCertificates() {
    const grid = document.getElementById('programmingGrid');
    
    programmingLanguages.forEach((lang, index) => {
        const card = document.createElement('div');
        card.className = `programming-card ${lang.color}`;
        
        card.innerHTML = `
            <div class="programming-content">
                <span class="programming-icon">${lang.icon}</span>
                <div class="programming-name">${lang.name}</div>
                <div class="programming-subtitle">Certificate</div>
            </div>
            <button class="btn btn-programming-view" onclick="viewProgrammingCertificate('${lang.name}')">
                VIEW
            </button>
        `;
        
        grid.appendChild(card);
    });
}

// Function to add a new certificate
function addCertificate() {
    alert('Add Certificate functionality would be implemented here');
    // This would typically open a modal or form to add a new certificate
}

// Function to add a new skill
function addSkill() {
    alert('Add Skill functionality would be implemented here');
    // This would typically open a modal or form to add a new skill
}

// Function to view a qualification certificate
function viewCertificate(index) {
    alert(`Viewing qualification certificate ${index + 1}`);
    // This would typically open a modal or new page to view the certificate
}

// Function to view a programming certificate
function viewProgrammingCertificate(language) {
    alert(`Viewing ${language} certificate`);
    // This would typically open a modal or new page to view the certificate
}

// Form validation (basic example)
function validateForm() {
    const requiredFields = document.querySelectorAll('input[required]');
    let isValid = true;
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.style.borderColor = '#ef4444';
            isValid = false;
        } else {
            field.style.borderColor = '';
        }
    });
    
    return isValid;
}

// Add form submission handler
document.addEventListener('DOMContentLoaded', function() {
    // Add event listeners for form inputs to clear error states
    const inputs = document.querySelectorAll('input, select');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            this.style.borderColor = '';
        });
    });
});

// Smooth scrolling for better UX
function smoothScrollTo(element) {
    element.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
    });
}

// Add hover effects for cards
document.addEventListener('DOMContentLoaded', function() {
    const cards = document.querySelectorAll('.programming-card');
    
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.05)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    });
});