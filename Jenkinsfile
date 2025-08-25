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
        
        // NUEVA ETAPA: Verificar conexión a la VM
        stage('Verify VM Connection') {
            steps {
                script {
                    echo "Verificando conexión SSH con la VM ${VM_IP}..."
                    
                    // Intentar conexión SSH y ejecutar un comando simple
                    def connectionTest = sh(
                        script: """
                            ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                            "echo 'Conexión SSH exitosa'; hostname; whoami"
                        """,
                        returnStatus: true
                    )
                    
                    if (connectionTest != 0) {
                        error "❌ No se pudo conectar a la VM ${VM_IP}. Verifica:\n" +
                              "1. Que la VM esté encendida\n" +
                              "2. Que la IP ${VM_IP} sea correcta\n" +
                              "3. Que el usuario ${VM_USER} exista\n" +
                              "4. Que la clave SSH sea válida\n" +
                              "5. Que el firewall permita conexiones SSH (puerto 22)"
                    } else {
                        echo "✅ Conexión SSH verificada exitosamente"
                    }
                    
                    // Verificar que Java esté instalado en la VM
                    echo "Verificando que Java esté instalado en la VM..."
                    def javaTest = sh(
                        script: """
                            ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                            "java -version 2>&1 || echo 'Java no encontrado'"
                        """,
                        returnStdout: true
                    )
                    
                    if (javaTest.contains("not found") || javaTest.contains("no such file")) {
                        error "❌ Java no está instalado en la VM. Instala Java con:\n" +
                              "sudo apt update && sudo apt install openjdk-17-jdk"
                    } else {
                        echo "✅ Java encontrado: ${javaTest.trim()}"
                    }
                    
                    // Verificar espacio en disco
                    echo "Verificando espacio en disco en la VM..."
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "df -h ${REMOTE_DIR} | tail -1"
                    """
                }
            }
        }
        
        stage('Deploy to Staging') {
            steps {
                script {
                    // Crear directorio remoto si no existe
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "mkdir -p ${REMOTE_DIR} && chmod 755 ${REMOTE_DIR}"
                    """
                    
                    // Verificar si hay una aplicación previa ejecutándose
                    echo "Verificando procesos Java previos..."
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "pkill -f 'java.*${JAR_NAME}' || echo 'No hay procesos previos'"
                    """
                    
                    // Esperar a que el puerto 80 se libere si estaba en uso
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "sleep 3"
                    """
                    
                    // Copiar el artefacto
                    echo "Copiando artefacto a la VM..."
                    sh """
                        scp -o StrictHostKeyChecking=no -i ${SSH_KEY} \
                        ${ARTIFACT_NAME} ${VM_USER}@${VM_IP}:${REMOTE_DIR}${JAR_NAME}
                    """
                    
                    // Dar permisos de ejecución al JAR
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "chmod +x ${REMOTE_DIR}${JAR_NAME}"
                    """
                    
                    // Ejecutar la nueva aplicación
                    echo "Iniciando la aplicación..."
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "cd ${REMOTE_DIR} && \
                         nohup java -jar ${JAR_NAME} --server.port=80 > app.log 2>&1 & \
                         echo \$! > app.pid"
                    """
                    
                    echo "✅ Despliegue completado. PID guardado en app.pid"
                }
            }
        }
        
        stage('Validate Deployment') {
            steps {
                script {
                    echo "Esperando a que la aplicación inicie..."
                    sh 'sleep 15'
                    
                    // Verificar que la aplicación esté ejecutándose
                    echo "Verificando procesos de la aplicación..."
                    sh """
                        ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                        "ps aux | grep java | grep ${JAR_NAME} || echo 'Proceso no encontrado'"
                    """
                    
                    // Intentar hacer un health check
                    echo "Realizando health check..."
                    def healthCheck = sh(
                        script: """
                            ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                            "curl -s -o /dev/null -w '%{http_code}' http://localhost:80/health || echo 'curl failed'"
                        """,
                        returnStdout: true
                    ).trim()
                    
                    if (healthCheck == "200") {
                        echo "✅ Health check exitoso (HTTP 200)"
                    } else {
                        echo "⚠️  Health check returned: ${healthCheck}"
                        // Mostrar logs para debugging
                        sh """
                            ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${VM_USER}@${VM_IP} \
                            "tail -20 ${REMOTE_DIR}app.log || echo 'No se pudieron leer logs'"
                        """
                    }
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