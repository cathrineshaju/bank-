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

    // Update navigation
    const dashboardLink = document.getElementById('dashboardLink');
    if (dashboardLink) {
        if (sectionId === 'dashboard' && currentUser) {
            dashboardLink.style.display = 'block';
        } else {
            dashboardLink.style.display = 'none';
        }
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
            console.log('Accounts loaded:', userAccounts);
            displayAccounts();
            populateFromAccountDropdown(); // This is the key function
            
            // If user has accounts, load transactions for the first account
            if (userAccounts.length > 0) {
                await loadTransactions(userAccounts[0].id);
            } else {
                displayTransactions(); // Show empty state
            }
        } else {
            throw new Error('Failed to load accounts');
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
                    <i class="fas fa-plus"></i> Create Account with $5000
                </button>
            </div>
        `;
        return;
    }

    accountsList.innerHTML = userAccounts.map(account => `
        <div class="account-item">
            <div class="account-info">
                <div class="account-type">${account.accountType} • ${account.accountNumber}</div>
                <div class="account-balance">$${formatCurrency(account.balance)}</div>
            </div>
            <div class="account-actions">
                <button class="btn btn-sm btn-success" onclick="addDemoMoney(${account.id}, 5000)" title="Add $5000">
                    <i class="fas fa-coins"></i> Add $5K
                </button>
                <button class="btn btn-sm btn-outline" onclick="loadTransactions(${account.id})">
                    <i class="fas fa-history"></i> History
                </button>
            </div>
        </div>
    `).join('');

    // Update quick stats
    updateQuickStats();
}

// FIXED: Populate the From Account dropdown - This is the main fix
function populateFromAccountDropdown() {
    console.log('populateFromAccountDropdown called with accounts:', userAccounts);
    
    const fromAccountSelect = document.getElementById('fromAccount');
    if (!fromAccountSelect) {
        console.error('From Account select element not found!');
        return;
    }

    // Clear existing options
    fromAccountSelect.innerHTML = '<option value="">Select account</option>';
    
    if (userAccounts.length === 0) {
        const option = document.createElement('option');
        option.value = "";
        option.textContent = "No accounts available";
        option.disabled = true;
        fromAccountSelect.appendChild(option);
        return;
    }

    // Add user's accounts to dropdown
    userAccounts.forEach(account => {
        const option = document.createElement('option');
        option.value = account.id;
        option.textContent = `${account.accountNumber} - $${formatCurrency(account.balance)} (${account.accountType})`;
        fromAccountSelect.appendChild(option);
    });

    console.log('Dropdown populated with', userAccounts.length, 'accounts');
}

// Populate transaction account filter
function populateTransactionFilter() {
    const filterSelect = document.getElementById('transactionAccountFilter');
    if (!filterSelect) return;

    filterSelect.innerHTML = '<option value="all">All Accounts</option>';
    
    userAccounts.forEach(account => {
        const option = document.createElement('option');
        option.value = account.id;
        option.textContent = `${account.accountNumber} (${account.accountType})`;
        filterSelect.appendChild(option);
    });
}

// Filter transactions by account
function filterTransactions() {
    const selectedAccountId = document.getElementById('transactionAccountFilter').value;
    
    if (selectedAccountId === 'all') {
        displayTransactions();
    } else {
        const filteredTransactions = userTransactions.filter(transaction => 
            (transaction.fromAccount && transaction.fromAccount.id == selectedAccountId) ||
            (transaction.toAccount && transaction.toAccount.id == selectedAccountId)
        );
        displayFilteredTransactions(filteredTransactions);
    }
}

// Display filtered transactions
function displayFilteredTransactions(transactions) {
    const transactionsList = document.getElementById('transactionsList');
    if (!transactionsList) return;

    if (transactions.length === 0) {
        transactionsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-receipt"></i>
                <p>No transactions found for this account</p>
            </div>
        `;
        return;
    }

    transactionsList.innerHTML = transactions.map(transaction => `
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

// Account Management Functions
async function createAccount() {
    if (!currentUser) return;

    try {
        showLoading('Creating account with $5000 demo money...');
        
        const response = await fetch(`${API_BASE}/accounts/create?userId=${currentUser.id}`, {
            method: 'POST'
        });

        if (response.ok) {
            const account = await response.json();
            showNotification(`Account ${account.accountNumber} created with $5000!`, 'success');
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

// Add demo money to all accounts
async function addDemoMoneyToAllAccounts() {
    if (!currentUser) return;

    try {
        showLoading('Adding $5000 to all your accounts...');
        
        const response = await fetch(`${API_BASE}/accounts/user/${currentUser.id}/add-demo-money`, {
            method: 'POST'
        });

        if (response.ok) {
            showNotification('Successfully added $5000 to all your accounts!', 'success');
            await loadUserData();
        } else {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to add demo money');
        }
    } catch (error) {
        console.error('Error adding demo money:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

// Add demo money to specific account
async function addDemoMoney(accountId, amount = 5000) {
    try {
        showLoading(`Adding $${amount} demo money...`);
        
        const response = await fetch(`${API_BASE}/accounts/${accountId}/deposit?amount=${amount}`, {
            method: 'POST'
        });

        if (response.ok) {
            showNotification(`Successfully added $${amount} demo money!`, 'success');
            await loadUserData();
        } else {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to add demo money');
        }
    } catch (error) {
        console.error('Error adding demo money:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

// Update quick stats
function updateQuickStats() {
    const totalBalance = userAccounts.reduce((sum, account) => sum + parseFloat(account.balance), 0);
    const totalAccounts = userAccounts.length;
    const totalTransactions = userTransactions.length;

    document.getElementById('totalBalance').textContent = `$${formatCurrency(totalBalance)}`;
    document.getElementById('totalAccounts').textContent = totalAccounts;
    document.getElementById('totalTransactions').textContent = totalTransactions;
}

// Transaction Functions
async function handleTransfer(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const fromAccountId = formData.get('fromAccountId');
    const toAccountNumber = formData.get('toAccountId');
    const amount = parseFloat(formData.get('amount'));
    const description = formData.get('description') || 'Fund Transfer';

    // Validation
    if (!fromAccountId) {
        showNotification('Please select a from account', 'error');
        return;
    }

    if (!toAccountNumber) {
        showNotification('Please enter a destination account number', 'error');
        return;
    }

    if (!amount || amount <= 0) {
        showNotification('Amount must be greater than zero', 'error');
        return;
    }

    // Find the from account to check balance
    const fromAccount = userAccounts.find(acc => acc.id == fromAccountId);
    if (!fromAccount) {
        showNotification('Invalid from account', 'error');
        return;
    }

    if (fromAccount.balance < amount) {
        showNotification('Insufficient balance', 'error');
        return;
    }

    try {
        showLoading('Processing transfer...');
        
        const transferData = {
            fromAccountId: parseInt(fromAccountId),
            toAccountId: toAccountNumber, // This should be the account ID
            amount: amount,
            description: description
        };

        const response = await fetch(`${API_BASE}/transactions/transfer`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(transferData)
        });

        const data = await response.json();

        if (response.ok) {
            showNotification(`Transfer of $${amount} completed successfully!`, 'success');
            event.target.reset();
            await loadUserData(); // Refresh all data
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

// Debug function to check accounts
function debugAccounts() {
    console.log('Current User:', currentUser);
    console.log('User Accounts:', userAccounts);
    console.log('From Account Select:', document.getElementById('fromAccount'));
}

// Manual refresh function
async function refreshAccounts() {
    console.log('Manual refresh triggered');
    await loadUserData();
}

// Emergency fix - manually populate dropdown after page load
setTimeout(() => {
    if (currentUser && userAccounts.length > 0) {
        console.log('Emergency fix: Manually populating dropdown');
        populateFromAccountDropdown();
    }
}, 2000);
// Setup event listeners for new forms
function setupEventListeners() {
    // Existing event listeners...
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const transferForm = document.getElementById('transferForm');
    
    if (loginForm) loginForm.addEventListener('submit', handleLogin);
    if (registerForm) registerForm.addEventListener('submit', handleRegister);
    if (transferForm) transferForm.addEventListener('submit', handleTransfer);
    
    // NEW: Add event listeners for withdraw and internal transfer
    const withdrawForm = document.getElementById('withdrawForm');
    const internalTransferForm = document.getElementById('internalTransferForm');
    
    if (withdrawForm) withdrawForm.addEventListener('submit', handleWithdraw);
    if (internalTransferForm) internalTransferForm.addEventListener('submit', handleInternalTransfer);
}

// Populate all account dropdowns
function populateAllAccountDropdowns() {
    populateFromAccountDropdown();
    populateWithdrawAccountDropdown();
    populateInternalTransferDropdowns();
}

// Populate withdraw account dropdown
function populateWithdrawAccountDropdown() {
    const withdrawAccountSelect = document.getElementById('withdrawAccount');
    if (!withdrawAccountSelect) return;

    withdrawAccountSelect.innerHTML = '<option value="">Select account</option>';
    
    userAccounts.forEach(account => {
        const option = document.createElement('option');
        option.value = account.id;
        option.textContent = `${account.accountNumber} - $${formatCurrency(account.balance)} (${account.accountType})`;
        withdrawAccountSelect.appendChild(option);
    });
}

// Populate internal transfer dropdowns
function populateInternalTransferDropdowns() {
    const fromAccountSelect = document.getElementById('internalFromAccount');
    const toAccountSelect = document.getElementById('internalToAccount');
    
    if (!fromAccountSelect || !toAccountSelect) return;

    // Clear existing options
    fromAccountSelect.innerHTML = '<option value="">Select account</option>';
    toAccountSelect.innerHTML = '<option value="">Select account</option>';
    
    // Add user's accounts to both dropdowns
    userAccounts.forEach(account => {
        const fromOption = document.createElement('option');
        fromOption.value = account.id;
        fromOption.textContent = `${account.accountNumber} - $${formatCurrency(account.balance)} (${account.accountType})`;
        fromAccountSelect.appendChild(fromOption);
        
        const toOption = document.createElement('option');
        toOption.value = account.id;
        toOption.textContent = `${account.accountNumber} - $${formatCurrency(account.balance)} (${account.accountType})`;
        toAccountSelect.appendChild(toOption);
    });
}

// Handle withdraw form submission
async function handleWithdraw(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const accountId = formData.get('accountId');
    const amount = parseFloat(formData.get('amount'));

    // Validation
    if (!accountId) {
        showNotification('Please select an account', 'error');
        return;
    }

    if (!amount || amount <= 0) {
        showNotification('Amount must be greater than zero', 'error');
        return;
    }

    // Find the account to check balance
    const account = userAccounts.find(acc => acc.id == accountId);
    if (!account) {
        showNotification('Invalid account', 'error');
        return;
    }

    if (account.balance < amount) {
        showNotification('Insufficient balance for withdrawal', 'error');
        return;
    }

    try {
        showLoading('Processing withdrawal...');
        
        const response = await fetch(`${API_BASE}/accounts/${accountId}/withdraw?amount=${amount}`, {
            method: 'POST'
        });

        const data = await response.json();

        if (response.ok) {
            showNotification(`Successfully withdrew $${amount} from your account!`, 'success');
            event.target.reset();
            await loadUserData(); // Refresh all data
        } else {
            throw new Error(data.error || data.message || 'Withdrawal failed');
        }
    } catch (error) {
        console.error('Withdrawal error:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

// Handle internal transfer between user's own accounts
async function handleInternalTransfer(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const fromAccountId = formData.get('fromAccountId');
    const toAccountId = formData.get('toAccountId');
    const amount = parseFloat(formData.get('amount'));
    const description = formData.get('description') || 'Internal Transfer';

    // Validation
    if (!fromAccountId || !toAccountId) {
        showNotification('Please select both from and to accounts', 'error');
        return;
    }

    if (fromAccountId === toAccountId) {
        showNotification('Cannot transfer to the same account', 'error');
        return;
    }

    if (!amount || amount <= 0) {
        showNotification('Amount must be greater than zero', 'error');
        return;
    }

    // Find the from account to check balance
    const fromAccount = userAccounts.find(acc => acc.id == fromAccountId);
    if (!fromAccount) {
        showNotification('Invalid from account', 'error');
        return;
    }

    if (fromAccount.balance < amount) {
        showNotification('Insufficient balance for transfer', 'error');
        return;
    }

    try {
        showLoading('Processing transfer...');
        
        const response = await fetch(`${API_BASE}/accounts/transfer?fromAccountId=${fromAccountId}&toAccountId=${toAccountId}&amount=${amount}&description=${encodeURIComponent(description)}`, {
            method: 'POST'
        });

        const data = await response.json();

        if (response.ok) {
            showNotification(`Successfully transferred $${amount} between your accounts!`, 'success');
            event.target.reset();
            await loadUserData(); // Refresh all data
        } else {
            throw new Error(data.error || data.message || 'Transfer failed');
        }
    } catch (error) {
        console.error('Internal transfer error:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

// Update the loadUserData function to populate all dropdowns
async function loadUserData() {
    if (!currentUser) return;

    try {
        showLoading('Loading your data...');
        
        // Load user accounts
        const accountsResponse = await fetch(`${API_BASE}/accounts/user/${currentUser.id}`);
        if (accountsResponse.ok) {
            userAccounts = await accountsResponse.json();
            console.log('Accounts loaded:', userAccounts);
            displayAccounts();
            populateAllAccountDropdowns(); // Updated this line
            
            // If user has accounts, load transactions for the first account
            if (userAccounts.length > 0) {
                await loadTransactions(userAccounts[0].id);
            } else {
                displayTransactions(); // Show empty state
            }
        } else {
            throw new Error('Failed to load accounts');
        }

    } catch (error) {
        console.error('Error loading user data:', error);
        showNotification('Error loading data', 'error');
    } finally {
        hideLoading();
    }
}

// Update account display to show withdraw buttons
function displayAccounts() {
    const accountsList = document.getElementById('accountsList');
    if (!accountsList) return;

    if (userAccounts.length === 0) {
        accountsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-wallet"></i>
                <p>No accounts found</p>
                <button class="btn btn-primary" onclick="createAccount()">
                    <i class="fas fa-plus"></i> Create Account with $5000
                </button>
            </div>
        `;
        return;
    }

    accountsList.innerHTML = userAccounts.map(account => `
        <div class="account-item">
            <div class="account-info">
                <div class="account-type">${account.accountType} • ${account.accountNumber}</div>
                <div class="account-balance">$${formatCurrency(account.balance)}</div>
            </div>
            <div class="account-actions">
                <button class="btn btn-sm btn-success" onclick="addDemoMoney(${account.id}, 5000)" title="Add $5000">
                    <i class="fas fa-coins"></i> Add $5K
                </button>
                <button class="btn btn-sm btn-warning" onclick="quickWithdraw(${account.id})" title="Withdraw $100">
                    <i class="fas fa-money-bill-wave"></i> Withdraw $100
                </button>
                <button class="btn btn-sm btn-outline" onclick="loadTransactions(${account.id})">
                    <i class="fas fa-history"></i> History
                </button>
            </div>
        </div>
    `).join('');

    updateQuickStats();
}

// Quick withdraw function
async function quickWithdraw(accountId, amount = 100) {
    const account = userAccounts.find(acc => acc.id == accountId);
    if (!account) {
        showNotification('Account not found', 'error');
        return;
    }

    if (account.balance < amount) {
        showNotification('Insufficient balance for withdrawal', 'error');
        return;
    }

    try {
        showLoading(`Withdrawing $${amount}...`);
        
        const response = await fetch(`${API_BASE}/accounts/${accountId}/withdraw?amount=${amount}`, {
            method: 'POST'
        });

        const data = await response.json();

        if (response.ok) {
            showNotification(`Successfully withdrew $${amount}!`, 'success');
            await loadUserData(); // Refresh all data
        } else {
            throw new Error(data.error || data.message || 'Withdrawal failed');
        }
    } catch (error) {
        console.error('Quick withdrawal error:', error);
        showNotification(error.message, 'error');
    } finally {
        hideLoading();
    }
}

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
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

.account-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    margin-bottom: 0.5rem;
    background: var(--surface-color);
}

.transaction-item {
    display: flex;
    justify-content: space-between;
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

.account-actions {
    display: flex;
    gap: 0.5rem;
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
    
    .account-actions {
        width: 100%;
        justify-content: space-between;
    }
}
`;

// Inject additional styles
const styleSheet = document.createElement('style');
styleSheet.textContent = additionalStyles;
document.head.appendChild(styleSheet);