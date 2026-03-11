import { useState, useEffect } from 'react';
import { CheckCircle, Loader2 } from 'lucide-react';
import { getBudget, updateWeeklyBudget, updateCategoryBudgets } from '../api';

const CATEGORY_CONFIG = [
    { key: 'Produce', color: '#22C55E' },
    { key: 'Dairy & Eggs', color: '#3B82F6' },
    { key: 'Meat & Seafood', color: '#EF4444' },
    { key: 'Pantry & Snacks', color: '#F59E0B' },
    { key: 'Beverages', color: '#8B5CF6' },
];

export default function BudgetSettings() {
    const [weeklyInput, setWeeklyInput] = useState('');
    const [catInputs, setCatInputs] = useState({});
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        getBudget().then((data) => {
            setWeeklyInput(Number(data.weeklyAmount).toFixed(2));
            const cats = {};
            CATEGORY_CONFIG.forEach(({ key }) => {
                cats[key] = Number(data.categories?.[key] || 0).toFixed(2);
            });
            setCatInputs(cats);
        }).finally(() => setLoading(false));
    }, []);

    async function handleSave(e) {
        e.preventDefault();
        const val = parseFloat(weeklyInput);
        if (isNaN(val) || val <= 0) return;
        setSaving(true);
        try {
            await updateWeeklyBudget(val);
            const cats = {};
            Object.entries(catInputs).forEach(([k, v]) => {
                const n = parseFloat(v);
                if (!isNaN(n)) cats[k] = n;
            });
            await updateCategoryBudgets(cats);
            setSaved(true);
            setTimeout(() => setSaved(false), 3000);
        } finally {
            setSaving(false);
        }
    }

    if (loading) return (
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, color: 'var(--color-text-muted)' }}>
            <Loader2 size={20} style={{ animation: 'spin 1s linear infinite' }} /> Loading budget…
        </div>
    );

    // Compute percentage hints from weekly amount
    const weeklyVal = parseFloat(weeklyInput) || 0;
    const pctHints = { 'Produce': 30, 'Dairy & Eggs': 22, 'Meat & Seafood': 20, 'Pantry & Snacks': 18, 'Beverages': 10 };

    return (
        <>
            <div>
                <h1 className="page-title">Budget Settings</h1>
                <p className="page-subtitle">Set your weekly budget and spending limits by category</p>
            </div>

            <form onSubmit={handleSave}>
                <div className="budget-card">
                    <h2 style={{ fontSize: 16, fontWeight: 600 }}>Weekly Budget</h2>
                    <p className="text-muted" style={{ fontSize: 13, marginTop: -12 }}>
                        Set your total weekly grocery spending limit
                    </p>

                    <div className="input-row">
                        <div className="input-group" style={{ width: 220 }}>
                            <label className="input-label">Weekly Budget Amount</label>
                            <div className="input-field">
                                <span className="text-muted">$</span>
                                <input
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={weeklyInput}
                                    onChange={(e) => { setWeeklyInput(e.target.value); setSaved(false); }}
                                />
                            </div>
                        </div>
                        <button type="submit" className="btn btn-primary" disabled={saving}>
                            {saving ? <Loader2 size={16} style={{ animation: 'spin 1s linear infinite' }} /> : null}
                            Save Budget
                        </button>
                    </div>

                    {saved && (
                        <div className="success-banner fade-in">
                            <CheckCircle size={16} />
                            Budget saved successfully!
                        </div>
                    )}

                    <div className="section-divider" />

                    <div>
                        <h3 style={{ fontSize: 15, fontWeight: 600, marginBottom: 4 }}>Category Limits (Optional)</h3>
                        <p className="text-muted" style={{ fontSize: 13 }}>
                            Set spending limits for specific categories within your weekly budget
                        </p>
                    </div>

                    <div className="cat-grid">
                        {CATEGORY_CONFIG.map(({ key, color }) => (
                            <div className="cat-budget-card" key={key}>
                                <div className="cat-budget-top">
                                    <div className="cat-dot-row">
                                        <div className="cat-dot-circle" style={{ background: color }} />
                                        <span style={{ fontSize: 13, fontWeight: 600 }}>{key}</span>
                                    </div>
                                    <span className="text-muted" style={{ fontSize: 12 }}>{pctHints[key]}%</span>
                                </div>
                                <div className="input-field-sm">
                                    <span className="text-muted">$</span>
                                    <input
                                        type="number"
                                        min="0"
                                        step="0.01"
                                        value={catInputs[key] || ''}
                                        onChange={(e) => {
                                            setCatInputs((prev) => ({ ...prev, [key]: e.target.value }));
                                            setSaved(false);
                                        }}
                                    />
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </form>
        </>
    );
}
