/*******************************************************************************
 * Copyright (c) 2021 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.web.dsl.sysmlv2.xtext.serializer;

/**
 * An {@link Exception} for cases where {@link SysmlLimitedSerializer}
 * explicitly does not support serializing a SysML v2 model.
 * 
 * @author flatombe
 *
 */
public class SysmlSerializationException extends Exception {

	private static final long serialVersionUID = 1L;

	public SysmlSerializationException(String message) {
		super(message);
	}

	public SysmlSerializationException(Throwable parent) {
		super(parent);
	}

	public SysmlSerializationException(String message, Throwable parent) {
		super(message, parent);
	}

}
