package io.github.octaviusframework.spring

import io.github.octaviusframework.driver.session.OctaviusSession
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@AutoConfiguration(after = [DataSourceAutoConfiguration::class])
@ConditionalOnClass(OctaviusSession::class, DataSource::class)
open class OctaviusSpringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun octaviusTemplate(dataSource: DataSource): OctaviusTemplate {
        return OctaviusTemplate(dataSource)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun transactionManager(dataSource: DataSource): org.springframework.transaction.PlatformTransactionManager {
        println("transactionManager dataSource: " + System.identityHashCode(dataSource))
        val tm = org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource)
        tm.isNestedTransactionAllowed = true // Nativne wsparcie dla zagnieżdżonych transakcji
        return tm
    }
}
