const BASE_URL = '/api';

export const tokenStore = {
  getAccessToken: () => localStorage.getItem('access_token'),
  getRefreshToken: () => localStorage.getItem('refresh_token'),
  getUser: () => {
    const user = localStorage.getItem('auth_user');
    return user ? JSON.parse(user) : null;
  },
  setTokens: (accessToken, refreshToken, user) => {
    if (accessToken) localStorage.setItem('access_token', accessToken);
    if (refreshToken) localStorage.setItem('refresh_token', refreshToken);
    if (user) localStorage.setItem('auth_user', JSON.stringify(user));
  },
  clear: () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('auth_user');
  }
};

// Generic authenticated fetch wrapper
async function authFetch(endpoint, options = {}) {
  const token = tokenStore.getAccessToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  let response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  // Handle token expiration: attempt one automatic refresh
  if (response.status === 401 && tokenStore.getRefreshToken()) {
    const refreshSuccess = await refreshAccessToken();
    if (refreshSuccess) {
      const retryToken = tokenStore.getAccessToken();
      headers.Authorization = `Bearer ${retryToken}`;
      response = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        headers,
      });
    }
  }

  if (!response.ok) {
    let errorDetail;
    try {
      errorDetail = await response.json();
    } catch {
      errorDetail = { message: response.statusText };
    }
    const err = new Error(errorDetail.message || errorDetail.error || 'Request failed');
    err.status = response.status;
    err.payload = errorDetail;
    throw err;
  }

  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return response.json();
  }
  return response.text();
}

// Authentication endpoints
export async function login(username, password) {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    const err = await response.json().catch(() => ({ message: 'Invalid credentials' }));
    throw new Error(err.message || 'Login failed');
  }

  const data = await response.json();
  tokenStore.setTokens(data.accessToken, data.refreshToken, { username });
  return data;
}

export async function refreshAccessToken() {
  const refreshToken = tokenStore.getRefreshToken();
  if (!refreshToken) return false;

  try {
    const response = await fetch(`${BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) {
      tokenStore.clear();
      return false;
    }

    const data = await response.json();
    tokenStore.setTokens(data.accessToken, null, null);
    return true;
  } catch {
    tokenStore.clear();
    return false;
  }
}

// Ledger endpoints
export function fetchPortfolio(accountId) {
  return authFetch(`/ledger/portfolio/${accountId}`);
}

export function fetchAccountHistory(accountId) {
  return authFetch(`/ledger/account/${accountId}`);
}

export function recordTrade(tradeData, idempotencyKey) {
  return authFetch('/ledger/trade', {
    method: 'POST',
    headers: idempotencyKey ? { 'X-Idempotency-Key': idempotencyKey } : {},
    body: JSON.stringify(tradeData),
  });
}

export function executeCorporateAction(actionData) {
  return authFetch('/ledger/corporate-action', {
    method: 'POST',
    body: JSON.stringify(actionData),
  });
}