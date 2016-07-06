//Modified version from:
// Author: Bostjan Lah
// (c) Copyright, Marand, http://www.marand.com
// Licensed under LGPL: http://www.gnu.org/copyleft/lesser.html
// Based on AQL grammar by Ocean Informatics: http://www.openehr.org/wiki/download/attachments/2949295/EQL_v0.6.grm?version=1&modificationDate=1259650833000
//
// Author: Christian Chevalley - July 2016:
// modified to support ANTLR4
// removed dependencies on specific packages
//-------------------------------------------
// TODO: fix Lexer conflict on boolean operators
grammar Aql;

query	:	queryExpr ;

queryExpr : select from (where)? (orderBy)? EOF;

select
        : SELECT selectExpr
        | SELECT topExpr selectExpr ;

topExpr
        : TOP INTEGER
        | TOP INTEGER BACKWARD
        | TOP INTEGER FORWARD ;

where
        : WHERE identifiedExpr ;

orderBy 
        : ORDERBY orderBySeq ;

orderBySeq  
        : orderByExpr
		| orderByExpr COMMA orderBySeq ;

orderByExpr 
        : identifiedPath
		| identifiedPath DESCENDING
		| identifiedPath ASCENDING
		| identifiedPath DESC
		| identifiedPath ASC ;

selectExpr 
        : identifiedPathSeq;

//! When multiple paths provided, each identifiedPath must represent an object of type DataValue
identifiedPathSeq 
            : identifiedPath
			| identifiedPath AS IDENTIFIER
			| identifiedPath COMMA identifiedPathSeq
			| identifiedPath AS IDENTIFIER COMMA identifiedPathSeq ;

from
        : FROM fromExpr		// stop or/and without root class
	    | FROM fromEHR (CONTAINS containsExpression)?
        | FROM ContainsOr;

fromEHR 
        : EHR standardPredicate
        | EHR IDENTIFIER standardPredicate
        | EHR IDENTIFIER ;

fromExpr
        : containsExpression;

//====== CONTAINMENT
containsExpression
        : containExpressionBool (BooleanOperator containsExpression)?;

containExpressionBool
        : contains
        | OPEN_PARENTHESIS containsExpression CLOSE_PARENTHESIS;

contains
        : simpleClassExpr (CONTAINS containsExpression)?;


//======= END CONTAINMENT
//TODO: use Lexer definition instead of String literal
identifiedExpr
 	    : identifiedExprAnd ((' OR '|' XOR '|' or '|' xor ') identifiedExprAnd)*;

identifiedExprAnd
	    : identifiedEquality ((' AND '|' and ') identifiedExpr)*;


identifiedEquality 
        : identifiedOperand COMPARABLEOPERATOR identifiedOperand
	    | identifiedOperand MATCHES OPEN_CURLY matchesOperand CLOSE_CURLY
        | identifiedOperand MATCHES REGEXPATTERN
        | NOT identifiedEquality
        | NOT IN OPEN_PARENTHESIS queryExpr CLOSE_PARENTHESIS
        | EXISTS identifiedPath ;

identifiedOperand 
        : operand 
        | identifiedPath ;

identifiedPath 
        : IDENTIFIER
        | IDENTIFIER predicate
        | IDENTIFIER SLASH objectPath
        | IDENTIFIER predicate SLASH objectPath ;


predicate 
        : nodePredicate;

nodePredicate
        : OPEN_BRACKET nodePredicateOr CLOSE_BRACKET;

nodePredicateOr
        : nodePredicateAnd
        | nodePredicateOr OR nodePredicateAnd ;

nodePredicateAnd
        : nodePredicateComparable
        | nodePredicateAnd AND nodePredicateComparable ;

nodePredicateComparable
        : predicateOperand COMPARABLEOPERATOR predicateOperand
        | NODEID
        | NODEID COMMA STRING        // <NodeId> and name/value = <String> shortcut
        | NODEID COMMA PARAMETER     // <NodeId> and name/value = <Parameter> shortcut
        | nodePredicateRegEx     // /items[{/at0001.*/}], /items[at0001 and name/value matches {//}]
        | ARCHETYPEID
        | ARCHETYPEID COMMA STRING        // <NodeId> and name/value = <String> shortcut
        | ARCHETYPEID COMMA PARAMETER ;   // <NodeId> and name/value = <Parameter> shortcut

nodePredicateRegEx
        : REGEXPATTERN
        | predicateOperand MATCHES REGEXPATTERN ;


matchesOperand
        : valueListItems
        | URIVALUE ;

valueListItems
        : operand
        | operand COMMA valueListItems ;

versionpredicate
 	    : OPEN_BRACKET versionpredicateOptions CLOSE_BRACKET;

versionpredicateOptions
        : 'latest_version'
        | ALL_VERSIONS;

standardPredicate
        : OPEN_BRACKET predicateExpr CLOSE_BRACKET;

predicateExpr
        : predicateOr;

predicateOr
        : predicateAnd (OR predicateAnd)*;

predicateAnd
        : predicateEquality (AND predicateEquality)*;

predicateEquality
        : predicateOperand COMPARABLEOPERATOR predicateOperand;

predicateOperand
        : objectPath | operand;

operand
        : STRING
        | INTEGER
        | FLOAT
        | DATE
        | PARAMETER
        | BOOLEAN;


objectPath
        : pathPart (SLASH pathPart)*;


pathPart
        : IDENTIFIER predicate?;


BooleanOperator: (' AND '|' OR '|' XOR '|' and '|' or '|' xor ');

classExpr
 	    : OPEN_PARENTHESIS simpleClassExpr CLOSE_PARENTHESIS
	    | simpleClassExpr
	    ;

simpleClassExpr
	    : IDENTIFIER IDENTIFIER?					//! RM_TYPE_NAME .. RM_TYPE_NAME identifier
        | archetypedClassExpr
        | versionedClassExpr
	    | versionClassExpr;

archetypedClassExpr
 	    : IDENTIFIER (IDENTIFIER)? (ARCHETYPE_PREDICATE)?;	//! RM_TYPE_NAME identifier? [archetype_id]

versionedClassExpr
 	    : VERSIONED_OBJECT (IDENTIFIER)? (standardPredicate)?;

versionClassExpr
 	    : VERSION (IDENTIFIER)? (standardPredicate|versionpredicate)?;

//
// LEXER PATTERNS
//

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) -> skip
    ;

SELECT : ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t') ;
TOP : ('T'|'t')('O'|'o')('P'|'p') ;
FORWARD : ('F'|'f')('O'|'o')('R'|'r')('W'|'w')('A'|'a')('R'|'r')('D'|'d') ;
BACKWARD : ('B'|'b')('A'|'a')('C'|'c')('K'|'k')('W'|'w')('A'|'a')('R'|'r')('D'|'d') ;
AS : ('A'|'a')('S'|'s') ;
CONTAINS : ('C'|'c')('O'|'o')('N'|'n')('T'|'t')('A'|'a')('I'|'i')('N'|'n')('S'|'s') ;
WHERE : ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e') ;
ORDERBY : ('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r')(' ')('B'|'b')('Y'|'y') ;
FROM : ('F'|'f')('R'|'r')('O'|'o')('M'|'m') ;
DESCENDING : ('D'|'d')('E'|'e')('S'|'s')('C'|'c')('E'|'e')('N'|'n')('D'|'d')('I'|'i')('N'|'n')('G'|'g') ;
ASCENDING : ('A'|'a')('S'|'s')('C'|'c')('E'|'e')('N'|'n')('D'|'d')('I'|'i')('N'|'n')('G'|'g') ;
DESC : ('D'|'d')('E'|'e')('S'|'s')('C'|'c') ;
ASC : ('A'|'a')('S'|'s')('C'|'c') ;
EHR : 'EHR';
AND : 'AND'|'and' ;
OR : ('O'|'o')('R'|'r') ;
XOR : ('X'|'x')('O'|'o')('R'|'r') ;
NOT : ('N'|'n')('O'|'o')('T'|'t') ;
IN : ('I'|'i')('N'|'n');
MATCHES : ('M'|'m')('A'|'a')('T'|'t')('C'|'c')('H'|'h')('E'|'e')('S'|'s') ;
EXISTS: ('E'|'e')('X'|'x')('I'|'i')('S'|'s')('T'|'t')('S'|'s') ;
VERSION	:	'VERSION';
VERSIONED_OBJECT	:	'VERSIONED_OBJECT';
ALL_VERSIONS
	:	'all_versions';
	
fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

QUOTE	:	'\'';

fragment
DIGIT	:	'0'..'9';

fragment
HEXCHAR	:	 DIGIT|'a'|'A'|'b'|'B'|'c'|'C'|'d'|'D'|'e'|'E'|'f'|'F';

fragment
LETTER
	:	'a'..'z'|'A'..'Z';

fragment
ALPHANUM
	:	LETTER|DIGIT;

fragment
LETTERMINUSA
	:	'b'..'z'|'B'..'Z';

fragment
LETTERMINUST
	:	'a'..'s'|'A'..'S'|'u'..'z'|'U'..'Z';

fragment
IDCHAR	:	ALPHANUM|'_';

fragment
IDCHARMINUST
	:	LETTERMINUST|DIGIT|'_';

fragment
URISTRING
	:	ALPHANUM|'_'|'-'|'/'|':'|'.'|'?'|'&'|'%'|'$'|'#'|'@'|'!'|'+'|'='|'*';

fragment
REGEXCHAR
	:	URISTRING|'('|')'|'\\'|'^'|'{'|'}'|']'|'[';


// Terminal Definitions

//Boolean     = 'true' | 'false'
BOOLEAN	:	'true' | 'false' | 'TRUE' | 'FALSE' ;

NODEID	:	'at' DIGIT+ ('.' DIGIT+)*; // DIGIT DIGIT DIGIT DIGIT;

IDENTIFIER
	:	('a'|'A') (ALPHANUM|'_')*
	| 	LETTERMINUSA IDCHAR*
	;

INTEGER	:	'-'? DIGIT+;

FLOAT	:	'-'? DIGIT+ '.' DIGIT+;

DATE	:	'\'' DIGIT DIGIT DIGIT DIGIT DIGIT DIGIT DIGIT DIGIT 'T' DIGIT DIGIT DIGIT DIGIT DIGIT DIGIT '.' DIGIT DIGIT DIGIT '+' DIGIT DIGIT DIGIT DIGIT '\'';

PARAMETER :	'$' LETTER IDCHAR*;

UNIQUEID:	DIGIT+ ('.' DIGIT+)+ '.' DIGIT+  // OID
            | HEXCHAR+ ('-' HEXCHAR+)+       // UUID
	;


ARCHETYPE_PREDICATE
        : '[' ARCHETYPEID ']'
        | '[' PARAMETER ']'
        | '[' REGEXPATTERN ']' ;

ARCHETYPEID
	:	LETTER+ '-' LETTER+ '-' (LETTER|'_')+ '.' (IDCHAR|'-')+ '.v' DIGIT+ ('.' DIGIT+)?
	;

COMPARABLEOPERATOR
	:	'=' | '!=' | '>' | '>=' | '<' | '<='
	;

URIVALUE: LETTER+ '://' (URISTRING|'['|']'|', \''|'\'')*
//	| LETTER+ ':' (URISTRING|'['|']'|'\'')*
        ;

REGEXPATTERN
	:	'{/' REGEXCHAR+ '/}';

STRING
    	:  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
    	|  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    	;

SLASH	:	'/';

COMMA	:	',';

SEMICOLON : ';';

OPEN_BRACKET :	'[';
CLOSE_BRACKET :	']';
	
OPEN_PARENTHESIS	:	'(';
CLOSE_PARENTHESIS	:	')';

OPEN_CURLY :	'{';
CLOSE_CURLY :	'}';
