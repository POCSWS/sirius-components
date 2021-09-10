package org.eclipse.sirius.web.dsl.statemachine.xtext;

import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry;
import org.eclipse.xtext.validation.IResourceValidator;

import com.google.inject.Binder;

import fr.obeo.dsl.designer.sample.StatemachineRuntimeModule;

/**
 * Specialization of {@link StatemachineRuntimeModule} to bind our
 * {@link IResourceValidator} implementation.
 * 
 * @author flatombe
 *
 */
public class StatemachineSiriusWebRuntimeModule extends StatemachineRuntimeModule {

	public StatemachineSiriusWebRuntimeModule() {
		super();
	}

	@Override
	public void configure(Binder binder) {
		super.configure(binder);
	}


	@Override
	public Registry bindIResourceServiceProvider$Registry() {
		return super.bindIResourceServiceProvider$Registry();
	}

	public Class<? extends IResourceValidator> bindIResourceValidator() {
		return StatemachineResourceValidator.class;
	}

	@Override
	public Class<? extends IGrammarAccess> bindIGrammarAccess() {
		return super.bindIGrammarAccess();
	}
}
