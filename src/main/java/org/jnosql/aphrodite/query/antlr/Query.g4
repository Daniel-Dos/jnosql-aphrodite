grammar Query;
select: 'select' fields 'from' entity where? skip? limit? order? EOF;
delete: 'delete' fields 'from' entity where? EOF;


fields: star | name (',' name)*;
star: '*';
skip: 'skip' INT;
limit: 'limit' INT;
order: 'order by' orderName (orderName)*;
orderName: name | name asc | name desc;
where: 'where' condition (and condition| or condition)* ;
condition: eq | gt | gte | lt | lte | between | in | like;
eq: not? name '=' value;
gt: not? name '>' value;
gte: not? name '>=' value;
lt: not? name '<' value;
lte: not? name '<=' value;
between: not? name 'between' value 'and' value;
in: name not? 'in' '('value (',' value)* ')';
like: name not? 'like' string;
not: 'not';
asc: 'asc';
desc: 'desc';
and: 'and';
or: 'or';
value: ( number | string | array | function | parameter | json);
name: ANY_NAME;
entity: ANY_NAME;
array: '{' element (',' element)* '}';
function: converter;
converter: 'convert(' element ',' name')';
element: number | string;
number: '-'? INT .? INT?;
string: STRING;
json: object_json | array_json;
object_json:  '{' pair_json (',' pair_json)* '}' | '{' '}';
pair_json:   STRING ':' value_json ;
value_json: STRING | NUMBER | object_json |   array_json | 'true' | 'false'| 'null';
array_json: '[' value_json (',' value_json)* ']'| '[' ']';
parameter: PARAMETER;
STRING :  '"' (ESC | ~["\\])* '"' ;
INT: [0-9]+;
ANY_NAME: [a-zA-Z_.] [a-zA-Z._0-9]*;
PARAMETER:'@'[a-zA-Z._0-9]*;
WS: [ \t\r\n]+ -> skip ;
SL_COMMENT: '//' .*? '\n' -> skip;
fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;