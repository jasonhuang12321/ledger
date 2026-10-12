const TOKEN_KEY = "ledgerToken";
const USER_KEY = "ledgerUsername";
const ROLE_KEY = "ledgerRole";

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function saveLogin(data) {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, data.username);
    localStorage.setItem(ROLE_KEY, data.role);
}

function clearLogin() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(ROLE_KEY);
}

function authHeaders() {
    const headers = {};
    const token = getToken();
    if (token) {
        headers.Authorization = "Bearer " + token;
    }
    return headers;
}

async function api(url, options) {
    const opts = options || {};
    const headers = Object.assign({ "Content-Type": "application/json" }, authHeaders(), opts.headers || {});
    const response = await fetch(url, Object.assign({}, opts, { headers: headers }));
    if (response.status === 401) {
        clearLogin();
        window.location.href = "/app/index.html";
        throw new Error("未登录");
    }
    return response;
}

function requireLogin() {
    if (!getToken()) {
        window.location.href = "/app/index.html";
        return false;
    }
    return true;
}

async function logoutApi() {
    try {
        await api("/api/logout", { method: "POST" });
    } catch (e) {
        // 令牌没了也清本地
    }
    clearLogin();
    window.location.href = "/app/index.html";
}
