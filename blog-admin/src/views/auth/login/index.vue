<!-- 登录页面 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1" style="background-color: var(--default-bg-color)">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <h3 class="title">{{ $t('login.title') }}</h3>
          <p class="sub-title">{{ $t('login.subTitle') }}</p>
          <ElForm
            ref="formRef"
            :key="formKey"
            :model="formData"
            :rules="rules"
            style="margin-top: 25px"
            @keyup.enter="handleSubmit"
          >
            <ElFormItem prop="username">
              <ElInput
                v-model.trim="formData.username"
                class="custom-height"
                :placeholder="$t('login.placeholder.username')"
              />
            </ElFormItem>
            <ElFormItem prop="password">
              <ElInput
                v-model.trim="formData.password"
                class="custom-height"
                :placeholder="$t('login.placeholder.password')"
                type="password"
                autocomplete="off"
                show-password
              />
            </ElFormItem>

            <!-- 图形验证码 -->
            <div class="relative pb-5 mt-6">
              <ElFormItem prop="captchaCode">
                <div class="flex items-center gap-3 w-full">
                  <ElInput
                    v-model.trim="formData.captchaCode"
                    class="custom-height flex-1"
                    placeholder="请输入验证码"
                    @keyup.enter="handleSubmit"
                  />
                  <div
                    class="captcha-image cursor-pointer shrink-0 h-10 rounded overflow-hidden border border-gray-200"
                    title="点击刷新验证码"
                    @click="refreshCaptcha"
                  >
                    <img
                      v-if="captchaImage"
                      :src="captchaImage"
                      alt="验证码"
                      class="h-full w-30 object-cover"
                    />
                    <div
                      v-else
                      class="h-full w-30 flex items-center justify-center text-gray-400 text-xs bg-gray-50"
                    >
                      加载中...
                    </div>
                  </div>
                </div>
              </ElFormItem>
            </div>

            <div class="flex-cb mt-2 text-sm">
              <ElCheckbox v-model="formData.rememberPassword">{{
                $t('login.rememberPwd')
              }}</ElCheckbox>
              <RouterLink class="text-theme" :to="{ name: 'ForgetPassword' }">{{
                $t('login.forgetPwd')
              }}</RouterLink>
            </div>

            <div style="margin-top: 30px">
              <ElButton
                v-ripple
                class="w-full custom-height"
                type="primary"
                :loading="loading"
                @click="handleSubmit"
              >
                {{ $t('login.btnText') }}
              </ElButton>
            </div>

            <div class="mt-5 text-sm text-gray-600">
              <span>{{ $t('login.noAccount') }}</span>
              <RouterLink class="text-theme" :to="{ name: 'Register' }">{{
                $t('login.register')
              }}</RouterLink>
            </div>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import AppConfig from '@/config'
  import { useUserStore } from '@/stores'
  import { useI18n } from 'vue-i18n'
  import { HttpError } from '@/utils/http/error'
  import { authApi } from '@/api'
  import { ElNotification, type FormInstance, type FormRules } from 'element-plus'

  defineOptions({ name: 'Login' })

  const { t, locale } = useI18n()
  const formKey = ref(0)

  // 监听语言切换，重置表单
  watch(locale, () => {
    formKey.value++
  })

  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()

  const systemName = AppConfig.systemInfo.name
  const formRef = ref<FormInstance>()

  // 验证码状态
  const captchaId = ref('')
  const captchaImage = ref('')

  const formData = reactive({
    username: 'admin',
    password: '123456',
    captchaCode: '',
    rememberPassword: true
  })

  const rules = computed<FormRules>(() => ({
    username: [{ required: true, message: t('login.placeholder.username'), trigger: 'blur' }],
    password: [{ required: true, message: t('login.placeholder.password'), trigger: 'blur' }],
    captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
  }))

  const loading = ref(false)

  onMounted(() => {
    refreshCaptcha()
  })

  // 获取验证码
  const refreshCaptcha = async () => {
    try {
      const data = await authApi.getCaptcha()
      if (data) {
        captchaId.value = data.captchaId
        captchaImage.value = data.captchaImage
        formData.captchaCode = ''
      }
    } catch (error) {
      console.error('[Captcha] Failed to load captcha:', error)
    }
  }

  // 登录
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      // 表单验证
      const valid = await formRef.value.validate()
      if (!valid) return

      loading.value = true

      // 登录请求
      const { username, password, captchaCode } = formData

      const { token } = await authApi.login({
        username,
        password,
        captchaId: captchaId.value,
        captchaCode
      })

      // 验证token
      if (!token) {
        throw new Error('Login failed - no token received')
      }

      // 存储 token 和登录状态
      userStore.setToken(token)
      userStore.setLoginStatus(true)

      // 登录成功处理
      showLoginSuccessNotice()

      // 获取 redirect 参数，如果存在则跳转到指定页面，否则跳转到首页
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error) {
      if (error instanceof HttpError) {
        // HttpError 已在 HTTP 拦截器中统一处理
        // 登录失败时刷新验证码
        refreshCaptcha()
      } else {
        console.error('[Login] Unexpected error:', error)
        refreshCaptcha()
      }
    } finally {
      loading.value = false
    }
  }

  // 登录成功提示
  const showLoginSuccessNotice = () => {
    setTimeout(() => {
      ElNotification({
        title: t('login.success.title'),
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: `${t('login.success.message')}, ${systemName}!`
      })
    }, 1000)
  }
</script>

<style scoped>
  @reference '@/styles/tailwind.css';

  /* 授权页右侧区域 */
  .auth-right-wrap {
    @apply absolute inset-0 w-[440px] h-[650px] py-[5px] m-auto overflow-hidden
  max-sm:px-7 max-sm:w-full
  animate-[slideInRight_0.6s_cubic-bezier(0.25,0.46,0.45,0.94)_forwards]
  max-md:animate-none;

    .form {
      @apply h-full py-[40px];
    }

    .title {
      @apply text-g-900 text-4xl font-semibold max-md:text-3xl max-sm:pt-10;
    }

    .sub-title {
      @apply mt-[10px] text-g-600 text-sm;
    }

    .custom-height {
      @apply !h-[40px];
    }
  }

  /* 滑入动画 */
  @keyframes slideInRight {
    from {
      opacity: 0;
      transform: translateX(30px);
    }

    to {
      opacity: 1;
      transform: translateX(0);
    }
  }
</style>
