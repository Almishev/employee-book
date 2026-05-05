package com.trackng.hours.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Латински букви (българска фонетична подредба + чести двубуквия) → кирилица за единно записване на имена.
 * Кирилица, цифри и разделители остават непроменени.
 */
public final class BulgarianLatinToCyrillic {

	private BulgarianLatinToCyrillic() {
	}

	public static String convertName(String raw) {
		if (raw == null || raw.isBlank()) {
			return raw == null ? null : raw.trim();
		}
		String s = raw.trim().replaceAll("\\s+", " ");
		StringBuilder out = new StringBuilder(s.length() + 8);
		int i = 0;
		while (i < s.length()) {
			int cp = s.codePointAt(i);
			int len = Character.charCount(cp);

			if (isCyrillic(cp) || Character.isDigit(cp) || cp == ' ' || cp == '-' || cp == '\'' || cp == '.') {
				out.appendCodePoint(cp);
				i += len;
				continue;
			}

			if (isAsciiLatin(cp)) {
				Digraph m = matchDigraph(s, i);
				if (m != null) {
					String latinSlice = s.substring(i, i + m.latinLength);
					out.append(applyLatinCase(latinSlice, m.lowerCyrillic));
					i += m.latinLength;
					continue;
				}
				char latin = (char) cp;
				Character cy = LATIN_TO_BG.get(Character.toLowerCase(latin));
				if (cy != null) {
					char outCh = Character.isUpperCase(latin) ? Character.toUpperCase(cy) : cy;
					out.append(outCh);
					i += len;
					continue;
				}
			}

			out.appendCodePoint(cp);
			i += len;
		}
		return out.toString();
	}

	private record Digraph(String lowerCyrillic, int latinLength) {
	}

	private static Digraph matchDigraph(String s, int start) {
		String t = s.substring(start).toLowerCase(Locale.ROOT);
		if (t.startsWith("sht")) {
			return new Digraph("щ", 3);
		}
		if (t.startsWith("zh")) {
			return new Digraph("ж", 2);
		}
		if (t.startsWith("ch")) {
			return new Digraph("ч", 2);
		}
		if (t.startsWith("sh")) {
			return new Digraph("ш", 2);
		}
		if (t.startsWith("ts")) {
			return new Digraph("ц", 2);
		}
		if (t.startsWith("yu")) {
			return new Digraph("ю", 2);
		}
		if (t.startsWith("ya")) {
			return new Digraph("я", 2);
		}
		if (t.startsWith("ja")) {
			return new Digraph("я", 2);
		}
		return null;
	}

	private static String applyLatinCase(String latinSlice, String lowerCyrillic) {
		if (latinSlice.isEmpty()) {
			return lowerCyrillic;
		}
		boolean allLettersUpper = latinSlice.chars().allMatch(ch -> Character.isLetter(ch) && Character.isUpperCase(ch));
		if (allLettersUpper) {
			return lowerCyrillic.toUpperCase(Locale.ROOT);
		}
		boolean title = Character.isUpperCase(latinSlice.charAt(0))
				&& latinSlice.length() > 1
				&& latinSlice.substring(1).equals(latinSlice.substring(1).toLowerCase(Locale.ROOT));
		if (title) {
			if (lowerCyrillic.length() == 1) {
				return lowerCyrillic.toUpperCase(Locale.ROOT);
			}
			return Character.toUpperCase(lowerCyrillic.charAt(0)) + lowerCyrillic.substring(1);
		}
		if (latinSlice.length() == 1 && Character.isUpperCase(latinSlice.charAt(0))) {
			return lowerCyrillic.toUpperCase(Locale.ROOT);
		}
		return lowerCyrillic;
	}

	private static boolean isAsciiLatin(int cp) {
		return (cp >= 'A' && cp <= 'Z') || (cp >= 'a' && cp <= 'z');
	}

	private static boolean isCyrillic(int cp) {
		return (cp >= 0x0400 && cp <= 0x04FF) || (cp >= 0x0500 && cp <= 0x052F);
	}

	private static final Map<Character, Character> LATIN_TO_BG = new HashMap<>();

	static {
		char[][] pairs = {
				{ 'a', 'а' }, { 'b', 'б' }, { 'c', 'ц' }, { 'd', 'д' }, { 'e', 'е' }, { 'f', 'ф' },
				{ 'g', 'г' }, { 'h', 'х' }, { 'i', 'и' }, { 'j', 'й' }, { 'k', 'к' }, { 'l', 'л' },
				{ 'm', 'м' }, { 'n', 'н' }, { 'o', 'о' }, { 'p', 'п' }, { 'q', 'я' }, { 'r', 'р' },
				{ 's', 'с' }, { 't', 'т' }, { 'u', 'у' }, { 'v', 'в' }, { 'w', 'в' }, { 'x', 'ч' },
				{ 'y', 'ъ' }, { 'z', 'з' },
		};
		for (char[] p : pairs) {
			LATIN_TO_BG.put(p[0], p[1]);
		}
	}
}
