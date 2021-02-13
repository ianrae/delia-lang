package org.delia.codegen.fluent;

public class CGBuilder3 {
	CGBuilder2 builder2;
	String packageName;

	public CGBuilder3(CGBuilder2 builder2) {
		this.builder2 = builder2;
	}
	
	public CGBuilder4 toPackage(String packageName) {
		this.packageName = packageName;
		return new CGBuilder4(this);
	}
	
}