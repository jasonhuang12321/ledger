// JavaScript 在浏览器里运行，不经过 Java
// 作用：点「删除」时先弹出确认框

document.querySelectorAll('form[action="/delete"]').forEach(function (form) {
    form.addEventListener("submit", function (event) {
        var ok = confirm("确定删除这一笔吗？");
        if (!ok) {
            event.preventDefault(); // 取消提交，Java 那边不会收到删除请求
        }
    });
});
