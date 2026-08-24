pipeline {

    agent any

    environment {

        AWS_REGION = 'us-east-1'
        ECR_REPO = 'order-history-service'
        ACCOUNT_ID = '123456789012'

        IMAGE_TAG = "1.0.${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {

            steps {

                git branch: 'main',
                url: 'https://github.com/your-org/order-history-service.git'

            }
        }

        stage('Maven Build') {

            steps {

                sh 'mvn clean package -DskipTests'

            }
        }

        stage('Docker Build') {

            steps {

                sh '''
                docker build \
                -t $ECR_REPO:$IMAGE_TAG .
                '''
            }
        }

        stage('ECR Login') {

            steps {

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

        stage('Push To ECR') {

            steps {

                sh '''

                docker tag \
                $ECR_REPO:$IMAGE_TAG \
                $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG

                docker push \
                $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG

                '''
            }
        }

        stage('Deploy EC2') {

            steps {

                sshagent(['ec2-ssh-key']) {

                    sh '''
                    ssh -o StrictHostKeyChecking=no ec2-user@10.0.1.100 << EOF

                    docker pull \
                    $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG

                    docker stop order-history-service || true

                    docker rm order-history-service || true

                    docker run -d \
                    --name order-history-service \
                    -p 9095:9095 \
                    $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG

                    EOF
                    '''
                }
            }
        }
    }
}
