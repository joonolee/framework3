/** 
 * @(#)Redis.java
 */
package framework.cache;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;
import framework.config.Config;

/**
 * Redis 캐시 구현체 (http://redis.io/)
 */
public class Redis extends AbstractCache {
	/**
	 * 싱글톤 객체
	 */
	private static Redis _uniqueInstance;

	/**
	 * 캐시 클라이언트
	 */
	private Jedis _client;

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private Redis() {
		String redisHost = null;
		if (_getConfig().containsKey("redis.host")) {
			redisHost = _getConfig().getString("redis.host");
		} else {
			throw new RuntimeException("redis의 호스트설정이 누락되었습니다.");
		}
		_client = new Jedis(redisHost);
	}

	/** 
	 * 객체의 인스턴스를 리턴해준다.
	 * 
	 * @return Memcached 객체의 인스턴스
	 */
	public synchronized static Redis getInstance() {
		if (_uniqueInstance == null) {
			_uniqueInstance = new Redis();
		}
		return _uniqueInstance;
	}

	@Override
	public void add(String key, Object value, int seconds) {
		_client.append(key, value.toString());
		_client.expire(key, seconds);
	}

	@Override
	public void set(String key, Object value, int seconds) {
		_client.set(key, value.toString());
		_client.expire(key, seconds);

	}

	@Override
	public void replace(String key, Object value, int seconds) {
		if (_client.get(key) == null) {
			return;
		}
		_client.set(key, value.toString());
		_client.expire(key, seconds);
	}

	@Override
	public Object get(String key) {
		return _client.get(key);
	}

	@Override
	public Map<String, Object> get(String[] keys) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (String key : keys) {
			resultMap.put(key, get(key));
		}
		return resultMap;
	}

	@Override
	public long incr(String key, int by) {
		return _client.incrBy(key, by);
	}

	@Override
	public long decr(String key, int by) {
		return _client.decrBy(key, by);
	}

	@Override
	public void delete(String key) {
		_client.del(key);
	}

	@Override
	public void clear() {
		_client.flushAll();
	}

	@Override
	public void stop() {
		_client.shutdown();
	}

	////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	/**
	* 설정파일(config.properties)에서 값을 읽어오는 클래스를 리턴한다.
	* @return 설정객체
	*/
	private Config _getConfig() {
		return Config.getInstance();
	}
}
