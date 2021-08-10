
# [HTTP Archive format files](https://www.wikiwand.com/en/HAR_(file_format)#:~:text=The%20HTTP%20Archive%20format%2C%20or,har.&text=The%20specification%20for%20this%20format,Wide%20Web%20Consortium%20(W3C).)

HAR files can be imported in tools like Insomnia or Postman and used to test web-APIs.
The provided file *questionnaire-test-requests.har* contains a couple of examples on how to upload an example ValueSet and a Questionnaire, and a QuestionnaireResponse which can be validated using the conformance resources.

 1. Upsert the example ValueSet resource using the **PUT ValueSet request**.
 2. Upsert the example Questionnaire using the **PUT Questionnaire request**.
 3. Validate the QuestionnaireResponse using the **POST QuestionnaireResponse-validate request**.
