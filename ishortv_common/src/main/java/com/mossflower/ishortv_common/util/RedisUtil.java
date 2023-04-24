package com.mossflower.ishortv_common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Component
//@SuppressWarnings("all")
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /*--------------------------------------通用方法-------------------------------------------*/

    /**
     * 设置key对应值的过期时间
     * 会重置过期时间 而不是续期
     *
     * @param key      键
     * @param expire   时间(毫秒)
     * @param timeUnit 时间单位
     * @return 是否成功
     */
    public Boolean setExpire(String key, Long expire, TimeUnit timeUnit) {
        return redisTemplate.expire(key, expire, timeUnit);
    }

    /**
     * 根据key获取过期时间
     *
     * @param key      键
     * @param timeUnit 时间单位
     * @return 时间
     * -1 没有过期时间 永久有效
     * -2 不存在该key对应的值
     * >0 返回过期时间
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 根据key 判断是否存在对应key值
     *
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 移除指定key的过期时间 使其永久有效
     *
     * @param key 键
     * @return 是否成功
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }


    /**
     * 删除缓存
     *
     * @param key 可以传一个值
     * @return 是否成功 删除成功返回true 否则(不存在该值或者其他原因)返回false
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     *
     * @param key 可以传一个值
     * @return 返回删除的个数
     */
    public Long deleteBatch(Collection<String> key) {
        return redisTemplate.delete(key);
    }

    /*- - - - - - - - - - - - - - - - - - - - -  String类型 - - - - - - - - - - - - - - - - - - - -*/

    /**
     * 根据key获取值
     *
     * @param key 键
     * @return 值
     * 不存在返回null 存在返回对应的值
     */
    public Object getStringValue(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 将值放入缓存 永久有效
     *
     * @param key   键
     * @param value 值
     */
    public void setStringValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 将值放入缓存并设置时间
     *
     * @param key    键
     * @param value  值
     * @param expire 负数表示永久有效
     */
    public void setStringValue(String key, String value, Long expire, TimeUnit timeUnit) {
        if (expire > 0) {
            redisTemplate.opsForValue().set(key, value, expire, timeUnit);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }


    /**
     * 批量添加 key (重复的键会覆盖)
     *
     * @param keyAndValue key-value
     */
    public void batchSetStringValue(Map<String, String> keyAndValue) {

        redisTemplate.opsForValue().multiSet(keyAndValue);
    }

    /**
     * 批量添加 key-value 只有在键不存在时,才添加
     * map 中只要有一个key存在,则全部不添加
     *
     * @param keyAndValue key-value
     */
    public void batchSetIfAbsent(Map<String, String> keyAndValue) {
        redisTemplate.opsForValue().multiSetIfAbsent(keyAndValue);
    }

    /**
     * 对一个 key-value 的值进行加减操作,
     * 如果该 key 不存在 将创建一个key 并赋值该 number
     * 如果 key 存在,但 value 不是长整型 ,将报错
     *
     * @param key    键
     * @param number 要加减的值
     * @return 加减后的值
     */
    public Long increment(String key, Long number) {
        return redisTemplate.opsForValue().increment(key, number);
    }

    /**
     * 对一个 key-value 的值进行加减操作,
     * 如果该 key 不存在 将创建一个key 并赋值该 number
     * 如果 key 存在,但 value 不是 纯数字 ,将报错
     *
     * @param key    键
     * @param number 要加减的值
     * @return 加减后的值
     */
    public Double increment(String key, Double number) {
        return redisTemplate.opsForValue().increment(key, number);
    }

    /**
     * 对一个 key-value 的值进行加减操作,
     * 如果该 key 不存在 将创建一个key 并赋值该 number
     * 如果 key 存在,但 value 不是长整型 ,将报错
     *
     * @param key    键
     * @param number 要加减的值
     * @return 加减后的值
     */
    public Long decrement(String key, Long number) {
        return redisTemplate.opsForValue().decrement(key, number);
    }

}
