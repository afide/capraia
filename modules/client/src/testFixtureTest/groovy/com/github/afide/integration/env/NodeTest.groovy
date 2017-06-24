package com.github.afide.integration.env

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class NodeTest extends Specification {

    @com.github.afide.integration.extension.Node ConfigObject defaultConfig

    @Shared String[][] configs = [
            ['one', 'two', 'three', 'forty-two'],
            ['capraiauno', 'capraiadue', 'capraiatre', 'default'],
    ]

    def "test default configuration"() {
        expect: 'the default configuration'
        defaultConfig.node.name == 'default'
        defaultConfig.node.host.ip == '127.0.0.1'
        defaultConfig.node.host.port == 46657
    }

    @Unroll def "test node '#node' configuration"(String node, String name) {
        when: 'a new config object is created'
        ConfigObject config = new ConfigSlurper(node).parse(Node.class)

        then: 'the config objects name has the expected value'
        config.node.name == name

        where: 'the node property has any arbitary value'
        node << configs[0]
        name << configs[1]
    }
}