package com.github.afide.app

import com.github.jtendermint.jabci.types.RequestEcho
import com.github.jtendermint.jabci.types.RequestInfo
import com.github.jtendermint.jabci.types.RequestSetOption
import com.github.jtendermint.jabci.types.ResponseEcho
import com.github.jtendermint.jabci.types.ResponseInfo
import com.github.jtendermint.jabci.types.ResponseSetOption
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
        RequestEcho req = RequestEcho.newBuilder().setMessage('xx').build()

        when: 'the request is executed'
        ResponseEcho res = app.requestEcho(req)

        then: 'we receive a valid response'
        res.message == 'xx'
    }

    def "test set option"() {
        given: 'a valid request message'
        RequestSetOption req = RequestSetOption.newBuilder().setKey('serial').setValue('true').build()

        when: 'the request is executed'
        ResponseSetOption res = app.requestSetOption(req)

        then: 'we receive a valid response'
        res.log == "Successfully updated field named 'serial'"
    }

    def "test info"() {
        given: 'a valid request message'
        RequestInfo req = RequestInfo.newBuilder().build()

        when: 'the request is executed'
        ResponseInfo res = app.requestInfo(req)

        then: 'we receive a valid response'
        res.data == '{"txModel":{"stop":false,"serial":true,"txCount":0}}'
        res.version == '0.1'
        res.lastBlockHeight == 0
        res.lastBlockAppHash.toStringUtf8() == ''
    }
}