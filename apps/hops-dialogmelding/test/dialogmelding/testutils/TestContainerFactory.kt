package dialogmelding.testutils

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

object TestContainerFactory {
    fun ibmMq() = GenericContainer<Nothing>(
        "ibmcom/mq:9.2.3.0-r1"
    ).apply {
        withEnv("LICENSE", "accept")
        withEnv("MQ_QMGR_NAME", "QM1")
        withEnv("MQ_APP_PASSWORD", "passw0rd")
        withExposedPorts(1414)
        waitingFor(
            Wait.forLogMessage(".*Daemon started for queue manager QM1.*", 1)
        )
    }
}
