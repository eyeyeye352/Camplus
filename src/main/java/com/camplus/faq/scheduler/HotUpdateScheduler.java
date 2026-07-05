package com.camplus.faq.scheduler;

import com.camplus.faq.service.serviceImpl.FaqServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每日定时任务调度器
 * 凌晨2点执行：热度分减半 + 今日查询次数重置
 */
@Component
public class HotUpdateScheduler {

    private static final Logger log = LoggerFactory.getLogger(HotUpdateScheduler.class);

    private final FaqServiceImpl faqService = new FaqServiceImpl();

    @Scheduled(cron = "0 0 2 * * ?")
    public void updateHotScoresDaily() {
        try {
            log.info("[定时任务] 开始更新热点问题统计...");
            
            faqService.updateHotScores();
            
            faqService.resetDailyStats();
            
            log.info("[定时任务] 热点问题统计更新完成！");
        } catch (Exception e) {
            log.error("[定时任务] 更新热点问题统计失败: {}", e.getMessage(), e);
        }
    }
}
