pipeline {
    agent none
    stages {
        stage('Clean workspace') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-8-slim'
                }
            }
            steps {
                sh 'mvn clean'
            }
        }
        stage('Build application') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-8-slim'
                }
            }
            steps {
                sh 'mvn install'
            }
        }
        stage('Build docker image') {
            agent any
            steps {
                sh 'docker image rm miraclewisp/hperproteinaemia-gateway || true'
                sh 'docker build -t miraclewisp/hperproteinaemia-gateway:${BUILD_NUMBER} -t miraclewisp/hperproteinaemia-gateway:latest .'
            }

        }
        stage('Push docker image') {
            agent any
            steps {
                withDockerRegistry([credentialsId: "dockerhub", url: ""]) {
                    sh 'docker push miraclewisp/hperproteinaemia-gateway:${BUILD_NUMBER}'
                    sh 'docker push miraclewisp/hperproteinaemia-gateway:latest'
                }
            }

        }
        stage('Deploy') {
            agent any
            steps {
                sh 'ssh Rinslet docker stop gateway || true'
                sh 'ssh Rinslet docker image rm miraclewisp/hperproteinaemia-gateway || true'
                sh 'ssh Rinslet docker pull miraclewisp/hperproteinaemia-gateway'
                sh 'ssh Rinslet docker run --rm --name gateway -d -p 8080:8080 --network host miraclewisp/hperproteinaemia-gateway'
            }
        }
    }
}
