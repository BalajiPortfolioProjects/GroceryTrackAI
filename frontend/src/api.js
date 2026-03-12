// Centralized API client — all requests use relative /api base
// Vite dev server proxies /api → http://localhost:8080
// Docker nginx proxies /api → http://backend:8080

const BASE = '/api';

async function request(path, options = {}) {
    const res = await fetch(`${BASE}${path}`, options);
    if (!res.ok) {
        const msg = await res.text().catch(() => res.statusText);
        throw new Error(msg || `HTTP ${res.status}`);
    }
    return res.json();
}

// ── Dashboard ──────────────────────────────────────────────────────────────
export const getDashboard = () => request('/dashboard');

// ── Receipts ───────────────────────────────────────────────────────────────
export const getReceipt = (id) => request(`/receipts/${id}`);

export async function parseReceipt(file) {
    const form = new FormData();
    form.append('file', file);
    const res = await fetch(`${BASE}/receipts/parse`, { method: 'POST', body: form });
    if (!res.ok) {
        const msg = await res.text().catch(() => res.statusText);
        throw new Error(msg || `Parse failed: HTTP ${res.status}`);
    }
    return res.json();
}

export const confirmReceipt = (receiptData) =>
    request('/receipts/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(receiptData)
    });

// ── Expenses ───────────────────────────────────────────────────────────────
export const getExpenses = () => request('/expenses');

// ── Budget ─────────────────────────────────────────────────────────────────
export const getBudget = () => request('/budget');

export const updateWeeklyBudget = (weeklyAmount) =>
    request('/budget', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ weeklyAmount }),
    });

export const updateCategoryBudgets = (categories) =>
    request('/budget/categories', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(categories),
    });

// ── Analytics ──────────────────────────────────────────────────────────────
export const getAnalytics = (period = 'week') =>
    request(`/analytics?period=${period}`);
