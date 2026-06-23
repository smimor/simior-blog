package org.simior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.simior.model.dto.TagDTO;
import org.simior.model.entity.BlogTag;
import org.simior.model.vo.TagVO;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService extends IService<BlogTag> {

    /**
     * 创建标签
     *
     * @param tagDTO 标签信息
     * @return 标签ID
     */
    Long createTag(TagDTO tagDTO);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(Long id);

    /**
     * 更新标签
     *
     * @param tagDTO 标签信息
     */
    void updateTag(TagDTO tagDTO);

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<TagVO> getAllTags();
}
