package no.nav.helse.hops.fhir.messages

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Resource

/** See https://www.hl7.org/fhir/messaging.html **/
abstract class BaseMessage(val bundle: Bundle) {
    init {
        require(bundle.entry != null && bundle.entry.count() > 0) { "Message cannot be empty." }
        requireEntry<MessageHeader>(0)
        requireNotNull(header.source?.endpoint) { "Message must have source.endpoint." }
        requireNotNull(header.destination?.singleOrNull()?.endpoint) { "Message must have 1 destination[].endpoint." }
    }

    val header: MessageHeader get() = resource(0)

    protected fun requireEntryCount(count: Int) =
        require(bundle.entry?.count() == count) { "Should be $count entries." }
    protected inline fun <reified T : Resource> requireEntry(index: Int) =
        require(bundle.entry[index].resource is T) { "Entry[$index] must be a ${T::class.java.simpleName}." }
    protected inline fun <reified R : Resource> resource(index: Int) =
        bundle.entry[index].resource as R
}
