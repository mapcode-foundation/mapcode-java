# Mapcode Library for Java

Copyright (C) 2014 Stichting Mapcode Foundation (http://www.mapcode.com)

----

This Java project contains a library to encode latitude/longitude pairs to mapcodes
and to decode mapcodes back to latitude/longitude pairs.

**Online documentation can be found at: http://mapcode-foundation.github.io/mapcode-java/**

**Release notes can be found at: http://mapcode-foundation.github.io/mapcode-java/ReleaseNotes.html**

**An example of how to use this library can be found at: https://github.com/mapcode-foundation/mapcode-java-example**

# License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Original C library created by Pieter Geelen. Work on Java version
of the mapcode library by Rijn Buve and Matthew Lowden.

# Using Git and .gitignore

It's good practice to set up a personal global .gitignore file on your machine which filters a number of files
on your file systems that you do not wish to submit to the Git repository. You can set up your own global
.gitignore file by executing:
`git config --global core.excludesfile ~/.gitignore`

In general, filter these file types in `~/.gitignore` (each entry should be on a separate line):
`*.com *.class *.dll *.exe *.o *.so *.log *.sql *.sqlite *.tlog *.epoch *.swp *.hprof *.hprof.index *~`

If you're using a Mac, filter:
`.DS_Store* Thumbs.db`

If you're using IntelliJ IDEA, filter:
`*.iml *.iws *.releaseBackup .idea/`

If you're using Eclips, filter:
`.classpath .project .settings .cache`

If you're using NetBeans, filter: 
`nb-configuration.xml *.orig`
