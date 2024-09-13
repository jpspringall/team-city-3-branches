package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'MasterBuild'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("MasterBuild")) {
    vcs {

        check(branchFilter == """
            +:refs/heads/main
            +:main
        """.trimIndent()) {
            "Unexpected option value: branchFilter = $branchFilter"
        }
        branchFilter = "+:*"
    }

    triggers {
        remove {
            vcs {
                branchFilter = """
                    +:refs/heads/main
                    +:main
                """.trimIndent()
            }
        }
    }
}
