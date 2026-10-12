<script setup>
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api, clearLogin, currentUser } from "../api.js";

const route = useRoute();
const router = useRouter();
const who = ref("");
const isAdmin = ref(false);
const month = ref("");
const page = ref(1);
const summary = ref({
  income: 0,
  expense: 0,
  balance: 0,
  entries: [],
  categoryStats: [],
  totalCount: 0,
  totalPages: 1,
  page: 1
});
const error = ref("");
const form = ref({ type: "支出", amount: "", category: "", note: "" });

function queryOf(pageNum) {
  const query = { page: String(pageNum) };
  if (month.value) {
    query.month = month.value;
  }
  return query;
}

function applyFilter() {
  router.push({ path: "/home", query: queryOf(1) });
}

function clearFilter() {
  month.value = "";
  router.push({ path: "/home", query: { page: "1" } });
}

function goPage(next) {
  router.push({ path: "/home", query: queryOf(next) });
}

async function load() {
  const qMonth = route.query.month;
  month.value = typeof qMonth === "string" ? qMonth : "";
  const qPage = Number(route.query.page);
  page.value = Number.isInteger(qPage) && qPage > 0 ? qPage : 1;
  who.value = currentUser().username;
  isAdmin.value = currentUser().role === "ADMIN";
  const params = new URLSearchParams({ page: String(page.value), size: "5" });
  if (month.value) {
    params.set("month", month.value);
  }
  const response = await api("/api/entries?" + params.toString());
  summary.value = await response.json();
}

async function add() {
  error.value = "";
  const response = await api("/api/entries", {
    method: "POST",
    body: JSON.stringify({
      type: form.value.type,
      amount: Number(form.value.amount),
      category: form.value.category,
      note: form.value.note
    })
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    error.value = data.error || "保存失败";
    return;
  }
  form.value.amount = "";
  form.value.category = "";
  form.value.note = "";
  await router.replace({ path: "/home", query: queryOf(1) });
  await load();
}

async function remove(id) {
  if (!confirm("确定删除？")) {
    return;
  }
  const response = await api("/api/entries/" + id, { method: "DELETE" });
  if (response.ok) {
    await load();
  }
}

async function exportExcel() {
  const params = month.value ? "?month=" + encodeURIComponent(month.value) : "";
  const response = await api("/api/entries/export" + params);
  const blob = await response.blob();
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = "我的账单.xlsx";
  a.click();
  URL.revokeObjectURL(a.href);
}

async function logout() {
  try {
    await api("/api/logout", { method: "POST" });
  } catch (e) {
    // ignore
  }
  clearLogin();
  router.push("/login");
}

watch(() => [route.query.month, route.query.page], load, { immediate: true });
</script>

<template>
  <div class="page">
    <h1>你好，{{ who }}</h1>
    <p>
      <button type="button" @click="logout">退出</button>
      <router-link v-if="isAdmin" to="/admin">管理员全部账单</router-link>
    </p>
    <p class="hint">看地址栏：<code>#/home</code> 是组件；<code>?month=</code> 和 <code>page=</code> 是这一页的筛选。</p>
    <div class="summary">
      <div>收入：<b class="income">{{ summary.income }}</b></div>
      <div>支出：<b class="expense">{{ summary.expense }}</b></div>
      <div>结余：<b>{{ summary.balance }}</b></div>
    </div>
    <p>
      月份：<input type="month" v-model="month">
      <button type="button" @click="applyFilter">筛选</button>
      <button type="button" @click="clearFilter">查看全部</button>
      <button type="button" @click="exportExcel">导出 Excel</button>
    </p>
    <h2>分类统计</h2>
    <p v-if="!summary.categoryStats || summary.categoryStats.length === 0">还没有支出。</p>
    <table v-else>
      <tr><th>分类</th><th>金额</th></tr>
      <tr v-for="s in summary.categoryStats" :key="s.category">
        <td>{{ s.category }}</td>
        <td>{{ s.amount }}</td>
      </tr>
    </table>
    <h2>记一笔</h2>
    <p class="error">{{ error }}</p>
    <p>
      <label><input type="radio" value="支出" v-model="form.type"> 支出</label>
      <label><input type="radio" value="收入" v-model="form.type"> 收入</label>
    </p>
    <p>金额：<input type="number" v-model="form.amount"></p>
    <p>分类：<input v-model="form.category"></p>
    <p>备注：<input v-model="form.note"></p>
    <button type="button" @click="add">保存</button>
    <h2>列表</h2>
    <table>
      <tr>
        <th>日期</th><th>类型</th><th>分类</th><th>金额</th><th>备注</th><th></th>
      </tr>
      <tr v-for="e in summary.entries" :key="e.id">
        <td>{{ e.createdAt }}</td>
        <td>{{ e.type }}</td>
        <td>{{ e.category }}</td>
        <td>{{ e.amount }}</td>
        <td>{{ e.note }}</td>
        <td><button type="button" @click="remove(e.id)">删除</button></td>
      </tr>
    </table>
    <p>
      共 {{ summary.totalCount }} 条，第 {{ summary.page }} / {{ summary.totalPages }} 页
      <button type="button" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
      <button type="button" :disabled="page >= summary.totalPages" @click="goPage(page + 1)">下一页</button>
    </p>
  </div>
</template>
