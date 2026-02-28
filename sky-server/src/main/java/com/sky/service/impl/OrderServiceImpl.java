package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    AddressBookMapper addressBookMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
//    @Autowired
//    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
//    @Value("${sky.shop.address}")
//    private String shopAddress;
//    @Value("${sky.baidu.ak}")
//    private String ak;
//    private final String BAIDU_GEOCODING = "https://api.map.baidu.com/geocoding/v3";
//    private final String BAIDU_DIRECTIONLITE = "https://api.map.baidu.com/directionlite/v1/driving";

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 判断当前订单对应的购物车和地址是否正常
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
////         判断当前位置是否超出配送距离(5公里)
//        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 插入一个 order
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setUserId(userId);
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(addressBook.getDetail());
        orderMapper.insert(orders);

        // 插入 n 个 orderDetail
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 清空购物车
        shoppingCartMapper.delete(shoppingCart);

        // 封装 VO 对象并返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .id(orders.getId())
                .build();
        return orderSubmitVO;
    }

//    /**
//     * 检查客户的收货地址是否超出配送范围
//     * @param address
//     */
//    private void checkOutOfRange(String address) {
//        Map<String, String> map = new HashMap();
//        map.put("address",shopAddress);
//        map.put("output","json");
//        map.put("ak",ak);
//
//        //获取店铺的经纬度坐标
//        String shopCoordinate = HttpClientUtil.doGet(BAIDU_GEOCODING, map);
//
//        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
//        if(!jsonObject.getString("status").equals("0")){
//            throw new OrderBusinessException("店铺地址解析失败");
//        }
//
//        //数据解析
//        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
//        String lat = location.getString("lat");
//        String lng = location.getString("lng");
//        //店铺经纬度坐标
//        String shopLngLat = lat + "," + lng;
//
//        map.put("address",address);
//        //获取用户收货地址的经纬度坐标
//        String userCoordinate = HttpClientUtil.doGet(BAIDU_GEOCODING, map);
//
//        jsonObject = JSON.parseObject(userCoordinate);
//        if(!jsonObject.getString("status").equals("0")){
//            throw new OrderBusinessException("收货地址解析失败");
//        }
//
//        //数据解析
//        location = jsonObject.getJSONObject("result").getJSONObject("location");
//        lat = location.getString("lat");
//        lng = location.getString("lng");
//        //用户收货地址经纬度坐标
//        String userLngLat = lat + "," + lng;
//
//        map.put("origin",shopLngLat);
//        map.put("destination",userLngLat);
//        map.put("steps_info","0");
//
//        //路线规划
//        String json = HttpClientUtil.doGet(BAIDU_DIRECTIONLITE, map);
//
//        jsonObject = JSON.parseObject(json);
//        if(!jsonObject.getString("status").equals("0")){
//            throw new OrderBusinessException("配送路线规划失败");
//        }
//
//        //数据解析
//        JSONObject result = jsonObject.getJSONObject("result");
//        JSONArray jsonArray = (JSONArray) result.get("routes");
//        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");
//
//        if(distance > 5000){
//            //配送距离超过5000米
//            throw new OrderBusinessException("超出配送范围");
//        }
//    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Long orderid = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber()).getId();
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderid);

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     */
    @Override
    public PageResult historyQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        Page<Orders> page = orderMapper.list(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        for (Orders orders : page) {
            List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(orders.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            list.add(orderVO);
        }
        PageResult pageResult = new PageResult();
        pageResult.setRecords(list);
        pageResult.setTotal(list.size());

        return pageResult;
    }

    /**
     * 根据id查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getById(Long id) {
        OrderVO orderVO = new OrderVO();
        Orders orders = orderMapper.getById(id);
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        // 根据id查询订单
        Orders ordersDB = getOrders(id);

        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        // 如果订单是已经接单或取消的状态，用户无法取消订单
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 将订单对应的订单明细重新插入购物车表
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(id);
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.list(ordersPageQueryDTO);
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    /**
     * 根据状态统计订单数量
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
         Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
         Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
         Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        // 查找出对应订单,判断订单是否存在
        Orders orders = getOrders(ordersConfirmDTO.getId());
        // 判断订单是否是待接单状态并且是已支付状态
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED) && !orders.getPayStatus().equals(Orders.PAID)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 修改订单为接单状态
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param
     * @return
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        // 查找出对应订单,判断订单是否存在
        Orders orders = getOrders(ordersRejectionDTO.getId());
        // 判断订单是否是待接单状态
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 已付款的订单拒单需要进行退款
        if (orders.getPayStatus().equals(Orders.PAID)) {
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
            //支付状态修改为退款
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = getOrders(ordersCancelDTO.getId());

        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        // 待付款、待派送、派送中、已完成状态可以进行取消操作
        Integer status = ordersDB.getStatus();
        if (status.equals(Orders.TO_BE_CONFIRMED) || status.equals(Orders.CANCELLED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于已付款状态下取消，需要进行退款
        if (ordersDB.getPayStatus().equals(Orders.PAID)) {
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(orders.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 根据id派送
     * @param id
     */
    @Override
    public void delivery(Long id) {
        // 查找出对应订单,判断订单是否存在
        Orders orders = getOrders(id);
        // 判断订单是否是已接单状态并且已付款
        if (!orders.getStatus().equals(Orders.CONFIRMED) && !orders.getPayStatus().equals(Orders.PAID)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 修改订单为派送中状态并更新数据库
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 根据id完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders = getOrders(id);
        // 判断订单是否是派送中状态并且已付款
        if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)  && !orders.getPayStatus().equals(Orders.PAID)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 修改订单为已完成状态并更新数据库
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }

    private Orders getOrders(Long id) {
        // 查找出对应订单,判断订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return orders;
    }
}
