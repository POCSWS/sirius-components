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
package org.eclipse.sirius.web.xtext;

import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

/**
 * which allows us to perform arbitrary behaviors after validating a
 * {@link Resource}. <br/>
 * In particular, this allows us to react to changes in a textual representation
 * that have an impact on the semantic model being represented.
 * 
 * @author flatombe
 *
 */
public interface IResourceValidatorWithPostValidation extends IResourceValidator {
	/**
	 * A behavior performed right after the usual resource validation.
	 * <ul>
	 * <li>The first parameter is the {@link List} of {@link Issue} as returned by
	 * {@link IResourceValidator#validate(Resource, CheckMode, CancelIndicator)},
	 * including all syntactic and semantic issues found during validation of the
	 * resource.</li>
	 * <li>The second parameter is the {@link Resource} that was validated (also
	 * first argument of the
	 * {@link {@link IResourceValidator#validate(Resource, CheckMode, CancelIndicator)}}
	 * call).</li>
	 * </ul>
	 * 
	 * @author flatombe
	 *
	 */
	@FunctionalInterface
	public interface PostResourceValidationBehavior extends BiConsumer<List<Issue>, Resource> {
	};

	/**
	 * Adds a {@link PostResourceValidationBehavior}.
	 * {@link PostResourceValidationBehavior} instances get executed in the order
	 * they were added to {@code this}, right after every call to
	 * {@link IResourceValidator#validate(Resource, CheckMode, CancelIndicator)} of
	 * {@code this}.
	 * 
	 * @param postValidationBehavior the (non-{@code null})
	 *                               {@link PostResourceValidationBehavior} to add.
	 */
	void addPostValidationBehavior(PostResourceValidationBehavior postValidationBehavior);

	/**
	 * Removes a {@link PostResourceValidationBehavior}.
	 * 
	 * @param postValidationBehavior the (non-{@code null})
	 *                               {@link PostResourceValidationBehavior} to
	 *                               remove.
	 */
	void removePostValidationBehavior(PostResourceValidationBehavior postValidationBehavior);
}
