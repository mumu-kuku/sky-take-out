package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务类
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;

    /**
     * 自动处理超时未支付订单
     * 每分钟检查一次，下单时间超过15分钟为超时自动取消
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void processTimeoutOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(15);
        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeBefore(Orders.PENDING_PAYMENT, timeoutTime);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                log.info("未付款订单超时自动取消订单，订单号：{}", orders.getNumber());
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("用户超过15分钟未付款，订单超时自动取消");
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 自动处理超时已支付未接单订单
     * 每五分钟检查一次，下单时间超过45分钟未接单自动退款取消订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void processUnacceptedOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(45);
        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeBefore(Orders.TO_BE_CONFIRMED, timeoutTime);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                log.info("已付款订单超时自动取消订单，订单号：{}", orders.getNumber());
                orders.setStatus(Orders.CANCELLED);
                orders.setPayStatus(Orders.CONFIRMED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("商家超过45分钟未接单，订单超时自动退款取消");
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 自动处理长时间未完成的派送中订单
     * 每天凌晨2点检查一次，下单时间超过24小时未完成的派送中订单自动完成
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void processDeliveringTimeoutOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusHours(24);
        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeBefore(Orders.DELIVERY_IN_PROGRESS, timeoutTime);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                log.info("派送中订单超时自动完成订单，订单号：{}", orders.getNumber());
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
