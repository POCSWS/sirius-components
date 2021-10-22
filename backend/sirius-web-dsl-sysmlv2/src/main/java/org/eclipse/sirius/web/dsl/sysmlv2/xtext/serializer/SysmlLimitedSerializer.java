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
package org.eclipse.sirius.web.dsl.sysmlv2.xtext.serializer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.util.EmfFormatter;
import org.omg.sysml.lang.sysml.ActionDefinition;
import org.omg.sysml.lang.sysml.Classifier;
import org.omg.sysml.lang.sysml.ConjugatedPortDefinition;
import org.omg.sysml.lang.sysml.ConjugatedPortTyping;
import org.omg.sysml.lang.sysml.Definition;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Expression;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.FeatureDirectionKind;
import org.omg.sysml.lang.sysml.FeatureMembership;
import org.omg.sysml.lang.sysml.FeatureTyping;
import org.omg.sysml.lang.sysml.FeatureValue;
import org.omg.sysml.lang.sysml.FlowConnectionUsage;
import org.omg.sysml.lang.sysml.LifeClass;
import org.omg.sysml.lang.sysml.Membership;
import org.omg.sysml.lang.sysml.MultiplicityRange;
import org.omg.sysml.lang.sysml.Namespace;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.OccurrenceUsage;
import org.omg.sysml.lang.sysml.Package;
import org.omg.sysml.lang.sysml.ParameterMembership;
import org.omg.sysml.lang.sysml.PartDefinition;
import org.omg.sysml.lang.sysml.PortDefinition;
import org.omg.sysml.lang.sysml.PortUsage;
import org.omg.sysml.lang.sysml.PortionKind;
import org.omg.sysml.lang.sysml.PortioningFeature;
import org.omg.sysml.lang.sysml.Redefinition;
import org.omg.sysml.lang.sysml.ReferenceUsage;
import org.omg.sysml.lang.sysml.Relationship;
import org.omg.sysml.lang.sysml.Subclassification;
import org.omg.sysml.lang.sysml.Subsetting;
import org.omg.sysml.lang.sysml.SuccessionAsUsage;
import org.omg.sysml.lang.sysml.SysMLPackage;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.lang.sysml.Usage;
import org.omg.sysml.lang.sysml.VisibilityKind;
import org.omg.sysml.lang.sysml.util.SysMLSwitch;

/**
 * A manually-maintained, partial, implementation of a serializer for a subset
 * of the <a href=
 * "https://github.com/Systems-Modeling/SysML-v2-Pilot-Implementation/blob/5df2783bd98db236c28c50c2d8824362a197e3da/org.omg.sysml/model/SysML.ecore">{@code SysML v2}
 * metamodel</a>, and consistent with the associated <a href=
 * "https://github.com/Systems-Modeling/SysML-v2-Pilot-Implementation/blob/5df2783bd98db236c28c50c2d8824362a197e3da/org.omg.sysml.xtext/src/org/omg/sysml/xtext/SysML.xtext">{@code SysML v2}
 * concrete textual syntax</a>. This consistency with the parser generated from
 * Xtext is important because Sirius Web needs to be capable of
 * parsing/serializing models in a consistent manner.
 * 
 * @author flatombe
 *
 */
//CHECKSTYLE:OFF
public class SysmlLimitedSerializer extends SysMLSwitch<String> {

	private static final SysMLPackage sysmlMetaPackage = SysMLPackage.eINSTANCE;

	private Set<Object> consumed = new HashSet<>();

	/**
	 * Serializes an {@link EObject} that is an instance of a metaclass from the
	 * SysML v2 metamodel.<br/>
	 * <b>Warning:Only part of the SysML v2 metamodel is currently supported.</b>
	 * Unserializable sections can be due to the following reasons:
	 * <ul>
	 * <li>This serializer explicitly does not support serializing an element: it
	 * may be due to its type, features, etc. A {@link SysmlSerializationException}
	 * is thrown, wrapping an {@link UnsupportedOperationException} containing
	 * explanations regarding the parts of the metamodel/grammar which are
	 * explicitly not supported.</li>
	 * <li>This serializer does not currently support serializing the element but it
	 * should probably do so. A TODO string is generated with some information to
	 * help implement missing parts in this serializer.</li>
	 * </ul>
	 * 
	 * @param eObject the (non-{@code null}) {@link EObject} to serialize. It is
	 *                expected to be an instance of an {@link EClass} from
	 *                {@link SysMLPackage}.
	 * @return the (non-{@code null}) {@link String} representing {@code eObject}
	 *         according to the Xtext grammar of {@code SysML v2}.
	 * @throws SysmlSerializationException in case the given model cannot be fully
	 *                                     serialized by this serializer.
	 */
	public String serialize(EObject eObject) throws SysmlSerializationException {
		try {
			this.consumed.clear();
			return this.doSwitch(eObject);
		} catch (UnsupportedOperationException unsupportedOperationException) {
			throw new SysmlSerializationException(unsupportedOperationException);
		}
	}

	// These are the entry points for the model being serialized.
	@Override
	public String caseNamespace(Namespace namespace) {
		return ruleRootNamespace(namespace);
	}

	@Override
	public String casePackage(Package aPackage) {
		return rulePackage(aPackage);
	}
	//

	private String ruleRootNamespace(Namespace namespace) {
		return namespace.getOwnedRelationship().stream().map(this::rulePackageBodyElement).filter(Objects::nonNull)
				.collect(Collectors.joining("\n"));
	}

	private String rulePackageBodyElement(Relationship relationship) {
		if (relationship.eClass() == SysMLPackage.Literals.MEMBERSHIP) {
			final String serialized = rulePackageMember((Membership) relationship);
			return serialized;
		} else {
			return throwExplicitlyNotSupported(relationship,
					"Only sub-rule 'ownedRelationship += PackageMember' of grammar rule 'PackageBodyElement' is supported.");
		}
	}

	private String rulePackageMember(Membership membership) {
		return Stream.of(ruleMemberPrefix(membership),
				membership.getOwnedRelatedElement().stream().map(ownedRelatedElement -> {
					if (ownedRelatedElement instanceof Usage) {
						final String serialized = ruleUsageElement((Usage) ownedRelatedElement);
						return serialized;
					} else {
						final String serialized = ruleDefinitionElement(ownedRelatedElement);
						return serialized;
					}
				}).collect(Collectors.joining("\n"))).collect(Collectors.joining(" "));
	}

	private String ruleUsageElement(Usage usage) {
		if (usage.eClass() == SysMLPackage.Literals.REFERENCE_USAGE) {
			final String serialized = ruleReferenceUsage((ReferenceUsage) usage);
			return serialized;
		} else {
			return throwExplicitlyNotSupported(usage,
					"Only sub-rule 'ReferenceUsage' of grammar rule 'UsageElement' is supported.");
		}
	}

	private String ruleReferenceUsage(ReferenceUsage referenceUsage) {
		return Stream.of(ruleRefPrefix(referenceUsage), "ref", ruleUsage(referenceUsage)).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String ruleRefPrefix(Usage usage) {
		return Stream.of(usage.getDirection() != null ? ruleFeatureDirection(usage.getDirection()) : null,
				usage.isAbstract() ? "abstract" : null, usage.isVariation() ? "variation" : null,
				usage.isEnd() ? "end" : null).filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleFeatureDirection(FeatureDirectionKind featureDirectionKind) {
		switch (featureDirectionKind) {
		case IN:
			return "in";
		case OUT:
			return "out";
		case INOUT:
			return "inout";
		default:
			return throwExplicitlyNotSupported(featureDirectionKind);
		}
	}

	private String ruleUsage(Usage usage) {
		return Stream.of(ruleUsageDeclaration(usage), ruleUsageCompletion(usage)).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String ruleUsageDeclaration(Feature feature) {
		final String serialized = ruleFeatureDeclaration(feature);
		return serialized;
	}

	private String ruleFeatureDeclaration(Feature feature) {
		return Stream.of(ruleIdentification(feature), ruleFeatureSpecializationPart(feature)).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String ruleIdentification(Element element) {
		if (element.getHumanId() != null) {
			return Stream.of("id", element.getHumanId(), element.getName()).filter(Objects::nonNull)
					.collect(Collectors.joining(" "));
		} else {
			if (element.getName() != null) {
				return element.getName();
			} else {
				return null;
			}
		}
	}

	private String ruleFeatureSpecializationPart(Feature feature) {
		if (feature.getOwnedRelationship().stream().filter(Membership.class::isInstance).map(Membership.class::cast)
				.anyMatch(membership -> membership.getOwnedRelatedElement().stream()
						.anyMatch(MultiplicityRange.class::isInstance))) {
			return throwExplicitlyNotSupported(feature,
					Feature.class.getSimpleName() + " instances with multiplicities are not supported.");
		} else {
			return ruleFeatureSpecialization(feature);
		}
	}

	private String ruleFeatureSpecialization(Feature feature) {
		if (feature.getOwnedRelationship().stream().anyMatch(Subsetting.class::isInstance)) {
			return throwExplicitlyNotSupported(feature, Feature.class.getSimpleName() + " instances with "
					+ Subsetting.class.getSimpleName() + " relationships are not supported.");
		} else if (feature.getOwnedRelationship().stream().anyMatch(Redefinition.class::isInstance)) {
			return throwExplicitlyNotSupported(feature, Feature.class.getSimpleName() + " instances with "
					+ Redefinition.class.getSimpleName() + " relationships are not supported.");
		} else {
			return ruleTypings(feature);
		}
	}

	private String ruleTypings(Feature feature) {
		return Stream.of(ruleTypedBy(feature),
				feature.getOwnedRelationship().stream().filter(relationship -> !this.consumed.contains(relationship))
						.filter(FeatureTyping.class::isInstance).map(FeatureTyping.class::cast)
						.map(this::ruleFeatureTyping).filter(Objects::nonNull).collect(Collectors.joining(", ")))
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleTypedBy(Feature feature) {
		final Collection<FeatureTyping> featureTypings = feature.getOwnedRelationship().stream()
				.filter(FeatureTyping.class::isInstance).map(FeatureTyping.class::cast).collect(Collectors.toList());
		if (featureTypings.size() != 1) {
			return throwExplicitlyNotSupported(feature,
					"Grammar rule 'TypedBy' expects exactly one " + FeatureTyping.class.getSimpleName()
							+ " relationship, but there were " + featureTypings.size() + ".");
		} else {
			final FeatureTyping featureTyping = featureTypings.iterator().next();
			// FIXME: the grammar is ambiguous as keyword may be either ":" or "defined by".
			return Stream.of("defined by", ruleFeatureTyping(featureTyping)).filter(Objects::nonNull)
					.collect(Collectors.joining(" "));
		}
	}

	private String ruleFeatureTyping(FeatureTyping featureTyping) {
		if (featureTyping instanceof ConjugatedPortTyping) {
			return ruleConjugatedPortTyping((ConjugatedPortTyping) featureTyping);
		} else {
			return ruleOwnedFeatureTyping(featureTyping);
		}
	}

	private String ruleConjugatedPortTyping(ConjugatedPortTyping conjugatedPortTyping) {
		return Stream.of("~", conjugatedPortTyping.getPortDefinition().getQualifiedName()).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String ruleOwnedFeatureTyping(FeatureTyping featureTyping) {
		final String serialized = featureTyping.getType().getQualifiedName();
		consume(featureTyping);
		return serialized;
	}

	private String ruleUsageCompletion(Usage usage) {
		// FIXME: for now we explicitly do not support usages with ValueOrFlowPart
		if (usage.getOwnedRelationship().stream().anyMatch(relationship -> {
			return (relationship instanceof FeatureValue
					&& relationship.getOwnedRelatedElement().stream().anyMatch(Expression.class::isInstance))
					|| (relationship instanceof Membership && relationship.getOwnedRelatedElement().stream()
							.anyMatch(FlowConnectionUsage.class::isInstance));
		})) {
			return throwExplicitlyNotSupported(usage,
					Usage.class.getSimpleName() + " instances with ValueOrFlowPart are not supported.");
		} else {
			return Stream.of(/* ruleValueOrFlowPart(usage), */ ruleUsageBody(usage)).filter(Objects::nonNull)
					.collect(Collectors.joining(" "));
		}
	}

	private String ruleValueOrFlowPart(Feature feature) {
		return Stream.of(ruleValuePart(feature),
				feature.getOwnedRelationship().stream()
						.allMatch(relationship -> relationship.eClass() == SysMLPackage.Literals.MEMBERSHIP)
								? feature.getOwnedRelationship().stream().map(Membership.class::cast)
										.map(this::ruleSourceItemFlowMember).collect(Collectors.joining(" "))
								: null)
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleSourceItemFlowMember(Membership membership) {
		return membership.getOwnedRelatedElement().stream().allMatch(FlowConnectionUsage.class::isInstance)
				? membership.getOwnedRelatedElement().stream().map(FlowConnectionUsage.class::cast)
						.map(this::ruleSourceItemFlow).collect(Collectors.joining(" "))
				: throwExplicitlyNotSupported(membership,
						"Grammar rule 'SourceItemFlowMember' expects all members of assignment 'ownedRelatedElement' to be instances of "
								+ FlowConnectionUsage.class.getSimpleName());
	}

	private String ruleSourceItemFlow(FlowConnectionUsage flowConnectionUsage) {
		return throwExplicitlyNotSupported(flowConnectionUsage);
	}

	private String ruleValuePart(Feature feature) {
		return throwExplicitlyNotSupported(feature);
	}

	private String ruleUsageBody(Usage usage) {
		return ruleDefinitionBody(usage);
	}

	private String rulePackage(Package sysmlPackage) {
		return Stream.of(rulePackageDeclaration(sysmlPackage), rulePackageBody(sysmlPackage)).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String rulePackageDeclaration(Package sysmlPackage) {
		return Stream.of("package", ruleIdentification(sysmlPackage)).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String rulePackageBody(Package sysmlPackage) {
		if (sysmlPackage.getOwnedRelationship().isEmpty()) {
			// FIXME: the grammar is ambiguous and allows either ";" or "{}" to represent a
			// Package with no relationships.
			return ";";
		} else {
			return Stream.of("{", sysmlPackage.getOwnedRelationship().stream().map(relationship -> {
				if (relationship.eClass() == SysMLPackage.Literals.MEMBERSHIP) {
					return rulePackageMember((Membership) relationship);
				} else {
					return throwExplicitlyNotSupported(relationship,
							"Only assignment 'ownedRelationship+=PackageMember' of grammar rule 'PackageBody' is supported.");
				}
			}).filter(Objects::nonNull).collect(Collectors.joining("\n")), "}").filter(Objects::nonNull)
					.collect(Collectors.joining(" "));
		}
	}

//	private String ruleMembership(Membership membership) {
//		return membership.getOwnedRelatedElement().stream().map(this::doSwitch).collect(Collectors.joining("\n"));
//	}
//
//	private String ruleImport(Import sysmlImport) {
//		return throwExplicitlyNotSupported(sysmlImport);
//	}
//
//	private String ruleFeatureTyping(FeatureTyping featureTyping) {
//		final Type type = featureTyping.getType();
//		if (type != null) {
//			final String typeQualifiedName = type.getQualifiedName();
//			if (typeQualifiedName != null) {
//				return typeQualifiedName;
//			} else {
//				return throwExplicitlyNotSupported(featureTyping,
//						"The qualified name of type " + type.toString() + " is null.");
//			}
//		} else {
//			// ConjugatedPortTyping has its own case.
//			return throwExplicitlyNotSupported(featureTyping);
//		}
//	}
//
//	private String ruleConjugatedPortTyping(ConjugatedPortTyping conjugatedPortTyping) {
//		return "~" + conjugatedPortTyping.getPortDefinition().getQualifiedName();
//	}
	////

	// Definitions
	private String ruleDefinition(Definition definition) {
		return Stream.of(ruleDefinitionDeclaration(definition), ruleDefinitionBody(definition)).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String ruleDefinitionDeclaration(Definition definition) {
		return Stream.of(ruleIdentification(definition), ruleSubClassificationPart(definition)).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String ruleSubClassificationPart(Classifier classifier) {
		// FIXME: the grammar is ambiguous and allows either ":>" or "specializes"
		// keywords.
		return classifier.getOwnedRelationship().stream().anyMatch(Subclassification.class::isInstance) ? Stream
				.of("specializes",
						// FIXME: note that we should theoretically ensure that all Subclassification
						// instances are contiguous in the list.
						classifier.getOwnedRelationship().stream().filter(Subclassification.class::isInstance)
								.map(Subclassification.class::cast).map(this::ruleOwnedSubclassification)
								.filter(Objects::nonNull).collect(Collectors.joining(", ")))
				.filter(Objects::nonNull).collect(Collectors.joining(" ")) : null;
	}

	private String ruleOwnedSubclassification(Subclassification subclassification) {
		return subclassification.getSuperclassifier().getQualifiedName();
	}

	private String ruleDefinitionBody(Type type) {
		if (type.getOwnedRelationship().isEmpty()) {
			// FIXME: the grammar is ambiguous and allows either ";" or "{}" to represent a
			// Type with no relationships.
			return ";";
		} else {
			return Stream.of("{", ruleDefinitionBodyItem(type), "}").filter(Objects::nonNull)
					.collect(Collectors.joining("\n"));
		}
	}

	private String ruleDefinitionBodyItem(Type type) {
		return type.getOwnedRelationship().stream().
		// We have to filter out already-consumed relationships.
				filter(relationship -> {
					return !this.consumed.contains(relationship);
				}).map(relationship -> {
					if (relationship.eClass() == SysMLPackage.Literals.MEMBERSHIP) {
						final Membership membership = (Membership) relationship;
						if (membership.getMemberName() != null && membership.getMemberElement() != null) {
							return ruleAliasMember(membership);
						} else {
							return ruleDefinitionMember(membership);
						}
					} else if (relationship.eClass() == SysMLPackage.Literals.FEATURE_MEMBERSHIP) {
						// There can be a confusion between sub-rules NonOccurrenceUsageMember,
						// EmptySuccessionMember, OccurrenceUsageMember because they are all typed as
						// FeatureMembership.
						final FeatureMembership featureMembership = (FeatureMembership) relationship;
						if (featureMembership.getOwnedRelatedElement().stream()
								.anyMatch(SuccessionAsUsage.class::isInstance)) {
							return ruleEmptySuccessionMember(featureMembership);
						} else if (featureMembership.getOwnedRelatedElement().stream()
								.anyMatch(PortUsage.class::isInstance)) {
							// FIXME: this is a bit too restrictive but for now we want to support only
							// PortUsage case.
							return ruleOccurrenceUsageMember(featureMembership);
						} else {
							return throwExplicitlyNotSupported(type,
									"Only sub-rule 'OccurrenceUsageMember' of grammar rule 'DefinitionBodyItem' is supported.");
						}
					} else {
						return throwExplicitlyNotSupported(relationship,
								"Only sub-rules 'AliasMember', 'DefinitionMember' and 'OccurrenceUsageMember' are supported in grammar rule 'DefinitionBodyItem'.");
					}
				}).filter(Objects::nonNull).collect(Collectors.joining("\n"));
	}

	private String ruleEmptySuccessionMember(FeatureMembership featureMembership) {
		return throwExplicitlyNotSupported(featureMembership, "Grammar rule 'EmptySuccessionMember' is not supported.");
	}

	private String ruleOccurrenceUsageMember(FeatureMembership featureMembership) {
		return Stream.of(ruleMemberPrefix(featureMembership), (featureMembership.getOwnedRelatedElement().size() == 1
				&& featureMembership.getOwnedRelatedElement().get(0) instanceof Usage)
						? ruleOccurrenceUsageElement((Usage) featureMembership.getOwnedRelatedElement().get(0))
						: throwExplicitlyNotSupported(featureMembership,
								"Grammar rule 'OccurrenceUsageMember' expects assignment 'ownedRelatedElement+=OccurrenceUsageElement' to contain a single Usage instance."))
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleOccurrenceUsageElement(Usage usage) {
		if (usage instanceof PortUsage) {
			return ruleStructureUsageElement(usage);
		} else {
			return throwExplicitlyNotSupported(usage,
					"Only sub-sub-rule 'PortUsage' of grammar rule 'OccurrenceUsageElement' is supported.");
		}
	}

	private String ruleStructureUsageElement(Usage usage) {
		if (usage instanceof PortUsage) {
			return rulePortUsage((PortUsage) usage);
		} else {
			return throwExplicitlyNotSupported(usage,
					"Only sub-rule 'PortUsage' of grammar rule 'StructureUsageElement' is supported.");
		}
	}

	private String rulePortUsage(PortUsage portUsage) {
		final String serialized = Stream.of(ruleOccurrenceUsagePrefix(portUsage), "port", ruleUsage(portUsage)).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
		consume(portUsage);
		return serialized;
	}

	private String ruleOccurrenceUsagePrefix(OccurrenceUsage occurrenceUsage) {
		final Collection<FeatureMembership> portioningFeatureMemberships = occurrenceUsage
				.getOwnedRelationship().stream().filter(FeatureMembership.class::isInstance)
				.map(FeatureMembership.class::cast).filter(featureMembership -> featureMembership
						.getOwnedRelatedElement().stream().anyMatch(PortioningFeature.class::isInstance))
				.collect(Collectors.toList());
		if (portioningFeatureMemberships.size() > 1) {
			return throwExplicitlyNotSupported(occurrenceUsage,
					"Grammar rule 'OccurrenceUsagePrefix' expects at most one of its owned relationships to match sub-rule 'PortioningFeatureMember', but there were "
							+ portioningFeatureMemberships.size() + ".");
		} else {
			final String firstPart = Stream
					.of(ruleUsagePrefix(occurrenceUsage), occurrenceUsage.isIndividual() ? "individual" : null)
					.filter(Objects::nonNull).collect(Collectors.joining(" "));
			if (portioningFeatureMemberships.isEmpty()) {
				return firstPart;
			} else {
				final FeatureMembership portioningFeatureMembership = portioningFeatureMemberships.iterator().next();
				if (occurrenceUsage.getPortionKind() == null) {
					return throwExplicitlyNotSupported(occurrenceUsage,
							"Grammar rule 'OccurrenceUsagePrefix' with a 'PortioningFeatureMember' assigned to 'ownedRelationship' expects portionKind to not be null, but it was.");
				} else {
					return Stream
							.of(firstPart, rulePortionKind(occurrenceUsage.getPortionKind()),
									rulePortioningFeatureMember(portioningFeatureMembership))
							.filter(Objects::nonNull).collect(Collectors.joining(" "));
				}
			}
		}
	}

	private String ruleUsagePrefix(Usage usage) {
		return Stream.of(ruleRefPrefix(usage), usage.isReference() ? "ref" : null).filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
	}

	private String rulePortionKind(PortionKind portionKind) {
		switch (portionKind) {
		case SNAPSHOT:
			return "snapshot";
		case TIMESLICE:
			return "timeslice";
		default:
			return throwExplicitlyNotSupported(portionKind);
		}
	}

	private String rulePortioningFeatureMember(FeatureMembership featureMembership) {
		if (featureMembership.getOwnedRelatedElement().size() == 1
				&& featureMembership.getOwnedRelatedElement().get(0) instanceof PortioningFeature) {
			return rulePortioningFeature((PortioningFeature) featureMembership.getOwnedRelatedElement().get(0));
		} else {
			return throwExplicitlyNotSupported(featureMembership,
					"Grammar rule 'PortioningFeatureMember' expects the " + FeatureMembership.class.getSimpleName()
							+ " to have exactly one related element of type "
							+ PortioningFeature.class.getSimpleName());
		}
	}

	private String rulePortioningFeature(PortioningFeature portioningFeature) {
		return throwExplicitlyNotSupported(portioningFeature,
				"Grammar rule 'PortioningFeature' has no sub-rule or assignments.");
	}

	private String ruleAliasMember(Membership membership) {
		return Stream
				.of("alias", membership.getMemberName(), "for", membership.getMemberElement().getQualifiedName(), ";")
				.collect(Collectors.joining(" "));
	}

	private String ruleDefinitionMember(Membership membership) {
		return Stream
				.of(ruleMemberPrefix(membership),
						membership.getOwnedRelatedElement().stream().map(this::ruleDefinitionElement)
								.collect(Collectors.joining("\n")))
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleMemberPrefix(Membership membership) {
		if (!membership.getOwnedRelationship().isEmpty()) {
			return throwExplicitlyNotSupported(membership,
					"Assignment 'ownedRelationship += PrefixDocumentation*' from grammar rule MemberPrefix is not supported");
		} else {
			if (membership.getVisibility() != null) {
				return ruleVisibilityIndicator(membership.getVisibility());
			} else {
				return null;
			}
		}
	}

	private String ruleDefinitionElement(Element element) {
		if (element.eClass() == SysMLPackage.Literals.PACKAGE) {
			return rulePackage((Package) element);
		} else if (element.eClass() == SysMLPackage.Literals.PART_DEFINITION) {
			return rulePartDefinition((PartDefinition) element);
		} else if (element.eClass() == SysMLPackage.Literals.PORT_DEFINITION) {
			return rulePortDefinition((PortDefinition) element);
		} else if (element.eClass() == SysMLPackage.Literals.ACTION_DEFINITION) {
			return ruleActionDefinition((ActionDefinition) element);
		} else {
			return throwExplicitlyNotSupported(element,
					"Only sub-rules 'Package', 'PartDefinition, 'PortDefinition', 'ActionDefinition' are supported in grammar rule 'DefinitionElement'.");
		}
	}

	private String rulePartDefinition(PartDefinition partDefinition) {
		return Stream.of(ruleOccurrenceDefinitionPrefix(partDefinition), "part def", ruleDefinition(partDefinition))
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleOccurrenceDefinitionPrefix(OccurrenceDefinition occurrenceDefinition) {
		// There should be 0 or 1 Membership with a single LifeClass instance.
		final Collection<Membership> membershipsWithLifeClassElement = occurrenceDefinition.getOwnedRelationship()
				.stream().filter(Membership.class::isInstance).map(Membership.class::cast)
				.filter(relationship -> relationship.getOwnedRelatedElement().size() == 1
						&& relationship.getOwnedRelatedElement().get(0) instanceof LifeClass)
				.collect(Collectors.toList());
		if (membershipsWithLifeClassElement.size() > 1) {
			return throwExplicitlyNotSupported(occurrenceDefinition,
					"Grammar rule 'OccurrenceDefinitionPrefix' expects only one owned relationship to be a Membership with a LifeClass element. There were "
							+ membershipsWithLifeClassElement.size());
		} else {
			if (membershipsWithLifeClassElement.isEmpty()) {
				return ruleDefinitionPrefix(occurrenceDefinition);
			} else {
				final Membership membershipWithLifeClassElement = membershipsWithLifeClassElement.iterator().next();
				return Stream
						.of(ruleDefinitionPrefix(occurrenceDefinition),
								occurrenceDefinition.isIndividual() ? "individual" : null,
								ruleLifeClassMembership(membershipWithLifeClassElement))
						.filter(Objects::nonNull).collect(Collectors.joining(" "));
			}
		}
	}

	private String ruleLifeClassMembership(Membership membership) {
		return Stream.of(membership.getOwnedRelatedElement().stream().filter(LifeClass.class::isInstance)
				.map(LifeClass.class::cast).map(this::ruleLifeClass).filter(Objects::nonNull)
				.collect(Collectors.joining(" "))).filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleLifeClass(LifeClass lifeClass) {
		return throwExplicitlyNotSupported(lifeClass, "Grammar rule 'LifeClass' has no assignments or sub-rules.");
	}

	private String ruleActionDefinition(ActionDefinition actionDefinition) {
		return Stream
				.of(ruleOccurrenceDefinitionPrefix(actionDefinition), "action def",
						ruleActionDeclaration(actionDefinition), ruleActionBody(actionDefinition))
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleActionBody(Type type) {
		// FIXME: the grammar is ambiguous here because an empty ActionBody could be
		// represented as ";" or "{}".
		if (type.getOwnedRelationship().isEmpty()) {
			return ";";
		} else {
			return Stream.of("{", ruleActionBodyItem(type), "}").filter(Objects::nonNull)
					.collect(Collectors.joining("\n"));
		}
	}

	private String ruleActionBodyItem(Type type) {
		return type.getOwnedRelationship().stream().map(relationship -> {
			if (relationship.eClass() == SysMLPackage.Literals.MEMBERSHIP) {
				final Membership membership = (Membership) relationship;
				if (membership.getMemberName() != null && membership.getMemberElement() != null) {
					return ruleAliasMember(membership);
				} else if (membership.getMemberElement() != null) {
					return ruleInitialNodeMember(membership);
				} else {
					return ruleDefinitionMember(membership);
				}
			} else {
				return throwExplicitlyNotSupported(type,
						"Only sub-rules 'AliasMember', 'DefinitionMember' and 'InitialNodeMember' of grammar rule 'ActionBodyItem' are supported.");
			}
		}).filter(Objects::nonNull).collect(Collectors.joining("\n"));
	}

	private String ruleInitialNodeMember(Membership membership) {
		return Stream.of(ruleMemberPrefix(membership), "first", membership.getMemberElement().getQualifiedName())
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleActionDeclaration(ActionDefinition actionDefinition) {
		return Stream.of(ruleDefinitionDeclaration(actionDefinition), ruleParameterList(actionDefinition))
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleParameterList(Type type) {
		return Stream
				.of("(", type.getOwnedRelationship().stream().filter(ParameterMembership.class::isInstance)
						.map(ParameterMembership.class::cast).map(this::ruleParameterMember).filter(Objects::nonNull)
						.collect(Collectors.joining(", ")), ")")
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleParameterMember(ParameterMembership parameterMembership) {
		if (parameterMembership.getOwnedRelatedElement().size() != 1) {
			return throwExplicitlyNotSupported(parameterMembership,
					"Grammar rule 'ParameterMember' expects the ParameterMembership to have exactly one ownedRelatedElement, but it had "
							+ parameterMembership.getOwnedRelatedElement().size());
		} else {
			final Element element = parameterMembership.getOwnedRelatedElement().get(0);
			if (element instanceof Usage) {
				return ruleParameter((Usage) element);
			} else {
				return throwExplicitlyNotSupported(parameterMembership,
						"Assignment 'ownedRelatedElement+=Parameter' in grammar rule 'ParameterMember' expects the element to be an instance of "
								+ Usage.class.getSimpleName() + " but it was : " + element.toString());
			}
		}
	}

	private String ruleParameter(Usage usage) {
		if (usage instanceof ReferenceUsage) {
			final ReferenceUsage referenceUsage = (ReferenceUsage) usage;
			return Stream
					.of(referenceUsage.getDirection() != null ? ruleFeatureDirection(referenceUsage.getDirection())
							: null,
							// FIXME: the grammar seems ambiguous here as the keyword is effectively
							// optional.
							"ref", ruleParameterDeclaration(usage))
					.filter(Objects::nonNull).collect(Collectors.joining(" "));
		} else {
			return throwExplicitlyNotSupported(usage, "Only " + ReferenceUsage.class.getSimpleName()
					+ " instances are supported for grammar rule 'Parameter'.");
		}
	}

	private String ruleParameterDeclaration(Usage usage) {
		// FIXME: for now we explicitly do not support usages with
		// ParameterSpecializationPart.
		if (usage.getOwnedRelationship().stream().filter(Membership.class::isInstance).map(Membership.class::cast)
				.anyMatch(membership -> membership.getOwnedRelatedElement().stream()
						.anyMatch(MultiplicityRange.class::isInstance))) {
			return throwExplicitlyNotSupported(usage,
					"Usages with multiplicities / typing specializations are not supported.");
		}
		return Stream.of(ruleIdentification(usage)/* , ruleParameterSpecializationPart(usage) */)
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleParameterSpecializationPart(Feature feature) {
		return throwExplicitlyNotSupported(feature, "Grammar rule 'ParameterSpecializationPart' is not supported.");
	}

	private String rulePortDefinition(PortDefinition portDefinition) {
		return Stream
				.of(ruleDefinitionPrefix(portDefinition), "port def", ruleDefinition(portDefinition),
						portDefinition.getOwnedRelationship().stream()
								.filter(relationship -> !this.consumed.contains(relationship))
								.filter(Membership.class::isInstance)
								.map(Membership.class::cast).map(this::ruleConjugatedPortDefinitionMember)
								.filter(Objects::nonNull).collect(Collectors.joining("\n")))
				.filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	private String ruleConjugatedPortDefinitionMember(Membership membership) {
		return membership.getOwnedRelatedElement().stream().filter(ConjugatedPortDefinition.class::isInstance)
				.map(ConjugatedPortDefinition.class::cast).map(this::ruleConjugatedPortDefinition)
				.collect(Collectors.joining("\n"));
	}

	private String ruleConjugatedPortDefinition(ConjugatedPortDefinition conjugatedPortDefinition) {
		return throwExplicitlyNotSupported(conjugatedPortDefinition,
				"Rule 'PortConjugation' in the grammar seems like it is missing some pieces.");
	}

	private String ruleDefinitionPrefix(Definition definition) {
		String definitionPrefix = "";
		if (definition.isAbstract()) {
			definitionPrefix += "abstract";
		}
		if (definition.isVariation()) {
			definitionPrefix += "variation";
		}
		// May be blank.
		return definitionPrefix;
	}

	private String ruleVisibilityIndicator(VisibilityKind visibilityKind) {
		switch (visibilityKind) {
		case PUBLIC:
			return "public";
		case PROTECTED:
			return "protected";
		case PRIVATE:
			return "private";
		default:
			return throwExplicitlyNotSupported(visibilityKind);
		}
	}
	////

	// Utilities
	@Override
	public String defaultCase(EObject eObject) {
		return "TODO: implement serialization for: " + eObject.toString() + "\n" + prettyPrintAstOf(eObject);
	}

	private <T> T consume(T object) {
		if (this.consumed.contains(object)) {
			throw new IllegalStateException(
					"Cannot consume " + object.toString() + " because it was already consumed.");
		} else {
			this.consumed.add(object);
			return object;
		}
	}

	/**
	 * Use this to indicate we are not able to serialize (part of) a model element.
	 * Usually, this is either because the object is malformed or because this
	 * serializer is not meant to support that part of the SysML metamodel.
	 * 
	 * @param object  the (non-{@code null}) {@link Object} that cannot be
	 *                serialized.
	 * @param reasons the (maybe-{@code null}) reasons for why it could not be
	 *                serialized.
	 * @return {@code null} theoretically, but actually always throws a
	 *         {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException
	 */
	private static String throwExplicitlyNotSupported(Object object, String... reasons)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot serialize the following element: " + object.toString() + "\n<<<"
				+ prettyPrintAstOf(object) + "\n>>>"
				+ ((reasons != null && reasons.length != 0)
						? "\nFor the following reasons:" + Stream.of(reasons).collect(Collectors.joining("\n-", "\n-",
								"\nDo remember that this serializer only supports a specific subset of the SysML metamodel and grammar."))
						: ""));
	}

	private static String prettyPrintAstOf(Object object) {
		return EmfFormatter.objToStr(object, feature -> feature.isDerived());
	}
	////

}
//CHECKSTYLE:N
