#!groovy

/* Only keep the 10 most recent builds. */
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '10']]])

def branch = env.BRANCH_NAME
def marathonUrl = "http://10.16.7.225:8080"
def containerName = "ci-pipeline-maven"
def containerVersion

node {
    stage('Checkout') {
        checkout([
                $class           : 'GitSCM',
                branches         : scm.branches,
                extensions       : scm.extensions + [
                        [$class: 'LocalBranch', localBranch: "${branch}"]],
                userRemoteConfigs: scm.userRemoteConfigs])
    }

    // If the commit is a release commit skip the entire flow as this was not a valid trigger for CI/CD
    if (!isReleaseCommit()) {

        // Compile and run Unit Tests. This will also run jacoco report for unit tests and report the findings.
        stage('Build and Test') {
            mvn "clean verify -DskipITs -DskipJacocoAggregate"
            junit '**/target/surefire-reports/*.xml'
            jacoco(execPattern: '**/target/jacoco.exec')
        }

        // Run Integration Tests separately because these may take longer then the unit tests.
        stage('Component Test') {
            mvn "verify -DskipUTs"
            jacoco(execPattern: '**/target/jacoco-it.exec')
            jacoco(execPattern: '**/target/jacoco-aggregate.exec')
        }

        /* If we are in a master branch or a develop branch then report code coverage and code statistics to Sonar.
           we are currently only tracking these two branches because sonar does not support multi-branch projects.
        if (branch == "master" || branch == "develop") {
            stage('Sonar Analysis') {
                withSonarQubeEnv('sonarqube') {
                    mvn "sonar:sonar"
                }
                timeout(time: 1, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure. ${qg.status}"
                        }
                    }
                }
            }
        }*/

        /* Build the docker container and set the current containerVersion from the pom.xml artifact version. Currently
           does not support multi module to have different versions.*/
        stage("Build Container") {
            mvn "install -DskipUTs -DskipITs -DskipJacocoAggregate"

            def pom = readMavenPom file: 'pom.xml'

            containerVersion = pom.version
            echo "Pom: ${pom}. pom version: ${containerVersion}"
            sh "docker build --build-arg artifactVersion=${containerVersion} -t ${containerName}:${containerVersion} ."
        }

        // Upload artifacts if we are in a master branch
        if (isDeployableBranch()) {
            stage('Upload Artifacts') {
                sh "docker tag ${containerName}:${containerVersion} rhldcmesboot711.na.rccl.com:5000/${containerName}:${containerVersion}"
                sh "docker push rhldcmesboot711.na.rccl.com:5000/${containerName}:${containerVersion}"
                sh "docker rmi rhldcmesboot711.na.rccl.com:5000/${containerName}:${containerVersion}"
            }
        }

        // Create a Release Link to track current tracked Jira tickets that have been merged to master.
        if (isMasterBranch()) {
            stage('JIRA') {
                //pre-req: master must be tagged with production release

                def latestReleaseTag = sh(returnStdout: true, script: 'git for-each-ref refs/tags/v*.*.* --sort=-taggerdate --format="%(refname:short)" --count=1').trim()
                echo latestReleaseTag
                def tickets = sh(returnStdout: true, script: "git log ${latestReleaseTag}.. --pretty=oneline" + ' | perl -ne \'{ /(\\w+)-(\\d+)/ && print \"$1-$2\n\" }\' | sort | uniq').trim()
                //TODO: clean this up, it's not full proof
                tickets = tickets.replaceAll('AUTOREL-42\n','').replaceAll('\n', ',')
                echo tickets
                def JIRAurl = "https://royal-digital.atlassian.net/issues/?jql=issue%20in%20%28${tickets}%29"
                def link = "<a href='${JIRAurl}' target='_blank'>JIRA Link</a>"
                //rtp nullAction: '1', parserName: 'Confluence', stableText: "[!http://10.16.4.8/service/jenkins-rccl/static/21acaa2b/images/48x48/blue.png!|${JIRAurl}]"
                rtp nullAction: '1', parserName: 'HTML', stableText: "${link}"
            }
        }

        if (isMasterBranch()) {
            sshagent(['47bd5394-9c3a-4dd9-9d71-5b2dcab92c0e']) {
                stage('Release Management') {
                    echo 'releasing'
                    sh 'git config user.email jenkins@rccl.com'
                    sh 'git config user.name jenkins'
                    mvn 'release:clean release:prepare -DscmCommentPrefix="AUTOREL-42 " -DtagNameFormat=v@{project.version} -Darguments=\"-DskipTests -DskipJacocoAggregate\"'
                    //sh 'git push --set-upstream origin local_branch'
                    echo 'fini!'
                    def pom = readMavenPom file: 'pom.xml'
                    def tag = pom.scm.tag
                    echo "tag from pom.scm ${tag}"
                    if (tag != null && containerVersion.contains(tag)) {
                        echo "deleting tag ${tag} because we are doing a pretend rollback of release (rollback not implemented)!"
                    }
                }
            }
        }

        // Deploy artifacts to DC/OS
        if (isDeployableBranch()) {
            stage("Deployment") {
                marathon id: '/dev/automation/ci-pipeline-maven',
                        credentialsId: '680555a7-32b1-4fde-b2bd-f11d03e4f026',
                        docker: "rhldcmesboot711.na.rccl.com:5000/${containerName}:${containerVersion}",
                        filename: 'ci-pipeline-maven.json',
                        url: "${marathonUrl}"
            }
        }

        // Clean up any temporary artifacts
        stage("Clean-Up") {
            sh "docker rmi ${containerName}:${containerVersion}"
        }
    }
}

/**
 * Shared method to execute method calls.
 *
 * @param mvnArgs maven arguments to pass
 * @param args non-maven arguments, this can be used to pass additional unix commands.
 */
void mvn(def mvnArgs, def args = "") {
    /* Get the maven tool. */
    def mvnHome = tool name: 'Maven 3.3.9', type: 'maven'

    if (isUnix()) {
        sh "${mvnHome}/bin/mvn ${mvnArgs} -B -V -U -e ${args}"
    } else {
        bat "${mvnHome}\\bin\\mvn ${mvnArgs} -B -V -U -e ${args}"
    }
}

/**
 * Check if the current branch is the master branch
 *
 * @return true if the branch is master, false otherwise
 */
boolean isMasterBranch() {
    return env.BRANCH_NAME == 'master'
}

/**
 * Check if the current branch should be deployed
 *
 * @return true if the current branch should be deployed, false otherwise
 */
boolean isDeployableBranch() {
    return (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == "develop" || env.BRANCH_NAME.startsWith('release/'));
}

/**
 * Checks if the last commit was a release commit. A release commit is a commit done by the automated release
 * management system
 *
 * @return true if the commit was a release commit.
 */
boolean isReleaseCommit() {
    // If we are not in master branch we do not have release management
    if (env.BRANCH_NAME != 'master') {
        return false
    }

    // Normally the exit status is 0 if a line is selected, 1 if no lines were selected, and 2 if an error occurred.
    try {
        result = sh(script: "git log -1 | grep '.*AUTOREL-42.*'", returnStatus: true)
        return result == 0
    } catch (Exception err) {
        echo "Error happened while executing git log -1 | grep '.*AUTOREL-42.*'"
        currentBuild.result = 'FAILURE'
    }
}

