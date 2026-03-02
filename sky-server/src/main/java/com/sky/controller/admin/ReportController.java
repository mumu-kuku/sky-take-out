package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Api("数据统计接口")
@Slf4j
public class ReportController {
    @Autowired
    ReportService reportService;

    /**
     * 统计begin到end之间销量前十的商品
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    @ApiOperation("统计销量前十的商品")
    public Result<SalesTop10ReportVO> getSalesTop10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("正在统计从{}到{}销量前十的商品", begin, end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.getSalesTop10(begin, end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 统计begin到end之间用户总数与新增用户数
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("统计用户总数与新增用户数")
    public Result<UserReportVO> getUserStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("正在统计从{}到{}的用户总数与新增用户数", begin, end);
        UserReportVO userReportVO = reportService.getUserStatistics(begin, end);
        return Result.success(userReportVO);
    }

    /**
     * 统计begin到end之间营业额
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("统计营业额")
    public Result<TurnoverReportVO> getTurnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("正在统计从{}到{}的营业额", begin, end);
        TurnoverReportVO TurnoverReportVO = reportService.getTurnoverStatistics(begin, end);
        return Result.success(TurnoverReportVO);
    }

    /**
     * 统计begin到end之间订单
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("统计订单")
    public Result<OrderReportVO> getOrdersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                     @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("正在统计从{}到{}的订单", begin, end);
        OrderReportVO orderReportVO = reportService.getOrdersStatistics(begin, end);
        return Result.success(orderReportVO);
    }

    /**
     * 导出数据为excel
     * @param response
     * @return
     */
    @GetMapping("/export")
    @ApiOperation("导出数据为excel报表")
    public Result export(HttpServletResponse response) {
        log.info("正在导出数据为excel");
        reportService.export(response);
        return Result.success();
    }
}
