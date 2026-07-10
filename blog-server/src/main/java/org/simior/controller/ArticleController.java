package org.simior.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.model.dto.ArticleDTO;
import org.simior.model.vo.ArticleListVO;
import org.simior.model.vo.ArticleVO;
import org.simior.service.ArticleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章管理控制器
 * <p>
 * 提供文章的 CRUD 操作、列表查询、热门/推荐文章获取，以及点赞和收藏功能。
 * URL 前缀：/v1/articles，遵循 RESTful 风格使用复数名词。
 * <p>
 * 公开接口（无需登录）：列表、详情、热门、推荐
 * 需 author 或 admin 角色：发布、编辑、删除
 * 需登录（任意角色）：点赞、收藏、我的文章
 */
@RestController
@RequestMapping("/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 发布文章（需 author 或 admin 角色）
     *
     * @param articleDTO 文章数据
     * @return 新文章ID
     */
    @SaCheckRole({"admin", "author"})
    @PostMapping
    public Result<Long> publishArticle(@Valid @RequestBody ArticleDTO articleDTO) {
        Long articleId = articleService.publishArticle(articleDTO);
        return Result.success("发布成功", articleId);
    }

    /**
     * 删除文章（仅作者本人或管理员可操作）
     *
     * @param id 文章ID
     * @return 操作结果
     */
    @SaCheckRole({"admin", "author"})
    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success("删除成功");
    }

    /**
     * 编辑文章（仅作者本人或管理员可操作）
     *
     * @param id         文章ID
     * @param articleDTO 文章数据
     * @return 操作结果
     */
    @SaCheckRole({"admin", "author"})
    @PutMapping("/{id}")
    public Result<String> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleDTO articleDTO) {
        articleDTO.setId(id);
        articleService.updateArticle(articleDTO);
        return Result.success("更新成功");
    }

    /**
     * 获取文章详情（公开接口，同时记录浏览量）
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO articleVO = articleService.getArticleDetail(id);
        articleService.recordView(id);
        return Result.success(articleVO);
    }

    /**
     * 分页查询文章列表（公开接口）
     *
     * @param pageNum    当前页码，默认1
     * @param pageSize   每页条数，默认10，最大100
     * @param categoryId 分类ID（可选筛选）
     * @param tagId      标签ID（可选筛选）
     * @param keyword    搜索关键词（可选）
     * @return 分页文章列表
     */
    @GetMapping
    public Result<Page<ArticleListVO>> getArticleList(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        Page<ArticleListVO> page = articleService.getArticleList(pageNum, pageSize, categoryId, tagId, keyword);
        return Result.success(page);
    }

    /**
     * 获取热门文章（公开接口，按浏览量排序）
     *
     * @param limit 返回数量，默认5，最大50
     * @return 热门文章列表
     */
    @GetMapping("/hot")
    public Result<List<ArticleListVO>> getHotArticles(@RequestParam(defaultValue = "5") Integer limit) {
        limit = Math.min(Math.max(limit, 1), 50);
        List<ArticleListVO> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取推荐文章（公开接口，按置顶文章）
     *
     * @param limit 返回数量，默认3，最大50
     * @return 推荐文章列表
     */
    @GetMapping("/recommend")
    public Result<List<ArticleListVO>> getRecommendArticles(@RequestParam(defaultValue = "3") Integer limit) {
        limit = Math.min(Math.max(limit, 1), 50);
        List<ArticleListVO> articles = articleService.getRecommendArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取我的文章列表（需登录，分页）
     *
     * @param pageNum  当前页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 我的文章分页列表
     */
    @GetMapping("/mine")
    public Result<Page<ArticleListVO>> getMyArticles(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        Page<ArticleListVO> page = articleService.getMyArticles(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 点赞文章（幂等操作，重复调用不会重复点赞）
     *
     * @param id 文章ID
     * @return 操作结果
     */
    @PostMapping("/{id}/like")
    public Result<String> likeArticle(@PathVariable Long id) {
        articleService.likeArticle(id);
        return Result.success("点赞成功");
    }

    /**
     * 取消点赞文章（幂等操作）
     *
     * @param id 文章ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/like")
    public Result<String> unlikeArticle(@PathVariable Long id) {
        articleService.unlikeArticle(id);
        return Result.success("取消点赞成功");
    }

    /**
     * 收藏文章（幂等操作）
     *
     * @param id 文章ID
     * @return 操作结果
     */
    @PostMapping("/{id}/collect")
    public Result<String> collectArticle(@PathVariable Long id) {
        articleService.collectArticle(id);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏文章（幂等操作）
     *
     * @param id 文章ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/collect")
    public Result<String> uncollectArticle(@PathVariable Long id) {
        articleService.uncollectArticle(id);
        return Result.success("取消收藏成功");
    }
}
