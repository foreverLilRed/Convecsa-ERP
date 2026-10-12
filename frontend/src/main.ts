import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Abre el calendario nativo con un clic en cualquier parte del input,
// no solo en el ícono de la derecha. Aplica a todo el sistema.
const DATE_LIKE_INPUT_TYPES = new Set(['date', 'datetime-local', 'month', 'time', 'week'])

document.addEventListener('click', (event) => {
  const target = event.target
  if (!(target instanceof HTMLInputElement)) {
    return
  }
  if (!DATE_LIKE_INPUT_TYPES.has(target.type) || target.disabled || target.readOnly) {
    return
  }
  try {
    target.showPicker()
  } catch {
    // El selector ya estaba abierto o el navegador no permite abrirlo.
  }
})

createApp(App).use(router).mount('#app')
