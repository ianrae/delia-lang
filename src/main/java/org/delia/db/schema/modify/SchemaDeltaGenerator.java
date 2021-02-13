package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.delia.core.FactoryService;
import org.delia.core.RegAwareServiceBase;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;

/**
	 * Compares two schemas and produces a delta object, listing all the
	 * changes to fields, types, constraints, and indexes.
	 * 
	 * @author ian
	 *
	 */
	public class SchemaDeltaGenerator extends RegAwareServiceBase {

		private SchemaDeltaGeneratorOther otherGenerator;

		public SchemaDeltaGenerator(DTypeRegistry registry, FactoryService factorySvc) {
			super(registry, factorySvc);
			this.otherGenerator = new SchemaDeltaGeneratorOther(registry, factorySvc);
		}
		
		public SchemaDelta generate(SchemaDefinition schema1, SchemaDefinition schema2) {
			SchemaDelta delta = new SchemaDelta();
			
			List<SxTypeInfo> list2 = new ArrayList<>(schema2.types);
			for(SxTypeInfo tt: schema1.types) {
				SxTypeInfo sc2 = findIn(tt, schema2);
				if (sc2 != null) {
					SxTypeDelta td = buildTypeDelta(tt);
					int n = diffFields(tt, sc2, delta, td);
					if (n > 0) {
						list2.remove(sc2);
						delta.typesU.add(td);
					}
					list2.remove(sc2);
				} else {
					SxTypeDelta td = buildTypeDelta(tt);
					delta.typesD.add(td); //in list1 but not in list2
				}
			}

			for(SxTypeInfo tt: list2) {
				SxTypeDelta td = buildTypeDelta(tt);
				delta.typesI.add(td);
			}
			
			otherGenerator.handleOthers(schema1, schema2, delta);

			return delta;
		}
		
		private SxTypeDelta buildTypeDelta(SxTypeInfo tt) {
			SxTypeDelta typeDelta = new SxTypeDelta(tt.nm);
			typeDelta.info = tt;
			return typeDelta;
		}

		private SxTypeInfo findIn(SxTypeInfo tt, SchemaDefinition schema2) {
			Optional<SxTypeInfo> opt = schema2.types.stream().filter(x -> x.nm.equals(tt.nm)).findAny();
			return opt.orElse(null);
		}
		
		private int diffFields(SxTypeInfo tt, SxTypeInfo sc2, SchemaDelta delta, SxTypeDelta td) {
//			private void diffFields(SchemaType st1, SchemaType st2, List<SchemaType> diffList) {
			
			List<SxFieldInfo> flist1 = tt.flds;
			List<SxFieldInfo> flist2 = sc2.flds;
			List<SxFieldInfo> list2 = new ArrayList<>(flist2);
			
			int nStart = td.fldsD.size() + td.fldsI.size() + td.fldsU.size();
			
			//TODO support rename
			//add fieldinfo.fieldIndex - index of field within dtype
			//if I and D have same fieldIndex and same type and options then do a rename

			//I/U/D/R/A/AT/ASZ/ASN
			for(SxFieldInfo finfo: flist1) {
				SxFieldInfo f2 = findFieldIn(finfo, flist2);
				if (f2 != null) {
					if (!finfo.t.equals(f2.t)) {
						SxFieldDelta fd = new SxFieldDelta(finfo.f, tt.nm);
						fd.tDelta = f2.t;
						fd.info = finfo;
						td.fldsU.add(fd);
					} else {
						Integer sizeofDelta = calcSizeofString(finfo, f2);
						if (sizeofDelta != null) {
							SxFieldDelta fd = new SxFieldDelta(finfo.f, tt.nm);
							fd.szDelta = sizeofDelta;
							fd.info = finfo;
							td.fldsU.add(fd);
						}
					}
					
					String deltaFlags = calcDeltaFlags(finfo, f2);
					if (!StringUtil.isNullOrEmpty(deltaFlags)) {
						SxFieldDelta fd = new SxFieldDelta(finfo.f, tt.nm);
						fd.flgsDelta = deltaFlags;
						fd.info = finfo;
						td.fldsU.add(fd);
					}
					
					list2.remove(f2);
				} else {
					SxFieldDelta fd = new SxFieldDelta(finfo.f, tt.nm);
					fd.info = finfo;
					td.fldsD.add(fd);
				}
			}

			for(SxFieldInfo f2: list2) {
				//in list2 but not in list1
				SxFieldDelta fd = new SxFieldDelta(f2.f, tt.nm);
				fd.info = f2;
				td.fldsI.add(fd);
			}
			
			int nEnd = td.fldsD.size() + td.fldsI.size() + td.fldsU.size();
			return nEnd - nStart;
		}

		private Integer calcSizeofString(SxFieldInfo finfo, SxFieldInfo f2) {
			if (finfo.sz == f2.sz) {
				return null;
			}
			
			return f2.sz; // new size value
		}

		/** generate a string of modifier changes.
		 *  +O means adding optional
		 *  -O means removing optional
		 *  -also +U,-U,+P,-P,+S,-S
		 */
		private String calcDeltaFlags(SxFieldInfo finfo, SxFieldInfo f2) {
			StringJoiner joiner = new StringJoiner(",");
			for(int i = 0; i < finfo.flgs.length(); i++) {
				char ch = finfo.flgs.charAt(i);
				if (f2.flgs.indexOf(ch) < 0) { //in f1 and not in f2?
					joiner.add(String.format("-%c", ch));
				}
			}		
			
			for(int i = 0; i < f2.flgs.length(); i++) {
				char ch = f2.flgs.charAt(i);
				if (finfo.flgs.indexOf(ch) < 0) { //not in f1 and in f2?
					joiner.add(String.format("+%c", ch));
				}
			}		
			
			return joiner.toString();
		}

		private SxFieldInfo findFieldIn(SxFieldInfo target, List<SxFieldInfo> flist2) {
			for(SxFieldInfo f2: flist2) {
				if (target.f.equals(f2.f)) {
					return f2;
				}
			}
			return null;
		}
	}