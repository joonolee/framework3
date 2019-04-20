package framework.cache;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * 기본 캐시 구현체 (http://ehcache.org/)
 */
public final class EhCache extends AbstractCache {

	/**
	 * 싱글톤 객체
	 */
	private static EhCache uniqueInstance;

	/**
	 * 캐시 매니저
	 */
	private final CacheManager cacheManager;

	/**
	 * 캐시 오브젝트
	 */
	private final net.sf.ehcache.Cache cache;

	/**
	 * 기본 캐시 이름
	 */
	private static final String CACHE_NAME = "framework3";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private EhCache() {
		cacheManager = CacheManager.create();
		cacheManager.addCache(CACHE_NAME);
		cache = cacheManager.getCache(CACHE_NAME);
	}

	/**
	 * 객체의 인스턴스를 리턴해준다.
	 *
	 * @return EhCache 객체의 인스턴스
	 */
	public synchronized static EhCache getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new EhCache();
		}
		return uniqueInstance;
	}

	@Override
	public void set(String key, Object value, int seconds) {
		Element e = new Element(key, value);
		e.setTimeToLive(seconds);
		cache.put(e);
	}

	@Override
	public Object get(String key) {
		Element e = cache.get(key);
		return (e == null) ? null : e.getObjectValue();
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
	public synchronized long incr(String key, int by) {
		Element e = cache.get(key);
		if (e == null) {
			return -1;
		}
		long newValue = ((Number) e.getObjectValue()).longValue() + by;
		Element newE = new Element(key, newValue);
		newE.setTimeToLive(e.getTimeToLive());
		cache.put(newE);
		return newValue;
	}

	@Override
	public synchronized long decr(String key, int by) {
		Element e = cache.get(key);
		if (e == null) {
			return -1;
		}
		long newValue = ((Number) e.getObjectValue()).longValue() - by;
		Element newE = new Element(key, newValue);
		newE.setTimeToLive(e.getTimeToLive());
		cache.put(newE);
		return newValue;
	}

	@Override
	public void delete(String key) {
		cache.remove(key);
	}

	@Override
	public void clear() {
		cache.removeAll();
	}
}