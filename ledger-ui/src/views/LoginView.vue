<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { saveLogin } from "../api.js";

const router = useRouter();
const username = ref("xiaoming");
const password = ref("123456");
const error = ref("");

async function login() {
  error.value = "";
  const response = await fetch("/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: username.value, password: password.value })
  });
  const data = await response.json();
  if (!response.ok) {
    error.value = data.error || "登录失败";
    return;
  }
  saveLogin(data);
  router.push("/home");
}
</script>

<template>
  <div class="page">
    <h1>记账本（Vue 独立工程）</h1>
    <p>这是另一个项目，只调 Java 的 /api。开发时 Vite 把 /api 转发给 8091。</p>
    <p class="error">{{ error }}</p>
    <p>用户名：<input v-model="username"></p>
    <p>密码：<input v-model="password" type="password"></p>
    <button type="button" @click="login">登录</button>
    <p>演示：xiaoming / 123456　管理员：admin / 123456</p>
  </div>
</template>
