package org.eclipse.sirius.web.dsl.statemachine.xtext;

import com.google.inject.Injector;

/**
 * Centralizes the initialization of the Xtext setup for the Statemachine DSL in
 * a Sirius Web context.
 * 
 * @author flatombe
 *
 */
public class StatemachineXtextHelper {

	private static final Injector INJECTOR = StatemachineSiriusWebIdeSetup.doSetup();

	public static Injector getInjector() {
		return INJECTOR;
	}
}
