package lol.kek

object StringUtils {

	private const val FEATURE_ID_PATTERN = "^mband_\\d+_[a-z0-9_]+\$"
	private const val JIRA_ID_PATTERN = "(?<=mband_)(.+?)(?=_)"
	private const val SPLIT_CHAR = "_"

	fun validateFeatureId(featureId: String): Boolean {
		return Regex(FEATURE_ID_PATTERN).matches(featureId)
	}

	fun extractJiraTaskId(featureId: String): String {
		return Regex(JIRA_ID_PATTERN).find(featureId)?.value.toString()
	}

	fun extractFeatureNameCamelCase(featureId: String): String {
		return featureId.split(SPLIT_CHAR).drop(2).joinToString("") { it.capitalize() }
	}

}