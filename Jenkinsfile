pipeline {
    agent {
        node {
            label 'docker-jdk-23'
        }
    }
    stages {
        stage ('Setup') {
            steps {
                sh 'chmod +x ./mvnw'
            }
        }

        stage ('Build') {
            steps {
                sh './mvnw -Dmaven.test.failure.ignore=true -Dgroups=!CI-skip install'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml'
                }
            }
        }
    }
}