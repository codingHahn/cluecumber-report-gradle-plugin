package de.javaansehz

import com.chrisgahlert.gradleguiceplugin.GradleInjector
import com.trivago.cluecumber.constants.PluginSettings
import com.trivago.cluecumber.exceptions.CluecumberPluginException
import com.trivago.cluecumber.filesystem.FileIO
import com.trivago.cluecumber.filesystem.FileSystemManager
import com.trivago.cluecumber.json.JsonPojoConverter
import com.trivago.cluecumber.json.pojo.Report
import com.trivago.cluecumber.json.processors.ElementIndexPreProcessor
import com.trivago.cluecumber.logging.CluecumberLogger
import com.trivago.cluecumber.properties.PropertyManager
import com.trivago.cluecumber.rendering.ReportGenerator
import com.trivago.cluecumber.rendering.pages.pojos.pagecollections.AllScenariosPageCollection
import org.gradle.api.DefaultTask

import javax.inject.Inject
import java.nio.file.Path

class CluecumberReportTask extends DefaultTask {

    @Inject
    CluecumberLogger ccLogger
    @Inject
    PropertyManager propertyManager
    @Inject
    FileSystemManager fileSystemManager
    @Inject
    FileIO fileIO
    @Inject
    JsonPojoConverter jsonPojoConverter
    @Inject
    ElementIndexPreProcessor elementIndexPreProcessor
    @Inject
    ReportGenerator reportGenerator

    CluecumberReportTask() throws CluecumberPluginException {
        GradleInjector.inject(getProject(), this)
        doLast() {
//            if (propertyManager.skip) {
//                ccLogger.info("Cluecumber report generation was skipped using the <skip> property.",
//                        CluecumberLogger.CluecumberLogLevel.DEFAULT)
//                return
//            }
//
//            ccLogger.logInfoSeparator(CluecumberLogger.CluecumberLogLevel.DEFAULT)
//            ccLogger.info(String.format(" Cluecumber Report Maven Plugin, version %s", getClass().getPackage()
//                    .getImplementationVersion()), CluecumberLogger.CluecumberLogLevel.DEFAULT)
//            ccLogger.logInfoSeparator(CluecumberLogger.CluecumberLogLevel.DEFAULT, CluecumberLogger.CluecumberLogLevel.COMPACT)

            // Create attachment directory here since they are handled during json generation.
            fileSystemManager.createDirectory(propertyManager.getGeneratedHtmlReportDirectory() + "/attachments")

            AllScenariosPageCollection allScenariosPageCollection = new AllScenariosPageCollection(propertyManager.getCustomPageTitle())
            List<Path> jsonFilePaths = fileSystemManager.getJsonFilePaths(propertyManager.getSourceJsonReportDirectory())
            for (Path jsonFilePath : jsonFilePaths) {
                String jsonString = fileIO.readContentFromFile(jsonFilePath.toString())
                try {
                    Report[] reports = jsonPojoConverter.convertJsonToReportPojos(jsonString)
                    allScenariosPageCollection.addReports(reports)
                } catch (CluecumberPluginException e) {
                    ccLogger.warn("Could not parse JSON in file '" + jsonFilePath.toString() + "': " + e.getMessage())
                }
            }
            elementIndexPreProcessor.addScenarioIndices(allScenariosPageCollection.getReports())
            reportGenerator.generateReport(allScenariosPageCollection)
//            ccLogger.info(
//                    "=> Cluecumber Report: " + propertyManager.getGeneratedHtmlReportDirectory() + "/" +
//                            PluginSettings.SCENARIO_SUMMARY_PAGE_PATH + PluginSettings.HTML_FILE_EXTENSION,
//                    CluecumberLogger.CluecumberLogLevel.DEFAULT,
//                    CluecumberLogger.CluecumberLogLevel.COMPACT,
//                    CluecumberLogger.CluecumberLogLevel.MINIMAL
//            )
        }
    }

}
