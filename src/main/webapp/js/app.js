// API Base URL
const API_URL = '/employee-dbms-project03/api/employees';

// DOM Elements
const employeeForm = document.getElementById('employeeForm');
const employeeTableBody = document.getElementById('employeeTableBody');
const formTitle = document.getElementById('formTitle');
const submitBtn = document.getElementById('submitBtn');
const cancelBtn = document.getElementById('cancelBtn');
const employeeIdInput = document.getElementById('employeeId');

// State
let isEditMode = false;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    console.log('=================================');
    console.log('App loaded. API URL:', API_URL);
    console.log('=================================');
    loadEmployees();
    setupEventListeners();
});

// Event Listeners
function setupEventListeners() {
    employeeForm.addEventListener('submit', handleSubmit);
    cancelBtn.addEventListener('click', resetForm);
}

// Load all employees
async function loadEmployees() {
    try {
        console.log('Fetching employees from:', API_URL);
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const employees = await response.json();
        console.log('Employees loaded:', employees.length);
        displayEmployees(employees);
    } catch (error) {
        console.error('Error loading employees:', error);
        employeeTableBody.innerHTML = '<tr><td colspan="9" class="empty-state">Error loading employees. Check console for details.</td></tr>';
    }
}

// Display employees in table
function displayEmployees(employees) {
    if (employees.length === 0) {
        employeeTableBody.innerHTML = '<tr><td colspan="9" class="empty-state">No employees found. Add one to get started!</td></tr>';
        return;
    }

    employeeTableBody.innerHTML = employees.map(employee => `
        <tr>
            <td>${employee.id}</td>
            <td>${employee.name}</td>
            <td>${employee.department}</td>
            <td>${employee.position}</td>
            <td>₹${parseFloat(employee.salary).toLocaleString('en-IN')}</td>
            <td>${employee.email}</td>
            <td>${employee.phone}</td>
            <td>${formatDate(employee.hireDate)}</td>
            <td>
                <button class="btn btn-edit" onclick="editEmployee(${employee.id})">Edit</button>
                <button class="btn btn-delete" onclick="deleteEmployee(${employee.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

// Handle form submission
async function handleSubmit(e) {
    e.preventDefault();

    console.log('=================================');
    console.log('FORM SUBMITTED');
    console.log('=================================');
    console.log('Edit mode:', isEditMode);
    console.log('Employee ID field value:', employeeIdInput.value);

    const employeeData = {
        name: document.getElementById('name').value,
        department: document.getElementById('department').value,
        position: document.getElementById('position').value,
        salary: document.getElementById('salary').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        hireDate: document.getElementById('hireDate').value
    };

    // If editing, add ID
    if (isEditMode && employeeIdInput.value) {
        employeeData.id = employeeIdInput.value;
        console.log('>>> UPDATE MODE - ID:', employeeData.id);
    } else {
        console.log('>>> ADD MODE - No ID');
    }

    console.log('Data to send:', employeeData);

    try {
        const formData = new URLSearchParams(employeeData);
        console.log('FormData string:', formData.toString());

        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData
        });

        console.log('Response status:', response.status);

        const responseText = await response.text();
        console.log('Response text:', responseText);

        if (!response.ok) {
            throw new Error(`Server error: ${response.status} - ${responseText}`);
        }

        const result = JSON.parse(responseText);
        console.log('Result:', result);

        if (result.success) {
            alert(result.message);
            resetForm();
            loadEmployees();
        } else {
            alert('Error: ' + result.message);
        }

    } catch (error) {
        console.error('Submit error:', error);
        alert('Error: ' + error.message);
    }

    console.log('=================================');
}

// Edit employee
async function editEmployee(id) {
    try {
        console.log('=================================');
        console.log('LOADING EMPLOYEE FOR EDIT');
        console.log('Employee ID:', id);
        console.log('=================================');

        const response = await fetch(`${API_URL}/${id}`);

        if (!response.ok) {
            throw new Error(`Failed to load employee: ${response.status}`);
        }

        const employee = await response.json();
        console.log('Employee loaded:', employee);

        // Fill form with employee data
        document.getElementById('name').value = employee.name || '';
        document.getElementById('department').value = employee.department || '';
        document.getElementById('position').value = employee.position || '';
        document.getElementById('salary').value = employee.salary || '';
        document.getElementById('email').value = employee.email || '';
        document.getElementById('phone').value = employee.phone || '';
        document.getElementById('hireDate').value = employee.hireDate || '';
        employeeIdInput.value = employee.id;

        console.log('Form filled. Hidden ID field value:', employeeIdInput.value);

        // Switch to edit mode
        isEditMode = true;
        formTitle.textContent = 'Edit Employee';
        submitBtn.textContent = 'Update Employee';
        cancelBtn.style.display = 'inline-block';

        // Scroll to form
        document.querySelector('.form-section').scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });

        console.log('Edit mode activated');
        console.log('=================================');

    } catch (error) {
        console.error('Error loading employee:', error);
        alert('Error loading employee data: ' + error.message);
    }
}

// Delete employee
async function deleteEmployee(id) {
    if (!confirm('Are you sure you want to delete this employee?')) {
        return;
    }

    try {
        console.log('Deleting employee ID:', id);
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Failed to delete employee');
        }

        alert('Employee deleted successfully!');
        loadEmployees();
    } catch (error) {
        console.error('Error deleting employee:', error);
        alert('Error deleting employee: ' + error.message);
    }
}

// Reset form
function resetForm() {
    console.log('Resetting form...');
    employeeForm.reset();
    employeeIdInput.value = '';
    isEditMode = false;
    formTitle.textContent = 'Add New Employee';
    submitBtn.textContent = 'Add Employee';
    cancelBtn.style.display = 'none';
    console.log('Form reset complete');
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// Export to Excel - NEW FUNCTION
function exportToExcel() {
    console.log('Exporting to Excel...');
    // Change this if your project name is different
    window.location.href = '/employee-dbms-project03/export/excel';
}

// Export to PDF - NEW FUNCTION
function exportToPDF() {
    console.log('Exporting to PDF...');
    // Change this if your project name is different
    window.location.href = '/employee-dbms-project03/export/pdf';
}
