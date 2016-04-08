# Load Some JSON data into a PostgreSQL DB

## Foreward

The following is a coding challenge. There is also a gist that is revision of this challenge in order to use DataFrames with Spark (Scala) to get similar results. The related Gist at:

https://gist.github.com/codeaperature/b3b007da4c5817ec9fa803b973ef1c25

The Gist is a companion piece showing another way to parse JSON and load a PostgreSQL database with DataFrames. What follows here is a straight Scala implementation.



## The Exercise:

For this exercise, just build a simple application for pulling and storing different
kinds of macroeconomic data using the Federal Reserve's FRED API. The FRED API provides a
RESTful means of accessing many datasets published by the Federal Reserve unemployment
rates, interest rates, GDP, etc. The data are modeled as "series" of "observations" with each
observation having a date and value. For more details on the API and datasets, visit
https://research.stlouisfed.org/docs/api/fred/ . The endpoint you will be most interested in is
fred/series/observations, so you may want to read the docs for that one in particular.
You'll need to create a FRED user account and API key for yourself, which you can do here:
https://research.stlouisfed.org/useraccount/register/step1 . Once you have an account, request your
API key and use it in your requests.

### Requirements:
The application should fetch the following FRED series in their entirety
Real Gross Domestic Product (GDPC1)
University of Michigan Consumer Sentiment Index (UMCSENT)
US Civilian Unemployment Rate (UNRATE)
Your application should store the observations in a relational database running on localhost.

### Delivery:

1. A SQL script that will create the schema (in this case a Postgres SQL)
2. The application source code and instructions for building/executing it
3. Answer to the following question, and the SQL you wrote to answer it:
4. What was the average rate of unemployment for each year starting with 1980 and going up to
2015?


## SUMMARY:

This program converts JSON observations from the FRED
and putting those into a PostgreSQL database (named FREDBASE)
Please set your ENV variables for the DB connection string and the
your FRED API key.

NOTE: Any observation values seen as '.' are considered as zeros.



## SETUP & RUNNING, THEN MORE:
The following contains information on setting up the system and running
the query. The next sections contain small bits of information about
the program & data. The last section is a note of the follow-up questions.
(The last section is answering the questions -- just keeping some info
handy.)


## SETUP ENVIRONMENT:
In your environment, add the following:

export FRED_API_KEY=97f7xxxxxxxxxxxxxxxxxxxxxxxxxxxx

Note: This is the key you got from FRED in the step:
https://research.stlouisfed.org/useraccount/register/step1

export SQL_CONNECTION="jdbc:postgresql://localhost:5432/fredhead?user=postgres&password=mypassword"

Note: Please use your proper user/password for the user you're going to
allow access to the postgres DB fredhead DB. (Read ahead.) You can come
back to this step.


## SETUP DATABASE:

From the commandline, setup the database by with the file = setupFCDB.sql:

/Library/PostgreSQL/9.4/bin/psql -U postgres -f setupFCDB.sql

Here are the contents of that file.

DROP DATABASE IF EXISTS fredhead;
CREATE DATABASE fredhead;
\c fredhead
DROP TABLE IF EXISTS UNRATE;
DROP TABLE IF EXISTS UMCSENT;
DROP TABLE IF EXISTS GDPC1;
CREATE TABLE UNRATE(
   datekey        DATE,
   realtime_start DATE,
   realtime_end   DATE,
   value          REAL
);

CREATE TABLE UMCSENT(
   datekey        DATE,
   realtime_start DATE,
   realtime_end   DATE,
   value          REAL
 );

CREATE TABLE GDPC1(
   datekey        DATE,
   realtime_start DATE,
   realtime_end   DATE,
   value          REAL
);


Notes:
1) You may see a message like:

NOTICE: database "fredbase" does not exist, skipping

This is OK and the script is useful for debugging.

2) The "DROP TABLE IF EXISTS XXXX" may produce some 'notices' like:

DROP TABLE
psql:setupFCDB.sql:5: NOTICE:  table "XXXX" does not exist, skipping

This is OK and the script is useful for debugging.

3) The username can substituted based on your requirements.



## RUNNING THE APPLICATION:

Now Run the LoadFredObservations Program:

Depending where you have jar file and where you have your JVM:

/Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home/bin/java -Dfile.encoding=UTF-8 -jar /Users/stephan/Career/fredhead/LoadFredObservations/out/artifacts/LoadFredObservations_jar/LoadFredObservations.jar



Note: You will need Intellij to build this project. The project is setup to
be built with the build, make project menu. You can also run the program IntelliJ or
use the above command line.

You'll see the data scroll by as it's loaded into the DB.
This should look like:

Fetching Series: UNRATE
INSERT INTO UNRATE(datekey, realtime_start, realtime_end, value) VALUES('1948-01-01','2016-03-04','2016-03-04', 3.40000);
INSERT INTO UNRATE(datekey, realtime_start, realtime_end, value) VALUES('1948-02-01','2016-03-04','2016-03-04', 3.80000);
INSERT INTO UNRATE(datekey, realtime_start, realtime_end, value) VALUES('1948-03-01','2016-03-04','2016-03-04', 4.00000);
...
INSERT INTO UNRATE(datekey, realtime_start, realtime_end, value) VALUES('2016-02-01','2016-03-04','2016-03-04', 4.90000);
Fetching Series: UMCSENT
INSERT INTO UMCSENT(datekey, realtime_start, realtime_end, value) VALUES('1952-11-01','2016-03-04','2016-03-04', 0.00000);
INSERT INTO UMCSENT(datekey, realtime_start, realtime_end, value) VALUES('1953-02-01','2016-03-04','2016-03-04', 0.00000);
INSERT INTO UMCSENT(datekey, realtime_start, realtime_end, value) VALUES('1953-08-01','2016-03-04','2016-03-04', 0.00000);
...
INSERT INTO UMCSENT(datekey, realtime_start, realtime_end, value) VALUES('2015-12-01','2016-03-04','2016-03-04', 92.6000);
INSERT INTO UMCSENT(datekey, realtime_start, realtime_end, value) VALUES('2016-01-01','2016-03-04','2016-03-04', 92.0000);
Fetching Series: GDPC1
INSERT INTO GDPC1(datekey, realtime_start, realtime_end, value) VALUES('1947-01-01','2016-03-04','2016-03-04', 1934.50);
INSERT INTO GDPC1(datekey, realtime_start, realtime_end, value) VALUES('1947-04-01','2016-03-04','2016-03-04', 1932.30);
...
INSERT INTO GDPC1(datekey, realtime_start, realtime_end, value) VALUES('2015-07-01','2016-03-04','2016-03-04', 16414.0);
INSERT INTO GDPC1(datekey, realtime_start, realtime_end, value) VALUES('2015-10-01','2016-03-04','2016-03-04', 16455.1);

The program inserts data as:

INSERT INTO %s(datekey, realtime_start, realtime_end, value) VALUES('%s','%s','%s', %g);



## RUNNING THE QUERY:

Run the psql shell, which in my case is:

/Library/PostgreSQL/9.4/scripts/runpsql.sh

Use the following selections:

Server [localhost]:
Database [postgres]: fredhead
Port [5432]:
Username [postgres]:

After the program runs, go back to your psql shell and run the following:


fredbase=# SELECT extract(year from datekey) AS yyyy,  avg(value) FROM UNRATE WHERE datekey < '2016-01-01' GROUP BY yyyy ORDER BY yyyy;
  yyyy |       avg
 ------+------------------
  1948 |             3.75
  1949 | 6.04999999205271
  1950 | 5.20833337306976
  1951 |  3.2833333214124
  1952 | 3.02500001589457
  1953 | 2.92499999205271
  1954 | 5.59166665871938
  1955 | 4.36666659514109
  1956 | 4.12500003973643
  1957 |  4.2999999721845
  1958 | 6.84166665871938
  1959 | 5.44999996821086
  1960 | 5.54166662693024
  1961 | 6.69166664282481
  1962 | 5.56666664282481
  1963 | 5.64166661103566
  1964 | 5.15833334128062
  1965 | 4.50833332538605
  1966 | 3.79166664679845
  1967 | 3.84166665871938
  1968 | 3.55833337704341
  1969 | 3.49166671435038
  1970 | 4.98333334922791
  1971 | 5.95000004768372
  1972 |   5.599999944369
  1973 | 4.85833342870076
  1974 | 5.64166661103566
  1975 | 8.47500006357829
  1976 | 7.70000000794729
  1977 | 7.05000003178914
  1978 | 6.06666672229767
  1979 | 5.85000002384186
  1980 | 7.17500003178914
  1981 | 7.61666667461395
  1982 | 9.70833341280619
  1983 | 9.59999998410543
  1984 | 7.50833336512248
  1985 | 7.19166664282481
  1986 | 6.99999992052714
  1987 | 6.17499999205271
  1988 | 5.49166667461395
  1989 | 5.25833332538605
  1990 | 5.61666667461395
  1991 | 6.85000006357829
  1992 | 7.49166671435038
  1993 | 6.90833334128062
  1994 | 6.09999998410543
  1995 | 5.59166661898295
  1996 | 5.40833330154419
  1997 | 4.94166664282481
  1998 |              4.5
  1999 | 4.21666665871938
  2000 |  3.9666666785876
  2001 | 4.74166667461395
  2002 | 5.78333330154419
  2003 | 5.99166667461395
  2004 | 5.54166666666667
  2005 | 5.08333333333333
  2006 | 4.60833326975505
  2007 | 4.61666659514109
  2008 | 5.80000003178914
  2009 | 9.28333330154419
  2010 | 9.60833326975504
  2011 | 8.93333347638448
  2012 | 8.07500000794729
  2013 |            7.375
  2014 | 6.16666654745738
  2015 | 5.28333334128062
 (68 rows)

NOTE: This query is 100% related to the data as inserted, where there will only be one line item per
each month. If using realtime start and end keys, you'll need to change this query's WHERE clause to
have "and" checks that will ensure an "atDateOfInterest" value that is between these columns' values.



## INNER WORKINGS:

Here are some examples of the JSON returned with the specific Rest requests given the series id:

https://api.stlouisfed.org/fred/series/observations?series_id=UMCSENT&api_key=97f7xxxxxxxxxxxxxxxxxxxxxxxxxxxx
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","observation_start":"1776-07-04","observation_end":"9999-12-31","units":"lin","output_type":1,"file_type":"json","order_by":"observation_date","sort_order":"asc","count":549,"offset":0,"limit":100000,"observations":[{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1952-11-01","value":"."},
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1953-02-01","value":"."},
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1953-08-01","value":"."},

https://api.stlouisfed.org/fred/series/observations?series_id=GDPC1&api_key=97f7xxxxxxxxxxxxxxxxxxxxxxxxxxxx
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","observation_start":"1776-07-04","observation_end":"9999-12-31","units":"lin","output_type":1,"file_type":"json","order_by":"observation_date","sort_order":"asc","count":276,"offset":0,"limit":100000,"observations":[{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1947-01-01","value":"1934.5"},
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1947-04-01","value":"1932.3"},
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1947-07-01","value":"1930.3"},
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1947-10-01","value":"1960.7"},

https://api.stlouisfed.org/fred/series/observations?series_id=UNRATE&api_key=97f7xxxxxxxxxxxxxxxxxxxxxxxxxxxx
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","observation_start":"1776-07-04","observation_end":"9999-12-31","units":"lin","output_type":1,"file_type":"json","order_by":"observation_date","sort_order":"asc","count":817,"offset":0,"limit":100000,"observations":[{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1948-01-01","value":"3.4"},
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1948-02-01","value":"3.8"},
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1948-03-01","value":"4.0"},
{"realtime_start":"2016-03-02","realtime_end":"2016-03-02","date":"1948-04-01","value":"3.9"},


IMPORTANT NOTE: Any observations values seen as '.' are considered as zeros.


## COMMENTARY
This exercise was for a coding challenge for an entrance to an onsite interview. The company shall remain
anonymous, but there are some Scala how-to things I would like to keep around in my repo - how to get a page,
how to parse JSON, how to connect wiht JDBC to postgres ... and so forth. 


Enjoy - Stephan

