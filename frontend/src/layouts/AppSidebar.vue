<script setup lang="ts">
import { LogOutIcon } from '@lucide/vue'
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

import { getGroupIdForRouteName, navItems, type NavGroup } from '@/layouts/nav'
import { getSession } from '@/services/auth/authService'

const props = defineProps<{
  open: boolean
  username: string
}>()

const emit = defineEmits<{
  close: []
  logout: []
}>()

const route = useRoute()
const session = getSession()

const visibleNavItems = computed<NavGroup[]>(() =>
  navItems.filter((item) => {
    if (item.children.length === 0) {
      return false
    }
    if (!item.roles || item.roles.length === 0) {
      return true
    }
    return session?.rol != null && item.roles.includes(session.rol)
  }),
)

function isGroupActive(group: NavGroup): boolean {
  return getGroupIdForRouteName(route.name) === group.id
}
</script>

<template>
  <aside
    class="fixed inset-y-0 left-0 z-30 flex w-64 flex-col border-r border-palette-cyan/70 bg-white transition-transform duration-300 lg:translate-x-0"
    :class="props.open ? 'translate-x-0' : '-translate-x-full'"
  >
    <div class="border-b border-palette-cyan/70 px-4 py-5">
      <div class="flex items-center gap-2.5">
        <img src="@/assets/convecsa-logo.png" alt="Convecsa" class="h-8 w-auto" />
        <span class="rounded bg-palette-navy px-1.5 py-0.5 text-[10px] font-bold leading-none tracking-wider text-white">ERP</span>
      </div>
    </div>

    <nav class="flex-1 space-y-4 overflow-y-auto px-3 py-4">
      <section v-for="item in visibleNavItems" :key="item.id">
        <p
          class="flex items-center gap-2 px-3 py-1.5 text-xs font-semibold uppercase tracking-wide"
          :class="isGroupActive(item) ? 'text-palette-navy' : 'text-slate-400'"
        >
          <component :is="item.icon" class="size-4 shrink-0 text-palette-royal" />
          <span>{{ item.label }}</span>
        </p>
        <ul class="mt-1 space-y-0.5">
          <li v-for="child in item.children" :key="child.name">
            <RouterLink
              :to="{ name: child.name }"
              class="flex items-center gap-2.5 rounded-md px-3 py-1.5 text-sm text-slate-500 transition-colors duration-150 hover:bg-palette-ice hover:text-palette-navy"
              active-class="!bg-palette-ice !text-palette-navy !font-medium"
              @click="emit('close')"
            >
              <component :is="child.icon" class="size-4 shrink-0 text-palette-royal" />
              <span>{{ child.label }}</span>
            </RouterLink>
          </li>
        </ul>
      </section>
    </nav>

    <div class="border-t border-palette-cyan/70 px-4 py-4">
      <p class="truncate text-xs font-semibold text-palette-navy">{{ username }}</p>
      <button
        class="mt-3 flex w-full items-center gap-2 rounded-md px-3 py-2 text-left text-sm font-medium text-slate-500 transition-colors hover:bg-palette-ice hover:text-palette-navy"
        type="button"
        title="Cerrar sesión"
        @click="emit('logout')"
      >
        <LogOutIcon class="size-4 shrink-0" />
        <span>Cerrar sesión</span>
      </button>
    </div>
  </aside>
</template>
