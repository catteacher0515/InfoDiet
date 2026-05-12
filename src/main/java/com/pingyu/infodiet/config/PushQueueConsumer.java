package com.pingyu.infodiet.config;

import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.service.PushQueueService;
import com.pingyu.infodiet.service.impl.PushQueueServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 推送消息消费者
 */
@Component
@Slf4j
public class PushQueueConsumer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PushQueueService pushQueueService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("info-diet-push-consumer");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * 启动消费者
     */
    @PostConstruct
    public void start() {
        running.set(true);
        executorService.submit(this::consumeLoop);
    }

    /**
     * 关闭消费者
     */
    @PreDestroy
    public void stop() {
        running.set(false);
        executorService.shutdownNow();
    }

    /**
     * 持续消费队列消息
     */
    protected void consumeLoop() {
        while (running.get()) {
            try {
                List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                        StreamOffset.create(PushQueueServiceImpl.PUSH_STREAM_KEY, ReadOffset.from("0-0"))
                );
                if (records == null || records.isEmpty()) {
                    sleepQuietly();
                    continue;
                }
                for (MapRecord<String, Object, Object> record : records) {
                    handleRecord(record);
                    stringRedisTemplate.opsForStream().delete(PushQueueServiceImpl.PUSH_STREAM_KEY, record.getId());
                }
            } catch (Exception e) {
                log.error("推送消息消费异常", e);
                sleepQuietly();
            }
        }
    }

    /**
     * 处理单条消息
     */
    protected void handleRecord(MapRecord<String, Object, Object> record) {
        Map<Object, Object> value = record.getValue();
        PushQueueService.PushMessage pushMessage = new PushQueueService.PushMessage(
                parseLong(value.get("pushId")),
                parseLong(value.get("userId")),
                parseLong(value.get("contentItemId")),
                value.get("pushChannel") == null ? "" : String.valueOf(value.get("pushChannel"))
        );
        pushQueueService.handlePushMessage(pushMessage);
    }

    /**
     * 安静等待
     */
    protected void sleepQuietly() {
        try {
            Thread.sleep(Duration.ofSeconds(1));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 转换 Long
     */
    protected Long parseLong(Object value) {
        if (value == null || StrUtil.isBlank(String.valueOf(value))) {
            return null;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
