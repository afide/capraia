package com.github.afide.app

import com.github.afide.model.SimpleModel
import com.github.jtendermint.jabci.types.Types
import com.github.jtmsp.merkletree.byteable.ByteableLong
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link com.github.afide.app.Dummy}.
 * @author tglaeser
 */
class DummyTest extends Specification {

    @Shared Dummy app

    def setup() {
        given: 'the Socket was properly initialized'
        app = new Dummy(new SimpleModel<ByteableLong>(false) {})
    }

    def "test info"() {
        given: 'a valid request message'
        Types.RequestInfo req = Types.RequestInfo.newBuilder().build()

        when: 'the request is executed'
        Types.ResponseInfo res = app.requestInfo(req)

        then: 'we receive a valid response'
        res.data == '{"txModel":{"stop":false,"persist":false,"updated":false}}'
        res.version == '0.1'
        res.lastBlockHeight == 0
        res.lastBlockAppHash.toStringUtf8() == ''
    }
}