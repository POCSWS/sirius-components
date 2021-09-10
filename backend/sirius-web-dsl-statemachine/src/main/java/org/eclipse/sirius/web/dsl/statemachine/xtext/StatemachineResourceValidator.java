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
package org.eclipse.sirius.web.dsl.statemachine.xtext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.service.OperationCanceledError;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.ResourceValidatorImpl;

import com.google.inject.Singleton;

/**
 * Specialization of {@link ResourceValidatorImpl} which hopefully will allow us
 * access to the parsed Xtext model everytime it successfully validates.
 *
 * @author flatombe
 */
@Singleton
public class StatemachineResourceValidator extends ResourceValidatorImpl {

	private final List<PostResourceValidationBehavior> postValidationBehaviors = new ArrayList<>();

	public StatemachineResourceValidator() {
	}
	
	@Override
	public List<Issue> validate(Resource resource, CheckMode mode, CancelIndicator mon) throws OperationCanceledError {
		List<Issue> issues = super.validate(resource, mode, mon);

		this.postValidation(issues, resource);

		return issues;
	}

	private void postValidation(List<Issue> issues, Resource resource) {
		this.postValidationBehaviors.forEach(behavior -> behavior.accept(issues, resource));
	}

	public void addPostValidationBehavior(PostResourceValidationBehavior postValidationBehavior) {
		this.postValidationBehaviors.add(postValidationBehavior);
	}

	public void removePostValidationBehavior(PostResourceValidationBehavior postValidationBehavior) {
		this.postValidationBehaviors.remove(postValidationBehavior);
	}

	/**
	 * A behavior performed right after
	 * {@link IResourceValidator#validate(Resource, CheckMode, CancelIndicator)}.
	 * 
	 * @author flatombe
	 *
	 */
	@FunctionalInterface
	public interface PostResourceValidationBehavior extends BiConsumer<List<Issue>, Resource> {

	};

}
