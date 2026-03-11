import { createContext, useContext, useReducer, useEffect } from 'react';

const INITIAL_RECEIPTS = [];

const INITIAL_BUDGET = {
  weekly: 0,
  categories: {},
};

function loadState() {
  try {
    const saved = localStorage.getItem('grocerytrack');
    if (saved) return JSON.parse(saved);
  } catch (e) {}
  return null;
}

function saveState(state) {
  try {
    localStorage.setItem('grocerytrack', JSON.stringify(state));
  } catch (e) {}
}

const initialState = loadState() || {
  receipts: INITIAL_RECEIPTS,
  budget: INITIAL_BUDGET,
};

function reducer(state, action) {
  switch (action.type) {
    case 'ADD_RECEIPT':
      return { ...state, receipts: [action.payload, ...state.receipts] };
    case 'SET_BUDGET':
      return { ...state, budget: { ...state.budget, weekly: action.payload } };
    case 'SET_CAT_BUDGET':
      return {
        ...state,
        budget: {
          ...state.budget,
          categories: { ...state.budget.categories, [action.category]: action.amount },
        },
      };
    default:
      return state;
  }
}

const StoreContext = createContext(null);

export function StoreProvider({ children }) {
  const [state, dispatch] = useReducer(reducer, initialState);

  useEffect(() => {
    saveState(state);
  }, [state]);

  return <StoreContext.Provider value={{ state, dispatch }}>{children}</StoreContext.Provider>;
}

export function useStore() {
  return useContext(StoreContext);
}

// Derived helpers
export function computeMetrics(state) {
  const totalSpent = state.receipts.reduce((s, r) => s + r.total, 0);
  const totalItems = state.receipts.reduce((s, r) => s + r.items, 0);
  const receiptsCount = state.receipts.length;
  const remaining = state.budget.weekly - totalSpent;

  // Category breakdown
  const catMap = {};
  for (const receipt of state.receipts) {
    for (const item of receipt.parsedItems || []) {
      catMap[item.category] = (catMap[item.category] || 0) + item.price;
    }
  }

  const categories = [
    { name: 'Produce & Vegetables', key: 'Produce', color: '#22C55E', amount: catMap['Produce'] || 0 },
    { name: 'Dairy & Eggs', key: 'Dairy & Eggs', color: '#3B82F6', amount: catMap['Dairy & Eggs'] || 0 },
    { name: 'Meat & Seafood', key: 'Meat & Seafood', color: '#EF4444', amount: catMap['Meat & Seafood'] || 0 },
    { name: 'Pantry & Snacks', key: 'Pantry & Snacks', color: '#F59E0B', amount: catMap['Pantry & Snacks'] || 0 },
    { name: 'Beverages', key: 'Beverages', color: '#8B5CF6', amount: catMap['Beverages'] || 0 },
  ].map((c) => ({ ...c, pct: totalSpent > 0 ? Math.round((c.amount / totalSpent) * 100) : 0 }));

  return { totalSpent, totalItems, receiptsCount, remaining, categories };
}
