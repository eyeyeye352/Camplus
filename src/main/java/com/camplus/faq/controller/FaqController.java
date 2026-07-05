package com.camplus.faq.controller;

import com.camplus.faq.mappers.FaqMapper;
import com.camplus.faq.pojo.Faq;
import com.camplus.faq.service.FaqService;
import com.camplus.faq.service.serviceImpl.FaqServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faq")
public class FaqController {

    private final FaqService faqService;
    private final FaqMapper faqMapper;

    public FaqController(FaqService faqService, FaqMapper faqMapper) {
        this.faqService = faqService;
        this.faqMapper = faqMapper;
    }

    @GetMapping("/hot")
    public Map<String, Object> getHotFaqs(@RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Faq> faqs = faqService.getHotFaqs(limit);
            result.put("success", true);
            result.put("data", faqs);
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> getAllFaqs() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Faq> faqs = faqMapper.selectAllByHotScore();
            result.put("success", true);
            result.put("data", faqs);
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/status")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Integer> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer faqId = request.get("faqId");
            Integer displayStatus = request.get("displayStatus");
            if (faqId == null || displayStatus == null) {
                result.put("success", false);
                result.put("msg", "参数不完整");
                return result;
            }
            faqMapper.updateDisplayStatus(faqId, displayStatus);
            result.put("success", true);
            result.put("msg", "操作成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/detail")
    public Map<String, Object> getFaqDetail(@RequestParam Integer faqId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Faq faq = faqService.getFaqById(faqId);
            result.put("success", true);
            result.put("data", faq);
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/click")
    public Map<String, Object> recordClick(@RequestParam Integer faqId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = faqService.recordClick(faqId);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/qa/forward")
    public Map<String, Object> forwardToQA(@RequestParam Integer faqId, @RequestParam String question) {
        Map<String, Object> result = new HashMap<>();
        try {
            faqService.recordClick(faqId);
            result.put("success", true);
            result.put("data", Map.of(
                "qaResult", "问答系统返回的回答：" + question,
                "source", "faq"
            ));
        } catch (Exception e) {
            result.put("success", false);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
