pipeline {
    agent any
    environment {
        IMAGE_NAME = "demo-ci-cd:latest"
        VM_IP = "74.163.99.83"                 // IP de tu VM
        VM_USER = "azureuser"                  // Usuario de tu VM
        SSH_KEY = credentials('vm-ssh-key')    // Credencial configurada en Jenkins
        ARTIFACT_NAME = "target/demo-0.0.1-SNAPSHOT.jar"
        REMOTE_DIR = "/home/azureuser/tarea/"
        JAR_NAME = "demo-0.0.1-SNAPSHOT.jar"
    }
    stages {
        /*stage('Clone Repository') {
            steps {
                git 'https://github.com/your-org/your-springboot-repo.git'
            }
        }*/
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Code Quality') {
            steps {
                sh 'mvn checkstyle:check -Dcheckstyle.config.location=checkstyle.xml'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Code Coverage') {
            steps {
                sh 'mvn jacoco:report'
            }
        }
        stage('Deploy to Staging') {
            steps {
                sshagent(['vm-ssh-key']) {
                    // Crear directorio remoto
                    sh """
                        ssh -o StrictHostKeyChecking=no ${VM_USER}@${VM_IP} \
                        "mkdir -p ${REMOTE_DIR}"
                    """
                    
                    // Copiar el artefacto
                    sh """
                        scp -o StrictHostKeyChecking=no \
                        ${ARTIFACT_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}${JAR_NAME}
                    """
                    
                    // Ejecutar la aplicación
                    sh """
                        ssh -o StrictHostKeyChecking=no ${VM_USER}@${VM_IP} \
                        "cd ${REMOTE_DIR} && \
                        sudo nohup java -jar ${JAR_NAME} --server.port=80 > app.log 2>&1 &"
                    """
                }
            }
        }
        stage('Validate Deployment') {
            steps {
                sh 'sleep 10'
                /*# sh 'curl --fail http://your-staging-server:8080/health' */
            }
        }
    }
}
