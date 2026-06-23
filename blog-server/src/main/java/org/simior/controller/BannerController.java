package org.simior.controller;

import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.model.vo.BannerVO;
import org.simior.service.BannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 轮播图控制器
 * <p>
 * 提供轮播图列表查询。URL 前缀：/v1/banners，为公开接口。
 */
@RestController
@RequestMapping("/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 获取轮播图列表（公开接口）
     *
     * @return 轮播图列表（仅启用的）
     */
    @GetMapping
    public Result<List<BannerVO>> getActiveBanners() {
        List<BannerVO> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }
}
