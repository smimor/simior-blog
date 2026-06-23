package org.simior.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.model.dto.CategoryDTO;
import org.simior.model.vo.CategoryVO;
import org.simior.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 * <p>
 * 提供分类的 CRUD 操作。URL 前缀：/v1/categories，遵循 RESTful 风格。
 * 列表查询为公开接口，增删改操作需要 admin 角色权限。
 */
@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建分类（管理员）
     *
     * @param categoryDTO 分类数据
     * @return 新分类ID
     */
    @SaCheckRole("admin")
    @PostMapping
    public Result<Long> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Long categoryId = categoryService.createCategory(categoryDTO);
        return Result.success("创建成功", categoryId);
    }

    /**
     * 删除分类（管理员，分类下有文章时不允许删除）
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @SaCheckRole("admin")
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功");
    }

    /**
     * 更新分类（管理员）
     *
     * @param id          分类ID
     * @param categoryDTO 分类数据
     * @return 操作结果
     */
    @SaCheckRole("admin")
    @PutMapping("/{id}")
    public Result<String> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        categoryDTO.setId(id);
        categoryService.updateCategory(categoryDTO);
        return Result.success("更新成功");
    }

    /**
     * 获取所有分类列表（公开接口）
     *
     * @return 分类列表（含文章计数）
     */
    @GetMapping
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
}
