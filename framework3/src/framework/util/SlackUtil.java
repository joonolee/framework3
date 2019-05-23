package framework.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 슬랙(slack.com) 알림 발송 유틸리티 클래스
 */
public class SlackUtil {

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private SlackUtil() {
	}

	/**
	 * 슬랙(slack.com)으로 메시지를 포스팅 한다.
	 * @param webhookUrl incoming webhook url
	 * @param channel 채널
	 * @param text 메시지
	 * @param username 사용자명
	 * @param icon_emoji 이모지(emoji) 아이콘
	 */
	public static void incomingWebhook(String webhookUrl, String channel, String text, String username, String icon_emoji) {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("channel", channel);
		payload.put("text", text);
		payload.put("username", username);
		payload.put("icon_emoji", icon_emoji);
		HttpUtil.post(webhookUrl, JsonUtil.stringify(payload));
	}
}