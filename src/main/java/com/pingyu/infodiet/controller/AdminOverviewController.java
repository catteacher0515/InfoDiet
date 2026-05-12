package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.dashboard.AdminSubscriptionOverviewVO;
import com.pingyu.infodiet.service.AdminOverviewService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理区总览接口
 */
@RestController
@RequestMapping("/admin/overview")
public class AdminOverviewController {

    @Resource
    private AdminOverviewService adminOverviewService;

    /**
     * 查询订阅总览
     */
    @GetMapping("/subscriptions")
    public BaseResponse<AdminSubscriptionOverviewVO> getSubscriptionOverview() {
        return ResultUtils.success(adminOverviewService.getSubscriptionOverview());
    }
}
