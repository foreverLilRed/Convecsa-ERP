<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

import { getGroupIdForRouteName, navItems, type NavGroup } from '@/layouts/nav'

defineProps<{
  open: boolean
  username: string
}>()

const emit = defineEmits<{
  close: []
  logout: []
}>()

const route = useRoute()
const openGroups = ref<Record<string, boolean>>({})

function isGroupOpen(id: string): boolean {
  return Boolean(openGroups.value[id])
}

function toggleGroup(id: string) {
  openGroups.value[id] = !openGroups.value[id]
}

function syncOpenGroupFromRoute() {
  const groupId = getGroupIdForRouteName(route.name)
  if (groupId) {
    openGroups.value[groupId] = true
  }
}

function isGroupActive(group: NavGroup): boolean {
  return group.children.some((child) => child.name === route.name)
}

onMounted(syncOpenGroupFromRoute)
watch(() => route.name, syncOpenGroupFromRoute)
</script>

<template>
  <aside
    class="fixed inset-y-0 left-0 z-30 flex w-64 flex-col border-r border-palette-cyan/70 bg-white transition-transform duration-300 lg:translate-x-0"
    :class="open ? 'translate-x-0' : '-translate-x-full'"
  >
    <div class="border-b border-palette-cyan/70 px-5 py-5">
      <div class="flex items-center gap-2.5">
        <img src="@/assets/convecsa-logo.png" alt="Convecsa" class="h-8 w-auto" />
        <span
          class="rounded bg-palette-navy px-1.5 py-0.5 text-[10px] font-bold leading-none tracking-wider text-white"
          >ERP</span
        >
      </div>
    </div>

    <nav class="flex-1 space-y-1 overflow-y-auto px-3 py-4">
      <template v-for="item in navItems" :key="item.kind === 'link' ? item.name : item.id">
        <RouterLink
          v-if="item.kind === 'link'"
          :to="{ name: item.name }"
          class="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-slate-500 transition-colors duration-150 hover:bg-palette-ice hover:text-palette-navy"
          exact-active-class="!bg-palette-ice !text-palette-navy"
          @click="emit('close')"
        >
          <svg
            v-if="item.icon === 'home'"
            class="h-4 w-4 shrink-0 text-palette-royal"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="1.75"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5Z"
            />
          </svg>
          {{ item.label }}
        </RouterLink>

        <div v-else>
          <button
            class="flex w-full items-center gap-3 rounded-md px-3 py-2 text-left text-sm font-medium transition-colors duration-150 hover:bg-palette-ice hover:text-palette-navy"
            :class="isGroupActive(item) ? 'text-palette-navy' : 'text-slate-500'"
            type="button"
            :aria-expanded="isGroupOpen(item.id)"
            @click="toggleGroup(item.id)"
          >
            <svg
              v-if="item.icon === 'users'"
              class="h-4 w-4 shrink-0 text-palette-royal"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="1.75"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm13 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"
              />
            </svg>
            <svg
              v-else-if="item.icon === 'folder'"
              class="h-4 w-4 shrink-0 text-palette-royal"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="1.75"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7Z"
              />
            </svg>
            <svg
              v-else-if="item.icon === 'shield'"
              class="h-4 w-4 shrink-0 text-palette-royal"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="1.75"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 3 20 7v6c0 5-3.5 8.5-8 9-4.5-.5-8-4-8-9V7l8-4Z"
              />
            </svg>
            <span class="flex-1">{{ item.label }}</span>
            <svg
              class="h-4 w-4 shrink-0 text-palette-royal transition-transform duration-200"
              :class="isGroupOpen(item.id) ? 'rotate-180' : ''"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="1.75"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="m6 9 6 6 6-6" />
            </svg>
          </button>

          <Transition name="submenu">
            <ul
              v-show="isGroupOpen(item.id) && item.children.length > 0"
              class="mt-1 space-y-0.5 overflow-hidden pl-4"
            >
              <li v-for="child in item.children" :key="child.name">
                <RouterLink
                  :to="{ name: child.name }"
                  class="block rounded-md px-3 py-1.5 text-sm text-slate-500 transition-colors duration-150 hover:bg-palette-ice hover:text-palette-navy"
                  active-class="!bg-palette-ice !text-palette-navy !font-medium"
                  @click="emit('close')"
                >
                  {{ child.label }}
                </RouterLink>
              </li>
            </ul>
          </Transition>
        </div>
      </template>
    </nav>

    <div class="border-t border-palette-cyan/70 px-4 py-4">
      <p class="truncate text-xs font-semibold text-palette-navy">{{ username }}</p>
      <button
        class="mt-3 w-full rounded-md px-3 py-2 text-left text-sm font-medium text-slate-500 transition-colors hover:bg-palette-ice hover:text-palette-navy"
        type="button"
        @click="emit('logout')"
      >
        Cerrar sesión
      </button>
    </div>
  </aside>
</template>
