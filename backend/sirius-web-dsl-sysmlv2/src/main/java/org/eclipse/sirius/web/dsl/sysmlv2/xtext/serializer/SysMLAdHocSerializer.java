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

import java.io.IOException;
import java.io.Writer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.util.ReplaceRegion;
import org.omg.sysml.lang.sysml.SysMLPackage;

/**
 * This is a temporary, ad-hoc, partial implementation of {@link ISerializer}
 * for {@code SysML v2}. This is needed because the Xtext generic serialization
 * does not work for SysML v2 currently, but we want to keep working on the
 * integration of SysML v2 into Sirius Web.
 * 
 * @author flatombe
 *
 */
public class SysMLAdHocSerializer implements ISerializer {

	@Override
	public String serialize(EObject eObjectToSerialize) {
		if (eObjectToSerialize.eClass().getEPackage() == SysMLPackage.eINSTANCE) {
			final SysmlLimitedSerializer limitedSerializer = new SysmlLimitedSerializer();
			try {
				return limitedSerializer.serialize(eObjectToSerialize);
			} catch (SysmlSerializationException serializationException) {
				// FIXME: we should probably return null and let callers gracefully interpret a
				// null String as a sign of error.
				throw new RuntimeException(serializationException);
			}
		} else {
			throw new IllegalArgumentException(
					"Cannot handle " + eObjectToSerialize + " because it is not from the SysML v2 metamodel.");
		}
	}

	/**
	 * <b>Warning: Unsupported.</b>
	 */
	@Override
	public String serialize(EObject obj, SaveOptions options) {
		throw new UnsupportedOperationException();
	}

	/**
	 * <b>Warning: Unsupported.</b>
	 */
	@Override
	public void serialize(EObject obj, Writer writer, SaveOptions options) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <b>Warning: Unsupported.</b>
	 */
	@Override
	public ReplaceRegion serializeReplacement(EObject obj, SaveOptions options) {
		throw new UnsupportedOperationException();
	}

}
