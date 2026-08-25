pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        ACCOUNT_ID = '123456789012'
        ECR_REGISTRY = '230476794540.dkr.ecr.us-east-1.amazonaws.com'
        ECR_REPO = 'meracommerce/order-history-service'
        EKS_CLUSTER = 'meracommerce-dev'
        NAMESPACE = 'order-history-ns'
    }

    stages {
        stage('Checkout') {
            steps {
                // Public repo: no credentials needed for clone
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

                    // If you want to push version.txt back to GitHub, add a GitHub PAT credential in Jenkins (e.g., id: 'github-token')
                    withCredentials([string(credentialsId: 'github-token', variable: 'Github_Pull_token')]) {
                        sh "git config user.email 'prabalpratap191@gmail.com'"
                        sh "git config user.name 'prabalpratap191'"
                        sh 'git add version.txt'
                        sh "git commit -m 'Bump version to ${newVersion} [ci skip]' || echo 'No changes to commit'"
                        // Use token for push
                        sh 'git remote set-url origin https://${Github_Pull_token}@github.com/prabalpratap191/order-history-service.git'
                        sh "git push origin HEAD:master || echo 'No changes to push'"
                    }

                    sh "mvn versions:set -DnewVersion=${newVersion}"
                    env.IMAGE_TAG = newVersion
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
                        def imageTag = env.IMAGE_TAG
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
                    sh "docker push ${env.ECR_REGISTRY}/${env.ECR_REPO}:${env.IMAGE_TAG}"
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
                        def imageName = "${env.ECR_REGISTRY}/${env.ECR_REPO}:${env.IMAGE_TAG}"
                        sh """
                            kubectl set image deployment/order-history-deployment order-history-container=${imageName} -n ${env.NAMESPACE} || \
                            kubectl create deployment order-history-deployment --image=${imageName} -n ${env.NAMESPACE}
                            kubectl rollout status deployment/order-history-deployment -n ${env.NAMESPACE}
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
