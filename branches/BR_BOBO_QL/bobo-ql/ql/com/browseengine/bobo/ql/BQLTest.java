package com.browseengine.bobo.ql;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;

import com.browseengine.bobo.ql.output.BQLLexer;
import com.browseengine.bobo.ql.output.BQLParser;

public class BQLTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		ANTLRInputStream input = new ANTLRInputStream(System.in);
		BQLLexer lexer = new BQLLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		BQLParser parser = new BQLParser(tokens);
	
		parser.select_stmt();
	}

}
