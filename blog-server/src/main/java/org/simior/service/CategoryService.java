package org.simior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.simior.model.dto.CategoryDTO;
import org.simior.model.entity.BlogCategory;
import org.simior.model.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<BlogCategory> {

    /**
     * 创建分类
     *
     * @param categoryDTO 分类信息
     * @return 分类ID
     */
    Long createCategory(CategoryDTO categoryDTO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 更新分类
     *
     * @param categoryDTO 分类信息
     */
    void updateCategory(CategoryDTO categoryDTO);

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<CategoryVO> getAllCategories();
}
