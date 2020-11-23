package lol.kek

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.intellij.openapi.fileTypes.StdFileTypes
import org.jetbrains.annotations.NonNls


class FileTemplateProvider : FileTemplateGroupDescriptorFactory {

    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor? {

        val group = FileTemplateGroupDescriptor(FEATURE_TOGGLE, StdFileTypes.JAVA.icon)
        group.addTemplate(FileTemplateDescriptor(TEMPLATE_NAME, StdFileTypes.JAVA.icon))
        return group
    }

    companion object {
        private const val FEATURE_TOGGLE = "Feature Toggle"
        const val TEMPLATE_NAME = "FeatureToggle.kt"
    }
}