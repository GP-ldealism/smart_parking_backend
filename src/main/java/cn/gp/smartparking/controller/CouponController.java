package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.Coupon;
import cn.gp.smartparking.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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

    @Operation(summary = "获取优惠券列表")
    @GetMapping("/list")
    public Result<List<Coupon>> getCouponList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Byte type,
            @RequestParam(required = false) Byte status) {
        
        List<Coupon> coupons = couponService.lambdaQuery()
                .eq(userId != null, Coupon::getUserId, userId)
                .eq(type != null, Coupon::getType, type)
                .eq(status != null, Coupon::getStatus, status)
                .gt(Coupon::getEndTime, new Date()) // 只查询未过期的优惠券
                .orderByDesc(Coupon::getCreateTime)
                .list();
        
        return Result.success("获取优惠券列表成功", coupons);
    }

    @Operation(summary = "获取优惠券详情")
    @GetMapping("/{id}")
    public Result<Coupon> getCouponDetail(@PathVariable Long id) {
        Coupon coupon = couponService.getById(id);
        return Result.success("获取优惠券详情成功", coupon);
    }

    @Operation(summary = "创建优惠券")
    @PostMapping
    public Result<Coupon> createCoupon(@RequestBody Coupon coupon) {
        couponService.save(coupon);
        return Result.success("创建优惠券成功", coupon);
    }

    @Operation(summary = "更新优惠券")
    @PutMapping("/{id}")
    public Result<Coupon> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        couponService.updateById(coupon);
        Coupon updated = couponService.getById(id);
        return Result.success("更新优惠券成功", updated);
    }

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

    @Operation(summary = "使用优惠券")
    @PostMapping("/{id}/use")
    public Result<Void> useCoupon(
            @PathVariable Long id,
            @RequestParam Long orderId) {
        
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
}