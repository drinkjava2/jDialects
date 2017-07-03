/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.drinkjava2.tinyjdbc;

public class TinyJdbcException extends RuntimeException {
	private static final long serialVersionUID = 1352967226525740020L;

	public TinyJdbcException(String msg) {
		super(msg);
	}

	public TinyJdbcException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public static Object throwEX(String message) {
		throw new TinyJdbcException(message);
	}

	public static void throwEX(Throwable e, String message) {
		throw new TinyJdbcException(message, e);
	}

	public static void eatException(Exception e) {
		// do nothing here
	}
}
