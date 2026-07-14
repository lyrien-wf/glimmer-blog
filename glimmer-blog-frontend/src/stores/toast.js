import { reactive } from 'vue'

// 全局 Toast 状态
export const toastState = reactive({
  visible: false,
  message: '',
  type: 'success'
})

let timer = null

function show(message, type = 'success', duration = 2500) {
  clearTimeout(timer)
  toastState.message = message
  toastState.type = type
  toastState.visible = true
  timer = setTimeout(() => {
    toastState.visible = false
  }, duration)
}

// 全局 toast 方法，任意组件可直接导入使用
export const toast = {
  success: (msg, duration) => show(msg, 'success', duration),
  error: (msg, duration) => show(msg, 'error', duration),
  info: (msg, duration) => show(msg, 'info', duration)
}
