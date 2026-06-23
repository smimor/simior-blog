package org.simior.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.simior.common.exception.BusinessException;
import org.simior.mapper.ArticleTagMapper;
import org.simior.mapper.TagMapper;
import org.simior.model.dto.TagDTO;
import org.simior.model.entity.BlogArticleTag;
import org.simior.model.entity.BlogTag;
import org.simior.model.vo.TagVO;
import org.simior.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, BlogTag> implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(TagDTO tagDTO) {
        LambdaQueryWrapper<BlogTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogTag::getTagName, tagDTO.getTagName());
        Long count = tagMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }
        BlogTag tag = new BlogTag();
        BeanUtils.copyProperties(tagDTO, tag);
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        LambdaQueryWrapper<BlogArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticleTag::getTagId, id);
        articleTagMapper.delete(wrapper);
        tagMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(TagDTO tagDTO) {
        BlogTag tag = tagMapper.selectById(tagDTO.getId());
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        BeanUtils.copyProperties(tagDTO, tag);
        tagMapper.updateById(tag);
    }

    @Override
    public List<TagVO> getAllTags() {
        List<BlogTag> tags = tagMapper.selectList(null);

        // 单次 GROUP BY 查询所有标签的文章计数（替代 N+1）
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : articleTagMapper.countArticlesGroupByTag()) {
            Long tagId = ((Number) row.get("tag_id")).longValue();
            Long count = ((Number) row.get("cnt")).longValue();
            countMap.put(tagId, count);
        }

        return tags.stream().map(tag -> {
            TagVO vo = new TagVO();
            BeanUtils.copyProperties(tag, vo);
            vo.setArticleCount(countMap.getOrDefault(tag.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }
}
