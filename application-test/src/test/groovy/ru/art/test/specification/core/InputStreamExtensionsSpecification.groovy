package ru.art.test.specification.core


import spock.lang.Specification

import static ru.art.core.extension.FileExtensions.readFileBytes
import static ru.art.core.extension.InputStreamExtensions.toByteArray

class InputStreamExtensionsSpecification extends Specification {
    def "should read file with size more than buffer size"() {
        setup:
        def buffer = 512 * 1024
        def stream = "http://www.ovh.net/files/1Mio.dat".toURI().toURL().content as InputStream
        def file = new File("temp.bin")
        new FileOutputStream(file).with {
            it.write((stream.newReader().readLines().sum() as String).bytes)
            it.flush()
            it.close()
        }

        when:
        def array = readFileBytes(file.absolutePath, buffer)

        then:
        array.size() > buffer

        cleanup:
        file.delete()
    }

    def "should read input stream with size more than buffer size"() {
        setup:
        def buffer = 512 * 1024
        def stream = "http://www.ovh.net/files/1Mio.dat".toURI().toURL().content as InputStream

        when:
        def array = toByteArray(stream, buffer)

        then:
        array.size() > buffer

        cleanup:
        stream.close()
    }
}
