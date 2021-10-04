/*
 * ao-tempfiles-servlet - Temporary file management in a Servlet environment.
 * Copyright (C) 2017, 2019, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-tempfiles-servlet.
 *
 * ao-tempfiles-servlet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-tempfiles-servlet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-tempfiles-servlet.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoapps.tempfiles.servlet;

import com.aoapps.servlet.attribute.AttributeEE;
import com.aoapps.servlet.attribute.ScopeEE;
import com.aoapps.tempfiles.TempFileContext;
import java.io.Serializable;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionListener;

/**
 * Provides {@linkplain TempFileContext temp file contexts} for {@link ServletContext},
 * {@link ServletRequest}, and {@link HttpSession}.
 */
public final class TempFileContextEE {

	// Make no instances
	private TempFileContextEE() {}

	static final AttributeEE.Name<TempFileContext> ATTRIBUTE =
		AttributeEE.attribute(TempFileContext.class.getName());

	/**
	 * Gets the {@linkplain TempFileContext temp file context} for the given {@linkplain ServletContext servlet context}.
	 *
	 * @throws  IllegalStateException  if the temp files have not been added to the servlet context.
	 */
	public static TempFileContext get(ServletContext servletContext) throws IllegalStateException {
		TempFileContext tempFiles = ATTRIBUTE.context(servletContext).get();
		if(tempFiles == null) throw new IllegalStateException(Initializer.class.getName() + " not added to ServletContext; please use Servlet 3.0+ specification or manually add listener to web.xml.");
		return tempFiles;
	}

	/**
	 * Gets the {@linkplain TempFileContext temp file context} for the given {@linkplain ServletRequest servlet request}.
	 *
	 * @throws  IllegalStateException  if the temp files have not been added to the servlet request.
	 */
	public static TempFileContext get(ServletRequest request) throws IllegalStateException {
		TempFileContext tempFiles = ATTRIBUTE.context(request).get();
		if(tempFiles == null) throw new IllegalStateException(Initializer.class.getName() + " not added to ServletRequest; please use Servlet 3.0+ specification or manually add listener to web.xml.");
		return tempFiles;
	}

	/**
	 * Private instance with full type information.
	 */
	static final ScopeEE.Session.Attribute<HttpSessionTempFileContext> SESSION_ATTRIBUTE_INT =
		ScopeEE.SESSION.attribute(HttpSessionTempFileContext.class.getName());

	public static final ScopeEE.Session.Attribute<?> SESSION_ATTRIBUTE = SESSION_ATTRIBUTE_INT;

	/**
	 * Gets the {@linkplain TempFileContext temp file context} for the given {@linkplain HttpSession session}.
	 * <p>
	 * At this time, temporary files put into the session are deleted when the session is
	 * {@link HttpSessionActivationListener#sessionWillPassivate(javax.servlet.http.HttpSessionEvent) passivated},
	 * at the {@link HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent) end of the session},
	 * or on JVM shutdown.  The temporary files are not {@link Serializable serialized} with the session.
	 * </p>
	 * <p>
	 * TODO: {@link TempFileContext} is not currently {@link Serializable}.  What would it mean to
	 * serialize temp files?  Would the files themselves be wrapped-up into the serialized form?
	 * Would just the filenames be serialized, assuming the underlying temp files are available
	 * to all servlet containers that might get the session?
	 * </p>
	 *
	 * @throws  IllegalStateException  if the temp files have not been added to the session.
	 */
	public static TempFileContext get(HttpSession session) throws IllegalStateException {
		HttpSessionTempFileContext wrapper = SESSION_ATTRIBUTE_INT.context(session).get();
		if(wrapper == null) throw new IllegalStateException(HttpSessionTempFileContext.class.getName() + " not added to HttpSession; please use Servlet 3.0+ specification or manually add listener to web.xml.");
		return wrapper.getTempFiles();
	}

	// TODO: PageScope, compatible with or similar to RegistryEE?
}
