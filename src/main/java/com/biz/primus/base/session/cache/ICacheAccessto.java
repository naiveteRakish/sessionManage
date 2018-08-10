package com.biz.primus.base.session.cache;

import java.util.Map;
/**
 * session 访问接口，如需更换数据存储源，可自行实现该接口
 * @author 胡君琦
 *
 */
public interface ICacheAccessto {
	/**
	 * getSession参数集合
	 * @param sessionId
	 * @return
	 */
	public Map<String,String> getSession(String sessionId);
	/**
	 * 保存session (只有新增和修改功能，并没有删除功能，也就是说无法覆盖整个map)
	 */
	public void save(String sessionId,Map<String,String> params);
	/**
	 * 删除散列集 多key
	 * @param sessionId
	 * @param strings
	 * @return
	 */
	public boolean delete(String sessionId,String[] strings);
	
	/**
	 * 移除session
	 * @param sessionId
	 */
	public boolean remove(String sessionId);
	/**
	 * 设置超时时间 单位秒
	 * @param key
	 * @param time
	 */
	public boolean expire(String key,Long time);
	public boolean isExist(String sessionID);
	
}
