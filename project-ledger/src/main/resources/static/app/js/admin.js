async function loadAdmin() {
    if (!requireLogin()) {
        return;
    }
    if (localStorage.getItem("ledgerRole") !== "ADMIN") {
        document.getElementById("error").textContent = "没有权限";
        return;
    }
    document.getElementById("who").textContent = localStorage.getItem("ledgerUsername") || "";
    const month = document.getElementById("month").value;
    const params = new URLSearchParams();
    params.set("page", "1");
    params.set("size", "20");
    if (month) {
        params.set("month", month);
    }
    const response = await api("/api/admin/entries?" + params.toString());
    const data = await response.json();
    if (!response.ok) {
        document.getElementById("error").textContent = data.error || "加载失败";
        return;
    }
    const names = data.usernames || {};
    const summary = data.summary;
    document.getElementById("income").textContent = summary.income;
    document.getElementById("expense").textContent = summary.expense;
    document.getElementById("balance").textContent = summary.balance;
    const body = document.getElementById("rows");
    body.innerHTML = "";
    summary.entries.forEach(function (e) {
        const name = names[e.userId] || names[String(e.userId)] || e.userId;
        const tr = document.createElement("tr");
        tr.innerHTML =
            "<td>" + name + "</td>" +
            "<td>" + e.createdAt + "</td>" +
            "<td>" + e.type + "</td>" +
            "<td>" + e.category + "</td>" +
            "<td>" + e.amount + "</td>" +
            "<td>" + (e.note || "") + "</td>";
        body.appendChild(tr);
    });
}

document.getElementById("filter-form").addEventListener("submit", function (event) {
    event.preventDefault();
    loadAdmin();
});

document.getElementById("logout-btn").addEventListener("click", function () {
    logoutApi();
});

loadAdmin();
