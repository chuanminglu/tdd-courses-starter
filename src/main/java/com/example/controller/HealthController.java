package com.example.controller;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器示例.
 * 演示基础的REST API开发
 */
@RestController
public class HealthController {

  /**
   * 健康检查接口.
   *
   * @return 健康状态信息
   */
  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of(
        "status", "UP",
        "timestamp", LocalDateTime.now(),
        "service", "AI TDD Banking Service"
    );
  }
}
