package com.camplus.faq.scheduler;

import com.camplus.faq.service.FaqService;
import com.camplus.faq.service.serviceImpl.FaqServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HotUpdateScheduler {

    private final FaqService faqService = new FaqServiceImpl();

    @Scheduled(cron = "0 0 2 * * ?")
    public void updateHotScoresDaily() {
        try {
            System.out.println("开始更新热点问题分数...");
            faqService.updateHotScores();
            System.out.println("热点问题分数更新完成！");
        } catch (Exception e) {
            System.err.println("更新热点问题分数失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
