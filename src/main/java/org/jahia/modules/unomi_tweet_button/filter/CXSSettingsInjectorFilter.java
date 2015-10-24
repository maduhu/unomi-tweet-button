/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, also available here:
 * http://www.jahia.com/license"
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 * Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 * streamlining Enterprise digital projects across channels to truly control
 * time-to-market and TCO, project after project.
 * Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 * marketing teams to collaboratively and iteratively build cutting-edge
 * online business solutions.
 * These, in turn, are securely and easily deployed as modules and apps,
 * reusable across any digital projects, thanks to the Jahia Private App Store Software.
 * Each solution provided by Jahia stems from this overarching vision:
 * Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 * Founded in 2002 and headquartered in Geneva, Switzerland,
 * Jahia Solutions Group has its North American headquarters in Washington DC,
 * with offices in Chicago, Toronto and throughout Europe.
 * Jahia counts hundreds of global brands and governmental organizations
 * among its loyal customers, in more than 20 countries across the globe.
 *
 * For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.unomi_tweet_button.filter;

import org.apache.commons.codec.binary.Base64;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.modules.marketingfactory.admin.ContextServerSettings;
import org.jahia.modules.marketingfactory.admin.ContextServerSettingsService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.io.UnsupportedEncodingException;

/**
 * A filter to inject in the global javascript scope a {@code CXSAuthorizationHeader} variable which value is a Basic Auth header suitable for authentication with the context
 * server.
 *
 * @author Christophe Laprun
 */
public class CXSSettingsInjectorFilter extends AbstractFilter implements ApplicationListener<ApplicationEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CXSSettingsInjectorFilter.class);
    private static final String CXSAUTHORIZATION_HEADER = "CXSAuthorizationHeader";
    private ContextServerSettingsService contextServerSettingsService;
    boolean done = false;

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final String siteKey = renderContext.getSite().getSiteKey();
        ContextServerSettings contextServerSettings = contextServerSettingsService.getSettings(siteKey);

        if (contextServerSettings == null) {
            // force a reload of settings
            contextServerSettingsService.afterPropertiesSet();

            // and re-attempt to get the settings
            contextServerSettings = contextServerSettingsService.getSettings(siteKey);

            if (contextServerSettings == null) {
                logger.error("Couldn't retrieve the settings for site " + siteKey + ". The twitter button component won't be working.");
                return previousOut;
            }
        }

        previousOut += "<script type=\"text/javascript\">var " + CXSAUTHORIZATION_HEADER + " ='" + generateBasicAuth(contextServerSettings) + "';</script>";
        return previousOut;
    }

    private String generateBasicAuth(ContextServerSettings contextServerSettings) {
        String username = contextServerSettings.getContextServerUsername() + ":" + contextServerSettings.getContextServerPassword();
        byte[] usernameBytes;
        try {
            usernameBytes = username.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new JahiaRuntimeException(e);
        }
        return "Basic " + Base64.encodeBase64String(usernameBytes);
    }

    public void setCxsSettingsService(ContextServerSettingsService cxsSettingsService) {
        this.contextServerSettingsService = cxsSettingsService;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextServerSettingsService.ContextServerSettingsChangedEvent) {
            done = false;
        }
    }
}
