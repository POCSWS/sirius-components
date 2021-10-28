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
package org.eclipse.sirius.web.dsl.sysmlv2.xtext.serializer.tests.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.assertj.core.api.Assertions;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.sirius.web.dsl.sysmlv2.xtext.SysMLSiriusWebIdeSetup;
import org.eclipse.sirius.web.dsl.sysmlv2.xtext.serializer.SysMLAdHocSerializer;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.SyntaxErrorMessage;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.util.EmfFormatter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.omg.sysml.lang.sysml.SysMLPackage;

import com.google.inject.Injector;

/**
 * First simple JUnit tests to ensure {@link SysMLAdHocSerializer} is consistent
 * with the parser.
 * 
 * @author flatombe
 *
 */
public class SysMLAdHocSerializerTests {

	@SuppressWarnings("unused")
	private static final SysMLPackage SYSML_PACKAGE = SysMLPackage.eINSTANCE;
	private static final Injector SYSML_INJECTOR = SysMLSiriusWebIdeSetup.doSetup();
	private final IParser sysmlParser = SYSML_INJECTOR.getInstance(IParser.class);
	private final ISerializer sysmlSerializer = SYSML_INJECTOR.getInstance(ISerializer.class);

	@ParameterizedTest
	@ValueSource(strings = { "models/1-EmptyNamespace.xmi", "models/2-NamespaceWithOnePackage.xmi",
			"models/3-NamespaceWithPackageAndSubPackageAndPartDefinitionAndActionDefinition.xmi",
	/*
	 * TODO: adapt these models / the serialization so they pass the test.
	 * "models/4-PortDefinitionWithPortUsage.xmi", "models/Step4.xmi",
	 * "models/Target.xmi"
	 */ })
	void fromXmiModelSerializeThenParse(String relativePathToXmiModelFile) {
		// Step 1: Load the model
		Resource.Factory.Registry factoryRegistry = Resource.Factory.Registry.INSTANCE;
		factoryRegistry.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		final Path pathToTargetFile = Paths.get(relativePathToXmiModelFile);
		final URI uri = URI.createFileURI(pathToTargetFile.toFile().toString());
		final ResourceSet resourceSet = new ResourceSetImpl();
		final Resource resource = resourceSet.getResource(uri, true);
		final EObject loadedModel = resource.getContents().get(0);

		// Step 2: Serialize it into text.
		final String serializationResult = sysmlSerializer.serialize(loadedModel);
		Assertions.assertThat(serializationResult).isNotNull(); // note that it could probably be blank.

		// Step 3: Parse it back into a model.
		final IParseResult parsingResult = sysmlParser.parse(new StringReader(serializationResult));
		final Collection<INode> syntaxErrors = StreamSupport
				.stream(parsingResult.getSyntaxErrors().spliterator(), false).collect(Collectors.toList());
		final String failMessageBase = "XMI model " + relativePathToXmiModelFile + " was serialized as:\n<<<\n"
				+ serializationResult + "\n>>>";
		Assertions.assertThat(syntaxErrors)
				.withFailMessage(
						() -> failMessageBase + "\nBut it could not be parsed back into a model because it contained "
								+ syntaxErrors.size() + " syntax errors:\n"
								+ syntaxErrors.stream().map(INode::getSyntaxErrorMessage)
										.map(SyntaxErrorMessage::getMessage).collect(Collectors.joining("\n")))
				.isEmpty();
		final EObject parsedModel = parsingResult.getRootASTElement();
		Assertions.assertThat(parsedModel).isNotNull();

		// Step 4: Compare the original model with the parsed model.
		// For now we just check that there are the same number of elements in both
		// models.
		final int originalModelNumberOfContents = getNumberOfContents(loadedModel);
		final int parsedModelNumberOfContents = getNumberOfContents(parsedModel);
		Assertions.assertThat(parsedModelNumberOfContents)
				.withFailMessage(() -> failMessageBase + "\nAnd then parsed back into a model with "
						+ parsedModelNumberOfContents + " contained elements, but the original model had "
						+ originalModelNumberOfContents + ".\nParsed containment tree:\n<<<\n"
						+ prettyPrintContainmentTree(parsedModel) + "\n>>>\nOriginal containment tree:\n<<<\n"
						+ prettyPrintContainmentTree(loadedModel) + "\n>>>.")
				.isEqualTo(originalModelNumberOfContents);
	}

	// TODO: Rework below unit tests later, when the serializer is more advanced.
//	@ParameterizedTest
//	@ValueSource(strings = { "models/PartTest.sysml" })
	void fromSourceFileParseThenSerialize(String relativePathToSourceFile) throws FileNotFoundException {
		final File sourceFile = Paths.get(relativePathToSourceFile).toFile();
		final IParseResult parsingResult = sysmlParser.parse(new FileReader(sourceFile));
		Assertions.assertThat(parsingResult.getSyntaxErrors()).isEmpty();

		final EObject parsedModel = parsingResult.getRootASTElement();
		Assertions.assertThat(parsedModel).isNotNull();

		System.out.println(prettyPrintContainmentTree(parsedModel));

//		final String serializationResult = sysmlSerializer.serialize(model);
//		Assertions
//				.assertThat(serializationResult).withFailMessage("Expected:\n<<<\n" + source + "\n>>>\nBut was:\n<<<\n"
//						+ serializationResult + "\n>>>\nFor AST:\n<<<\n" + prettyPrintContainmentTree(model) + "\n>>>")
//				.isEqualTo(source);
	}

	private static String prettyPrintContainmentTree(EObject eObject) {
		return EmfFormatter.objToStr(eObject, feature -> feature.isDerived()
				|| (feature instanceof EReference && !((EReference) feature).isContainment()));
	}

	private static int getNumberOfContents(EObject eObject) {
		Iterator<EObject> iterator = eObject.eAllContents();
		int size = 0;
		while (iterator.hasNext()) {
			iterator.next();
			size++;
		}
		return size;
	}

}
