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
        // Optionally, set JAVA_HOME if needed for your build tool
        IMAGE_TAG = "1.0.${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {

            steps {

                git branch: 'main',
                url: 'https://github.com/prabalpratap191/order-history-service.git'

            }
        }

        stage('Maven Build') {

            steps {

                sh 'mvn clean package -DskipTests'

            }
        }

        stage('Docker Build & Tag') {

            steps {
                script {
                    VERSION = "${env.BUILD_NUMBER}"
                    sh "docker build -t ${IMAGE_NAME}:${VERSION} ."
                    sh "docker tag ${IMAGE_NAME}:${VERSION} ${IMAGE_NAME}:latest"
                }
            }
        }

        stage('ECR Login') {

            steps {
  withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'jenkins-user']
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
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'jenkins-user']
                ]) {
                sh '''

                docker tag \
                $ECR_REPO:$IMAGE_TAG \
                $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG

                docker push \
                $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG

                '''
  }
            }
        }

        stage('Update Kubeconfig') {
                    steps {
                          withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'jenkins-user']
                ]) {
                        sh "aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER}"
                    }
                    }
        }


        stage('Deploy to EKS') {
            steps {
                  withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'jenkins-user']
                ]) {
                script {
                    // If using Helm, replace with helm upgrade/install command
                    sh """
                    kubectl set image deployment/order-history-deployment order-history-container=${IMAGE_NAME}:${VERSION} -n ${NAMESPACE} || \
                    kubectl create deployment order-history-deployment --image=${IMAGE_NAME}:${VERSION} -n ${NAMESPACE}
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
