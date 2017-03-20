package com.github.afide.model

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * System under specification: {@link CounterModel}.
 * @author tglaeser
 */
class CountTest extends Specification {

    @Shared String[] sources = [
            'on', 'yes', 'true',
            'off', 'no', 'false',
    ]
    @Shared boolean[] targets = [
            true, true, true,
            false, false, false,
    ]

    @Unroll def "test setting field 'serial' to '#source'"(String source, boolean target) {
        given: 'the CounterModel model was properly initialized'
        CounterModel count = new CounterModel(false)

        when: 'attempt is made to update the field via reflection'
        count.setOption( 'serial', source)

        then: 'the field value matches the expected target value'
        count.serial == target

        where: 'the source and target objects are valid'
        source << sources // Supported string values
        target << targets // Reulting boolean values
    }
}