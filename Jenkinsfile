pipeline {
    agent any
    
    environment {
        IMAGE_NAME = "demo-ci-cd:latest"
        VM_IP = "20.206.160.112"                 // IP de tu VM
        VM_USER = "azureuser"                  // Usuario de tu VM
        SSH_KEY = credentials('ssh-key-tarea')    // Credencial configurada en Jenkins
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

        
        stage('Deploy to VM') {
            steps {
                script {
                    // Crear directorio remoto si no existe
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "mkdir -p ${REMOTE_DIR} 
                        "
                    """
                    
                    // Copiar el artefacto
                    sh """
                        scp -o StrictHostKeyChecking=no -i ${SSH_KEY} \
                        ${ARTIFACT_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}${JAR_NAME}
                    """
                    
                    // Detener aplicación anterior si existe y ejecutar la nueva
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "cd ${REMOTE_DIR} && \
                         sudo nohup java -jar ${JAR_NAME} --server.port=80 > app.log 2>&1 &"
                    """
                }
            }
        }
        

    }
    
    post {
        always {
            echo "Pipeline execution completed"
            // Limpieza opcional: cerrar conexiones SSH, etc.
        }
        failure {
            echo "Pipeline failed - Check logs for details"
            // Podrías agregar notificaciones aquí (email, Slack, etc.)
        }
        success {
            echo "Pipeline succeeded! 🎉"
        }
    }
}