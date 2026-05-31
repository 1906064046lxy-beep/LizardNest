package com.lizardnest.app.data.datasource

/**
 * 视频流数据源接口
 * MVP 阶段使用 Mock 实现，后续切换为真实硬件通信
 */
interface VideoStreamDataSource {

    /**
     * 获取视频流地址
     * @return 视频流 URL（RTMP/RTSP/HLS 等）
     */
    fun getStreamUrl(): String
}
