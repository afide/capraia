package com.github.afide.app

import com.github.jtendermint.jabci.types.Types
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link com.github.afide.app.Counter}.
 * @author tglaeser
 */
class CounterTest extends Specification {

    @Shared Counter app

    def setup() {
        given: 'the app was properly initialized'
        app = new Counter(true)
    }

    def "test echo"() {
        given: 'a valid request message'
        Types.RequestEcho req = Types.RequestEcho.newBuilder().setMessage('xx').build()

        when: 'the request is executed'
        Types.ResponseEcho res = app.requestEcho(req)

        then: 'we receive a valid response'
        'xx' == res.message
    }

    def "test set option"() {
        given: 'a valid request message'
        Types.RequestSetOption req = Types.RequestSetOption.newBuilder().setKey('serial').setValue('true').build()

        when: 'the request is executed'
        Types.ResponseSetOption res = app.requestSetOption(req)

        then: 'we receive a valid response'
        "Successfully updated field named 'serial'" == res.log
    }

    def "test info"() {
        given: 'a valid request message'
        Types.RequestInfo req = Types.RequestInfo.newBuilder().build()

        when: 'the request is executed'
        Types.ResponseInfo res = app.requestInfo(req)

        then: 'we receive a valid response'
        res.data == '{"txModel":{"stop":false,"serial":true,"txCount":0}}'
        res.version == '0.1'
        res.lastBlockHeight == 0
        res.lastBlockAppHash.toStringUtf8() == ''
    }
}