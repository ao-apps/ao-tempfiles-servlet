/*
 * ao-tempfiles-servlet - Temporary file management in a Servlet environment.
 * Copyright (C) 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.servlet.attribute.ScopeEE;
import com.aoapps.tempfiles.TempFileContext;
import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

class HttpSessionTempFileContext implements Serializable, HttpSessionActivationListener, Closeable {

  private static final long serialVersionUID = 1L;

  private transient TempFileContext tempFiles;

  HttpSessionTempFileContext(ServletContext servletContext) {
    tempFiles = new TempFileContext(
      ScopeEE.Application.TEMPDIR.context(servletContext).get()
    );
  }

  @Override
  public void sessionWillPassivate(HttpSessionEvent event) {
    if (tempFiles != null) {
      try {
        tempFiles.close();
      } catch (IOException e) {
        event.getSession().getServletContext().log("Error deleting temporary files", e);
      }
      tempFiles = null;
    }
  }

  @Override
  public void sessionDidActivate(HttpSessionEvent event) {
    if (tempFiles == null) {
      tempFiles = new TempFileContext(
        ScopeEE.Application.TEMPDIR.context(event.getSession().getServletContext()).get()
      );
    }
  }

  TempFileContext getTempFiles() {
    TempFileContext _tempFiles = tempFiles;
    if (_tempFiles == null) {
      throw new IllegalStateException(HttpSessionTempFileContext.class.getName() + ".tempFiles is null");
    }
    return _tempFiles;
  }

  @Override
  public void close() throws IOException {
    TempFileContext _tempFiles = tempFiles;
    if (_tempFiles != null) {
      tempFiles = null;
      _tempFiles.close();
    }
  }
}
