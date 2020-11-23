package lol.kek

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.dialog
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel
import lol.kek.Strings.ACTION_NAME
import lol.kek.Strings.FIELD_FEATURE_ID
import lol.kek.Strings.FIELD_FEATURE_NAME
import lol.kek.Strings.FIELD_FEATURE_TEAM
import lol.kek.Strings.FIELD_FILENAME
import lol.kek.Strings.FIELD_MBAND
import javax.swing.DefaultComboBoxModel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class CreationDialogWrapper {

	private var featureCreationInfo: FeatureCreationInfo? = null

	fun createDialog(teams: List<String>): DialogWrapper {
		val featureIdFeild = JTextField()
		val filenameField = JTextField()
		val mbandField = JTextField()
		val featureNameField = JTextField()

		println(teams)

		featureIdFeild.addListener {
			val newFeatureCreationInfo = FeatureCreationInfo(featureIdFeild.text).prefill()
			filenameField.text = newFeatureCreationInfo.filename
			mbandField.text = newFeatureCreationInfo.mbandTask
			featureNameField.text = newFeatureCreationInfo.featureName

			newFeatureCreationInfo.team = featureCreationInfo?.team ?: ""
			featureCreationInfo = newFeatureCreationInfo
		}

		filenameField.addListener { featureCreationInfo?.filename = it }
		mbandField.addListener { featureCreationInfo?.mbandTask = it }
		featureNameField.addListener { featureCreationInfo?.featureName = it }

		/*featureIdFeild.isEnabled = false
		mbandField.isEnabled = false
		featureNameField.isEnabled = false*/

		return dialog(
			title = ACTION_NAME,
			resizable = true,
			panel = panel {
				row(FIELD_FEATURE_ID) {
					featureIdFeild(growPolicy = GrowPolicy.MEDIUM_TEXT,)
					largeGapAfter()
				}
				row(FIELD_FILENAME) { filenameField() }
				row(FIELD_MBAND) { mbandField() }
				row(FIELD_FEATURE_NAME) { featureNameField() }
				row(FIELD_FEATURE_TEAM) {
					comboBox(
						DefaultComboBoxModel<String>(teams.toTypedArray()),
						{ "" },
						{ }
					).apply {
						component.addActionListener {
							featureCreationInfo = featureCreationInfo ?: FeatureCreationInfo("")
							featureCreationInfo?.team = teams[component.selectedIndex]
						}
					}
				}
			},
			focusedComponent = featureIdFeild
		)
	}

	fun getFeatureCreationInfo(): FeatureCreationInfo? {
		return featureCreationInfo
	}

	private fun JTextField.addListener(listener: (String) -> Unit) {
		document.addDocumentListener(
			object : DocumentListener {
				override fun changedUpdate(e: DocumentEvent?) {
					listener(text)
				}

				override fun insertUpdate(e: DocumentEvent?) {
					listener(text)
				}

				override fun removeUpdate(e: DocumentEvent?) {
					listener(text)
				}
			}
		)
	}
}