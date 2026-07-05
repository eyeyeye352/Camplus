package com.camplus.common.controller;

import com.camplus.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    private static final String LOG_DIR = "./logs";
    private static final String DEFAULT_LOG_FILE = "Camplus.log";

    @GetMapping("/api/logs")
    public Result<List<String>> getLogs(
            @RequestParam(name = "lines", defaultValue = "100") int lines,
            @RequestParam(name = "file", required = false) String file) {

        String fileName = file != null && !file.trim().isEmpty() ? file.trim() : DEFAULT_LOG_FILE;
        Path logPath = Paths.get(LOG_DIR, fileName);

        if (!Files.exists(logPath)) {
            return Result.fail("日志文件不存在");
        }

        try {
            List<String> logLines = readLastLines(logPath.toFile(), lines);
            return Result.ok("success", logLines);
        } catch (IOException e) {
            logger.error("读取日志文件失败", e);
            return Result.fail("读取日志文件失败");
        }
    }

    @GetMapping("/api/logs/list")
    public Result<List<String>> getLogFileList() {
        Path logDirPath = Paths.get(LOG_DIR);

        if (!Files.exists(logDirPath) || !Files.isDirectory(logDirPath)) {
            return Result.ok("success", new ArrayList<>());
        }

        try {
            List<String> files = new ArrayList<>();
            Files.list(logDirPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".log"))
                    .forEach(path -> files.add(path.getFileName().toString()));
            return Result.ok("success", files);
        } catch (IOException e) {
            logger.error("获取日志文件列表失败", e);
            return Result.fail("获取日志文件列表失败");
        }
    }

    private List<String> readLastLines(File file, int lineCount) throws IOException {
        List<String> result = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            if (fileLength == 0) {
                return result;
            }

            long position = fileLength - 1;
            int linesRead = 0;
            StringBuilder currentLine = new StringBuilder();

            while (position >= 0 && linesRead < lineCount) {
                raf.seek(position);
                byte b = raf.readByte();

                if (b == '\n' && currentLine.length() > 0) {
                    result.add(currentLine.reverse().toString());
                    currentLine = new StringBuilder();
                    linesRead++;
                } else if (b != '\r') {
                    currentLine.append((char) b);
                }

                position--;
            }

            if (currentLine.length() > 0) {
                result.add(currentLine.reverse().toString());
            }

            java.util.Collections.reverse(result);
        }

        return result;
    }
}