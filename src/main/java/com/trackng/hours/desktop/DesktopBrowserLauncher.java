package com.trackng.hours.desktop;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * При десктоп инсталация (профил {@code desktop}) отваря началния адрес в подразбирания браузър след старт на сървъра.
 */
@Component
@Profile("desktop")
public class DesktopBrowserLauncher {

	private static final Logger log = LoggerFactory.getLogger(DesktopBrowserLauncher.class);

	@Value("${server.port:8086}")
	private String serverPort;

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		String url = "http://localhost:" + serverPort + "/";
		try {
			if (openOnWindows(url)) {
				return;
			}
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(URI.create(url));
				log.info("Отворен браузър: {}", url);
				return;
			}
		} catch (Exception e) {
			log.warn("Неуспешно отваряне на браузър за {}: {}", url, e.getMessage());
		}
		log.warn("Отвори ръчно: {}", url);
	}

	private static boolean openOnWindows(String url) throws IOException {
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (!os.contains("win")) {
			return false;
		}
		// start "" <url> — подразбирания браузър; работи и при headless JVM
		new ProcessBuilder("cmd", "/c", "start", "", url).start();
		return true;
	}
}
