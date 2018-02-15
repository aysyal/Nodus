/*
 * NodusSQLTokenMaker.java - Scanner for SQL.
 *
 */
package edu.uclouvain.core.nodus.database.gui;

import java.io.*;
import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.*;


/**
 * This class generates tokens representing a text stream as SQL.<p>
 *
 * This implementation was created using
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.1; however, the generated file
 * was modified for performance.  Memory allocation needs to be almost
 * completely removed to be competitive with the handwritten lexers (subclasses
 * of <code>AbstractTokenMaker</code>, so this class has been modified so that
 * Strings are never allocated (via yytext()), and the scanner never has to
 * worry about refilling its buffer (needlessly copying chars around).
 * We can achieve this because RText always scans exactly 1 line of tokens at a
 * time, and hands the scanner this line as an array of characters (a Segment
 * really).  Since tokens contain pointers to char arrays instead of Strings
 * holding their contents, there is no need for allocating new memory for
 * Strings.<p>
 *
 * The actual algorithm generated for scanning has, of course, not been
 * modified.<p>
 *
 * If you wish to regenerate this file yourself, keep in mind the following:
 * <ul>
 *   <li>The generated <code>NodusSQLTokenMaker.java</code> file will contain two
 *       definitions of both <code>zzRefill</code> and <code>yyreset</code>.
 *       You should hand-delete the second of each definition (the ones
 *       generated by the lexer), as these generated methods modify the input
 *       buffer, which we'll never have to do.</li>
 *   <li>You should also change the declaration/definition of zzBuffer to NOT
 *       be initialized.  This is a needless memory allocation for us since we
 *       will be pointing the array somewhere else anyway.</li>
 *   <li>You should NOT call <code>yylex()</code> on the generated scanner
 *       directly; rather, you should use <code>getTokenList</code> as you would
 *       with any other <code>TokenMaker</code> instance.</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.5
 *
 */
%%

%public
%class NodusSQLTokenMaker
%extends AbstractJFlexTokenMaker
%unicode
%ignorecase
%type org.fife.ui.rsyntaxtextarea.Token


%{


	/**
	 * Constructor.  This must be here because JFlex does not generate a
	 * no-parameter constructor.
	 */
	public NodusSQLTokenMaker() {
		super();
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param array The character array.
	 * @param start The starting offset in the array.
	 * @param end The ending offset in the array.
	 * @param tokenType The token's type.
	 * @param startOffset The offset in the document at which this token
	 *                    occurs.
	 */
	@Override
	public void addToken(char[] array, int start, int end, int tokenType, int startOffset) {
		super.addToken(array, start,end, tokenType, startOffset);
		zzStartRead = zzMarkedPos;
	}


	/**
	 * Overridden to return <code>true</code> so paren matching occurs for
	 * SQL.
	 *
	 * @return <code>true</code> always.
	 */
	@Override
	public boolean getCurlyBracesDenoteCodeBlocks(int languageIndex) {
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getLineCommentStartAndEnd(int languageIndex) {
		return new String[] { "--", null };
	}


	/**
	 * Returns the first token in the linked list of tokens generated
	 * from <code>text</code>.  This method must be implemented by
	 * subclasses so they can correctly implement syntax highlighting.
	 *
	 * @param text The text from which to get tokens.
	 * @param initialTokenType The token type we should start with.
	 * @param startOffset The offset into the document at which
	 *        <code>text</code> starts.
	 * @return The first <code>Token</code> in a linked list representing
	 *         the syntax highlighted text.
	 */
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

		resetTokenList();
		this.offsetShift = -text.offset + startOffset;

		// Start off in the proper state.
		int state = Token.NULL;
		switch (initialTokenType) {
			case Token.LITERAL_STRING_DOUBLE_QUOTE:
				state = STRING;
				start = text.offset;
				break;
			case Token.LITERAL_CHAR:
				state = CHAR;
				start = text.offset;
				break;
			case Token.COMMENT_MULTILINE:
				state = MLC;
				start = text.offset;
				break;
			default:
				state = Token.NULL;
		}

		s = text;
		try {
			yyreset(zzReader);
			yybegin(state);
			return yylex();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return new TokenImpl();
		}

	}


	/**
	 * Refills the input buffer.
	 *
	 * @return      <code>true</code> if EOF was reached, otherwise
	 *              <code>false</code>.
	 */
	private boolean zzRefill() {
		return zzCurrentPos>=s.offset+s.count;
	}


	/**
	 * Resets the scanner to read from a new input stream.
	 * Does not close the old reader.
	 *
	 * All internal variables are reset, the old input stream 
	 * <b>cannot</b> be reused (internal buffer is discarded and lost).
	 * Lexical state is set to <tt>YY_INITIAL</tt>.
	 *
	 * @param reader   the new input stream 
	 */
	public final void yyreset(java.io.Reader reader) {
		// 's' has been updated.
		zzBuffer = s.array;
		/*
		 * We replaced the line below with the two below it because zzRefill
		 * no longer "refills" the buffer (since the way we do it, it's always
		 * "full" the first time through, since it points to the segment's
		 * array).  So, we assign zzEndRead here.
		 */
		//zzStartRead = zzEndRead = s.offset;
		zzStartRead = s.offset;
		zzEndRead = zzStartRead + s.count - 1;
		zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
		zzLexicalState = YYINITIAL;
		zzReader = reader;
		zzAtBOL  = true;
		zzAtEOF  = false;
	}


%}

LineTerminator		= ([\n])
Letter			= ([A-Za-z])
Digit			= ([0-9])
Whitespace		= ([ \t]+)

IdentifierStart	= ({Letter})
IdentifierPart		= ({IdentifierStart}|{Digit}|[_])
Identifier		= ({IdentifierStart}{IdentifierPart}*)

Operator			= (">="|"<="|"<>"|">"|"<"|"="|"+"|"-"|"*"|"/")
Separator			= ([\(\)])

Parameter			= ([:]{Identifier})

Integer			= ({Digit}+)
Float			= (({Digit}+[.]{Digit}*)|([.]{Digit}*))
ApproxNum			= (({Digit}+[eE][+-]?{Digit}+)|({Digit}+[.]{Digit}*[eE][+-]?[0-9]+)|([.][0-9]*[eE][+-]?[0-9]+))

CommentBegin		= ("--")
Comment			= ({CommentBegin}.*)
MLCBegin			= "/*"
MLCEnd			= "*/"

%state STRING
%state CHAR
%state MLC

%%

<YYINITIAL> {

	/* Keywords */
	"ADD" |
	"ALL" |
	"ALTER" |
	"AND" |
	"ANY" |
	"AS" |
	"ASC" |
	"AUTOINCREMENT" |
	"AVA" |
	"BETWEEN" |
	"BINARY" |
	"BIT" |
	"BOOLEAN" |
	"BY" |
	"BYTE" |
	"CHAR" |
	"CHARACTER" |
	"COLUMN" |
	"CONSTRAINT" |
	"COUNT" |
	"COUNTER" |
	"CREATE" |
	"CURRENCY" |
	"DATABASE" |
	"DATE" |
	"DATETIME" |
	"DELETE" |
	"DESC" |
	"DISALLOW" |
	"DISTINCT" |
	"DISTINCTROW" |
	"DOUBLE" |
	"DROP" |
	"EXISTS" |
	"FLOAT" |
	"FLOAT4" |
	"FLOAT8" |
	"FOREIGN" |
	"FROM" |
	"GENERAL" |
	"GROUP" |
	"GUID" |
	"HAVING" |
	"INNER" |
	"INSERT" |
	"IGNORE" |
	"IMP" |
	"IN" |
	"INDEX" |
	"INT" |
	"INTEGER" |
	"INTEGER1" |
	"INTEGER2" |
	"INTEGER4" |
	"INTO" |
	"IS" |
	"JOIN" |
	"KEY" |
	"LEFT" |
	"LEVEL" |
	"LIKE" |
	"LOGICAL" |
	"LONG" |
	"LONGBINARY" |
	"LONGTEXT" |
	"MAX" |
	"MEMO" |
	"MIN" |
	"MOD" |
	"MONEY" |
	"NOT" |
	"NULL" |
	"NUMBER" |
	"NUMERIC" |
	"OLEOBJECT" |
	"ON" |
	"OPTION" |
	"OR" |
	"ORDER" |
	"OUTER" |
	"OWNERACCESS" |
	"PARAMETERS" |
	"PASSWORD" |
	"PERCENT" |
	"PIVOT" |
	"PRIMARY" |
	"REAL" |
	"REFERENCES" |
	"RIGHT" |
	"SELECT" |
	"SET" |
	"SHORT" |
	"SINGLE" |
	"SMALLINT" |
	"SOME" |
	"STDEV" |
	"STDEVP" |
	"STRING" |
	"SUM" |
	"TABLE" |
	"TABLEID" |
	"TEXT" |
	"TIME" |
	"TIMESTAMP" |
	"TOP" |
	"TRANSFORM" |
	"TYPE" |
	"UNION" |
	"UNIQUE" |
	"UPDATE" |
	"USER" |
	"VALUE" |
	"VALUES" |
	"VAR" |
	"VARBINARY" |
	"VARCHAR" |
	"VARP" |
	"WHERE" |
	"WITH" |
	"IMPORTDBF" |
	"EXPORTDBF" |
	"IMPORTCSV" |
	"IMPORTCSVH" |
	"EXPORTCSV" |
	"EXPORTCSVH" |
	"EXTRACTSHP" |
	"IMPORTXLS" |
	"EXPORTXLS" |
	"IMPORTXLSX" |
	"EXPORTXLSX" |
	"ENABLEHEADERS" |
	"DISABLEHEADERS" |
	"ENABLEECHO" |
	"DISABLEECHO" |
	"CLRSCR" |
	"STOP" |
	"DISPLAYGRID" |
	"DISPLAYTEXT" |
	"CLEARSCENARIO" |
	"YESNO"					{ addToken(Token.RESERVED_WORD); }

	/* SQL99 aggregate functions */
	"AVG" |
	"COUNT" |
	"MIN" |
	"MAX" |
	"SUM"					{ addToken(Token.FUNCTION); }

	/* SQL99 built-in scalar functions */
	"CURRENT_DATE" |
	"CURRENT_TIME" |
	"CURRENT_TIMESTAMP" |
	"CURRENT_USER" |
	"SESSION_USER" |
	"SYSTEM_USER"			{ addToken(Token.FUNCTION); }

	/* SQL99 numeric scalar functions */
	"BIT_LENGTH" |
	"CHAR_LENGTH" |
	"EXTRACT" |
	"OCTET_LENGTH" |
	"POSITION"				{ addToken(Token.FUNCTION); }

	/* SQL99 string functions */
	"CONCATENATE" |
	"CONVERT" |
	"LOWER" |
	"SUBSTRING" |
	"TRANSLATE" |
	"TRIM" |
	"UPPER"					{ addToken(Token.FUNCTION); }

	{LineTerminator}				{ addNullToken(); return firstToken; }

	{Identifier}					{ addToken(Token.IDENTIFIER); }
	";"							{ addToken(Token.IDENTIFIER); }

	{Parameter}					{ addToken(Token.IDENTIFIER); }

	{Comment}						{ addToken(Token.COMMENT_EOL); }
	{MLCBegin}					{ start = zzMarkedPos-2; yybegin(MLC); }

	{Whitespace}					{ addToken(Token.WHITESPACE); }

	{Operator}					{ addToken(Token.OPERATOR); }
	{Separator}					{ addToken(Token.SEPARATOR); }

	{Integer}						{ addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
	{Float}						{ addToken(Token.LITERAL_NUMBER_FLOAT); }
	{ApproxNum}					{ addToken(Token.LITERAL_NUMBER_FLOAT); }

	"\""							{ start = zzMarkedPos-1; yybegin(STRING); }
	"\'"							{ start = zzMarkedPos-1; yybegin(CHAR); }

	"["[^\]]*"]"					{ addToken(Token.PREPROCESSOR); }
	"["[^\]]*						{ addToken(Token.ERROR_IDENTIFIER); addNullToken(); return firstToken; }

	<<EOF>>						{ addNullToken(); return firstToken; }

	/* Catch any other (unhandled) characters and flag them as OK; */
	/* I don't know enough about SQL to know what's really invalid. */
	.							{ addToken(Token.IDENTIFIER); }

}

<STRING> {

	[^\n\"]+				{}
	\n					{ addToken(start,zzStartRead-1, Token.LITERAL_STRING_DOUBLE_QUOTE); return firstToken; }
	"\"\""				{}
	"\""					{ yybegin(YYINITIAL); addToken(start,zzStartRead, Token.LITERAL_STRING_DOUBLE_QUOTE); }
	<<EOF>>				{ addToken(start,zzStartRead-1, Token.LITERAL_STRING_DOUBLE_QUOTE); return firstToken; }

}

<CHAR> {

	[^\n\']+				{}
	\n					{ addToken(start,zzStartRead-1, Token.LITERAL_CHAR); return firstToken; }
	"\'\'"				{}
	"\'"					{ yybegin(YYINITIAL); addToken(start,zzStartRead, Token.LITERAL_CHAR); }
	<<EOF>>				{ addToken(start,zzStartRead-1, Token.LITERAL_CHAR); return firstToken; }

}

<MLC> {

	[^\n\*]+				{}
	\n					{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }
	{MLCEnd}				{ yybegin(YYINITIAL); addToken(start,zzStartRead+1, Token.COMMENT_MULTILINE); }
	\*					{}
	<<EOF>>				{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }

}
