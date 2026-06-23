package org.simior.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simior.common.exception.BusinessException;
import org.simior.properties.UploadProperties;
import org.simior.strategy.UploadStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云OSS上传策略实现
 */
@Slf4j
@Service("ossUploadStrategyImpl")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blog.upload.mode", havingValue = "oss")
public class OssUploadStrategyImpl implements UploadStrategy {

    private final OSS ossClient;
    private final UploadProperties uploadProperties;

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 获取文件原名
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀   dfdfdf.png
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 生成新文件名：日期/UUID.后缀
            String fileName = UUID.randomUUID().toString() + StrUtil.DOT + suffix;
            // String fileName = DateUtil.today() + "/" + IdUtil.fastSimpleUUID() + suffix;
            // 拼接完整路径
            String objectName = StrUtil.isBlank(path) ? fileName : path + "/" + fileName;

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        uploadProperties.getOss().getBucketName(),
                        objectName,
                        inputStream
                );
                ossClient.putObject(putObjectRequest);
            }

            // 文件访问路径规则 https://BucketName.Endpoint/ObjectName
            String url = "https://" + uploadProperties.getOss().getBucketName() +
                    StrUtil.DOT +
                    uploadProperties.getOss().getEndpoint() +
                    "/" +
                    objectName;

            log.info("文件上传成功，访问地址:{}", url);
            return url;

        } catch (Exception e) {
            log.error("OSS文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取对象名称
            String host = uploadProperties.getOss().getBucketName() + StrUtil.DOT +
                    uploadProperties.getOss().getEndpoint();
            String objectName = fileUrl.substring(fileUrl.indexOf(host) + host.length() + 1);

            ossClient.deleteObject(uploadProperties.getOss().getBucketName(), objectName);
        } catch (Exception e) {
            log.error("OSS文件删除失败：{}", e.getMessage(), e);
            throw new BusinessException("文件删除失败");
        }
    }

}
