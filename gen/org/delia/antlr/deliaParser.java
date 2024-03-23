// Generated from C:/Users/16136/Documents/GitHub/delia/delia-lang/src/test/java/org/delia/antlr/delia.g4 by ANTLR 4.13.1
package org.delia.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class deliaParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, BEGF=37, ENDF=38, BEGPAREN=39, 
		ENDPAREN=40, TRUE=41, FALSE=42, NULL=43, SEP=44, DOT=45, NOT=46, SYMBOL=47, 
		DOLLAR=48, NUM=49, COMMENT=50, LINE_COMMENT=51, FloatingPointLiteral=52, 
		StringLiteral=53, StringLiteral2=54, HexDigit=55, WS=56;
	public static final int
		RULE_deliaStatement = 0, RULE_statement = 1, RULE_configureStatement = 2, 
		RULE_schemaStatement = 3, RULE_logStatement = 4, RULE_typeStatement = 5, 
		RULE_scalarTypeStatement = 6, RULE_structTypeStatement = 7, RULE_structFields = 8, 
		RULE_structField = 9, RULE_relationName = 10, RULE_fieldModifiers = 11, 
		RULE_defaultValue = 12, RULE_defargs = 13, RULE_fieldModifier = 14, RULE_drules = 15, 
		RULE_drule = 16, RULE_insertStatement = 17, RULE_valuePairs = 18, RULE_valuePairArg = 19, 
		RULE_crudAction = 20, RULE_elemList = 21, RULE_deleteStatement = 22, RULE_updateStatement = 23, 
		RULE_upsertFlag = 24, RULE_letStatement = 25, RULE_letVar = 26, RULE_fnChain = 27, 
		RULE_fnChainArg = 28, RULE_filter = 29, RULE_filterexpr = 30, RULE_cexpr = 31, 
		RULE_elem = 32, RULE_fn = 33, RULE_fnargs = 34, RULE_name = 35, RULE_scalar = 36;
	private static String[] makeRuleNames() {
		return new String[] {
			"deliaStatement", "statement", "configureStatement", "schemaStatement", 
			"logStatement", "typeStatement", "scalarTypeStatement", "structTypeStatement", 
			"structFields", "structField", "relationName", "fieldModifiers", "defaultValue", 
			"defargs", "fieldModifier", "drules", "drule", "insertStatement", "valuePairs", 
			"valuePairArg", "crudAction", "elemList", "deleteStatement", "updateStatement", 
			"upsertFlag", "letStatement", "letVar", "fnChain", "fnChainArg", "filter", 
			"filterexpr", "cexpr", "elem", "fn", "fnargs", "name", "scalar"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'configure'", "'='", "'schema'", "'off'", "'log'", "'type'", "'end'", 
			"'{'", "'}'", "'relation'", "'default'", "'optional'", "'unique'", "'primaryKey'", 
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
			null, "BEGF", "ENDF", "BEGPAREN", "ENDPAREN", "TRUE", "FALSE", "NULL", 
			"SEP", "DOT", "NOT", "SYMBOL", "DOLLAR", "NUM", "COMMENT", "LINE_COMMENT", 
			"FloatingPointLiteral", "StringLiteral", "StringLiteral2", "HexDigit", 
			"WS"
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

	@SuppressWarnings("CheckReturnValue")
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
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDeliaStatement(this);
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
			setState(75); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(74);
				statement();
				}
				}
				setState(77); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 140737537114218L) != 0) );
			setState(79);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(89);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__24:
			case SYMBOL:
				enterOuterAlt(_localctx, 1);
				{
				setState(81);
				letStatement();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(82);
				configureStatement();
				}
				break;
			case T__2:
				enterOuterAlt(_localctx, 3);
				{
				setState(83);
				schemaStatement();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 4);
				{
				setState(84);
				typeStatement();
				}
				break;
			case T__18:
				enterOuterAlt(_localctx, 5);
				{
				setState(85);
				insertStatement();
				}
				break;
			case T__21:
				enterOuterAlt(_localctx, 6);
				{
				setState(86);
				deleteStatement();
				}
				break;
			case T__20:
			case T__22:
				enterOuterAlt(_localctx, 7);
				{
				setState(87);
				updateStatement();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 8);
				{
				setState(88);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
			setState(91);
			match(T__0);
			setState(92);
			name();
			setState(93);
			match(T__1);
			setState(94);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
			setState(100);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				_localctx = new SchemaContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(96);
				match(T__2);
				setState(97);
				name();
				}
				break;
			case 2:
				_localctx = new SchemaOffContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(98);
				match(T__2);
				setState(99);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(102);
			match(T__4);
			setState(105);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SYMBOL:
				{
				setState(103);
				name();
				}
				break;
			case T__35:
			case TRUE:
			case FALSE:
			case NULL:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				{
				setState(104);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(109);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(107);
				scalarTypeStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(108);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
			setState(111);
			match(T__5);
			setState(112);
			match(SYMBOL);
			setState(113);
			name();
			setState(115);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32596740192337920L) != 0)) {
				{
				setState(114);
				drules();
				}
			}

			setState(117);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
			setState(119);
			match(T__5);
			setState(120);
			match(SYMBOL);
			setState(121);
			name();
			setState(122);
			match(T__7);
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9 || _la==SYMBOL) {
				{
				setState(123);
				structFields();
				}
			}

			setState(126);
			match(T__8);
			setState(128);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32596740192337920L) != 0)) {
				{
				setState(127);
				drules();
				}
			}

			setState(130);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(132);
			structField();
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(133);
				match(SEP);
				setState(134);
				structField();
				}
				}
				setState(139);
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

	@SuppressWarnings("CheckReturnValue")
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
		public DefaultValueContext defaultValue() {
			return getRuleContext(DefaultValueContext.class,0);
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
			setState(141);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9) {
				{
				setState(140);
				match(T__9);
				}
			}

			setState(143);
			match(SYMBOL);
			setState(144);
			name();
			setState(146);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringLiteral || _la==StringLiteral2) {
				{
				setState(145);
				relationName();
				}
			}

			setState(149);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 520192L) != 0)) {
				{
				setState(148);
				fieldModifiers();
				}
			}

			setState(152);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__10) {
				{
				setState(151);
				defaultValue();
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
			setState(156);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
				_localctx = new RelationNameStrContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(154);
				match(StringLiteral);
				}
				break;
			case StringLiteral2:
				_localctx = new RelationNameStr2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(155);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(158);
			fieldModifier();
			setState(162);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 520192L) != 0)) {
				{
				{
				setState(159);
				fieldModifier();
				}
				}
				setState(164);
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

	@SuppressWarnings("CheckReturnValue")
	public static class DefaultValueContext extends ParserRuleContext {
		public TerminalNode BEGPAREN() { return getToken(deliaParser.BEGPAREN, 0); }
		public TerminalNode ENDPAREN() { return getToken(deliaParser.ENDPAREN, 0); }
		public DefargsContext defargs() {
			return getRuleContext(DefargsContext.class,0);
		}
		public DefaultValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDefaultValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDefaultValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDefaultValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultValueContext defaultValue() throws RecognitionException {
		DefaultValueContext _localctx = new DefaultValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_defaultValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			match(T__10);
			setState(166);
			match(BEGPAREN);
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32244346715635712L) != 0)) {
				{
				setState(167);
				defargs();
				}
			}

			setState(170);
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

	@SuppressWarnings("CheckReturnValue")
	public static class DefargsContext extends ParserRuleContext {
		public ScalarContext scalar() {
			return getRuleContext(ScalarContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DefargsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defargs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDefargs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDefargs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDefargs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefargsContext defargs() throws RecognitionException {
		DefargsContext _localctx = new DefargsContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_defargs);
		try {
			setState(174);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__35:
			case TRUE:
			case FALSE:
			case NULL:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				enterOuterAlt(_localctx, 1);
				{
				setState(172);
				scalar();
				}
				break;
			case SYMBOL:
				enterOuterAlt(_localctx, 2);
				{
				setState(173);
				name();
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 28, RULE_fieldModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 520192L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 30, RULE_drules);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			drule();
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(179);
				match(SEP);
				setState(180);
				drule();
				}
				}
				setState(185);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 32, RULE_drule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 34, RULE_insertStatement);
		int _la;
		try {
			_localctx = new InsertContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			match(T__18);
			setState(189);
			name();
			setState(190);
			match(T__7);
			setState(192);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 140737495171072L) != 0)) {
				{
				setState(191);
				valuePairs();
				}
			}

			setState(194);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 36, RULE_valuePairs);
		int _la;
		try {
			_localctx = new VpValuePairsContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			valuePairArg();
			setState(201);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(197);
				match(SEP);
				setState(198);
				valuePairArg();
				}
				}
				setState(203);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 38, RULE_valuePairArg);
		int _la;
		try {
			setState(219);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				_localctx = new VpElemContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(205);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 6815744L) != 0)) {
					{
					setState(204);
					crudAction();
					}
				}

				setState(207);
				match(SYMBOL);
				setState(208);
				match(T__19);
				setState(209);
				elem();
				}
				break;
			case 2:
				_localctx = new VpListContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(211);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 6815744L) != 0)) {
					{
					setState(210);
					crudAction();
					}
				}

				setState(213);
				match(SYMBOL);
				setState(214);
				match(T__19);
				setState(215);
				match(BEGF);
				setState(216);
				elemList();
				setState(217);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 40, RULE_crudAction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 6815744L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 42, RULE_elemList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			elem();
			setState(228);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(224);
				match(SEP);
				setState(225);
				elem();
				}
				}
				setState(230);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 44, RULE_deleteStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(231);
			match(T__21);
			setState(232);
			name();
			setState(233);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 46, RULE_updateStatement);
		int _la;
		try {
			setState(256);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__20:
				_localctx = new Update1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(235);
				match(T__20);
				setState(236);
				name();
				setState(237);
				filter();
				setState(238);
				match(T__7);
				setState(240);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 140737495171072L) != 0)) {
					{
					setState(239);
					valuePairs();
					}
				}

				setState(242);
				match(T__8);
				}
				break;
			case T__22:
				_localctx = new Upsert1Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(244);
				match(T__22);
				setState(246);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__23) {
					{
					setState(245);
					upsertFlag();
					}
				}

				setState(248);
				name();
				setState(249);
				filter();
				setState(250);
				match(T__7);
				setState(252);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 140737495171072L) != 0)) {
					{
					setState(251);
					valuePairs();
					}
				}

				setState(254);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 48, RULE_upsertFlag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258);
			match(T__23);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 50, RULE_letStatement);
		int _la;
		try {
			setState(278);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				_localctx = new LetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(260);
				match(T__24);
				setState(261);
				letVar();
				setState(262);
				match(T__1);
				setState(263);
				name();
				setState(264);
				filter();
				setState(266);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(265);
					fnChain();
					}
				}

				}
				break;
			case 2:
				_localctx = new LetscalarContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(268);
				match(T__24);
				setState(269);
				letVar();
				setState(270);
				match(T__1);
				setState(271);
				elem();
				}
				break;
			case 3:
				_localctx = new LetNoVarContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(273);
				name();
				setState(274);
				filter();
				setState(276);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(275);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 52, RULE_letVar);
		try {
			setState(291);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				_localctx = new NoTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(283);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SYMBOL:
					{
					setState(280);
					match(SYMBOL);
					}
					break;
				case DOLLAR:
					{
					setState(281);
					match(DOLLAR);
					setState(282);
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
				setState(288);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SYMBOL:
					{
					setState(285);
					match(SYMBOL);
					}
					break;
				case DOLLAR:
					{
					setState(286);
					match(DOLLAR);
					setState(287);
					match(DOLLAR);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(290);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 54, RULE_fnChain);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293);
			match(DOT);
			setState(294);
			fnChainArg();
			setState(299);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(295);
				match(DOT);
				setState(296);
				fnChainArg();
				}
				}
				setState(301);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 56, RULE_fnChainArg);
		try {
			setState(304);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(302);
				fn();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(303);
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

	@SuppressWarnings("CheckReturnValue")
	public static class FilterContext extends ParserRuleContext {
		public TerminalNode BEGF() { return getToken(deliaParser.BEGF, 0); }
		public FilterexprContext filterexpr() {
			return getRuleContext(FilterexprContext.class,0);
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
		enterRule(_localctx, 58, RULE_filter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(306);
			match(BEGF);
			setState(307);
			filterexpr();
			setState(308);
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

	@SuppressWarnings("CheckReturnValue")
	public static class FilterexprContext extends ParserRuleContext {
		public CexprContext cexpr() {
			return getRuleContext(CexprContext.class,0);
		}
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
		public FilterexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterexpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFilterexpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFilterexpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFilterexpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterexprContext filterexpr() throws RecognitionException {
		FilterexprContext _localctx = new FilterexprContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_filterexpr);
		int _la;
		try {
			setState(322);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__35:
			case BEGPAREN:
			case TRUE:
			case FALSE:
			case NULL:
			case NOT:
			case SYMBOL:
			case DOLLAR:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				enterOuterAlt(_localctx, 1);
				{
				setState(310);
				cexpr(0);
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 2);
				{
				setState(311);
				match(T__7);
				setState(312);
				elem();
				setState(317);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==SEP) {
					{
					{
					setState(313);
					match(SEP);
					setState(314);
					elem();
					}
					}
					setState(319);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(320);
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

	@SuppressWarnings("CheckReturnValue")
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
		int _startState = 62;
		enterRecursionRule(_localctx, 62, RULE_cexpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(335);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__35:
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
				setState(325);
				elem();
				}
				break;
			case BEGPAREN:
				{
				setState(326);
				match(BEGPAREN);
				setState(327);
				cexpr(0);
				setState(328);
				match(ENDPAREN);
				}
				break;
			case NOT:
				{
				setState(330);
				match(NOT);
				setState(331);
				match(BEGPAREN);
				setState(332);
				cexpr(0);
				setState(333);
				match(ENDPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(351);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(349);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
					case 1:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(337);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(338);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17045651456L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(339);
						cexpr(3);
						}
						break;
					case 2:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(340);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(341);
						_la = _input.LA(1);
						if ( !(_la==T__33 || _la==T__34) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(342);
						cexpr(2);
						}
						break;
					case 3:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(343);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(344);
						match(T__25);
						setState(345);
						match(BEGF);
						setState(346);
						fnargs();
						setState(347);
						match(ENDF);
						}
						break;
					}
					} 
				}
				setState(353);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 64, RULE_elem);
		try {
			setState(359);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(354);
				scalar();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(355);
				name();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(356);
				fn();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(357);
				match(DOLLAR);
				setState(358);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 66, RULE_fn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			name();
			setState(362);
			match(BEGPAREN);
			setState(364);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32525821692346368L) != 0)) {
				{
				setState(363);
				fnargs();
				}
			}

			setState(366);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 68, RULE_fnargs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(368);
			elem();
			setState(373);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(369);
				match(SEP);
				setState(370);
				elem();
				}
				}
				setState(375);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 70, RULE_name);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(376);
			match(SYMBOL);
			setState(381);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(377);
					match(DOT);
					setState(378);
					match(SYMBOL);
					}
					} 
				}
				setState(383);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 72, RULE_scalar);
		int _la;
		try {
			setState(394);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				_localctx = new NumContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(384);
				match(NUM);
				}
				break;
			case 2:
				_localctx = new NegNumContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(385);
				match(T__35);
				setState(386);
				match(NUM);
				}
				break;
			case 3:
				_localctx = new RealContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(387);
				match(FloatingPointLiteral);
				}
				break;
			case 4:
				_localctx = new NegRealContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(388);
				match(T__35);
				setState(389);
				match(FloatingPointLiteral);
				}
				break;
			case 5:
				_localctx = new BoolContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(390);
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
				setState(391);
				match(StringLiteral);
				}
				break;
			case 7:
				_localctx = new Str2Context(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(392);
				match(StringLiteral2);
				}
				break;
			case 8:
				_localctx = new NullValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(393);
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
		case 31:
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
		"\u0004\u00018\u018d\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0001\u0000\u0004\u0000L\b\u0000\u000b\u0000\f"+
		"\u0000M\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001Z\b"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003e\b\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0003\u0004j\b\u0004\u0001\u0005\u0001"+
		"\u0005\u0003\u0005n\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0003\u0006t\b\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007}\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0081\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\b\u0001\b\u0001\b\u0005\b\u0088\b\b\n\b\f\b\u008b\t\b\u0001\t\u0003\t"+
		"\u008e\b\t\u0001\t\u0001\t\u0001\t\u0003\t\u0093\b\t\u0001\t\u0003\t\u0096"+
		"\b\t\u0001\t\u0003\t\u0099\b\t\u0001\n\u0001\n\u0003\n\u009d\b\n\u0001"+
		"\u000b\u0001\u000b\u0005\u000b\u00a1\b\u000b\n\u000b\f\u000b\u00a4\t\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0003\f\u00a9\b\f\u0001\f\u0001\f\u0001\r\u0001"+
		"\r\u0003\r\u00af\b\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0005\u000f\u00b6\b\u000f\n\u000f\f\u000f\u00b9\t\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011"+
		"\u00c1\b\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0005\u0012\u00c8\b\u0012\n\u0012\f\u0012\u00cb\t\u0012\u0001\u0013\u0003"+
		"\u0013\u00ce\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003"+
		"\u0013\u00d4\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0003\u0013\u00dc\b\u0013\u0001\u0014\u0001\u0014\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u00e3\b\u0015\n\u0015\f\u0015"+
		"\u00e6\t\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u00f1\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u00f7\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u00fd\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u0101\b\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0003\u0019\u010b\b\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u0115\b\u0019"+
		"\u0003\u0019\u0117\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a"+
		"\u011c\b\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u0121\b"+
		"\u001a\u0001\u001a\u0003\u001a\u0124\b\u001a\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0005\u001b\u012a\b\u001b\n\u001b\f\u001b\u012d\t\u001b"+
		"\u0001\u001c\u0001\u001c\u0003\u001c\u0131\b\u001c\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0005\u001e\u013c\b\u001e\n\u001e\f\u001e\u013f\t\u001e\u0001"+
		"\u001e\u0001\u001e\u0003\u001e\u0143\b\u001e\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u0150\b\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0005\u001f\u015e"+
		"\b\u001f\n\u001f\f\u001f\u0161\t\u001f\u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0003 \u0168\b \u0001!\u0001!\u0001!\u0003!\u016d\b!\u0001!\u0001!\u0001"+
		"\"\u0001\"\u0001\"\u0005\"\u0174\b\"\n\"\f\"\u0177\t\"\u0001#\u0001#\u0001"+
		"#\u0005#\u017c\b#\n#\f#\u017f\t#\u0001$\u0001$\u0001$\u0001$\u0001$\u0001"+
		"$\u0001$\u0001$\u0001$\u0001$\u0003$\u018b\b$\u0001$\u0000\u0001>%\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*,.02468:<>@BDFH\u0000\u0005\u0001\u0000\f\u0012\u0002\u0000"+
		"\u0013\u0013\u0015\u0016\u0001\u0000\u001b!\u0001\u0000\"#\u0001\u0000"+
		")*\u01a6\u0000K\u0001\u0000\u0000\u0000\u0002Y\u0001\u0000\u0000\u0000"+
		"\u0004[\u0001\u0000\u0000\u0000\u0006d\u0001\u0000\u0000\u0000\bf\u0001"+
		"\u0000\u0000\u0000\nm\u0001\u0000\u0000\u0000\fo\u0001\u0000\u0000\u0000"+
		"\u000ew\u0001\u0000\u0000\u0000\u0010\u0084\u0001\u0000\u0000\u0000\u0012"+
		"\u008d\u0001\u0000\u0000\u0000\u0014\u009c\u0001\u0000\u0000\u0000\u0016"+
		"\u009e\u0001\u0000\u0000\u0000\u0018\u00a5\u0001\u0000\u0000\u0000\u001a"+
		"\u00ae\u0001\u0000\u0000\u0000\u001c\u00b0\u0001\u0000\u0000\u0000\u001e"+
		"\u00b2\u0001\u0000\u0000\u0000 \u00ba\u0001\u0000\u0000\u0000\"\u00bc"+
		"\u0001\u0000\u0000\u0000$\u00c4\u0001\u0000\u0000\u0000&\u00db\u0001\u0000"+
		"\u0000\u0000(\u00dd\u0001\u0000\u0000\u0000*\u00df\u0001\u0000\u0000\u0000"+
		",\u00e7\u0001\u0000\u0000\u0000.\u0100\u0001\u0000\u0000\u00000\u0102"+
		"\u0001\u0000\u0000\u00002\u0116\u0001\u0000\u0000\u00004\u0123\u0001\u0000"+
		"\u0000\u00006\u0125\u0001\u0000\u0000\u00008\u0130\u0001\u0000\u0000\u0000"+
		":\u0132\u0001\u0000\u0000\u0000<\u0142\u0001\u0000\u0000\u0000>\u014f"+
		"\u0001\u0000\u0000\u0000@\u0167\u0001\u0000\u0000\u0000B\u0169\u0001\u0000"+
		"\u0000\u0000D\u0170\u0001\u0000\u0000\u0000F\u0178\u0001\u0000\u0000\u0000"+
		"H\u018a\u0001\u0000\u0000\u0000JL\u0003\u0002\u0001\u0000KJ\u0001\u0000"+
		"\u0000\u0000LM\u0001\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000MN\u0001"+
		"\u0000\u0000\u0000NO\u0001\u0000\u0000\u0000OP\u0005\u0000\u0000\u0001"+
		"P\u0001\u0001\u0000\u0000\u0000QZ\u00032\u0019\u0000RZ\u0003\u0004\u0002"+
		"\u0000SZ\u0003\u0006\u0003\u0000TZ\u0003\n\u0005\u0000UZ\u0003\"\u0011"+
		"\u0000VZ\u0003,\u0016\u0000WZ\u0003.\u0017\u0000XZ\u0003\b\u0004\u0000"+
		"YQ\u0001\u0000\u0000\u0000YR\u0001\u0000\u0000\u0000YS\u0001\u0000\u0000"+
		"\u0000YT\u0001\u0000\u0000\u0000YU\u0001\u0000\u0000\u0000YV\u0001\u0000"+
		"\u0000\u0000YW\u0001\u0000\u0000\u0000YX\u0001\u0000\u0000\u0000Z\u0003"+
		"\u0001\u0000\u0000\u0000[\\\u0005\u0001\u0000\u0000\\]\u0003F#\u0000]"+
		"^\u0005\u0002\u0000\u0000^_\u0003H$\u0000_\u0005\u0001\u0000\u0000\u0000"+
		"`a\u0005\u0003\u0000\u0000ae\u0003F#\u0000bc\u0005\u0003\u0000\u0000c"+
		"e\u0005\u0004\u0000\u0000d`\u0001\u0000\u0000\u0000db\u0001\u0000\u0000"+
		"\u0000e\u0007\u0001\u0000\u0000\u0000fi\u0005\u0005\u0000\u0000gj\u0003"+
		"F#\u0000hj\u0003H$\u0000ig\u0001\u0000\u0000\u0000ih\u0001\u0000\u0000"+
		"\u0000j\t\u0001\u0000\u0000\u0000kn\u0003\f\u0006\u0000ln\u0003\u000e"+
		"\u0007\u0000mk\u0001\u0000\u0000\u0000ml\u0001\u0000\u0000\u0000n\u000b"+
		"\u0001\u0000\u0000\u0000op\u0005\u0006\u0000\u0000pq\u0005/\u0000\u0000"+
		"qs\u0003F#\u0000rt\u0003\u001e\u000f\u0000sr\u0001\u0000\u0000\u0000s"+
		"t\u0001\u0000\u0000\u0000tu\u0001\u0000\u0000\u0000uv\u0005\u0007\u0000"+
		"\u0000v\r\u0001\u0000\u0000\u0000wx\u0005\u0006\u0000\u0000xy\u0005/\u0000"+
		"\u0000yz\u0003F#\u0000z|\u0005\b\u0000\u0000{}\u0003\u0010\b\u0000|{\u0001"+
		"\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000}~\u0001\u0000\u0000\u0000"+
		"~\u0080\u0005\t\u0000\u0000\u007f\u0081\u0003\u001e\u000f\u0000\u0080"+
		"\u007f\u0001\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000\u0000\u0081"+
		"\u0082\u0001\u0000\u0000\u0000\u0082\u0083\u0005\u0007\u0000\u0000\u0083"+
		"\u000f\u0001\u0000\u0000\u0000\u0084\u0089\u0003\u0012\t\u0000\u0085\u0086"+
		"\u0005,\u0000\u0000\u0086\u0088\u0003\u0012\t\u0000\u0087\u0085\u0001"+
		"\u0000\u0000\u0000\u0088\u008b\u0001\u0000\u0000\u0000\u0089\u0087\u0001"+
		"\u0000\u0000\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a\u0011\u0001"+
		"\u0000\u0000\u0000\u008b\u0089\u0001\u0000\u0000\u0000\u008c\u008e\u0005"+
		"\n\u0000\u0000\u008d\u008c\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000"+
		"\u0000\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f\u0090\u0005/\u0000"+
		"\u0000\u0090\u0092\u0003F#\u0000\u0091\u0093\u0003\u0014\n\u0000\u0092"+
		"\u0091\u0001\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000\u0000\u0093"+
		"\u0095\u0001\u0000\u0000\u0000\u0094\u0096\u0003\u0016\u000b\u0000\u0095"+
		"\u0094\u0001\u0000\u0000\u0000\u0095\u0096\u0001\u0000\u0000\u0000\u0096"+
		"\u0098\u0001\u0000\u0000\u0000\u0097\u0099\u0003\u0018\f\u0000\u0098\u0097"+
		"\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u0013"+
		"\u0001\u0000\u0000\u0000\u009a\u009d\u00055\u0000\u0000\u009b\u009d\u0005"+
		"6\u0000\u0000\u009c\u009a\u0001\u0000\u0000\u0000\u009c\u009b\u0001\u0000"+
		"\u0000\u0000\u009d\u0015\u0001\u0000\u0000\u0000\u009e\u00a2\u0003\u001c"+
		"\u000e\u0000\u009f\u00a1\u0003\u001c\u000e\u0000\u00a0\u009f\u0001\u0000"+
		"\u0000\u0000\u00a1\u00a4\u0001\u0000\u0000\u0000\u00a2\u00a0\u0001\u0000"+
		"\u0000\u0000\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u0017\u0001\u0000"+
		"\u0000\u0000\u00a4\u00a2\u0001\u0000\u0000\u0000\u00a5\u00a6\u0005\u000b"+
		"\u0000\u0000\u00a6\u00a8\u0005\'\u0000\u0000\u00a7\u00a9\u0003\u001a\r"+
		"\u0000\u00a8\u00a7\u0001\u0000\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000"+
		"\u0000\u00a9\u00aa\u0001\u0000\u0000\u0000\u00aa\u00ab\u0005(\u0000\u0000"+
		"\u00ab\u0019\u0001\u0000\u0000\u0000\u00ac\u00af\u0003H$\u0000\u00ad\u00af"+
		"\u0003F#\u0000\u00ae\u00ac\u0001\u0000\u0000\u0000\u00ae\u00ad\u0001\u0000"+
		"\u0000\u0000\u00af\u001b\u0001\u0000\u0000\u0000\u00b0\u00b1\u0007\u0000"+
		"\u0000\u0000\u00b1\u001d\u0001\u0000\u0000\u0000\u00b2\u00b7\u0003 \u0010"+
		"\u0000\u00b3\u00b4\u0005,\u0000\u0000\u00b4\u00b6\u0003 \u0010\u0000\u00b5"+
		"\u00b3\u0001\u0000\u0000\u0000\u00b6\u00b9\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b5\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8"+
		"\u001f\u0001\u0000\u0000\u0000\u00b9\u00b7\u0001\u0000\u0000\u0000\u00ba"+
		"\u00bb\u0003>\u001f\u0000\u00bb!\u0001\u0000\u0000\u0000\u00bc\u00bd\u0005"+
		"\u0013\u0000\u0000\u00bd\u00be\u0003F#\u0000\u00be\u00c0\u0005\b\u0000"+
		"\u0000\u00bf\u00c1\u0003$\u0012\u0000\u00c0\u00bf\u0001\u0000\u0000\u0000"+
		"\u00c0\u00c1\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000\u0000"+
		"\u00c2\u00c3\u0005\t\u0000\u0000\u00c3#\u0001\u0000\u0000\u0000\u00c4"+
		"\u00c9\u0003&\u0013\u0000\u00c5\u00c6\u0005,\u0000\u0000\u00c6\u00c8\u0003"+
		"&\u0013\u0000\u00c7\u00c5\u0001\u0000\u0000\u0000\u00c8\u00cb\u0001\u0000"+
		"\u0000\u0000\u00c9\u00c7\u0001\u0000\u0000\u0000\u00c9\u00ca\u0001\u0000"+
		"\u0000\u0000\u00ca%\u0001\u0000\u0000\u0000\u00cb\u00c9\u0001\u0000\u0000"+
		"\u0000\u00cc\u00ce\u0003(\u0014\u0000\u00cd\u00cc\u0001\u0000\u0000\u0000"+
		"\u00cd\u00ce\u0001\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000\u0000\u0000"+
		"\u00cf\u00d0\u0005/\u0000\u0000\u00d0\u00d1\u0005\u0014\u0000\u0000\u00d1"+
		"\u00dc\u0003@ \u0000\u00d2\u00d4\u0003(\u0014\u0000\u00d3\u00d2\u0001"+
		"\u0000\u0000\u0000\u00d3\u00d4\u0001\u0000\u0000\u0000\u00d4\u00d5\u0001"+
		"\u0000\u0000\u0000\u00d5\u00d6\u0005/\u0000\u0000\u00d6\u00d7\u0005\u0014"+
		"\u0000\u0000\u00d7\u00d8\u0005%\u0000\u0000\u00d8\u00d9\u0003*\u0015\u0000"+
		"\u00d9\u00da\u0005&\u0000\u0000\u00da\u00dc\u0001\u0000\u0000\u0000\u00db"+
		"\u00cd\u0001\u0000\u0000\u0000\u00db\u00d3\u0001\u0000\u0000\u0000\u00dc"+
		"\'\u0001\u0000\u0000\u0000\u00dd\u00de\u0007\u0001\u0000\u0000\u00de)"+
		"\u0001\u0000\u0000\u0000\u00df\u00e4\u0003@ \u0000\u00e0\u00e1\u0005,"+
		"\u0000\u0000\u00e1\u00e3\u0003@ \u0000\u00e2\u00e0\u0001\u0000\u0000\u0000"+
		"\u00e3\u00e6\u0001\u0000\u0000\u0000\u00e4\u00e2\u0001\u0000\u0000\u0000"+
		"\u00e4\u00e5\u0001\u0000\u0000\u0000\u00e5+\u0001\u0000\u0000\u0000\u00e6"+
		"\u00e4\u0001\u0000\u0000\u0000\u00e7\u00e8\u0005\u0016\u0000\u0000\u00e8"+
		"\u00e9\u0003F#\u0000\u00e9\u00ea\u0003:\u001d\u0000\u00ea-\u0001\u0000"+
		"\u0000\u0000\u00eb\u00ec\u0005\u0015\u0000\u0000\u00ec\u00ed\u0003F#\u0000"+
		"\u00ed\u00ee\u0003:\u001d\u0000\u00ee\u00f0\u0005\b\u0000\u0000\u00ef"+
		"\u00f1\u0003$\u0012\u0000\u00f0\u00ef\u0001\u0000\u0000\u0000\u00f0\u00f1"+
		"\u0001\u0000\u0000\u0000\u00f1\u00f2\u0001\u0000\u0000\u0000\u00f2\u00f3"+
		"\u0005\t\u0000\u0000\u00f3\u0101\u0001\u0000\u0000\u0000\u00f4\u00f6\u0005"+
		"\u0017\u0000\u0000\u00f5\u00f7\u00030\u0018\u0000\u00f6\u00f5\u0001\u0000"+
		"\u0000\u0000\u00f6\u00f7\u0001\u0000\u0000\u0000\u00f7\u00f8\u0001\u0000"+
		"\u0000\u0000\u00f8\u00f9\u0003F#\u0000\u00f9\u00fa\u0003:\u001d\u0000"+
		"\u00fa\u00fc\u0005\b\u0000\u0000\u00fb\u00fd\u0003$\u0012\u0000\u00fc"+
		"\u00fb\u0001\u0000\u0000\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000\u00fd"+
		"\u00fe\u0001\u0000\u0000\u0000\u00fe\u00ff\u0005\t\u0000\u0000\u00ff\u0101"+
		"\u0001\u0000\u0000\u0000\u0100\u00eb\u0001\u0000\u0000\u0000\u0100\u00f4"+
		"\u0001\u0000\u0000\u0000\u0101/\u0001\u0000\u0000\u0000\u0102\u0103\u0005"+
		"\u0018\u0000\u0000\u01031\u0001\u0000\u0000\u0000\u0104\u0105\u0005\u0019"+
		"\u0000\u0000\u0105\u0106\u00034\u001a\u0000\u0106\u0107\u0005\u0002\u0000"+
		"\u0000\u0107\u0108\u0003F#\u0000\u0108\u010a\u0003:\u001d\u0000\u0109"+
		"\u010b\u00036\u001b\u0000\u010a\u0109\u0001\u0000\u0000\u0000\u010a\u010b"+
		"\u0001\u0000\u0000\u0000\u010b\u0117\u0001\u0000\u0000\u0000\u010c\u010d"+
		"\u0005\u0019\u0000\u0000\u010d\u010e\u00034\u001a\u0000\u010e\u010f\u0005"+
		"\u0002\u0000\u0000\u010f\u0110\u0003@ \u0000\u0110\u0117\u0001\u0000\u0000"+
		"\u0000\u0111\u0112\u0003F#\u0000\u0112\u0114\u0003:\u001d\u0000\u0113"+
		"\u0115\u00036\u001b\u0000\u0114\u0113\u0001\u0000\u0000\u0000\u0114\u0115"+
		"\u0001\u0000\u0000\u0000\u0115\u0117\u0001\u0000\u0000\u0000\u0116\u0104"+
		"\u0001\u0000\u0000\u0000\u0116\u010c\u0001\u0000\u0000\u0000\u0116\u0111"+
		"\u0001\u0000\u0000\u0000\u01173\u0001\u0000\u0000\u0000\u0118\u011c\u0005"+
		"/\u0000\u0000\u0119\u011a\u00050\u0000\u0000\u011a\u011c\u00050\u0000"+
		"\u0000\u011b\u0118\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000\u0000"+
		"\u0000\u011c\u0124\u0001\u0000\u0000\u0000\u011d\u0121\u0005/\u0000\u0000"+
		"\u011e\u011f\u00050\u0000\u0000\u011f\u0121\u00050\u0000\u0000\u0120\u011d"+
		"\u0001\u0000\u0000\u0000\u0120\u011e\u0001\u0000\u0000\u0000\u0121\u0122"+
		"\u0001\u0000\u0000\u0000\u0122\u0124\u0003F#\u0000\u0123\u011b\u0001\u0000"+
		"\u0000\u0000\u0123\u0120\u0001\u0000\u0000\u0000\u01245\u0001\u0000\u0000"+
		"\u0000\u0125\u0126\u0005-\u0000\u0000\u0126\u012b\u00038\u001c\u0000\u0127"+
		"\u0128\u0005-\u0000\u0000\u0128\u012a\u00038\u001c\u0000\u0129\u0127\u0001"+
		"\u0000\u0000\u0000\u012a\u012d\u0001\u0000\u0000\u0000\u012b\u0129\u0001"+
		"\u0000\u0000\u0000\u012b\u012c\u0001\u0000\u0000\u0000\u012c7\u0001\u0000"+
		"\u0000\u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012e\u0131\u0003B!\u0000"+
		"\u012f\u0131\u0003F#\u0000\u0130\u012e\u0001\u0000\u0000\u0000\u0130\u012f"+
		"\u0001\u0000\u0000\u0000\u01319\u0001\u0000\u0000\u0000\u0132\u0133\u0005"+
		"%\u0000\u0000\u0133\u0134\u0003<\u001e\u0000\u0134\u0135\u0005&\u0000"+
		"\u0000\u0135;\u0001\u0000\u0000\u0000\u0136\u0143\u0003>\u001f\u0000\u0137"+
		"\u0138\u0005\b\u0000\u0000\u0138\u013d\u0003@ \u0000\u0139\u013a\u0005"+
		",\u0000\u0000\u013a\u013c\u0003@ \u0000\u013b\u0139\u0001\u0000\u0000"+
		"\u0000\u013c\u013f\u0001\u0000\u0000\u0000\u013d\u013b\u0001\u0000\u0000"+
		"\u0000\u013d\u013e\u0001\u0000\u0000\u0000\u013e\u0140\u0001\u0000\u0000"+
		"\u0000\u013f\u013d\u0001\u0000\u0000\u0000\u0140\u0141\u0005\t\u0000\u0000"+
		"\u0141\u0143\u0001\u0000\u0000\u0000\u0142\u0136\u0001\u0000\u0000\u0000"+
		"\u0142\u0137\u0001\u0000\u0000\u0000\u0143=\u0001\u0000\u0000\u0000\u0144"+
		"\u0145\u0006\u001f\uffff\uffff\u0000\u0145\u0150\u0003@ \u0000\u0146\u0147"+
		"\u0005\'\u0000\u0000\u0147\u0148\u0003>\u001f\u0000\u0148\u0149\u0005"+
		"(\u0000\u0000\u0149\u0150\u0001\u0000\u0000\u0000\u014a\u014b\u0005.\u0000"+
		"\u0000\u014b\u014c\u0005\'\u0000\u0000\u014c\u014d\u0003>\u001f\u0000"+
		"\u014d\u014e\u0005(\u0000\u0000\u014e\u0150\u0001\u0000\u0000\u0000\u014f"+
		"\u0144\u0001\u0000\u0000\u0000\u014f\u0146\u0001\u0000\u0000\u0000\u014f"+
		"\u014a\u0001\u0000\u0000\u0000\u0150\u015f\u0001\u0000\u0000\u0000\u0151"+
		"\u0152\n\u0002\u0000\u0000\u0152\u0153\u0007\u0002\u0000\u0000\u0153\u015e"+
		"\u0003>\u001f\u0003\u0154\u0155\n\u0001\u0000\u0000\u0155\u0156\u0007"+
		"\u0003\u0000\u0000\u0156\u015e\u0003>\u001f\u0002\u0157\u0158\n\u0005"+
		"\u0000\u0000\u0158\u0159\u0005\u001a\u0000\u0000\u0159\u015a\u0005%\u0000"+
		"\u0000\u015a\u015b\u0003D\"\u0000\u015b\u015c\u0005&\u0000\u0000\u015c"+
		"\u015e\u0001\u0000\u0000\u0000\u015d\u0151\u0001\u0000\u0000\u0000\u015d"+
		"\u0154\u0001\u0000\u0000\u0000\u015d\u0157\u0001\u0000\u0000\u0000\u015e"+
		"\u0161\u0001\u0000\u0000\u0000\u015f\u015d\u0001\u0000\u0000\u0000\u015f"+
		"\u0160\u0001\u0000\u0000\u0000\u0160?\u0001\u0000\u0000\u0000\u0161\u015f"+
		"\u0001\u0000\u0000\u0000\u0162\u0168\u0003H$\u0000\u0163\u0168\u0003F"+
		"#\u0000\u0164\u0168\u0003B!\u0000\u0165\u0166\u00050\u0000\u0000\u0166"+
		"\u0168\u00050\u0000\u0000\u0167\u0162\u0001\u0000\u0000\u0000\u0167\u0163"+
		"\u0001\u0000\u0000\u0000\u0167\u0164\u0001\u0000\u0000\u0000\u0167\u0165"+
		"\u0001\u0000\u0000\u0000\u0168A\u0001\u0000\u0000\u0000\u0169\u016a\u0003"+
		"F#\u0000\u016a\u016c\u0005\'\u0000\u0000\u016b\u016d\u0003D\"\u0000\u016c"+
		"\u016b\u0001\u0000\u0000\u0000\u016c\u016d\u0001\u0000\u0000\u0000\u016d"+
		"\u016e\u0001\u0000\u0000\u0000\u016e\u016f\u0005(\u0000\u0000\u016fC\u0001"+
		"\u0000\u0000\u0000\u0170\u0175\u0003@ \u0000\u0171\u0172\u0005,\u0000"+
		"\u0000\u0172\u0174\u0003@ \u0000\u0173\u0171\u0001\u0000\u0000\u0000\u0174"+
		"\u0177\u0001\u0000\u0000\u0000\u0175\u0173\u0001\u0000\u0000\u0000\u0175"+
		"\u0176\u0001\u0000\u0000\u0000\u0176E\u0001\u0000\u0000\u0000\u0177\u0175"+
		"\u0001\u0000\u0000\u0000\u0178\u017d\u0005/\u0000\u0000\u0179\u017a\u0005"+
		"-\u0000\u0000\u017a\u017c\u0005/\u0000\u0000\u017b\u0179\u0001\u0000\u0000"+
		"\u0000\u017c\u017f\u0001\u0000\u0000\u0000\u017d\u017b\u0001\u0000\u0000"+
		"\u0000\u017d\u017e\u0001\u0000\u0000\u0000\u017eG\u0001\u0000\u0000\u0000"+
		"\u017f\u017d\u0001\u0000\u0000\u0000\u0180\u018b\u00051\u0000\u0000\u0181"+
		"\u0182\u0005$\u0000\u0000\u0182\u018b\u00051\u0000\u0000\u0183\u018b\u0005"+
		"4\u0000\u0000\u0184\u0185\u0005$\u0000\u0000\u0185\u018b\u00054\u0000"+
		"\u0000\u0186\u018b\u0007\u0004\u0000\u0000\u0187\u018b\u00055\u0000\u0000"+
		"\u0188\u018b\u00056\u0000\u0000\u0189\u018b\u0005+\u0000\u0000\u018a\u0180"+
		"\u0001\u0000\u0000\u0000\u018a\u0181\u0001\u0000\u0000\u0000\u018a\u0183"+
		"\u0001\u0000\u0000\u0000\u018a\u0184\u0001\u0000\u0000\u0000\u018a\u0186"+
		"\u0001\u0000\u0000\u0000\u018a\u0187\u0001\u0000\u0000\u0000\u018a\u0188"+
		"\u0001\u0000\u0000\u0000\u018a\u0189\u0001\u0000\u0000\u0000\u018bI\u0001"+
		"\u0000\u0000\u0000.MYdims|\u0080\u0089\u008d\u0092\u0095\u0098\u009c\u00a2"+
		"\u00a8\u00ae\u00b7\u00c0\u00c9\u00cd\u00d3\u00db\u00e4\u00f0\u00f6\u00fc"+
		"\u0100\u010a\u0114\u0116\u011b\u0120\u0123\u012b\u0130\u013d\u0142\u014f"+
		"\u015d\u015f\u0167\u016c\u0175\u017d\u018a";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}