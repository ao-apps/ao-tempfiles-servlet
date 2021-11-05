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
 * along with ao-tempfiles-servlet.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aoapps.tempfiles.servlet;

import com.aoapps.tempfiles.TempFileContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @deprecated  Please use {@link TempFileContextEE}
 */
@Deprecated
public abstract class ServletTempFileContext {

	/** Make no instances. */
	private ServletTempFileContext() {throw new AssertionError();}

	/**
	 * @deprecated  Please use {@link TempFileContextEE#get(javax.servlet.ServletContext)}
	 */
	@Deprecated
	public static TempFileContext getTempFileContext(ServletContext servletContext) throws IllegalStateException {
		return TempFileContextEE.get(servletContext);
	}

	/**
	 * @deprecated  Please use {@link TempFileContextEE#get(javax.servlet.ServletRequest)}
	 */
	@Deprecated
	public static TempFileContext getTempFileContext(ServletRequest request) throws IllegalStateException {
		return TempFileContextEE.get(request);
	}

	/**
	 * @deprecated  Please use {@link TempFileContextEE#SESSION_ATTRIBUTE}
	 */
	@Deprecated
	public static final String SESSION_ATTRIBUTE = TempFileContextEE.SESSION_ATTRIBUTE.getName();

	/**
	 * @deprecated  Please use {@link TempFileContextEE#get(javax.servlet.http.HttpSession)}
	 */
	@Deprecated
	public static TempFileContext getTempFileContext(HttpSession session) throws IllegalStateException {
		return TempFileContextEE.get(session);
	}
}
