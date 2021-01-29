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
package org.eclipse.sirius.web.integration.tests;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Used to control the PostgreSQL Docker container used by the integration tests.
 *
 * @author sbegaudeau
 */
public class PostgreSQLTestContainer extends PostgreSQLContainer<PostgreSQLTestContainer> {
    // Nothing on purpose
}