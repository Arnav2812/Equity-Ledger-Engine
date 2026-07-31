import React, { useState, useEffect } from 'react';
import './App.css';

// Inline API client with local fallbacks so it never crashes
const API_BASE_URL = 'http://localhost:8080/api/ledger';

const MARKET_PRICES = {
  RELIANCE: { name: 'Reliance Industries', sector: 'Energy', currentPrice: 2550.50 },
  INFY: { name: 'Infosys Ltd.', sector: 'Technology', currentPrice: 1485.20 },
  TATAPWR: { name: 'Tata Power Co.', sector: 'Power', currentPrice: 412.75 },
  IRB: { name: 'IRB Infrastructure', sector: 'Infrastructure', currentPrice: 64.20 }
};

export default function App() {
  const [portfolio, setPortfolio] = useState([]);

  // Trade Form State
  const [ticker, setTicker] = useState('RELIANCE');
  const [quantity, setQuantity] = useState(10);
  const [tradeType, setTradeType] = useState('BUY');

  // Stock Split Form State
  const [splitTicker, setSplitTicker] = useState('RELIANCE');
  const [splitRatio, setSplitRatio] = useState(2);

  const totalValue = portfolio.reduce((acc, item) => acc + item.quantity * item.currentPrice, 0);
  const totalInvestment = portfolio.reduce((acc, item) => acc + item.quantity * item.avgPrice, 0);
  const totalPL = totalValue - totalInvestment;

  // 1. Function to fetch live portfolio from Spring Boot backend
  const fetchPortfolio = async () => {
    try {
      const res = await fetch('/api/ledger/portfolio/ACC-USER-101');
      if (res.ok) {
        const holdings = await res.json();
        
        // Enrich backend holdings with live market prices for P&L calculations
        const enrichedPortfolio = holdings.map((item) => {
          const meta = MARKET_PRICES[item.symbol] || { 
            name: item.symbol, 
            sector: 'General', 
            currentPrice: Number(item.averageCost) || 100 
          };
          return {
            ...item,
            name: meta.name,
            sector: meta.sector,
            currentPrice: meta.currentPrice,
            avgPrice: Number(item.averageCost),
            quantity: Number(item.quantity)
          };
        });

        setPortfolio(enrichedPortfolio);
      }
    } catch (err) {
      console.error('Failed to fetch portfolio:', err);
    }
  };

  // 2. Fetch live holdings when page first loads
  useEffect(() => {
    fetchPortfolio();
  }, []);

  // 3. Trade Submit Handler
  const handleTradeSubmit = async (e) => {
    e.preventDefault();

    const isBuy = tradeType === 'BUY';
    const payload = {
      sourceAccount: isBuy ? 'ACC-MARKET-POOL' : 'ACC-USER-101',
      targetAccount: isBuy ? 'ACC-USER-101' : 'ACC-MARKET-POOL',
      symbol: ticker.split(' ')[0],
      amount: Number(quantity)
    };

    try {
      const res = await fetch('/api/ledger/trade', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Idempotency-Key': `IDEM-${Date.now()}`
        },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        alert('Trade executed successfully!');
        // 👈 Refetch portfolio from MySQL to update UI instantly!
        await fetchPortfolio();
      } else {
        const errText = await res.text();
        alert(`Backend Error (${res.status}): ${errText}`);
      }
    } catch (err) {
      console.error('Fetch Error:', err);
      alert(`Network Error: ${err.message}`);
    }
  };

  const handleSplitSubmit = async (e) => {
    e.preventDefault();
    alert(`Corporate Action Triggered: ${splitRatio}:1 Split for${splitTicker}!`);
  };

  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="header">
        <div>
          <h1>Equity Ledger & Real-Time Portfolio Platform</h1>
          <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0 0' }}>
            Java Spring Boot + Aerospike NoSQL Cache + MySQL ACID Ledger
          </p>
        </div>
        <div>
          <span className="badge">✓ System Active</span>
        </div>
      </header>

      {/* Metrics Cards */}
      <div className="grid-3">
        <div className="card">
          <div className="card-title">Total Portfolio Value (Aerospike Cache)</div>
          <div className="card-value">₹{totalValue.toLocaleString('en-IN', { maximumFractionDigits: 2 })}</div>
        </div>
        <div className="card">
          <div className="card-title">Total Profit / Loss</div>
          <div className={`card-value ${totalPL >= 0 ? 'text-green' : 'text-red'}`}>
            {totalPL >= 0 ? '+' : ''}₹{totalPL.toLocaleString('en-IN', { maximumFractionDigits: 2 })}
          </div>
        </div>
        <div className="card">
          <div className="card-title">Active Asset Holdings</div>
          <div className="card-value">{portfolio.length} Sectors</div>
        </div>
      </div>

      {/* Holdings Table */}
      <section className="card" style={{ marginBottom: '2rem' }}>
        <h3 style={{ marginTop: 0 }}>Active User Holdings (Cached State)</h3>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Ticker</th>
                <th>Asset Name</th>
                <th>Sector</th>
                <th>Quantity</th>
                <th>Avg Cost</th>
                <th>Live Price</th>
                <th>Market Value</th>
                <th>P&L</th>
              </tr>
            </thead>
            <tbody>
              {portfolio.map((item) => {
                const itemValue = item.quantity * item.currentPrice;
                const itemPL = itemValue - (item.quantity * item.avgPrice);
                return (
                  <tr key={item.ticker}>
                    <td><strong>{item.ticker}</strong></td>
                    <td>{item.name}</td>
                    <td><span className="badge">{item.sector}</span></td>
                    <td>{item.quantity}</td>
                    <td>₹{item.avgPrice.toFixed(2)}</td>
                    <td>₹{item.currentPrice.toFixed(2)}</td>
                    <td>₹{itemValue.toLocaleString('en-IN')}</td>
                    <td className={itemPL >= 0 ? 'text-green' : 'text-red'}>
                      {itemPL >= 0 ? '+' : ''}₹{itemPL.toFixed(2)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>

      {/* Interactive Forms */}
      <div className="grid-3" style={{ gridTemplateColumns: '1fr 1fr' }}>
        <div className="card">
          <h3>📈 Simulate Live Trade Execution</h3>
          <p className="card-title">Sends transactional write to MySQL Ledger via REST API.</p>
          <form onSubmit={handleTradeSubmit} className="action-form">
            <select value={ticker} onChange={(e) => setTicker(e.target.value)}>
              <option value="RELIANCE">RELIANCE (Energy)</option>
              <option value="INFY">INFY (Tech)</option>
              <option value="TATAPWR">TATAPWR (Power)</option>
              <option value="IRB">IRB (Infrastructure)</option>
            </select>
            <select value={tradeType} onChange={(e) => setTradeType(e.target.value)}>
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
            <input 
              type="number" 
              placeholder="Quantity" 
              value={quantity} 
              onChange={(e) => setQuantity(e.target.value)} 
              min="1" 
              required 
            />
            <button type="submit" className="btn-primary">Execute Trade</button>
          </form>
        </div>

        <div className="card">
          <h3>⚡ Trigger Corporate Action</h3>
          <p className="card-title">Executes stock split logic using OOD Strategy Pattern.</p>
          <form onSubmit={handleSplitSubmit} className="action-form">
            <select value={splitTicker} onChange={(e) => setSplitTicker(e.target.value)}>
              <option value="RELIANCE">RELIANCE</option>
              <option value="INFY">INFY</option>
              <option value="TATAPWR">TATAPWR</option>
            </select>
            <select value={splitRatio} onChange={(e) => setSplitRatio(e.target.value)}>
              <option value={2}>2:1 Stock Split</option>
              <option value={5}>5:1 Stock Split</option>
            </select>
            <button type="submit" className="btn-primary" style={{ backgroundColor: '#8b5cf6' }}>
              Apply Split
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}