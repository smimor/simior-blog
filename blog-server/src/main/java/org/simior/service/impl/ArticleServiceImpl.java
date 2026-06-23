package org.simior.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simior.common.exception.BusinessException;
import org.simior.mapper.*;
import org.simior.model.dto.ArticleDTO;
import org.simior.model.entity.*;
import org.simior.model.vo.ArticleListVO;
import org.simior.model.vo.ArticleVO;
import org.simior.model.vo.TagVO;
import org.simior.service.ArticleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, BlogArticle> implements ArticleService {

    private static final ConcurrentHashMap<String, Object> LOCK_MAP = new ConcurrentHashMap<>();

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCollectMapper articleCollectMapper;
    private final ViewHistoryMapper viewHistoryMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDTO articleDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建文章
        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(articleDTO, article);
        article.setUserId(userId);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        article.setVersion(1);
        article.setAuditStatus(1); // 默认审核通过
        article.setIsTop(articleDTO.getIsTop() != null ? articleDTO.getIsTop() : 0);
        article.setIsDraft(articleDTO.getIsDraft() != null ? articleDTO.getIsDraft() : 0);

        articleMapper.insert(article);

        // 保存文章标签关联
        saveArticleTags(article.getId(), articleDTO.getTagIds());

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文章
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 校验权限
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此文章");
        }

        // 清理标签关联数据
        LambdaQueryWrapper<BlogArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(BlogArticleTag::getArticleId, id);
        articleTagMapper.delete(tagWrapper);

        // 清理点赞数据
        LambdaQueryWrapper<BlogArticleLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(BlogArticleLike::getArticleId, id);
        articleLikeMapper.delete(likeWrapper);

        // 清理收藏数据
        LambdaQueryWrapper<BlogArticleCollect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(BlogArticleCollect::getArticleId, id);
        articleCollectMapper.delete(collectWrapper);

        // 清理评论数据（逻辑删除）
        LambdaQueryWrapper<BlogComment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(BlogComment::getArticleId, id);
        commentMapper.delete(commentWrapper);

        // 清理浏览记录
        LambdaQueryWrapper<BlogViewHistory> viewWrapper = new LambdaQueryWrapper<>();
        viewWrapper.eq(BlogViewHistory::getArticleId, id);
        viewHistoryMapper.delete(viewWrapper);

        // 逻辑删除文章
        articleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleDTO articleDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文章
        BlogArticle article = articleMapper.selectById(articleDTO.getId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 校验权限
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此文章");
        }

        // 手动设置允许更新的字段，防止通过 BeanUtils 全量拷贝篡改敏感字段
        if (articleDTO.getTitle() != null) {
            article.setTitle(articleDTO.getTitle());
        }
        if (articleDTO.getSummary() != null) {
            article.setSummary(articleDTO.getSummary());
        }
        if (articleDTO.getCoverImage() != null) {
            article.setCoverImage(articleDTO.getCoverImage());
        }
        if (articleDTO.getContent() != null) {
            article.setContent(articleDTO.getContent());
        }
        if (articleDTO.getCategoryId() != null) {
            article.setCategoryId(articleDTO.getCategoryId());
        }
        if (articleDTO.getIsDraft() != null) {
            article.setIsDraft(articleDTO.getIsDraft());
        }
        // 注意：isTop 不允许普通用户设置，仅管理员可操作
        // 如需管理员置顶功能，请添加权限校验后放开

        articleMapper.updateById(article);

        // 删除旧的标签关联
        LambdaQueryWrapper<BlogArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);

        // 保存新的标签关联
        saveArticleTags(article.getId(), articleDTO.getTagIds());
    }

    @Override
    public ArticleVO getArticleDetail(Long id) {
        // 查询文章
        BlogArticle article = articleMapper.selectById(id);
        if (article == null || article.getIsDraft() == 1) {
            throw new BusinessException("文章不存在");
        }

        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);

        // 查询作者信息
        SysUser author = userMapper.selectById(article.getUserId());
        if (author != null) {
            articleVO.setAuthorNickname(author.getNickname());
            articleVO.setAuthorAvatar(author.getAvatar());
        }

        // 查询分类信息
        if (article.getCategoryId() != null) {
            BlogCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                articleVO.setCategoryName(category.getCategoryName());
            }
        }

        // 批量查询标签列表（消除 N+1）
        LambdaQueryWrapper<BlogArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(BlogArticleTag::getArticleId, id);
        List<BlogArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);

        List<TagVO> tags = new ArrayList<>();
        if (!articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream()
                    .map(BlogArticleTag::getTagId)
                    .collect(Collectors.toList());
            List<BlogTag> tagList = tagMapper.selectBatchIds(tagIds);
            for (BlogTag tag : tagList) {
                TagVO tagVO = new TagVO();
                BeanUtils.copyProperties(tag, tagVO);
                tags.add(tagVO);
            }
        }
        articleVO.setTags(tags);

        // 查询当前用户是否点赞（仅捕获 NotLoginException，不吞掉其他异常）
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (NotLoginException e) {
            // 未登录用户
        }

        if (userId != null) {
            LambdaQueryWrapper<BlogArticleLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(BlogArticleLike::getArticleId, id)
                    .eq(BlogArticleLike::getUserId, userId);
            Long likeCount = articleLikeMapper.selectCount(likeWrapper);
            articleVO.setIsLiked(likeCount > 0);

            // 查询当前用户是否收藏
            LambdaQueryWrapper<BlogArticleCollect> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(BlogArticleCollect::getArticleId, id)
                    .eq(BlogArticleCollect::getUserId, userId);
            Long collectCount = articleCollectMapper.selectCount(collectWrapper);
            articleVO.setIsCollected(collectCount > 0);
        } else {
            articleVO.setIsLiked(false);
            articleVO.setIsCollected(false);
        }

        return articleVO;
    }

    @Override
    public Page<ArticleListVO> getArticleList(Long pageNum, Long pageSize, Long categoryId, Long tagId, String keyword) {
        Page<BlogArticle> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的文章
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.eq(BlogArticle::getAuditStatus, 1);

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(BlogArticle::getCategoryId, categoryId);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BlogArticle::getTitle, keyword)
                    .or().like(BlogArticle::getSummary, keyword));
        }

        // 标签筛选
        if (tagId != null) {
            LambdaQueryWrapper<BlogArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(BlogArticleTag::getTagId, tagId);
            List<BlogArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            List<Long> articleIds = articleTags.stream()
                    .map(BlogArticleTag::getArticleId)
                    .collect(Collectors.toList());
            if (!articleIds.isEmpty()) {
                wrapper.in(BlogArticle::getId, articleIds);
            } else {
                // 没有文章匹配该标签
                return new Page<>(pageNum, pageSize);
            }
        }

        // 按置顶和创建时间排序
        wrapper.orderByDesc(BlogArticle::getIsTop);
        wrapper.orderByDesc(BlogArticle::getCreateTime);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);

        // 批量转换为VO
        Page<ArticleListVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(articlePage.getTotal());
        voPage.setRecords(batchConvertToListVO(articlePage.getRecords()));

        return voPage;
    }

    @Override
    public List<ArticleListVO> getHotArticles(Integer limit) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.eq(BlogArticle::getAuditStatus, 1);
        wrapper.orderByDesc(BlogArticle::getViewCount);
        wrapper.last("LIMIT " + Math.max(0, limit));

        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return batchConvertToListVO(articles);
    }

    @Override
    public List<ArticleListVO> getRecommendArticles(Integer limit) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.eq(BlogArticle::getAuditStatus, 1);
        wrapper.eq(BlogArticle::getIsTop, 1);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
        wrapper.last("LIMIT " + Math.max(0, limit));

        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return batchConvertToListVO(articles);
    }

    @Override
    public Page<ArticleListVO> getMyArticles(Long pageNum, Long pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();

        Page<BlogArticle> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getUserId, userId);
        wrapper.orderByDesc(BlogArticle::getCreateTime);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);

        // 批量转换为VO
        Page<ArticleListVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(articlePage.getTotal());
        voPage.setRecords(batchConvertToListVO(articlePage.getRecords()));

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = "like:" + userId + ":" + articleId;
        Object lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new Object());
        try {
            synchronized (lock) {
                // 校验文章存在
                BlogArticle article = articleMapper.selectById(articleId);
                if (article == null) {
                    throw new BusinessException("文章不存在");
                }

                // 查询是否已点赞
                LambdaQueryWrapper<BlogArticleLike> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BlogArticleLike::getArticleId, articleId)
                        .eq(BlogArticleLike::getUserId, userId);
                BlogArticleLike existLike = articleLikeMapper.selectOne(wrapper);

                if (existLike != null) {
                    // 已点赞，幂等返回
                    return;
                }

                // 点赞
                BlogArticleLike like = new BlogArticleLike();
                like.setArticleId(articleId);
                like.setUserId(userId);
                articleLikeMapper.insert(like);
                // 原子更新文章点赞数
                articleMapper.incrementLikeCount(articleId);
            }
        } finally {
            LOCK_MAP.remove(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = "like:" + userId + ":" + articleId;
        Object lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new Object());
        try {
            synchronized (lock) {
                // 校验文章存在
                BlogArticle article = articleMapper.selectById(articleId);
                if (article == null) {
                    throw new BusinessException("文章不存在");
                }

                // 查询是否已点赞
                LambdaQueryWrapper<BlogArticleLike> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BlogArticleLike::getArticleId, articleId)
                        .eq(BlogArticleLike::getUserId, userId);
                BlogArticleLike existLike = articleLikeMapper.selectOne(wrapper);

                if (existLike == null) {
                    // 未点赞，幂等返回
                    return;
                }

                // 取消点赞
                articleLikeMapper.deleteById(existLike.getId());
                // 原子更新文章点赞数
                articleMapper.decrementLikeCount(articleId);
            }
        } finally {
            LOCK_MAP.remove(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = "collect:" + userId + ":" + articleId;
        Object lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new Object());
        try {
            synchronized (lock) {
                // 校验文章存在
                BlogArticle article = articleMapper.selectById(articleId);
                if (article == null) {
                    throw new BusinessException("文章不存在");
                }

                // 查询是否已收藏
                LambdaQueryWrapper<BlogArticleCollect> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BlogArticleCollect::getArticleId, articleId)
                        .eq(BlogArticleCollect::getUserId, userId);
                BlogArticleCollect existCollect = articleCollectMapper.selectOne(wrapper);

                if (existCollect != null) {
                    // 已收藏，幂等返回
                    return;
                }

                // 收藏
                BlogArticleCollect collect = new BlogArticleCollect();
                collect.setArticleId(articleId);
                collect.setUserId(userId);
                articleCollectMapper.insert(collect);
                // 原子更新文章收藏数
                articleMapper.incrementCollectCount(articleId);
            }
        } finally {
            LOCK_MAP.remove(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = "collect:" + userId + ":" + articleId;
        Object lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new Object());
        try {
            synchronized (lock) {
                // 校验文章存在
                BlogArticle article = articleMapper.selectById(articleId);
                if (article == null) {
                    throw new BusinessException("文章不存在");
                }

                // 查询是否已收藏
                LambdaQueryWrapper<BlogArticleCollect> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BlogArticleCollect::getArticleId, articleId)
                        .eq(BlogArticleCollect::getUserId, userId);
                BlogArticleCollect existCollect = articleCollectMapper.selectOne(wrapper);

                if (existCollect == null) {
                    // 未收藏，幂等返回
                    return;
                }

                // 取消收藏
                articleCollectMapper.deleteById(existCollect.getId());
                // 原子更新文章收藏数
                articleMapper.decrementCollectCount(articleId);
            }
        } finally {
            LOCK_MAP.remove(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordView(Long articleId) {
        // 记录浏览历史
        BlogViewHistory viewHistory = new BlogViewHistory();
        viewHistory.setArticleId(articleId);

        try {
            Long userId = StpUtil.getLoginIdAsLong();
            viewHistory.setUserId(userId);
        } catch (NotLoginException e) {
            // 未登录用户
            viewHistory.setUserId(null);
        }

        // 记录客户端 IP 地址
        String clientIp = getClientIp();
        viewHistory.setIpAddress(clientIp);

        viewHistoryMapper.insert(viewHistory);

        // 原子更新文章浏览量
        articleMapper.incrementViewCount(articleId);
    }

    /**
     * 获取客户端真实 IP 地址
     */
    private String getClientIp() {
        jakarta.servlet.http.HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 保存文章标签关联数据
     *
     * @param articleId 文章ID
     * @param tagIds    标签ID列表
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<BlogArticleTag> tagEntities = tagIds.stream().map(tagId -> {
            BlogArticleTag articleTag = new BlogArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            return articleTag;
        }).collect(Collectors.toList());
        for (BlogArticleTag tag : tagEntities) {
            articleTagMapper.insert(tag);
        }
    }

    /**
     * 批量转换为列表VO（消除 N+1 查询）
     * <p>
     * 先收集所有 userId 和 categoryId，批量查询后构建 Map，
     * 再遍历文章列表填充 VO，将 O(2N+1) 查询降为 O(3)。
     */
    private List<ArticleListVO> batchConvertToListVO(List<BlogArticle> articles) {
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集所有需要查询的 ID
        Set<Long> userIds = articles.stream()
                .map(BlogArticle::getUserId)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream()
                .map(BlogArticle::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 批量查询并构建 Map
        Map<Long, SysUser> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        Map<Long, BlogCategory> categoryMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(BlogCategory::getId, c -> c));

        // 遍历填充 VO
        return articles.stream().map(article -> {
            ArticleListVO vo = new ArticleListVO();
            BeanUtils.copyProperties(article, vo);

            SysUser author = userMap.get(article.getUserId());
            if (author != null) {
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }

            if (article.getCategoryId() != null) {
                BlogCategory category = categoryMap.get(article.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getCategoryName());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

}
