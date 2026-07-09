<script setup lang="ts">
import type { ComputedRef, Ref } from 'vue'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

// 导入头像图片
import avatar1 from '@/assets/images/avatar/avatar1.webp'
import avatar2 from '@/assets/images/avatar/avatar2.webp'
import avatar3 from '@/assets/images/avatar/avatar3.webp'
import avatar4 from '@/assets/images/avatar/avatar4.webp'
import avatar5 from '@/assets/images/avatar/avatar5.webp'
import avatar6 from '@/assets/images/avatar/avatar6.webp'

interface NoticeItem {
  /** 标题 */
  title: string
  /** 时间 */
  time: string
  /** 类型 */
  type: NoticeType
}

interface MessageItem {
  /** 标题 */
  title: string
  /** 时间 */
  time: string
  /** 头像 */
  avatar: string
}

interface PendingItem {
  /** 标题 */
  title: string
  /** 时间 */
  time: string
}

interface BarItem {
  /** 名称 */
  name: ComputedRef<string>
  /** 数量 */
  num: number
}

interface NoticeStyle {
  /** 图标 */
  icon: string
  /** icon 样式 */
  iconClass: string
}

type NoticeType = 'email' | 'message' | 'collection' | 'user' | 'notice'

const { t } = useI18n()

const props = defineProps<{
  value: boolean
}>()

const emit = defineEmits<{
  'update:value': [value: boolean]
}>()

const show = ref(false)
const visible = ref(false)
const barActiveIndex = ref(0)

const useNotificationData = () => {
  // 通知数据
  const noticeList = ref<NoticeItem[]>([
    {
      title: '新增国际化',
      time: '2024-6-13 0:10',
      type: 'notice',
    },
    {
      title: '冷月呆呆给你发了一条消息',
      time: '2024-4-21 8:05',
      type: 'message',
    },
    {
      title: '小肥猪关注了你',
      time: '2020-3-17 21:12',
      type: 'collection',
    },
    {
      title: '新增使用文档',
      time: '2024-02-14 0:20',
      type: 'notice',
    },
    {
      title: '小肥猪给你发了一封邮件',
      time: '2024-1-20 0:15',
      type: 'email',
    },
    {
      title: '菜单mock本地真实数据',
      time: '2024-1-17 22:06',
      type: 'notice',
    },
  ])

  // 消息数据
  const msgList = ref<MessageItem[]>([
    {
      title: '池不胖 关注了你',
      time: '2021-2-26 23:50',
      avatar: avatar1,
    },
    {
      title: '唐不苦 关注了你',
      time: '2021-2-21 8:05',
      avatar: avatar2,
    },
    {
      title: '中小鱼 关注了你',
      time: '2020-1-17 21:12',
      avatar: avatar3,
    },
    {
      title: '何小荷 关注了你',
      time: '2021-01-14 0:20',
      avatar: avatar4,
    },
    {
      title: '誶誶淰 关注了你',
      time: '2020-12-20 0:15',
      avatar: avatar5,
    },
    {
      title: '冷月呆呆 关注了你',
      time: '2020-12-17 22:06',
      avatar: avatar6,
    },
  ])

  // 待办数据
  const pendingList = ref<PendingItem[]>([])

  // 标签栏数据
  const barList = computed<BarItem[]>(() => [
    {
      name: computed(() => t('notice.bar[0]')),
      num: noticeList.value.length,
    },
    {
      name: computed(() => t('notice.bar[1]')),
      num: msgList.value.length,
    },
    {
      name: computed(() => t('notice.bar[2]')),
      num: pendingList.value.length,
    },
  ])

  return {
    noticeList,
    msgList,
    pendingList,
    barList,
  }
}

// 样式管理
const useNotificationStyles = () => {
  const noticeStyleMap: Record<NoticeType, NoticeStyle> = {
    email: {
      icon: 'ri:mail-line',
      iconClass: 'bg-warning/12 text-warning',
    },
    message: {
      icon: 'ri:volume-down-line',
      iconClass: 'bg-success/12 text-success',
    },
    collection: {
      icon: 'ri:heart-3-line',
      iconClass: 'bg-danger/12 text-danger',
    },
    user: {
      icon: 'ri:volume-down-line',
      iconClass: 'bg-info/12 text-info',
    },
    notice: {
      icon: 'ri:notification-3-line',
      iconClass: 'bg-theme/12 text-theme',
    },
  }

  const getNoticeStyle = (type: NoticeType): NoticeStyle => {
    const defaultStyle: NoticeStyle = {
      icon: 'ri:arrow-right-circle-line',
      iconClass: 'bg-theme/12 text-theme',
    }

    return noticeStyleMap[type] || defaultStyle
  }

  return {
    getNoticeStyle,
  }
}

// 动画管理
const useNotificationAnimation = () => {
  const showNotice = (open: boolean) => {
    if (open) {
      visible.value = true
      setTimeout(() => {
        show.value = true
      }, 5)
    } else {
      show.value = false
      setTimeout(() => {
        visible.value = false
      }, 350)
    }
  }

  return {
    showNotice,
  }
}

// 标签页管理
const useTabManagement = (
  noticeList: Ref<NoticeItem[]>,
  msgList: Ref<MessageItem[]>,
  pendingList: Ref<PendingItem[]>,
  businessHandlers: {
    handleNoticeAll: () => void
    handleMsgAll: () => void
    handlePendingAll: () => void
  },
) => {
  const changeBar = (index: number) => {
    console.log('切换标签页:', index, '当前:', barActiveIndex.value)

    barActiveIndex.value = index
    // 强制触发更新
    nextTick(() => {
      console.log('切换后:', barActiveIndex.value)
    })
  }

  // 检查当前标签页是否为空
  const currentTabIsEmpty = computed(() => {
    const tabDataMap = [noticeList.value, msgList.value, pendingList.value]

    const currentData = tabDataMap[barActiveIndex.value]
    return currentData && currentData.length === 0
  })

  const handleViewAll = () => {
    // 查看全部处理器映射
    const viewAllHandlers: Record<number, () => void> = {
      0: businessHandlers.handleNoticeAll,
      1: businessHandlers.handleMsgAll,
      2: businessHandlers.handlePendingAll,
    }

    const handler = viewAllHandlers[barActiveIndex.value]
    handler?.()

    // 关闭通知面板
    emit('update:value', false)
  }

  return {
    changeBar,
    currentTabIsEmpty,
    handleViewAll,
  }
}

// 业务逻辑处理
const useBusinessLogic = () => {
  const handleNoticeAll = () => {
    // 处理查看全部通知
    console.log('查看全部通知')
  }

  const handleMsgAll = () => {
    // 处理查看全部消息
    console.log('查看全部消息')
  }

  const handlePendingAll = () => {
    // 处理查看全部待办
    console.log('查看全部待办')
  }

  return {
    handleNoticeAll,
    handleMsgAll,
    handlePendingAll,
  }
}

// 组合所有逻辑
const { noticeList, msgList, pendingList, barList } = useNotificationData()
const { getNoticeStyle } = useNotificationStyles()
const { showNotice } = useNotificationAnimation()
const { handleNoticeAll, handleMsgAll, handlePendingAll } = useBusinessLogic()
const { changeBar, currentTabIsEmpty, handleViewAll } = useTabManagement(noticeList, msgList, pendingList, {
  handleNoticeAll,
  handleMsgAll,
  handlePendingAll,
})

// 监听属性变化
watch(
  () => props.value,
  (newValue) => {
    showNotice(newValue)
  },
)
</script>
<template>
  <div
    class="notification-panel w-90 h-125 shadow-xl!"
    :style="{
      transform: show ? 'scaleY(1)' : 'scaleY(0.9)',
      opacity: show ? 1 : 0,
    }"
    v-show="visible"
    @click.stop
  >
    <div class="notification-header">
      <span class="title">{{ $t('notice.title') }}</span>
      <span class="btn-read"> {{ $t('notice.btnRead') }} </span>
    </div>

    <ul class="notification-tabs">
      <li
        v-for="(item, index) in barList"
        :key="index"
        :class="{ 'bar-active': barActiveIndex === index }"
        @click="changeBar(index)"
      >
        {{ item.name }} ({{ item.num }})
      </li>
    </ul>

    <div class="notification-content">
      <div class="notification-list scrollbar-thin">
        <!-- 通知 -->
        <ul v-show="barActiveIndex === 0" class="notification-items">
          <li v-for="(item, index) in noticeList" :key="index" class="notification-item hover:bg-g-200/60">
            <div class="icon-wrapper" :class="[getNoticeStyle(item.type).iconClass]">
              <SvgIcon class="text-lg bg-transparent!" :icon="getNoticeStyle(item.type).icon" />
            </div>
            <div class="item-content w-[calc(100%-45px)]">
              <h4 class="item-title text-sm font-normal leading-5.5 text-g-900">{{ item.title }}</h4>
              <p class="item-time mt-1.5 text-xs text-g-500">{{ item.time }}</p>
            </div>
          </li>
        </ul>

        <!-- 消息 -->
        <ul v-show="barActiveIndex === 1" class="message-items">
          <li v-for="(item, index) in msgList" :key="index" class="message-item last:border-b-0 hover:bg-g-200/60">
            <div class="w-9 h-9">
              <img :src="item.avatar" class="w-full h-full rounded-lg" alt="" />
            </div>
            <div class="item-content">
              <h4 class="item-title leading-5.5">{{ item.title }}</h4>
              <p class="item-time">{{ item.time }}</p>
            </div>
          </li>
        </ul>

        <!-- 待办 -->
        <ul v-show="barActiveIndex === 2" class="pending-items">
          <li
            v-for="(item, index) in pendingList"
            :key="index"
            class="pending-item box-border px-5 py-3.5 last:border-b-0"
          >
            <h4 class="item-title">{{ item.title }}</h4>
            <p class="item-time text-xs text-g-500">{{ item.time }}</p>
          </li>
        </ul>

        <!-- 空状态 -->
        <div v-show="currentTabIsEmpty" class="empty-state text-center bg-transparent!">
          <SvgIcon icon="system-uicons:inbox" class="text-5xl" />
          <p class="empty-text mt-3.5 text-xs bg-transparent!">
            {{ $t('notice.text[0]') }}{{ barList[barActiveIndex].name }}
          </p>
        </div>
      </div>

      <div class="view-all">
        <el-button type="primary" plain class="view-all-btn" @click="handleViewAll" v-ripple>
          {{ $t('notice.viewAll') }}
        </el-button>
      </div>
    </div>

    <div class="h-25"></div>
  </div>
</template>

<style scoped lang="scss">
.notification-panel {
  position: absolute;
  top: 3.625rem;
  right: 1.25rem;
  z-index: 1000;
  overflow: hidden;
  background-color: var(--default-box-color);
  border: 1px solid var(--card-border);
  border-radius: var(--custom-radius);
  transform-origin: top;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: top, left;
}

@media (width < 640px) {
  .notification-panel {
    top: 65px;
    right: 0;
    width: 100%;
    height: 80vh;
  }
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-inline: 0.875rem;
  margin-top: 0.875rem;
  color: var(--color-g-800);

  .title {
    font-size: 1rem;
    font-weight: 500;
  }

  .btn-read {
    padding: 0.25rem 0.375rem;
    font-size: 0.75rem;
    cursor: pointer;
    user-select: none;
    border-radius: 0.25rem;
    transition: all 0.2s ease;

    &:hover {
      color: var(--color-g-900); // 悬停文字色
      background-color: var(--color-g-200); // 悬停背景色
    }
  }
}

.notification-tabs {
  display: flex;
  gap: 1.25rem;
  align-items: flex-end;
  width: 100%;
  height: 3.125rem;
  padding-inline: 0.875rem;
  color: var(--color-g-700);
  user-select: none;
  border-bottom: 1px solid var(--default-border);

  li {
    height: 3rem;
    overflow: hidden;
    font-size: 13px;
    line-height: 3rem;
    cursor: pointer;

    &.bar-active {
      color: var(--theme-color);
      border-bottom: 2px solid var(--theme-color);
    }
  }
}

.notification-content {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100% - 95px);

  .notification-list {
    height: calc(100% - 60px);
    padding: 0.5rem 0;
    overflow-y: auto;

    .notification-item,
    .message-item,
    .pending-item {
      display: flex;
      gap: 0.875rem;
      align-items: center;
      padding: 0.875rem;
      cursor: pointer;
      transition: background-color 0.2s;

      &:last-child {
        border-bottom: none;
      }
    }

    .icon-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 2.25rem;
      height: 2.25rem;
      border-radius: 0.5rem;
    }

    .item-content {
      flex: 1;
      min-width: 0;
    }

    .item-title {
      font-size: 0.875rem;
      font-weight: 400;
    }

    .item-time {
      margin: 0.375rem 0 0;
      font-size: 0.75rem;
      color: var(--color-g-500);
    }
  }

  .empty-state {
    position: relative;
    height: 100%;
    margin-top: 0.875rem;
    color: var(--color-g-500);
    text-align: center;
    background-color: transparent;

    .empty-text {
      margin-top: 0.875rem;
      font-size: 0.75rem;
    }
  }

  .view-all {
    position: relative;
    width: 100%;
    padding-inline: 0.875rem;

    .view-all-btn {
      width: 100%;
      margin-top: 0.75rem;
    }
  }
}

.scrollbar-thin::-webkit-scrollbar {
  width: 5px !important;
}

.dark .scrollbar-thin::-webkit-scrollbar-track {
  background-color: var(--default-box-color);
}

.dark .scrollbar-thin::-webkit-scrollbar-thumb {
  background-color: #222 !important;
}
</style>
