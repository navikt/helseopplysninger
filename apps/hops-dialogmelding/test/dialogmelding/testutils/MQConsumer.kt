package dialogmelding.testutils

import com.ibm.mq.constants.CMQC.MQENC_NATIVE
import com.ibm.msg.client.jms.JmsConstants.AUTO_ACKNOWLEDGE
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET
import com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING
import com.ibm.msg.client.jms.JmsConstants.PASSWORD
import com.ibm.msg.client.jms.JmsConstants.USERID
import com.ibm.msg.client.jms.JmsConstants.USER_AUTHENTICATION_MQCSP
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
import javax.jms.TextMessage

private const val UTF_8_WITH_PUA = 1208

class MQConsumer() {
    private val connectionFactory = createConnectionFactory()

    fun receive(): String {
        connectionFactory.createContext(AUTO_ACKNOWLEDGE).use {
            val queue = it.createQueue("DEV.QUEUE.1")
            it.createConsumer(queue).use { consumer ->
                val message = consumer.receive() as TextMessage
                return message.text
            }
        }
    }
}

private fun createConnectionFactory() =
    JmsFactoryFactory.getInstance(WMQ_PROVIDER).createConnectionFactory().apply {
        setBooleanProperty(USER_AUTHENTICATION_MQCSP, true);
        setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA)
        setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE)
        setIntProperty(WMQ_CCSID, UTF_8_WITH_PUA)
        setIntProperty(WMQ_CONNECTION_MODE, WMQ_CM_CLIENT)
        setIntProperty(WMQ_PORT, 1414)
        setStringProperty(PASSWORD, "passw0rd")
        setStringProperty(USERID, "app")
        setStringProperty(WMQ_APPLICATIONNAME, "test")
        setStringProperty(WMQ_CHANNEL, "DEV.APP.SVRCONN")
        setStringProperty(WMQ_HOST_NAME, "localhost")
        setStringProperty(WMQ_QUEUE_MANAGER, "QM1")
    }
