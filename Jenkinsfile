@Library("yooture")

def yooBuild = new yooture.jenkins.yooture.YooBuild(this)
def snapshotTag = "N/A"
def releasedArtifact = ["version":"N/A"]

pipeline {
  agent any

  parameters {
    string(name: 'VERSION', defaultValue: 'NONE', description: 'the version to publish the artifact to nexus with. (NONE: do not publish, just build)')
  }



  // environment {
  //   // Creates variables AZ_DOCKER_REGISTRY=uuuu:pppp, AZ_DOCKER_REGISTRY_USR=uuuu, AZ_DOCKER_REGISTRY_PSW=pppp
  //   //DOCKER_REGISTRY = credentials("AZ_DOCKER_REGISTRY")
  // }

  options {
    // General Jenkins job properties
    timeout(time: 45, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr:'10'))
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    // wait 20 sec for other possible changes comming in
    quietPeriod(20)
  }

  stages {

    stage('build Snapshot') {
      when { expression { return params.VERSION == "NONE" } }
      agent {
        docker {
          reuseNode true
          image 'yooture/yoo-java-11-node-8:4'
          // - mount maven repo from host system
          args '-v $HOME/.m2:/root/.m2'
        }
      }
      steps {
        script {
          snapshotTag = "${currentBuild.startTimeInMillis}-SNAPSHOT"
          yooBuild.mvnInstall('.')
        }
      }
    }

    stage('Build Release') {
      when { expression { return params.VERSION != "NONE" } }
      agent {
        docker {
          reuseNode true
          image 'yooture/yoo-java-11-node-8:4'
          // - mount maven repo from host system
          args '-v $HOME/.m2:/root/.m2'
        }
      }
      steps {
        script {
          withMaven(mavenSettingsConfig: 'DEFAULT_YOOTRE_MAVEN_SETTINGS', options: [artifactsPublisher(disabled: true)]) {
            runMvn "versions:set -DnewVersion=${params.VERSION}"
            runMvn "clean deploy"

            def model = readMavenPom(file: 'pom.xml')
            def releasedArtifact = "${model.groupId}:${model.artifactId}:${model.version}"

            // restore the old version to be prepared for new releases
            runMvn "versions:revert"
            slackSend color: 'good', message: "a new version was released to the maven repository: ${releasedArtifact}"
          }
        }
      }
    }

  }
  post {
    always {
      junit allowEmptyResults: true, testResults: '**/target/*-reports/*.xml'
      jiraSendBuildInfo site: 'yooture.atlassian.net'
    }
    fixed {
      slackSend color: 'good', message: "FIXED ${currentBuild.displayName}: ${currentBuild.projectName} - ${currentBuild.absoluteUrl}"
    }
    unstable {
      slackSend color: 'warning', message: "please fix!!! ${currentBuild.displayName}: ${currentBuild.projectName} - ${currentBuild.absoluteUrl}"
    }
    failure {
      slackSend color: 'danger', message: "please fix!!! ${currentBuild.displayName}: ${currentBuild.projectName} - ${currentBuild.absoluteUrl}"
    }
  }
}