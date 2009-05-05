grammar boboQL;

options {
	language = Java;
}

tokens{
 KW_SELECT ='SELECT';
 KW_FROM='FROM';
 KW_WHERE='WHERE';
 KW_SORT_DIR_ASC='asc';
 KW_SORT_DIR_DESC='desc';
 KW_ORDER_BY = 'ORDER BY';
 KW_AND = 'AND';
 KW_OR = 'OR';
}

ID	:	('a'..'z'|'A'..'Z')+;

NAME_LIST :	ID (',' ID)* | '*';

WS	:	(' ')+;

sortExpr: KW_SORT_DIR_ASC | KW_SORT_DIR_DESC;

expr 	:	;

select_stmt:	KW_SELECT WS NAME_LIST WS KW_FROM WS ID (KW_WHERE)*;                                                                                                           

stmt	:	select_stmt;
