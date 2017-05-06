package com.github.afide.integration.endpoint

import com.github.afide.integration.extension.Node
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class TendermintTest extends Specification {

    @Shared @Node protected ConfigObject config

    @Shared String[] methods = [
            'broadcast_tx_commit',
            'broadcast_tx_sync',
            'broadcast_tx_async',
    ]

    public "test 'abci_info'"() {
        given: 'a REST client'
        RESTClient client = new RESTClient( "http://${config.node.host.ip}:${config.node.host.port}")

        when: 'we make a get request'
        HttpResponseDecorator resp = client.get(path : '/abci_info', contentType: ContentType.JSON.toString()) as HttpResponseDecorator

        then: 'we receive a valid response'
        resp.success
        resp.status == 200
        resp.contentType == ContentType.JSON.toString()
        println "response payload - $resp.data"
        resp.data.error == ''
        def result = resp.data.result.pop()
        resp.data.result == [113]
        result.response.version == '0.1'
        result.response.last_block_height > 0
        result.response.data != null
    }

    public "test 'status'"() {
        given: 'a REST client'
        RESTClient client = new RESTClient( "http://${config.node.host.ip}:${config.node.host.port}")

        when: 'we make a get request'
        HttpResponseDecorator resp = client.get(path : '/status', contentType: ContentType.JSON.toString()) as HttpResponseDecorator

        then: 'we receive a valid response'
        resp.success
        resp.status == 200
        resp.contentType == ContentType.JSON.toString()
        println "response payload - $resp.data"
        resp.data.error == ''
        def result = resp.data.result.pop()
        resp.data.result == [32]
        result.latest_block_hash != null
        result.latest_block_height > 0
    }

    @Unroll public "test '#method'"(String method) {
        given: 'a REST client'
        RESTClient client = new RESTClient( "http://${config.node.host.ip}:${config.node.host.port}")

        and: 'a valid JSON-RPC request'
        def requestParams = [
                'jsonrpc':'2.0',
                'id':'',
                'method':method,
                'params': ['0000000000000003']
        ]

        when: 'we post the request'
        HttpResponseDecorator resp = client.post(path : '/', contentType: ContentType.JSON.toString(), body : requestParams) as HttpResponseDecorator

        then: 'we receive a valid response'
        resp.success
        resp.status == 200
        resp.contentType == ContentType.JSON.toString()
        println "response payload - $resp.data"
        resp.data.error == ''

        where: 'we a are using valid methods'
        method << methods
    }
}