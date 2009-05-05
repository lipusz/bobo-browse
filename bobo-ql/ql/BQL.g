grammar BQL;

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

@header{
package com.browseengine.bobo.ql;
import java.util.ArrayList;
import com.browseengine.bobo.api.BrowseRequest;
}

@lexer::header{ 
package com.browseengine.bobo.ql;
} 

@members{
ArrayList<String> _selectedOutputFields=new ArrayList<String>();
}

ID	:	('a'..'z'|'A'..'Z')+;

NAMELIST :	ID (',' ID)* | '*';

WS	:	(' ')+;

NEWLINE	:	'\r'? '\n';


sortExpr: KW_SORT_DIR_ASC | KW_SORT_DIR_DESC;

expr 	:	;

select_stmt:	KW_SELECT WS NAMELIST WS KW_FROM WS ID {
			System.out.println("index name: "+$ID.text);
		}(KW_WHERE)* ';'  ;                                                                                                        

stmt	:	select_stmt;
