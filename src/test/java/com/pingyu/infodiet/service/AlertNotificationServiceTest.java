package com.pingyu.infodiet.service;

import com.lark.oapi.Client;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import com.pingyu.infodiet.config.FeishuBaseProperties;
import com.pingyu.infodiet.model.entity.AlertRecord;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.impl.AlertNotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertNotificationServiceTest {

    @Test
    void sendAlertToFeishuShouldMarkSentWhenSuccess() throws Exception {
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        Client client = Mockito.mock(Client.class);
        com.lark.oapi.service.im.ImService imService = Mockito.mock(com.lark.oapi.service.im.ImService.class);
        com.lark.oapi.service.im.v1.resource.Message message = Mockito.mock(com.lark.oapi.service.im.v1.resource.Message.class);
        CreateMessageResp response = Mockito.mock(CreateMessageResp.class);

        when(alertRecordService.getById(1L)).thenReturn(AlertRecord.builder()
                .id(1L)
                .alertType("push_final_failed")
                .alertLevel("error")
                .alertTitle("用户内容推送最终失败")
                .alertContent("pushId=1")
                .build());
        when(userProfileService.listEnabledUsers()).thenReturn(List.of(
                UserProfile.builder().id(1L).pushChannel("feishu").feishuUserId("ou_test_user_001").status(1).build()
        ));
        when(client.im()).thenReturn(imService);
        when(imService.message()).thenReturn(message);
        when(message.create(any())).thenReturn(response);
        when(response.success()).thenReturn(true);

        TestableAlertNotificationService service = new TestableAlertNotificationService(client);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "feishuBaseProperties", buildProperties());

        boolean result = service.sendAlertToFeishu(1L);

        assertEquals(true, result);
        verify(alertRecordService, times(1)).markAlertSent(1L);
    }

    @Test
    void sendAlertToFeishuShouldMarkFailedWhenNoFeishuUser() {
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        when(alertRecordService.getById(1L)).thenReturn(AlertRecord.builder()
                .id(1L)
                .alertType("push_final_failed")
                .alertTitle("用户内容推送最终失败")
                .alertContent("pushId=1")
                .build());
        when(userProfileService.listEnabledUsers()).thenReturn(List.of());

        TestableAlertNotificationService service = new TestableAlertNotificationService(null);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "feishuBaseProperties", buildProperties());

        boolean result = service.sendAlertToFeishu(1L);

        assertEquals(false, result);
        verify(alertRecordService, times(1)).markAlertSendFailed(1L, "未找到可用的飞书告警接收人");
    }

    @Test
    void sendPendingAlertsToFeishuShouldReturnSuccessCount() {
        TestableAlertNotificationService service = Mockito.spy(new TestableAlertNotificationService(null));
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);
        when(alertRecordService.listPendingAlerts()).thenReturn(List.of(
                AlertRecord.builder().id(1L).build(),
                AlertRecord.builder().id(2L).build()
        ));
        when(service.sendAlertToFeishu(1L)).thenReturn(true);
        when(service.sendAlertToFeishu(2L)).thenReturn(false);

        int result = service.sendPendingAlertsToFeishu();

        assertEquals(1, result);
    }

    private static FeishuBaseProperties buildProperties() {
        FeishuBaseProperties properties = new FeishuBaseProperties();
        properties.setAppId("cli_test");
        properties.setAppSecret("secret_test");
        properties.setAppToken("token_test");
        properties.setTableId("table_test");
        return properties;
    }

    private static class TestableAlertNotificationService extends AlertNotificationServiceImpl {

        private final Client client;

        private TestableAlertNotificationService(Client client) {
            this.client = client;
        }

        @Override
        protected Client buildClient() {
            return client;
        }

        @Override
        protected LocalDateTime now() {
            return LocalDateTime.now();
        }
    }
}
