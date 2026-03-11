import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import Dashboard from './pages/Dashboard';
import UploadReceipt from './pages/UploadReceipt';
import Expenses from './pages/Expenses';
import BudgetSettings from './pages/BudgetSettings';
import Analytics from './pages/Analytics';
import './index.css';

export default function App() {
  return (
    <BrowserRouter>
      <AppLayout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/upload" element={<UploadReceipt />} />
          <Route path="/expenses" element={<Expenses />} />
          <Route path="/budget" element={<BudgetSettings />} />
          <Route path="/analytics" element={<Analytics />} />
        </Routes>
      </AppLayout>
    </BrowserRouter>
  );
}
