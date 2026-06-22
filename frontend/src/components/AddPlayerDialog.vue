<template>
  <el-dialog
    v-model="dialogVisible"
    title="添加玩家"
    width="400px"
    :close-on-click-modal="false"
  >
    <div v-if="order" style="margin-bottom: 20px">
      <div style="font-size: 18px; font-weight: 600; margin-bottom: 10px">
        {{ order.scriptName }}
      </div>
      <div style="color: #606266; font-size: 14px; line-height: 1.8">
        <div>⏱️ 开场时间：{{ formatTime(order.startTime) }}</div>
        <div>👥 当前人数：{{ order.totalPlayers }}人
          （♂ {{ order.currentMale }} / ♀ {{ order.currentFemale }}）
        </div>
        <div style="color: #e6a23c" v-if="order.status === 'PENDING'">
          🎯 还差 {{ order.needTotal }} 人成团
          <span v-if="order.needMale > 0">（缺{{ order.needMale }}男）</span>
          <span v-if="order.needFemale > 0">（缺{{ order.needFemale }}女）</span>
        </div>
      </div>
    </div>

    <el-form label-width="100px">
      <el-form-item label="增加男生">
        <el-input-number
          v-model="addMale"
          :min="0"
          :max="20"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="增加女生">
        <el-input-number
          v-model="addFemale"
          :min="0"
          :max="20"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确认添加
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { orderApi } from '../api'

const props = defineProps({
  visible: Boolean,
  order: Object
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const loading = ref(false)
const addMale = ref(0)
const addFemale = ref(0)

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const handleSubmit = async () => {
  if (addMale.value === 0 && addFemale.value === 0) {
    ElMessage.warning('请至少添加一名玩家')
    return
  }

  loading.value = true
  try {
    await orderApi.addPlayer({
      orderId: props.order.id,
      addMale: addMale.value,
      addFemale: addFemale.value
    })
    emit('success')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    addMale.value = 0
    addFemale.value = 0
  }
})
</script>
