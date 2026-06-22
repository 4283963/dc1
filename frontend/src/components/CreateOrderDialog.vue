<template>
  <el-dialog
    v-model="dialogVisible"
    title="发起拼单"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="选择剧本" prop="scriptId">
        <el-select
          v-model="form.scriptId"
          placeholder="请选择剧本"
          style="width: 100%"
          @change="handleScriptChange"
        >
          <el-option
            v-for="script in scripts"
            :key="script.id"
            :label="script.name"
            :value="script.id"
          >
            <span>{{ script.name }}</span>
            <span style="float: right; color: #999; font-size: 13px">
              {{ script.totalPlayers }}人 · ¥{{ script.price }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="开场时间" prop="startTime">
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          placeholder="选择开场时间"
          style="width: 100%"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
      </el-form-item>

      <el-form-item label="已有男生">
        <el-input-number
          v-model="form.initialMale"
          :min="0"
          :max="20"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="已有女生">
        <el-input-number
          v-model="form.initialFemale"
          :min="0"
          :max="20"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="选填"
        />
      </el-form-item>

      <div v-if="selectedScript" style="background: #f5f7fa; padding: 12px; border-radius: 8px; margin-bottom: 15px">
        <div style="font-weight: 600; margin-bottom: 8px; color: #303133">{{ selectedScript.name }}</div>
        <div style="font-size: 13px; color: #606266; line-height: 1.8">
          <div>📋 人数要求：{{ selectedScript.totalPlayers }}人
            <span v-if="selectedScript.minMale">（至少{{ selectedScript.minMale }}男）</span>
            <span v-if="selectedScript.minFemale">（至少{{ selectedScript.minFemale }}女）</span>
          </div>
          <div>⏱️ 游戏时长：{{ selectedScript.duration }}分钟</div>
          <div>💰 单人价格：¥{{ selectedScript.price }}</div>
          <div v-if="selectedScript.description">📝 {{ selectedScript.description }}</div>
        </div>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        发起拼单
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '../api'

const props = defineProps({
  visible: Boolean,
  scripts: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const loading = ref(false)
const form = ref({
  scriptId: null,
  startTime: null,
  initialMale: 0,
  initialFemale: 0,
  remark: ''
})

const selectedScript = computed(() => {
  return props.scripts.find(s => s.id === form.value.scriptId)
})

const rules = {
  scriptId: [{ required: true, message: '请选择剧本', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开场时间', trigger: 'change' }]
}

const handleScriptChange = () => {
}

const resetForm = () => {
  form.value = {
    scriptId: null,
    startTime: null,
    initialMale: 0,
    initialFemale: 0,
    remark: ''
  }
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await orderApi.create(form.value)
      emit('success')
      resetForm()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '创建失败')
    } finally {
      loading.value = false
    }
  })
}

watch(() => props.visible, (val) => {
  if (!val) {
    resetForm()
  }
})
</script>
