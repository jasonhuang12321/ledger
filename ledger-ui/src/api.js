const TOKEN_KEY = "ledgerToken";
const USER_KEY = "ledgerUsername";
const ROLE_KEY = "ledgerRole";

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function saveLogin(data) {
  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(USER_KEY, data.username);
  localStorage.setItem(ROLE_KEY, data.role);
}

export function clearLogin() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem(ROLE_KEY);
}

export function currentUser() {
  return {
    username: localStorage.getItem(USER_KEY) || "",
    role: localStorage.getItem(ROLE_KEY) || ""
  };
}

export async function api(url, options = {}) {
  const headers = { ...(options.headers || {}) };
  const token = getToken();
  if (token) {
    headers.Authorization = "Bearer " + token;
  }
  if (options.body && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }
  const response = await fetch(url, { ...options, headers });
  if (response.status === 401) {
    clearLogin();
    window.location.href = import.meta.env.BASE_URL + "#/login";
    throw new Error("未登录");
  }
  return response;
}
