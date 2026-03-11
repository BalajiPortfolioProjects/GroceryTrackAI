import { NavLink } from 'react-router-dom';
import {
    ShoppingCart,
    LayoutDashboard,
    Upload,
    Receipt,
    Target,
    TrendingUp,
} from 'lucide-react';

const navItems = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/upload', label: 'Upload Receipt', icon: Upload },
    { to: '/expenses', label: 'Expenses', icon: Receipt },
    { to: '/budget', label: 'Budget', icon: Target },
    { to: '/analytics', label: 'Analytics', icon: TrendingUp },
];

export default function Sidebar() {
    return (
        <aside className="sidebar">
            <div className="sidebar-header">
                <ShoppingCart size={22} color="#2563EB" />
                <span className="sidebar-logo-text">GroceryTrack</span>
            </div>

            <nav className="sidebar-nav">
                {navItems.map(({ to, label, icon: Icon }) => (
                    <NavLink
                        key={to}
                        to={to}
                        end={to === '/'}
                        className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                    >
                        <Icon size={18} />
                        {label}
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-footer">
                <div className="avatar">JD</div>
                <div className="user-info">
                    <div className="user-name">Jane Doe</div>
                    <div className="user-email">jane@example.com</div>
                </div>
            </div>
        </aside>
    );
}
