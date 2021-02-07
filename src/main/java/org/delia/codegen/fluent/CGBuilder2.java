package org.delia.codegen.fluent;

import java.util.ArrayList;
import java.util.List;

import org.delia.codegen.CodeGenerator;
import org.delia.codegen.NewEntityCodeGen;
import org.delia.codegen.NewGetterInterfaceCodeGen;
import org.delia.codegen.NewImmutCodeGen;
import org.delia.codegen.NewSetterInterfaceCodeGen;

public class CGBuilder2 {
	CodeGenBuilder builder;
	List<CodeGenerator> generatorsL = new ArrayList<>();
	
	public CGBuilder2(CodeGenBuilder builder) {
		this.builder = builder;
	}
	
	public CGBuilder3 addStandardGenerators() {
		generatorsL.add(new NewGetterInterfaceCodeGen());
		generatorsL.add(new NewImmutCodeGen());
		generatorsL.add(new NewSetterInterfaceCodeGen());
		generatorsL.add(new NewEntityCodeGen());
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