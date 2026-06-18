package com.xtx.category.controller;

import com.xtx.common.core.result.FrontResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理端点控制器。
 * <p>
 * 提供服务健康检查、版本信息等管理接口。
 * v1.7 新增 {@code /admin/health} 和 {@code /admin/version} 端点。
 * </p>
 */
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final DataSource dataSource;

    @Value("${spring.application.name:xtx-category-service}")
    private String serviceName;

    private static final String VERSION = "v1.7";

    /**
     * GET /admin/health
     * 返回服务健康状态，包含数据库连接检查。
     */
    @GetMapping("/admin/health")
    public FrontResponse<Map<String, Object>> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("service", serviceName);
        info.put("version", VERSION);
        info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 检查数据库连接
        String dbStatus = "UP";
        String dbDetail = "";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            dbDetail = "MySQL";
        } catch (Exception e) {
            dbStatus = "DOWN";
            dbDetail = e.getMessage();
        }
        Map<String, Object> db = new LinkedHashMap<>();
        db.put("status", dbStatus);
        db.put("type", dbDetail);
        info.put("database", db);

        // 整体状态
        String overallStatus = "UP".equals(dbStatus) ? "UP" : "DOWN";
        info.put("status", overallStatus);

        return FrontResponse.success(info);
    }

    /**
     * GET /admin/version
     * 返回当前服务版本号。
     */
    @GetMapping("/admin/version")
    public FrontResponse<Map<String, Object>> version() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("service", serviceName);
        info.put("version", VERSION);
        return FrontResponse.success(info);
    }
}
