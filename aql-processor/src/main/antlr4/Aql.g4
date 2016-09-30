//Modified version from:
// Author: Bostjan Lah
// (c) Copyright, Marand, http://www.marand.com
// Licensed under LGPL: http://www.gnu.org/copyleft/lesser.html
// Based on AQL grammar by Ocean Informatics: http://www.openehr.org/wiki/download/attachments/2949295/EQL_v0.6.grm?version=1&modificationDate=1259650833000
//
// Author: Christian Chevalley - July 2016:
// - modified to support ANTLR4
// - removed dependencies on specific packages
// - clean-up lexer conflicts and ambiguities
// - simplified grammar
//-------------------------------------------
grammar Aql;

query	:	queryExpr ;

queryExpr : select from (where)? (orderBy)? EOF ;

select
        : SELECT selectExpr
        | SELECT topExpr selectExpr ;

topExpr
        : TOP INTEGER (BACKWARD|FORWARD)?;
//        | TOP INTEGER BACKWARD
//        | TOP INTEGER FORWARD ;

function
        : FUNCTION_IDENTIFIER OPEN_PAR IDENTIFIER (COMMA IDENTIFIER)* CLOSE_PAR;

where
        : WHERE identifiedExpr ;

orderBy 
        : ORDERBY orderBySeq ;

orderBySeq  
        : orderByExpr (COMMA orderBySeq)?;
//		| orderByExpr COMMA orderBySeq ;

orderByExpr : identifiedPath (DESCENDING|ASCENDING|DESC|ASC);
//		| identifiedPath DESCENDING
//		| identifiedPath ASCENDING
//		| identifiedPath DESC
//		| identifiedPath ASC ;

//selectExpr
//        : variableSeq;

selectExpr
        : identifiedPath (AS IDENTIFIER)? (COMMA selectExpr)?
        | function (AS IDENTIFIER)? (COMMA selectExpr)?
        ;

//variableSeq_
//        : identifiedPath (AS IDENTIFIER)? (COMMA variableSeq)?
//        | function (AS IDENTIFIER)? (COMMA variableSeq)?
//        ;

from
        : FROM fromExpr		// stop or/and without root class
	    | FROM fromEHR (CONTAINS containsExpression)?;
//        | FROM ContainsOr;

fromEHR 
        : EHR standardPredicate
        | EHR IDENTIFIER standardPredicate
        | EHR IDENTIFIER ;

//====== CONTAINMENT
fromExpr
        : containsExpression;


containsExpression
        : containExpressionBool ((AND|OR|XOR) containsExpression)?;

containExpressionBool
        : contains
        | OPEN_PAR containsExpression CLOSE_PAR;

contains
        : simpleClassExpr (CONTAINS containsExpression)?;
//======= END CONTAINMENT

identifiedExpr
 	    : identifiedEquality ((OR|XOR|AND) identifiedEquality)*
 	    | OPEN_PAR identifiedEquality ((OR|XOR|AND) identifiedEquality)* CLOSE_PAR
 	    ;

//identifiedExprAnd
//	    : identifiedEquality (AND identifiedEquality)*;
//	    : identifiedEquality (AND identifiedExpr)*;

//TODO: the NOT token is not correctly interpreted (greedy match issue)
identifiedEquality 
        : NOT? identifiedOperand COMPARABLEOPERATOR identifiedOperand
	    | NOT? identifiedOperand MATCHES OPEN_CURLY matchesOperand CLOSE_CURLY
        | NOT? identifiedOperand MATCHES REGEXPATTERN
//        | NOT identifiedEquality
        | NOT? IN OPEN_PAR queryExpr CLOSE_PAR
        | NOT? EXISTS identifiedPath
        | NOT? EXISTS identifiedExpr;

identifiedOperand 
        : operand 
        | identifiedPath ;

identifiedPath
        : IDENTIFIER (SLASH objectPath)?
        | IDENTIFIER predicate (SLASH objectPath)?;
//        | IDENTIFIER SLASH objectPath
//        | IDENTIFIER predicate SLASH objectPath ;


predicate : OPEN_BRACKET nodePredicateOr CLOSE_BRACKET;

//nodePredicate_
//        : OPEN_BRACKET nodePredicateOr CLOSE_BRACKET;

nodePredicateOr
        : nodePredicateAnd (OR nodePredicateAnd)*;
//        | nodePredicateOr (OR nodePredicateAnd)* ;

nodePredicateAnd
        : nodePredicateComparable (AND nodePredicateComparable)*;
//        | nodePredicateAnd AND nodePredicateComparable ;

nodePredicateComparable
 	: NODEID (COMMA (STRING|PARAMETER))?
 	| ARCHETYPEID (COMMA (STRING|PARAMETER))?
 	| predicateOperand ((COMPARABLEOPERATOR predicateOperand)|(MATCHES REGEXPATTERN))
    | REGEXPATTERN     //! /items[{/at0001.*/}], /items[at0001 and name/value matches {//}]
    ;

nodePredicateRegEx
        : REGEXPATTERN
        | predicateOperand MATCHES REGEXPATTERN ;


matchesOperand
        : valueListItems
        | URIVALUE ;

valueListItems
        : operand (COMMA valueListItems)?;
//        | operand COMMA valueListItems ;

versionpredicate
 	    : OPEN_BRACKET versionpredicateOptions CLOSE_BRACKET;

versionpredicateOptions
        : LATEST_VERSION
        | ALL_VERSIONS;

standardPredicate
        : OPEN_BRACKET predicateExpr CLOSE_BRACKET;

predicateExpr
        : predicateAnd (OR predicateAnd)*;

//predicateOr_
//        : predicateAnd (OR predicateAnd)*;

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

classExpr
 	    : OPEN_PAR simpleClassExpr CLOSE_PAR
	    | simpleClassExpr
	    ;

simpleClassExpr
	    : IDENTIFIER IDENTIFIER?					//! RM_TYPE_NAME .. RM_TYPE_NAME identifier
        | archetypedClassExpr
        | versionedClassExpr
	    | versionClassExpr;

archetypedClassExpr
 	    : IDENTIFIER (IDENTIFIER)? (OPEN_BRACKET ARCHETYPEID CLOSE_BRACKET)?;	//! RM_TYPE_NAME identifier? [archetype_id]

versionedClassExpr
 	    : VERSIONED_OBJECT (IDENTIFIER)? (standardPredicate)?;

versionClassExpr
 	    : VERSION (IDENTIFIER)? (standardPredicate|versionpredicate)?;

//
// LEXER PATTERNS
//

EHR : E H R;
AND :  A N D ;
OR : O R ;
XOR : X O R ;
NOT : N O T ;
IN : I N ;
MATCHES : M A T C H E S ;
SELECT : S E L E C T ;
TOP : T O P ;
FORWARD : F O R W A R D ;
BACKWARD : B A C K W A R D ;
AS : A S ;
CONTAINS : C O N T A I N S ;
WHERE : W H E R E ;
ORDERBY : O R D E R B Y ;
FROM : F R O M ;
DESCENDING : D E S C E N D I N G ;
ASCENDING : A S C E N D I N G ;
DESC : D E S C ;
ASC : A S C ;
EXISTS: E X I S T S ;
VERSION	:	V E R S I O N ;
VERSIONED_OBJECT	:	V E R S I O N E D '_' O B J E C T;
ALL_VERSIONS :	A L L '_' V E R S I O N S;
LATEST_VERSION : L A T E S T '_' V E R S I O N ;

FUNCTION_IDENTIFIER : COUNT | AVG | BOOL_AND | BOOL_OR | EVERY | MAX | MIN | SUM;

// Terminal Definitions
BOOLEAN	:	(T R U E)|(F A L S E) ;
NODEID	:	'at' DIGIT+ ('.' DIGIT+)*;
ARCHETYPEID :	LETTER+ '-' LETTER+ '-' (LETTER|'_')+ '.' (IDCHAR|'-')+ '.v' DIGIT+ ('.' DIGIT+)?;

IDENTIFIER
	:	A (ALPHANUM|'_')*
	| 	LETTERMINUSA IDCHAR*
	;

INTEGER	:	'-'? DIGIT+;
FLOAT	:	'-'? DIGIT+ '.' DIGIT+;
DATE	:	'\'' DIGIT DIGIT DIGIT DIGIT DIGIT DIGIT DIGIT DIGIT 'T' DIGIT DIGIT DIGIT DIGIT DIGIT DIGIT '.' DIGIT DIGIT DIGIT '+' DIGIT DIGIT DIGIT DIGIT '\'';
PARAMETER :	'$' LETTER IDCHAR*;

UNIQUEID:	DIGIT+ ('.' DIGIT+)+ '.' DIGIT+  // OID
            | HEXCHAR+ ('-' HEXCHAR+)+       // UUID
	;

COMPARABLEOPERATOR
	:	'=' | '!=' | '>' | '>=' | '<' | '<='
	;

URIVALUE: LETTER+ '://' (URISTRING|OPEN_BRACKET|CLOSE_BRACKET|', \''|'\'')*;

REGEXPATTERN : '{/' REGEXCHAR+ '/}';

STRING
    	:  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
    	|  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    	;

SLASH	:	'/';
COMMA	:	',';
SEMICOLON : ';';
OPEN_BRACKET :	'[';
CLOSE_BRACKET :	']';
OPEN_PAR	:	'(';
CLOSE_PAR	:	')';
OPEN_CURLY :	'{';
CLOSE_CURLY :	'}';

COUNT: C O U N T;
AVG: A V G;
BOOL_AND: B O O L '_' A N D;
BOOL_OR: B O O L '_' O R;
EVERY: E V E R Y;
MAX: M A X;
MIN: M I N;
SUM: S U M;


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

fragment
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

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];


WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) -> skip
    ;