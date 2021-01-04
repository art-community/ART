package ru.art.test.specification.sql


import spock.lang.Specification

import static org.jooq.impl.DSL.using
import static ru.art.config.extensions.activator.AgileConfigurationsActivator.useAgileConfigurations
import static ru.art.sql.constants.DbProvider.DERBY_EMBEDDED
import static ru.art.sql.module.SqlModule.sqlModule

class SqlSpecification extends Specification {
    def "should connect to embedded SQL DB and make validation query"() {
        setup:
        useAgileConfigurations()

        def dsl = using(sqlModule().getJooqConfiguration("embedded"))

        when:
        def count = dsl.execute(DERBY_EMBEDDED.validationQuery)

        then:
        count == 0
    }
}