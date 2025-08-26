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
                git 'https://github.com/MiguelMachuca/springboot-ci-staging.git'                
            }
        }*/
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
                /*sh 'mvn clean package spring-boot:repackage -DskipTests'*/
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
                script {
                    // Crear directorio remoto si no existe
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "mkdir -p ${REMOTE_DIR}"
                    """
                    
                    // Copiar el artefacto
                    sh """
                        scp -o StrictHostKeyChecking=no -i ${SSH_KEY} \
                        ${ARTIFACT_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}${JAR_NAME}
                    """
                    
                    // Detener aplicación anterior (si existe) y levantar nueva
                    /*sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "cd ${REMOTE_DIR} && \
                        pkill -f '${JAR_NAME}' || true && \
                        sleep 10 \
                        nohup java -jar ${JAR_NAME} --server.port=8080 > app.log 2>&1 &"                        
                    """*/

                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "cd ${REMOTE_DIR} && \
                        echo '⏹️ Deteniendo aplicación anterior...' && \
                        pkill -f '${JAR_NAME}' || true && \
                        
                        echo '⏳ Esperando a que el puerto 8080 quede libre...' && \
                        while lsof -i:8080 -t >/dev/null 2>&1; do sleep 2; done && \

                        echo '🚀 Iniciando nueva versión...' && \
                        setsid nohup java -jar ${JAR_NAME} --server.port=8080 > app.log 2>&1 &"
                    """                   
                }
            }
        }

        stage('Validate Deployment') {
            steps {
                sh 'sleep 10'
                sh """curl --fail http://${VM_IP}:8080/health"""
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