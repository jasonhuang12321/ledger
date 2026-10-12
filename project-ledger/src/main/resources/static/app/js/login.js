document.getElementById("login-form").addEventListener("submit", async function (event) {
    event.preventDefault();
    const errorBox = document.getElementById("error");
    errorBox.textContent = "";
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const response = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: username, password: password })
    });
    const data = await response.json();
    if (!response.ok) {
        errorBox.textContent = data.error || "登录失败";
        return;
    }
    saveLogin(data);
    window.location.href = "/app/home.html";
});
