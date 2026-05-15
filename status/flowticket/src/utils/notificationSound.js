let audioContext = null
let unlocked = false

function createAudioContext() {
  if (audioContext) return audioContext

  const AudioContextClass = window.AudioContext || window.webkitAudioContext
  if (!AudioContextClass) return null

  audioContext = new AudioContextClass()
  return audioContext
}

async function unlockAudio() {
  const context = createAudioContext()
  if (!context) return

  if (context.state === 'suspended') {
    await context.resume()
  }
  unlocked = true
}

if (typeof window !== 'undefined') {
  const unlock = () => {
    unlockAudio().catch(() => {})
  }

  window.addEventListener('pointerdown', unlock, { once: true })
  window.addEventListener('keydown', unlock, { once: true })
}

export function playNotificationSound() {
  const context = createAudioContext()
  if (!context || !unlocked) return

  const now = context.currentTime
  const gain = context.createGain()
  gain.gain.setValueAtTime(0.0001, now)
  gain.gain.exponentialRampToValueAtTime(0.08, now + 0.02)
  gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.42)
  gain.connect(context.destination)

  ;[660, 880].forEach((frequency, index) => {
    const oscillator = context.createOscillator()
    oscillator.type = 'sine'
    oscillator.frequency.setValueAtTime(frequency, now + index * 0.12)
    oscillator.connect(gain)
    oscillator.start(now + index * 0.12)
    oscillator.stop(now + index * 0.12 + 0.22)
  })
}
