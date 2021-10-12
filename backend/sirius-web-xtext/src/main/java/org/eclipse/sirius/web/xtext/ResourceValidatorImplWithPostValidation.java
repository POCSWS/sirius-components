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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.service.OperationCanceledError;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.ResourceValidatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * Specialization of {@link ResourceValidatorImpl} (which is the default
 * implementation of {@link IResourceValidator}) that implements
 * {@link IResourceValidatorWithPostValidation}.
 * 
 * Note that some Xtext-based languages may rely on their own custom
 * implementation of {@link IResourceValidator}. In that case, a new specific
 * subtype of their implementation also implementing
 * {@link IResourceValidatorWithPostValidation} should be used instead of this.
 *
 * @author flatombe
 */
@Singleton
public class ResourceValidatorImplWithPostValidation extends ResourceValidatorImpl
		implements IResourceValidatorWithPostValidation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceValidatorImplWithPostValidation.class);

	private final List<PostResourceValidationBehavior> postValidationBehaviors = new ArrayList<>();

	public ResourceValidatorImplWithPostValidation() {
	}

	@Override
	public void addPostValidationBehavior(PostResourceValidationBehavior postValidationBehavior) {
		Objects.requireNonNull(postValidationBehavior);

		this.postValidationBehaviors.add(postValidationBehavior);
	}

	@Override
	public void removePostValidationBehavior(PostResourceValidationBehavior postValidationBehavior) {
		Objects.requireNonNull(postValidationBehavior);

		this.postValidationBehaviors.remove(postValidationBehavior);
	}

	@Override
	public List<Issue> validate(Resource resource, CheckMode mode, CancelIndicator mon) throws OperationCanceledError {
		List<Issue> issues = super.validate(resource, mode, mon);

		this.postValidation(issues, resource);

		return issues;
	}

	/**
	 * Performs the post-validation phase.
	 * 
	 * @param issues   the (non-{@code null}) result of the validation.
	 * @param resource the (non-{@code null}) {@link Resource} that was validated.
	 */
	private void postValidation(List<Issue> issues, Resource resource) {
		LOGGER.trace("Performing " + postValidationBehaviors.size() + " post-validation behaviors for resource "
				+ resource.getURI());
		this.postValidationBehaviors.forEach(behavior -> behavior.accept(issues, resource));
	}

}
