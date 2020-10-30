# HungryMoose

<img src="moose-01.png" alt="HungryMoose moose image" width="50%" height="50%">

## About HungryMoose

The aim of HungryMoose is to make documenting tested APIs almost effortless. 
By using a unified data format to generate both tests and Web-based documentation, 
developers can rest assured that a test and its documentation will form an atomic unit. As a user, 
you are guaranteed that any examples you see in the documentation are functional examples. 

- [HungryMoose Docs](https://github.com/FordLabs/HungryMoose/tree/master/hungry-moose-docs)
- [HungryMoose Test](https://github.com/FordLabs/HungryMoose/tree/master/hungry-moose-test)

## HungryMoose File Structure

HungryMoose is built around structured YAML files. These files are used by both the JUnit runner and the 
documentation generator to complete their respective tasks. You can learn more about what both of these projects do in
the [documentation](https://github.com/FordLabs/HungryMoose/tree/master/hungry-moose-docs) and [testing](https://github.com/FordLabs/HungryMoose/tree/master/hungry-moose-test) projects.

The YAML file can contain multiple examples known as *scenarios*. They are broken up in standard YAML fashion by  triple 
dashes `---` Each *scenario* is made up of four fields as described below.

| Field | Description |
| ----------- | ----------- |
| name | The name of the *scenario*. Should be something easily understood like the name of a test |
| detail | Additional context on what the *scenario* is for or trying to accomplish |
| request | The text representation of the request including the url, verb, headers, and body |
| response | The text representation of the response including the status code, headers, and body |

The request object would look like something similar to this:

```$xslt
POST /endpoint/to/hit
Content-Type: application/json

{"data": "foo" }
```

and a response would look somthing like this:

```$xslt
HTTP/1.1 200 OK
Content-Type: application/json

{"data":"bar"}
```

A complete file with two examples would look like this:

```$xslt
name: Can send arbitrary HTTP headers
request: |
  GET /echo
  Something: somewhere
  Everything: everywhere

response: |
  HTTP/1.1 200 OK
  Content-Type: application/json

  {
    "method": "GET",
    "headers": {
      "something": ["somewhere"],
      "everything": ["everywhere"]
    },
    "body": "...",
    "uri": "..."
  }
  
---

name: Can submit a PUT request
request: |
  PUT /echo
  Content-Type: application/xml

  {"data":"here ya go"}

response: |
  HTTP/1.1 200 OK
  Content-Type: application/json

  {
    "uri": "/echo",
    "method": "PUT",
    "headers": "...",
    "body": {"data":"here ya go"}
  }

```
