import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig(({ command }) => ({
  plugins: [vue()],
  base: command === "build" ? "/ui/" : "/",
  build: {
    outDir: "../project-ledger/src/main/resources/static/ui",
    emptyOutDir: true
  },
  server: {
    port: 5173,
    proxy: {
      "/api": "http://127.0.0.1:8091"
    }
  }
}));
