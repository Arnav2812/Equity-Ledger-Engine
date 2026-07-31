import axios from 'axios';

// Matches your Spring Boot @RequestMapping("/api/ledger")
const API_BASE_URL = 'http://localhost:8080/api/ledger';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const ledgerApi = {
  // Matches @PostMapping("/trade")
  executeTrade: async (tradeData) => {
    const response = await api.post('/trade', tradeData, {
      headers: {
        'X-Idempotency-Key': `IDEM-${Date.now()}` // Passes your required idempotency header
      }
    });
    return response.data;
  },

  // Matches @PostMapping("/corporate-action")
  triggerStockSplit: async (splitData) => {
    const response = await api.post('/corporate-action', splitData);
    return response.data;
  },

  getPortfolio: async (userId) => {
    try {
      const response = await api.get(`/portfolio/${userId}`);
      return response.data;
    } catch {
      return mockPortfolio; // Fallback mock data
    }
  },

  getLedgerHistory: async (userId) => {
    try {
      const response = await api.get(`/history/${userId}`);
      return response.data;
    } catch {
      return mockLedgerHistory; // Fallback mock data
    }
  }
};

const mockPortfolio = [
  { ticker: 'RELIANCE', name: 'Reliance Industries', quantity: 150, avgPrice: 2420.00, currentPrice: 2550.50, sector: 'Energy' },
  { ticker: 'INFY', name: 'Infosys Ltd.', quantity: 200, avgPrice: 1410.00, currentPrice: 1485.20, sector: 'Technology' },
  { ticker: 'TATAPWR', name: 'Tata Power Co.', quantity: 500, avgPrice: 380.00, currentPrice: 412.75, sector: 'Power' },
  { ticker: 'IRB', name: 'IRB Infrastructure', quantity: 1200, avgPrice: 58.50, currentPrice: 64.20, sector: 'Infrastructure' }
];

const mockLedgerHistory = [
  { transactionId: 'TXN-9021', ticker: 'RELIANCE', type: 'BUY', quantity: 50, price: 2420.00, timestamp: '2026-07-28 14:32:10', status: 'SETTLED' },
  { transactionId: 'TXN-8812', ticker: 'TATAPWR', type: 'BUY', quantity: 500, price: 380.00, timestamp: '2026-07-25 10:15:00', status: 'SETTLED' }
];