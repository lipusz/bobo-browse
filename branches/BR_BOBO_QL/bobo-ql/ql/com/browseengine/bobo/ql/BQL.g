grammar BQL;

options {
	language = Java;
}

tokens{
KW_AND = 'AND';
KW_OR 	= 'OR';
}
@header{
package com.browseengine.bobo.ql.output;
import java.util.ArrayList;
import com.browseengine.bobo.api.BrowseRequest;
import org.apache.lucene.search.SortField;
}

@lexer::header{ 
package com.browseengine.bobo.ql.output;
} 

@members{
ArrayList<String> _selectedOutputFields=new ArrayList<String>();
BrowseRequest br = new BrowseRequest();
}

KW_SELECT : ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');
KW_SORT_DIR_ASC	: ('A'|'a')('S'|'s')('C'|'c');
KW_SORT_DIR_DESC  :	('D'|'d')('E'|'e')('S'|'s')('C'|'c');
KW_ORDER_BY  :	 ('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r')('B'|'b')('Y'|'y');
KW_FROM	:	('F'|'f')('R'|'r')('O'|'o')('M'|'m');
KW_WHERE :	('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');
/*
KW_AND :	 'AND';
KW_OR 	:	 'OR';
*/

//ID	:	('a'..'z'|'A'..'Z')+;

WS	:	(' '|'\t'|'\r'|'\n')+;

FIELDATA : ('A'..'Z' | 'a'..'z' | '0'..'9' | '-' | '.' | '[' | ']')+;

//('A'..'Z' | 'a'..'z' | '0'..'9' | '-' | '.' | '/' | '$' | '@' | '[' | ']')+;

outputFieldName : FIELDATA {_selectedOutputFields.add($FIELDATA.text);};

namelist :	outputFieldName (',' outputFieldName)* | '*' {_selectedOutputFields.clear();};

indexname :	FIELDATA;


notVal 	: '!' FIELDATA;

selVal 	: FIELDATA (',' FIELDATA)*;

selClause 	:  selVal | '+' '(' selVal ')';

selExpr	: FIELDATA ':' selVal (WS notVal)*;

sortExpr: FIELDATA {br.addSortField(new SortField($FIELDATA.text,false));}
	| FIELDATA WS KW_SORT_DIR_ASC{br.addSortField(new SortField($FIELDATA.text,false));} 
	| FIELDATA WS KW_SORT_DIR_DESC{br.addSortField(new SortField($FIELDATA.text,true));};

sortList:	KW_ORDER_BY WS sortExpr (',' sortExpr)*;

selecteFields returns [List<String> list]	:	{$list = _selectedOutputFields;};

select_stmt returns [BrowseRequest value]:	
		KW_SELECT 
		WS namelist 
		(WS KW_FROM WS indexname {
			System.out.println("index name: "+$indexname.text);
		})? 
		(WS KW_WHERE (WS selExpr)+)? 
		(WS sortList)? 
		';' {$value=br;};                                                                                                        
