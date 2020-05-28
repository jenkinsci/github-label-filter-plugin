# Github pull requests filter by labels plugin

This plugin is inspired by the [Github additional traits plugin](https://github.com/jenkinsci/github-additional-traits-plugin).
It provides filters for:
 - Filter pull requests with all specified labels.  
   - The pull request will be discovered which having all labels 
 - Filter pull requests with any specified labels.
   - The pull request will be discovered which having any labels.
 - Exclude pull requests with any specified labels.
   - The pull requests will be excluded which having any labels.
   
This plugin adds a new "Scan by labeled/unlabeled github webhook events" option.  
After enable this one, labeling or unlabeling a PR on the github repository triggers a scanning job again.
 
