package org.delia.zdb.mem.hld.function;

import org.delia.db.hls.GElement;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.mem.hld.MemFunctionBase;

public abstract class GelMemFunctionBase extends MemFunctionBase {

		protected GElement op;

		public GelMemFunctionBase(DTypeRegistry registry, GElement op) {
			super(registry);
			this.op = op;
		}
		
	}