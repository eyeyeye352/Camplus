package com.camplus.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/db")
public class DatabaseConfigController {

    private final DatabaseConfigService databaseConfigService;
    private final DynamicDataSourceConfig dataSourceConfig;

    public DatabaseConfigController(DatabaseConfigService databaseConfigService, DynamicDataSourceConfig dataSourceConfig) {
        this.databaseConfigService = databaseConfigService;
        this.dataSourceConfig = dataSourceConfig;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("connected", databaseConfigService.isConnectionValid());
        result.put("username", databaseConfigService.getCurrentUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            return ResponseEntity.ok(result);
        }

        if (password == null) {
            password = "";
        }

        boolean valid = databaseConfigService.validateConnection(username.trim(), password);
        result.put("success", valid);
        result.put("message", valid ? "连接成功" : "连接失败，请检查用户名和密码");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            return ResponseEntity.ok(result);
        }

        if (password == null) {
            password = "";
        }

        boolean success = databaseConfigService.updateConnection(username.trim(), password);
        if (success) {
            dataSourceConfig.refreshDataSource(databaseConfigService);
            result.put("success", true);
            result.put("message", "配置更新成功");
        } else {
            result.put("success", false);
            result.put("message", "连接失败，请检查用户名和密码");
        }
        return ResponseEntity.ok(result);
    }
}