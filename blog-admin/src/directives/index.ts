import type { App } from 'vue'
import { type AuthDirective, setupAuthDirective } from './core/auth'
import { type RippleDirective, setupRippleDirective } from './business/ripple'
import { type RolesDirective, setupRolesDirective } from './core/roles'

export function setupGlobDirectives(app: App) {
  setupAuthDirective(app) // 权限指令
  setupRolesDirective(app) // 角色权限指令
  setupRippleDirective(app) // 水波纹指令
}

export type { AuthDirective, RippleDirective, RolesDirective }
