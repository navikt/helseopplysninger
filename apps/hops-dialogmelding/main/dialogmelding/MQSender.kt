package dialogmelding

import com.ibm.mq.constants.CMQC.MQENC_NATIVE
import com.ibm.msg.client.jms.JmsConstants.AUTO_ACKNOWLEDGE
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING
import com.ibm.msg.client.jms.JmsConstants.PASSWORD
import com.ibm.msg.client.jms.JmsConstants.USERID
import com.ibm.msg.client.jms.JmsFactoryFactory
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_APPLICATIONNAME
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CCSID
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CHANNEL
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CONNECTION_MODE
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_HOST_NAME
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_PORT
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_PROVIDER
import com.ibm.msg.client.wmq.common.CommonConstants.WMQ_QUEUE_MANAGER

private const val UTF_8_WITH_PUA = 1208

class MQSender(private val config: Config.MQ) {
    private val connectionFactory = createConnectionFactory(config)

    fun send(payload: String) {
        connectionFactory.createContext(AUTO_ACKNOWLEDGE).use {
            val queue = it.createQueue(config.queue)
            val message = it.createTextMessage(payload)
            it.createProducer().send(queue, message)
        }
    }
}

private fun createConnectionFactory(config: Config.MQ) =
    JmsFactoryFactory.getInstance(WMQ_PROVIDER).createConnectionFactory().apply {
        setIntProperty(WMQ_CONNECTION_MODE, WMQ_CM_CLIENT)
        setStringProperty(WMQ_QUEUE_MANAGER, config.queueManager)
        setStringProperty(WMQ_HOST_NAME, config.host)
        setStringProperty(WMQ_APPLICATIONNAME, config.applicationName)
        setIntProperty(WMQ_PORT, config.port)
        setStringProperty(WMQ_CHANNEL, config.channel)
        setIntProperty(WMQ_CCSID, UTF_8_WITH_PUA)
        setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE)
        setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA)
        setStringProperty(USERID, config.user)
        setStringProperty(PASSWORD, config.password)
    }
