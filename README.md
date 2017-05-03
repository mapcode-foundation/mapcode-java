# Mapcode Library for Java

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/0c9f11645f504b50bd154a80fb95a5c3)](https://www.codacy.com/app/rijnb/mapcode-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mapcode-foundation/mapcode-java&amp;utm_campaign=Badge_Grade)
[![Build Status](https://img.shields.io/travis/mapcode-foundation/mapcode-java.svg?maxAge=3600)](https://travis-ci.org/mapcode-foundation/mapcode-java)
[![Coverage Status](https://coveralls.io/repos/github/mapcode-foundation/mapcode-java/badge.svg?branch=master&maxAge=3600)](https://coveralls.io/github/mapcode-foundation/mapcode-java?branch=master)
[![License](http://img.shields.io/badge/license-APACHE2-blue.svg)]()
[![Release](https://img.shields.io/github/release/mapcode-foundation/mapcode-java.svg?maxAge=3600)](https://github.com/mapcode-foundation/mapcode-java/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.mapcode/mapcode.svg?maxAge=3600)](https://maven-badges.herokuapp.com/maven-central/com.mapcode/mapcode)

**Copyright (C) 2014-2016, Stichting Mapcode Foundation (http://www.mapcode.com)**

This Java project contains a library to encode latitude/longitude pairs to mapcodes
and to decode mapcodes back to latitude/longitude pairs.

**Release notes: http://mapcode-foundation.github.io/mapcode-java/ReleaseNotes.html**

**Online documentation: http://mapcode-foundation.github.io/mapcode-java/**

**Latest stable release: https://github.com/mapcode-foundation/mapcode-java/releases**

**Example: https://github.com/mapcode-foundation/mapcode-java-example**

If you wish to use mapcodes in your own application landscape, consider using running an instance of the
Mapcode REST API, which can be found on: **https://github.com/mapcode-foundation/mapcode-rest-service**

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
of the mapcode library by Rijn Buve. Initial port by Matthew Lowden.

# Using the Mapcode Library

For a description of what mapcodes are, please visit http://mapcode.com.

This library exposes a `Mapcode` class, which represents a full mapcode, 
consisting of a code and a territory. You can extract the code and territory
with methods like `getCode()` and `getTerritory()`. Codes may be retrieved 
in a multitude of scripts (or 'alphabets', as they are called in this
library), such as Roman, Greek, Hindi and Arabic.

`Mapcode` objects are returned by the `MapcodeCodec` with encodes coordinates
to mapcodes and decodes mapcodes to coordinates (codec means coder/decoder).

# Examples

Here's an example to `decode()` mapcode within a given territory context.
Note that the territory context (`NLD` is this case) is only used to
disambiguate the code if needed. If the provided code is an international
code, the context is simply ignored, because no disambiguation is needed.

    final Territory territory = Territory.fromString("NLD");
    final String mapcode = "49.4V";
    try {
        final Point p = MapcodeCodec.decode(mapcode, territory);
        // p now points at the (lat, lon) for the mapcode.
    } catch (final UnknownMapcodeException ignored) {
        // The mapcode was not valid.
    }
  
And to `encode()` a latitude and longitude into a mapcode you would write:

    final List<Mapcode> results = MapcodeCodec.encode(lat, lon);

As you see, the returned result is actually a list of mapcodes. That's 
because many locations have multiple mapcodes. The last code in the list
is always the longest code, the international code. This means this method
always returns a result (as for every location on Earth, there is at least
one mapcode).

So, the last code is the international code, but notice that the first code 
is not always the shortest, nor does it need  to have the "correct"
territory associated to it.

If you want to get the shortest code for a coordinate, you should should
specify a territory and use `encodeToShortest` like this:

    final Territory territory = Territory.NLD;
    final Mapcode mapcode = encodeToShortest(lat, lon, territory);
    
This makes sure you get the shortest code for a coordinate which has
territory `NLD` (or whatever you choose the territory to be).

This method may fail, as the specified coordinate may not lie in the
specified territory. In that case a `UnknownMapcodeException` is thrown.

The `Mapcode` class offers some additional methods to, for example, get
high-resolution mapcode strings.

    // Retrieve an ultra-high precision mapcode.
    final String highRez = mapcode.getCode(8);

Or to get full mapcode strings (Unicode), in different scripts (or alphabets):

    // Get a Roman version of the mapcode.
    final String roman = mapcode.getCodeWithTerritory(Alphabet.ROMAN);

    // Get an Arabic version of the mapcode.
    final String arabic = mapcode.getCodeWithTerritory(Alphabet.ARABIC);

Note that mapcodes from different scripts can be passed as strings back
to the decoder and they will 'just work'.

There's also a `Territory` class, which allows you get territory codes,
given their ISO 3-character code, or their full names (or even some
supported aliases).

    final Territory territory = Territory.fromString("France");
    // territory is now Territory.FRA.
    
Finally, there is a `Point` utility class, which represents coordinates
with a latitude and longitude. The class offers some simple utility methods
like `distanceInMeters` to calculate the distance between two points. Note 
that this method is only accurate for pretty short distances, up to, say
a couple of hundred kilometers.

The `Point` class is usually easier to use than individual latitude and
longitude paramters and class makes sure it always wraps latitudes to
a range of `[-90, 90]` and longitude to `[-180, 180>`. 

# Coding Formatting Style

The code formatting style used is the default code formatting style from IntelliJ IDEA (version 14).
it is recommended to always use auto-format on any (Java) files before committing to maintain consistent layout.

# Using Git and `.gitignore`

It's good practice to set up a personal global `.gitignore` file on your machine which filters a number of files
on your file systems that you do not wish to submit to the Git repository. You can set up your own global
`~/.gitignore` file by executing:
`git config --global core.excludesfile ~/.gitignore`

In general, add the following file types to `~/.gitignore` (each entry should be on a separate line):
`*.com *.class *.dll *.exe *.o *.so *.log *.sql *.sqlite *.tlog *.epoch *.swp *.hprof *.hprof.index *.releaseBackup *~`

If you're using a Mac, filter:
`.DS_Store* Thumbs.db`

If you're using IntelliJ IDEA, filter:
`*.iml *.iws .idea/`

If you're using Eclips, filter:
`.classpath .project .settings .cache`

If you're using NetBeans, filter:
`nb-configuration.xml *.orig`

The local `.gitignore` file in the Git repository itself to reflect those file only that are produced by executing
regular compile, build or release commands, such as:
`target/ out/`

# Bug Reports and New Feature Requests

If you encounter any problems with this library, don't hesitate to use the `Issues` session to file your issues.
Normally, one of our developers should be able to comment on them and fix.
