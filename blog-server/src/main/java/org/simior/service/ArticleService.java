package org.simior.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.simior.model.dto.ArticleDTO;
import org.simior.model.entity.BlogArticle;
import org.simior.model.vo.ArticleListVO;
import org.simior.model.vo.ArticleVO;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService extends IService<BlogArticle> {

    /**
     * 发布文章
     *
     * @param articleDTO 文章信息
     * @return 文章ID
     */
    Long publishArticle(ArticleDTO articleDTO);

    /**
     * 删除文章
     *
     * @param id 文章ID
     */
    void deleteArticle(Long id);

    /**
     * 编辑文章
     *
     * @param articleDTO 文章信息
     */
    void updateArticle(ArticleDTO articleDTO);


    /**
     * 获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO getArticleDetail(Long id);

    /**
     * 分页查询文章列表
     *
     * @param pageNum    当前页
     * @param pageSize   每页大小
     * @param categoryId 分类ID
     * @param tagId      标签ID
     * @param keyword    关键词
     * @return 文章列表
     */
    Page<ArticleListVO> getArticleList(Long pageNum, Long pageSize, Long categoryId, Long tagId, String keyword);

    /**
     * 获取热门文章
     *
     * @param limit 数量
     * @return 文章列表
     */
    List<ArticleListVO> getHotArticles(Integer limit);

    /**
     * 获取推荐文章
     *
     * @param limit 数量
     * @return 文章列表
     */
    List<ArticleListVO> getRecommendArticles(Integer limit);

    /**
     * 获取我的文章列表
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @return 文章列表
     */
    Page<ArticleListVO> getMyArticles(Long pageNum, Long pageSize);

    /**
     * 文章点赞（幂等操作，重复调用不会重复点赞）
     *
     * @param articleId 文章ID
     */
    void likeArticle(Long articleId);

    /**
     * 取消文章点赞（幂等操作）
     *
     * @param articleId 文章ID
     */
    void unlikeArticle(Long articleId);

    /**
     * 文章收藏（幂等操作，重复调用不会重复收藏）
     *
     * @param articleId 文章ID
     */
    void collectArticle(Long articleId);

    /**
     * 取消文章收藏（幂等操作）
     *
     * @param articleId 文章ID
     */
    void uncollectArticle(Long articleId);

    /**
     * 记录浏览历史
     *
     * @param articleId 文章ID
     */
    void recordView(Long articleId);
}
