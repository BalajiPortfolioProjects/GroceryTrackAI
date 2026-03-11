import { useState, useEffect } from 'react';
import { Receipt, Loader2 } from 'lucide-react';
import { getExpenses } from '../api';

export default function Expenses() {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        getExpenses()
            .then(setData)
            .catch((e) => setError(e.message))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return (
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, color: 'var(--color-text-muted)' }}>
            <Loader2 size={20} style={{ animation: 'spin 1s linear infinite' }} /> Loading expenses…
        </div>
    );
    if (error) return (
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#EF4444' }}>
            {error}
        </div>
    );

    const expenses = data?.expenses || [];
    const totalSpent = Number(data?.totalSpent || 0);

    if (expenses.length === 0) {
        return (
            <>
                <div>
                    <h1 className="page-title">Expenses</h1>
                    <p className="page-subtitle">All your grocery receipts in one place</p>
                </div>
                <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 12, color: 'var(--color-text-muted)', fontSize: 14 }}>
                    <Receipt size={48} strokeWidth={1.5} />
                    <span>No receipts yet. Upload one to get started!</span>
                </div>
            </>
        );
    }

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-title">Expenses</h1>
                    <p className="page-subtitle">All your grocery receipts in one place</p>
                </div>
                <span className="badge" style={{ background: 'var(--color-primary-light)', color: 'var(--color-primary)', height: 28, padding: '0 12px', fontSize: 13 }}>
                    Total: ${totalSpent.toFixed(2)}
                </span>
            </div>

            <div className="card" style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
                {/* Table header */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 160px 120px 100px', padding: '8px 0 12px', borderBottom: '1px solid var(--color-border)', gap: 12 }}>
                    {['Store', 'Date', 'Items', 'Total'].map((h) => (
                        <span key={h} style={{ fontSize: 11, fontWeight: 600, color: 'var(--color-text-muted)', letterSpacing: 1 }}>{h.toUpperCase()}</span>
                    ))}
                </div>

                {/* Rows */}
                <div style={{ overflowY: 'auto', flex: 1 }}>
                    {expenses.map((r) => (
                        <div key={r.id} style={{ display: 'grid', gridTemplateColumns: '1fr 160px 120px 100px', alignItems: 'center', padding: '14px 0', borderBottom: '1px solid var(--color-border)', gap: 12 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                <div style={{ width: 36, height: 36, borderRadius: 8, background: r.categoryBg || '#F0FDF4', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                                    <Receipt size={16} color={r.categoryColor || '#16A34A'} />
                                </div>
                                <span style={{ fontWeight: 600, fontSize: 14 }}>{r.store}</span>
                            </div>
                            <span style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>{r.date}</span>
                            <span style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>{r.itemsCount} items</span>
                            <span style={{ fontWeight: 600, fontSize: 14 }}>${Number(r.total).toFixed(2)}</span>
                        </div>
                    ))}
                </div>
            </div>
        </>
    );
}
