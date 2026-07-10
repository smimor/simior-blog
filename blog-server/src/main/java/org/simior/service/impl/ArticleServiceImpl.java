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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, BlogArticle> implements ArticleService {

    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final long LOCK_TIMEOUT_SECONDS = 30;

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCollectMapper articleCollectMapper;
    private final ViewHistoryMapper viewHistoryMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final StringRedisTemplate stringRedisTemplate;

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
        String lockKey = LOCK_KEY_PREFIX + "like:" + userId + ":" + articleId;
        tryLock(lockKey);
        try {
            BlogArticle article = articleMapper.selectById(articleId);
            if (article == null) throw new BusinessException("文章不存在");

            LambdaQueryWrapper<BlogArticleLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogArticleLike::getArticleId, articleId)
                    .eq(BlogArticleLike::getUserId, userId);
            if (articleLikeMapper.selectOne(wrapper) != null) return;

            BlogArticleLike like = new BlogArticleLike();
            like.setArticleId(articleId);
            like.setUserId(userId);
            articleLikeMapper.insert(like);
            articleMapper.incrementLikeCount(articleId);
        } finally {
            unlock(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = LOCK_KEY_PREFIX + "like:" + userId + ":" + articleId;
        tryLock(lockKey);
        try {
            BlogArticle article = articleMapper.selectById(articleId);
            if (article == null) throw new BusinessException("文章不存在");

            LambdaQueryWrapper<BlogArticleLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogArticleLike::getArticleId, articleId)
                    .eq(BlogArticleLike::getUserId, userId);
            BlogArticleLike existLike = articleLikeMapper.selectOne(wrapper);
            if (existLike == null) return;

            articleLikeMapper.deleteById(existLike.getId());
            articleMapper.decrementLikeCount(articleId);
        } finally {
            unlock(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = LOCK_KEY_PREFIX + "collect:" + userId + ":" + articleId;
        tryLock(lockKey);
        try {
            BlogArticle article = articleMapper.selectById(articleId);
            if (article == null) throw new BusinessException("文章不存在");

            LambdaQueryWrapper<BlogArticleCollect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogArticleCollect::getArticleId, articleId)
                    .eq(BlogArticleCollect::getUserId, userId);
            if (articleCollectMapper.selectOne(wrapper) != null) return;

            BlogArticleCollect collect = new BlogArticleCollect();
            collect.setArticleId(articleId);
            collect.setUserId(userId);
            articleCollectMapper.insert(collect);
            articleMapper.incrementCollectCount(articleId);
        } finally {
            unlock(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = LOCK_KEY_PREFIX + "collect:" + userId + ":" + articleId;
        tryLock(lockKey);
        try {
            BlogArticle article = articleMapper.selectById(articleId);
            if (article == null) throw new BusinessException("文章不存在");

            LambdaQueryWrapper<BlogArticleCollect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogArticleCollect::getArticleId, articleId)
                    .eq(BlogArticleCollect::getUserId, userId);
            BlogArticleCollect existCollect = articleCollectMapper.selectOne(wrapper);
            if (existCollect == null) return;

            articleCollectMapper.deleteById(existCollect.getId());
            articleMapper.decrementCollectCount(articleId);
        } finally {
            unlock(lockKey);
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

        // 浏览量去重：同一 IP 对同一文章 24 小时内只计一次
        LambdaQueryWrapper<BlogViewHistory> dedupWrapper = new LambdaQueryWrapper<>();
        dedupWrapper.eq(BlogViewHistory::getArticleId, articleId)
                .eq(BlogViewHistory::getIpAddress, clientIp)
                .ge(BlogViewHistory::getCreateTime, java.time.LocalDateTime.now().minusHours(24));
        Long recentViews = viewHistoryMapper.selectCount(dedupWrapper);
        if (recentViews > 0) {
            // 24 小时内已有浏览记录，只插入历史不递增浏览量
            viewHistoryMapper.insert(viewHistory);
            return;
        }

        viewHistoryMapper.insert(viewHistory);

        // 原子更新文章浏览量
        articleMapper.incrementViewCount(articleId);
    }

    /**
     * 获取客户端真实 IP 地址
     * 优先取 X-Real-IP（Nginx 设置，不可被客户端伪造），
     * X-Forwarded-For 可被客户端伪造，仅作为最后兜底。
     */
    private String getClientIp() {
        jakarta.servlet.http.HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 优先 X-Real-IP（Nginx 的 proxy_set_header X-Real-IP 只取第一个代理的真实 IP）
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取最后一个（最靠近服务端的代理）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[ip.split(",").length - 1].trim();
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

    /**
     * Redis 分布式锁：获取锁（SETNX + TTL，30 秒自动过期防止死锁）
     */
    private void tryLock(String lockKey) {
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException("操作过于频繁，请稍后重试");
        }
    }

    /**
     * Redis 分布式锁：释放锁
     */
    private void unlock(String lockKey) {
        stringRedisTemplate.delete(lockKey);
    }

}
