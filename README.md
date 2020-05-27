# Github pull requests filter by labels plugin

This plugin is inspired by the [Github additional traits plugin](https://github.com/jenkinsci/github-additional-traits-plugin).
It provides filters for:
 - Filter pull requests with all specified labels.  
   - The pull request will be discovered which having all labels 
 - Filter pull requests with any specified labels.
   - The pull request will be discovered which having any labels.
 - Exclude pull requests with any specified labels.
   - The pull requests will be excluded which having any labels.
   
Since it's only possible to create or delete the pull request job can created or deleted when create or delete the Github pull request,
to auto create or delete the jobs, this plugin adds a new option to "Scan by labeled/unlabeled github webhook events".  
After enable this one, labeling or unlabeling a PR on the github repository triggers a scanning job.
 
