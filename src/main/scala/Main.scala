import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import java.io.File
import java.util.UUID

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.{Done, NotUsed}
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions}
import akka.util.ByteString
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import akka.stream.scaladsl._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future
import scala.concurrent.duration._


object Mqtt {
  val connectionSettings = MqttConnectionSettings(
    "tcp://172.30.117.236:1883",
    "test-scala-client",
    new MemoryPersistence
  )

  def source(topic: String): Source[MqttMessage, Future[Done]] =
    MqttSource.atMostOnce(
      connectionSettings.withClientId(clientId = UUID.randomUUID.toString),
      MqttSubscriptions(Map(topic -> MqttQoS.AtLeastOnce)),
      bufferSize = 8
    )
}

object KafkaProducer {
  def settings(implicit system: ActorSystem): ProducerSettings[String, String] = {
    val config = system.settings.config.getConfig("akka.kafka.producer")
      ProducerSettings(config, new StringSerializer, new StringSerializer)
        .withBootstrapServers("172.30.117.236:9092")
  }
}

object Main extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("system")

  val mqttTopic = args(0)
  val kafkaTopic = args(1)

  println(s"Starting mqtt to kafka connector ($mqttTopic -> $kafkaTopic)")

  Mqtt.source(mqttTopic)
    .map { m => m.payload.utf8String }
    .log("mqtt-message")
    .map { m => new ProducerRecord[String, String](kafkaTopic, m)}
    .runWith(Producer.plainSink(KafkaProducer.settings))
}