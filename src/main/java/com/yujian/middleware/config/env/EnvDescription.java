
package com.yujian.middleware.config.env;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 * @author cy
 * @Date 2021/7/27 2:42 PM
 */
public class EnvDescription {
    public static void main(String[] args) {
        description();
    }

    public static void description() {
        StringBuilder sb = new StringBuilder("\n********环境标识相关信息展示**************\n");
        sb.append("配置文件").append(EnvConfig.ENV_CONFIG_FILE_PATH).append("内容为:\n");
        sb.append(getFileContent(EnvConfig.ENV_CONFIG_FILE_PATH)).append("\n");
        sb.append("配置文件").append(MiddlewareEnv.LOCAL_ENV_FILE_PATH).append("内容为:\n");
        sb.append(getFileContent(MiddlewareEnv.LOCAL_ENV_FILE_PATH)).append("\n");
        sb.append("当前生效环境配置:\n").append(Env.descriptions()).append("\n\n");
        sb.append("JVM参数middleware开头参数内容为:\n").append(getMiddlewareSystemProperties()).append("\n\n");
        sb.append("当前hostName:").append(Env.hostName).append("\n");
        sb.append("当前IP:").append(Env.ipAddress).append("\n\n");
        try {
            String currentEnv = MiddlewareEnv.getEnvName();
            sb.append("识别出当前环境标识为:").append(currentEnv).append("\n");
        } catch (Exception ex) {
            sb.append("识别当前环境标识出错:").append(ex.getMessage()).append("\n");
        }
        System.out.println(sb.toString());
    }

    private static String getFileContent(String path) {
        try {
            File tFile = new File(path);
            if (!tFile.exists() || !tFile.isFile()) {
                return null;
            }
            RandomAccessFile file = new RandomAccessFile(tFile, "r");
            long fileSize = file.length();
            byte[] bytes = new byte[(int) fileSize];
            long readLength = 0L;
            while (readLength < fileSize) {
                int onceLength = file.read(bytes, (int) readLength, (int) (fileSize - readLength));
                if (onceLength > 0) {
                    readLength += onceLength;
                } else {
                    break;
                }
            }
            try {
                file.close();
            } catch (Exception e) {
            }
            String content = new String(bytes);
            if (content.trim().length() == 0) {
                return null;
            }
            return content;
        } catch (IOException e) {
            return String.format("读取文件%s失败，%s", path, e.getMessage());
        }
    }

    private static String getMiddlewareSystemProperties() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry entry : System.getProperties().entrySet()) {
            if (entry.getKey() instanceof String && ((String) entry.getKey()).startsWith(MiddlewareEnv.MIDDLEWARE_SYSTEM_PROPERTY_PREFIX)) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
        }
        if (sb.length() == 0) {
            return "无";
        }
        return sb.toString();
    }
}
