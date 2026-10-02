<script setup lang="ts">
import type { ComboboxInputEmits, ComboboxInputProps } from 'reka-ui'

import type { HTMLAttributes } from 'vue'
import { SearchIcon } from '@lucide/vue'
import { reactiveOmit } from '@vueuse/core'
import { ComboboxInput, useForwardPropsEmits } from 'reka-ui'
import { cn } from '@/lib/utils'
import { InputGroup, InputGroupAddon } from '@/components/ui/input-group'

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<ComboboxInputProps & {
  class?: HTMLAttributes['class']
}>()

const emits = defineEmits<ComboboxInputEmits>()

const delegatedProps = reactiveOmit(props, 'class')

const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
  <InputGroup>
    <InputGroupAddon>
      <SearchIcon class="size-4 shrink-0 opacity-50" />
    </InputGroupAddon>
    <ComboboxInput
      data-slot="input-group-control"
      :class="cn(
        'flex-1 rounded-none border-0 bg-transparent px-2.5 py-1 text-sm shadow-none outline-none ring-0 placeholder:text-muted-foreground focus-visible:border-0 focus-visible:ring-0 disabled:cursor-not-allowed disabled:opacity-50 disabled:bg-transparent',
        props.class,
      )"
      v-bind="{ ...$attrs, ...forwarded }"
    />
  </InputGroup>
</template>
