package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    RedissonClient redissonClient;


    //分布式锁
    @Test
    public void redisson(){
        System.out.println(redissonClient);
        //1.获取一把锁
        RLock lock = redissonClient.getLock("my-lock");

        //2.加锁
        //lock.lock();  //阻塞式等待，默认加的锁都是30时间，
        //1)锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s,不容担心业务时间长，锁自动过期被删掉
        //2)加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，默认在30s以后自动删除，


        lock.lock(10, TimeUnit.SECONDS);  //10秒自动解锁，自动解锁时间一定要大于业务的执行时间，
        //自己设置时间只要一到，锁就会自动被解锁，不会自动续期
        //1.如果我们传递了锁的时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        //2.如果我们未指定锁的超时时间，就使用30*1000【LockWatchdogTimout看门狗的默认时间】
        //   只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】，每隔10s都会自动续期为30s

        //最佳实战
        //1)lock.lock(30,TimeUnit.SECONDS); 省掉了整个续费操作，手动解锁，

        try{
            System.out.println("加锁成功" +Thread.currentThread().getId());
            Thread.sleep(30000);
        }catch (Exception e){

        }finally {
            //3.解锁  将设解锁代码没有运行，redisson也不会出现死锁
            lock.unlock();
        }
    }

    //分布式闭锁
    @Test
    public void countDownLunch1(){
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        try {
            door.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("出来了");
    }
    @Test
    public void countDownLunch2() {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();
        System.out.println("减1");
    }






    @Test
    void contextLoads() {

        BrandEntity brandEntity=new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("华为");


//        brandEntity.setName("huawei");
        brandService.updateById(brandEntity);
        System.out.println("OK");

    }

}
