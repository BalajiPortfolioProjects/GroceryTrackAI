import { useState, useEffect } from 'react';
import { TrendingUp, CheckCircle, Loader2 } from 'lucide-react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    PieChart, Pie, Cell,
} from 'recharts';
import { getAnalytics } from '../api';

const TABS = [
    { label: 'This Week', period: 'week' },
    { label: 'This Month', period: 'month' },
    { label: '3 Months', period: '3months' },
];

const PIE_COLORS = ['#22C55E', '#3B82F6', '#EF4444', '#F59E0B', '#8B5CF6', '#6B7280'];

export default function Analytics() {
    const [activeIdx, setActiveIdx] = useState(0);
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        setLoading(true);
        setError(null);
        getAnalytics(TABS[activeIdx].period)
            .then(setData)
            .catch((e) => setError(e.message))
            .finally(() => setLoading(false));
    }, [activeIdx]);

    const chartData = (data?.chartData || []).map((p) => ({ day: p.label, amount: Number(p.amount) }));
    const pieData = (data?.categories || [])
        .filter((c) => Number(c.amount) > 0)
        .map((c) => ({ name: c.name, value: Number(c.amount) }));

    return (
        <>
            {/* Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Analytics</h1>
                    <p className="page-subtitle">Analyze your spending trends and budget performance</p>
                </div>
                <div className="tab-group">
                    {TABS.map((t, i) => (
                        <button
                            key={t.label}
                            className={`tab-item ${activeIdx === i ? 'active' : ''}`}
                            onClick={() => setActiveIdx(i)}
                        >
                            {t.label}
                        </button>
                    ))}
                </div>
            </div>

            {loading ? (
                <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, color: 'var(--color-text-muted)' }}>
                    <Loader2 size={20} style={{ animation: 'spin 1s linear infinite' }} /> Loading…
                </div>
            ) : error ? (
                <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#EF4444' }}>
                    {error}
                </div>
            ) : (
                <>
                    {/* Summary stats */}
                    <div className="stat-row">
                        <div className="stat-card">
                            <span className="stat-label">Total Spent</span>
                            <span className="stat-value">${Number(data.totalSpent).toFixed(2)}</span>
                            <div className="stat-sub text-red">
                                <TrendingUp size={14} />
                                {data.budgetPct}% of budget
                            </div>
                        </div>
                        <div className="stat-card">
                            <span className="stat-label">Remaining Budget</span>
                            <span className="stat-value text-green">${Math.max(0, Number(data.remaining)).toFixed(2)}</span>
                            <div className="stat-sub text-green">
                                <CheckCircle size={14} />
                                {data.budgetPct}% of budget used
                            </div>
                        </div>
                        <div className="stat-card">
                            <span className="stat-label">Avg Per Trip</span>
                            <span className="stat-value">${Number(data.avgPerTrip).toFixed(2)}</span>
                            <div className="stat-sub text-muted">{(data.categories || []).length > 0 ? 'across all trips' : 'No trips yet'}</div>
                        </div>
                    </div>

                    {/* Charts */}
                    <div className="charts-row" style={{ flex: 1, minHeight: 0 }}>
                        {/* Bar chart */}
                        <div className="chart-card">
                            <div className="chart-title-row">
                                <span className="card-title">Spending Over Time</span>
                                <span className="text-muted" style={{ fontSize: 13 }}>{TABS[activeIdx].label}</span>
                            </div>
                            <div style={{ flex: 1, minHeight: 0 }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={chartData} margin={{ top: 5, right: 10, bottom: 5, left: 0 }}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#E4E4E7" vertical={false} />
                                        <XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#71717A' }} />
                                        <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#71717A' }} tickFormatter={(v) => `$${v}`} />
                                        <Tooltip
                                            contentStyle={{ borderRadius: 8, border: '1px solid #E4E4E7', fontSize: 13 }}
                                            formatter={(v) => [`$${Number(v).toFixed(2)}`, 'Spent']}
                                        />
                                        <Bar dataKey="amount" fill="#2563EB" radius={[4, 4, 0, 0]} />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>

                        {/* Pie chart + legend */}
                        <div className="chart-card">
                            <div className="chart-title-row">
                                <span className="card-title">By Category</span>
                                <span className="text-muted" style={{ fontSize: 13 }}>{TABS[activeIdx].label}</span>
                            </div>
                            <div style={{ height: 200, flexShrink: 0 }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <PieChart>
                                        <Pie
                                            data={pieData.length > 0 ? pieData : [{ name: 'No data', value: 1 }]}
                                            cx="50%" cy="50%"
                                            innerRadius={55} outerRadius={85}
                                            paddingAngle={3} dataKey="value"
                                        >
                                            {(pieData.length > 0 ? pieData : [{}]).map((_, i) => (
                                                <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                                            ))}
                                        </Pie>
                                        <Tooltip
                                            contentStyle={{ borderRadius: 8, border: '1px solid #E4E4E7', fontSize: 13 }}
                                            formatter={(v) => [`$${Number(v).toFixed(2)}`]}
                                        />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>
                            <div className="pie-legend">
                                {(data.categories || []).map((c, i) => (
                                    <div className="pie-legend-item" key={c.name}>
                                        <div className="pie-legend-left">
                                            <div className="pie-legend-dot" style={{ background: PIE_COLORS[i % PIE_COLORS.length] }} />
                                            <span style={{ fontSize: 13 }}>{c.name}</span>
                                        </div>
                                        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                                            <span className="text-muted" style={{ fontSize: 12 }}>{c.pct}%</span>
                                            <span style={{ fontSize: 13, fontWeight: 600 }}>${Number(c.amount).toFixed(2)}</span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </>
            )}
        </>
    );
}
