import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // standalone 模式：Next.js 会生成一个独立的 Node.js 服务器
  // 不依赖外部的 Node.js 模块，适合 Docker 部署
  output: "standalone",

  // React 编译器：自动优化 React 组件的重新渲染
  // 需要 React 19+
  reactCompiler: true,

  // 图片优化配置
  images: {
    remotePatterns: [
      {
        // 允许加载 MinIO 上的图片（容器内部访问）
        protocol: "http",
        hostname: "minio",
        port: "9000",
        pathname: "/simior-blog/**",
      },
      {
        // 本地开发时 MinIO 在 localhost
        protocol: "http",
        hostname: "127.0.0.1",
        port: "9000",
        pathname: "/simior-blog/**",
      },
    ],
    // 图片缓存时间（秒），60天
    minimumCacheTTL: 5184000,
  },
};

export default nextConfig;
