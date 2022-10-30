package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.StockDetailTo;
import com.atguigu.common.to.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.*;
import com.mysql.cj.util.StringUtils;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@RabbitListener(queues = {"stock.release.delay.queue"})
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    /**
     *
     *库存解锁的场景
     * 1）、下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     *
     *
     *2）、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁
     * */

    @Override
    public void unlockStock(StockLockedTo to) {
        System.out.println("收到解锁库存的消息");
        StockDetailTo detail = to.getDetail();//
        Long detailId = detail.getId();


        //解锁
        //1.查询数据库关于这个订单的锁定库存信息，
        //有：
        //没有：库存锁定失败，库存回滚了，这种情况无需解锁
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId!=null){
            //解锁
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn(); //根据订单号查询订单的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (0==r.getCode()){
                //订单数据返回成功
                OrderVo data = r.getData("data", new TypeReference<OrderVo>() {
                });
                if (null==data||4==data.getStatus()){
                    //订单已经被取消了，
                    unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                }
            }else {
                //消息拒绝以后重新放到队列里面，让别人继续消费解锁。
                throw new RuntimeException("远程服务失败");
            }
        }else {
            //无需解锁
        }
    }

    private void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId){
        wareSkuDao.unlockStock(skuId,wareId,num);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if(!StringUtils.isNullOrEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isNullOrEmpty(skuId)){
            queryWrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        //判断商品库存是否已经有了，如果没有就先插入一个
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities==null || entities.size()==0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字   如果失败，整个事务无需回滚
            //1.catch掉异常
            //2.TODO 还可以用什么办法，让异常出现以后不回滚
            try{
                R info = productFeignService.info(skuId);
                Map<String,Object> data=(Map<String,Object>)info.get("skuInfo");

                if (info.getCode()==0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }



            skuEntity.setSkuName("");
            wareSkuDao.insert(skuEntity);
        }

        //如果商品库存已经有了，那么就增加，
        wareSkuDao.addStock(skuId,wareId,skuNum);
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            //查询当前sku的总库存量，
            //SELECT SUM(stock-stock_locked) from `wms_ware_sku` where sku_id=1
            Long count = this.baseMapper.getSkuStock(skuId);
            vo.setHasStock(count==null?false:count> 0);
            vo.setSkuId(skuId);
            return vo;
        }).collect(Collectors.toList());

        return collect;

    }

    @Override
    public FareVo getFare(Long addrId) {
        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        if (data!=null){
            String phone = data.getPhone();
            //手机号最后一位作为运费，
            String substring = phone.substring(phone.length() - 1, phone.length());
            BigDecimal bigDecimal = new BigDecimal(substring);

            FareVo fareVo = new FareVo();
            fareVo.setAddress(data);
            fareVo.setFare(bigDecimal);

            return fareVo;
        }
        return null;
    }


    /**
     * @Transactional(rollbackFor = NoStockException.class)
     *
     *
     *库存解锁的场景
     * 1）、下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     *
     *
     *2）、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁
     *
     * */



    @Transactional  //默认只要这个函数有异常都会事务回滚
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         * 保存库存工作单的详情
         * 追溯
         * */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);


        List<OrderItemVo> locks = vo.getLocks();
        //1.找到每个商品在哪个仓库都有库存
        List<SkuWareHashStock> collect = locks.stream().map(item -> {
            SkuWareHashStock stock = new SkuWareHashStock();
            Long skuId = stock.getSkuId();
            stock.setSkuId(skuId);
            //查询这个商品在哪个有库存
            List<Long> wareId = wareSkuDao.listWareIdHashSkuStock(skuId);
            stock.setWareId(wareId);
            stock.setNum(item.getCount());
            return stock;
        }).collect(Collectors.toList());

        //2.锁定库存
        for (SkuWareHashStock hasStock : collect) {
            Boolean skuStocked=false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds==null||wareIds.size()==0){
                //没有任何仓库有这个商品的库存
                throw new NoStockException();
            }
            for (Long wareId : wareIds) {
                //锁成功count=1 失败count=0
                Long count=wareSkuDao.lockSkuStock(skuId,wareId,hasStock.getNum()); //每次传入一个有库存的仓库
                if (count==1){
                    skuStocked=true;
                    //TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(entity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity,stockDetailTo);
                    lockedTo.setDetail(stockDetailTo);
//rabbitmqTemplate
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",lockedTo);
                    break;   //只要某个商品的一个仓库锁成功，那么就break进入下一个商品的锁库存
                }else{
                    //当前仓库锁失败，重试下一个仓库
                }
            }
            if (skuStocked==false){
                //当前商品所有仓库都没有锁成功
                throw new NoStockException();
            }
        }

        //3.上面没有抛异常，那么就成功了，


        return true;
    }



    @Data
    class SkuWareHashStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}