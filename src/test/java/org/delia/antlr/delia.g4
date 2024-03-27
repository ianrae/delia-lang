grammar delia;

deliaStatement
 : statement+ EOF
;

statement
 : letStatement
 | configureStatement
 | schemaStatement
 | typeStatement
 | insertStatement
 | deleteStatement
 | updateStatement
 | logStatement
 ;

configureStatement
: 'configure' name '=' scalar # configure
;

schemaStatement
: 'schema' name  # schema
| 'schema' 'off' # schemaOff
;

logStatement
: 'log' (name | scalar)
;

typeStatement
 : scalarTypeStatement
 | structTypeStatement
 ;

scalarTypeStatement
 : 'type' SYMBOL name drules? 'end'   # typeScalar
 ;
structTypeStatement
 : 'type' SYMBOL name '{' structFields? '}' drules? 'end'   # typeStruct
 ;

structFields
 : structField (',' structField)*
 ;

structField
 : 'relation'? SYMBOL name relationName? fieldModifiers? defaultValue?
 ;

relationName
 : StringLiteral         # relationNameStr
 | StringLiteral2        # relationNameStr2
;

fieldModifiers
 : fieldModifier (fieldModifier)*
 ;

defaultValue
 : 'default' BEGPAREN defargs? ENDPAREN
 ;

defargs
 : scalar
 | name
 ;

fieldModifier
 : 'optional'
 | 'unique'
 | 'primaryKey'
 | 'serial'
 | 'one'
 | 'many'
 | 'parent'
 ;

drules
 : drule (',' drule)*
 ;

drule
 : cexpr
;

insertStatement
 : 'insert' name '{' valuePairs? '}' #insert
 ;

valuePairs
 : valuePairArg (',' valuePairArg)* #vpValuePairs
 ;

valuePairArg
 : crudAction? SYMBOL ':' valueElem               # vpElem
 | crudAction? SYMBOL ':' '[' valueElemList ']'   # vpList
 ;

crudAction
 : ('update'|'insert'|'delete')
 ;

valueElem
 : cexpr
 | '{' elem ( SEP elem )* '}'
 ;

valueElemList
 : valueElem (',' valueElem)*
 ;

deleteStatement
 : 'delete' name filter
 ;

updateStatement
 : 'update' name filter '{' valuePairs? '}' #update1
 | 'upsert' upsertFlag? name filter '{' valuePairs? '}' #upsert1
 ;

upsertFlag
 : '-noUpdate'
 ;

letStatement
 : 'let' letVar '=' name filter fnChain?   # let
 | 'let' letVar '=' elem                   # letscalar
 | name filter fnChain?                    # letNoVar
 ;

letVar
 : (SYMBOL|DOLLAR DOLLAR)  # noType
 | (SYMBOL|DOLLAR DOLLAR) name  # withType
 ;

fnChain
 : '.' fnChainArg ('.' fnChainArg)*
 ;

fnChainArg
 : fn
 | name
 ;


filter
 : BEGF filterexpr ENDF
 ;

filterexpr
 : cexpr
 | '{' elem ( SEP elem )* '}'
 ;

cexpr
 : elem
 | cexpr 'in' BEGF fnargs ENDF
 | BEGPAREN cexpr ENDPAREN
 | NOT BEGPAREN cexpr ENDPAREN
 | cexpr ('=='|'!='|'<'|'<='|'>'|'>='|'like') cexpr
 | cexpr ('and'|'or') cexpr
 ;

elem
 : scalar
 | name
 | fn
 | DOLLAR DOLLAR
 ;

//list
// : BEGL elem ( SEP elem )* ENDF
// ;

fn
 : name BEGPAREN fnargs? ENDPAREN
 ;
 
fnargs  
 : elem ( SEP elem )*
 ;

name
 : SYMBOL (DOT SYMBOL)*
 ;

scalar: NUM              # Num
 | '-' NUM               # NegNum
 | FloatingPointLiteral  # Real
 | '-' FloatingPointLiteral  # NegReal
 | (TRUE|FALSE)          # Bool
 | StringLiteral         # Str
 | StringLiteral2         # Str2
 | NULL # NullValue
 ;

BEGF : '[';
ENDF : ']';
BEGPAREN : '(';
ENDPAREN : ')';
TRUE : 'true';
FALSE : 'false' ;
NULL : 'null' ;
SEP  : ',';
DOT : '.';
NOT : '!';
SYMBOL : [_a-zA-Z]+ [_a-zA-Z0-9]* ;
DOLLAR : '$' ;

NUM  : [0-9]+;

COMMENT
    : '/*' .*? '*/' -> skip
;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
;

FloatingPointLiteral
    :   ('0'..'9')+ '.' ('0'..'9')* Exponent?
    |   '.' ('0'..'9')+ Exponent?
    |   ('0'..'9')+ Exponent
    |   ('0'..'9')+
    ;

fragment
Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

StringLiteral
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;
StringLiteral2
    :  '\'' ( EscapeSequence2 | ~('\\'|'\'') )* '\''
    ;
fragment
EscapeSequence
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\\')
    |   UnicodeEscape
    |   OctalEscape
    ;
fragment
EscapeSequence2
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\''|'\\')
    |   UnicodeEscape
    |   OctalEscape
    ;

fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

WS   : [ \t\r\n]+ -> skip;