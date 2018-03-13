package com.github.afide.integration.tendermint

import com.github.jtendermint.jabci.types.Request
import com.github.jtendermint.jabci.types.RequestEcho
import com.github.jtendermint.jabci.types.RequestSetOption
import com.github.jtendermint.jabci.types.Response
import com.github.jtendermint.jabci.types.ResponseEcho
import com.github.jtendermint.jabci.types.ResponseSetOption
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import spock.lang.Specification

class TypesTest extends Specification {

    Socket clientSocket
    CodedInputStream inputStream
    CodedOutputStream outputStream

    def setup() {
        given: 'the Socket was properly initialized'
        clientSocket = new Socket("localhost", 46658)
        outputStream = CodedOutputStream.newInstance(clientSocket.getOutputStream())
        inputStream = CodedInputStream.newInstance(clientSocket.getInputStream())
        inputStream.resetSizeCounter()
    }

    def "test echo"() {
        when: 'request echo gets send'
        RequestEcho requestEcho = RequestEcho.newBuilder().setMessage('xx').build()
        Request request = Request.newBuilder().setEcho(requestEcho).build()
        sendRequest(request)

        ResponseEcho responseEcho = readResonse().getEcho()
        String result = responseEcho.message

        then: 'the response echo is equal to the request echo'
        'xx' == result
    }

    def "test set option"() {
        when: 'request set option gets send'
        RequestSetOption requestSetOption = RequestSetOption.newBuilder().setKey('serial').setValue('true').build()
        Request request = Request.newBuilder().setSetOption(requestSetOption).build()
        sendRequest(request)

        ResponseSetOption responseSetOption = readResonse().getSetOption()
        String result = responseSetOption.getLog()

        then: 'the response set option does not log any error'
        "Successfully updated field named 'serial'" == result
    }

    def "test shutdown"() {
        when: 'request set option gets send'
        RequestSetOption requestSetOption = RequestSetOption.newBuilder().setKey('stop').setValue('true').build()
        Request request = Request.newBuilder().setSetOption(requestSetOption).build()
        sendRequest(request)

        ResponseSetOption responseSetOption = readResonse().getSetOption()
        String result = responseSetOption.getLog()

        then: 'the response set option does not log any error'
        "Successfully updated field named 'stop'" == result
    }

    /**
     * Writes a {@link Request} to the socket output stream.
     * @param request
     * @throws IOException
     */
    void sendRequest(Request request) throws IOException {
        long length = request.getSerializedSize()

        if (outputStream != null) {
            // HEADER: first byte(s) is varint-uint64 encoded length of the message
            outputStream.writeUInt64NoTag(CodedOutputStream.encodeZigZag64(length))
            request.writeTo(outputStream)
            outputStream.flush()
        }
    }

    /**
     * Reads a {@link Response} from the socket input stream.
     * @return
     * @throws IOException
     */
    Response readResonse() throws IOException {
        // Size counter is used to enforce a size limit per message (see CodedInputStream.setSizeLimit()).
        // We need to reset it before reading the next message:
        inputStream.resetSizeCounter()
        // HEADER: first byte(s) is varint-uint64 encoded length of the message
        int varintLengthByte = (int) CodedInputStream.decodeZigZag64(inputStream.readUInt64())
        int oldLimit = inputStream.pushLimit(varintLengthByte)
        final Response response = Response.parseFrom(inputStream)
        inputStream.popLimit(oldLimit)
        response
    }
}