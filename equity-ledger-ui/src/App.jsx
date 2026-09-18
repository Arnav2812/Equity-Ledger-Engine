import React, { useState, useEffect } from 'react';
import {
  login,
  tokenStore,
  fetchPortfolio,
  fetchAccountHistory,
  recordTrade,
  executeCorporateAction
} from './api/ledgerApi';
import './App.css';

export default function App() {
  const [currentUser, setCurrentUser] = useState(tokenStore.getUser());
  const [currentRole, setCurrentRole] = useState(null);
  const [accountId, setAccountId] = useState('ACC-USER-101');
  const [portfolio, setPortfolio] = useState([]);
  const [history, setHistory] = useState([]);
  const [alert, setAlert] = useState(null);
  const [loading, setLoading] = useState(false);

  // Trade form state
  const [tradeSymbol, setTradeSymbol] = useState('RELIANCE');
  const [tradeAmount, setTradeAmount] = useState(10);
  const [tradeType, setTradeType] = useState('BUY');

  // Corporate Action form state
  const [caType, setCaType] = useState('STOCK_SPLIT');
  const [caSymbol, setCaSymbol] = useState('RELIANCE');
  const [caRatio, setCaRatio] = useState(2);

  // Parse current role from token payload
  const updateRoleFromToken = () => {
    const token = tokenStore.getAccessToken();
    if (!token) {
      setCurrentRole(null);
      return;
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      setCurrentRole(payload.role || 'UNKNOWN');
    } catch {
      setCurrentRole(null);
    }
  };

  useEffect(() => {
    updateRoleFromToken();
  }, [currentUser]);

  const showAlert = (message, type = 'info', status = null) => {
    setAlert({ message, type, status });
    setTimeout(() => setAlert(null), 6000);
  };

  // Preset role authentication
  const switchRole = async (username, password) => {
    setLoading(true);
    try {
      await login(username, password);
      setCurrentUser(tokenStore.getUser());
      updateRoleFromToken();
      showAlert(`Authenticated as ${username.toUpperCase()}`, 'success');
      loadDashboardData();
    } catch (err) {
      showAlert(err.message, 'error', err.status);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    tokenStore.clear();
    setCurrentUser(null);
    setCurrentRole(null);
    showAlert('Session cleared (Unauthenticated state)', 'warning');
  };

  const loadDashboardData = async () => {
    try {
      const [holdings, entries] = await Promise.all([
        fetchPortfolio(accountId),
        fetchAccountHistory(accountId),
      ]);
      setPortfolio(holdings);
      setHistory(entries);
    } catch (err) {
      if (err.status === 401) {
        showAlert('401 Unauthorized: Valid JWT access token required.', 'error', 401);
      } else if (err.status === 403) {
        showAlert('403 Forbidden: Insufficient role permissions.', 'error', 403);
      } else {
        showAlert(err.message, 'error', err.status);
      }
    }
  };

  useEffect(() => {
    if (tokenStore.getAccessToken()) {
      loadDashboardData();
    }
  }, [accountId]);

  const handleTradeSubmit = async (e) => {
    e.preventDefault();
    const isBuy = tradeType === 'BUY';
    const payload = {
      sourceAccount: isBuy ? 'ACC-MARKET-POOL' : accountId,
      targetAccount: isBuy ? accountId : 'ACC-MARKET-POOL',
      symbol: tradeSymbol,
      amount: Number(tradeAmount),
    };
    const idempotencyKey = `UI-TX-${Date.now()}`;

    try {
      await recordTrade(payload, idempotencyKey);
      showAlert(`200 OK: Trade executed successfully (${tradeType} ${tradeAmount} ${tradeSymbol})`, 'success', 200);
      loadDashboardData();
    } catch (err) {
      showAlert(
        `${err.status ? `${err.status} ` : ''}${err.status === 403 ? 'Forbidden: ' : ''}${err.message}`,
        'error',
        err.status
      );
    }
  };

  const handleCorporateActionSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      type: caType,
      symbol: caSymbol,
      ratio: Number(caRatio),
      targetAccount: accountId,
    };

    try {
      const res = await executeCorporateAction(payload);
      showAlert(`200 OK: ${res}`, 'success', 200);
      loadDashboardData();
    } catch (err) {
      showAlert(
        `${err.status ? `${err.status} ` : ''}${err.status === 403 ? 'Forbidden: ' : ''}${err.message}`,
        'error',
        err.status
      );
    }
  };

  return (
    <div className="app-container">
      {/* Top IAM Control Bar */}
      <header className="iam-header">
        <div className="brand">
          <h2>⚡ Equity Ledger Engine</h2>
          <span className="subtitle">High-Throughput ACID Trading & IAM Platform</span>
        </div>

        <div className="iam-controls">
          <div className="user-badge">
            <span className="dot" style={{ backgroundColor: currentRole ? '#10b981' : '#ef4444' }}></span>
            <span>Role: <strong>{currentRole || 'UNAUTHENTICATED'}</strong></span>
          </div>

          <div className="quick-switch">
            <button className="btn-role" onClick={() => switchRole('admin', 'admin123')}>Admin</button>
            <button className="btn-role" onClick={() => switchRole('operator', 'op123')}>Operator</button>
            <button className="btn-role" onClick={() => switchRole('viewer', 'view123')}>Viewer</button>
            <button className="btn-logout" onClick={handleLogout}>Clear Auth</button>
          </div>
        </div>
      </header>

      {/* Global Status Banner */}
      {alert && (
        <div className={`status-banner banner-${alert.type}`}>
          <span className="status-code">{alert.status ? `[HTTP ${alert.status}]` : 'ℹ️'}</span>
          <span>{alert.message}</span>
        </div>
      )}

      <main className="dashboard-grid">
        {/* Left Column: Actions */}
        <section className="controls-column">
          {/* Trade Execution Panel */}
          <div className="card">
            <h3>📈 Order Execution (ADMIN / OPERATOR)</h3>
            <form onSubmit={handleTradeSubmit}>
              <div className="form-group">
                <label>Action</label>
                <select value={tradeType} onChange={(e) => setTradeType(e.target.value)}>
                  <option value="BUY">BUY (Market Pool → User)</option>
                  <option value="SELL">SELL (User → Market Pool)</option>
                </select>
              </div>

              <div className="form-group">
                <label>Symbol</label>
                <input
                  type="text"
                  value={tradeSymbol}
                  onChange={(e) => setTradeSymbol(e.target.value.toUpperCase())}
                  required
                />
              </div>

              <div className="form-group">
                <label>Shares</label>
                <input
                  type="number"
                  min="1"
                  value={tradeAmount}
                  onChange={(e) => setTradeAmount(e.target.value)}
                  required
                />
              </div>

              <button type="submit" className="btn-submit" disabled={loading}>
                Execute Trade
              </button>
            </form>
          </div>

          {/* Corporate Actions Panel */}
          <div className="card">
            <h3>🏛️ Corporate Actions (ADMIN Only)</h3>
            <form onSubmit={handleCorporateActionSubmit}>
              <div className="form-group">
                <label>Action Type</label>
                <select value={caType} onChange={(e) => setCaType(e.target.value)}>
                  <option value="STOCK_SPLIT">Stock Split</option>
                  <option value="BONUS_ISSUE">Bonus Issue</option>
                </select>
              </div>

              <div className="form-group">
                <label>Symbol</label>
                <input
                  type="text"
                  value={caSymbol}
                  onChange={(e) => setCaSymbol(e.target.value.toUpperCase())}
                  required
                />
              </div>

              <div className="form-group">
                <label>Ratio / Multiplier</label>
                <input
                  type="number"
                  min="1"
                  value={caRatio}
                  onChange={(e) => setCaRatio(e.target.value)}
                  required
                />
              </div>

              <button type="submit" className="btn-submit btn-corp" disabled={loading}>
                Apply Action
              </button>
            </form>
          </div>
        </section>

        {/* Right Column: Holdings & Audit Trail */}
        <section className="data-column">
          <div className="card">
            <div className="card-header">
              <h3>💼 Real-Time Portfolio ({accountId})</h3>
              <button className="btn-refresh" onClick={loadDashboardData}>Refresh</button>
            </div>
            <table>
              <thead>
                <tr>
                  <th>Symbol</th>
                  <th>Total Shares</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.length === 0 ? (
                  <tr>
                    <td colSpan="2" className="empty-text">No positions held or unauthenticated.</td>
                  </tr>
                ) : (
                  portfolio.map((item, idx) => (
                    <tr key={idx}>
                      <td className="symbol-cell">{item.symbol}</td>
                      <td className="qty-cell">{item.quantity}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <div className="card">
            <h3>📜 Double-Entry Audit Trail</h3>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Type</th>
                    <th>Symbol</th>
                    <th>Amount</th>
                    <th>Tx ID</th>
                  </tr>
                </thead>
                <tbody>
                  {history.length === 0 ? (
                    <tr>
                      <td colSpan="4" className="empty-text">No audit history recorded.</td>
                    </tr>
                  ) : (
                    history.map((entry, idx) => (
                      <tr key={idx}>
                        <td>
                          <span className={`badge ${entry.entryType === 'CREDIT' ? 'badge-credit' : 'badge-debit'}`}>
                            {entry.entryType}
                          </span>
                        </td>
                        <td>{entry.symbol}</td>
                        <td>{entry.amount}</td>
                        <td className="mono-cell">{entry.transactionId ? entry.transactionId.substring(0, 16) : '-'}...</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}