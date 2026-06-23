import type {AppRouteRecord} from '@/types/router'

export const helpRoutes: AppRouteRecord[] = [
  {
    name: 'Test',
    path: '/test',
    component: '/test',
    meta: {
      title: '测试',
      icon: 'ri:gamepad-line',
      keepAlive: false
    }
  }
]
