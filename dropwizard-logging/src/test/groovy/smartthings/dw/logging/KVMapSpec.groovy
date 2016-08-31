package smartthings.dw.logging

import spock.lang.Specification

class KVMapSpec extends Specification {

	def 'odd number of values throws exception'() {
		when:
		KVMap.of("hello")

		then:
		def e = thrown(IllegalArgumentException)
		assert e.message.contains("saw 1")
	}

	def 'maps are build as executed'() {
		when:
		Map<String, Object> actual = input

		then:
		assert actual == expected

		where:
		input                                                     | expected
		KVMap.of("hello", "world")                                | [hello: "world"]
		KVMap.of("null test", null, "totally", "works")           | ["null test": null, totally: "works"]
		KVMap.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, null) | ['1': 2, '3': 4, '5': 6, '7': 8, '9': 10, '11':12, '13':null]
	}
}
