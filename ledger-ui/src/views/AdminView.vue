<script setup>
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api, currentUser } from "../api.js";

const route = useRoute();
const router = useRouter();
const who = ref("");
const month = ref("");
const error = ref("");
const income = ref(0);
const expense = ref(0);
const balance = ref(0);
const rows = ref([]);

function applyFilter() {
  const query = month.value ? { month: month.value } : {};
  router.push({ path: "/admin", query });
}

async function load() {
  const qMonth = route.query.month;
  month.value = typeof qMonth === "string" ? qMonth : "";
  who.value = currentUser().username;
  const params = new URLSearchParams({ page: "1", size: "20" });
  if (month.value) {
    params.set("month", month.value);
  }
  const response = await api("/api/admin/entries?" + params.toString());
  const data = await response.json();
  if (!response.ok) {
    error.value = data.error || "没有权限";
    return;
  }
  error.value = "";
  const names = data.usernames || {};
  income.value = data.summary.income;
  expense.value = data.summary.expense;
  balance.value = data.summary.balance;
  rows.value = data.summary.entries.map((e) => ({
    ...e,
    username: names[e.userId] || names[String(e.userId)] || e.userId
  }));
}

watch(() => route.query.month, load, { immediate: true });
</script>

<template>
  <div class="page">
    <h1>全部账单（管理员）</h1>
    <p>{{ who }}　<router-link to="/home">回首页</router-link></p>
    <p class="hint">地址栏：<code>#/admin</code>，筛选在 <code>?month=</code>。</p>
    <p class="error">{{ error }}</p>
    <div class="summary">
      <div>收入：<b class="income">{{ income }}</b></div>
      <div>支出：<b class="expense">{{ expense }}</b></div>
      <div>结余：<b>{{ balance }}</b></div>
    </div>
    <p>
      月份：<input type="month" v-model="month">
      <button type="button" @click="applyFilter">筛选</button>
    </p>
    <table>
      <tr>
        <th>用户</th><th>日期</th><th>类型</th><th>分类</th><th>金额</th><th>备注</th>
      </tr>
      <tr v-for="e in rows" :key="e.id">
        <td>{{ e.username }}</td>
        <td>{{ e.createdAt }}</td>
        <td>{{ e.type }}</td>
        <td>{{ e.category }}</td>
        <td>{{ e.amount }}</td>
        <td>{{ e.note }}</td>
      </tr>
    </table>
  </div>
</template>
