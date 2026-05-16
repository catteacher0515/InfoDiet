package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.entity.SourceProfile;
import com.pingyu.infodiet.service.SourceProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SourceProfileControllerTest {

    @Test
    void saveOrUpdateSourceProfileShouldReturnSuccess() {
        SourceProfileService sourceProfileService = Mockito.mock(SourceProfileService.class);
        when(sourceProfileService.saveOrUpdateSourceProfile(any(SourceProfile.class))).thenReturn(true);

        SourceProfileController controller = new SourceProfileController();
        ReflectionTestUtils.setField(controller, "sourceProfileService", sourceProfileService);

        BaseResponse<Boolean> response = controller.saveOrUpdateSourceProfile(SourceProfile.builder()
                .platform("github")
                .profileType("author")
                .sourceKey("openai")
                .sourceCategory("official")
                .sourceTier("T1")
                .build());

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void listSourceProfilesShouldReturnEnabledList() {
        SourceProfileService sourceProfileService = Mockito.mock(SourceProfileService.class);
        when(sourceProfileService.listEnabledSourceProfiles()).thenReturn(List.of(
                SourceProfile.builder().id(1L).platform("github").profileType("author").sourceKey("openai").sourceTier("T1").build()
        ));

        SourceProfileController controller = new SourceProfileController();
        ReflectionTestUtils.setField(controller, "sourceProfileService", sourceProfileService);

        BaseResponse<List<SourceProfile>> response = controller.listSourceProfiles(true);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("github", response.getData().getFirst().getPlatform());
    }

    @Test
    void listSourceProfilesShouldReturnAllList() {
        SourceProfileService sourceProfileService = Mockito.mock(SourceProfileService.class);
        when(sourceProfileService.listAllSourceProfiles()).thenReturn(List.of(
                SourceProfile.builder().id(1L).platform("github").build(),
                SourceProfile.builder().id(2L).platform("youtube").build()
        ));

        SourceProfileController controller = new SourceProfileController();
        ReflectionTestUtils.setField(controller, "sourceProfileService", sourceProfileService);

        BaseResponse<List<SourceProfile>> response = controller.listSourceProfiles(false);

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().size());
    }
}
