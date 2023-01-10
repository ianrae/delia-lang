// Generated from C:/Users/16136/Documents/GitHub/delia/delia-antlr/src/test/java/org/delia/antlr\delia.g4 by ANTLR 4.9.1
package org.delia.compiler.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class deliaParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, BEGF=36, ENDF=37, BEGPAREN=38, 
		ENDPAREN=39, TRUE=40, FALSE=41, NULL=42, SEP=43, DOT=44, NOT=45, SYMBOL=46, 
		DOLLAR=47, NUM=48, COMMENT=49, LINE_COMMENT=50, FloatingPointLiteral=51, 
		StringLiteral=52, StringLiteral2=53, HexDigit=54, WS=55;
	public static final int
		RULE_deliaStatement = 0, RULE_statement = 1, RULE_configureStatement = 2, 
		RULE_schemaStatement = 3, RULE_logStatement = 4, RULE_typeStatement = 5, 
		RULE_scalarTypeStatement = 6, RULE_structTypeStatement = 7, RULE_structFields = 8, 
		RULE_structField = 9, RULE_relationName = 10, RULE_fieldModifiers = 11, 
		RULE_fieldModifier = 12, RULE_drules = 13, RULE_drule = 14, RULE_insertStatement = 15, 
		RULE_valuePairs = 16, RULE_valuePairArg = 17, RULE_crudAction = 18, RULE_elemList = 19, 
		RULE_deleteStatement = 20, RULE_updateStatement = 21, RULE_upsertFlag = 22, 
		RULE_letStatement = 23, RULE_letVar = 24, RULE_fnChain = 25, RULE_fnChainArg = 26, 
		RULE_filter = 27, RULE_cexpr = 28, RULE_elem = 29, RULE_fn = 30, RULE_fnargs = 31, 
		RULE_name = 32, RULE_scalar = 33;
	private static String[] makeRuleNames() {
		return new String[] {
			"deliaStatement", "statement", "configureStatement", "schemaStatement", 
			"logStatement", "typeStatement", "scalarTypeStatement", "structTypeStatement", 
			"structFields", "structField", "relationName", "fieldModifiers", "fieldModifier", 
			"drules", "drule", "insertStatement", "valuePairs", "valuePairArg", "crudAction", 
			"elemList", "deleteStatement", "updateStatement", "upsertFlag", "letStatement", 
			"letVar", "fnChain", "fnChainArg", "filter", "cexpr", "elem", "fn", "fnargs", 
			"name", "scalar"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'configure'", "'='", "'schema'", "'off'", "'log'", "'type'", "'end'", 
			"'{'", "'}'", "'relation'", "'optional'", "'unique'", "'primaryKey'", 
			"'serial'", "'one'", "'many'", "'parent'", "'insert'", "':'", "'update'", 
			"'delete'", "'upsert'", "'-noUpdate'", "'let'", "'in'", "'=='", "'!='", 
			"'<'", "'<='", "'>'", "'>='", "'like'", "'and'", "'or'", "'-'", "'['", 
			"']'", "'('", "')'", "'true'", "'false'", "'null'", "','", "'.'", "'!'", 
			null, "'$'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"BEGF", "ENDF", "BEGPAREN", "ENDPAREN", "TRUE", "FALSE", "NULL", "SEP", 
			"DOT", "NOT", "SYMBOL", "DOLLAR", "NUM", "COMMENT", "LINE_COMMENT", "FloatingPointLiteral", 
			"StringLiteral", "StringLiteral2", "HexDigit", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "delia.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public deliaParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class DeliaStatementContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(deliaParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public DeliaStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deliaStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDeliaStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDeliaStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor) return ((deliaVisitor<? extends T>)visitor).visitDeliaStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeliaStatementContext deliaStatement() throws RecognitionException {
		DeliaStatementContext _localctx = new DeliaStatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_deliaStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(68);
				statement();
				}
				}
				setState(71); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__2) | (1L << T__4) | (1L << T__5) | (1L << T__17) | (1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__23) | (1L << SYMBOL))) != 0) );
			setState(73);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public LetStatementContext letStatement() {
			return getRuleContext(LetStatementContext.class,0);
		}
		public ConfigureStatementContext configureStatement() {
			return getRuleContext(ConfigureStatementContext.class,0);
		}
		public SchemaStatementContext schemaStatement() {
			return getRuleContext(SchemaStatementContext.class,0);
		}
		public TypeStatementContext typeStatement() {
			return getRuleContext(TypeStatementContext.class,0);
		}
		public InsertStatementContext insertStatement() {
			return getRuleContext(InsertStatementContext.class,0);
		}
		public DeleteStatementContext deleteStatement() {
			return getRuleContext(DeleteStatementContext.class,0);
		}
		public UpdateStatementContext updateStatement() {
			return getRuleContext(UpdateStatementContext.class,0);
		}
		public LogStatementContext logStatement() {
			return getRuleContext(LogStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(83);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__23:
			case SYMBOL:
				enterOuterAlt(_localctx, 1);
				{
				setState(75);
				letStatement();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(76);
				configureStatement();
				}
				break;
			case T__2:
				enterOuterAlt(_localctx, 3);
				{
				setState(77);
				schemaStatement();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 4);
				{
				setState(78);
				typeStatement();
				}
				break;
			case T__17:
				enterOuterAlt(_localctx, 5);
				{
				setState(79);
				insertStatement();
				}
				break;
			case T__20:
				enterOuterAlt(_localctx, 6);
				{
				setState(80);
				deleteStatement();
				}
				break;
			case T__19:
			case T__21:
				enterOuterAlt(_localctx, 7);
				{
				setState(81);
				updateStatement();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 8);
				{
				setState(82);
				logStatement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConfigureStatementContext extends ParserRuleContext {
		public ConfigureStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_configureStatement; }
	 
		public ConfigureStatementContext() { }
		public void copyFrom(ConfigureStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ConfigureContext extends ConfigureStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ScalarContext scalar() {
			return getRuleContext(ScalarContext.class,0);
		}
		public ConfigureContext(ConfigureStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterConfigure(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitConfigure(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitConfigure(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConfigureStatementContext configureStatement() throws RecognitionException {
		ConfigureStatementContext _localctx = new ConfigureStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_configureStatement);
		try {
			_localctx = new ConfigureContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			match(T__0);
			setState(86);
			name();
			setState(87);
			match(T__1);
			setState(88);
			scalar();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SchemaStatementContext extends ParserRuleContext {
		public SchemaStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaStatement; }
	 
		public SchemaStatementContext() { }
		public void copyFrom(SchemaStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SchemaContext extends SchemaStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public SchemaContext(SchemaStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterSchema(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitSchema(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitSchema(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SchemaOffContext extends SchemaStatementContext {
		public SchemaOffContext(SchemaStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterSchemaOff(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitSchemaOff(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitSchemaOff(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaStatementContext schemaStatement() throws RecognitionException {
		SchemaStatementContext _localctx = new SchemaStatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_schemaStatement);
		try {
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				_localctx = new SchemaContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				match(T__2);
				setState(91);
				name();
				}
				break;
			case 2:
				_localctx = new SchemaOffContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				match(T__2);
				setState(93);
				match(T__3);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LogStatementContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ScalarContext scalar() {
			return getRuleContext(ScalarContext.class,0);
		}
		public LogStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterLogStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitLogStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitLogStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogStatementContext logStatement() throws RecognitionException {
		LogStatementContext _localctx = new LogStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_logStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			match(T__4);
			setState(99);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SYMBOL:
				{
				setState(97);
				name();
				}
				break;
			case T__34:
			case TRUE:
			case FALSE:
			case NULL:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				{
				setState(98);
				scalar();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeStatementContext extends ParserRuleContext {
		public ScalarTypeStatementContext scalarTypeStatement() {
			return getRuleContext(ScalarTypeStatementContext.class,0);
		}
		public StructTypeStatementContext structTypeStatement() {
			return getRuleContext(StructTypeStatementContext.class,0);
		}
		public TypeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterTypeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitTypeStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitTypeStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeStatementContext typeStatement() throws RecognitionException {
		TypeStatementContext _localctx = new TypeStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_typeStatement);
		try {
			setState(103);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(101);
				scalarTypeStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(102);
				structTypeStatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScalarTypeStatementContext extends ParserRuleContext {
		public ScalarTypeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scalarTypeStatement; }
	 
		public ScalarTypeStatementContext() { }
		public void copyFrom(ScalarTypeStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TypeScalarContext extends ScalarTypeStatementContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DrulesContext drules() {
			return getRuleContext(DrulesContext.class,0);
		}
		public TypeScalarContext(ScalarTypeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterTypeScalar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitTypeScalar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitTypeScalar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScalarTypeStatementContext scalarTypeStatement() throws RecognitionException {
		ScalarTypeStatementContext _localctx = new ScalarTypeStatementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_scalarTypeStatement);
		int _la;
		try {
			_localctx = new TypeScalarContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(T__5);
			setState(106);
			match(SYMBOL);
			setState(107);
			name();
			setState(109);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__34) | (1L << BEGPAREN) | (1L << TRUE) | (1L << FALSE) | (1L << NULL) | (1L << NOT) | (1L << SYMBOL) | (1L << DOLLAR) | (1L << NUM) | (1L << FloatingPointLiteral) | (1L << StringLiteral) | (1L << StringLiteral2))) != 0)) {
				{
				setState(108);
				drules();
				}
			}

			setState(111);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StructTypeStatementContext extends ParserRuleContext {
		public StructTypeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structTypeStatement; }
	 
		public StructTypeStatementContext() { }
		public void copyFrom(StructTypeStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TypeStructContext extends StructTypeStatementContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public StructFieldsContext structFields() {
			return getRuleContext(StructFieldsContext.class,0);
		}
		public DrulesContext drules() {
			return getRuleContext(DrulesContext.class,0);
		}
		public TypeStructContext(StructTypeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterTypeStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitTypeStruct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitTypeStruct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructTypeStatementContext structTypeStatement() throws RecognitionException {
		StructTypeStatementContext _localctx = new StructTypeStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_structTypeStatement);
		int _la;
		try {
			_localctx = new TypeStructContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			match(T__5);
			setState(114);
			match(SYMBOL);
			setState(115);
			name();
			setState(116);
			match(T__7);
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9 || _la==SYMBOL) {
				{
				setState(117);
				structFields();
				}
			}

			setState(120);
			match(T__8);
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__34) | (1L << BEGPAREN) | (1L << TRUE) | (1L << FALSE) | (1L << NULL) | (1L << NOT) | (1L << SYMBOL) | (1L << DOLLAR) | (1L << NUM) | (1L << FloatingPointLiteral) | (1L << StringLiteral) | (1L << StringLiteral2))) != 0)) {
				{
				setState(121);
				drules();
				}
			}

			setState(124);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StructFieldsContext extends ParserRuleContext {
		public List<StructFieldContext> structField() {
			return getRuleContexts(StructFieldContext.class);
		}
		public StructFieldContext structField(int i) {
			return getRuleContext(StructFieldContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public StructFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStructFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStructFields(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStructFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructFieldsContext structFields() throws RecognitionException {
		StructFieldsContext _localctx = new StructFieldsContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_structFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			structField();
			setState(131);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(127);
				match(SEP);
				setState(128);
				structField();
				}
				}
				setState(133);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StructFieldContext extends ParserRuleContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public RelationNameContext relationName() {
			return getRuleContext(RelationNameContext.class,0);
		}
		public FieldModifiersContext fieldModifiers() {
			return getRuleContext(FieldModifiersContext.class,0);
		}
		public StructFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStructField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStructField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStructField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructFieldContext structField() throws RecognitionException {
		StructFieldContext _localctx = new StructFieldContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_structField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9) {
				{
				setState(134);
				match(T__9);
				}
			}

			setState(137);
			match(SYMBOL);
			setState(138);
			name();
			setState(140);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringLiteral || _la==StringLiteral2) {
				{
				setState(139);
				relationName();
				}
			}

			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << T__16))) != 0)) {
				{
				setState(142);
				fieldModifiers();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationNameContext extends ParserRuleContext {
		public RelationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationName; }
	 
		public RelationNameContext() { }
		public void copyFrom(RelationNameContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RelationNameStrContext extends RelationNameContext {
		public TerminalNode StringLiteral() { return getToken(deliaParser.StringLiteral, 0); }
		public RelationNameStrContext(RelationNameContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterRelationNameStr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitRelationNameStr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitRelationNameStr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RelationNameStr2Context extends RelationNameContext {
		public TerminalNode StringLiteral2() { return getToken(deliaParser.StringLiteral2, 0); }
		public RelationNameStr2Context(RelationNameContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterRelationNameStr2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitRelationNameStr2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitRelationNameStr2(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationNameContext relationName() throws RecognitionException {
		RelationNameContext _localctx = new RelationNameContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_relationName);
		try {
			setState(147);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
				_localctx = new RelationNameStrContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				match(StringLiteral);
				}
				break;
			case StringLiteral2:
				_localctx = new RelationNameStr2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(146);
				match(StringLiteral2);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldModifiersContext extends ParserRuleContext {
		public List<FieldModifierContext> fieldModifier() {
			return getRuleContexts(FieldModifierContext.class);
		}
		public FieldModifierContext fieldModifier(int i) {
			return getRuleContext(FieldModifierContext.class,i);
		}
		public FieldModifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldModifiers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFieldModifiers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFieldModifiers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFieldModifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldModifiersContext fieldModifiers() throws RecognitionException {
		FieldModifiersContext _localctx = new FieldModifiersContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_fieldModifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			fieldModifier();
			setState(153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << T__16))) != 0)) {
				{
				{
				setState(150);
				fieldModifier();
				}
				}
				setState(155);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldModifierContext extends ParserRuleContext {
		public FieldModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFieldModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFieldModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFieldModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldModifierContext fieldModifier() throws RecognitionException {
		FieldModifierContext _localctx = new FieldModifierContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_fieldModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << T__16))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DrulesContext extends ParserRuleContext {
		public List<DruleContext> drule() {
			return getRuleContexts(DruleContext.class);
		}
		public DruleContext drule(int i) {
			return getRuleContext(DruleContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public DrulesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drules; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDrules(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDrules(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDrules(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DrulesContext drules() throws RecognitionException {
		DrulesContext _localctx = new DrulesContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_drules);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			drule();
			setState(163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(159);
				match(SEP);
				setState(160);
				drule();
				}
				}
				setState(165);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DruleContext extends ParserRuleContext {
		public CexprContext cexpr() {
			return getRuleContext(CexprContext.class,0);
		}
		public DruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDrule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDrule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDrule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DruleContext drule() throws RecognitionException {
		DruleContext _localctx = new DruleContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_drule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
			cexpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InsertStatementContext extends ParserRuleContext {
		public InsertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertStatement; }
	 
		public InsertStatementContext() { }
		public void copyFrom(InsertStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class InsertContext extends InsertStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ValuePairsContext valuePairs() {
			return getRuleContext(ValuePairsContext.class,0);
		}
		public InsertContext(InsertStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterInsert(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitInsert(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitInsert(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertStatementContext insertStatement() throws RecognitionException {
		InsertStatementContext _localctx = new InsertStatementContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_insertStatement);
		int _la;
		try {
			_localctx = new InsertContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			match(T__17);
			setState(169);
			name();
			setState(170);
			match(T__7);
			setState(172);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__19) | (1L << T__20) | (1L << SYMBOL))) != 0)) {
				{
				setState(171);
				valuePairs();
				}
			}

			setState(174);
			match(T__8);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValuePairsContext extends ParserRuleContext {
		public ValuePairsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuePairs; }
	 
		public ValuePairsContext() { }
		public void copyFrom(ValuePairsContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class VpValuePairsContext extends ValuePairsContext {
		public List<ValuePairArgContext> valuePairArg() {
			return getRuleContexts(ValuePairArgContext.class);
		}
		public ValuePairArgContext valuePairArg(int i) {
			return getRuleContext(ValuePairArgContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public VpValuePairsContext(ValuePairsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterVpValuePairs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitVpValuePairs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitVpValuePairs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuePairsContext valuePairs() throws RecognitionException {
		ValuePairsContext _localctx = new ValuePairsContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_valuePairs);
		int _la;
		try {
			_localctx = new VpValuePairsContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			valuePairArg();
			setState(181);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(177);
				match(SEP);
				setState(178);
				valuePairArg();
				}
				}
				setState(183);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValuePairArgContext extends ParserRuleContext {
		public ValuePairArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuePairArg; }
	 
		public ValuePairArgContext() { }
		public void copyFrom(ValuePairArgContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class VpListContext extends ValuePairArgContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public TerminalNode BEGF() { return getToken(deliaParser.BEGF, 0); }
		public ElemListContext elemList() {
			return getRuleContext(ElemListContext.class,0);
		}
		public TerminalNode ENDF() { return getToken(deliaParser.ENDF, 0); }
		public CrudActionContext crudAction() {
			return getRuleContext(CrudActionContext.class,0);
		}
		public VpListContext(ValuePairArgContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterVpList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitVpList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitVpList(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class VpElemContext extends ValuePairArgContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public ElemContext elem() {
			return getRuleContext(ElemContext.class,0);
		}
		public CrudActionContext crudAction() {
			return getRuleContext(CrudActionContext.class,0);
		}
		public VpElemContext(ValuePairArgContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterVpElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitVpElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitVpElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuePairArgContext valuePairArg() throws RecognitionException {
		ValuePairArgContext _localctx = new ValuePairArgContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_valuePairArg);
		int _la;
		try {
			setState(199);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				_localctx = new VpElemContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__19) | (1L << T__20))) != 0)) {
					{
					setState(184);
					crudAction();
					}
				}

				setState(187);
				match(SYMBOL);
				setState(188);
				match(T__18);
				setState(189);
				elem();
				}
				break;
			case 2:
				_localctx = new VpListContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(191);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__19) | (1L << T__20))) != 0)) {
					{
					setState(190);
					crudAction();
					}
				}

				setState(193);
				match(SYMBOL);
				setState(194);
				match(T__18);
				setState(195);
				match(BEGF);
				setState(196);
				elemList();
				setState(197);
				match(ENDF);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CrudActionContext extends ParserRuleContext {
		public CrudActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_crudAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterCrudAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitCrudAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitCrudAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CrudActionContext crudAction() throws RecognitionException {
		CrudActionContext _localctx = new CrudActionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_crudAction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__19) | (1L << T__20))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElemListContext extends ParserRuleContext {
		public List<ElemContext> elem() {
			return getRuleContexts(ElemContext.class);
		}
		public ElemContext elem(int i) {
			return getRuleContext(ElemContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public ElemListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elemList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterElemList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitElemList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitElemList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElemListContext elemList() throws RecognitionException {
		ElemListContext _localctx = new ElemListContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_elemList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			elem();
			setState(208);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(204);
				match(SEP);
				setState(205);
				elem();
				}
				}
				setState(210);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeleteStatementContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public DeleteStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDeleteStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDeleteStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDeleteStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeleteStatementContext deleteStatement() throws RecognitionException {
		DeleteStatementContext _localctx = new DeleteStatementContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_deleteStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			match(T__20);
			setState(212);
			name();
			setState(213);
			filter();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UpdateStatementContext extends ParserRuleContext {
		public UpdateStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateStatement; }
	 
		public UpdateStatementContext() { }
		public void copyFrom(UpdateStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Update1Context extends UpdateStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public ValuePairsContext valuePairs() {
			return getRuleContext(ValuePairsContext.class,0);
		}
		public Update1Context(UpdateStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterUpdate1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitUpdate1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitUpdate1(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Upsert1Context extends UpdateStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public UpsertFlagContext upsertFlag() {
			return getRuleContext(UpsertFlagContext.class,0);
		}
		public ValuePairsContext valuePairs() {
			return getRuleContext(ValuePairsContext.class,0);
		}
		public Upsert1Context(UpdateStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterUpsert1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitUpsert1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitUpsert1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UpdateStatementContext updateStatement() throws RecognitionException {
		UpdateStatementContext _localctx = new UpdateStatementContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_updateStatement);
		int _la;
		try {
			setState(236);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__19:
				_localctx = new Update1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(215);
				match(T__19);
				setState(216);
				name();
				setState(217);
				filter();
				setState(218);
				match(T__7);
				setState(220);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__19) | (1L << T__20) | (1L << SYMBOL))) != 0)) {
					{
					setState(219);
					valuePairs();
					}
				}

				setState(222);
				match(T__8);
				}
				break;
			case T__21:
				_localctx = new Upsert1Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(224);
				match(T__21);
				setState(226);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__22) {
					{
					setState(225);
					upsertFlag();
					}
				}

				setState(228);
				name();
				setState(229);
				filter();
				setState(230);
				match(T__7);
				setState(232);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__19) | (1L << T__20) | (1L << SYMBOL))) != 0)) {
					{
					setState(231);
					valuePairs();
					}
				}

				setState(234);
				match(T__8);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UpsertFlagContext extends ParserRuleContext {
		public UpsertFlagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_upsertFlag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterUpsertFlag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitUpsertFlag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitUpsertFlag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UpsertFlagContext upsertFlag() throws RecognitionException {
		UpsertFlagContext _localctx = new UpsertFlagContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_upsertFlag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			match(T__22);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LetStatementContext extends ParserRuleContext {
		public LetStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letStatement; }
	 
		public LetStatementContext() { }
		public void copyFrom(LetStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class LetscalarContext extends LetStatementContext {
		public LetVarContext letVar() {
			return getRuleContext(LetVarContext.class,0);
		}
		public ElemContext elem() {
			return getRuleContext(ElemContext.class,0);
		}
		public LetscalarContext(LetStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterLetscalar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitLetscalar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitLetscalar(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LetContext extends LetStatementContext {
		public LetVarContext letVar() {
			return getRuleContext(LetVarContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public FnChainContext fnChain() {
			return getRuleContext(FnChainContext.class,0);
		}
		public LetContext(LetStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterLet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitLet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitLet(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LetNoVarContext extends LetStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public FnChainContext fnChain() {
			return getRuleContext(FnChainContext.class,0);
		}
		public LetNoVarContext(LetStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterLetNoVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitLetNoVar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitLetNoVar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LetStatementContext letStatement() throws RecognitionException {
		LetStatementContext _localctx = new LetStatementContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_letStatement);
		int _la;
		try {
			setState(258);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				_localctx = new LetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(240);
				match(T__23);
				setState(241);
				letVar();
				setState(242);
				match(T__1);
				setState(243);
				name();
				setState(244);
				filter();
				setState(246);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(245);
					fnChain();
					}
				}

				}
				break;
			case 2:
				_localctx = new LetscalarContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(248);
				match(T__23);
				setState(249);
				letVar();
				setState(250);
				match(T__1);
				setState(251);
				elem();
				}
				break;
			case 3:
				_localctx = new LetNoVarContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(253);
				name();
				setState(254);
				filter();
				setState(256);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(255);
					fnChain();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LetVarContext extends ParserRuleContext {
		public LetVarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letVar; }
	 
		public LetVarContext() { }
		public void copyFrom(LetVarContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class NoTypeContext extends LetVarContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public List<TerminalNode> DOLLAR() { return getTokens(deliaParser.DOLLAR); }
		public TerminalNode DOLLAR(int i) {
			return getToken(deliaParser.DOLLAR, i);
		}
		public NoTypeContext(LetVarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNoType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNoType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNoType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class WithTypeContext extends LetVarContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public List<TerminalNode> DOLLAR() { return getTokens(deliaParser.DOLLAR); }
		public TerminalNode DOLLAR(int i) {
			return getToken(deliaParser.DOLLAR, i);
		}
		public WithTypeContext(LetVarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterWithType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitWithType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitWithType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LetVarContext letVar() throws RecognitionException {
		LetVarContext _localctx = new LetVarContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_letVar);
		try {
			setState(271);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				_localctx = new NoTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(263);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SYMBOL:
					{
					setState(260);
					match(SYMBOL);
					}
					break;
				case DOLLAR:
					{
					setState(261);
					match(DOLLAR);
					setState(262);
					match(DOLLAR);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 2:
				_localctx = new WithTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(268);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SYMBOL:
					{
					setState(265);
					match(SYMBOL);
					}
					break;
				case DOLLAR:
					{
					setState(266);
					match(DOLLAR);
					setState(267);
					match(DOLLAR);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(270);
				name();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FnChainContext extends ParserRuleContext {
		public List<TerminalNode> DOT() { return getTokens(deliaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(deliaParser.DOT, i);
		}
		public List<FnChainArgContext> fnChainArg() {
			return getRuleContexts(FnChainArgContext.class);
		}
		public FnChainArgContext fnChainArg(int i) {
			return getRuleContext(FnChainArgContext.class,i);
		}
		public FnChainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fnChain; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFnChain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFnChain(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFnChain(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FnChainContext fnChain() throws RecognitionException {
		FnChainContext _localctx = new FnChainContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_fnChain);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(273);
			match(DOT);
			setState(274);
			fnChainArg();
			setState(279);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(275);
				match(DOT);
				setState(276);
				fnChainArg();
				}
				}
				setState(281);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FnChainArgContext extends ParserRuleContext {
		public FnContext fn() {
			return getRuleContext(FnContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FnChainArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fnChainArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFnChainArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFnChainArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFnChainArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FnChainArgContext fnChainArg() throws RecognitionException {
		FnChainArgContext _localctx = new FnChainArgContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_fnChainArg);
		try {
			setState(284);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(282);
				fn();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(283);
				name();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FilterContext extends ParserRuleContext {
		public TerminalNode BEGF() { return getToken(deliaParser.BEGF, 0); }
		public CexprContext cexpr() {
			return getRuleContext(CexprContext.class,0);
		}
		public TerminalNode ENDF() { return getToken(deliaParser.ENDF, 0); }
		public FilterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFilter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFilter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFilter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterContext filter() throws RecognitionException {
		FilterContext _localctx = new FilterContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_filter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			match(BEGF);
			setState(287);
			cexpr(0);
			setState(288);
			match(ENDF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CexprContext extends ParserRuleContext {
		public ElemContext elem() {
			return getRuleContext(ElemContext.class,0);
		}
		public TerminalNode BEGPAREN() { return getToken(deliaParser.BEGPAREN, 0); }
		public List<CexprContext> cexpr() {
			return getRuleContexts(CexprContext.class);
		}
		public CexprContext cexpr(int i) {
			return getRuleContext(CexprContext.class,i);
		}
		public TerminalNode ENDPAREN() { return getToken(deliaParser.ENDPAREN, 0); }
		public TerminalNode NOT() { return getToken(deliaParser.NOT, 0); }
		public TerminalNode BEGF() { return getToken(deliaParser.BEGF, 0); }
		public FnargsContext fnargs() {
			return getRuleContext(FnargsContext.class,0);
		}
		public TerminalNode ENDF() { return getToken(deliaParser.ENDF, 0); }
		public CexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cexpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterCexpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitCexpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitCexpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CexprContext cexpr() throws RecognitionException {
		return cexpr(0);
	}

	private CexprContext cexpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		CexprContext _localctx = new CexprContext(_ctx, _parentState);
		CexprContext _prevctx = _localctx;
		int _startState = 56;
		enterRecursionRule(_localctx, 56, RULE_cexpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(301);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__34:
			case TRUE:
			case FALSE:
			case NULL:
			case SYMBOL:
			case DOLLAR:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				{
				setState(291);
				elem();
				}
				break;
			case BEGPAREN:
				{
				setState(292);
				match(BEGPAREN);
				setState(293);
				cexpr(0);
				setState(294);
				match(ENDPAREN);
				}
				break;
			case NOT:
				{
				setState(296);
				match(NOT);
				setState(297);
				match(BEGPAREN);
				setState(298);
				cexpr(0);
				setState(299);
				match(ENDPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(317);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(315);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
					case 1:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(303);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(304);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__25) | (1L << T__26) | (1L << T__27) | (1L << T__28) | (1L << T__29) | (1L << T__30) | (1L << T__31))) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(305);
						cexpr(3);
						}
						break;
					case 2:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(306);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(307);
						_la = _input.LA(1);
						if ( !(_la==T__32 || _la==T__33) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(308);
						cexpr(2);
						}
						break;
					case 3:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(309);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(310);
						match(T__24);
						setState(311);
						match(BEGF);
						setState(312);
						fnargs();
						setState(313);
						match(ENDF);
						}
						break;
					}
					} 
				}
				setState(319);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ElemContext extends ParserRuleContext {
		public ScalarContext scalar() {
			return getRuleContext(ScalarContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FnContext fn() {
			return getRuleContext(FnContext.class,0);
		}
		public List<TerminalNode> DOLLAR() { return getTokens(deliaParser.DOLLAR); }
		public TerminalNode DOLLAR(int i) {
			return getToken(deliaParser.DOLLAR, i);
		}
		public ElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElemContext elem() throws RecognitionException {
		ElemContext _localctx = new ElemContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_elem);
		try {
			setState(325);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(320);
				scalar();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(321);
				name();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(322);
				fn();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(323);
				match(DOLLAR);
				setState(324);
				match(DOLLAR);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FnContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode BEGPAREN() { return getToken(deliaParser.BEGPAREN, 0); }
		public TerminalNode ENDPAREN() { return getToken(deliaParser.ENDPAREN, 0); }
		public FnargsContext fnargs() {
			return getRuleContext(FnargsContext.class,0);
		}
		public FnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FnContext fn() throws RecognitionException {
		FnContext _localctx = new FnContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_fn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(327);
			name();
			setState(328);
			match(BEGPAREN);
			setState(330);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__34) | (1L << TRUE) | (1L << FALSE) | (1L << NULL) | (1L << SYMBOL) | (1L << DOLLAR) | (1L << NUM) | (1L << FloatingPointLiteral) | (1L << StringLiteral) | (1L << StringLiteral2))) != 0)) {
				{
				setState(329);
				fnargs();
				}
			}

			setState(332);
			match(ENDPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FnargsContext extends ParserRuleContext {
		public List<ElemContext> elem() {
			return getRuleContexts(ElemContext.class);
		}
		public ElemContext elem(int i) {
			return getRuleContext(ElemContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public FnargsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fnargs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFnargs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFnargs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFnargs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FnargsContext fnargs() throws RecognitionException {
		FnargsContext _localctx = new FnargsContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_fnargs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(334);
			elem();
			setState(339);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(335);
				match(SEP);
				setState(336);
				elem();
				}
				}
				setState(341);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameContext extends ParserRuleContext {
		public List<TerminalNode> SYMBOL() { return getTokens(deliaParser.SYMBOL); }
		public TerminalNode SYMBOL(int i) {
			return getToken(deliaParser.SYMBOL, i);
		}
		public List<TerminalNode> DOT() { return getTokens(deliaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(deliaParser.DOT, i);
		}
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_name);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(342);
			match(SYMBOL);
			setState(347);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(343);
					match(DOT);
					setState(344);
					match(SYMBOL);
					}
					} 
				}
				setState(349);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScalarContext extends ParserRuleContext {
		public ScalarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scalar; }
	 
		public ScalarContext() { }
		public void copyFrom(ScalarContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class StrContext extends ScalarContext {
		public TerminalNode StringLiteral() { return getToken(deliaParser.StringLiteral, 0); }
		public StrContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NullValueContext extends ScalarContext {
		public TerminalNode NULL() { return getToken(deliaParser.NULL, 0); }
		public NullValueContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNullValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNullValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNullValue(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BoolContext extends ScalarContext {
		public TerminalNode TRUE() { return getToken(deliaParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(deliaParser.FALSE, 0); }
		public BoolContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterBool(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitBool(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitBool(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Str2Context extends ScalarContext {
		public TerminalNode StringLiteral2() { return getToken(deliaParser.StringLiteral2, 0); }
		public Str2Context(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStr2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStr2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStr2(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumContext extends ScalarContext {
		public TerminalNode NUM() { return getToken(deliaParser.NUM, 0); }
		public NumContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNum(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNum(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RealContext extends ScalarContext {
		public TerminalNode FloatingPointLiteral() { return getToken(deliaParser.FloatingPointLiteral, 0); }
		public RealContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterReal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitReal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitReal(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NegNumContext extends ScalarContext {
		public TerminalNode NUM() { return getToken(deliaParser.NUM, 0); }
		public NegNumContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNegNum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNegNum(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNegNum(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NegRealContext extends ScalarContext {
		public TerminalNode FloatingPointLiteral() { return getToken(deliaParser.FloatingPointLiteral, 0); }
		public NegRealContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNegReal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNegReal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNegReal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScalarContext scalar() throws RecognitionException {
		ScalarContext _localctx = new ScalarContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_scalar);
		int _la;
		try {
			setState(360);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				_localctx = new NumContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(350);
				match(NUM);
				}
				break;
			case 2:
				_localctx = new NegNumContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(351);
				match(T__34);
				setState(352);
				match(NUM);
				}
				break;
			case 3:
				_localctx = new RealContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(353);
				match(FloatingPointLiteral);
				}
				break;
			case 4:
				_localctx = new NegRealContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(354);
				match(T__34);
				setState(355);
				match(FloatingPointLiteral);
				}
				break;
			case 5:
				_localctx = new BoolContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(356);
				_la = _input.LA(1);
				if ( !(_la==TRUE || _la==FALSE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 6:
				_localctx = new StrContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(357);
				match(StringLiteral);
				}
				break;
			case 7:
				_localctx = new Str2Context(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(358);
				match(StringLiteral2);
				}
				break;
			case 8:
				_localctx = new NullValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(359);
				match(NULL);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 28:
			return cexpr_sempred((CexprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean cexpr_sempred(CexprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		case 1:
			return precpred(_ctx, 1);
		case 2:
			return precpred(_ctx, 5);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\39\u016d\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\3\2\6\2H\n\2\r\2\16\2I\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\5\3V\n\3\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\5\5a\n\5\3\6\3"+
		"\6\3\6\5\6f\n\6\3\7\3\7\5\7j\n\7\3\b\3\b\3\b\3\b\5\bp\n\b\3\b\3\b\3\t"+
		"\3\t\3\t\3\t\3\t\5\ty\n\t\3\t\3\t\5\t}\n\t\3\t\3\t\3\n\3\n\3\n\7\n\u0084"+
		"\n\n\f\n\16\n\u0087\13\n\3\13\5\13\u008a\n\13\3\13\3\13\3\13\5\13\u008f"+
		"\n\13\3\13\5\13\u0092\n\13\3\f\3\f\5\f\u0096\n\f\3\r\3\r\7\r\u009a\n\r"+
		"\f\r\16\r\u009d\13\r\3\16\3\16\3\17\3\17\3\17\7\17\u00a4\n\17\f\17\16"+
		"\17\u00a7\13\17\3\20\3\20\3\21\3\21\3\21\3\21\5\21\u00af\n\21\3\21\3\21"+
		"\3\22\3\22\3\22\7\22\u00b6\n\22\f\22\16\22\u00b9\13\22\3\23\5\23\u00bc"+
		"\n\23\3\23\3\23\3\23\3\23\5\23\u00c2\n\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\5\23\u00ca\n\23\3\24\3\24\3\25\3\25\3\25\7\25\u00d1\n\25\f\25\16\25\u00d4"+
		"\13\25\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\5\27\u00df\n\27\3"+
		"\27\3\27\3\27\3\27\5\27\u00e5\n\27\3\27\3\27\3\27\3\27\5\27\u00eb\n\27"+
		"\3\27\3\27\5\27\u00ef\n\27\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\5\31"+
		"\u00f9\n\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u0103\n\31\5"+
		"\31\u0105\n\31\3\32\3\32\3\32\5\32\u010a\n\32\3\32\3\32\3\32\5\32\u010f"+
		"\n\32\3\32\5\32\u0112\n\32\3\33\3\33\3\33\3\33\7\33\u0118\n\33\f\33\16"+
		"\33\u011b\13\33\3\34\3\34\5\34\u011f\n\34\3\35\3\35\3\35\3\35\3\36\3\36"+
		"\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\5\36\u0130\n\36\3\36\3\36"+
		"\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\7\36\u013e\n\36\f\36"+
		"\16\36\u0141\13\36\3\37\3\37\3\37\3\37\3\37\5\37\u0148\n\37\3 \3 \3 \5"+
		" \u014d\n \3 \3 \3!\3!\3!\7!\u0154\n!\f!\16!\u0157\13!\3\"\3\"\3\"\7\""+
		"\u015c\n\"\f\"\16\"\u015f\13\"\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\5#\u016b"+
		"\n#\3#\2\3:$\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64"+
		"\668:<>@BD\2\7\3\2\r\23\4\2\24\24\26\27\3\2\34\"\3\2#$\3\2*+\2\u0184\2"+
		"G\3\2\2\2\4U\3\2\2\2\6W\3\2\2\2\b`\3\2\2\2\nb\3\2\2\2\fi\3\2\2\2\16k\3"+
		"\2\2\2\20s\3\2\2\2\22\u0080\3\2\2\2\24\u0089\3\2\2\2\26\u0095\3\2\2\2"+
		"\30\u0097\3\2\2\2\32\u009e\3\2\2\2\34\u00a0\3\2\2\2\36\u00a8\3\2\2\2 "+
		"\u00aa\3\2\2\2\"\u00b2\3\2\2\2$\u00c9\3\2\2\2&\u00cb\3\2\2\2(\u00cd\3"+
		"\2\2\2*\u00d5\3\2\2\2,\u00ee\3\2\2\2.\u00f0\3\2\2\2\60\u0104\3\2\2\2\62"+
		"\u0111\3\2\2\2\64\u0113\3\2\2\2\66\u011e\3\2\2\28\u0120\3\2\2\2:\u012f"+
		"\3\2\2\2<\u0147\3\2\2\2>\u0149\3\2\2\2@\u0150\3\2\2\2B\u0158\3\2\2\2D"+
		"\u016a\3\2\2\2FH\5\4\3\2GF\3\2\2\2HI\3\2\2\2IG\3\2\2\2IJ\3\2\2\2JK\3\2"+
		"\2\2KL\7\2\2\3L\3\3\2\2\2MV\5\60\31\2NV\5\6\4\2OV\5\b\5\2PV\5\f\7\2QV"+
		"\5 \21\2RV\5*\26\2SV\5,\27\2TV\5\n\6\2UM\3\2\2\2UN\3\2\2\2UO\3\2\2\2U"+
		"P\3\2\2\2UQ\3\2\2\2UR\3\2\2\2US\3\2\2\2UT\3\2\2\2V\5\3\2\2\2WX\7\3\2\2"+
		"XY\5B\"\2YZ\7\4\2\2Z[\5D#\2[\7\3\2\2\2\\]\7\5\2\2]a\5B\"\2^_\7\5\2\2_"+
		"a\7\6\2\2`\\\3\2\2\2`^\3\2\2\2a\t\3\2\2\2be\7\7\2\2cf\5B\"\2df\5D#\2e"+
		"c\3\2\2\2ed\3\2\2\2f\13\3\2\2\2gj\5\16\b\2hj\5\20\t\2ig\3\2\2\2ih\3\2"+
		"\2\2j\r\3\2\2\2kl\7\b\2\2lm\7\60\2\2mo\5B\"\2np\5\34\17\2on\3\2\2\2op"+
		"\3\2\2\2pq\3\2\2\2qr\7\t\2\2r\17\3\2\2\2st\7\b\2\2tu\7\60\2\2uv\5B\"\2"+
		"vx\7\n\2\2wy\5\22\n\2xw\3\2\2\2xy\3\2\2\2yz\3\2\2\2z|\7\13\2\2{}\5\34"+
		"\17\2|{\3\2\2\2|}\3\2\2\2}~\3\2\2\2~\177\7\t\2\2\177\21\3\2\2\2\u0080"+
		"\u0085\5\24\13\2\u0081\u0082\7-\2\2\u0082\u0084\5\24\13\2\u0083\u0081"+
		"\3\2\2\2\u0084\u0087\3\2\2\2\u0085\u0083\3\2\2\2\u0085\u0086\3\2\2\2\u0086"+
		"\23\3\2\2\2\u0087\u0085\3\2\2\2\u0088\u008a\7\f\2\2\u0089\u0088\3\2\2"+
		"\2\u0089\u008a\3\2\2\2\u008a\u008b\3\2\2\2\u008b\u008c\7\60\2\2\u008c"+
		"\u008e\5B\"\2\u008d\u008f\5\26\f\2\u008e\u008d\3\2\2\2\u008e\u008f\3\2"+
		"\2\2\u008f\u0091\3\2\2\2\u0090\u0092\5\30\r\2\u0091\u0090\3\2\2\2\u0091"+
		"\u0092\3\2\2\2\u0092\25\3\2\2\2\u0093\u0096\7\66\2\2\u0094\u0096\7\67"+
		"\2\2\u0095\u0093\3\2\2\2\u0095\u0094\3\2\2\2\u0096\27\3\2\2\2\u0097\u009b"+
		"\5\32\16\2\u0098\u009a\5\32\16\2\u0099\u0098\3\2\2\2\u009a\u009d\3\2\2"+
		"\2\u009b\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c\31\3\2\2\2\u009d\u009b"+
		"\3\2\2\2\u009e\u009f\t\2\2\2\u009f\33\3\2\2\2\u00a0\u00a5\5\36\20\2\u00a1"+
		"\u00a2\7-\2\2\u00a2\u00a4\5\36\20\2\u00a3\u00a1\3\2\2\2\u00a4\u00a7\3"+
		"\2\2\2\u00a5\u00a3\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\35\3\2\2\2\u00a7"+
		"\u00a5\3\2\2\2\u00a8\u00a9\5:\36\2\u00a9\37\3\2\2\2\u00aa\u00ab\7\24\2"+
		"\2\u00ab\u00ac\5B\"\2\u00ac\u00ae\7\n\2\2\u00ad\u00af\5\"\22\2\u00ae\u00ad"+
		"\3\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\u00b1\7\13\2\2"+
		"\u00b1!\3\2\2\2\u00b2\u00b7\5$\23\2\u00b3\u00b4\7-\2\2\u00b4\u00b6\5$"+
		"\23\2\u00b5\u00b3\3\2\2\2\u00b6\u00b9\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b7"+
		"\u00b8\3\2\2\2\u00b8#\3\2\2\2\u00b9\u00b7\3\2\2\2\u00ba\u00bc\5&\24\2"+
		"\u00bb\u00ba\3\2\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00be"+
		"\7\60\2\2\u00be\u00bf\7\25\2\2\u00bf\u00ca\5<\37\2\u00c0\u00c2\5&\24\2"+
		"\u00c1\u00c0\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3\u00c4"+
		"\7\60\2\2\u00c4\u00c5\7\25\2\2\u00c5\u00c6\7&\2\2\u00c6\u00c7\5(\25\2"+
		"\u00c7\u00c8\7\'\2\2\u00c8\u00ca\3\2\2\2\u00c9\u00bb\3\2\2\2\u00c9\u00c1"+
		"\3\2\2\2\u00ca%\3\2\2\2\u00cb\u00cc\t\3\2\2\u00cc\'\3\2\2\2\u00cd\u00d2"+
		"\5<\37\2\u00ce\u00cf\7-\2\2\u00cf\u00d1\5<\37\2\u00d0\u00ce\3\2\2\2\u00d1"+
		"\u00d4\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3)\3\2\2\2"+
		"\u00d4\u00d2\3\2\2\2\u00d5\u00d6\7\27\2\2\u00d6\u00d7\5B\"\2\u00d7\u00d8"+
		"\58\35\2\u00d8+\3\2\2\2\u00d9\u00da\7\26\2\2\u00da\u00db\5B\"\2\u00db"+
		"\u00dc\58\35\2\u00dc\u00de\7\n\2\2\u00dd\u00df\5\"\22\2\u00de\u00dd\3"+
		"\2\2\2\u00de\u00df\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e1\7\13\2\2\u00e1"+
		"\u00ef\3\2\2\2\u00e2\u00e4\7\30\2\2\u00e3\u00e5\5.\30\2\u00e4\u00e3\3"+
		"\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\u00e7\5B\"\2\u00e7"+
		"\u00e8\58\35\2\u00e8\u00ea\7\n\2\2\u00e9\u00eb\5\"\22\2\u00ea\u00e9\3"+
		"\2\2\2\u00ea\u00eb\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec\u00ed\7\13\2\2\u00ed"+
		"\u00ef\3\2\2\2\u00ee\u00d9\3\2\2\2\u00ee\u00e2\3\2\2\2\u00ef-\3\2\2\2"+
		"\u00f0\u00f1\7\31\2\2\u00f1/\3\2\2\2\u00f2\u00f3\7\32\2\2\u00f3\u00f4"+
		"\5\62\32\2\u00f4\u00f5\7\4\2\2\u00f5\u00f6\5B\"\2\u00f6\u00f8\58\35\2"+
		"\u00f7\u00f9\5\64\33\2\u00f8\u00f7\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\u0105"+
		"\3\2\2\2\u00fa\u00fb\7\32\2\2\u00fb\u00fc\5\62\32\2\u00fc\u00fd\7\4\2"+
		"\2\u00fd\u00fe\5<\37\2\u00fe\u0105\3\2\2\2\u00ff\u0100\5B\"\2\u0100\u0102"+
		"\58\35\2\u0101\u0103\5\64\33\2\u0102\u0101\3\2\2\2\u0102\u0103\3\2\2\2"+
		"\u0103\u0105\3\2\2\2\u0104\u00f2\3\2\2\2\u0104\u00fa\3\2\2\2\u0104\u00ff"+
		"\3\2\2\2\u0105\61\3\2\2\2\u0106\u010a\7\60\2\2\u0107\u0108\7\61\2\2\u0108"+
		"\u010a\7\61\2\2\u0109\u0106\3\2\2\2\u0109\u0107\3\2\2\2\u010a\u0112\3"+
		"\2\2\2\u010b\u010f\7\60\2\2\u010c\u010d\7\61\2\2\u010d\u010f\7\61\2\2"+
		"\u010e\u010b\3\2\2\2\u010e\u010c\3\2\2\2\u010f\u0110\3\2\2\2\u0110\u0112"+
		"\5B\"\2\u0111\u0109\3\2\2\2\u0111\u010e\3\2\2\2\u0112\63\3\2\2\2\u0113"+
		"\u0114\7.\2\2\u0114\u0119\5\66\34\2\u0115\u0116\7.\2\2\u0116\u0118\5\66"+
		"\34\2\u0117\u0115\3\2\2\2\u0118\u011b\3\2\2\2\u0119\u0117\3\2\2\2\u0119"+
		"\u011a\3\2\2\2\u011a\65\3\2\2\2\u011b\u0119\3\2\2\2\u011c\u011f\5> \2"+
		"\u011d\u011f\5B\"\2\u011e\u011c\3\2\2\2\u011e\u011d\3\2\2\2\u011f\67\3"+
		"\2\2\2\u0120\u0121\7&\2\2\u0121\u0122\5:\36\2\u0122\u0123\7\'\2\2\u0123"+
		"9\3\2\2\2\u0124\u0125\b\36\1\2\u0125\u0130\5<\37\2\u0126\u0127\7(\2\2"+
		"\u0127\u0128\5:\36\2\u0128\u0129\7)\2\2\u0129\u0130\3\2\2\2\u012a\u012b"+
		"\7/\2\2\u012b\u012c\7(\2\2\u012c\u012d\5:\36\2\u012d\u012e\7)\2\2\u012e"+
		"\u0130\3\2\2\2\u012f\u0124\3\2\2\2\u012f\u0126\3\2\2\2\u012f\u012a\3\2"+
		"\2\2\u0130\u013f\3\2\2\2\u0131\u0132\f\4\2\2\u0132\u0133\t\4\2\2\u0133"+
		"\u013e\5:\36\5\u0134\u0135\f\3\2\2\u0135\u0136\t\5\2\2\u0136\u013e\5:"+
		"\36\4\u0137\u0138\f\7\2\2\u0138\u0139\7\33\2\2\u0139\u013a\7&\2\2\u013a"+
		"\u013b\5@!\2\u013b\u013c\7\'\2\2\u013c\u013e\3\2\2\2\u013d\u0131\3\2\2"+
		"\2\u013d\u0134\3\2\2\2\u013d\u0137\3\2\2\2\u013e\u0141\3\2\2\2\u013f\u013d"+
		"\3\2\2\2\u013f\u0140\3\2\2\2\u0140;\3\2\2\2\u0141\u013f\3\2\2\2\u0142"+
		"\u0148\5D#\2\u0143\u0148\5B\"\2\u0144\u0148\5> \2\u0145\u0146\7\61\2\2"+
		"\u0146\u0148\7\61\2\2\u0147\u0142\3\2\2\2\u0147\u0143\3\2\2\2\u0147\u0144"+
		"\3\2\2\2\u0147\u0145\3\2\2\2\u0148=\3\2\2\2\u0149\u014a\5B\"\2\u014a\u014c"+
		"\7(\2\2\u014b\u014d\5@!\2\u014c\u014b\3\2\2\2\u014c\u014d\3\2\2\2\u014d"+
		"\u014e\3\2\2\2\u014e\u014f\7)\2\2\u014f?\3\2\2\2\u0150\u0155\5<\37\2\u0151"+
		"\u0152\7-\2\2\u0152\u0154\5<\37\2\u0153\u0151\3\2\2\2\u0154\u0157\3\2"+
		"\2\2\u0155\u0153\3\2\2\2\u0155\u0156\3\2\2\2\u0156A\3\2\2\2\u0157\u0155"+
		"\3\2\2\2\u0158\u015d\7\60\2\2\u0159\u015a\7.\2\2\u015a\u015c\7\60\2\2"+
		"\u015b\u0159\3\2\2\2\u015c\u015f\3\2\2\2\u015d\u015b\3\2\2\2\u015d\u015e"+
		"\3\2\2\2\u015eC\3\2\2\2\u015f\u015d\3\2\2\2\u0160\u016b\7\62\2\2\u0161"+
		"\u0162\7%\2\2\u0162\u016b\7\62\2\2\u0163\u016b\7\65\2\2\u0164\u0165\7"+
		"%\2\2\u0165\u016b\7\65\2\2\u0166\u016b\t\6\2\2\u0167\u016b\7\66\2\2\u0168"+
		"\u016b\7\67\2\2\u0169\u016b\7,\2\2\u016a\u0160\3\2\2\2\u016a\u0161\3\2"+
		"\2\2\u016a\u0163\3\2\2\2\u016a\u0164\3\2\2\2\u016a\u0166\3\2\2\2\u016a"+
		"\u0167\3\2\2\2\u016a\u0168\3\2\2\2\u016a\u0169\3\2\2\2\u016bE\3\2\2\2"+
		"+IU`eiox|\u0085\u0089\u008e\u0091\u0095\u009b\u00a5\u00ae\u00b7\u00bb"+
		"\u00c1\u00c9\u00d2\u00de\u00e4\u00ea\u00ee\u00f8\u0102\u0104\u0109\u010e"+
		"\u0111\u0119\u011e\u012f\u013d\u013f\u0147\u014c\u0155\u015d\u016a";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}