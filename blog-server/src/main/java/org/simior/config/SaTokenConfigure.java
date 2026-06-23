package org.simior.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 配置类
 *
 * <p>采用方法感知的路由鉴权策略：
 * <ul>
 *   <li>GET 请求：放行公开读取接口（文章列表、详情、分类、标签等）</li>
 *   <li>POST/PUT/DELETE 请求：仅放行认证接口（登录、注册、检查），其余均需登录</li>
 * </ul>
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器，按 HTTP 方法分别配置鉴权规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // GET 请求：放行公开读取接口
                    SaRouter.match(SaHttpMethod.GET)
                            .match("/**")
                            .notMatch(publicReadPaths())
                            .check(r -> StpUtil.checkLogin());

                    // POST/PUT/DELETE 请求：仅放行认证接口，其余均需登录
                    SaRouter.match(SaHttpMethod.POST, SaHttpMethod.PUT, SaHttpMethod.DELETE)
                            .match("/**")
                            .notMatch(authOnlyPaths())
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }

    /**
     * GET 请求免登录路径（公开读取接口）
     * <p>
     * 仅适用于 GET 方法，写操作（POST/PUT/DELETE）不在此列表中。
     */
    public List<String> publicReadPaths() {
        return Arrays.asList(
                // 认证相关
                "/v1/auth/login",
                "/v1/auth/register",
                "/v1/auth/check",
                // 博客公开读取
                "/v1/banners",
                "/v1/articles",
                "/v1/articles/hot",
                "/v1/articles/recommend",
                "/v1/articles/*",
                "/v1/categories",
                "/v1/categories/*",
                "/v1/tags",
                "/v1/tags/*",
                "/v1/articles/*/comments"
        );
    }

    /**
     * 写操作免登录路径（仅限认证接口）
     * <p>
     * POST/PUT/DELETE 请求中，只有登录、注册、检查接口不需要登录。
     * 其余写操作（发布文章、编辑、删除、点赞等）必须登录。
     */
    public List<String> authOnlyPaths() {
        return Arrays.asList(
                "/v1/auth/login",
                "/v1/auth/register",
                "/v1/auth/check"
        );
    }
}
