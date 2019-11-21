# fast-redis-annotation
提供基于redisTemplate的注解化redis操作服务，致力改善@Cacheable中的问题
## 导入
直接通过模块导入，且在maven中配置
```
<dependency>
        <groupId>com.mola.utils</groupId>
        <artifactId>fast-redis-annotation</artifactId>
        <version>1.0.0</version>
</dependency>
```
## 配置
在启动类上标注，注意加入自己的配置，或重新写一个注解类
```
@ComponentScan({"com.mola.redis.*","com.xx.xx.*"})
```
yml配置如下
```
# redis基本配置
spring:
    redis:
        database: 0
        host: 127.0.0.1
        port: 6379
# 使用默认redisTemplate配置
fast-redis:
     use-default-redisTemplate: true
```
此处的默认redisTemplate使用Json作为序列化方法，使用lettuce作为连接池，当然也可以使用自己的redisTemplate，将use-default-redisTemplate设为false，再以@bean的方式将自己的redisTemplate注入容器。
## 使用方法
#### @FastRedisCache
##### 注解在方法上，根据key缓存或者更新方法的返回值。
|  变量  | 默认值  | 解释 |
|  ----  | ----  | ----  |
| namespace  | "" | 命名空间  |
| key  | "" | redis的key，允许使用spel表达式  |
| timeout  | -1 | key超时时间 |
| allowKeyEmpty  | false | 允许缓存值为空  |
| cacheType  | CacheType.NORMAL | 缓存方式，分为普通和只更新  |
#### @FastRedisNamespace
##### 注解在类上，定义一级命名空间。
|  变量  | 默认值  | 解释 |
|  ----  | ----  | ---- |
| namespace  | "" | 命名空间  |
#### @FastRedisDelete
##### 注解在方法上，删除对应key的缓存。
|  变量  | 默认值  | 解释 |
|  ----  | ----  | ----  |
| namespace  | "" | 命名空间  |
| key  | "" | redis的key，允许使用spel表达式  |
| beforeInvocation  | false | 是否在方法执行之前删除 |
| allowKeyEmpty  | false | 允许缓存值为空  |
## demo
```
@Service
@FastRedisNamespace(namespace = "blog")
@Slf4j
public class RedisService {

    @Autowired
    private BlogMapper blogMapper;

    /**
     * @param id
     * @return
     */
    @FastRedisCache(key = "#id")
    public Blog selectOneByIdCached(Integer id){
        log.info("selectOneByIdCached");
        Blog blog = blogMapper.selectByPrimaryKey(id);
        return blog;
    }

    @FastRedisDelete(key = "#id")
    public void deleteById(Integer id){
        // do delete
        log.info("deleteById");
    }

    /**
     * @param blog
     * @return
     */
    @FastRedisCache(key = "#blog.id")
    public Blog selectOneByIdCached(Blog blog){
        return blogMapper.selectByPrimaryKey(blog.getId());
    }


    /**
     * 更新操作：同时更新缓存
     * 纯put，不查询
     * @param title
     * @param id
     * @return
     */
    @FastRedisCache(key = "#id", cacheType = CacheType.UPDATE)
    @FastRedisDelete(key = "list")
    public Blog updateBlogTitle(String title, Integer id){
        Blog blog = blogMapper.selectByPrimaryKey(id);
        blog.setTitle(title);
        blogMapper.updateByPrimaryKey(blog);
        log.info("update");
        return blog;
    }

    /**
     * 使用自定义缓存注解
     * @return
     */
    @FastRedisCache(key = "list")
    public List<Blog> list(){
        log.info("list");
        return blogMapper.selectByExample(new BlogExample());
    }
}

```
