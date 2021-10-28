/**
 */
package org.omg.sysml.lang.sysml.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

import org.omg.sysml.lang.sysml.Definition;
import org.omg.sysml.lang.sysml.SysMLPackage;

/**
 * This is the item provider adapter for a {@link org.omg.sysml.lang.sysml.Definition} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class DefinitionItemProvider extends ClassifierItemProvider {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DefinitionItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addOwnedPortPropertyDescriptor(object);
			addDirectedUsagePropertyDescriptor(object);
			addUsagePropertyDescriptor(object);
			addOwnedStatePropertyDescriptor(object);
			addOwnedConstraintPropertyDescriptor(object);
			addOwnedTransitionPropertyDescriptor(object);
			addOwnedRequirementPropertyDescriptor(object);
			addOwnedCalculationPropertyDescriptor(object);
			addIsVariationPropertyDescriptor(object);
			addVariantMembershipPropertyDescriptor(object);
			addOwnedAnalysisCasePropertyDescriptor(object);
			addVariantPropertyDescriptor(object);
			addOwnedCasePropertyDescriptor(object);
			addOwnedReferencePropertyDescriptor(object);
			addOwnedActionPropertyDescriptor(object);
			addOwnedConnectionPropertyDescriptor(object);
			addOwnedItemPropertyDescriptor(object);
			addOwnedPartPropertyDescriptor(object);
			addOwnedInterfacePropertyDescriptor(object);
			addOwnedAttributePropertyDescriptor(object);
			addOwnedViewPropertyDescriptor(object);
			addOwnedViewpointPropertyDescriptor(object);
			addOwnedRenderingPropertyDescriptor(object);
			addOwnedVerificationCasePropertyDescriptor(object);
			addOwnedEnumerationPropertyDescriptor(object);
			addOwnedAllocationPropertyDescriptor(object);
			addOwnedConcernPropertyDescriptor(object);
			addOwnedOccurrencePropertyDescriptor(object);
			addOwnedUseCasePropertyDescriptor(object);
			addOwnedFlowPropertyDescriptor(object);
			addOwnedUsagePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Owned Port feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedPortPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedPort_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedPort_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_PORT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Directed Usage feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDirectedUsagePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_directedUsage_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_directedUsage_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__DIRECTED_USAGE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Usage feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addUsagePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_usage_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_usage_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__USAGE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned State feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedStatePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedState_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedState_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_STATE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Constraint feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedConstraintPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedConstraint_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedConstraint_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_CONSTRAINT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Transition feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedTransitionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedTransition_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedTransition_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_TRANSITION,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Requirement feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedRequirementPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedRequirement_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedRequirement_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_REQUIREMENT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Calculation feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedCalculationPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedCalculation_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedCalculation_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_CALCULATION,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Is Variation feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addIsVariationPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_isVariation_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_isVariation_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__IS_VARIATION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Variant Membership feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addVariantMembershipPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_variantMembership_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_variantMembership_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__VARIANT_MEMBERSHIP,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Analysis Case feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedAnalysisCasePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedAnalysisCase_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedAnalysisCase_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_ANALYSIS_CASE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Variant feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addVariantPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_variant_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_variant_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__VARIANT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Case feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedCasePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedCase_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedCase_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_CASE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Reference feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedReferencePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedReference_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedReference_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_REFERENCE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Action feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedActionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedAction_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedAction_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_ACTION,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Connection feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedConnectionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedConnection_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedConnection_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_CONNECTION,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Item feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedItemPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedItem_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedItem_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_ITEM,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Part feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedPartPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedPart_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedPart_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_PART,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Interface feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedInterfacePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedInterface_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedInterface_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_INTERFACE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Attribute feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedAttributePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedAttribute_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedAttribute_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_ATTRIBUTE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned View feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedViewPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedView_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedView_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_VIEW,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Viewpoint feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedViewpointPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedViewpoint_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedViewpoint_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_VIEWPOINT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Rendering feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedRenderingPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedRendering_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedRendering_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_RENDERING,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Verification Case feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedVerificationCasePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedVerificationCase_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedVerificationCase_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_VERIFICATION_CASE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Enumeration feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedEnumerationPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedEnumeration_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedEnumeration_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_ENUMERATION,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Allocation feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedAllocationPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedAllocation_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedAllocation_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_ALLOCATION,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Concern feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedConcernPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedConcern_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedConcern_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_CONCERN,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Occurrence feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedOccurrencePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedOccurrence_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedOccurrence_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_OCCURRENCE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Use Case feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedUseCasePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedUseCase_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedUseCase_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_USE_CASE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Flow feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedFlowPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedFlow_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedFlow_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_FLOW,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Owned Usage feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOwnedUsagePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Definition_ownedUsage_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Definition_ownedUsage_feature", "_UI_Definition_type"),
				 SysMLPackage.Literals.DEFINITION__OWNED_USAGE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((Definition)object).getName();
		return label == null || label.length() == 0 ?
			getString("_UI_Definition_type") :
			getString("_UI_Definition_type") + " " + label;
	}


	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(Definition.class)) {
			case SysMLPackage.DEFINITION__IS_VARIATION:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
	 * that can be created under this object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);
	}

}
