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
package org.eclipse.sirius.web.dsl.statemachine.configuration;

import fr.obeo.dsl.designer.sample.statemachine.Command;
import fr.obeo.dsl.designer.sample.statemachine.Event;
import fr.obeo.dsl.designer.sample.statemachine.State;
import fr.obeo.dsl.designer.sample.statemachine.Statemachine;
import fr.obeo.dsl.designer.sample.statemachine.StatemachinePackage;
import fr.obeo.dsl.designer.sample.statemachine.util.StatemachineSwitch;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

/**
 * {@link StatemachineSwitch} implementation providing the {@link EAttribute} to use as label for {@link EObject
 * EObjects} of the {@code Statemachine} domain.
 *
 * @author flatombe
 *
 */
class StatemachineLabelFeatureSwitch extends StatemachineSwitch<EAttribute> {

    @Override
    public EAttribute caseStatemachine(Statemachine object) {
        return StatemachinePackage.Literals.STATEMACHINE__NAME;
    }

    @Override
    public EAttribute caseCommand(Command object) {
        return StatemachinePackage.Literals.COMMAND__NAME;
    }

    @Override
    public EAttribute caseState(State object) {
        return StatemachinePackage.Literals.STATE__NAME;
    }

    @Override
    public EAttribute caseEvent(Event object) {
        return StatemachinePackage.Literals.EVENT__NAME;
    }
}
