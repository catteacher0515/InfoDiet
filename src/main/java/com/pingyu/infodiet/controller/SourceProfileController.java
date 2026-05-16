package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.entity.SourceProfile;
import com.pingyu.infodiet.service.SourceProfileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 信源档案接口
 */
@RestController
@RequestMapping("/source-profile")
public class SourceProfileController {

    @Resource
    private SourceProfileService sourceProfileService;

    /**
     * 保存或更新信源档案
     */
    @PostMapping("/save")
    public BaseResponse<Boolean> saveOrUpdateSourceProfile(@RequestBody SourceProfile request) {
        return ResultUtils.success(sourceProfileService.saveOrUpdateSourceProfile(request));
    }

    /**
     * 查询信源档案列表
     */
    @GetMapping("/list")
    public BaseResponse<List<SourceProfile>> listSourceProfiles(@RequestParam(defaultValue = "true") boolean enabledOnly) {
        return ResultUtils.success(enabledOnly
                ? sourceProfileService.listEnabledSourceProfiles()
                : sourceProfileService.listAllSourceProfiles());
    }
}
