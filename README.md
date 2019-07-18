# Mapcode Library for Java

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/0c9f11645f504b50bd154a80fb95a5c3)](https://www.codacy.com/app/rijnb/mapcode-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mapcode-foundation/mapcode-java&amp;utm_campaign=Badge_Grade)
[![Build Status](https://img.shields.io/travis/mapcode-foundation/mapcode-java.svg?maxAge=3600&branch=master)](https://travis-ci.org/mapcode-foundation/mapcode-java)
[![Coverage Status](https://coveralls.io/repos/github/mapcode-foundation/mapcode-java/badge.svg?branch=master&maxAge=3600)](https://coveralls.io/github/mapcode-foundation/mapcode-java?branch=master)
[![License](http://img.shields.io/badge/license-APACHE2-blue.svg)]()
[![Release](https://img.shields.io/github/release/mapcode-foundation/mapcode-java.svg?maxAge=3600)](https://github.com/mapcode-foundation/mapcode-java/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.mapcode/mapcode.svg?maxAge=3600)](https://maven-badges.herokuapp.com/maven-central/com.mapcode/mapcode)

**Copyright (C) 2014-2019, Stichting Mapcode Foundation (http://www.mapcode.com)**

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


# Contents

1. [What is a Mapcode?](#intro)
1. [Examples](#examples)
1. [Using the Mapcode Library](#library)
    1. [`MapcodeCodec`](#mapcodec)
    1. [`Mapcode`](#mapcode)
    1. [`Territory`](#territory)
    1. [`Alphabet`](#alphabet)
    1. [`Point`](#point)
    1. [`Rectangle`](#rectangle)
1. [Release Notes](#releasenotes)


# What Is A Mapcode? <a name="intro"></a>

A mapcode represents a location. Every location on Earth can be represented by a mapcode. Mapcodes
were designed to be short, easy to recognise, remember and communicate. They are precise to a few
meters, which is good enough for every-day use.


## Mapcodes Are Free! 

Mapcodes are free. They can be used by anyone, and may be supported, provided or generated by anyone,
as long as this is done free of charge, conditions or restrictions. Technical details and sources are
available on our developers page.


## what Does A Mapcode Look Like?

A mapcode consists of two groups of letters and digits, separated by a dot. An example of a mapcode is

    49.4V

This is sufficient as long as it is clear what country or state the mapcode belongs in. On a business card,
it is therefore a good idea to put it after the country or state name:

    John Smith
    Oosterdoksstraat 114
    Amsterdam
    Netherlands 49.4V

When storing mapcodes in a database, it is recommended to explicitly specify the country:

    Netherlands 49.4V

or via the standard 3-letter abbreviation:

    NLD 49.4V

In eight very large countries (The USA, Canada, Mexico, Brazil, India, Australia, Russia, and China),
an address has little meaning without knowing the state (just like elsewhere, an address has little meaning
without knowing the country). For example, there are 27 cities called Washington in the USA. If you want to
refer to a location in the capital city, you would always refer to "Washington DC".

    DC 18.JQZ

or (in an international database):

    US-DC 18.JQZ

More information on mapcodes and their underlying concepts can be found in our reference material.


## Where Did Mapcodes Come From?

Mapcodes were developed in 2001 by Pieter Geelen and Harold Goddijn, soon after the GPS satellite signals
were opened up for civilian use. It was decided to donate the mapcode system to the public domain in 2008.
The algorithms and data tables are maintained by the Stichting Mapcode Foundation.

The mapcode system is being filed as a standard at the International Organisation for Standardisation.


# Examples <a name="examples"></a>

For a description of what mapcodes are, please visit http://mapcode.com.

This library exposes a `Mapcode` class, which represents a full mapcode, 
consisting of a code and a territory. You can extract the code and territory
with methods like `getCode()` and `getTerritory()`. Codes may be retrieved 
in a multitude of scripts (or 'alphabets', as they are called in this
library), such as Roman, Greek, Hindi and Arabic.

`Mapcode` objects are returned by the `MapcodeCodec` with encodes coordinates
to mapcodes and decodes mapcodes to coordinates (codec means coder/decoder).

Here's an example to `decode()` mapcode within a given territory context.
Note that the territory context (`NLD` is this case) is only used to
disambiguate the code if needed. If the provided code is an international
code, the context is simply ignored, because no disambiguation is needed.

```
    final Territory territory = Territory.fromString("NLD");
    final String mapcode = "49.4V";
    try {
        final Point p = MapcodeCodec.decode(mapcode, territory);
        // p now contains the (lat, lon) for mapcode "49.YV".
    } catch (final UnknownMapcodeException ignored) {
        // The mapcode was not valid.
    }
``` 
 
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


# Using the Mapcode Library <a name="library"></a>

Welcome to the Java library to handle mapcodes. The original C library was created by Pieter Geelen. 
The initial port to Java and speed-ups were done by Matthew Lowden. 
Rijn Buve has developed and contributed to the Java version of the Mapcode library since, providing
a simple and consistent API for other developers. He has also built a number of applications using
this library, which can also be found in the Github respositories of the Mapcode Foundation.


## How To Build The Mapcode Library JAR File

The sources for the Mapcode Library for Java contain everything to build the Mapcode JAR file as well
as a significant number of unit tests to verify the correctness of the implementation against the
reference C implementation.

The library requires a minimum Java language revision level 6, but has been tested and verified to work
with JDK 1.6, JDK 1.7 and JDK 1.8.

First, make sure you have the correct file encoding (UTF8) set for Java on your system.
Include this environment variable in your `.profile` or `.bashrc`:

    export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF8"

To build the library:

    cd <MAPCODE-HOME>
    mvn clean install

This produces a JAR file in your local Maven repository at `~/.m2/repository/com/mapcode/mapcode/<version>/`
You can include this JAR in your project, or store it in your local Nexus repository, for example.

If you create a Maven project, much simpler than building the library yourself, is to include it from
Maven Central, adding this dependency to your `pom.xml`:

    <dependency>
        <groupId>com.mapcode</groupId>
        <artifactId>mapcode</artifactId>
        <version>{fill in latest version}</version>
    </dependency>

The latest official version of the libray on Maven Central can be found [**here**](http://search.maven.org/#search%7Cga%7C1%7Cmapcode).

## How To Use This Library In Your Application

There are two classes you interact with as a client of the Mapcode Library. These are:

    MapcodeCodec.java
    Mapcode.java
    Point.java


### Class `MapcodeCodec` <a name="mapcodec"></a>

This class contains the **encoder** and **decoder** for mapcodes. The class exposes two methods (with some
variants): `encode` and `decode`.

The **encoder** encodes a (latitude, longitude) pair into a result set of mapcodes. A single (latitude,
longitude) pair may produce multiple mapcodes, some of which are known as **local** mapcodes
(which are only unique within a given territory) and one which is globally unique in the entire world.

The **decoder** decodes a local or world-wide mapcode string into a (latitude, longitude) pair. For
local mapcodes a territory may be specified which is used to disambiguate the mapcode resolution.

Note that encoding a (latitude, longitude) pair to a mapcode and then decoding it may result in a
slightly offset position, as the mapcodes have a limited precision. The library offers "high-precision"
mapcodes as well, but you van never assume the resulting latitudes and longitudes to exactly match the
original input.

**`List<Mapcode> encode(double latitude, double longitude)`** encodes a (latitude, longitude) pair.

Example:

    double lat = 52.376514;
    double lon = 4.908542;
    List<Mapcode> results = MapcodeCodec.encode(lat, lon); 
    // Returns a non-empty list of results.

This produces a non-empty list of resulting mapcodes. The shortest (potentially local) mapcodes is always
the first mapcodes in the list. The last mapcode in the list is always the
globally unique international mapcode.

**`List<Mapcode> encode(double latitude, double longitude, Territory territory)`** encodes a (latitude,
longitude) pair, where encoding is restricted to a specific territory. 
    
Example:

    List<Mapcode> results = MapcodeCodec.encode(lat, lon, Territory.NLD);
    // Returns an empty list of results if the location is not within territory NLD.

This resticts encoding to a specific territory and produces a potentially empty list of results.
Again, if encoding succeeded, the first mapcode is the shortest one and the last mapcode in the list is the
globally unique international mapcode.

**`List<Mapcode> encodeRestrictToCountryISO2/ISO3(double latitude, double longitude, String countryISO2/ISO3)`** encodes a (latitude,
longitude) pair, where encoding is restricted to a specific country, provided as an ISO 3166 2 or 3 characters country code. 
    
Example:

    List<Mapcode> results = MapcodeCodec.encodeRestrictToCountryISO2(lat, lon, "BR");
    // Returns a list of mapcodes retricted to Brazil, so their territories start with BR- or BRA.

    List<Mapcode> results = MapcodeCodec.encodeRestrictToCountryISO3(lat, lon, "MEX");
    // Returns a list of mapcodes retricted to Mexico, so their therritories start with MX- or MEX.

**Important notice:** The codes used in these methods asume the ISO conversion, 
not the `fromString` conversion from `Territory`. For example, `Territory.fromString("BR")` 
produce the territory `IN-BR`, whereas `Territory.fromCountryISO2("BR")` produces 
the territory `BRA`. 

Both `encode()` methods are also offered as a `encodeToShortest()` method, which essentially
returns only the first result of the previous methods (if there are any results).

    Mapcode result = MapcodeCodec.encodeToShortest(lat, lon);
    // Always returns a mapcode (or valid lat and lon values).

    try {
        Mapcode result = MapcodeCodec.encodeToShortest(lat, lon, Territory.NLD);
        // This may fail.
    }
    catch (UnknownMapcodeException e) {
        // If the location is not within the territory, this exception is thrown.
    }

**`Point decode(String mapcode)`** decodes a mapcode to a `Point` which contains a location. Example:

    Point p = MapcodeCodec.decode("NLD 49.4V");

**`Point decode(String mapcode, Territory territory)`** decodes a mapcode to a `Point` which contains a
location, where the mapcode must be located within a specific territory.

Examples of usage:

    Point p = MapcodeCodec.decode("49.4V", Territory.NLD);


## Class `Mapcode` <a name="mapcode"></a>

This class represents mapcodes, which consist of a string of characters, digits and a decimal point and
a territory specification. The territory specification is required for national (local) mapcodes, which
are not globally unique.

The class also exposes methods to convert mapcodes to proper mapcode strings, usable for printing and
it allows string-formatted mapcodes to be converted to `Mapcode` objects, where territory information
is properly parsed and converted to a `Territory` enumeration value.

**`String getCode()`** returns the mapcode string which does not include territory information. You can also
use `getCode(1)` and `getCode(2)` for more precision, but longer mapcodes.

The default precision offered by `getCode()` is approximately 10m (maximum distance to latitude,
longitude the mapcode decodes to). This corresponds to an area of 20m x 20m (400m2). These mapcodes include
no additional precision digits.

The precision offered by `getCode(1)` is approximately 2m.
This corresponds to an area of 4m x 4m (16m2). These mapcodes include 1 additional precision digit.

The precision offered by `getCode(2)` is approximately 0.40m. This corresponds to an area
of 0.80m x 0.80m (0.64m2). These mapcodes include 2 additional precision digits.

This goes up to `getCode(8)`, which provides nanometer accuracy. (Please note one of the main advantages
of mapcodes over WGS84 coordinates is their simplicity and short size, so try to use as little precision 
as required for your application...) 

**`Territory getTerritory()`** returns the territory information.

**`toString()`** and **`getCodeWithTerritory()`** return mapcodes string with territory information,
specified as a ISO code.


## Enum `Territory` <a name="territory"></a>

This enum defines the territories for which local mapcodes are defined. The added benefit of using
local mapcodes over international mapcodes is simply that they are shorter and easier to remember.

Rather than writing `WLR9B.RP9P` (to locate a park in Moscow) you can use `MOW HG.4L` (or `MOW НГ.4Л` 
in Cyrillic). And most of the time you can even omit the prefix `MOW`, as in many practical situations 
the territory is given implicitly by the context of usage.

Note that in this case `MOW` is not really a territory, but a sub-territory of `RUS`. It's full
name is `RU-MOW`. 

The following territories are subdivided into subterritories to make sure the territory codes represent
smaller areas, so mapcodes can remain fairly short:

* `USA`, `US-XXX`: USA
* `IND`, `IN-XXX`: India
* `CAN`, `CA-XXX`: Canada
* `AUS`, `AU-XXX`: Australia
* `MEX`, `MX-XXX`: Mexico
* `BRA`, `BR-XXX`: Brasil
* `RUS`, `RU-XXX`: Russia
* `CHN`, `CN-XXX`: China

Rather than using the 3-letter territory code for mapcodes in these territories, you'd probably want
to use the `TT-XXX` form, where `XXX` defines the subterritory (state, province, etc.) 

Two convenience methods are provided to create a territory code from an ISO 3166 2 or 3 character code: 
`Territory.fromCountryISO2(String)` and `Territory.fromCountryISO3(String)`. 


## Enum `Alphabet` <a name="alphabet"></a>

This enum defines the alphabets, or rather scripts, that are supported by the Mapcode Library.
Encoding mapcodes procudes a Unicode string and this enum can be used to identify the script
for the result.

Note that the character mapping between scripts is based on similarity in appearance, so mapcodes
in different scripts can be remembered more easily with the help of your visual memory.


## Class `Point` <a name="point"></a>

This class represents (latitude, longitude) locations. It offers methods to create locations using
degrees.

**`Point fromDeg(double latitude, double longitude)`** returns a `Point` for a given (latitude, longitude)
pair. Note that latitudes are always between **-90** and **90**, and longitudes are 
always between **-180** and **180** (non-inclusive) whenreturned. 
However, values outside these range are correctly limited (latitude) or wrapped (longitude) to these ranges
when supplied to the class.

The methods **`double getLat()`** and **`getLon()`** return the latitude and longitude respectively, in degrees.


## Class `Rectangle` <a name="point"></a>

This class represents a geospatial rectangle. This class only accurately represents areas on the
surface of the Earth for small rectangles (as the curvature of the surface is not taken into account).

It is used to return the bounding box for a given mapcode. Beware: bounding boxes of mapcodes in a single 
territory and of the same length do not overlap, but others may. The bounding boxes of international
mapcodes do not overlap with each other and they are smaller territorial mapcodes (but the codes are 
longer).
 

## Code Style Settings for IntelliJ IDEA

The Java code uses the *default* [JetBrains IntelliJ IDEA](https://www.jetbrains.com/idea) 
code style settings for Java, with one exception:
code blocks are always surround by `{...}` and on separate lines.


## Using Git and `.gitignore`

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


## Bug Reports and New Feature Requests

If you encounter any problems with this library, don't hesitate to use the `Issues` session to file your issues.
Normally, one of our developers should be able to comment on them and fix.


# Release Notes <a name="releasenotes"></a>
mat
These are the release notes for the Java library for mapcodes.

### 2.4.13

* Minor code cleanup.

### 2.4.12

* Added unit tests to check internal data structures.

### 2.4.11

* Fixed a bug in `Mapcode.isValidMapcodeFormat()` which caused an exception when parsing a Unicode mapcode
which included a numeral, like `THA จก.ผถฉ๕` (which is `THA GB.RMH5` in non-Unicode). 

### 2.4.10

* Changed `Mapcode.isValidMapcodeFormat()` to return `false` if the string is `null`. Trimming the string is also
no longer needed.

### 2.4.9

* Updated dependencies for security patches.

### 2.4.8

* Updated dependencies for security patches.

### 2.4.7

* Added `fromCountryISO/2/3` methods to get a `Territory` value from a 2- or 3-character ISO 3166 string.

* Added `allCountryISO2/3Codes` to get a set of all 2- or 3-character ISO 3166 codes.

* Added `encodeRestrictToCountryISO` to restrict mapcodes to a 2- or 3-character ISO 3166 country.

### 2.4.6

* General cleanup after running stricter IntelliJ inspections profile.

* Added convenience methods to restrict encoded mapcodes to specific ISO 3166 2 or 3 character country codes.

### 2.4.5

* Remove hard reference to `log4j` for production. Left only for unit tests.

### 2.4.4

* Added calls to decode an international or territorial mapcode to its encompassing
rectangle using `decodeToRectangle`. 

* Minor code hygiene improvements.

* Moved all documentation to `README.md`.  

### 2.4.3

* Updated Maven dependencies for latest patches.

### 2.4.2

* Removed secret Coveralls key from POM file.
 
### 2.4.1

* Added scripts for Tifinagh (Berber), Tamil, Amharic, Telugu, Odia, Kannada, Gujarati.

* Added `getAlphabets()` to `Territory` class, returning the most commonly used languages for the territory.

* Renamed constant `HINDI` to `DEVANAGIRI`.

* Improved some characters for Arabic and Devanagari.

* Fixed Bengali to also support Assamese.

### 2.4.0

* Added scripts for Korean (Choson'gul/Hangul), Burmese, Khmer, Sinhalese, Thaana (Maldivan), Chinese (Zhuyin, Bopomofo).

* Renamed constant `MALAY` to `MALAYALAM`.

### 2.3.1

* Fixed data for some parts of China.

### 2.3.0

* Added Arabic support.

* Fixed Greek, Hebrew and Hindi support.

### 2.2.5

* Updated documentation.

* Cleaned up POM, sorted dependencies.

### 2.2.4

* Added Travis CI and Coveralls badges to `README.md`. 

* Replaces static `DataAccess` class with singleton `DataModel` to allow testing
of incorrect data model files.

* Fixed error handling for incorrect data model files.
 
* Fix error to info logging in `aeuUnpack`.

* Updated all POM dependencies.

* Updated copyright messages.

* Improved test coverage of unit tests.

### 2.2.3

* Issue #23: Fixed `Territory.fromString` to make sure the parent territory is valid for
  input like "CHE-GR". This returned "MX-GRO" instead of throwing `UnknownTerritoryException`.
  Added unit test for this type of case.

* Fixed minor JavaDoc issues.

### 2.2.2

* Fixed error in `Point` which in rare cases would allow longitudes outside proper range.

### 2.2.1

* Fixed unit test. Reduced size of files for unit tests considerably. Improved unit test speed.

* Fixed `Point` interface.

* Cleaned up `Boundary` and `DataAccess`.

### 2.2.0

* Solved 1-microdegree gap in a few spots on Earth, noticable now extreme precision is possible.

* Replaced floating point by fixed point math.

* Improved speed.

* Enforce `Mencode(decode(M)) == M`, except at territory border corners.

* Cleaned up source; moved hard-coded data into `mminfo.dat`.

### 2.1.0

* Added micro-meter precision (mapcodes can now have eight precision digits).

* Assure that encode(decode(m)) delivers m.

* Renames to bring source more in line with other implementations.

### 2.0.2

* Renamed `isValidPrecisionFormat` to `isValidMapcodeFormat`.

* Removed public microdegree references from `Point` class. Everything is degrees now.

* Removed `ParentTerritory` class.

### 2.0.1

* Reverted Java JDK level to 1.6 (Java 6) from 1.8 (Java 8), so the library can be used on
  Android platforms operating at Java 6 as well.

* Use multi-threading for long running test to speed them up (uses all CPU cores now).

* Added the ability to use a country name for `Territory.fromString()`.

### 2.0.0

* Fixes to the data rectangles (primarily intended for ISO proposal).

* Removed functionality to use numeric territory codes; only alpha codes are accepted.

* Note that this release only allows high-precision mapcodes up to 2 additional suffix characters.
A future release will be scheduled to allow up to 8 suffix characters (nanometer accuracy).

### 1.50.3

* This release breaks compatiblity with earlier releases, to clean up the interface significantly.

* Removed `Mapcode.encodeToShortest(lat, lon))` as this will produce a randomly chosen territory.
  You must specify a `restrictToTerritory` now.

* Renamed `Territory.code` to `Territory.number`.

* Renamed `fromTerritoryCode())` to `fromNumber())`.

* Renamed `Territory.isState())` to `Territory.isSubdivision())` and

* Renamed `Territory.hasStates())` to `Territory.hasSubdivision())`.

* Renamed `Alphabet.code` to `Alphabet.number`.

* Renamed `fromCode())` to `fromNumber())`.

* Renamed `MapcodeFormat` to `PrecisionFormat`.

* Deprecated methods have been removed.

### 1.50.2

* Cleaned up Unicode handling a bit.

* Speed up of reading initialization data.

* Rename `toNameFormat` into `toAlphaFormat` and `NAME_FORMAT` to `ALPHA_FORMAT`.

### 1.50.1

* Bugfix for mapcodes in IN-DD (in India).

### 1.50

* Major release. This version is not backwards compatible with mapcode 1.4x: is has dropped support for
  Antartica AT0-8 codes and has a changed (improved) way of dealing with the Greek alphabet.

* Added 22-chararcter post-processing of all-digit mapcodes for the Greek alphabet.

* Retired legacy aliases EAZ and SKM, AU-QL, AU-TS, AU-NI and AU-JB.

* Retired legacy Antarctica claims AT0 through AT8.

* Added convencience methods for `MapcodeCodec` to accept `Point` for all encode functions
  as well (not just `latDeg`, `lonDeg`).

* Added alphabet support to convert mapcodes (both codes and territories) between `Alphabet`s.

* Exceptions have been corrected and documented in code.

* Allowed nullable values in `MapcodeCodec` encode and decode methods to assume reasonable defaults.

* Microdegrees are no longer support publicly in `Point`. Only degrees.

* Latitudes are limited to -90..90 and longitudes are wrapped to -180..180 (non inclusive).

### 1.42.3

* To be done.

### 1.42.2

* Upper- and lowercase mapcodes always allowed.

### 1.42.1

* Cleaned up source. Removed all pending IntelliJ IDEA inspection warnings and reformatted code
  using default IDEA code style to maintain consistent layout.

* Add additional unit tests to check for correct handling of international mapcode handling.

* Added safe constants for the maximum delta distance in meters for mapcode accuracy.

### 1.42

* Fixed a bug in `MapcodeCodec.encodeToShortest` which would not always return the shortest code (see
  next bullet). Reproducible with `curl -X GET http://localhost:8080/mapcode/to/47.1243/-111.28564/local`.

* Fixed a bug where `Encoder.encode` would sometime retrieve more than one result even if result set
  was limited to 1 result.

### 1.41.1

* Added convenience method to Mapcode.

### 1.41

* Added the India state Telangana (IN-TG), until 2014 a region in Adhra Pradesh.

* Updated POM dependencies to latest library versions of standard components.

### 1.40.3

* Minor code clean-up with no functional effect.

* (Issue #6) Removed non-project specific unwanted files out of `.gitignore`. These should be listed in the
developer's own global `~/.gitignore` file instead.

### 1.40.2

* Added `getMapcodeFormatType` and `isValidMapcodeFormat` to check validity of mapcode strings. Added
unit tests for these methods as well.

* Constructor of `Mapcode` now checks for validity of mapcode string.

* Added Unicode handling of high precision mapcodes and added check to throw an `IllegalArgumentException`
if the character 'Z' or equivalent Unicode character is contained in the high precision part according to
the Mapcode documenation.

* Added method `convertToAscii` which produces the ASCII, non-Unicode variant of a mapcode which contains
Unicode characters§.

### 1.40.1

* Deprecated names `getMapcodeHighPrecision` and `getMapcodeMediumPrecision`.
  Replaced those with `getMapcodePrecision1` and `getMapcodePrecision2`.

* Fixed all occurences of incorrectly cased Mapcode vs. mapcode.

### 1.40

* Renamed class `Mapcode` to `MapcodeCodec`.

* Renamed class `MapcodeInfo` to `Mapcode`.

* Added high precision Mapcodes, with methods `getMapcodeHighPrecision`

* Seriously reduced test set size.

* Replaced Unicode characters in source code to escapes.

* Added explicit character encoding to `pom.xml`.

* Fixed issues with decoder at some boundaries.

### 1.33.2

* Clean-up of release 1.33.2.

* Added release notes.

* Removed GSON dependency from production (now scope 'test' only).

* Added robustness with respect to Unicode characters.

### 1.33.1

* First release of Java library for MapCodes. Includes extensive test suite.
