package org.delia.codegen.newcodegen;

import java.util.List;
import java.util.stream.Collectors;

public class CGBuilder4 {
	CGBuilder3 builder3;

	public CGBuilder4(CGBuilder3 builder3) {
		this.builder3 = builder3;
	}
	
	public CodeGeneratorService build() {
		List<String> typeNames = buildTypeNamesList();
		CodegenBuilder builder = builder3.builder2.builder;
		return new CodeGeneratorService(builder.registry, builder.factorySvc, typeNames, builder3.builder2.generatorsL, builder3.packageName);
	}

	private List<String> buildTypeNamesList() {
		CodegenBuilder builder = builder3.builder2.builder;
		if (builder.allTypes) {
			return builder.registry.getOrderedList().stream().map(x -> x.getName()).collect(Collectors.toList());
		}
		return builder.theseTypes;
	}
}