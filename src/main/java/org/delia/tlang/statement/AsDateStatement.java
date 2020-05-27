package org.delia.tlang.statement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.StringExp;
import org.delia.core.TimeZoneService;
import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangStatement;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class AsDateStatement implements TLangStatement {
	private String format;
	private DateTimeFormatter sdf;
	private boolean isDateOnly;
	private TimeZoneService tzSvc;
	
	public AsDateStatement(Exp arg1, TimeZoneService tzSvc) {
		StringExp nexp = (StringExp) arg1;
		this.format = nexp.val;
		this.tzSvc = tzSvc;
		this.sdf = DateTimeFormatter.ofPattern(format);
		this.isDateOnly = isDateOnly(format);
	}
	private boolean isDateOnly(String fmt) {
		//yyyy-MM-dd'T'HH:mm:ss.SSSZ"
		if (fmt.contains("HH") || fmt.contains("mm") || fmt.contains("ss")) {
			return false;
		}
		return true;
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
			if (isDateOnly) {
				LocalDate ldt = LocalDate.parse(s, sdf);
				dt = ZonedDateTime.of(ldt.atStartOfDay(), tzSvc.getDefaultTimeZone());
			} else {
				dt = ZonedDateTime.parse(s, sdf);
				//TODO. probably need an hasTZ and use LocalDateTime
			}
		} catch (DateTimeParseException e) {
			DeliaExceptionHelper.throwError("asdate-failed", e.getMessage());
		}
		result.val = ctx.builder.buildDate(dt);
	}
}