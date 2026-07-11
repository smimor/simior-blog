package org.simior.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.common.utils.PageUtils;
import org.simior.model.dto.CommentDTO;
import org.simior.model.vo.CommentVO;
import org.simior.service.CommentService;
import org.springframework.web.bind.annotation.*;

/**
 * 评论管理控制器
 * <p>
 * 提供评论的发布、删除、点赞及文章评论列表查询。
 * 评论列表为公开接口（嵌套在文章子资源下 /v1/articles/{articleId}/comments），
 * 发布和删除需要登录。
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 发布评论（需登录）
     *
     * @param articleId  文章ID（路径参数）
     * @param commentDTO 评论数据
     * @return 新评论ID
     */
    @PostMapping("/v1/articles/{articleId}/comments")
    public Result<Long> publishComment(@PathVariable Long articleId,
                                       @Valid @RequestBody CommentDTO commentDTO) {
        commentDTO.setArticleId(articleId);
        Long commentId = commentService.publishComment(commentDTO);
        return Result.success("评论成功", commentId);
    }

    /**
     * 删除评论（评论作者或管理员可操作）
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/v1/comments/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success("删除成功");
    }

    /**
     * 点赞评论（需登录）
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @PostMapping("/v1/comments/{id}/like")
    public Result<String> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return Result.success("点赞成功");
    }

    /**
     * 取消点赞评论（需登录）
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/v1/comments/{id}/like")
    public Result<String> unlikeComment(@PathVariable Long id) {
        commentService.unlikeComment(id);
        return Result.success("取消点赞成功");
    }

    /**
     * 获取文章评论列表（公开接口，分页）
     *
     * @param articleId 文章ID
     * @param pageNum   当前页码，默认1
     * @param pageSize  每页条数，默认10
     * @return 评论分页数据（含子评论和点赞状态）
     */
    @GetMapping("/v1/articles/{articleId}/comments")
    public Result<Page<CommentVO>> getArticleComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        pageNum = PageUtils.clamp(pageNum, 1, Long.MAX_VALUE);
        pageSize = PageUtils.clamp(pageSize, 1, 100);
        Page<CommentVO> page = commentService.getArticleComments(articleId, pageNum, pageSize);
        return Result.success(page);
    }
}
