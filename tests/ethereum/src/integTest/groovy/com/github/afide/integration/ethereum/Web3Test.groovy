package com.github.afide.integration.ethereum

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import org.web3j.protocol.http.HttpService
import spock.lang.Specification

class Web3Test extends Specification {

    public "test web3 client version"() {
        given: 'a web3 Java client'
        Web3j web3 = Web3j.build(new HttpService());  // defaults to http://localhost:8545/

        when: 'we post the request'
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();

        then: 'we receive a valid response'
        clientVersion != null
        println "client version - $clientVersion"
    }
}