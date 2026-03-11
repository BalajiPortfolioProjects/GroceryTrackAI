import { createContext, useContext, useReducer, useEffect } from 'react';

const INITIAL_RECEIPTS = [
  {
    id: 1,
    store: 'Whole Foods Market',
    date: 'Mar 9',
    dateISO: '2025-03-09',
    items: 12,
    total: 67.42,
    category: 'Produce',
    categoryColor: '#16A34A',
    categoryBg: '#F0FDF4',
    icon: 'cart',
    parsedItems: [
      { name: 'Organic Spinach 5oz', category: 'Produce', catColor: '#16A34A', catBg: '#F0FDF4', price: 4.99 },
      { name: 'Free Range Eggs (12 ct)', category: 'Dairy & Eggs', catColor: '#2563EB', catBg: '#EFF6FF', price: 6.49 },
      { name: 'Wild Salmon Fillet 1lb', category: 'Meat & Seafood', catColor: '#DC2626', catBg: '#FEF2F2', price: 14.99 },
      { name: 'Organic Whole Milk 1gal', category: 'Dairy & Eggs', catColor: '#2563EB', catBg: '#EFF6FF', price: 5.29 },
      { name: 'Roma Tomatoes 2lb', category: 'Produce', catColor: '#16A34A', catBg: '#F0FDF4', price: 3.49 },
      { name: 'Greek Yogurt 32oz', category: 'Dairy & Eggs', catColor: '#2563EB', catBg: '#EFF6FF', price: 6.99 },
      { name: 'Baby Carrots 1lb', category: 'Produce', catColor: '#16A34A', catBg: '#F0FDF4', price: 2.29 },
      { name: 'Cheddar Cheese 8oz', category: 'Dairy & Eggs', catColor: '#2563EB', catBg: '#EFF6FF', price: 4.79 },
      { name: 'Avocados (3 ct)', category: 'Produce', catColor: '#16A34A', catBg: '#F0FDF4', price: 4.49 },
      { name: 'Atlantic Cod Fillet', category: 'Meat & Seafood', catColor: '#DC2626', catBg: '#FEF2F2', price: 8.99 },
      { name: 'Broccoli Crowns', category: 'Produce', catColor: '#16A34A', catBg: '#F0FDF4', price: 2.49 },
      { name: 'Butter (4 sticks)', category: 'Dairy & Eggs', catColor: '#2563EB', catBg: '#EFF6FF', price: 2.12 },
    ],
  },
  {
    id: 2,
    store: "Trader Joe's",
    date: 'Mar 7',
    dateISO: '2025-03-07',
    items: 8,
    total: 34.20,
    category: 'Dairy',
    categoryColor: '#2563EB',
    categoryBg: '#EFF6FF',
    icon: 'milk',
    parsedItems: [
      { name: 'Almond Milk 64oz', category: 'Dairy & Eggs', catColor: '#2563EB', catBg: '#EFF6FF', price: 3.99 },
      { name: 'Mixed Nuts 16oz', category: 'Pantry & Snacks', catColor: '#F59E0B', catBg: '#FFFBEB', price: 7.99 },
      { name: 'Sparkling Water (12pk)', category: 'Beverages', catColor: '#8B5CF6', catBg: '#F5F3FF', price: 4.49 },
      { name: 'Dark Chocolate Bar', category: 'Pantry & Snacks', catColor: '#F59E0B', catBg: '#FFFBEB', price: 2.49 },
      { name: 'Frozen Brown Rice', category: 'Pantry & Snacks', catColor: '#F59E0B', catBg: '#FFFBEB', price: 2.29 },
      { name: 'Goat Cheese 4oz', category: 'Dairy & Eggs', catColor: '#2563EB', catBg: '#EFF6FF', price: 4.49 },
      { name: 'Cage-Free Eggs (6ct)', category: 'Dairy & Eggs', catColor: '#2563EB', catBg: '#EFF6FF', price: 3.99 },
      { name: 'Orange Juice 64oz', category: 'Beverages', catColor: '#8B5CF6', catBg: '#F5F3FF', price: 4.47 },
    ],
  },
  {
    id: 3,
    store: 'Costco',
    date: 'Mar 5',
    dateISO: '2025-03-05',
    items: 27,
    total: 26.23,
    category: 'Meat',
    categoryColor: '#DC2626',
    categoryBg: '#FEF2F2',
    icon: 'beef',
    parsedItems: [
      { name: 'Ground Beef 3lb', category: 'Meat & Seafood', catColor: '#DC2626', catBg: '#FEF2F2', price: 12.99 },
      { name: 'Chicken Thighs 4lb', category: 'Meat & Seafood', catColor: '#DC2626', catBg: '#FEF2F2', price: 9.99 },
      { name: 'Pork Tenderloin', category: 'Meat & Seafood', catColor: '#DC2626', catBg: '#FEF2F2', price: 3.25 },
    ],
  },
];

const INITIAL_BUDGET = {
  weekly: 200,
  categories: {
    Produce: 60,
    'Dairy & Eggs': 44,
    'Meat & Seafood': 40,
    'Pantry & Snacks': 36,
    Beverages: 20,
  },
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
