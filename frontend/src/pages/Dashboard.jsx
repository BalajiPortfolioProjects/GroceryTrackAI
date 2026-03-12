import { useState, useEffect } from 'react';
import { ShoppingBag, Target, Package, ScanLine, TrendingUp, Upload, ShoppingCart, Loader2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { getDashboard } from '../api';

function CategoryRow({ name, color, pct, amount }) {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ width: 8, height: 8, borderRadius: '50%', background: color, flexShrink: 0 }} />
                    <span style={{ fontSize: 13, color: 'var(--color-text-primary)' }}>{name}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <span style={{ fontSize: 12, color: 'var(--color-text-muted)', minWidth: 32, textAlign: 'right' }}>{pct}%</span>
                    <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--color-text-primary)', minWidth: 56, textAlign: 'right' }}>${Number(amount).toFixed(2)}</span>
                </div>
            </div>
            <div style={{ height: 4, borderRadius: 99, background: '#F4F4F5', overflow: 'hidden' }}>
                <div style={{ height: '100%', width: `${pct}%`, borderRadius: 99, background: color, transition: 'width 0.4s ease' }} />
            </div>
        </div>
    );
}

function TransactionRow({ receipt }) {
    return (
        <>
            <div className="tx-row">
                <div className="tx-left">
                    <div className="tx-icon-wrap" style={{ background: receipt.categoryBg || '#F0FDF4' }}>
                        <ShoppingCart size={16} color={receipt.categoryColor || '#16A34A'} />
                    </div>
                    <div>
                        <div className="tx-store">{receipt.store}</div>
                        <div className="tx-date">{receipt.date} · {receipt.items} items</div>
                    </div>
                </div>
                <div className="tx-right">
                    <span className="tx-amount">-${Number(receipt.total).toFixed(2)}</span>
                    <span className="badge" style={{ background: receipt.categoryBg, color: receipt.categoryColor }}>
                        {receipt.category}
                    </span>
                </div>
            </div>
        </>
    );
}

export default function Dashboard() {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        getDashboard()
            .then(setData)
            .catch((e) => setError(e.message))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return (
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, color: 'var(--color-text-muted)' }}>
            <Loader2 size={20} style={{ animation: 'spin 1s linear infinite' }} /> Loading dashboard…
        </div>
    );
    if (error) return (
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#EF4444' }}>
            Error: {error} — make sure the backend is running on port 8080.
        </div>
    );

    const { weeklySpent, weeklyBudget, totalSpends, itemsTracked, receiptsScanned, budgetPct, remaining, categories, recentTransactions } = data;

    return (
        <>
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Dashboard</h1>
                    <p className="page-subtitle">Track your grocery spending and manage your budget</p>
                </div>
                <div className="header-right">
                    <Link to="/upload" className="btn btn-primary" style={{ textDecoration: 'none' }}>
                        <Upload size={16} />
                        Upload Receipt
                    </Link>
                </div>
            </div>

            {/* Metrics Row */}
            <div className="metrics-row">
                <div className="metric-card">
                    <div className="metric-header">
                        <span className="metric-label">Weekly Spending</span>
                        <ShoppingBag size={16} className="metric-icon" />
                    </div>
                    <div className="metric-value">${Number(weeklySpent).toFixed(2)}</div>
                    <div className="metric-footer text-green">
                        <TrendingUp size={14} />
                        {budgetPct}% of budget used
                    </div>
                </div>

                <div className="metric-card">
                    <div className="metric-header">
                        <span className="metric-label">Weekly Budget</span>
                        <Target size={16} className="metric-icon" />
                    </div>
                    <div className="metric-value">${Number(weeklyBudget).toFixed(2)}</div>
                    <div>
                        <div className="budget-labels">
                            <span>${Number(weeklySpent).toFixed(2)} spent</span>
                            <span>${Math.max(0, Number(remaining)).toFixed(2)} remaining</span>
                        </div>
                        <div className="progress-track">
                            <div
                                className="progress-fill"
                                style={{
                                    width: `${Math.min(100, budgetPct)}%`,
                                    background: budgetPct > 90 ? '#EF4444' : '#2563EB',
                                }}
                            />
                        </div>
                    </div>
                </div>

                <div className="metric-card">
                    <div className="metric-header">
                        <span className="metric-label">Total Spends</span>
                        <ShoppingBag size={16} className="metric-icon" />
                    </div>
                    <div className="metric-value">${Number(totalSpends).toFixed(2)}</div>
                    <div className="metric-footer text-green">
                        <TrendingUp size={14} />
                        All time
                    </div>
                </div>

                <div className="metric-card">
                    <div className="metric-header">
                        <span className="metric-label">Items Tracked</span>
                        <Package size={16} className="metric-icon" />
                    </div>
                    <div className="metric-value">{itemsTracked}</div>
                    <div className="metric-footer text-green">
                        <TrendingUp size={14} />
                        This week
                    </div>
                </div>

                <div className="metric-card">
                    <div className="metric-header">
                        <span className="metric-label">Receipts Scanned</span>
                        <ScanLine size={16} className="metric-icon" />
                    </div>
                    <div className="metric-value">{receiptsScanned}</div>
                    <div className="metric-footer">
                        <span className="badge" style={{ background: '#DCFCE7', color: '#16A34A' }}>This week</span>
                    </div>
                </div>
            </div>

            {/* Bottom Row */}
            <div className="bottom-row" style={{ flex: 1, minHeight: 0 }}>
                {/* Category Breakdown */}
                <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16, overflow: 'hidden' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span className="card-title">Spending by Category</span>
                        <span className="text-muted" style={{ fontSize: 13 }}>All time</span>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 14, overflowY: 'auto', flex: 1 }}>
                        {(categories || []).map((c) => (
                            <CategoryRow key={c.name} name={c.name} color={c.color} pct={c.pct} amount={c.amount} />
                        ))}
                    </div>
                </div>

                {/* Recent Transactions */}
                <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 0, overflow: 'hidden' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                        <span className="card-title">Recent Transactions</span>
                        <Link to="/expenses" style={{ color: '#2563EB', fontSize: 13, textDecoration: 'none' }}>View all →</Link>
                    </div>
                    <div className="divider" />
                    <div style={{ overflowY: 'auto', flex: 1 }}>
                        {(recentTransactions || []).map((r, i) => (
                            <div key={r.id}>
                                <TransactionRow receipt={r} />
                                {i < recentTransactions.length - 1 && <div className="divider" />}
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </>
    );
}
