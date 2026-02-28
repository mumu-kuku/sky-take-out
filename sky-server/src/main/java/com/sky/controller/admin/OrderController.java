package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("orderAdminController")
@RequestMapping("/admin/order")
@Api(tags = "订单相关接口")
public class OrderController {
    @Autowired
    OrderService orderService;

    /**
     * 搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("搜索订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO)  {
        log.info("正在搜索订单，条件为:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据状态统计订单数量
     * @param
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("根据状态统计订单数量")
    public Result<OrderStatisticsVO> statistics()  {
        log.info("正在根据状态统计订单数量");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 根据状态统计订单数量
     * @param
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("根据id查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id)  {
        log.info("根据id{}查询订单详情", id);
        OrderVO orderVO = orderService.getById(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO)  {
        log.info("根据id{}接单", ordersConfirmDTO.getId());
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     * @param
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO)  {
        log.info("根据id{}拒单，拒单原因:{}", ordersRejectionDTO.getId(), ordersRejectionDTO.getRejectionReason());
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 商家取消订单
     * @param
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("商家取消订单")
    public Result rejection(@RequestBody OrdersCancelDTO ordersCancelDTO)  {
        log.info("根据id{}取消订单，取消原因:{}", ordersCancelDTO.getId(), ordersCancelDTO.getCancelReason());
        orderService.adminCancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 根据id派送
     * @param
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("根据id派送")
    public Result delivery(@PathVariable Long id)  {
        log.info("根据id{}派送", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 根据id完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("根据id完成订单")
    public Result complete(@PathVariable Long id) {
        log.info("根据id{}完成订单", id);
        orderService.complete(id);
        return Result.success();
    }
}
