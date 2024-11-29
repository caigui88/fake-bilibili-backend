package com.bilibili.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志过滤器类，
 * 该类继承自 `Filter<ILoggingEvent>`。其主要作用是基于时间间隔来决定是否记录日志。具体来说：
 *
 * - `FIVE_MINUTES_MILLIS` 是一个常量，表示五分钟的毫秒数。
 * - `lastLogTimestamp` 是一个静态变量，用于记录上一次日志记录的时间戳。
 * - `decide` 方法是过滤器的核心逻辑：
 *   - 获取当前日志事件的时间戳 `currentTime`。
 *   - 如果 `lastLogTimestamp` 为 0，表示这是第一次记录日志，将 `lastLogTimestamp` 设置为当前时间戳。
 *   - 如果当前时间戳与上一次记录日志的时间戳之间的差值大于五分钟，则记录一个日志信息，并更新 `lastLogTimestamp` 为当前时间戳。
 *   - 最后，返回 `FilterReply.NEUTRAL`，表示该过滤器不会阻止日志事件的传播。
 */
@Slf4j
public class TimeBasedFilter extends Filter<ILoggingEvent> {

    private static final long FIVE_MINUTES_MILLIS = 5 * 60 * 1000;

    private static long lastLogTimestamp;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        long currentTime = event.getTimeStamp();
        if(lastLogTimestamp==0){
            lastLogTimestamp = currentTime;
        }
        if(currentTime - lastLogTimestamp > FIVE_MINUTES_MILLIS) {
            log.info("\n\n\n\n\n\n");
            lastLogTimestamp = currentTime;
        }
        return FilterReply.NEUTRAL;
    }
}
