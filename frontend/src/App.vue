<template>
  <router-view />
</template>

<style>
:root {
  --app-bg: #f4f8f0;
  --surface: #fcfdf9;
  --paper: #fbfdf8;
  --ink: #182a20;
  --ink-soft: #40564a;
  --muted: #74897b;
  --line: #e0eada;
  --mint: #e9f4e4;
  --mint-deep: #d9e9d1;
  --sage: #6f9a7d;
  --brand: #14352a;
  --brand-2: #1f5c45;
  --brand-3: #3a7d5c;
  --lime: #c8f169;
  --orange: #d98a3d;
  --shadow-sm: 0 6px 18px rgba(20, 53, 42, 0.05);
  --shadow: 0 12px 32px rgba(20, 53, 42, 0.08);

  --el-color-primary: #1f5c45;
  --el-color-primary-light-3: #4e8269;
  --el-color-primary-light-5: #7fa792;
  --el-color-primary-light-7: #afcbbd;
  --el-color-primary-light-8: #c7dbd0;
  --el-color-primary-light-9: #e9f3ec;
  --el-color-primary-dark-2: #14352a;
  --el-color-success: #4c8b58;
  --el-color-success-light-9: #edf6ec;
  --el-color-warning: #c2913d;
  --el-color-warning-light-9: #faf4e6;
  --el-color-danger: #c0564a;
  --el-color-danger-light-9: #faeceb;
  --el-color-info: #7b8f82;
  --el-color-info-light-9: #f1f5f0;
  --el-text-color-primary: #22342b;
  --el-text-color-regular: #4a6154;
  --el-text-color-secondary: #7b8f82;
  --el-text-color-placeholder: #a3b3a8;
  --el-text-color-disabled: #b9c7bd;
  --el-border-color: #dfe9da;
  --el-border-color-light: #e7efe2;
  --el-border-color-lighter: #edf4e8;
  --el-border-color-extra-light: #f2f7ee;
  --el-fill-color: #eef6e9;
  --el-fill-color-light: #f2f8ee;
  --el-fill-color-lighter: #f6faf3;
  --el-fill-color-blank: #fcfdf9;
  --el-bg-color: #fcfdf9;
  --el-bg-color-page: #f4f8f0;
  --el-bg-color-overlay: #fcfdf9;
  --el-mask-color: rgba(20, 53, 42, 0.32);
  --el-border-radius-base: 10px;
  --el-font-family: "PingFang SC", "HarmonyOS Sans SC", "Microsoft YaHei",
    "Helvetica Neue", Arial, sans-serif;
}

html,
body,
#app {
  height: 100%;
}

html,
body {
  margin: 0;
  padding: 0;
}

body {
  background: var(--app-bg);
  color: var(--ink);
  font-family: var(--el-font-family);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  box-sizing: border-box;
}

h1,
h2,
h3,
h4,
p {
  margin: 0;
}

a {
  color: inherit;
  text-decoration: none;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-thumb {
  background: #c4d6c7;
  border-radius: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

.el-button {
  font-weight: 600;
}

.el-input__wrapper {
  border-radius: 12px;
}

.el-dialog {
  border-radius: 18px;
}

/* 统一的卡片与表单观感 */
.el-card {
  border: 1px solid rgba(31, 92, 69, 0.08);
  border-radius: 18px;
  background: var(--surface);
  box-shadow: var(--shadow-sm);
}

.el-card__header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--line);
}

.el-card__header b {
  font-size: 15px;
  letter-spacing: -0.01em;
}

.el-card__body {
  padding: 20px;
}

.el-form-item__label {
  font-weight: 600;
  color: var(--ink-soft);
}

.el-table {
  --el-table-border-color: #edf4e8;
  --el-table-header-bg-color: #f3f8ef;
  --el-table-row-hover-bg-color: #f6fbf2;
  border-radius: 12px;
}

.el-table th.el-table__cell {
  background: #f3f8ef;
  color: var(--ink-soft);
  font-size: 13px;
  font-weight: 600;
}

.el-table td.el-table__cell {
  font-size: 13px;
}

.el-alert {
  border-radius: 12px;
  padding: 10px 14px;
}

.el-alert .el-alert__title {
  font-size: 12.5px;
}

.el-collapse {
  border: none;
}

.el-collapse-item__header {
  padding: 0 14px;
  border-bottom: none;
  border-radius: 12px;
  background: #f3f8ef;
}

.el-collapse-item__wrap {
  border-bottom: none;
}

.el-dialog__header {
  padding: 20px 22px 8px;
  font-weight: 700;
}

.el-dialog__body {
  padding: 8px 22px 16px;
}

.el-dialog__footer {
  padding: 8px 22px 18px;
}

.el-tag {
  border-radius: 999px;
}

.el-pagination button,
.el-pager li {
  border-radius: 8px;
}

.el-progress-bar__outer {
  background-color: #e9f2e5;
}

.el-descriptions {
  border-radius: 12px;
  overflow: hidden;
}

.el-empty__description p {
  color: var(--muted);
}

/* 手机端全局适配：仅窄屏生效，不影响桌面 */
@media (max-width: 768px) {
  html,
  body {
    -webkit-text-size-adjust: 100%;
  }

  .el-dialog {
    width: calc(100vw - 24px) !important;
    max-width: 640px;
    margin-top: 6vh !important;
  }

  .el-message-box {
    width: calc(100vw - 40px) !important;
    max-width: 420px;
  }

  .el-message {
    min-width: 0;
    max-width: calc(100vw - 32px);
  }

  .el-card__header {
    padding: 13px 14px;
  }

  .el-card__body {
    padding: 14px !important;
  }

  .el-dialog__header {
    padding: 16px 16px 6px;
  }

  .el-dialog__body {
    padding: 8px 16px 12px;
  }

  .el-dialog__footer {
    padding: 6px 16px 14px;
  }

  .el-form-item {
    margin-bottom: 16px;
  }

  .el-pagination {
    flex-wrap: wrap;
    justify-content: center;
    row-gap: 6px;
  }

  .el-alert {
    padding: 9px 12px;
  }

  .el-alert .el-alert__title {
    font-size: 12px;
    line-height: 1.6;
  }
}

</style>
