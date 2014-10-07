QRCode
======

Generates png QRCodes on the command line

How to make a command line executable from the .jar
---------------------------------------------------
This will help you create an executable without having to enter `java -jar` all the time.

```
> (echo '#!/usr/bin/java -jar'; cat out/artifacts/QRCode_jar/QRCode.jar) > qrcode
```