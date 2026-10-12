import { createApp } from "vue";
import { createRouter, createWebHashHistory } from "vue-router";
import App from "./App.vue";
import LoginView from "./views/LoginView.vue";
import HomeView from "./views/HomeView.vue";
import AdminView from "./views/AdminView.vue";
import { getToken, currentUser } from "./api.js";
import "./style.css";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", redirect: "/home" },
    { path: "/login", component: LoginView },
    { path: "/home", component: HomeView },
    { path: "/admin", component: AdminView }
  ]
});

router.beforeEach((to) => {
  if (to.path === "/login") {
    return true;
  }
  if (!getToken()) {
    return "/login";
  }
  if (to.path === "/admin" && currentUser().role !== "ADMIN") {
    return "/home";
  }
  return true;
});

createApp(App).use(router).mount("#app");
