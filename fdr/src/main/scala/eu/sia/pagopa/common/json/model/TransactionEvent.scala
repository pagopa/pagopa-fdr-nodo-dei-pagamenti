package eu.sia.pagopa.common.json.model

import scalaxbmodel.nodoperpsp.NodoInviaFlussoRendicontazione

import java.time.LocalDateTime

class Event()

case class FdREventToHistory(
                              sessionId: String,
                              nifr: NodoInviaFlussoRendicontazione,
                              soapRequest: String,
                              insertedTimestamp: LocalDateTime,
                              elaborate: Boolean,
                              retry: Integer
                            ) extends Event


