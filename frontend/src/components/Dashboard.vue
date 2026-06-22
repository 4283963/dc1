<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <div class="create-btn">
        <el-button type="primary" size="large" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          发起拼单
        </el-button>
      </div>
      <h1>剧本杀拼单排房看板</h1>
      <div class="current-time">{{ currentTime }}</div>
    </div>

    <div class="dashboard-content">
      <div class="panel">
        <div class="panel-header">
          <span class="status-dot"></span>
          <h2>拼单列表</h2>
          <span style="color: rgba(255,255,255,0.5); font-size: 13px;">
            共 {{ activeOrders.length }} 场
          </span>
        </div>
        <div class="panel-body">
          <div class="stat-row">
            <div class="stat-item">
              <div class="stat-number">{{ stats.pending }}</div>
              <div class="stat-label">拼单中</div>
            </div>
            <div class="stat-item">
              <div class="stat-number" style="color: #00ff88">{{ stats.confirmed }}</div>
              <div class="stat-label">已成团</div>
            </div>
            <div class="stat-item">
              <div class="stat-number" style="color: #ffaa00">{{ stats.inProgress }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>

          <div v-if="activeOrders.length === 0" class="empty-state">
            暂无拼单数据
          </div>
          <div v-else>
            <div
              v-for="order in activeOrders"
              :key="order.id"
              class="order-card"
              :class="{
                confirmed: order.status === 'CONFIRMED',
                'in-progress': order.status === 'IN_PROGRESS'
              }"
            >
              <div class="order-card-header">
                <span class="script-name">{{ order.scriptName }}</span>
                <span class="status-tag" :class="getStatusClass(order.status)">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
              <div class="order-card-info">
                <span class="info-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(order.startTime) }} - {{ formatTime(order.endTime) }}
                </span>
                <span class="info-item" v-if="order.roomName">
                  <el-icon><House /></el-icon>
                  {{ order.roomName }}
                </span>
                <span class="info-item">
                  <el-icon><User /></el-icon>
                  {{ order.totalPlayers }}/{{ order.totalPlayers + order.needTotal }}人
                </span>
              </div>
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: getProgress(order) + '%' }"
                ></div>
              </div>
              <div class="order-card-footer">
                <div class="need-players" v-if="order.status === 'PENDING'">
                <span class="need-male" v-if="order.needMale > 0">
                  ♂ 缺{{ order.needMale }}男</span>
                <span class="need-female" v-if="order.needFemale > 0">
                  ♀ 缺{{ order.needFemale }}女</span>
                <span style="color: rgba(255,255,255,0.6)" v-if="order.needTotal > 0">
                  还差 {{ order.needTotal }} 人成团
                </span>
              </div>
              <span v-else style="color: rgba(255,255,255,0.6)">
                已{{ order.totalPlayers }}人参加
              </span>
              <div class="action-btns">
                <el-button
                  v-if="order.status === 'PENDING'"
                  size="small"
                  type="primary"
                  @click="openAddPlayer(order)"
                >
                  加人
                </el-button>
                <el-button
                  v-if="order.status === 'CONFIRMED'"
                  size="small"
                  type="success"
                  @click="startOrder(order.id)"
                >
                  开场
                </el-button>
                <el-button
                  v-if="order.status === 'IN_PROGRESS'"
                  size="small"
                  type="warning"
                  @click="finishOrder(order.id)"
                >
                  结束
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <span class="status-dot" style="background: #00ff88"></span>
          <h2>房间状态</h2>
        </div>
        <div class="panel-body">
          <div v-if="roomStatuses.length === 0" class="empty-state">
            暂无房间数据
          </div>
          <div v-else>
            <div
              v-for="room in roomStatuses"
              :key="room.id"
              class="room-item"
            >
              <div class="room-info">
                <div class="room-name">{{ room.name }}</div>
                <div class="room-detail">
                  容纳 {{ room.capacity }} 人
                  <span v-if="room.currentOrderScript">
                    · {{ room.currentOrderScript }}
                  </span>
                </div>
                <div class="room-detail" v-if="room.currentOrderTime">
                  {{ room.currentOrderTime }}
                </div>
              </div>
              <span class="room-status" :class="getRoomStatusClass(room.status)">
                {{ room.status }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <CreateOrderDialog
      v-model:visible="showCreateDialog"
      :scripts="scripts"
      @success="handleCreateSuccess"
    />

    <AddPlayerDialog
      v-model:visible="showAddPlayerDialog"
      :order="currentOrder"
      @success="handleAddPlayerSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Clock, House, User } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { orderApi, roomApi, scriptApi } from '../api'
import CreateOrderDialog from './CreateOrderDialog.vue'
import AddPlayerDialog from './AddPlayerDialog.vue'

const currentTime = ref('')
const activeOrders = ref([])
const roomStatuses = ref([])
const scripts = ref([])
const showCreateDialog = ref(false)
const showAddPlayerDialog = ref(false)
const currentOrder = ref(null)

let timer = null
let refreshTimer = null

const stats = computed(() => {
  const result = { pending: 0, confirmed: 0, inProgress: 0 }
  activeOrders.value.forEach(order => {
    if (order.status === 'PENDING') result.pending++
    else if (order.status === 'CONFIRMED') result.confirmed++
    else if (order.status === 'IN_PROGRESS') result.inProgress++
  })
  return result
})

const updateTime = () => {
  currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

const fetchData = async () => {
  try {
    const [orders, rooms] = await Promise.all([
      orderApi.getActive(),
      roomApi.getStatuses()
    ])
    activeOrders.value = orders
    roomStatuses.value = rooms
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const fetchScripts = async () => {
  try {
    scripts.value = await scriptApi.getAll()
  } catch (error) {
    console.error('获取剧本列表失败:', error)
  }
}

const formatTime = (time) => {
  return dayjs(time).format('HH:mm')
}

const getStatusClass = (status) => {
  const map = {
    PENDING: 'status-pending',
    CONFIRMED: 'status-confirmed',
    IN_PROGRESS: 'status-in-progress'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    PENDING: '拼单中',
    CONFIRMED: '已成团',
    IN_PROGRESS: '进行中',
    FINISHED: '已结束',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getRoomStatusClass = (status) => {
  if (status === '空闲') return 'free'
  if (status === '使用中') return 'busy'
  return 'waiting'
}

const getProgress = (order) => {
  const total = order.totalPlayers + order.needTotal
  if (total === 0) return 0
  return Math.min(100, (order.totalPlayers / total) * 100)
}

const openAddPlayer = (order) => {
  currentOrder.value = order
  showAddPlayerDialog.value = true
}

const handleCreateSuccess = () => {
  showCreateDialog.value = false
  fetchData()
  ElMessage.success('拼单创建成功')
}

const handleAddPlayerSuccess = () => {
  showAddPlayerDialog.value = false
  currentOrder.value = null
  fetchData()
  ElMessage.success('添加成功')
}

const startOrder = async (id) => {
  try {
    await orderApi.start(id)
    fetchData()
    ElMessage.success('已开场')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const finishOrder = async (id) => {
  try {
    await orderApi.finish(id)
    fetchData()
    ElMessage.success('已结束')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  fetchData()
  fetchScripts()
  refreshTimer = setInterval(fetchData, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>
