package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.entity.AlertRecord;
import com.pingyu.infodiet.service.AlertNotificationService;
import com.pingyu.infodiet.service.AlertRecordService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AlertRecordControllerTest {

    @Test
    void listPendingAlertsShouldReturnPendingRecords() {
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        when(alertRecordService.listPendingAlerts()).thenReturn(List.of(
                AlertRecord.builder().id(1L).alertType("task_failed").alertStatus(0).build()
        ));

        AlertRecordController controller = new AlertRecordController();
        ReflectionTestUtils.setField(controller, "alertRecordService", alertRecordService);

        BaseResponse<List<AlertRecord>> response = controller.listPendingAlerts();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("task_failed", response.getData().getFirst().getAlertType());
    }

    @Test
    void markAlertSentShouldReturnTrue() {
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        when(alertRecordService.markAlertSent(1L)).thenReturn(true);

        AlertRecordController controller = new AlertRecordController();
        ReflectionTestUtils.setField(controller, "alertRecordService", alertRecordService);

        BaseResponse<Boolean> response = controller.markAlertSent(1L);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void sendPendingAlertsToFeishuShouldReturnSuccessCount() {
        AlertNotificationService alertNotificationService = Mockito.mock(AlertNotificationService.class);
        when(alertNotificationService.sendPendingAlertsToFeishu()).thenReturn(2);

        AlertRecordController controller = new AlertRecordController();
        ReflectionTestUtils.setField(controller, "alertNotificationService", alertNotificationService);

        BaseResponse<Integer> response = controller.sendPendingAlertsToFeishu();

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData());
    }

    @Test
    void sendAlertToFeishuShouldReturnTrue() {
        AlertNotificationService alertNotificationService = Mockito.mock(AlertNotificationService.class);
        when(alertNotificationService.sendAlertToFeishu(1L)).thenReturn(true);

        AlertRecordController controller = new AlertRecordController();
        ReflectionTestUtils.setField(controller, "alertNotificationService", alertNotificationService);

        BaseResponse<Boolean> response = controller.sendAlertToFeishu(1L);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }
}
