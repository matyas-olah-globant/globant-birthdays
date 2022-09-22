# Globant birthdays

A simple Java project to read Globant employees' data from the daily Birthdays! e-mails.

---
### Prerequisites:

Run MongoDB as a local service and have a dedicated database.\
The default values (`localhost:27017/company_data`) are hardcoded in the `MongoUtil` class.

**! Please keep using a local DB instance (instead of connecting to a remote location, eg. MongoDB Atlas),\
as the processed data is confidential information, therefore should be kept on Globant premises.**

---
### Usage:

Download the e-mails as .eml files, then pass their filename in as command line arguments.\
You can also pass a directory, in which case the contents will be checked, ignoring files that are not .eml format.\
Also, you can pass multiple arguments.

---

Comments, code reviews (even informal ones), suggestions are welcome,\
feel free to contact me via [e-mail](mailto:matyas.olah@globant.com) or on [Globant Slack](https://globant.slack.com/archives/D01GU6JPEH0).

---
