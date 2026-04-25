package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.PaymentRecord;
import cn.gp.smartparking.service.PaymentRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@Tag(name = "支付管理")
public class PaymentController {

    @Resource
    private PaymentRecordService paymentRecordService;

    @Log(module = "支付管理", operation = "创建", description = "创建支付记录")
    @Operation(summary = "创建支付记录")
    @PostMapping
    public Result<PaymentRecord> createPayment(@RequestBody PaymentRecord paymentRecord) {
        paymentRecordService.save(paymentRecord);
        return Result.success("创建支付记录成功", paymentRecord);
    }

    @Operation(summary = "获取支付记录详情")
    @GetMapping("/{id}")
    public Result<PaymentRecord> getPaymentDetail(@PathVariable Long id) {
        PaymentRecord paymentRecord = paymentRecordService.getById(id);
        return Result.success("获取支付记录详情成功", paymentRecord);
    }

    @Operation(summary = "根据订单ID查询支付记录")
    @GetMapping("/order/{orderId}")
    public Result<PaymentRecord> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentRecord paymentRecord = paymentRecordService.lambdaQuery()
                .eq(PaymentRecord::getOrderId, orderId)
                .one();
        return Result.success("根据订单ID查询支付记录成功", paymentRecord);
    }

    @Operation(summary = "获取用户支付记录列表")
    @GetMapping("/user")
    public Result<List<PaymentRecord>> getUserPayments(@RequestParam Long userId) {
        List<PaymentRecord> payments = paymentRecordService.lambdaQuery()
                .eq(PaymentRecord::getCreateBy, userId)
                .orderByDesc(PaymentRecord::getCreateTime)
                .list();
        return Result.success("获取用户支付记录列表成功", payments);
    }

    @Log(module = "支付管理", operation = "更新状态", description = "更新支付状态")
    @Operation(summary = "更新支付状态")
    @PostMapping("/{id}/status")
    public Result<Void> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam Byte paymentStatus) {
        PaymentRecord paymentRecord = paymentRecordService.getById(id);
        if (paymentRecord != null) {
            paymentRecord.setPaymentStatus(Integer.valueOf(paymentStatus));
            paymentRecordService.updateById(paymentRecord);
            return Result.success("更新支付状态成功");
        }
        return Result.fail("支付记录不存在");
    }

    @Operation(summary = "获取支付统计")
    @GetMapping("/statistics")
    public Result<Object> getPaymentStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // 这里简化处理，实际应该根据日期范围查询统计数据
        // 可以统计总金额、成功支付数、失败支付数等
        Object statistics = new Object(); // 实际应该返回统计数据对象
        return Result.success("获取支付统计成功", statistics);
    }

    @Operation(summary = "查询支付记录列表")
    @GetMapping("/list")
    public Result<List<PaymentRecord>> getPaymentList(
            @RequestParam(required = false) Byte paymentStatus,
            @RequestParam(required = false) Byte paymentMethod) {
        
        List<PaymentRecord> payments = paymentRecordService.lambdaQuery()
                .eq(paymentStatus != null, PaymentRecord::getPaymentStatus, paymentStatus)
                .eq(paymentMethod != null, PaymentRecord::getPaymentMethod, paymentMethod)
                .orderByDesc(PaymentRecord::getCreateTime)
                .list();
        
        return Result.success("查询支付记录列表成功", payments);
    }

    @Operation(summary = "分页查询支付记录列表（管理员功能）")
    @GetMapping("/page")
    public Result<Map<String, Object>> getPaymentPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) Integer paymentMethod,
            @RequestParam(required = false) Long orderId) {

        int offset = (page - 1) * size;

        long total = paymentRecordService.lambdaQuery()
                .eq(paymentStatus != null, PaymentRecord::getPaymentStatus, paymentStatus)
                .eq(paymentMethod != null, PaymentRecord::getPaymentMethod, paymentMethod)
                .eq(orderId != null, PaymentRecord::getOrderId, orderId)
                .count();

        List<PaymentRecord> records = paymentRecordService.getBaseMapper().selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PaymentRecord>()
                        .eq(paymentStatus != null, "payment_status", paymentStatus)
                        .eq(paymentMethod != null, "payment_method", paymentMethod)
                        .eq(orderId != null, "order_id", orderId)
                        .orderByDesc("create_time")
                        .last("LIMIT " + size + " OFFSET " + offset)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", total);
        response.put("current", page);
        response.put("size", size);

        return Result.success("查询支付记录分页列表成功", response);
    }

    @Operation(summary = "获取支付统计数据（管理员功能）")
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getPaymentStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        long totalCount = paymentRecordService.count();
        long successCount = paymentRecordService.lambdaQuery()
                .eq(PaymentRecord::getPaymentStatus, 1)
                .count();
        long pendingCount = paymentRecordService.lambdaQuery()
                .eq(PaymentRecord::getPaymentStatus, 0)
                .count();
        long failedCount = paymentRecordService.lambdaQuery()
                .eq(PaymentRecord::getPaymentStatus, 2)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("pendingCount", pendingCount);
        stats.put("failedCount", failedCount);

        return Result.success("获取支付统计数据成功", stats);
    }
}