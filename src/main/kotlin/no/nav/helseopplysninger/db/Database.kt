package no.nav.helseopplysninger.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import java.sql.Connection


data class DatabaseConfig(
        val jdbcUrl: String,
        val password: String,
        val username: String,
        val databaseName: String,
        val maxPoolSize: Int = 2
)

class Database(val daoConfig: DatabaseConfig) : DatabaseInterface {
    var dataSource: HikariDataSource

    init {
        runFlywayMigrations(daoConfig.jdbcUrl, daoConfig.username, daoConfig.password)

        dataSource = HikariDataSource(HikariConfig().apply {
            driverClassName = org.postgresql.Driver::class.java.name
            jdbcUrl = daoConfig.jdbcUrl
            username = daoConfig.username
            password = daoConfig.password
            maximumPoolSize = daoConfig.maxPoolSize
            minimumIdle = 1
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        })

    }

    override val connection: Connection
        get() = dataSource.connection

    fun runFlywayMigrations(jdbcUrl: String, username: String, password: String) = Flyway.configure().run {
        dataSource(jdbcUrl, username, password)
        load().migrate()
    }

}

interface DatabaseInterface {
    val connection: Connection
}
