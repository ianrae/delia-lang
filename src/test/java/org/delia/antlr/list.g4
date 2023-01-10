grammar list;

list
 : BEGL elems? ENDL
 NUM
 ;

elems
 : elem ( SEP elem )*
 ;

elem
 : NUM
 ;

BEGL : '[';
ENDL : ']';
SEP  : ',';
NUM  : [0-9]+;
WS   : [ \t\r\n]+ -> skip;