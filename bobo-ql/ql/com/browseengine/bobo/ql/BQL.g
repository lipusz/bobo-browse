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
package com.browseengine.bobo.ql.output;
import java.util.ArrayList;
import com.browseengine.bobo.api.BrowseRequest;
}

@lexer::header{ 
package com.browseengine.bobo.ql.output;
} 

@members{
ArrayList<String> _selectedOutputFields=new ArrayList<String>();
}

ID	:	('a'..'z'|'A'..'Z')+;

namelist :	ID (',' ID)* | '*';

indexname :	ID;

WS	:	(' ')+;

NEWLINE	:	'\r'? '\n';


sortExpr: KW_SORT_DIR_ASC | KW_SORT_DIR_DESC;

expr 	:	;

select_stmt:	KW_SELECT WS namelist WS KW_FROM WS indexname {
			System.out.println("index name: "+$indexname.text);
		}(KW_WHERE)* ';'  ;                                                                                                        

stmt	:	select_stmt;
