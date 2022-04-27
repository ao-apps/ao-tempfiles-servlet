/*
 * ao-tempfiles-servlet - Temporary file management in a Servlet environment.
 * Copyright (C) 2021, 2022  AO Industries, Inc.
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
module com.aoapps.tempfiles.servlet {
  exports com.aoapps.tempfiles.servlet;
  // Direct
  requires com.aoapps.servlet.util; // <groupId>com.aoapps</groupId><artifactId>ao-servlet-util</artifactId>
  requires com.aoapps.tempfiles; // <groupId>com.aoapps</groupId><artifactId>ao-tempfiles</artifactId>
  requires javax.servlet.api; // <groupId>javax.servlet</groupId><artifactId>javax.servlet-api</artifactId>
} // TODO: Avoiding rewrite-maven-plugin-4.22.2 truncation
