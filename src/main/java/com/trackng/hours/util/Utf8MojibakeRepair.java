package com.trackng.hours.util;

import java.nio.charset.StandardCharsets;

/**
 * Възстановява текст, при който UTF-8 байтове са били погрешно интерпретирани като ISO-8859-1
 * (типично при грешно кодиране на properties или JDBC) — вижда се като „Ð Ð°...“ вместо кирилица.
 */
public final class Utf8MojibakeRepair {

	private Utf8MojibakeRepair() {
	}

	public static String repairMisreadUtf8AsLatin1(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		if (containsCyrillic(s)) {
			return s;
		}
		byte[] raw = s.getBytes(StandardCharsets.ISO_8859_1);
		String candidate = new String(raw, StandardCharsets.UTF_8);
		if (candidate.indexOf('\uFFFD') >= 0) {
			return s;
		}
		if (!containsCyrillic(candidate)) {
			return s;
		}
		return candidate;
	}

	private static boolean containsCyrillic(String str) {
		return str.codePoints().anyMatch(cp ->
				(cp >= 0x0400 && cp <= 0x04FF) || (cp >= 0x0500 && cp <= 0x052F));
	}
}
