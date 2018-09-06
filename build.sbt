name := "GeosparkShapeWriter"

version := "0.1"

scalaVersion := "2.11.12"


val sparkVersion = "2.1.0"


resolvers ++= Seq( "ClouderaRepo" at "https://repository.cloudera.com/artifactory/cloudera-repos/",

  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Boundless repo" at "http://repo.boundlessgeo.com/main/",
  "Boundless repo 2" at "https://boundless.artifactoryonline.com/boundless/main",
  "Another repo" at "http://download.osgeo.org/webdav/geotools/"
)


libraryDependencies ++= Seq(

  "org.datasyslab" % "geospark" % "1.1.3",
  "org.datasyslab" % "geospark-sql_2.1" % "1.1.3",
  "org.apache.spark" % "spark-sql_2.11" % "2.1.0.cloudera1" % "provided",
  "org.apache.spark" % "spark-core_2.11" % "2.1.0.cloudera1" % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",

  // hadoop
  "org.apache.hadoop" % "hadoop-common" % "2.6.0-cdh5.11.1" % "provided",
  "org.apache.commons" % "commons-io" % "1.3.2" % "provided",
  "org.apache.hadoop" % "hadoop-hdfs" % "2.6.0-cdh5.11.1" % "provided",
  // https://mvnrepository.com/artifact/org.geotools/gt-shapefile
  "org.geotools" % "gt-main" % "19.1",
  "org.geotools" % "gt-shapefile" % "19.1",
  // https://mvnrepository.com/artifact/org.geotools/gt-epsg-hsql
  "org.geotools" % "gt-epsg-hsql" % "19.1"

)
