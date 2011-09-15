-- DDL file defining the schemas and tables to create in the test database.
-- Read and executed by net.jakubholy.testing.dbunit.DatabaseCreator#createDbSchemaFromDdl(Connection)
-- see net.jakubholy.testing.dbunit.DatabaseCreator#main.

-- Replace the text below with whatever you need.
create schema sa;

create table sa.book (
	isbn varchar(8) primary key
	, ranking int
	, author varchar(50)
	, name  varchar(50)
	, available int);