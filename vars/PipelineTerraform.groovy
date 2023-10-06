def call(Map pipelineParams) {

  if(!pipelineParams) {
     pipelineParams = [:]
   }

  pipeline {
    agent {
      docker {
          reuseNode true
          image 'hashicorp/terraform'
          args '--entrypoint="" -v /etc/passwd:/etc/passwd -v /home/ubuntu/.ssh:/home/ubuntu/.ssh -v "$PWD":"${PWD}"'
      }
    }

    environment {
      AWS_DEFAULT_REGION = "us-east-1"
      TF_LOG = "${pipelineParams.terraformDebugOutput ? : ''}"
    }

    options {
      buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

      stage('terraform: init') {
        steps {
          sshagent (['09734b96-0fae-4930-8b8e-3475c93ceaa3']){
            sh 'terraform init'
          }
        }
      }

      stage('terraform: deve') {
        when {
          not {
            branch 'master'
          }
        }
        steps {
          script {
            def exists = fileExists 'deve.tfvars'
            if (exists){
              def status = sh(returnStatus: true, script: "terraform workspace list | grep deve")
              if (status == 0) {
                sh "terraform workspace select deve"
              } else {
                sh "terraform workspace new deve"
              }
              sh "terraform plan -var-file=deve.tfvars -out=./terraform.out && terraform apply './terraform.out' && rm terraform.out"
            } else {
              echo "Pipeline Dev Step Skipped Due To No Terraform File"
            }
          }
        }
      }

      stage('terraform: test') {
        when {
          branch "master"
        }
        steps {
          script {
            def exists = fileExists 'test.tfvars'
            if (exists){
              def status = sh(returnStatus: true, script: "terraform workspace list | grep test")
              if (status == 0) {
                sh "terraform workspace select test"
              } else {
                sh "terraform workspace new test"
              }
              sh "terraform plan -var-file=test.tfvars -out=./terraform.out && terraform apply './terraform.out' && rm terraform.out"
            } else {
              echo "Pipeline Test Step Skipped Due To No Terraform File"
            }
          }
        }
      }

      stage('terraform plan: stag') {
        when {
          branch "master"
        }
        steps {
          script {
            def exists = fileExists 'stag.tfvars'
            if (exists){
              def status = sh(returnStatus: true, script: "terraform workspace list | grep stag")
              if (status == 0) {
                sh "terraform workspace select stag"
              } else {
                sh "terraform workspace new stag"
              }
              sh "terraform plan -var-file=stag.tfvars -out=./terraform.out"
            }
          }
        }
      }

      stage('Approve Terraform Plan: Stag') {
        when {
          branch "master"
        }
        steps {
          script {
            def exists = fileExists 'stag.tfvars'
            if (exists){
              input 'Deploy to Stag?'
            }
          }
        }
      }

      stage('terraform apply: stag') {
        when {
          branch "master"
        }
        steps {
          script {
            def exists = fileExists 'stag.tfvars'
            if (exists){
              sh "terraform apply './terraform.out' && rm terraform.out"
            }
          }
        }
      }

      stage('terraform plan: prod') {
        when {
          branch "master"
        }
        steps {
          script {
            def exists = fileExists 'prod.tfvars'
            if (exists){
              def status = sh(returnStatus: true, script: "terraform workspace list | grep prod")
              if (status == 0) {
                sh "terraform workspace select prod"
              } else {
                sh "terraform workspace new prod"
              }
              sh "terraform plan -var-file=prod.tfvars -out=./terraform.out"
            }
          }
        }
      }

      stage('Approve Terraform Plan: Prod') {
        when {
          branch "master"
        }
        steps {
          script {
            def exists = fileExists 'prod.tfvars'
            if (exists){
              input 'Deploy to Prod?'
            }
          }
        }
      }

      stage('terraform apply: prod') {
        when {
          branch "master"
        }
        steps {
          script {
            def exists = fileExists 'prod.tfvars'
            if (exists){
              sh "terraform apply './terraform.out' && rm terraform.out"
            }
          }
        }
      }
    }

    post {
      always {
        deleteDir()
        cleanWs()
      }
    }
  }
}
