package org.delia.bddnew;

/**
 * TODO FOR DELIA ANTLR
 *
 *  DONE -all bdd tests postgres
 *  DONE -all bdd tests mem
 *  DONE -fix transitory queries Customer[wid < 100].addr in both
 *  DONE  -sql MM
 *  DONE  -sql 1:1 and N:1
 *  DONE  -mem MM
 *  DONE  -mem 1:1 and N:1
 *   -orderBy('wid') -- this may cause implicit join
 *    -get working on sql and mem for M:M and N:1
 *  DONE -all unit tests postgres
 *  DONE -all unit tests mem
 *  DONE -rename zdb and cleanup code
 *  DONE -remove Shape.LONG
 *  DONE-sprig support
 *
 *  next round
 *  DONE -DValueIterator. so can import from csv w/o having to generate delia src and compile it
 *  DONE -fetch
 *  DONE -Tok instead of Exp for rules,where,and fieldAndFuncs
 *  DONE -date fns
 *  DONE -fix lld/ and sql/ tests and delete MyWhereSqlVisitor
 *  DONE -tests 1700,1800
 *  DONE -schema support (eg parcel., manifest.)
 *  DONE -tests 2200
 *  DONE -transactions
 *
 *  DONE error handling
 *  -how return errors from MEM
 *  -and from sql
 *  DONE -tests for syntax errors
 *  DONE -give line#
 *  DONE -blob
 *  DONE July2023 -bulk import
 *
 *  query with unknown field should be an error: rawDataSetId==%d and xxrowIndex=%d
 *
 *  use VARCHAR and TEXT when large
 *   DValueConverterService: 			//TODO we already have normalized DValues. Why build again?
 *   schema handling
 *   a) donothing. user must create valid delia that matches schema
 *   a2) solve not creating the DAT table??
 *   b) migration. our old code
 *   c) schema-to-delia. (from seede)
 *
 *  -delete with join. delete Customer[date < '2019']
 *  -upsert can have filter that is unique key (not just pk) upsert Customer[guid=44]
 *  -dto and yield(). struct types that aren't entities
 *  -H2
 *  -TODOs in code (convert to FUTURE if not fix immediately)
 *  -BDD: TODOs
 *  -BDD: SKIP
 *  -add edge cases
 *  -perf improvement
 *   -use PreparedStatements so db caches them (and doesn't need to reparse)
 *     -can we just do this always? or only sometimes?
 *  -improve SQL
 *   -CTEs
 *  -move code over to main delia repo
 *  -docs
 *  -seede
 *  -csv import stuff (no. leave outside of delia)
 *  -NO! support insert Customer { id:4 addr:55} //parent inserting a value for relation
 *
 */
public class ToDoPlan {
}
