@Library("yooture")

def yooBuild = new yooture.jenkins.yooture.YooBuild(this)
def snapshotTag = "N/A"
def releasedArtifact = ["version":"N/A"]

pipeline {
  agent any

//   environment {
    // Creates variables AZ_DOCKER_REGISTRY=uuuu:pppp, AZ_DOCKER_REGISTRY_USR=uuuu, AZ_DOCKER_REGISTRY_PSW=pppp
    //DOCKER_REGISTRY = credentials("AZ_DOCKER_REGISTRY")
//   }

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
      when { not { branch 'feature/update-spring' } }
      agent {
        docker {
          reuseNode true
          image 'yooture/yoo-java-11-node-8:4'
          // - mount maven repo from host system
          // - mount docker socket from host system
          // - run as root
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
      when { branch 'feature/update-spring' }
      agent {
        docker {
          reuseNode true
          image 'yooture/yoo-java-11-node-8:4'
          // - mount maven repo from host system
          // - mount docker socket from host system
          // - run as root
          args '-v $HOME/.m2:/root/.m2'
        }
      }
      steps {
        script {
          withMaven(mavenSettingsConfig: 'DEFAULT_YOOTRE_MAVEN_SETTINGS', options: [artifactsPublisher(disabled: true)]) {
            yooBuild.doGitConfig(false)
            releasedArtifact = yooBuild.mvnReleaseMasterBranch(".")
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