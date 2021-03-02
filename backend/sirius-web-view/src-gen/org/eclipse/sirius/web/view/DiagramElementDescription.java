/**
 */
package org.eclipse.sirius.web.view;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Diagram Element Description</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sirius.web.view.DiagramElementDescription#getDomainType <em>Domain Type</em>}</li>
 *   <li>{@link org.eclipse.sirius.web.view.DiagramElementDescription#getSemanticCandidatesExpression <em>Semantic Candidates Expression</em>}</li>
 *   <li>{@link org.eclipse.sirius.web.view.DiagramElementDescription#getCreationMode <em>Creation Mode</em>}</li>
 *   <li>{@link org.eclipse.sirius.web.view.DiagramElementDescription#getLabelExpression <em>Label Expression</em>}</li>
 *   <li>{@link org.eclipse.sirius.web.view.DiagramElementDescription#getStyle <em>Style</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sirius.web.view.ViewPackage#getDiagramElementDescription()
 * @model abstract="true"
 * @generated
 */
public interface DiagramElementDescription extends EObject {
	/**
	 * Returns the value of the '<em><b>Domain Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Domain Type</em>' attribute.
	 * @see #setDomainType(String)
	 * @see org.eclipse.sirius.web.view.ViewPackage#getDiagramElementDescription_DomainType()
	 * @model
	 * @generated
	 */
	String getDomainType();

	/**
	 * Sets the value of the '{@link org.eclipse.sirius.web.view.DiagramElementDescription#getDomainType <em>Domain Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Domain Type</em>' attribute.
	 * @see #getDomainType()
	 * @generated
	 */
	void setDomainType(String value);

	/**
	 * Returns the value of the '<em><b>Semantic Candidates Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Semantic Candidates Expression</em>' attribute.
	 * @see #setSemanticCandidatesExpression(String)
	 * @see org.eclipse.sirius.web.view.ViewPackage#getDiagramElementDescription_SemanticCandidatesExpression()
	 * @model
	 * @generated
	 */
	String getSemanticCandidatesExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.sirius.web.view.DiagramElementDescription#getSemanticCandidatesExpression <em>Semantic Candidates Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Semantic Candidates Expression</em>' attribute.
	 * @see #getSemanticCandidatesExpression()
	 * @generated
	 */
	void setSemanticCandidatesExpression(String value);

	/**
	 * Returns the value of the '<em><b>Creation Mode</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.sirius.web.view.Mode}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Creation Mode</em>' attribute.
	 * @see org.eclipse.sirius.web.view.Mode
	 * @see #setCreationMode(Mode)
	 * @see org.eclipse.sirius.web.view.ViewPackage#getDiagramElementDescription_CreationMode()
	 * @model
	 * @generated
	 */
	Mode getCreationMode();

	/**
	 * Sets the value of the '{@link org.eclipse.sirius.web.view.DiagramElementDescription#getCreationMode <em>Creation Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Creation Mode</em>' attribute.
	 * @see org.eclipse.sirius.web.view.Mode
	 * @see #getCreationMode()
	 * @generated
	 */
	void setCreationMode(Mode value);

	/**
	 * Returns the value of the '<em><b>Label Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Label Expression</em>' attribute.
	 * @see #setLabelExpression(String)
	 * @see org.eclipse.sirius.web.view.ViewPackage#getDiagramElementDescription_LabelExpression()
	 * @model
	 * @generated
	 */
	String getLabelExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.sirius.web.view.DiagramElementDescription#getLabelExpression <em>Label Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Label Expression</em>' attribute.
	 * @see #getLabelExpression()
	 * @generated
	 */
	void setLabelExpression(String value);

	/**
	 * Returns the value of the '<em><b>Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Style</em>' containment reference.
	 * @see #setStyle(Style)
	 * @see org.eclipse.sirius.web.view.ViewPackage#getDiagramElementDescription_Style()
	 * @model containment="true"
	 * @generated
	 */
	Style getStyle();

	/**
	 * Sets the value of the '{@link org.eclipse.sirius.web.view.DiagramElementDescription#getStyle <em>Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Style</em>' containment reference.
	 * @see #getStyle()
	 * @generated
	 */
	void setStyle(Style value);

} // DiagramElementDescription