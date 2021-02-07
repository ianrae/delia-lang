package org.delia.codegen.newcodegen;

import java.util.ArrayList;
import java.util.List;

public class CGBuilder2 {
	CodegenBuilder builder;
	List<CodeGenerator> generatorsL = new ArrayList<>();
	
	public CGBuilder2(CodegenBuilder builder) {
		this.builder = builder;
	}
	
	public CGBuilder3 addStandardGenerators() {
		generatorsL.add(new NewGetterInterfaceCodeGen());
		//TODO more
		return new CGBuilder3(this);
	}
	public CGBuilder3 addGenerator(CodeGenerator generator) {
		generatorsL.add(generator);
		return new CGBuilder3(this);
	}
	public CGBuilder3 addGenerators(List<CodeGenerator> generators) {
		generatorsL.addAll(generators);
		return new CGBuilder3(this);
	}
}