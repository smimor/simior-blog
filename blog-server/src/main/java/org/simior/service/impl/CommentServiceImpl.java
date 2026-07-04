package org.simior.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.simior.common.exception.BusinessException;
import org.simior.mapper.ArticleMapper;
import org.simior.mapper.CommentLikeMapper;
import org.simior.mapper.CommentMapper;
import org.simior.mapper.UserMapper;
import org.simior.model.dto.CommentDTO;
import org.simior.model.entity.BlogArticle;
import org.simior.model.entity.BlogComment;
import org.simior.model.entity.BlogCommentLike;
import org.simior.model.entity.SysUser;
import org.simior.model.vo.CommentVO;
import org.simior.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, BlogComment> implements CommentService {

    private static final ConcurrentHashMap<String, Object> LOCK_MAP = new ConcurrentHashMap<>();

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishComment(CommentDTO commentDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验文章存在
        BlogArticle article = articleMapper.selectById(commentDTO.getArticleId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        BlogComment comment = new BlogComment();
        BeanUtils.copyProperties(commentDTO, comment);
        comment.setUserId(userId);
        comment.setLikeCount(0);
        comment.setAuditStatus(1);
        commentMapper.insert(comment);

        // 原子更新文章评论数
        if (commentDTO.getArticleId() != null) {
            articleMapper.incrementCommentCount(commentDTO.getArticleId());
        }

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        BlogComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            // 非评论作者，检查是否为管理员
            try {
                StpUtil.checkRole("admin");
            } catch (Exception e) {
                throw new BusinessException("无权删除此评论");
            }
        }
        commentMapper.deleteById(id);

        // 原子更新文章评论数
        if (comment.getArticleId() != null) {
            articleMapper.decrementCommentCount(comment.getArticleId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = "commentLike:" + userId + ":" + commentId;
        Object lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new Object());
        try {
            synchronized (lock) {
                // 校验评论存在
                BlogComment comment = commentMapper.selectById(commentId);
                if (comment == null) {
                    throw new BusinessException("评论不存在");
                }

                LambdaQueryWrapper<BlogCommentLike> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BlogCommentLike::getCommentId, commentId)
                        .eq(BlogCommentLike::getUserId, userId);
                BlogCommentLike existLike = commentLikeMapper.selectOne(wrapper);

                // 幂等：已点赞则直接返回，不重复操作
                if (existLike != null) {
                    return;
                }

                BlogCommentLike like = new BlogCommentLike();
                like.setCommentId(commentId);
                like.setUserId(userId);
                commentLikeMapper.insert(like);
                commentMapper.incrementLikeCount(commentId);
            }
        }
        // 不移除锁条目，避免竞态条件
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeComment(Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        String lockKey = "commentLike:" + userId + ":" + commentId;
        Object lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new Object());
        try {
            synchronized (lock) {
                // 校验评论存在
                BlogComment comment = commentMapper.selectById(commentId);
                if (comment == null) {
                    throw new BusinessException("评论不存在");
                }

                LambdaQueryWrapper<BlogCommentLike> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BlogCommentLike::getCommentId, commentId)
                        .eq(BlogCommentLike::getUserId, userId);
                BlogCommentLike existLike = commentLikeMapper.selectOne(wrapper);

                if (existLike != null) {
                    commentLikeMapper.deleteById(existLike.getId());
                    commentMapper.decrementLikeCount(commentId);
                }
            }
        }
        // 不移除锁条目，避免竞态条件
    }

    @Override
    public Page<CommentVO> getArticleComments(Long articleId, Long pageNum, Long pageSize) {
        Page<BlogComment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogComment::getArticleId, articleId);
        wrapper.isNull(BlogComment::getParentId);
        wrapper.eq(BlogComment::getAuditStatus, 1);
        wrapper.orderByDesc(BlogComment::getCreateTime);

        Page<BlogComment> commentPage = commentMapper.selectPage(page, wrapper);
        List<BlogComment> rootComments = commentPage.getRecords();

        if (rootComments.isEmpty()) {
            Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
            voPage.setTotal(commentPage.getTotal());
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        // 1. 一次性查询所有子评论（替代逐条 root 查询子评论的 N+1）
        List<Long> rootIds = rootComments.stream()
                .map(BlogComment::getId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<BlogComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.in(BlogComment::getParentId, rootIds);
        childWrapper.eq(BlogComment::getAuditStatus, 1);
        childWrapper.orderByAsc(BlogComment::getCreateTime);
        List<BlogComment> allChildren = commentMapper.selectList(childWrapper);

        // 按 parentId 分组
        Map<Long, List<BlogComment>> childrenMap = allChildren.stream()
                .collect(Collectors.groupingBy(BlogComment::getParentId));

        // 2. 收集所有 userId，批量查询用户信息
        Set<Long> userIds = new HashSet<>();
        List<BlogComment> allComments = new ArrayList<>(rootComments);
        allComments.addAll(allChildren);
        for (BlogComment c : allComments) {
            if (c.getUserId() != null) userIds.add(c.getUserId());
            if (c.getReplyUserId() != null) userIds.add(c.getReplyUserId());
        }
        Map<Long, SysUser> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 3. 批量查询当前用户的点赞状态
        Set<Long> likedCommentIds = Collections.emptySet();
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            List<Long> allCommentIds = allComments.stream()
                    .map(BlogComment::getId)
                    .collect(Collectors.toList());
            if (!allCommentIds.isEmpty()) {
                LambdaQueryWrapper<BlogCommentLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.in(BlogCommentLike::getCommentId, allCommentIds)
                        .eq(BlogCommentLike::getUserId, currentUserId);
                List<BlogCommentLike> likes = commentLikeMapper.selectList(likeWrapper);
                likedCommentIds = likes.stream()
                        .map(BlogCommentLike::getCommentId)
                        .collect(Collectors.toSet());
            }
        } catch (NotLoginException e) {
            // 未登录用户，点赞状态全部为 false
        }

        // 4. 使用预加载数据构建 VO 树
        final Set<Long> finalLikedIds = likedCommentIds;
        List<CommentVO> voList = rootComments.stream()
                .map(c -> convertToVO(c, userMap, finalLikedIds))
                .collect(Collectors.toList());

        for (CommentVO vo : voList) {
            List<BlogComment> children = childrenMap.getOrDefault(vo.getId(), Collections.emptyList());
            vo.setChildren(children.stream()
                    .map(c -> convertToVO(c, userMap, finalLikedIds))
                    .collect(Collectors.toList()));
        }

        Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(commentPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 使用预加载的用户数据和点赞状态转换为 CommentVO（消除 N+1）
     */
    private CommentVO convertToVO(BlogComment comment, Map<Long, SysUser> userMap, Set<Long> likedCommentIds) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);

        SysUser user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        if (comment.getReplyUserId() != null) {
            SysUser replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyNickname(replyUser.getNickname());
            }
        }

        vo.setIsLiked(likedCommentIds.contains(comment.getId()));
        return vo;
    }
}
