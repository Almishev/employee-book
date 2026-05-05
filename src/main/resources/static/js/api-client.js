const HOURS_JWT = "hours_jwt";

/**
 * Базов URL на API: същият хост като страницата (типично за production).
 * При отваряне като file:// ползва localhost:8086 само за локален тест.
 */
function apiBase() {
	if (location.protocol === "http:" || location.protocol === "https:") {
		return location.origin;
	}
	return "http://localhost:8086";
}

function authHeaders() {
	const h = {};
	const t = sessionStorage.getItem(HOURS_JWT);
	if (t) h["Authorization"] = "Bearer " + t;
	return h;
}

async function apiRequest(method, path, body) {
	const hadToken = !!sessionStorage.getItem(HOURS_JWT);
	const url = apiBase() + path;
	const opts = {
		method,
		headers: { Accept: "application/json", ...authHeaders() },
	};
	if (body !== undefined) {
		opts.headers["Content-Type"] = "application/json";
		opts.body = JSON.stringify(body);
	}
	const res = await fetch(url, opts);
	const text = await res.text();
	let data = null;
	if (text) {
		try {
			data = JSON.parse(text);
		} catch {
			data = text;
		}
	}
	if (res.status === 401 && hadToken && !path.startsWith("/api/auth/login")) {
		sessionStorage.removeItem(HOURS_JWT);
		sessionStorage.removeItem("hours_role");
		sessionStorage.removeItem("hours_name");
		sessionStorage.removeItem("hours_phone");
		const p = typeof location !== "undefined" ? location.pathname : "";
		if (p.endsWith(".html") && !p.endsWith("/login.html") && !p.endsWith("login.html")) {
			location.href = "/login.html?return=" + encodeURIComponent(location.pathname + location.search);
		}
	}
	return { ok: res.ok, status: res.status, statusText: res.statusText, data };
}

function toLocalIsoString(d) {
	const pad = (n) => String(n).padStart(2, "0");
	return (
		d.getFullYear() +
		"-" +
		pad(d.getMonth() + 1) +
		"-" +
		pad(d.getDate()) +
		"T" +
		pad(d.getHours()) +
		":" +
		pad(d.getMinutes()) +
		":" +
		pad(d.getSeconds())
	);
}

function fromDatetimeLocal(val) {
	if (!val) return null;
	const d = new Date(val);
	if (Number.isNaN(d.getTime())) return null;
	return toLocalIsoString(d);
}
