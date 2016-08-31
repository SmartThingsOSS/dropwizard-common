package smartthings.dw.buildinfo

import spock.lang.Specification

class BuildMetadataResourceSpec extends Specification {
	def 'test loading missing properties file'() {
		when:
		MetadataResponse actual = BuildMetadataResource.loadProperties('/missing.properties')

		then:
		actual.branch == null
		actual.buildNumber == null
		actual.commit == null
		actual.gitDescription == null
		actual.repo == null
		actual.tag == null
	}

	def 'test loading actual properties file'() {
		when:
		MetadataResponse actual = BuildMetadataResource.loadProperties('/test_full.properties')

		then:
		actual.branch == 'master'
		actual.buildNumber == '304'
		actual.commit == '5aa7704949d544af0da4fd1d4337870c1377725f'
		actual.gitDescription == '5aa7704 Merge pull request #1396'
		actual.repo == 'test-repo'
		actual.tag == '1.1.1'
	}

	def 'test loading partial properties file'() {
		when:
		MetadataResponse actual = BuildMetadataResource.loadProperties('/test_partial.properties')

		then:
		actual.branch == null
		actual.buildNumber == null
		actual.commit == '5aa7704949d544af0da4fd1d4337870c1377725f'
		actual.gitDescription == null
		actual.repo == null
		actual.tag == '1.1.1'
	}
}
