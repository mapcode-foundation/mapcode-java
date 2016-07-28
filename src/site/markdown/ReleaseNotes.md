# Release Notes

These are the release notes for the Java library for mapcodes.

### 2.2.4

* Fix error to info logging in `aeuUnpack`.

* Updated all POM dependencies.

* Updated copyright messages.

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
