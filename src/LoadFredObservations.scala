/**
  * Created by Stephan on 3/3/16.
  *
  * Please see the notes.txt which contain more info.
  *
  *
  * For this exercise, we would like you to build a simple application for pulling and storing different
kinds of macroeconomic data using the Federal Reserve's FRED API. The FRED API provides a
RESTful means of accessing many datasets published by the Federal Reserve unemployment
rates, interest rates, GDP, etc. The data are modeled as "series" of "observations" with each
observation having a date and value. For more details on the API and datasets, visit
https://research.stlouisfed.org/docs/api/fred/ . The endpoint you will be most interested in is
fred/series/observations, so you may want to read the docs for that one in particular.
You'll need to create a FRED user account and API key for yourself, which you can do here:
https://research.stlouisfed.org/useraccount/register/step1 . Once you have an account, request your
API key and use it in your requests.

Requirements:
  Your application should fetch the following FRED series in their entirety
    Real Gross Domestic Product (GDPC1)
    University of Michigan Consumer Sentiment Index (UMCSENT)
    US Civilian Unemployment Rate (UNRATE)
  Your application should store the observations in a relational database running on localhost.

Delivery: a zip file containing:

A SQL script that will create your schema (we use Postgres here, but if you write ANSIstandard
SQL it shouldn't matter)

Your application source code and instructions for building/executing it
Your answer to the following question, and the SQL you wrote to answer it:
What was the average rate of unemployment for each year starting with 1980 and going up to
2015?


SUMMARY:

This program is converting JSON observations from the FRED
and putting those into a PostgreSQL database (named FREDBASE)
Please set your ENV variables for the DB connection string and the
your FRED API key.

NOTE: Any observation values seen as '.' are considered as zeros.

  **/

object LoadFredObservations extends App {

  import java.sql.DriverManager
  import scala.util.parsing.json._

  /**
    *
    *
    * Returns the text (content) from a REST URL as a String.
    * Inspired by http://matthewkwong.blogspot.com/2009/09/scala-scalaiosource-fromurl-blo...
    * and http://alvinalexander.com/blog/post/java/how-open-url-read-contents-http...
    *
    * FROM:
    * http://alvinalexander.com/scala/how-to-write-scala-http-get-request-client-source-fromurl
    *
    * The `connectTimeout` and `readTimeout` comes from the Java URLConnection
    * class Javadoc.
    * @param url The full URL to connect to.
    * @param connectTimeout Sets a specified timeout value, in milliseconds,
    * to be used when opening a communications link to the resource referenced
    * by this URLConnection. If the timeout expires before the connection can
    * be established, a java.net.SocketTimeoutException
    * is raised. A timeout of zero is interpreted as an infinite timeout.
    * Defaults to 5000 ms.
    * @param readTimeout If the timeout expires before there is data available
    * for read, a java.net.SocketTimeoutException is raised. A timeout of zero
    * is interpreted as an infinite timeout. Defaults to 5000 ms.
    * @param requestMethod Defaults to "GET". (Other methods have not been tested.)
    *
    * @example get("http://www.example.com/getInfo")
    * @example get("http://www.example.com/getInfo", 5000)
    * @example get("http://www.example.com/getInfo", 5000, 5000)
    */
  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  def get(url: String,
          connectTimeout:Int =5000,
          readTimeout:Int =5000,
          requestMethod: String = "GET") = {
    import java.net.{HttpURLConnection, URL}
    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content = io.Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }

  // Replace unknown values with 0.0 (to combat the '.' values)
  def parseDoubleOrZero(s : String) = try { s.toDouble }  catch { case _ :Exception => 0.0}

  // Setup the DB connection
  val dbConnection = DriverManager.getConnection(sys.env("SQL_CONNECTION"))
  val statement = dbConnection.createStatement()

  // These are the 3 series we're getting - These could be input parameters to the
  // code so that new observations can be added on the fly. Or ... these could be a
  // environment var
  val seriesOfFRED = List("UNRATE", "UMCSENT", "GDPC1")
  // Loops over our series
  for(series <- seriesOfFRED) {
    // For testing inner loop: val series = "UNRATE"
    println("Fetching Series: " + series)

    // get the JSON content as a string
    val content = get("https://api.stlouisfed.org/fred/series/observations?series_id=%s&api_key=%s&file_type=json".
      format(series, sys.env("FRED_API_KEY"))
    )
    // This statement can be combined with the one above but it's
    // converting the response into a parsable structure / object
    val respFromFRED = JSON.parseFull(content)
    // Let's get the observations part
    val observations = respFromFRED.get.asInstanceOf[Map[String, Any]]("observations").asInstanceOf[List[Any]]

    // loop over all the observations
    for(singleOb <- observations) {
      //  For testing inner loop: val singleOb = observations.head
      // Show status
      val insertStr = "INSERT INTO %s(datekey, realtime_start, realtime_end, value) VALUES('%s','%s','%s', %g);".
        format(
          series,
          singleOb.asInstanceOf[Map[String, Any]]("date").asInstanceOf[String],
          singleOb.asInstanceOf[Map[String, Any]]("realtime_start").asInstanceOf[String],
          singleOb.asInstanceOf[Map[String, Any]]("realtime_end").asInstanceOf[String],
          parseDoubleOrZero(singleOb.asInstanceOf[Map[String, Any]]("value").asInstanceOf[String])
        )

      println(insertStr)
      // Here we're pumping the observations into the DB
      // Note: It may be possible to do a bulk insert instead
      try {
        val statement = dbConnection.createStatement()
        statement.executeUpdate(insertStr)
      } catch {
        case e : Exception => println("Insert Error: " + e.printStackTrace())
      }
    } // foreach singleOb in
  } // for each series in seriesOfFRED
  // close the DB connection
  dbConnection.close
}