<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统配置</h2>
        <p class="page-subtitle">配置 SLA、自动分派和通知策略。</p>
      </div>
      <el-button type="primary">保存配置</el-button>
    </div>

    <div class="settings-grid">
      <div class="panel">
        <h3 class="panel-title">SLA 策略</h3>
        <el-form label-width="130px">
          <el-form-item label="高优先级响应">
            <el-input-number v-model="settings.highResponse" :min="5" :step="5" />
            <span class="unit">分钟</span>
          </el-form-item>
          <el-form-item label="中优先级响应">
            <el-input-number v-model="settings.mediumResponse" :min="10" :step="10" />
            <span class="unit">分钟</span>
          </el-form-item>
          <el-form-item label="默认解决时限">
            <el-input-number v-model="settings.resolveHours" :min="1" />
            <span class="unit">小时</span>
          </el-form-item>
        </el-form>
      </div>

      <div class="panel">
        <h3 class="panel-title">自动化</h3>
        <el-form label-width="130px">
          <el-form-item label="自动分派">
            <el-switch v-model="settings.autoAssign" />
          </el-form-item>
          <el-form-item label="超时提醒">
            <el-switch v-model="settings.timeoutAlert" />
          </el-form-item>
          <el-form-item label="通知渠道">
            <el-checkbox-group v-model="settings.channels">
              <el-checkbox label="站内信" />
              <el-checkbox label="邮件" />
              <el-checkbox label="短信" />
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const settings = reactive({
  highResponse: 15,
  mediumResponse: 60,
  resolveHours: 24,
  autoAssign: true,
  timeoutAlert: true,
  channels: ['站内信', '邮件']
})
</script>

<style scoped>
.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.unit {
  margin-left: 10px;
  color: #64748b;
}

@media (max-width: 900px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
