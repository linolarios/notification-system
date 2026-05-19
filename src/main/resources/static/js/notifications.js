const form = document.getElementById('notificationForm');
const categorySelect = document.getElementById('category');
const messageInput = document.getElementById('message');
const messageCounter = document.getElementById('messageCounter');
const submitButton = document.getElementById('submitButton');
const submissionResult = document.getElementById('submissionResult');
const logsTableBody = document.getElementById('logsTableBody');
const refreshLogsButton = document.getElementById('refreshLogsButton');
const logSummary = document.getElementById('logSummary');
const previousLogsPageButton = document.getElementById('previousLogsPageButton');
const nextLogsPageButton = document.getElementById('nextLogsPageButton');
const logsPageInfo = document.getElementById('logsPageInfo');
const logCorrelationFilter = document.getElementById('logCorrelationFilter');
const logCategoryFilter = document.getElementById('logCategoryFilter');
const logSortDirection = document.getElementById('logSortDirection');
const applyLogFiltersButton = document.getElementById('applyLogFiltersButton');
const clearLogFiltersButton = document.getElementById('clearLogFiltersButton');

const logsPageSize = 5;
let currentLogsPage = 0;

document.addEventListener('DOMContentLoaded', async () => {
    bindEvents();
    await loadCategories();
    await loadLogs();
});

function bindEvents() {
    messageInput.addEventListener('input', () => {
        messageCounter.textContent = messageInput.value.length;
    });

    refreshLogsButton.addEventListener('click', () => {
        loadLogs(currentLogsPage);
    });

    previousLogsPageButton.addEventListener('click', () => {
        if (currentLogsPage > 0) {
            loadLogs(currentLogsPage - 1);
        }
    });

    nextLogsPageButton.addEventListener('click', () => {
        loadLogs(currentLogsPage + 1);
    });

    applyLogFiltersButton.addEventListener('click', () => {
        loadLogs(0);
    });

    clearLogFiltersButton.addEventListener('click', () => {
        logCorrelationFilter.value = '';
        logCategoryFilter.value = '';
        logSortDirection.value = 'desc';
        loadLogs(0);
    });

    logCorrelationFilter.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            loadLogs(0);
        }
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        await submitNotification();
    });
}

async function loadCategories() {
    try {
        const response = await fetch('/api/categories');

        if (!response.ok) {
            throw new Error('Failed to load categories');
        }

        const categories = await response.json();

        categorySelect.innerHTML = '<option value="">Select a category</option>';
        logCategoryFilter.innerHTML = '<option value="">All categories</option>';

        for (const category of categories) {
            categorySelect.appendChild(createCategoryOption(category));
            logCategoryFilter.appendChild(createCategoryOption(category));
        }
    } catch (error) {
        categorySelect.innerHTML = '<option value="">Unable to load categories</option>';
        logCategoryFilter.innerHTML = '<option value="">Unable to load categories</option>';
        showMessage(error.message, 'error');
    }
}

function createCategoryOption(category) {
    const option = document.createElement('option');
    option.value = category.code;
    option.textContent = category.name;
    return option;
}

async function submitNotification() {
    const category = categorySelect.value;
    const message = messageInput.value.trim();

    if (!category) {
        showMessage('Please select a category.', 'error');
        return;
    }

    if (!message) {
        showMessage('Message must not be blank.', 'error');
        return;
    }

    submitButton.disabled = true;

    try {
        const response = await fetch('/api/notifications', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Correlation-Id': crypto.randomUUID()
            },
            body: JSON.stringify({
                category,
                message
            })
        });

        const body = await response.json();

        if (!response.ok) {
            throw new Error(extractErrorMessage(body));
        }

        showMessage(
            `Accepted for processing. Correlation ID: ${body.correlationId}, Job ID: ${body.jobId}`,
            'success'
        );

        messageInput.value = '';
        messageCounter.textContent = '0';

        setTimeout(() => loadLogs(0), 1200);
    } catch (error) {
        showMessage(error.message, 'error');
    } finally {
        submitButton.disabled = false;
    }
}

async function loadLogs(pageNumber = currentLogsPage) {
    logsTableBody.innerHTML = `
        <tr>
            <td colspan="7" class="empty">Loading logs...</td>
        </tr>
    `;

    setPaginationControlsDisabled(true);

    try {
        const response = await fetch(buildLogsUrl(pageNumber));

        if (!response.ok) {
            throw new Error('Failed to load notification logs');
        }

        const page = await response.json();

        currentLogsPage = page.page ?? pageNumber;

        renderLogs(page.content || []);
        renderLogsPagination(page);

        logSummary.textContent = `${page.totalElements ?? 0} total log records`;
    } catch (error) {
        logsTableBody.innerHTML = `
            <tr>
                <td colspan="7" class="empty">${escapeHtml(error.message)}</td>
            </tr>
        `;

        logSummary.textContent = '';
        logsPageInfo.textContent = 'Page - of -';
        setPaginationControlsDisabled(true);
    }
}

function buildLogsUrl(pageNumber) {
    const params = new URLSearchParams({
        page: String(pageNumber),
        size: String(logsPageSize),
        sortDirection: logSortDirection.value || 'desc'
    });

    const correlationId = logCorrelationFilter.value.trim();
    const category = logCategoryFilter.value;

    if (correlationId) {
        params.append('correlationId', correlationId);
    }

    if (category) {
        params.append('category', category);
    }

    return `/api/notification-logs?${params.toString()}`;
}

function renderLogs(logs) {
    if (!logs.length) {
        logsTableBody.innerHTML = `
            <tr>
                <td colspan="7" class="empty">No notification logs found.</td>
            </tr>
        `;
        return;
    }

    logsTableBody.innerHTML = logs.map(log => `
        <tr>
            <td>${formatDate(log.createdAt)}</td>
            <td class="correlation">${escapeHtml(log.correlationId || '-')}</td>
            <td>${escapeHtml(log.category)}</td>
            <td>${escapeHtml(log.channel)}</td>
            <td>
                <strong>${escapeHtml(log.recipientName)}</strong><br>
                <span>${escapeHtml(log.recipientEmail || '')}</span><br>
                <span>${escapeHtml(log.recipientPhoneNumber || '')}</span>
            </td>
            <td>
                <span class="status ${escapeHtml(log.status)}">${escapeHtml(log.status)}</span>
            </td>
            <td>${escapeHtml(log.errorMessage || '-')}</td>
        </tr>
    `).join('');
}

function renderLogsPagination(page) {
    const totalPages = page.totalPages ?? 0;
    const totalElements = page.totalElements ?? 0;
    const isEmpty = totalElements === 0 || totalPages === 0;

    if (isEmpty) {
        logsPageInfo.textContent = 'Page 0 of 0';
        setPaginationControlsDisabled(true);
        return;
    }

    const displayPage = currentLogsPage + 1;

    logsPageInfo.textContent = `Page ${displayPage} of ${totalPages}`;
    previousLogsPageButton.disabled = page.first ?? currentLogsPage === 0;
    nextLogsPageButton.disabled = page.last ?? currentLogsPage >= totalPages - 1;
}

function setPaginationControlsDisabled(disabled) {
    previousLogsPageButton.disabled = disabled;
    nextLogsPageButton.disabled = disabled;
}

function showMessage(message, type) {
    submissionResult.textContent = message;
    submissionResult.className = `alert ${type}`;
}

function extractErrorMessage(body) {
    if (body?.fieldErrors?.length) {
        return body.fieldErrors
            .map(fieldError => `${fieldError.field}: ${fieldError.message}`)
            .join(', ');
    }

    return body?.message || 'Unexpected error';
}

function formatDate(value) {
    if (!value) {
        return '-';
    }

    return new Date(value).toLocaleString();
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
