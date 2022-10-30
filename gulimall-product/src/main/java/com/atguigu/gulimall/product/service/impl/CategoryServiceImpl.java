package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import springfox.documentation.annotations.Cacheable;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    private CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {

        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2.组装成父子的树形结构
        //1)找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }
    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((categoryEntity) -> {
            //1.找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //2.菜单排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }


    @Override
    public void removeMenuByIds(List<Long> asList) {
       //TODO  //1.检查当前删除菜单是否被别的地方引用

        //我们配置了mybatis-plus逻辑删除
        baseMapper.deleteBatchIds(asList);
    }




    /**
     * @CacheEvict这个注解说明，只要数据库修改，那么缓存中相应的数据就会被删除
     *
     * @Caching同时进行多种缓存操作，
     *
     * */

//    @Caching(evict = {   //@Caching同时进行多种缓存操作，
//            @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category",key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category",allEntries = true)  //allEntries是true的话，会删除在category分区下所有的key对应的值，
    @Override    //级联更新所有关联的数据
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }




    /**
     * spirng-cache默认行为：
     * 1）如果缓存中有，方法不用调用，
     * 2）key默认自动生成，
     * 3）缓存的value的值，默认使用jdk序列化机制，将序列化后的数据存到redis
     * 4)默认ttl时间-1，
     *
     * 自定义：
     * 1）指定生成的缓存使用key； key属性指定接收一个SpEl
     * 2）指定缓存的数据的存活时间，配置文件修改ttl
     * 3)将数据保存为json格式，
     *
     *
     * Spring-Cache的不足：
     *      缓存穿透：查询一个null数据，解决：spring-cache-null-value=true 配置
     *      缓存刺穿： 大量并发进行同时查询一个正好过期的数据，解决：默认是无锁的， @Cacheable注解可以加上sync=true,那么就可以实现本地锁，够了
     *      缓存雪崩：大量的key同时过期，解决：加随机时间。加上过期时间。spring.cache.redis.time-to-live=360000,配置缓存过期时间
     *
     *
     * */
    //每个需要缓存的数据我们都来指定要放到那个名字的缓存   【缓存的分区（按业务类型分区）】
    @Cacheable(value = "{category}")  //这个注解代表当前方法的结果需要缓存，如果缓存中有，方法不用调用，如果缓存没有，回调用方法，结果存入缓存
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_id",0));
    }





    //使用spring-cache这个组件，注解开发实现数据缓存，
    @Cacheable(value = "category")
    @Override
    public Map<String,List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询数据库");
        List<CategoryEntity> selectList= this.baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys =getParent_cid(selectList,0L);

        //2.封装数据  封装成map
        Map<String,List<Catelog2Vo>> parent_id=level1Categorys.stream().collect(Collectors.toMap(k->k.getCatId().toString(),v->{
            //1.每一个的1级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList,v.getCatId());
            //2.封装上面的结果
            List<Catelog2Vo> catelog2Vos=null;
            if(categoryEntities!=null){
                catelog2Vos=categoryEntities.stream().map(l2->{
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    //1.找当前二级分类的三级分类封装正vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList,l2.getCatId());
                    if(level3Catelog!=null){
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        return parent_id;
    }





    //TODO 产生对外内存溢出 OutOfDirectMemoryError
    /**
     * 1).springboot2.0默认使用lettuce作为redis的客户端，它使用netty进行网络通信
     * 2）.lettuce的bug导致netty对外内存泄漏 ,netty如果没有指定对外内存，默认使用-Xmx虚拟机设置的堆内存
     *
     * 解决方案：1.升级lettuce客户端    2.切换使用jedis
     *
     * redisTemplate:
     *  lettuce,jedis 操作redis的底层客户端, Spring再次封装redisTemplate
     *
     *
     * */
    //使用redisClient的api进行数据缓存
    //@Override
    public Map<String,List<Catelog2Vo>> getCatalogJson2() {
        //给缓存中放json字符串，拿出的json字符串，  还要逆转为能用的对象类型，   【序列化与反序列化】

        /**
         * 1.空结果缓存：解决缓存击穿问题
         * 2.设置过期时间（加随机值）：解决缓存雪崩
         * 3.加锁：解决缓存击穿
         * */

        //1.加入缓存逻辑，缓存中存的是json字符串
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(StringUtils.isEmpty(catalogJSON)){
            //2.缓存中没有，查询数据库
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();
            return catalogJsonFromDb;
        }
        //json字符串转为我们所需的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });


        return result;
    }



    //使用本地锁，
    public Map<String,List<Catelog2Vo>> getCatalogJsonFromDb() {

        //TODO 本地锁：syschronized JUC(Lock) ,在分布式情况下，想要锁住不同服务器上的同一种服务，必须使用分布式锁，
        synchronized (this){
            Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDb();
            return dataFromDb;
        }
    }

    //使用redisson框架的分布式锁，
    public Map<String,List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock(){
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDb();

        try{
            dataFromDb=getDataFromDb();
        }catch (Exception e){

        }finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    //就使用redis的锁分布式锁
    public Map<String,List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //1.占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        //Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "111");
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,300,TimeUnit.SECONDS);//上锁是设置过期时间，这样就是原子操作
        if(lock){
            //加锁成功
            //redisTemplate.expire("lock",30, TimeUnit.SECONDS);   //设置锁过期时间，防止因为断电、异常没有释放锁，其他分布式服务拿不到锁而死锁。
                                                                            //但是还有问题，如果在设置过期时间前断电或者异常那么还是没有释放锁
                                                                //所以上锁和设置过期时间必须是原子操作，
            Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDb();

            //获取值+对比值必须是原子操作
            String lock1 = redisTemplate.opsForValue().get("lock");
            if(lock1==uuid){   //自己线程的锁，才能删
                redisTemplate.delete("lock");    //查询完后解锁，
            }
            return dataFromDb;
        }else{
            //加锁失败...重试
            //休眠100ms重试
            return getCatalogJsonFromDbWithRedisLock();   //自旋的方式
        }
    }


    //从数据库中的到数据，分装成一个函数
    private Map<String,List<Catelog2Vo>> getDataFromDb(){
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(!StringUtils.isEmpty(catalogJSON)){
            //缓存不为空,直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }


        //优化，将数据库查询变为一次
        List<CategoryEntity> selectList= this.baseMapper.selectList(null);


        //1.查出所有1级分类
        List<CategoryEntity> level1Categorys =getParent_cid(selectList,0L);

        //2.封装数据  封装成map
        Map<String,List<Catelog2Vo>> parent_id=level1Categorys.stream().collect(Collectors.toMap(k->k.getCatId().toString(),v->{
            //1.每一个的1级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList,v.getCatId());
            //2.封装上面的结果
            List<Catelog2Vo> catelog2Vos=null;
            if(categoryEntities!=null){
                catelog2Vos=categoryEntities.stream().map(l2->{
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    //1.找当前二级分类的三级分类封装正vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList,l2.getCatId());
                    if(level3Catelog!=null){
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        //3.数据库中查到再放到缓存中，将对象转为json放在缓存中
        String s = JSON.toJSONString(parent_id);
        redisTemplate.opsForValue().set("catalogJSON",s);
        return parent_id;
    }

    //查找子分类CategoryEntity
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parent_cid){
        return selectList.stream().filter(item->{return item.getParentCid()==parent_cid;} ).collect(Collectors.toList());
    }





    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths=new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return (Long[])parentPath.toArray(new Long[parentPath.size()]);
    }
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1.收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(0==byId.getParentCid()){
            List<Long> parentPath = findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }


























}