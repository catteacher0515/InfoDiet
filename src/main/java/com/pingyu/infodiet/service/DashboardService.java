package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.dashboard.AdminDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.OpsDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.WorkspaceDashboardVO;

/**
 * 工作台概览服务
 */
public interface DashboardService {

    /**
     * 查询当前用户工作台概览
     */
    WorkspaceDashboardVO getWorkspaceDashboard();

    /**
     * 查询管理端概览
     */
    AdminDashboardVO getAdminDashboard();

    /**
     * 查询运维端概览
     */
    OpsDashboardVO getOpsDashboard();
}
