package org.eclipse.sirius.web.dsl.statemachine.xtext;

import org.eclipse.xtext.validation.IResourceValidator;

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

	/**
	 * This binding gets picked up reflexively thanks to the method name.
	 */
	public Class<? extends IResourceValidator> bindIResourceValidator() {
		return StatemachineResourceValidator.class;
	}
}
