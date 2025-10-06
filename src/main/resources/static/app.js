// Global variables
let currentUser = null;
let userAccounts = [];
let userTransactions = [];

// API Base URL
const API_BASE = 'http://localhost:8080/api';

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
});

function initializeApp() {
    // Check if user is already logged in (from localStorage)
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
        currentUser = JSON.parse(savedUser);
        showDashboard();
        loadUserData();
    } else {
        showSection('home');
    }
}

function setupEventListeners() {
    // Login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Register form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // Transfer form
    const transferForm = document.getElementById('transferForm');
    if (transferForm) {
        transferForm.addEventListener('submit', handleTransfer);
    }
}

// Navigation Functions
function showSection(sectionId) {
    // Hide all sections
    const sections = document.querySelectorAll('.section');
    sections.forEach(section => {
        section.classList.remove('active');
    });

    // Show selected section
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
    }

    // If showing dashboard, load user data
    if (sectionId === 'dashboard' && currentUser) {
        loadUserData();
    }
}

// Authentication Functions
async function handleLogin(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const loginData = {
        email: formData.get('email'),
        password: formData.get('password')
    };

    try {
        showLoading('Logging in...');
        
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(loginData)
        });

        const data = await response.json();

        if (response.ok) {
            currentUser = data.user;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            showNotification('Login successful!', 'success');
            showDashboard();
            await loadUserData();
        } else {
            throw new Error(data.error || 'Login failed');
        }
    } catch (error) {
        console.error('Login error:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');

    // Password validation
    if (password !== confirmPassword) {
        showNotification('Passwords do not match', 'error');
        return;
    }

    if (password.length < 6) {
        showNotification('Password must be at least 6 characters long', 'error');
        return;
    }

    const registerData = {
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        email: formData.get('email'),
        password: password,
        phone: formData.get('phone')
    };

    try {
        showLoading('Creating account...');
        
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(registerData)
        });

        const data = await response.json();

        if (response.ok) {
            showNotification('Account created successfully! Please login.', 'success');
            showSection('login');
            event.target.reset();
        } else {
            throw new Error(data.error || 'Registration failed');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

function logout() {
    currentUser = null;
    userAccounts = [];
    userTransactions = [];
    localStorage.removeItem('currentUser');
    showNotification('Logged out successfully', 'success');
    showSection('home');
}

// Dashboard Functions
function showDashboard() {
    document.getElementById('userName').textContent = 
        `${currentUser.firstName} ${currentUser.lastName}`;
    showSection('dashboard');
}

async function loadUserData() {
    if (!currentUser) return;

    try {
        showLoading('Loading your data...');
        
        // Load user accounts
        const accountsResponse = await fetch(`${API_BASE}/accounts/user/${currentUser.id}`);
        if (accountsResponse.ok) {
            userAccounts = await accountsResponse.json();
            displayAccounts();
            populateFromAccountDropdown();
        }

        // If user has accounts, load transactions for the first account
        if (userAccounts.length > 0) {
            await loadTransactions(userAccounts[0].id);
        }

    } catch (error) {
        console.error('Error loading user data:', error);
        showNotification('Error loading data', 'error');
    } finally {
        hideLoading();
    }
}

function displayAccounts() {
    const accountsList = document.getElementById('accountsList');
    if (!accountsList) return;

    if (userAccounts.length === 0) {
        accountsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-wallet"></i>
                <p>No accounts found</p>
                <button class="btn btn-primary" onclick="createAccount()">
                    Create Your First Account
                </button>
            </div>
        `;
        return;
    }

    accountsList.innerHTML = userAccounts.map(account => `
        <div class="account-item">
            <div class="account-info">
                <div class="account-type">${account.accountType}</div>
                <div class="account-number">${account.accountNumber}</div>
            </div>
            <div class="account-balance">
                $${formatCurrency(account.balance)}
            </div>
            <button class="btn btn-sm btn-outline" onclick="loadTransactions(${account.id})">
                View Transactions
            </button>
        </div>
    `).join('');
}

function populateFromAccountDropdown() {
    const fromAccountSelect = document.getElementById('fromAccount');
    if (!fromAccountSelect) return;

    fromAccountSelect.innerHTML = '<option value="">Select account</option>' +
        userAccounts.map(account => `
            <option value="${account.id}">
                ${account.accountNumber} ($${formatCurrency(account.balance)})
            </option>
        `).join('');
}

async function loadTransactions(accountId) {
    try {
        const response = await fetch(`${API_BASE}/transactions/account/${accountId}`);
        if (response.ok) {
            userTransactions = await response.json();
            displayTransactions();
        }
    } catch (error) {
        console.error('Error loading transactions:', error);
        showNotification('Error loading transactions', 'error');
    }
}

function displayTransactions() {
    const transactionsList = document.getElementById('transactionsList');
    if (!transactionsList) return;

    if (userTransactions.length === 0) {
        transactionsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-receipt"></i>
                <p>No transactions yet</p>
            </div>
        `;
        return;
    }

    transactionsList.innerHTML = userTransactions.map(transaction => `
        <div class="transaction-item">
            <div class="transaction-info">
                <div class="transaction-type">${transaction.transactionType}</div>
                <div class="transaction-description">${transaction.description || 'No description'}</div>
                <div class="transaction-date">${formatDate(transaction.transactionDate)}</div>
            </div>
            <div class="transaction-amount ${getTransactionAmountClass(transaction)}">
                ${formatTransactionAmount(transaction)}
            </div>
            <div class="transaction-status ${transaction.status.toLowerCase()}">
                ${transaction.status}
            </div>
        </div>
    `).join('');
}

// Transaction Functions
async function handleTransfer(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const transferData = {
        fromAccountId: parseInt(formData.get('fromAccountId')),
        toAccountId: formData.get('toAccountId'), // This might be account number, need to handle
        amount: parseFloat(formData.get('amount')),
        description: formData.get('description') || 'Fund Transfer'
    };

    // Validation
    const fromAccount = userAccounts.find(acc => acc.id === transferData.fromAccountId);
    if (!fromAccount) {
        showNotification('Please select a valid from account', 'error');
        return;
    }

    if (fromAccount.balance < transferData.amount) {
        showNotification('Insufficient balance', 'error');
        return;
    }

    if (transferData.amount <= 0) {
        showNotification('Amount must be greater than zero', 'error');
        return;
    }

    try {
        showLoading('Processing transfer...');
        
        // First, we need to get the account ID from the account number
        // For now, we'll assume toAccountId is the account ID
        // In a real application, you'd look up the account by number first
        
        const response = await fetch(`${API_BASE}/transactions/transfer`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(transferData)
        });

        const data = await response.json();

        if (response.ok) {
            showNotification('Transfer completed successfully!', 'success');
            event.target.reset();
            await loadUserData(); // Refresh data
        } else {
            throw new Error(typeof data === 'string' ? data : data.error || 'Transfer failed');
        }
    } catch (error) {
        console.error('Transfer error:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

async function createAccount() {
    if (!currentUser) return;

    try {
        showLoading('Creating account...');
        
        const response = await fetch(`${API_BASE}/accounts/create?userId=${currentUser.id}`, {
            method: 'POST'
        });

        if (response.ok) {
            const account = await response.json();
            showNotification(`Account ${account.accountNumber} created successfully!`, 'success');
            await loadUserData();
        } else {
            throw new Error('Failed to create account');
        }
    } catch (error) {
        console.error('Error creating account:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

// Utility Functions
function formatCurrency(amount) {
    return parseFloat(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
}

function formatTransactionAmount(transaction) {
    const amount = parseFloat(transaction.amount);
    const sign = transaction.fromAccount && 
                 transaction.fromAccount.id === getCurrentAccountId() ? '-' : '+';
    return `${sign}$${formatCurrency(amount)}`;
}

function getTransactionAmountClass(transaction) {
    if (transaction.fromAccount && 
        transaction.fromAccount.id === getCurrentAccountId()) {
        return 'negative';
    }
    return 'positive';
}

function getCurrentAccountId() {
    // For simplicity, return the first account ID
    return userAccounts.length > 0 ? userAccounts[0].id : null;
}

function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const icon = input.parentNode.querySelector('.password-toggle i');
    
    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}

// Notification System
function showNotification(message, type = 'info') {
    const notification = document.getElementById('notification');
    const messageElement = document.getElementById('notificationMessage');
    
    if (!notification || !messageElement) return;
    
    // Remove existing classes
    notification.className = 'notification';
    
    // Add type class and show
    notification.classList.add(type);
    notification.classList.remove('hidden');
    
    messageElement.textContent = message;
    
    // Auto hide after 5 seconds
    setTimeout(() => {
        hideNotification();
    }, 5000);
}

function hideNotification() {
    const notification = document.getElementById('notification');
    if (notification) {
        notification.classList.add('hidden');
    }
}

// Loading State
function showLoading(message = 'Loading...') {
    // Create loading overlay if it doesn't exist
    let loadingOverlay = document.getElementById('loadingOverlay');
    if (!loadingOverlay) {
        loadingOverlay = document.createElement('div');
        loadingOverlay.id = 'loadingOverlay';
        loadingOverlay.className = 'loading-overlay';
        loadingOverlay.innerHTML = `
            <div class="loading-spinner">
                <i class="fas fa-spinner fa-spin"></i>
                <p>${message}</p>
            </div>
        `;
        document.body.appendChild(loadingOverlay);
    } else {
        loadingOverlay.querySelector('p').textContent = message;
        loadingOverlay.style.display = 'flex';
    }
}

function hideLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'none';
    }
}

// Error handling for fetch requests
function handleApiError(error) {
    console.error('API Error:', error);
    if (error instanceof TypeError && error.message.includes('fetch')) {
        showNotification('Network error: Please check if the server is running', 'error');
    } else {
        showNotification('An unexpected error occurred', 'error');
    }
}

// Add global error handler for unhandled promise rejections
window.addEventListener('unhandledrejection', function(event) {
    console.error('Unhandled promise rejection:', event.reason);
    handleApiError(event.reason);
});

// Add CSS for loading overlay and additional styles
const additionalStyles = `
.loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    color: white;
}

.loading-spinner {
    text-align: center;
}

.loading-spinner i {
    font-size: 2rem;
    margin-bottom: 1rem;
}

.account-item {
    display: flex;
    justify-content: between;
    align-items: center;
    padding: 1rem;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    margin-bottom: 0.5rem;
    background: var(--surface-color);
}

.transaction-item {
    display: flex;
    justify-content: between;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid var(--border-color);
}

.transaction-amount.positive {
    color: var(--success-color);
    font-weight: bold;
}

.transaction-amount.negative {
    color: var(--error-color);
    font-weight: bold;
}

.empty-state {
    text-align: center;
    padding: 2rem;
    color: var(--text-secondary);
}

.empty-state i {
    font-size: 3rem;
    margin-bottom: 1rem;
    color: var(--border-color);
}

.btn-sm {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
}

.btn-full {
    width: 100%;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

@media (max-width: 768px) {
    .form-row {
        grid-template-columns: 1fr;
    }
    
    .account-item, .transaction-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }
}
`;

// Inject additional styles
const styleSheet = document.createElement('style');
styleSheet.textContent = additionalStyles;
document.head.appendChild(styleSheet);