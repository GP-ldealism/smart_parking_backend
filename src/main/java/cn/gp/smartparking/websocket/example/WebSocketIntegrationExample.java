package cn.gp.smartparking.websocket.example;

import cn.gp.smartparking.algorithm.entity.RecommendResult;
import cn.gp.smartparking.algorithm.entity.PredictionResult;
import cn.gp.smartparking.controller.ParkingLotController;
import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.websocket.listener.ParkingBusinessEventListener;
import cn.gp.smartparking.websocket.service.ParkingWebSocketIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import cn.gp.smartparking.common.Result;

import java.util.List;

/**
 * WebSocket集成示例Controller
 * 展示如何在现有业务中集成WebSocket推送
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Slf4j
@RestController
@RequestMapping("/api/websocket-example")
@RequiredArgsConstructor
public class WebSocketIntegrationExample {

    private final ParkingWebSocketIntegrationService webSocketIntegrationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 模拟停车场状态更新并推送WebSocket消息
     */
    @PostMapping("/parking-lot/update")
    public Result<String> updateParkingLotWithWebSocket(@RequestBody ParkingLot parkingLot, 
                                                      @RequestParam Long userId) {
        try {
            // 1. 正常的业务逻辑（更新停车场信息）
            log.info("更新停车场信息: parkingLotId={}, userId={}", parkingLot.getId(), userId);
            
            // 模拟重要变化（比如车位数量大幅变化）
            boolean importantChange = parkingLot.getFreeSpace() != null && parkingLot.getFreeSpace() < 5;
            
            // 2. 发布业务事件（自动触发WebSocket推送）
            eventPublisher.publishEvent(
                new ParkingBusinessEventListener.ParkingLotStatusUpdateEvent(userId, parkingLot, importantChange)
            );
            
            // 3. 或者直接调用WebSocket服务
            webSocketIntegrationService.pushParkingLotStatusUpdate(userId, parkingLot);
            
            return Result.success("停车场状态更新并推送成功");
            
        } catch (Exception e) {
            log.error("停车场状态更新失败", e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 模拟推荐结果更新并推送WebSocket消息
     */
    @PostMapping("/recommendation/update")
    public Result<String> updateRecommendationWithWebSocket(@RequestParam Long userId,
                                                           @RequestBody List<RecommendResult> recommendations) {
        try {
            log.info("更新推荐结果: userId={}, recommendations.size={}", userId, recommendations.size());
            
            // 发布推荐更新事件
            eventPublisher.publishEvent(
                new ParkingBusinessEventListener.RecommendationUpdateEvent(userId, recommendations)
            );
            
            return Result.success("推荐结果更新并推送成功");
            
        } catch (Exception e) {
            log.error("推荐结果更新失败", e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 模拟预测结果更新并推送WebSocket消息
     */
    @PostMapping("/prediction/update")
    public Result<String> updatePredictionWithWebSocket(@RequestParam Long userId,
                                                       @RequestBody List<PredictionResult> predictions) {
        try {
            log.info("更新预测结果: userId={}, predictions.size={}", userId, predictions.size());
            
            // 发布预测更新事件
            eventPublisher.publishEvent(
                new ParkingBusinessEventListener.PredictionUpdateEvent(userId, predictions)
            );
            
            return Result.success("预测结果更新并推送成功");
            
        } catch (Exception e) {
            log.error("预测结果更新失败", e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 广播系统通知
     */
    @PostMapping("/notification/broadcast")
    public Result<String> broadcastNotification(@RequestParam String message) {
        try {
            log.info("广播系统通知: {}", message);
            
            webSocketIntegrationService.broadcastSystemNotification(message);
            
            return Result.success("系统通知广播成功");
            
        } catch (Exception e) {
            log.error("广播系统通知失败", e);
            return Result.fail("广播失败: " + e.getMessage());
        }
    }

    /**
     * 向特定用户发送通知
     */
    @PostMapping("/notification/user")
    public Result<String> sendUserNotification(@RequestParam Long userId, @RequestParam String message) {
        try {
            log.info("向用户发送通知: userId={}, message={}", userId, message);
            
            webSocketIntegrationService.notifyUser(userId, message);
            
            return Result.success("用户通知发送成功");
            
        } catch (Exception e) {
            log.error("用户通知发送失败", e);
            return Result.fail("发送失败: " + e.getMessage());
        }
    }

    /**
     * 向管理员发送通知
     */
    @PostMapping("/notification/admin")
    public Result<String> sendAdminNotification(@RequestParam String message) {
        try {
            log.info("向管理员发送通知: {}", message);
            
            webSocketIntegrationService.notifyAdmins(message);
            
            return Result.success("管理员通知发送成功");
            
        } catch (Exception e) {
            log.error("管理员通知发送失败", e);
            return Result.fail("发送失败: " + e.getMessage());
        }
    }
}
