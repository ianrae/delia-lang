package org.delia.tlang.statement;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.StringExp;
import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangStatement;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class AsDateStatement implements TLangStatement {
	private String format;
	private DateTimeFormatter sdf;
	
	public AsDateStatement(Exp arg1) {
		StringExp nexp = (StringExp) arg1;
		this.format = nexp.val;
		this.sdf = DateTimeFormatter.ofPattern(format);
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
		ZonedDateTime dt = null;
		try {
			dt = ZonedDateTime.parse(s, sdf);
		} catch (DateTimeParseException e) {
			DeliaExceptionHelper.throwError("asdate-failed", "ffff");
		}
		result.val = ctx.builder.buildDate(dt);
	}
}