
DROP DATABASE IF EXISTS fredbase;
CREATE DATABASE fredbase;
\c fredbase
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

