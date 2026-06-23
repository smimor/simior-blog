package org.simior.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.upload")
public class UploadProperties {

    /**
     * 上传模式：minio 或 oss
     */
    private String mode = "minio";

    /**
     * MinIO配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * OSS配置
     */
    private OssConfig oss = new OssConfig();


    @Data
    public static class MinioConfig {
        /**
         * 端点
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 密钥
         */
        private String secretKey;

        /**
         * 存储桶名称
         */
        private String bucketName;
    }


    @Data
    public static class OssConfig {
        /**
         * 端点
         */
        private String endpoint;

        /**
         * 访问密钥ID
         */
        private String accessKeyId;

        /**
         * 访问密钥
         */
        private String accessKeySecret;

        /**
         * 存储桶名称
         */
        private String bucketName;
    }
}