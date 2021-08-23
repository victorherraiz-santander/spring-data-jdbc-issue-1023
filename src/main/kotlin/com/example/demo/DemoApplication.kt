package com.example.demo

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import java.util.*

@SpringBootApplication
@EnableJdbcRepositories
class DemoApplication {
    @Bean
    fun run(repository: AggregateRepository) = ApplicationRunner {
        println(repository.customFind(setOf(Data.One, Data.Three)))
    }
}

fun main(args: Array<String>) {
    EmbeddedPostgres.builder().setPort(5555).start().also {
        val db = it.getDatabase("postgres", "postgres")
        val conn = db.connection
        val sta = conn.createStatement()
        val text = ClassPathResource("/data.sql").file.readText()
        sta.execute(text)
        sta.close()
        conn.close()
    }
    runApplication<DemoApplication>(*args)
}

enum class Data {
    One, Two, Three
}

@Table("aggregate")
data class Aggregate01(
        @Column("id") val id: UUID,
        @Column("data") val data: Data
)

interface AggregateRepository : CrudRepository<Aggregate01, UUID> {

    @Query("""SELECT * FROM aggregate WHERE data in (:dataSet)""")
    fun customFind(dataSet: Set<Data>): List<Aggregate01>
}
