package org.delia.codegen.fluent;

import java.util.ArrayList;
import java.util.List;

import org.delia.codegen.CodeGenerator;
import org.delia.codegen.EntityCodeGen;
import org.delia.codegen.GetterInterfaceCodeGen;
import org.delia.codegen.ImmutCodeGen;
import org.delia.codegen.SetterInterfaceCodeGen;

public class CGBuilder2 {
	CodeGenBuilder builder;
	List<CodeGenerator> generatorsL = new ArrayList<>();
	
	public CGBuilder2(CodeGenBuilder builder) {
		this.builder = builder;
	}
	
	public CGBuilder3 addStandardGenerators() {
		generatorsL.add(new GetterInterfaceCodeGen());
		generatorsL.add(new ImmutCodeGen());
		generatorsL.add(new SetterInterfaceCodeGen());
		generatorsL.add(new EntityCodeGen());
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