### Example data

Here you will find example data for the jarfter and warfter web services

The files is meant for the following services:
- newTransformation.clj - For generating Jar files that transform from CSV to any of the other formats (rdf, ttl, n3...)
- csvTransformation.clj - For generating Jar files that transform from CSV to CSV
- example-data_scenario1.csv - Raw data for the Jar files created based on the above two transformations

- warfter_input.clj - For creating war file through the warfter service in jarfter.
- example-data.csv - Input for the war file created from warfter_input.clj.



### Testing deployed warfter

In order to test a deployed instance of warfter, use the following curl command:

```bash
curl -X POST --data-binary @example-data.csv -H "Content-type: text/txt" http://<ip-address>:<port>/<warfter-name>/streamTransform
```