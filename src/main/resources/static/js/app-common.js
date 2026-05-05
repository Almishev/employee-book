/**
 * Споделена логика за страниците след вход (ключът трябва да съвпада с api-client.js → HOURS_JWT).
 */
function hoursIsAdmin() {
	return sessionStorage.getItem("hours_role") === "ADMIN";
}

function hoursIsLoggedIn() {
	const t = sessionStorage.getItem(HOURS_JWT);
	return typeof t === "string" && t.trim().length > 0;
}

function hoursRequireAuth() {
	if (!hoursIsLoggedIn()) {
		const ret = encodeURIComponent(location.pathname + location.search);
		location.href = "/login.html?return=" + ret;
		return false;
	}
	return true;
}

function hoursRequireAdmin() {
	if (!hoursRequireAuth()) {
		return false;
	}
	if (!hoursIsAdmin()) {
		location.href = "/record-day.html";
		return false;
	}
	return true;
}

function hoursLogout() {
	sessionStorage.removeItem(HOURS_JWT);
	sessionStorage.removeItem("hours_role");
	sessionStorage.removeItem("hours_name");
	sessionStorage.removeItem("hours_phone");
	location.href = "/login.html";
}

function hoursEscapeHtml(s) {
	return String(s)
		.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;");
}

function hoursEscapeAttr(s) {
	return String(s ?? "")
		.replace(/&/g, "&amp;")
		.replace(/"/g, "&quot;")
		.replace(/</g, "&lt;");
}

function hoursPad(n) {
	return String(n).padStart(2, "0");
}

function hoursToDatetimeLocalValue(d) {
	return (
		d.getFullYear() +
		"-" +
		hoursPad(d.getMonth() + 1) +
		"-" +
		hoursPad(d.getDate()) +
		"T" +
		hoursPad(d.getHours()) +
		":" +
		hoursPad(d.getMinutes())
	);
}

function hoursToDatetimeLocalFromApi(iso) {
	if (!iso) {
		return "";
	}
	const d = new Date(String(iso).replace(" ", "T"));
	if (Number.isNaN(d.getTime())) {
		return "";
	}
	return hoursToDatetimeLocalValue(d);
}

function hoursTodayAt(h, min) {
	const d = new Date();
	d.setSeconds(0, 0);
	d.setHours(h, min, 0, 0);
	return d;
}

/**
 * @param {"record"|"entries"|"hours"|"admin"} active
 */
function hoursInitAppHeader(active) {
	const name = sessionStorage.getItem("hours_name") || "";
	const phone = sessionStorage.getItem("hours_phone") || "";
	const who = document.getElementById("whoami");
	const whoPh = document.getElementById("whoPhone");
	if (who) {
		who.textContent = name;
	}
	if (whoPh) {
		whoPh.textContent = phone ? " · " + phone : "";
	}
	const adminLink = document.getElementById("navAdminLink");
	if (adminLink) {
		adminLink.style.display = hoursIsAdmin() ? "inline" : "none";
	}
	document.querySelectorAll(".app-nav a[data-nav]").forEach((a) => {
		a.classList.toggle("nav-active", a.getAttribute("data-nav") === active);
	});
	const lo = document.getElementById("btnLogout");
	if (lo && !lo.dataset.bound) {
		lo.dataset.bound = "1";
		lo.addEventListener("click", hoursLogout);
	}
}
