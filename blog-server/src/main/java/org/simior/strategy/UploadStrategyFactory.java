package org.simior.strategy;

import lombok.RequiredArgsConstructor;
import org.simior.common.exception.BusinessException;
import org.simior.properties.UploadProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 上传策略工厂
 */
@Component
@RequiredArgsConstructor
public class UploadStrategyFactory {

    private final Map<String, UploadStrategy> uploadStrategyMap;
    private final UploadProperties uploadProperties;

    /**
     * 获取上传策略
     *
     * @return 上传策略
     */
    public UploadStrategy getStrategy() {
        String mode = uploadProperties.getMode();
        String beanName = mode + "UploadStrategyImpl";
        UploadStrategy strategy = uploadStrategyMap.get(beanName);
        if (strategy == null) {
            throw new BusinessException("不支持的上传模式: " + mode);
        }
        return strategy;
    }
}
