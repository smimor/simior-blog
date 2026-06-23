package org.simior.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.simior.common.exception.BusinessException;
import org.simior.mapper.ArticleMapper;
import org.simior.mapper.CategoryMapper;
import org.simior.model.dto.CategoryDTO;
import org.simior.model.entity.BlogArticle;
import org.simior.model.entity.BlogCategory;
import org.simior.model.vo.CategoryVO;
import org.simior.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, BlogCategory> implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CategoryDTO categoryDTO) {
        LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogCategory::getCategoryName, categoryDTO.getCategoryName());
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        BlogCategory category = new BlogCategory();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getCategoryId, id);
        Long count = articleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该分类下还有文章,无法删除");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CategoryDTO categoryDTO) {
        BlogCategory category = categoryMapper.selectById(categoryDTO.getId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查新名称是否与其他分类重复
        if (categoryDTO.getCategoryName() != null) {
            LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogCategory::getCategoryName, categoryDTO.getCategoryName());
            wrapper.ne(BlogCategory::getId, categoryDTO.getId());
            Long count = categoryMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("分类名称已存在");
            }
        }

        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.updateById(category);
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BlogCategory::getSort);
        List<BlogCategory> categories = categoryMapper.selectList(wrapper);

        // 单次 GROUP BY 查询所有分类的文章计数（替代 N+1）
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : articleMapper.countArticlesGroupByCategory()) {
            Long categoryId = ((Number) row.get("category_id")).longValue();
            Long count = ((Number) row.get("cnt")).longValue();
            countMap.put(categoryId, count);
        }

        return categories.stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(category, vo);
            vo.setArticleCount(countMap.getOrDefault(category.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }
}
