package com.github.afide.integration.tendermint

import com.github.jtendermint.jabci.types.Types
import com.google.protobuf.CodedInputStream
import spock.lang.Specification

class TypesTest extends Specification {

    Socket clientSocket
    CodedInputStream inputStream
    BufferedOutputStream outputStream

    def setup() {
        given: 'the Socket was properly initialized'
        clientSocket = new Socket("localhost", 46658)
        outputStream = new BufferedOutputStream(clientSocket.getOutputStream())
        inputStream = CodedInputStream.newInstance(clientSocket.getInputStream())
        inputStream.resetSizeCounter()
    }

    def "test echo"() {
        when: 'request echo gets send'
        Types.RequestEcho requestEcho = Types.RequestEcho.newBuilder().setMessage('xx').build()
        Types.Request request = Types.Request.newBuilder().setEcho(requestEcho).build()
        sendRequest(request)

        Types.ResponseEcho responseEcho = readResonse().getEcho()
        String result = responseEcho.message

        then: 'the response echo is equal to the request echo'
        'xx' == result
    }

    def "test set option"() {
        when: 'request set option gets send'
        Types.RequestSetOption requestSetOption = Types.RequestSetOption.newBuilder().setKey('serial').setValue('true').build()
        Types.Request request = Types.Request.newBuilder().setSetOption(requestSetOption).build()
        sendRequest(request)

        Types.ResponseSetOption responseSetOption = readResonse().getSetOption()
        String result = responseSetOption.getLog()

        then: 'the response set option does not log any error'
        "Successfully updated field named 'serial'" == result
    }

    def "test shutdown"() {
        when: 'request set option gets send'
        Types.RequestSetOption requestSetOption = Types.RequestSetOption.newBuilder().setKey('stop').setValue('true').build()
        Types.Request request = Types.Request.newBuilder().setSetOption(requestSetOption).build()
        sendRequest(request)

        Types.ResponseSetOption responseSetOption = readResonse().getSetOption()
        String result = responseSetOption.getLog()

        then: 'the response set option does not log any error'
        "Successfully updated field named 'stop'" == result
    }

    /**
     * Writes a {@link Types.Request} to the socket output stream.
     * @param request
     * @throws IOException
     */
    void sendRequest(Types.Request request) throws IOException {
        byte[] message = request.toByteArray()
        long length = message.length;
        byte[] varint = BigInteger.valueOf(length).toByteArray();
        long varintLength = varint.length;
        byte[] varintPrefix = BigInteger.valueOf(varintLength).toByteArray();

        if (outputStream != null) {
            outputStream.write(varintPrefix);
            outputStream.write(varint);
            outputStream.write(message);
            outputStream.flush();
        }
    }

    /**
     * Reads a {@link Types.Response} from the socket input stream.
     * @return
     * @throws IOException
     */
    Types.Response readResonse() throws IOException {
        byte varintLength = inputStream.readRawByte()
        byte[] messageLengthBytes = inputStream.readRawBytes(varintLength)
        byte[] messageLengthLongBytes = new byte[5];
        System.arraycopy(messageLengthBytes, 0, messageLengthLongBytes, 5 - varintLength, varintLength);
        long messageLengthLong = new BigInteger(messageLengthLongBytes).longValue();
        int messageLength = (int) messageLengthLong;
        int oldLimit = inputStream.pushLimit(messageLength)
        final Types.Response response = Types.Response.parseFrom(inputStream)
        inputStream.popLimit(oldLimit)
        response
    }
}