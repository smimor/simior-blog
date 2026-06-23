package org.simior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.simior.model.entity.BlogBanner;
import org.simior.model.vo.BannerVO;

import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService extends IService<BlogBanner> {

    /**
     * 获取所有启用的轮播图
     *
     * @return 轮播图列表
     */
    List<BannerVO> getActiveBanners();
}
