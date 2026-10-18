import type { VariantProps } from 'class-variance-authority'
import { cva } from 'class-variance-authority'

export { default as Badge } from './Badge.vue'

export const badgeVariants = cva(
  'inline-flex w-fit shrink-0 items-center justify-center gap-1 overflow-hidden rounded-full border px-2.5 py-1 text-xs font-semibold leading-none whitespace-nowrap',
  {
    variants: {
      variant: {
        default: 'border-sky-200 bg-sky-50 text-sky-700',
        success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
        secondary: 'border-slate-200 bg-slate-100 text-slate-600',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
)

export type BadgeVariants = VariantProps<typeof badgeVariants>
