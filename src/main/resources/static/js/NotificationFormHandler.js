const form = document.getElementById('notificationForm');
const categorySelect = document.getElementById('category');
const messageInput = document.getElementById('message');
const messageCounter = document.getElementById('messageCounter');
const submitButton = document.getElementById('submitButton');
const submissionResult = document.getElementById('submissionResult');
const logsTableBody = document.getElementById('logsTableBody');
const refreshLogsButton = document.getElementById('refreshLogsButton');
const logSummary = document.getElementById('logSummary');

document.addEventListener('DOMContentLoaded', async () => {
    bindEvents();
    await loadCategories();
    await loadLogs();
});

function bindEvents() {
    messageInput.addEventListener('input', () => {
        messageCounter.textContent = messageInput.value.length;
    });

    refreshLogsButton.addEventListener('click', loadLogs);

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

        for (const category of categories) {
            const option = document.createElement('option');
            option.value = category.code;
            option.textContent = category.name;
            categorySelect.appendChild(option);
        }
    } catch (error) {
        categorySelect.innerHTML = '<option value="">Unable to load categories</option>';
        showMessage(error.message, 'error');
    }
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

        setTimeout(loadLogs, 1200);
    } catch (error) {
        showMessage(error.message, 'error');
    } finally {
        submitButton.disabled = false;
    }
}

async function loadLogs() {
    logsTableBody.innerHTML = `
        <tr>
            <td colspan="7" class="empty">Loading logs...</td>
        </tr>
    `;

    try {
        const response = await fetch('/api/notification-logs?page=0&size=20');

        if (!response.ok) {
            throw new Error('Failed to load notification logs');
        }

        const page = await response.json();
        renderLogs(page.content || []);

        logSummary.textContent = `${page.totalElements ?? 0} total log records`;
    } catch (error) {
        logsTableBody.innerHTML = `
            <tr>
                <td colspan="7" class="empty">${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderLogs(logs) {
    if (!logs.length) {
        logsTableBody.innerHTML = `
            <tr>
                <td colspan="7" class="empty">No notification logs yet.</td>
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
