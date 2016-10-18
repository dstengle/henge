/**
 * Copyright (C) ${project.inceptionYear} Kenzan - Kyle S. Bober (kbober@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenzan.henge.exception;

import javax.ws.rs.core.Response.Status;

public class RuntimeHengeException extends RuntimeException {

	private static final long serialVersionUID = -3608476531257141338L;
	
	private Status status;
	
	public RuntimeHengeException(Status status) {
		super();
		this.status = status;
	}
	
	public RuntimeHengeException(Status status, String message, Throwable t) {
		super(message, t);
		this.status = status;
	}

	public RuntimeHengeException(Status status, String message) {
		super(message);
		this.status = status;
	}
	
	public RuntimeHengeException(String message) {
		super(message);
	}
	
	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}
	
}
