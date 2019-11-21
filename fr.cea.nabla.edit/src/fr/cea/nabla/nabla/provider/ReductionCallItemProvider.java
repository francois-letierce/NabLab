/**
 * generated by Xtext 2.15.0
 */
package fr.cea.nabla.nabla.provider;


import fr.cea.nabla.nabla.NablaFactory;
import fr.cea.nabla.nabla.NablaPackage;
import fr.cea.nabla.nabla.ReductionCall;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a {@link fr.cea.nabla.nabla.ReductionCall} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ReductionCallItemProvider extends IterableItemProvider {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReductionCallItemProvider(AdapterFactory adapterFactory) {
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

			addReductionPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Reduction feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addReductionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ReductionCall_reduction_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_ReductionCall_reduction_feature", "_UI_ReductionCall_type"),
				 NablaPackage.Literals.REDUCTION_CALL__REDUCTION,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(NablaPackage.Literals.REDUCTION_CALL__ARG);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns ReductionCall.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/ReductionCall"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		return getString("_UI_ReductionCall_type");
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

		switch (notification.getFeatureID(ReductionCall.class)) {
			case NablaPackage.REDUCTION_CALL__ARG:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
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

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createExpression()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createReductionCall()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createArgOrVarRef()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createContractedIf()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createOr()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createAnd()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createEquality()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createComparison()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createPlus()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createMinus()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createMulOrDiv()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createModulo()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createParenthesis()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createUnaryMinus()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createNot()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createIntConstant()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createRealConstant()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createBoolConstant()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createMinConstant()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createMaxConstant()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createFunctionCall()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createBaseTypeConstant()));

		newChildDescriptors.add
			(createChildParameter
				(NablaPackage.Literals.REDUCTION_CALL__ARG,
				 NablaFactory.eINSTANCE.createVectorConstant()));
	}

}
