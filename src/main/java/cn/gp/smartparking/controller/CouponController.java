package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.dto.CouponPushDTO;
import cn.gp.smartparking.model.entity.Coupon;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.service.CouponService;
import cn.gp.smartparking.service.UserService;
import cn.gp.smartparking.websocket.service.ParkingWebSocketIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon")
@Tag(name = "优惠券管理")
public class CouponController {

    @Resource
    private CouponService couponService;

    @Resource
    private UserService userService;

    @Resource
    private ParkingWebSocketIntegrationService webSocketIntegrationService;

    @Operation(summary = "获取优惠券列表")
    @GetMapping("/list")
    public Result<java.util.Map<String, Object>> getCouponList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Byte type,
            @RequestParam(required = false) Byte status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Coupon> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Coupon> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        wrapper.eq(userId != null, Coupon::getUserId, userId);
        wrapper.eq(type != null, Coupon::getType, type);
        wrapper.eq(status != null, Coupon::getStatus, status);
        wrapper.eq(Coupon::getIsDeleted, 0);
        wrapper.orderByDesc(Coupon::getCreateTime);

        com.baomidou.mybatisplus.core.metadata.IPage<Coupon> result = couponService.page(page, wrapper);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("records", result.getRecords());
        response.put("total", result.getTotal());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());

        return Result.success("查询成功", response);
    }

    @Operation(summary = "获取优惠券详情")
    @GetMapping("/{id}")
    public Result<Coupon> getCouponDetail(@PathVariable Long id) {
        Coupon coupon = couponService.getById(id);
        return Result.success("获取优惠券详情成功", coupon);
    }

    @Log(module = "优惠券管理", operation = "创建", description = "创建优惠券")
    @Operation(summary = "创建优惠券")
    @PostMapping
    public Result<Coupon> createCoupon(@RequestBody Coupon coupon) {
        couponService.save(coupon);
        return Result.success("创建优惠券成功", coupon);
    }

    @Log(module = "优惠券管理", operation = "更新", description = "更新优惠券")
    @Operation(summary = "更新优惠券")
    @PutMapping("/{id}")
    public Result<Coupon> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        couponService.updateById(coupon);
        Coupon updated = couponService.getById(id);
        return Result.success("更新优惠券成功", updated);
    }

    @Log(module = "优惠券管理", operation = "删除", description = "删除优惠券")
    @Operation(summary = "删除优惠券")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        couponService.removeById(id);
        return Result.success("删除优惠券成功");
    }

    @Operation(summary = "获取用户可用优惠券")
    @GetMapping("/user/{userId}/available")
    public Result<List<Coupon>> getUserAvailableCoupons(@PathVariable Long userId) {
        List<Coupon> coupons = couponService.lambdaQuery()
                .eq(Coupon::getUserId, userId)
                .eq(Coupon::getStatus, (byte) 0) // 未使用
                .gt(Coupon::getEndTime, LocalDateTime.now()) // 未过期
                .list();
        
        // 添加通用券
        List<Coupon> commonCoupons = couponService.lambdaQuery()
                .isNull(Coupon::getUserId)
                .eq(Coupon::getStatus, (byte) 0)
                .gt(Coupon::getEndTime, LocalDateTime.now())
                .list();
        
        coupons.addAll(commonCoupons);
        return Result.success("获取用户可用优惠券成功", coupons);
    }

    @Log(module = "优惠券管理", operation = "使用", description = "使用优惠券")
    @Operation(summary = "使用优惠券")
    @PostMapping("/{id}/use")
    public Result<Void> useCoupon(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Long> requestBody) {

        Long orderId = requestBody.get("orderId");

        Coupon coupon = couponService.getById(id);
        if (coupon != null && coupon.getStatus() == 0 && coupon.getEndTime().after(new Date())) {
            coupon.setStatus(1); // 已使用
            coupon.setUsedTime(new Date());
            coupon.setUsedOrderId(orderId);
            couponService.updateById(coupon);
            return Result.success("使用优惠券成功");
        }
        return Result.fail("优惠券不可用");
    }

    @Operation(summary = "根据优惠码查询优惠券")
    @GetMapping("/code/{code}")
    public Result<Coupon> getCouponByCode(@PathVariable String code) {
        Coupon coupon = couponService.lambdaQuery()
                .eq(Coupon::getCode, code)
                .one();
        return Result.success("根据优惠码查询优惠券成功", coupon);
    }

    @Operation(summary = "分页查询优惠券列表（管理员功能）")
    @GetMapping("/admin/page")
    public Result<Map<String, Object>> getCouponPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {

        int offset = (page - 1) * size;

        long total = couponService.lambdaQuery()
                .like(name != null, Coupon::getName, name)
                .eq(type != null, Coupon::getType, type)
                .eq(status != null, Coupon::getStatus, status)
                .count();

        List<Coupon> records = couponService.getBaseMapper().selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Coupon>()
                        .like(name != null, "name", name)
                        .eq(type != null, "type", type)
                        .eq(status != null, "status", status)
                        .orderByDesc("create_time")
                        .last("LIMIT " + size + " OFFSET " + offset)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", total);
        response.put("current", page);
        response.put("size", size);

        return Result.success("查询优惠券分页列表成功", response);
    }

    @Operation(summary = "获取优惠券统计数据（管理员功能）")
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getCouponStats() {

        long totalCount = couponService.count();
        long unusedCount = couponService.lambdaQuery()
                .eq(Coupon::getStatus, 0)
                .gt(Coupon::getEndTime, new Date())
                .count();
        long usedCount = couponService.lambdaQuery()
                .eq(Coupon::getStatus, 1)
                .count();
        long expiredCount = couponService.lambdaQuery()
                .eq(Coupon::getStatus, 2)
                .or()
                .eq(Coupon::getStatus, 0)
                .lt(Coupon::getEndTime, new Date())
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("unusedCount", unusedCount);
        stats.put("usedCount", usedCount);
        stats.put("expiredCount", expiredCount);

        return Result.success("获取优惠券统计数据成功", stats);
    }

    @Log(module = "优惠券管理", operation = "推送", description = "推送优惠券")
    @Operation(summary = "推送优惠券给用户（管理员功能）")
    @PostMapping("/push")
    public Result<Void> pushCoupon(@RequestBody CouponPushDTO dto, HttpServletRequest request) {
        try {
            // 根据推送范围处理
            if (dto.getPushScope() == 0) {
                // 推送给所有非管理员用户
                List<User> users = userService.lambdaQuery()
                        .eq(User::getRole, 0) // 只查询车主（非管理员）
                        .eq(User::getIsDeleted, 0)
                        .list();

                List<Coupon> coupons = new ArrayList<>();
                for (User user : users) {
                    Coupon coupon = new Coupon();
                    coupon.setName(dto.getName());
                    coupon.setType(dto.getType());
                    coupon.setValue(dto.getValue());
                    coupon.setMinAmount(dto.getMinAmount());
                    coupon.setStartTime(dto.getStartTime());
                    coupon.setEndTime(dto.getEndTime());
                    // 为每个用户生成唯一的优惠码
                    String code = dto.getCode();
                    if (code == null || code.trim().isEmpty()) {
                        code = generateCouponCode(user.getId());
                    }
                    coupon.setCode(code);
                    coupon.setStatus(0); // 未使用
                    coupon.setUserId(user.getId()); // 为每个用户创建专属优惠券
                    coupons.add(coupon);
                }

                // 批量保存优惠券
                if (!coupons.isEmpty()) {
                    couponService.saveBatch(coupons);
                }

                // 构建推送数据
                java.util.Map<String, Object> pushData = new java.util.HashMap<>();
                pushData.put("name", dto.getName());
                pushData.put("type", dto.getType());
                pushData.put("value", dto.getValue());
                pushData.put("minAmount", dto.getMinAmount());
                pushData.put("startTime", dto.getStartTime());
                pushData.put("endTime", dto.getEndTime());

                // 推送给所有车主
                webSocketIntegrationService.broadcastCouponNotification(pushData);

            } else if (dto.getPushScope() == 1 && dto.getUserIds() != null) {
                // 推送给指定用户
                List<Coupon> coupons = new ArrayList<>();
                for (Long userId : dto.getUserIds()) {
                    // 检查用户是否为管理员
                    User user = userService.getById(userId);
                    if (user != null && user.getRole() == 1) {
                        continue; // 跳过管理员
                    }

                    Coupon coupon = new Coupon();
                    coupon.setName(dto.getName());
                    coupon.setType(dto.getType());
                    coupon.setValue(dto.getValue());
                    coupon.setMinAmount(dto.getMinAmount());
                    coupon.setStartTime(dto.getStartTime());
                    coupon.setEndTime(dto.getEndTime());
                    // 为每个用户生成唯一的优惠码
                    String code = dto.getCode();
                    if (code == null || code.trim().isEmpty()) {
                        code = generateCouponCode(userId);
                    }
                    coupon.setCode(code);
                    coupon.setStatus(0); // 未使用
                    coupon.setUserId(userId);
                    coupons.add(coupon);
                }

                // 批量保存优惠券
                if (!coupons.isEmpty()) {
                    couponService.saveBatch(coupons);
                }

                // 构建推送数据
                java.util.Map<String, Object> pushData = new java.util.HashMap<>();
                pushData.put("name", dto.getName());
                pushData.put("type", dto.getType());
                pushData.put("value", dto.getValue());
                pushData.put("minAmount", dto.getMinAmount());
                pushData.put("startTime", dto.getStartTime());
                pushData.put("endTime", dto.getEndTime());

                // 推送给指定用户
                for (Long userId : dto.getUserIds()) {
                    webSocketIntegrationService.pushCouponNotification(userId, pushData);
                }
            }

            return Result.success("优惠券推送成功");
        } catch (Exception e) {
            return Result.fail("优惠券推送失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成随机优惠码
     */
    private String generateCouponCode() {
        long timestamp = System.currentTimeMillis();
        String timeSuffix = String.valueOf(timestamp).substring(String.valueOf(timestamp).length() - 6);
        return "USER" + timeSuffix + (int)(Math.random() * 1000);
    }

    /**
     * 为指定用户生成唯一优惠码
     */
    private String generateCouponCode(Long userId) {
        long timestamp = System.currentTimeMillis();
        String timeSuffix = String.valueOf(timestamp).substring(String.valueOf(timestamp).length() - 6);
        return "USER" + userId + timeSuffix + (int)(Math.random() * 100);
    }
}