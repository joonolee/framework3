package framework.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	 * Attachment 객체
	 */
	public static class Attachment {
		private String fallback;
		private String color;
		private String pretext;
		private String author_name;
		private String author_link;
		private String author_icon;
		private String title;
		private String title_link;
		private String text;
		private String image_url;
		private String thumb_url;
		private String footer;
		private String footer_icon;
		private Long ts = System.currentTimeMillis() / 1000;

		public String getFallback() {
			return fallback;
		}

		public void setFallback(String fallback) {
			this.fallback = fallback;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public String getPretext() {
			return pretext;
		}

		public void setPretext(String pretext) {
			this.pretext = pretext;
		}

		public String getAuthor_name() {
			return author_name;
		}

		public void setAuthor_name(String author_name) {
			this.author_name = author_name;
		}

		public String getAuthor_link() {
			return author_link;
		}

		public void setAuthor_link(String author_link) {
			this.author_link = author_link;
		}

		public String getAuthor_icon() {
			return author_icon;
		}

		public void setAuthor_icon(String author_icon) {
			this.author_icon = author_icon;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTitle_link() {
			return title_link;
		}

		public void setTitle_link(String title_link) {
			this.title_link = title_link;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getImage_url() {
			return image_url;
		}

		public void setImage_url(String image_url) {
			this.image_url = image_url;
		}

		public String getThumb_url() {
			return thumb_url;
		}

		public void setThumb_url(String thumb_url) {
			this.thumb_url = thumb_url;
		}

		public String getFooter() {
			return footer;
		}

		public void setFooter(String footer) {
			this.footer = footer;
		}

		public String getFooter_icon() {
			return footer_icon;
		}

		public void setFooter_icon(String footer_icon) {
			this.footer_icon = footer_icon;
		}

		public Long getTs() {
			return ts;
		}

		public void setTs(Long ts) {
			this.ts = ts;
		}

		/**
		 * Map 으로 변환
		 * @return 맵 객체
		 */
		public Map<String, Object> toMap() {
			Map<String, Object> returnMap = new HashMap<String, Object>();
			if (StringUtil.isNotEmpty(fallback)) {
				returnMap.put("fallback", fallback);
			}
			if (StringUtil.isNotEmpty(color)) {
				returnMap.put("color", color);
			}
			if (StringUtil.isNotEmpty(pretext)) {
				returnMap.put("pretext", escapeHtmlSpecialChars(pretext));
			}
			if (StringUtil.isNotEmpty(author_name)) {
				returnMap.put("author_name", escapeHtmlSpecialChars(author_name));
			}
			if (StringUtil.isNotEmpty(author_link)) {
				returnMap.put("author_link", author_link);
			}
			if (StringUtil.isNotEmpty(author_icon)) {
				returnMap.put("author_icon", author_icon);
			}
			if (StringUtil.isNotEmpty(title)) {
				returnMap.put("title", escapeHtmlSpecialChars(title));
			}
			if (StringUtil.isNotEmpty(title_link)) {
				returnMap.put("title_link", title_link);
			}
			if (StringUtil.isNotEmpty(text)) {
				returnMap.put("text", escapeHtmlSpecialChars(text));
			}
			if (StringUtil.isNotEmpty(image_url)) {
				returnMap.put("image_url", image_url);
			}
			if (StringUtil.isNotEmpty(thumb_url)) {
				returnMap.put("thumb_url", thumb_url);
			}
			if (StringUtil.isNotEmpty(footer)) {
				returnMap.put("footer", escapeHtmlSpecialChars(footer));
			}
			if (StringUtil.isNotEmpty(footer_icon)) {
				returnMap.put("footer_icon", footer_icon);
			}
			if (ts != null) {
				returnMap.put("ts", ts);
			}
			return returnMap;
		}

		/**
		 * Json 으로 변환
		 * @return json
		 */
		public String toJson() {
			return JsonUtil.stringify(toMap());
		}
	}

	/**
	 * 슬랙(slack.com)으로 메시지를 포스팅 한다.(Basic Format)
	 * @param webhookUrl incoming webhook url
	 * @param text 메시지
	 * @param username 사용자명
	 */
	public static void sendMessage(String webhookUrl, String text, String username) {
		Map<String, Object> payloadMap = new HashMap<String, Object>();
		payloadMap.put("mrkdwn", Boolean.TRUE);
		if (StringUtil.isNotEmpty(text)) {
			payloadMap.put("text", escapeHtmlSpecialChars(text));
		}
		if (StringUtil.isNotEmpty(username)) {
			payloadMap.put("username", escapeHtmlSpecialChars(username));
		}
		sendMessage(webhookUrl, JsonUtil.stringify(payloadMap));
	}

	/**
	 * 슬랙(slack.com)으로 메시지를 포스팅 한다.- 간략한 형식(Attachment Format)
	 * @param webhookUrl incoming webhook url
	 * @param attachment Attachment 객체
	 */
	public static void sendMessage(String webhookUrl, Attachment attachment) {
		sendMessage(webhookUrl, null, null, attachment);
	}

	/**
	 * 슬랙(slack.com)으로 메시지를 포스팅 한다.- 간략한 형식(Attachment Format)
	 * @param webhookUrl incoming webhook url
	 * @param attachments Attachment 리스트
	 */
	public static void sendMessage(String webhookUrl, List<Attachment> attachments) {
		sendMessage(webhookUrl, null, null, attachments);
	}

	/**
	 * 슬랙(slack.com)으로 메시지를 포스팅 한다.(Attachment Format)
	 * @param webhookUrl incoming webhook url
	 * @param text 메시지
	 * @param username 사용자명
	 * @param attachment Attachment 객체
	 */
	public static void sendMessage(String webhookUrl, String text, String username, Attachment attachment) {
		sendMessage(webhookUrl, text, username, Arrays.asList(attachment));
	}

	/**
	 * 슬랙(slack.com)으로 메시지를 포스팅 한다.(Attachment Format)
	 * @param webhookUrl incoming webhook url
	 * @param text 메시지
	 * @param username 사용자명
	 * @param attachments Attachment 리스트
	 */
	public static void sendMessage(String webhookUrl, String text, String username, List<Attachment> attachments) {
		Map<String, Object> payloadMap = new HashMap<String, Object>();
		payloadMap.put("mrkdwn", Boolean.TRUE);
		if (StringUtil.isNotEmpty(text)) {
			payloadMap.put("text", escapeHtmlSpecialChars(text));
		}
		if (StringUtil.isNotEmpty(username)) {
			payloadMap.put("username", escapeHtmlSpecialChars(username));
		}
		if (attachments != null) {
			List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
			for (Attachment attachment : attachments) {
				mapList.add(attachment.toMap());
			}
			payloadMap.put("attachments", mapList);
		}
		sendMessage(webhookUrl, JsonUtil.stringify(payloadMap));
	}

	/**
	 * 슬랙(slack.com)으로 메시지를 포스팅 한다.
	 * @param webhookUrl incoming webhook url
	 * @param payloadJson 메시지 json 문자열
	 */
	public static void sendMessage(String webhookUrl, String payloadJson) {
		HttpUtil.post(webhookUrl, payloadJson);
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드
	private static String escapeHtmlSpecialChars(String src) {
		if (src == null) {
			return "";
		}
		StringBuilder result = new StringBuilder(src.length() * 2);
		for (int i = 0; i < src.length(); i++) {
			switch (src.charAt(i)) {
			case '&':
				result.append("&amp;");
				break;
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			default:
				result.append(src.charAt(i));
				break;
			}
		}
		return result.toString();
	}
}