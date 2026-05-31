package com.lizardnest.app.data.datasource.mock

import com.lizardnest.app.data.datasource.VideoStreamDataSource

/**
 * Mock 视频流数据源
 * 返回公开测试视频流 URL
 */
class MockVideoStreamDataSource : VideoStreamDataSource {

    override fun getStreamUrl(): String {
        // 使用公开的 HLS 测试流
        // 这是一个常用的公开测试视频流，适合 MVP 开发调试
        return "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    }
}
