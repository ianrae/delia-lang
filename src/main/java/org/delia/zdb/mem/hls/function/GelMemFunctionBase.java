package org.delia.zdb.mem.hls.function;

import org.delia.db.hls.GElement;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.mem.hls.MemFunctionBase;

public abstract class GelMemFunctionBase extends MemFunctionBase {

		protected GElement op;

		public GelMemFunctionBase(DTypeRegistry registry, GElement op) {
			super(registry);
			this.op = op;
		}
		
	}