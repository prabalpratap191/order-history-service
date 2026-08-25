pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        ACCOUNT_ID = '123456789012'
        ECR_REGISTRY = '230476794540.dkr.ecr.us-east-1.amazonaws.com'
        ECR_REPO = 'meracommerce/order-history-service'
        IMAGE_NAME = "${ECR_REGISTRY}/${ECR_REPO}"
        EKS_CLUSTER = 'meracommerce-dev'
        NAMESPACE = 'order-history-ns' // Change as needed per service
        IMAGE_TAG = "1.0.${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master',
                    url: 'https://github.com/prabalpratap191/order-history-service.git'
            }
        }

        stage('Set Version') {
            steps {
                script {
                    def version = readFile('version.txt').trim()
                    def (major, minor, patch) = version.tokenize('.')
                    patch = (patch as int) + 1
                    def newVersion = "${major}.${minor}.${patch}"
                    writeFile file: 'version.txt', text: newVersion
                    sh "mvn versions:set -DnewVersion=${newVersion}"
                }
            }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn clean package -DskipTests'
                sh 'cd ./target'
                sh 'ls'
            }
        }

        stage('Docker Build & Tag') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'jenkins-user']
                ]) {
                    script {
                        def imageTag = "${env.BUILD_NUMBER}"
                        sh "docker build -t ${env.ECR_REGISTRY}/${env.ECR_REPO}:${imageTag} ."
                        sh "docker tag ${env.ECR_REGISTRY}/${env.ECR_REPO}:${imageTag} ${env.ECR_REGISTRY}/${env.ECR_REPO}:latest"
                    }
                }
            }
        }

        stage('ECR Login') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'jenkins-user']
                ]) {
                    sh '''
                        aws ecr get-login-password \
                        --region $AWS_REGION \
                        | docker login \
                        --username AWS \
                        --password-stdin \
                        $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
                    '''
                }
            }
        }

        stage('Push To ECR') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'jenkins-user']
                ]) {
                    sh "docker push ${env.ECR_REGISTRY}/${env.ECR_REPO}:${env.BUILD_NUMBER}"
                    sh "docker push ${env.ECR_REGISTRY}/${env.ECR_REPO}:latest"
                }
            }
        }

        stage('Update Kubeconfig') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'jenkins-user']
                ]) {
                    sh "aws eks update-kubeconfig --region ${env.AWS_REGION} --name ${env.EKS_CLUSTER}"
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'jenkins-user']
                ]) {
                    script {
                        sh """
                            kubectl set image deployment/order-history-deployment order-history-container=${IMAGE_NAME}:${IMAGE_TAG} -n ${NAMESPACE} || \
                            kubectl create deployment order-history-deployment --image=${IMAGE_NAME}:${IMAGE_TAG} -n ${NAMESPACE}
                            kubectl rollout status deployment/order-history-deployment -n ${NAMESPACE}
                        """
                    }
                }
            }
        }
    }

    post {
        failure {
            echo 'Pipeline failed. Please check the logs.'
        }
        success {
            echo 'Deployment successful!'
        }
    }
}
