import type { AuthDirective, RippleDirective, RolesDirective } from '@/directives'

declare module 'vue' {
  export interface GlobalDirectives {
    vAuth: AuthDirective
    vRoles: RolesDirective
    vRipple: RippleDirective
  }
}
