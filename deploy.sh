#!/bin/bash
set -e

# Configuration
CLUSTER_NAME="meracommerce-dev"
REGION="us-east-1"
NAMESPACE="default"
APP_NAME="springboot-app"
ECR_REPO="230476794540.dkr.ecr.us-east-1.amazonaws.com/your-springboot-app"
IMAGE_TAG="latest"

echo "=== Configuring kubectl ==="
aws eks update-kubeconfig --region $REGION --name $CLUSTER_NAME

echo "=== Verifying cluster access ==="
kubectl get nodes

echo "=== Installing AWS Load Balancer Controller ==="
# Check if already installed
if ! kubectl get deployment -n kube-system aws-load-balancer-controller &> /dev/null; then
    helm repo add eks https://aws.github.io/eks-charts
    helm repo update
    
    VPC_ID=$(aws eks describe-cluster --name $CLUSTER_NAME --region $REGION --query "cluster.resourcesVpcConfig.vpcId" --output text)
    
    helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
        -n kube-system \
        --set clusterName=$CLUSTER_NAME \
        --set serviceAccount.create=false \
        --set serviceAccount.name=aws-load-balancer-controller \
        --set region=$REGION \
        --set vpcId=$VPC_ID
    
    echo "Waiting for Load Balancer Controller to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment/aws-load-balancer-controller -n kube-system
else
    echo "AWS Load Balancer Controller already installed"
fi

echo "=== Validating Helm chart ==="
helm lint $APP_NAME/

echo "=== Deploying application ==="
helm upgrade --install $APP_NAME ./$APP_NAME \
    --namespace $NAMESPACE \
    --create-namespace \
    --set image.repository=$ECR_REPO \
    --set image.tag=$IMAGE_TAG \
    --wait \
    --timeout 10m

echo "=== Deployment Status ==="
kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=$APP_NAME
kubectl get svc -n $NAMESPACE -l app.kubernetes.io/name=$APP_NAME
kubectl get ingress -n $NAMESPACE

echo "=== Getting ALB DNS Name ==="
ALB_DNS=$(kubectl get ingress $APP_NAME -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
echo "Application URL: http://$ALB_DNS"
echo "Swagger UI: http://$ALB_DNS/swagger-ui.html"

echo "=== Deployment Complete ==="
