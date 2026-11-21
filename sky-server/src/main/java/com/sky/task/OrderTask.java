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
 * 定时任务类，定时处理订单状态
 */
@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟触发一次
    //@Scheduled(cron = "1/5 * * * * ?") // 每隔5秒触发一次
    public void processTimeoutOrder() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        List<Orders> statusAndOrderTimeLIst = orderMapper.getStatusAndOrderTimeLIst(
                Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));

        // 如果list不为空，更新订单
        if (statusAndOrderTimeLIst != null && statusAndOrderTimeLIst.size() > 0) {
            for (Orders order : statusAndOrderTimeLIst) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }


    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点触发一次
    //@Scheduled(cron = "0/5 * * * * ?") // 每隔5秒触发一次
    public void processDeliveryOrder() {
        log.info("定时处理派送中的订单:{}", LocalDateTime.now());

        List<Orders> statusAndOrderTimeLIst = orderMapper.getStatusAndOrderTimeLIst(
                Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));

        if (statusAndOrderTimeLIst != null && statusAndOrderTimeLIst.size() > 0) {
            for (Orders order : statusAndOrderTimeLIst) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
