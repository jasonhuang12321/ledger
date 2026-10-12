let page = 1;
let month = "";

function qs(id) {
    return document.getElementById(id);
}

async function loadList() {
    if (!requireLogin()) {
        return;
    }
    qs("who").textContent = localStorage.getItem("ledgerUsername") || "";
    const adminLink = qs("admin-link");
    if (localStorage.getItem("ledgerRole") === "ADMIN") {
        adminLink.style.display = "inline";
    }
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", "5");
    if (month) {
        params.set("month", month);
    }
    const response = await api("/api/entries?" + params.toString());
    const data = await response.json();
    if (!response.ok) {
        qs("error").textContent = data.error || "加载失败";
        return;
    }
    qs("income").textContent = data.income;
    qs("expense").textContent = data.expense;
    qs("balance").textContent = data.balance;
    qs("pager").textContent = "共 " + data.totalCount + " 条，第 " + data.page + " / " + data.totalPages + " 页";
    qs("prev").disabled = data.page <= 1;
    qs("next").disabled = data.page >= data.totalPages;
    const stats = qs("stats");
    stats.innerHTML = "";
    if (!data.categoryStats || data.categoryStats.length === 0) {
        stats.innerHTML = "<p>还没有支出记录。</p>";
    } else {
        let html = "<table class=\"ledger\"><tr><th>分类</th><th>金额</th></tr>";
        data.categoryStats.forEach(function (s) {
            html += "<tr><td>" + s.category + "</td><td>" + s.amount + "</td></tr>";
        });
        html += "</table>";
        stats.innerHTML = html;
    }
    const body = qs("rows");
    body.innerHTML = "";
    if (!data.entries || data.entries.length === 0) {
        body.innerHTML = "<tr><td colspan=\"5\">还没有记录。</td></tr>";
        return;
    }
    data.entries.forEach(function (e) {
        const tr = document.createElement("tr");
        tr.innerHTML =
            "<td>" + e.createdAt + "</td>" +
            "<td>" + e.type + "</td>" +
            "<td>" + e.category + "</td>" +
            "<td>" + e.amount + "</td>" +
            "<td>" + (e.note || "") + "</td>" +
            "<td><button data-id=\"" + e.id + "\">删除</button></td>";
        tr.querySelector("button").addEventListener("click", function () {
            deleteOne(e.id);
        });
        body.appendChild(tr);
    });
}

async function deleteOne(id) {
    if (!confirm("确定删除这一笔？")) {
        return;
    }
    const response = await api("/api/entries/" + id, { method: "DELETE" });
    if (response.ok) {
        loadList();
    }
}

qs("filter-form").addEventListener("submit", function (event) {
    event.preventDefault();
    month = qs("month").value;
    page = 1;
    loadList();
});

qs("clear-month").addEventListener("click", function () {
    qs("month").value = "";
    month = "";
    page = 1;
    loadList();
});

qs("prev").addEventListener("click", function () {
    if (page > 1) {
        page -= 1;
        loadList();
    }
});

qs("next").addEventListener("click", function () {
    page += 1;
    loadList();
});

qs("add-form").addEventListener("submit", async function (event) {
    event.preventDefault();
    qs("error").textContent = "";
    const body = {
        type: document.querySelector("input[name=type]:checked").value,
        amount: Number(qs("amount").value),
        category: qs("category").value,
        note: qs("note").value
    };
    const response = await api("/api/entries", {
        method: "POST",
        body: JSON.stringify(body)
    });
    const data = await response.json().catch(function () { return {}; });
    if (!response.ok) {
        qs("error").textContent = data.error || "保存失败";
        return;
    }
    qs("amount").value = "";
    qs("category").value = "";
    qs("note").value = "";
    page = 1;
    loadList();
});

qs("export-btn").addEventListener("click", async function () {
    const params = month ? ("?month=" + encodeURIComponent(month)) : "";
    const response = await api("/api/entries/export" + params, { headers: {} });
    if (!response.ok) {
        return;
    }
    const blob = await response.blob();
    const a = document.createElement("a");
    a.href = URL.createObjectURL(blob);
    a.download = "我的账单.xlsx";
    a.click();
    URL.revokeObjectURL(a.href);
});

qs("logout-btn").addEventListener("click", function () {
    logoutApi();
});

loadList();
