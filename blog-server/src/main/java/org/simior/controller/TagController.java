package org.simior.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.model.dto.TagDTO;
import org.simior.model.vo.TagVO;
import org.simior.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 * <p>
 * 提供标签的 CRUD 操作。URL 前缀：/v1/tags，遵循 RESTful 风格。
 * 列表查询为公开接口，增删改操作需要 admin 角色权限。
 */
@RestController
@RequestMapping("/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 创建标签（管理员）
     *
     * @param tagDTO 标签数据
     * @return 新标签ID
     */
    @SaCheckRole("admin")
    @PostMapping
    public Result<Long> createTag(@Valid @RequestBody TagDTO tagDTO) {
        Long tagId = tagService.createTag(tagDTO);
        return Result.success("创建成功", tagId);
    }

    /**
     * 删除标签（管理员）
     *
     * @param id 标签ID
     * @return 操作结果
     */
    @SaCheckRole("admin")
    @DeleteMapping("/{id}")
    public Result<String> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success("删除成功");
    }

    /**
     * 更新标签（管理员）
     *
     * @param id     标签ID
     * @param tagDTO 标签数据
     * @return 操作结果
     */
    @SaCheckRole("admin")
    @PutMapping("/{id}")
    public Result<String> updateTag(@PathVariable Long id, @Valid @RequestBody TagDTO tagDTO) {
        tagDTO.setId(id);
        tagService.updateTag(tagDTO);
        return Result.success("更新成功");
    }

    /**
     * 获取所有标签列表（公开接口）
     *
     * @return 标签列表（含文章计数）
     */
    @GetMapping
    public Result<List<TagVO>> getAllTags() {
        List<TagVO> tags = tagService.getAllTags();
        return Result.success(tags);
    }
}
