/**
 */
package org.eclipse.sirius.web.view;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Node Description</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sirius.web.view.NodeDescription#getChildrenDescriptions <em>Children Descriptions</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sirius.web.view.ViewPackage#getNodeDescription()
 * @model
 * @generated
 */
public interface NodeDescription extends DiagramElementDescription {
	/**
	 * Returns the value of the '<em><b>Children Descriptions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sirius.web.view.NodeDescription}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children Descriptions</em>' containment reference list.
	 * @see org.eclipse.sirius.web.view.ViewPackage#getNodeDescription_ChildrenDescriptions()
	 * @model containment="true"
	 * @generated
	 */
	EList<NodeDescription> getChildrenDescriptions();

} // NodeDescription