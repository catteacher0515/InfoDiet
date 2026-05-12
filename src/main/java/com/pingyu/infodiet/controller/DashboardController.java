package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.dashboard.AdminDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.OpsDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.WorkspaceDashboardVO;
import com.pingyu.infodiet.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台概览接口
 */
@RestController
@RequestMapping
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    /**
     * 当前用户工作台概览
     */
    @GetMapping("/workspace/dashboard/me")
    public BaseResponse<WorkspaceDashboardVO> getWorkspaceDashboard() {
        return ResultUtils.success(dashboardService.getWorkspaceDashboard());
    }

    /**
     * 管理端概览
     */
    @GetMapping("/admin/dashboard")
    public BaseResponse<AdminDashboardVO> getAdminDashboard() {
        return ResultUtils.success(dashboardService.getAdminDashboard());
    }

    /**
     * 运维端概览
     */
    @GetMapping("/ops/dashboard")
    public BaseResponse<OpsDashboardVO> getOpsDashboard() {
        return ResultUtils.success(dashboardService.getOpsDashboard());
    }
}
