
import java.io.Serializable


import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.types._
import org.geotools.feature.DefaultFeatureCollection
import org.geotools.feature.simple.{SimpleFeatureBuilder, SimpleFeatureTypeBuilder}
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.opengis.feature.simple.SimpleFeatureType
import java.io.File
import java.util

import org.geotools.data.DefaultTransaction
import org.geotools.data.shapefile.{ShapefileDataStore, ShapefileDataStoreFactory}
import org.geotools.data.simple.SimpleFeatureStore

object ShapefileWriter {
  val logger = Logger.getLogger("ShapefileWriter")
  logger.setLevel(Level.INFO)

  private var featureType: SimpleFeatureType = null

  def writeDFtoShapefile(spatialData: DataFrame, path: String, shapeClass: Class[_]) = {
    featureType = createFeatureType(spatialData.schema, shapeClass)
    val data = buildCollection(spatialData)
    save(data, path)

  }

  private def createFeatureType(dataFrameSchema: StructType, shapeClass: Class[_]): SimpleFeatureType = {
    logger.info("Shape class provided: " + shapeClass.toString)
    logger.info("DataFrame schema:")
    dataFrameSchema.map(x => logger.info(x.toString()))

    val geometry = dataFrameSchema.filter(x => x.dataType.typeName.toLowerCase.contains("geometry"))
    if (geometry.size != 1) {
      println("Here are geometry columns found:")
      geometry.foreach(println)
      throw new Exception("There must be exactly one geometry type in the provided dataframe (found " + geometry.size + ")")
    }

    val longFieldNames = dataFrameSchema.filter(x => x.name.length > 10)
    if (longFieldNames.size > 0) {
      logger.info("Column names exceeding 10 characters found. Please rename them to meet shapefile standard. Columns are:")
      logger.info(geometry)
      throw new Exception("Column names are too long for shapefile")
    }

    val builder = new SimpleFeatureTypeBuilder()
    builder.setName("Location")
    builder.setCRS(DefaultGeographicCRS.WGS84) // <- Coordinate reference system

    dataFrameSchema.map({
      x =>
        x.dataType.typeName match {
          case "string" => builder.length(100).add(x.name, classOf[String])
          case "double" => builder.add(x.name, classOf[Double])
          case "float" => builder.add(x.name, classOf[Float])
          case "long" => builder.add(x.name, classOf[Long])
          case "integer" => builder.add(x.name, classOf[Integer])
          case "geometry" => builder.add("the_geom", shapeClass)
          case _ => throw new Exception("Unsupported datatype: " + x.dataType.typeName)
        }
    })
    builder.buildFeatureType
  }


  private def save(collection: DefaultFeatureCollection, path: String) = {

    val newFile: File = new File(path)

    val dataStoreFactory = new ShapefileDataStoreFactory

    val params: util.Map[String, Serializable] = new util.HashMap[String, Serializable]()
    params.put("url", newFile.toURI().toURL())
    params.put("create spatial index", false)

    val newDataStore = dataStoreFactory.createNewDataStore(params).asInstanceOf[ShapefileDataStore]
    newDataStore.createSchema(featureType)
    val transaction = new DefaultTransaction("create")

    val typeName = newDataStore.getTypeNames()(0)
    val featureSource = newDataStore.getFeatureSource(typeName)

    val featureStore = featureSource.asInstanceOf[SimpleFeatureStore]
    featureStore.setTransaction(transaction)
    try {
      featureStore.addFeatures(collection)
      transaction.commit()
    } catch {
      case problem: Exception =>
        problem.printStackTrace()
        transaction.rollback()
        throw problem
    } finally transaction.close()
  }


  private def buildCollection(dataFrame: DataFrame): DefaultFeatureCollection = {
    logger.info("Starting collection build at " + java.time.LocalTime.now)
    val featureBuilder = new SimpleFeatureBuilder(featureType)
    val collection: DefaultFeatureCollection = new DefaultFeatureCollection("Something", featureType)
    val data = dataFrame.collect()
    val schema = dataFrame.schema
    data.map(record => {
      schema.map(x => featureBuilder.add(record.getAs(x.name)))
      collection.add(featureBuilder.buildFeature(null))
    })
    logger.info("Finished collection build at " + java.time.LocalTime.now)
    collection
  }


}
