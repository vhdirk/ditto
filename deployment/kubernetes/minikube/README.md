## Eclipse Ditto :: Minikube 

This folder contains example yaml files which can be used to start Eclipse Ditto 
with its backing Database - MongoDB - and a reverse proxy - nginx - in front of the HTTP and WebSocket API.

## Configure nginx
The nginx's configuration is located in the `nginx.conf` file and contains a "Basic authentication" 
for accessing the HTTP and WebSocket API. The users for this sample authentication are configured 
in the `nginx.httpasswd` file also located in this directory.

In order to add a new entry to this file, use the "openssl passwd" tool to create a hashed password:
```bash
openssl passwd -quiet
 Password: <enter password>
 Verifying - Password: <enter password>
```

Append the printed hash in the `nginx.httpasswd` file placing the username who shall receive this 
password in front like this:
```
ditto:A6BgmB8IEtPTs
```

## Requirements
* [Minikube](https://github.com/kubernetes/minikube/)
* [kubectl](https://kubernetes.io/docs/tasks/kubectl/install/)
* [VirtualBox](https://www.virtualbox.org/wiki/Downloads)

## Start Eclipse Ditto

### Start Minikube
```bash
minikube start 
```  

### Apply the pod reader role 
This is necessary for the pods to access the Kubernetes API and then build the akka cluster.
```bash
cd <DITTO_PATH>
kubectl apply -f deployment/kubernetes/deploymentFiles/pod-reader-role.yaml
```

### Create configuration mappings
```bash
kubectl create configmap nginx-conf --from-file=deployment/kubernetes/deploymentFiles/nginx/nginx.conf
kubectl create configmap nginx-cors --from-file=deployment/kubernetes/deploymentFiles/nginx/nginx-cors.conf
kubectl create configmap nginx-htpasswd --from-file=deployment/kubernetes/deploymentFiles/nginx/nginx.htpasswd
kubectl create configmap nginx-index --from-file=deployment/kubernetes/deploymentFiles/nginx/index.html
kubectl create configmap swagger-ui-api --from-file=$PWD/documentation/src/main/resources/openapi
```

### Start Eclipse Ditto

#### Start MongoDB
There are two ways starting a MongoDB instance.
Either use a simple MongoDB container without persistence.
```bash
kubectl apply -f deployment/kubernetes/deploymentFiles/mongodb/mongodb.yaml
```

Or use the stateful MongoDB set with a local persistent volume.
```bash
kubectl apply -f deployment/kubernetes/deploymentFiles/mongodb-statefulset/storage-class.yaml
envsubst < deployment/kubernetes/deploymentFiles/mongodb-statefulset/persistent-volume.yaml | kubectl apply -f -
kubectl apply -f deployment/kubernetes/deploymentFiles/mongodb-statefulset/mongodb-statefulset.yaml
```

#### Start Ditto services
```bash
kubectl apply -f deployment/kubernetes/deploymentFiles/ditto/ditto-cluster.yaml
# Start ditto services with an alternative version e.g. 0-SNAPSHOT
# cat kubernetes/ditto/ditto-cluster.yaml | sed s/latest/0-SNAPSHOT/ | kubectl apply -f -
```

#### Start Swagger UI
```bash
kubectl apply -f deployment/kubernetes/deploymentFiles/swagger/swagger.yaml
```

#### Start Reverse Proxy (nginx)
```bash
kubectl apply -f deployment/kubernetes/deploymentFiles/nginx/nginx.yaml
```

### Use Eclipse Ditto
```bash
minikube service ditto
```

### Use Minikube Dashboard
```bash
minikube dashboard
```

Have Fun!
