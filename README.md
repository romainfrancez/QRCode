QRCode
======

Generates png QRCodes on the command line

How to make a command line executable from the .jar
---------------------------------------------------
This will help you create an executable without having to enter `java -jar` all the time.

```
> (echo '#!/usr/bin/java -jar'; cat out/artifacts/QRCode_jar/QRCode.jar) > qrcode
```

Licenses
--------

This software is under the MIT License

```
The MIT License (MIT)

Copyright (c) 2014 Romain Francez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

It uses the XZing library which is under the Apache License Version 2.0

```
http://www.apache.org/licenses/LICENSE-2.0
```