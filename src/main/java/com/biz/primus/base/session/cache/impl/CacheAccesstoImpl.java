package com.biz.primus.base.session.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import com.biz.primus.base.session.cache.ICacheAccessto;

/**
 * session存取接口,如果某些方法业务变更，可自行继承重写某些方法
 * @author 胡君琦
 *
 */
public class CacheAccesstoImpl implements ICacheAccessto{
	
	private RedisTemplate<String, String> redisTemplate;
	
	
	
	/**
	 * 获取session
	 */
	public Map<String,String> getSession(String sessionId){
		Map<Object, Object> map = redisTemplate.opsForHash().entries(sessionId);
		Map<String ,String> mmap = new HashMap<>(map.size()+1,1);
		for (Entry<Object, Object> element : map.entrySet()) {
			mmap.put(element.getKey().toString(), element.getValue().toString());
		}
		return mmap;
	}
	/**
	 * 保存session (只有新增和修改功能，并没有删除功能，也就是说无法覆盖整个map)
	 */
	public void save(String sessionId,Map<String,String> params){
		redisTemplate.opsForHash().putAll(sessionId, params);
	}
	
	/**
	 * 删除散列集 多key
	 */
	public boolean delete(String sessionId,String... strings){
		return redisTemplate.opsForHash().delete(sessionId,strings)==1L;
	}


	public CacheAccesstoImpl(RedisTemplate<String, String> rt) {
		super();
		this.redisTemplate = rt;
	}
	public CacheAccesstoImpl() {
		super();
	}
	public void setRt(RedisTemplate<String, String> rt) {
		this.redisTemplate = rt;
	}

	@Override
	public boolean remove(String sessionId) {
		return redisTemplate.delete(sessionId);
	}

	@Override
	public boolean expire(String key, Long time) {
		return redisTemplate.expire(key, time, TimeUnit.SECONDS);
	}
	@Override
	public boolean isExist(String sessionID) {
		return redisTemplate.hasKey(sessionID);
	}
}
