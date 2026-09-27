<script setup lang="ts">
import { CheckIcon, ChevronsUpDownIcon } from '@lucide/vue'
import { computed, ref } from 'vue'

import { Button } from '@/components/ui/button'
import {
  Combobox,
  ComboboxAnchor,
  ComboboxEmpty,
  ComboboxGroup,
  ComboboxInput,
  ComboboxItem,
  ComboboxItemIndicator,
  ComboboxList,
  ComboboxTrigger,
} from '@/components/ui/combobox'
import { normalizeUbigeo } from '@/data/ubigeo-peru'

const props = defineProps<{
  modelValue: string
  options: string[]
  placeholder?: string
  disabled?: boolean
  searchPlaceholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const search = ref('')

const selected = computed({
  get: () => (props.modelValue ? props.modelValue : undefined),
  set: (value: string | undefined) => {
    emit('update:modelValue', value ?? '')
  },
})

const filteredOptions = computed(() => {
  const query = normalizeUbigeo(search.value)
  if (!query) {
    return props.options
  }
  return props.options.filter((option) => normalizeUbigeo(option).includes(query))
})

const customOption = computed(() => {
  const value = search.value.trim()
  if (!value) {
    return ''
  }
  const exists = props.options.some((option) => normalizeUbigeo(option) === normalizeUbigeo(value))
  return exists ? '' : value
})
</script>

<template>
  <Combobox
    v-model="selected"
    :disabled="disabled"
    ignore-filter
    class="w-full"
  >
    <ComboboxAnchor class="w-full">
      <ComboboxTrigger as-child>
        <Button
          variant="outline"
          class="mt-1.5 h-10 w-full justify-between px-3 font-normal text-slate-900"
          :disabled="disabled"
          type="button"
        >
          <span class="truncate" :class="modelValue ? 'text-slate-900' : 'text-slate-400'">
            {{ modelValue || placeholder || 'Seleccione o escriba' }}
          </span>
          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
        </Button>
      </ComboboxTrigger>
    </ComboboxAnchor>

    <ComboboxList class="z-[80] w-(--reka-combobox-trigger-width)" align="start">
      <ComboboxInput
        v-model="search"
        :placeholder="searchPlaceholder || 'Buscar o escribir...'"
      />
      <ComboboxEmpty>Sin resultados.</ComboboxEmpty>
      <ComboboxGroup>
        <ComboboxItem v-if="customOption" :value="customOption">
          Usar “{{ customOption }}”
        </ComboboxItem>
        <ComboboxItem v-for="option in filteredOptions" :key="option" :value="option">
          {{ option }}
          <ComboboxItemIndicator>
            <CheckIcon />
          </ComboboxItemIndicator>
        </ComboboxItem>
      </ComboboxGroup>
    </ComboboxList>
  </Combobox>
</template>
