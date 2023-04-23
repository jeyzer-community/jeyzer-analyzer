package org.jeyzer.web.config;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPortal {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigPortal.class);
	
	private static final String ENV_WEB_PORTAL_TITLE_DISPLAY = "JEYZER_WEB_PORTAL_TITLE_DISPLAY";
	private static final String ENV_WEB_PORTAL_PROFILES_DISPLAY = "JEYZER_WEB_PORTAL_PROFILES_DISPLAY";
	private static final String ENV_WEB_PORTAL_PERIOD_DISPLAY = "JEYZER_WEB_PORTAL_PERIOD_DISPLAY";
	private static final String ENV_WEB_PORTAL_TIME_ZONE_DISPLAY = "JEYZER_WEB_PORTAL_TIME_ZONE_DISPLAY";
	private static final String ENV_WEB_PORTAL_AGREEMENT_DISPLAY = "JEYZER_WEB_PORTAL_AGREEMENT_DISPLAY";
	private static final String ENV_WEB_PORTAL_PRIVACY_POLICY_URL = "JEYZER_WEB_PORTAL_PRIVACY_POLICY_URL";
	private static final String ENV_WEB_PORTAL_CAPTCHA_DISPLAY = "JEYZER_WEB_PORTAL_CAPTCHA_DISPLAY";
	private static final String ENV_WEB_PORTAL_CAPTCHA_SECRET_KEY = "JEYZER_WEB_PORTAL_CAPTCHA_SKEY";
	private static final String ENV_WEB_PORTAL_CAPTCHA_WEBSITE_KEY = "JEYZER_WEB_PORTAL_CAPTCHA_WEBSITE_KEY";
	private static final String ENV_WEB_PORTAL_FORUM_URL = "JEYZER_WEB_PORTAL_FORUM_URL";
	
	public static final String SERVLET_PARAM_PORTAL_TITLE_DISPLAY = "portal-title-display";
	public static final String SERVLET_PARAM_PORTAL_PROFILES_DISPLAY = "portal-profiles-display";
	public static final String SERVLET_PARAM_PORTAL_PERIOD_DISPLAY = "portal-period-display";
	public static final String SERVLET_PARAM_PORTAL_TIME_ZONE_DISPLAY = "portal-time-zone-display";
	public static final String SERVLET_PARAM_PORTAL_AGREEMENT_DISPLAY = "portal-agreement-display";
	public static final String SERVLET_PARAM_PORTAL_PRIVACY_POLICY_URL = "portal-privacy-policy-url";
	public static final String SERVLET_PARAM_PORTAL_CAPTCHA_DISPLAY = "portal-captcha-display";
	public static final String SERVLET_PARAM_PORTAL_CAPTCHA_SECRET_KEY = "portal-captcha-secret-key";
	public static final String SERVLET_PARAM_PORTAL_CAPTCHA_WEBSITE_KEY = "portal-captcha-website-key";
	public static final String SERVLET_PARAM_PORTAL_FORUM_URL = "portal-forum-url";

	private boolean displayTitle = false;
	private boolean displayProfiles = false;
	private boolean displayPeriod = false;
	private boolean displayTimeZone = false;

	private boolean displayAgreement = false;
	private String privacyPolicyUrl;

	private boolean displayCaptcha = false;
	private String captchaSecretKey;
	private String captchaWebSiteKey;
	
	private String forumUrl;
	
	public ConfigPortal(ServletConfig servletConfig){
		this.displayTitle = ConfigUtil.loadBoolAttribute(servletConfig, ENV_WEB_PORTAL_TITLE_DISPLAY, SERVLET_PARAM_PORTAL_TITLE_DISPLAY);
		this.displayProfiles = ConfigUtil.loadBoolAttribute(servletConfig, ENV_WEB_PORTAL_PROFILES_DISPLAY, SERVLET_PARAM_PORTAL_PROFILES_DISPLAY);
		this.displayPeriod = ConfigUtil.loadBoolAttribute(servletConfig, ENV_WEB_PORTAL_PERIOD_DISPLAY, SERVLET_PARAM_PORTAL_PERIOD_DISPLAY);
		this.displayTimeZone = ConfigUtil.loadBoolAttribute(servletConfig, ENV_WEB_PORTAL_TIME_ZONE_DISPLAY, SERVLET_PARAM_PORTAL_TIME_ZONE_DISPLAY);
		this.displayAgreement = ConfigUtil.loadBoolAttribute(servletConfig, ENV_WEB_PORTAL_AGREEMENT_DISPLAY, SERVLET_PARAM_PORTAL_AGREEMENT_DISPLAY);
		this.privacyPolicyUrl = ConfigUtil.loadStringAttribute(servletConfig, ENV_WEB_PORTAL_PRIVACY_POLICY_URL, SERVLET_PARAM_PORTAL_PRIVACY_POLICY_URL);
		this.displayCaptcha = ConfigUtil.loadBoolAttribute(servletConfig, ENV_WEB_PORTAL_CAPTCHA_DISPLAY, SERVLET_PARAM_PORTAL_CAPTCHA_DISPLAY);
		this.captchaSecretKey = ConfigUtil.loadStringAttribute(servletConfig, ENV_WEB_PORTAL_CAPTCHA_SECRET_KEY, SERVLET_PARAM_PORTAL_CAPTCHA_SECRET_KEY);
		this.captchaWebSiteKey = ConfigUtil.loadStringAttribute(servletConfig, ENV_WEB_PORTAL_CAPTCHA_WEBSITE_KEY, SERVLET_PARAM_PORTAL_CAPTCHA_WEBSITE_KEY);
		this.forumUrl = ConfigUtil.loadStringAttribute(servletConfig, ENV_WEB_PORTAL_FORUM_URL, SERVLET_PARAM_PORTAL_FORUM_URL);
		validateConfig();
	}

	public boolean isTitleDisplayed() {
		return displayTitle;
	}
	
	public boolean areProfilesDisplayed() {
		return displayProfiles;
	}
	
	public boolean isPeriodDisplayed() {
		return displayPeriod;
	}

	public boolean isAgreementDisplayed() {
		return displayAgreement;
	}

	public boolean isCaptchaDisplayed() {
		return displayCaptcha;
	}

	public String getPrivacyPolicyUrl() {
		return privacyPolicyUrl;
	}
	
	public String getCaptchaSecretKey() {
		return captchaSecretKey;
	}

	public String getCaptchaWebSiteKey() {
		return captchaWebSiteKey;
	}
	
	public String getForumUrl() {
		return forumUrl;
	}

	private void validateConfig() {
		if (this.displayCaptcha && ("secret-key-to-set".equals(this.captchaSecretKey) || "website-key-to-set".equals(captchaWebSiteKey)))
			logger.error("Web analysis will not be accessible as Captcha setup is invalid. When Captcha is enabled, the related Captcha keys must be set. Either set the proper keys or disable the Captcha.");
		if (this.displayCaptcha && !this.displayAgreement)
			logger.error("Web analysis will not be accessible as Captcha MUST be enabled along with the privacy policy agreement. Please activate the privacy policy agreement display.");
	}

	public boolean isTimeZoneDisplayed() {
		return displayTimeZone;
	}
}
