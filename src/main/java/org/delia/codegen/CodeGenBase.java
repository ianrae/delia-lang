package org.delia.codegen;

import org.delia.db.sql.StrCreator;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public abstract class CodeGenBase implements CodeGenerator {
	
	protected CodeGeneratorOptions options;
	protected String packageName;
	protected boolean structTypesOnly;
	private DTypeRegistry registry;
	private CodeGenHelper helper;

	public CodeGenBase(boolean structTypesOnly) {
		this.structTypesOnly = structTypesOnly;
		this.helper = null;//new CodeGenHelper(DTypeRegistry registry, String packageName) {

	}
	
	protected void addDoNotModifyComment(StrCreator sc) {
		sc.o("//DO NOT MODIFY THIS FILE. IT WAS CREATED BY DELIA CODE GENERATOR.");
		sc.nl();
	}
	
	protected CodeGenHelper helper() {
		if (this.helper == null) {
			helper = new CodeGenHelper(registry, packageName);
		}
		return helper;
	}

	@Override
	public void setOptions(CodeGeneratorOptions options) {
		this.options = options;
	}

	@Override
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public boolean canProcess(DType dtype) {
		if (structTypesOnly) {
			return dtype.isStructShape();
		}
		return false;
	}

	@Override
	public void setRegistry(DTypeRegistry registry) {
		this.registry = registry;
	}
}