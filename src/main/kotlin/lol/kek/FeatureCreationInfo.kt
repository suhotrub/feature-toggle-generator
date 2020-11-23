package lol.kek

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

data class FeatureCreationInfo(
	var featureId: String,
	var filename: String = "",
	var mbandTask: String = "",
	var featureName: String = "",
	var team: String = ""
) {
	fun prefill(): FeatureCreationInfo {
		if (StringUtils.validateFeatureId(featureId)) {
			mbandTask = StringUtils.extractJiraTaskId(featureId)
			featureName = StringUtils.extractFeatureNameCamelCase(featureId)
			filename = featureName + mbandTask
		}
		return this
	}

	fun serialize(): String {
		return listOf(featureId, filename, mbandTask, featureName, team).joinToString(SEPARATOR)
	}

	companion object {
		private const val SEPARATOR = "@#"

		fun deserialize(string: String): FeatureCreationInfo? {
			val iter = string.split(SEPARATOR).iterator()
			return FeatureCreationInfo(
				iter.next(),
				iter.next(),
				iter.next(),
				iter.next(),
				iter.next()
			)
		}
	}
}