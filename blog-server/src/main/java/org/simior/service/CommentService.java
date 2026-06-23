package org.simior.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.simior.model.dto.CommentDTO;
import org.simior.model.entity.BlogComment;
import org.simior.model.vo.CommentVO;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<BlogComment> {

    /**
     * 发布评论
     *
     * @param commentDTO 评论信息
     * @return 评论ID
     */
    Long publishComment(CommentDTO commentDTO);

    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    void deleteComment(Long id);

    /**
     * 评论点赞/取消点赞
     *
     * @param commentId 评论ID
     */
    void likeComment(Long commentId);

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     */
    void unlikeComment(Long commentId);

    /**
     * 获取文章评论列表
     *
     * @param articleId 文章ID
     * @param pageNum   当前页
     * @param pageSize  每页大小
     * @return 评论列表
     */
    Page<CommentVO> getArticleComments(Long articleId, Long pageNum, Long pageSize);
}
