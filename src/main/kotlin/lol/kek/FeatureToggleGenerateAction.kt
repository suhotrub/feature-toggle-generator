package lol.kek

import com.intellij.CommonBundle
import com.intellij.ide.actions.ElementCreator
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.*
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import lol.kek.Strings.ACTION_NAME
import lol.kek.Strings.CONST_FEATURE_TEAM_CLASS
import lol.kek.Strings.ERROR_FILE
import lol.kek.Strings.ERROR_NAME
import org.jetbrains.kotlin.idea.search.projectScope
import java.util.*


class FeatureToggleGenerateAction : AnAction() {

	lateinit var elementCreator: ElementCreator

	override fun actionPerformed(e: AnActionEvent) {

		val project = e.project ?: return
		val module = LangDataKeys.MODULE.getData(e.dataContext) ?: return
		val targetFile = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
			?: return showError(ERROR_FILE, project)
		val psiManager = PsiManager.getInstance(project)
		lateinit var psiFile: PsiFile

		val psiDirectory: PsiDirectory = if (targetFile.isDirectory) {
			psiManager.findDirectory(targetFile)
		} else {
			psiFile = psiManager.findFile(targetFile) ?: return showError(ERROR_FILE, project)
			psiFile.parent
		} ?: return showError(ERROR_FILE, project)

		val teams = findFeatureTeams(project)

		elementCreator = Creator(
			this,
			psiDirectory,
			module,
			module.project,
			CommonBundle.getErrorTitle()
		)
		showDialog(psiDirectory, teams);
	}

	fun create(featureCreationInfo: FeatureCreationInfo?, directory: PsiDirectory, module: Module): PsiElement? {

		return if (featureCreationInfo?.filename?.isNotEmpty() == true) {
			val fileTemplateManager = FileTemplateManager.getDefaultInstance()
			createFile(directory, fileTemplateManager, featureCreationInfo)
		} else {
			showError(ERROR_NAME, module.project)
			null
		}
	}

	private fun createFile(
		directory: PsiDirectory,
		fileTemplateManager: FileTemplateManager,
		featureCreationInfo: FeatureCreationInfo
	): PsiElement {
		val template: FileTemplate = fileTemplateManager.getInternalTemplate(FileTemplateProvider.TEMPLATE_NAME)
		val props: Properties = fileTemplateManager.defaultProperties
		props["MBAND_TASK"] = featureCreationInfo.mbandTask
		props["FEATURE_NAME"] = featureCreationInfo.featureName
		props["FEATURE_ID"] = featureCreationInfo.featureId
		props["TEAM"] = featureCreationInfo.team

		return try {
			FileTemplateUtil.createFromTemplate(template, featureCreationInfo.filename, props, directory)
		} catch (e: Exception) {
			throw RuntimeException("Unable to create template for '" + featureCreationInfo.filename + "'", e)
		}
	}

	private fun showDialog(psiDirectory: PsiDirectory?, teams: List<String>) {
		val dialogWrapper = CreationDialogWrapper()
		val dialog = dialogWrapper.createDialog(teams)
		dialog.show()
		if (dialog.exitCode == OK_EXIT_CODE) {
			val featureCreationInfo = dialogWrapper.getFeatureCreationInfo()?.serialize()
			if (psiDirectory != null && featureCreationInfo != null) {
				elementCreator.tryCreate(featureCreationInfo)
			}
		}
	}

	fun findFeatureTeams(project: Project): List<String> {
		var teams: List<String> = listOf()
		FilenameIndex.getVirtualFilesByName(
			project,
			CONST_FEATURE_TEAM_CLASS,
			GlobalSearchScope.projectScope(project)
		).find { it ->
			it.refresh(false, false)
			val teamsContent = VfsUtil.loadText(it)
			if ("enum" in teamsContent) {
				teams = teamsContent.split("{")[1]
					.split("}")[0]
					.split(",")
					.map { word -> word.filter { char -> char.isUpperCase() || char == '_' } }
				true
			} else {
				false
			}
		}
		return teams
	}

	private fun showError(text: String, project: Project) {
		JBPopupFactory.getInstance().createMessage(text).showCenteredInCurrentWindow(project)
	}

	class Creator(
		private val featureToggleGenerateAction: FeatureToggleGenerateAction,
		private val directory: PsiDirectory,
		private val module: Module,
		project: Project,
		errorTitle: String
	) : ElementCreator(project, errorTitle) {

		override fun create(s: String): Array<PsiElement> {
			val featureCreationInfo = FeatureCreationInfo.deserialize(s)
			val element = featureToggleGenerateAction.create(featureCreationInfo, directory, module)
			return element?.let { arrayOf(it) } ?: PsiElement.EMPTY_ARRAY
		}

		override fun getActionName(s: String): String = ACTION_NAME
	}

}