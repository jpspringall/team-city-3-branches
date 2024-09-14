import CommonSteps.buildAndTest
import CommonSteps.createParameters
import CommonSteps.printDeployNumber
import CommonSteps.printPullRequestNumber
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

val mainHead = "+:refs/heads/main"
val releaseHead = "+:refs/heads/release"

val mainBuild = BuildType{
    val buildTypeName = "Main Build"
    name = buildTypeName
    id = RelativeId(buildTypeName.toId())

    vcs {
        root(DslContext.settingsRoot)
        branchFilter = mainHead
        cleanCheckout = true
        excludeDefaultBranchChanges = true
    }

    params {
        param("git.branch.specification", "")
    }

    createParameters()

    printPullRequestNumber()

    buildAndTest()

    triggers {
        vcs {
            branchFilter = mainHead
        }
    }

    features {}
}

val releaseBuild = BuildType{
    val buildTypeName = "Release Build"
    name = buildTypeName
    id = RelativeId(buildTypeName.toId())

    vcs {
        root(DslContext.settingsRoot)
        branchFilter = releaseHead
        cleanCheckout = true
        excludeDefaultBranchChanges = true
    }

    params {
        param("git.branch.specification", "")
    }

    createParameters()

    printPullRequestNumber()

    buildAndTest()

    triggers {
        vcs {
            branchFilter = releaseHead
        }
    }

    features {}
}

val deployMainBuild = BuildType{

    val buildTypeName = "Deploy Main Build"
    name = buildTypeName
    id = RelativeId(buildTypeName.toId())

    vcs {
        root(DslContext.settingsRoot)
        branchFilter = mainHead
        cleanCheckout = true
        excludeDefaultBranchChanges = true
    }

    buildNumberPattern = mainBuild.depParamRefs.buildNumber.toString()

    dependencies {
        snapshot(mainBuild) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    params {
        param("git.branch.specification", "")
    }

    createParameters()

    printDeployNumber()

    triggers {
        finishBuildTrigger {
            buildType = mainBuild.id.toString()
            branchFilter = mainHead
        }
    }

    features {}
}

val deployPilotBuild = BuildType{

    val buildTypeName = "Deploy Pilot Build"
    name = buildTypeName
    id = RelativeId(buildTypeName.toId())

    vcs {
        root(DslContext.settingsRoot)
        branchFilter = mainHead
        cleanCheckout = true
        excludeDefaultBranchChanges = true
    }

   buildNumberPattern = mainBuild.depParamRefs.buildNumber.toString()

    dependencies {
        snapshot(mainBuild) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    params {
        param("git.branch.specification", "")
    }

    createParameters()

    printDeployNumber()

    triggers {
    }

    features {}
}


val builds: ArrayList<BuildType> = arrayListOf()

builds.add(mainBuild)
builds.add(releaseBuild)
builds.add(deployMainBuild)
builds.add(deployPilotBuild)


val project = Project {
    // Disable editing of project and build settings from the UI to avoid issues with TeamCity
//    params {
//        param("teamcity.ui.settings.readOnly", "true")
//    }

    sequential  {
        buildType(mainBuild)
        parallel (options = {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }) { // non-default snapshot dependency options
            buildType(deployMainBuild)
            buildType(deployPilotBuild)
        }
    }

        builds.forEach{
        buildType(it)
    }

    buildTypesOrder = builds
}

project(project)