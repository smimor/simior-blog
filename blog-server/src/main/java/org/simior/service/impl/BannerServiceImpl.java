package org.simior.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.simior.mapper.BannerMapper;
import org.simior.model.entity.BlogBanner;
import org.simior.model.vo.BannerVO;
import org.simior.service.BannerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, BlogBanner> implements BannerService {

    private final BannerMapper bannerMapper;

    @Override
    public List<BannerVO> getActiveBanners() {
        LambdaQueryWrapper<BlogBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogBanner::getStatus, 1);
        wrapper.orderByAsc(BlogBanner::getSort);
        List<BlogBanner> banners = bannerMapper.selectList(wrapper);
        return banners.stream().map(banner -> {
            BannerVO vo = new BannerVO();
            BeanUtils.copyProperties(banner, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
