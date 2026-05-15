const DEFAULT_DURATION = 6000

function ensureToastRoot() {
  let root = document.querySelector('.app-toast-root')
  if (!root) {
    root = document.createElement('div')
    root.className = 'app-toast-root'
    document.body.appendChild(root)
  }
  return root
}

export function showAppToast(options = {}) {
  const root = ensureToastRoot()
  const duration = options.duration ?? DEFAULT_DURATION
  const type = options.type || 'success'
  const toast = document.createElement('div')
  toast.className = `app-toast app-toast--${type}`

  toast.innerHTML = `
    <span class="app-toast__icon"></span>
    <span class="app-toast__text">${options.message || '操作成功'}</span>
    <button class="app-toast__close" type="button" aria-label="关闭">×</button>
    <span class="app-toast__progress"></span>
  `

  const close = () => {
    toast.classList.add('is-leaving')
    window.setTimeout(() => toast.remove(), 180)
  }

  toast.querySelector('.app-toast__close').addEventListener('click', close)
  toast.style.setProperty('--toast-duration', `${duration}ms`)
  root.appendChild(toast)

  // 自动关闭和底部进度条保持同一时长，模拟截图中的提示条。
  window.setTimeout(close, duration)
}

export function showErrorToast(error, fallback = '操作失败，请稍后重试') {
  if (error?.silent) return
  showAppToast({
    type: 'error',
    message: error?.message || fallback
  })
}
