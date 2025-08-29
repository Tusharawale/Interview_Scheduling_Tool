// Sample data for profiles
const profilesData = [
    {
        id: 1,
        name: "John Doe",
        status: "Active",
        post: "Manager",
        branch: "Mumbai",
        gender: "Male",
        education: "MBA",
        state: "Maharashtra",
        city: "Mumbai",
    },
    {
        id: 2,
        name: "Jane Smith",
        status: "Active",
        post: "Developer",
        branch: "Delhi",
        gender: "Female",
        education: "B.Tech",
        state: "Delhi",
        city: "New Delhi",
    },
    {
        id: 3,
        name: "Mike Johnson",
        status: "Inactive",
        post: "Designer",
        branch: "Bangalore",
        gender: "Male",
        education: "B.Des",
        state: "Karnataka",
        city: "Bangalore",
    },
    {
        id: 4,
        name: "Sarah Wilson",
        status: "Active",
        post: "Analyst",
        branch: "Chennai",
        gender: "Female",
        education: "MBA",
        state: "Tamil Nadu",
        city: "Chennai",
    },
    {
        id: 5,
        name: "David Brown",
        status: "Active",
        post: "Lead",
        branch: "Mumbai",
        gender: "Male",
        education: "M.Tech",
        state: "Maharashtra",
        city: "Mumbai",
    },
    {
        id: 6,
        name: "Lisa Davis",
        status: "Inactive",
        post: "Consultant",
        branch: "Pune",
        gender: "Female",
        education: "MBA",
        state: "Maharashtra",
        city: "Pune",
    },
    {
        id: 7,
        name: "Tom Anderson",
        status: "Active",
        post: "Developer",
        branch: "Hyderabad",
        gender: "Male",
        education: "B.Tech",
        state: "Telangana",
        city: "Hyderabad",
    },
    {
        id: 8,
        name: "Emma Taylor",
        status: "Active",
        post: "Manager",
        branch: "Kolkata",
        gender: "Female",
        education: "MBA",
        state: "West Bengal",
        city: "Kolkata",
    },
    {
        id: 9,
        name: "Chris Lee",
        status: "Inactive",
        post: "Designer",
        branch: "Bangalore",
        gender: "Male",
        education: "B.Des",
        state: "Karnataka",
        city: "Bangalore",
    },
    {
        id: 10,
        name: "Amy Chen",
        status: "Active",
        post: "Analyst",
        branch: "Delhi",
        gender: "Female",
        education: "B.Tech",
        state: "Delhi",
        city: "New Delhi",
    },
    {
        id: 11,
        name: "Robert Kim",
        status: "Active",
        post: "Lead",
        branch: "Chennai",
        gender: "Male",
        education: "M.Tech",
        state: "Tamil Nadu",
        city: "Chennai",
    },
    {
        id: 12,
        name: "Maria Garcia",
        status: "Inactive",
        post: "Consultant",
        branch: "Mumbai",
        gender: "Female",
        education: "MBA",
        state: "Maharashtra",
        city: "Mumbai",
    },
];

// Global variables
let filteredProfiles = [...profilesData];
let selectedProfiles = [];
let currentFilters = {
    branch: [],
    gender: [],
    education: [],
    state: [],
    city: [],
    post: []
};

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    renderProfiles();
    setupEventListeners();
    updateResultsCount();
}

// Setup all event listeners
function setupEventListeners() {
    // Dropdown toggle functionality
    document.querySelectorAll('.dropdown-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const filterType = this.getAttribute('data-filter');
            toggleDropdown(filterType);
        });
    });

    // Close dropdowns when clicking outside
    document.addEventListener('click', function() {
        closeAllDropdowns();
    });

    // Prevent dropdown from closing when clicking inside
    document.querySelectorAll('.dropdown-content').forEach(dropdown => {
        dropdown.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    });

    // Filter checkbox change events
    document.querySelectorAll('.dropdown-content input[type="checkbox"]').forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const filterType = this.closest('.dropdown-content').id.replace('-dropdown', '');
            const value = this.value;
            const isChecked = this.checked;
            
            updateFilter(filterType, value, isChecked);
        });
    });

    // Apply filters button
    document.getElementById('apply-btn').addEventListener('click', applyFilters);

    // Clear filters button
    document.getElementById('clear-btn').addEventListener('click', clearAllFilters);

    // Select all toggle
    document.getElementById('select-all-toggle').addEventListener('change', function() {
        handleSelectAll(this.checked);
    });
}

// Dropdown functionality
function toggleDropdown(filterType) {
    const dropdown = document.getElementById(filterType + '-dropdown');
    const isCurrentlyOpen = dropdown.classList.contains('show');
    
    // Close all dropdowns first
    closeAllDropdowns();
    
    // Open the clicked dropdown if it wasn't already open
    if (!isCurrentlyOpen) {
        dropdown.classList.add('show');
    }
}

function closeAllDropdowns() {
    document.querySelectorAll('.dropdown-content').forEach(dropdown => {
        dropdown.classList.remove('show');
    });
}

// Filter management
function updateFilter(filterType, value, isChecked) {
    if (isChecked) {
        if (!currentFilters[filterType].includes(value)) {
            currentFilters[filterType].push(value);
        }
    } else {
        currentFilters[filterType] = currentFilters[filterType].filter(item => item !== value);
    }
}

function applyFilters() {
    filteredProfiles = profilesData.filter(profile => {
        return Object.keys(currentFilters).every(filterType => {
            const filterValues = currentFilters[filterType];
            if (filterValues.length === 0) return true;
            return filterValues.includes(profile[filterType]);
        });
    });
    
    selectedProfiles = [];
    document.getElementById('select-all-toggle').checked = false;
    
    renderProfiles();
    updateResultsCount();
    closeAllDropdowns();
}

function clearAllFilters() {
    // Reset filters
    currentFilters = {
        branch: [],
        gender: [],
        education: [],
        state: [],
        city: [],
        post: []
    };
    
    // Uncheck all checkboxes
    document.querySelectorAll('.dropdown-content input[type="checkbox"]').forEach(checkbox => {
        checkbox.checked = false;
    });
    
    // Reset data
    filteredProfiles = [...profilesData];
    selectedProfiles = [];
    document.getElementById('select-all-toggle').checked = false;
    
    renderProfiles();
    updateResultsCount();
    closeAllDropdowns();
}

// Profile rendering
function renderProfiles() {
    const grid = document.getElementById('profiles-grid');
    const noResults = document.getElementById('no-results');
    
    if (filteredProfiles.length === 0) {
        grid.style.display = 'none';
        noResults.style.display = 'block';
        return;
    }
    
    grid.style.display = 'grid';
    noResults.style.display = 'none';
    
    grid.innerHTML = filteredProfiles.map(profile => `
        <div class="profile-card" data-profile-id="${profile.id}">
            <div class="selection-indicator ${selectedProfiles.includes(profile.id) ? 'selected' : ''}" 
                 onclick="toggleProfileSelection(${profile.id})">
            </div>
            
            <div class="profile-image">
                <i class="fas fa-user"></i>
            </div>
            
            <div class="profile-info">
                <h3>${profile.name}</h3>
                <p>${profile.status}</p>
                <p>${profile.post}</p>
            </div>
            
            <button class="view-profile-btn" onclick="viewProfile(${profile.id})">
                View profile
            </button>
        </div>
    `).join('');
}

// Selection functionality
function toggleProfileSelection(profileId) {
    if (selectedProfiles.includes(profileId)) {
        selectedProfiles = selectedProfiles.filter(id => id !== profileId);
    } else {
        selectedProfiles.push(profileId);
    }
    
    updateSelectionUI();
    updateResultsCount();
    updateSelectAllToggle();
}

function handleSelectAll(isChecked) {
    if (isChecked) {
        selectedProfiles = filteredProfiles.map(profile => profile.id);
    } else {
        selectedProfiles = [];
    }
    
    updateSelectionUI();
    updateResultsCount();
}

function updateSelectionUI() {
    document.querySelectorAll('.selection-indicator').forEach(indicator => {
        const profileId = parseInt(indicator.closest('.profile-card').getAttribute('data-profile-id'));
        if (selectedProfiles.includes(profileId)) {
            indicator.classList.add('selected');
        } else {
            indicator.classList.remove('selected');
        }
    });
}

function updateSelectAllToggle() {
    const selectAllToggle = document.getElementById('select-all-toggle');
    selectAllToggle.checked = filteredProfiles.length > 0 && selectedProfiles.length === filteredProfiles.length;
}

// Results count update
function updateResultsCount() {
    const resultsCount = document.getElementById('results-count');
    const selectedText = selectedProfiles.length > 0 ? ` (${selectedProfiles.length} selected)` : '';
    resultsCount.textContent = `Showing ${filteredProfiles.length} of ${profilesData.length} profiles${selectedText}`;
}

// Profile actions
function viewProfile(profileId) {
    const profile = profilesData.find(p => p.id === profileId);
    if (profile) {
        alert(`Viewing profile for ${profile.name}\n\nDetails:\nStatus: ${profile.status}\nPost: ${profile.post}\nBranch: ${profile.branch}\nEducation: ${profile.education}\nState: ${profile.state}\nCity: ${profile.city}`);
    }
}

// Utility functions
function getSelectedProfilesData() {
    return profilesData.filter(profile => selectedProfiles.includes(profile.id));
}

function exportSelectedProfiles() {
    const selected = getSelectedProfilesData();
    if (selected.length === 0) {
        alert('No profiles selected for export.');
        return;
    }
    
    console.log('Exporting profiles:', selected);
    alert(`Exporting ${selected.length} selected profiles...`);
}

// Additional utility functions for future enhancements
function searchProfiles(searchTerm) {
    const filtered = profilesData.filter(profile => 
        profile.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        profile.post.toLowerCase().includes(searchTerm.toLowerCase()) ||
        profile.branch.toLowerCase().includes(searchTerm.toLowerCase())
    );
    
    filteredProfiles = filtered;
    renderProfiles();
    updateResultsCount();
}

function sortProfiles(sortBy, order = 'asc') {
    filteredProfiles.sort((a, b) => {
        let aValue = a[sortBy];
        let bValue = b[sortBy];
        
        if (typeof aValue === 'string') {
            aValue = aValue.toLowerCase();
            bValue = bValue.toLowerCase();
        }
        
        if (order === 'asc') {
            return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        } else {
            return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
        }
    });
    
    renderProfiles();
}
