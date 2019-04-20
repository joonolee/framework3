package framework.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import framework.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Redis 캐시 구현체 (http://redis.io/)
 */
public final class Redis extends AbstractCache {

	/**
	 * 싱글톤 객체
	 */
	private static Redis uniqueInstance;

	/**
	 * 타임아웃 값 (ms)
	 */
	private static final int TIMEOUT = 500;

	/**
	 * 캐시 클라이언트 Pool
	 */
	private final ShardedJedisPool pool;

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private Redis() {
		List<JedisShardInfo> shards;
		if (Config.getInstance().containsKey("redis.servers")) {
			shards = getAddresses(Config.getInstance().getString("redis.servers"));
		} else {
			throw new RuntimeException("redis의 호스트설정이 누락되었습니다.");
		}
		pool = new ShardedJedisPool(new JedisPoolConfig(), shards);
	}

	/**
	 * 객체의 인스턴스를 리턴해준다.
	 *
	 * @return Redis 객체의 인스턴스
	 */
	public synchronized static Redis getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Redis();
		}
		return uniqueInstance;
	}

	@Override
	public void set(String key, Object value, int seconds) {
		set(serialize(key), serialize(value), seconds);
	}

	public void set(byte[] key, byte[] value, int seconds) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.setex(key, seconds, value);
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
			}
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}
	}

	@Override
	public Object get(String key) {
		return get(serialize(key));
	}

	public Object get(byte[] key) {
		ShardedJedis jedis = null;
		Object value = null;
		try {
			jedis = pool.getResource();
			value = deserialize(jedis.get(key));
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
			}
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}
		return value;
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
		return incr(serialize(key), by);
	}

	public long incr(byte[] key, int by) {
		ShardedJedis jedis = null;
		Long value = null;
		try {
			jedis = pool.getResource();
			value = jedis.incrBy(key, by);
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
			}
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}
		if (value == null) {
			value = Long.valueOf(-1);
		}
		return value;
	}

	@Override
	public long decr(String key, int by) {
		return decr(serialize(key), by);
	}

	public long decr(byte[] key, int by) {
		ShardedJedis jedis = null;
		Long value = null;
		try {
			jedis = pool.getResource();
			value = jedis.decrBy(key, by);
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
			}
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}
		if (value == null) {
			value = Long.valueOf(-1);
		}
		return value;
	}

	@Override
	public void delete(String key) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.del(key);
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
			}
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}
	}

	@Override
	public void clear() {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			for (Jedis j : jedis.getAllShards()) {
				j.flushAll();
			}
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				pool.returnBrokenResource(jedis);
			}
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	/**
	 * 문자열에서 redis 호스트 주소를 파싱하여 리턴한다.
	 * @param str 스페이스로 구분된 주소문자열
	 * @return 샤드주소객체
	 */
	private List<JedisShardInfo> getAddresses(String str) {
		if (str == null || "".equals(str.trim())) {
			throw new IllegalArgumentException("redis의 호스트설정이 누락되었습니다.");
		}
		ArrayList<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		for (String addr : str.split("(?:\\s|,)+")) {
			if ("".equals(addr)) {
				continue;
			}
			int sep = addr.lastIndexOf(':');
			if (sep < 1) {
				throw new IllegalArgumentException("서버설정이 잘못되었습니다. 형식=>호스트:포트");
			}
			shards.add(new JedisShardInfo(addr.substring(0, sep), Integer.valueOf(addr.substring(sep + 1)), TIMEOUT));
		}
		assert !shards.isEmpty() : "redis의 호스트설정이 누락되었습니다.";
		return shards;
	}

	/**
	 * 객체를 바이트배열로 직렬화 한다.
	 * @param obj 직렬화할 객체
	 * @return 바이트배열
	 */
	private byte[] serialize(Object obj) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			return baos.toByteArray();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 바이트배열을 객체로 역직렬화 한다.
	 * @param bytes 바이트배열
	 * @return 역직렬화된 객체
	 */
	private Object deserialize(byte[] bytes) {
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}