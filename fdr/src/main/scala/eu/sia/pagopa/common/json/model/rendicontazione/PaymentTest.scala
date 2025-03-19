package eu.sia.pagopa.common.json.model.rendicontazione

case class PaymentTest(
                    flowId: Integer,
                    iuv: String,
                    iur: String,
                    index: Integer,
                    amount: Integer,
                    payDate: String,
                    payStatus: PayStatusEnum.Value,
                    transferId: Integer,
                    created: String,
                    updated: String
)

//object ConvertFdrResponse extends DefaultJsonProtocol {
//
//  implicit val format: RootJsonFormat[ConvertFdrResponse] = new RootJsonFormat[ConvertFdrResponse] {
//    def write(res: ConvertFdrResponse): JsObject = {
//      var fields: Map[String, JsValue] =
//        Map("outcome" -> JsString(res.outcome))
//      if (res.description.isDefined) {
//        fields = fields + ("description" -> JsString(res.description.get))
//      }
//      JsObject(fields)
//    }
//
//    def read(json: JsValue): NotifyFdrResponse = ???
//  }
//}
//
//case class NotifyFdrResponse(outcome: String, description: Option[String])