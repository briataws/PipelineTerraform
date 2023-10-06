# Jenkins Pipeline: PipelineTerraform

## Description

This project is intended for use with [Jenkins](https://jenkins.io/) and Global Pipeline Libraries through the
[Pipeline Shared Groovy Libraries Plugin](https://wiki.jenkins.io/display/JENKINS/Pipeline+Shared+Groovy+Libraries+Plugin).

A common scenario when developing Jenkins [declarative pipelines](https://jenkins.io/doc/book/pipeline/syntax/), is
to bundle common custom pipeline tasks in a shared library so that all Jenkins pipeline configurations in an organisation
can leverage from them without the need to reimplement the same logic.

This pipeline integrates with [Terraform](https://www.terraform.io/docs/index.html) to allow the consumer of a
Terraform module to deploy their infrastructure into one or more defined cloud environments.

## Pipeline Usage & Workflow

### Jenkinsfile

To use this pipeline all that is required is adding _PipelineTerraform()_ into a file named `Jenkinsfile` in the root
of your project repository.

### Parameters

#### Required

Currently, there are _NO_ required parameters for this pipeline.

#### Optional

* _terraformDebugOutput_: [`TRACE`, `WARN`, `ERROR`, `DEBUG`] - Enable Terraform debugging output (defaults to `NULL`).

### Examples

`Jenkinsfile`

```groovy
PipelineTerraform()
```

---

`Jenkinsfile`

```groovy
PipelineTerraform(terraformDebugOutput: 'DEBUG')
```

### Additional Required Terraform Variable Files

This pipeline integrates with Terraform and thus expects that the following Terraform variable files be present
alongside the `Jenkinsfile`. Variable files are associated with a cloud environment of the same name. Excluding one
of the variable files will cause Jenkins to skip deployments to that environment.

* `deve.tfvars`
* `test.tfvars`
* `stag.tfvars`
* `prod.tfvars`

### Workflow

This pipeline follows a defined behavior and will perform the following workflow.

#### Non-Main Branch

This pipeline will push out changes to the `deve` environment for all changes to the non-master branch. Be aware that
this can cause conflicts if multiple long running branches exist.

#### Main Branch

Once code is merged into the master branch that code will be promoted to the following environments: `test`, `stag`, `prod`.
Before code is promoted to `prod` the pipeline will prompt you to accept the changes.
