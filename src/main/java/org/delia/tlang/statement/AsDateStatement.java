package org.delia.tlang.statement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.StringExp;
import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangStatement;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class AsDateStatement implements TLangStatement {
	private String format;
	private SimpleDateFormat sdf;
	
	public AsDateStatement(Exp arg1) {
		StringExp nexp = (StringExp) arg1;
		this.format = nexp.val;
		this.sdf = new SimpleDateFormat(format);
	}
	@Override
	public String getName() {
		return "asDate";
	}
	@Override
	public boolean evalCondition(DValue dval) {
		return true;
	}
	@Override
	public void execute(DValue value, TLangResult result, TLangContext ctx) {
		String s = value.asString();
		Date dt = null;
		try {
			dt = sdf.parse(s);
		} catch (ParseException e) {
			DeliaExceptionHelper.throwError("asdate-failed", "ffff");
		}
		result.val = ctx.builder.buildDate(dt);
	}
}