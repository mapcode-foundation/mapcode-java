# Mapcode Library for Java

Copyright (C) 2014-2015 Stichting Mapcode Foundation (http://www.mapcode.com)

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

# Code Style Settings in IntelliJ IDEA

In order to get consistent code layout for Java files, source files should be reformatted in your IDE according to the 
settings below. These settings are describes as IntelliJ IDEA "Code Style" settings (and they can be copied directly 
into your `.idea/codeStyleSetting.xml` file, if you use IDEA):

```
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ProjectCodeStyleSettingsManager">
    <option name="PER_PROJECT_SETTINGS">
      <value>
        <option name="OTHER_INDENT_OPTIONS">
          <value>
            <option name="INDENT_SIZE" value="4" />
            <option name="CONTINUATION_INDENT_SIZE" value="4" />
            <option name="TAB_SIZE" value="8" />
            <option name="USE_TAB_CHARACTER" value="false" />
            <option name="SMART_TABS" value="false" />
            <option name="LABEL_INDENT_SIZE" value="0" />
            <option name="LABEL_INDENT_ABSOLUTE" value="false" />
            <option name="USE_RELATIVE_INDENTS" value="false" />
          </value>
        </option>
        <option name="LINE_SEPARATOR" value="&#10;" />
        <option name="CLASS_COUNT_TO_USE_IMPORT_ON_DEMAND" value="9999" />
        <option name="NAMES_COUNT_TO_USE_IMPORT_ON_DEMAND" value="9999" />
        <option name="IMPORT_LAYOUT_TABLE">
          <value>
            <package name="" withSubpackages="true" static="false" />
            <emptyLine />
            <package name="javax" withSubpackages="true" static="false" />
            <package name="java" withSubpackages="true" static="false" />
            <emptyLine />
            <package name="com.tomtom" withSubpackages="true" static="false" />
            <emptyLine />
            <package name="" withSubpackages="true" static="true" />
          </value>
        </option>
        <option name="WRAP_WHEN_TYPING_REACHES_RIGHT_MARGIN" value="true" />
        <option name="JD_P_AT_EMPTY_LINES" value="false" />
        <option name="JD_KEEP_EMPTY_PARAMETER" value="false" />
        <option name="JD_KEEP_EMPTY_EXCEPTION" value="false" />
        <option name="JD_KEEP_EMPTY_RETURN" value="false" />
        <option name="JD_PRESERVE_LINE_FEEDS" value="true" />
        <option name="KEEP_CONTROL_STATEMENT_IN_ONE_LINE" value="false" />
        <option name="BLANK_LINES_BEFORE_PACKAGE" value="1" />
        <option name="BLANK_LINES_AFTER_IMPORTS" value="3" />
        <option name="BLANK_LINES_AROUND_CLASS" value="3" />
        <option name="BLANK_LINES_AROUND_METHOD_IN_INTERFACE" value="0" />
        <option name="ELSE_ON_NEW_LINE" value="true" />
        <option name="WHILE_ON_NEW_LINE" value="true" />
        <option name="CATCH_ON_NEW_LINE" value="true" />
        <option name="FINALLY_ON_NEW_LINE" value="true" />
        <option name="ALIGN_MULTILINE_PARAMETERS" value="false" />
        <option name="ALIGN_GROUP_FIELD_DECLARATIONS" value="true" />
        <option name="CALL_PARAMETERS_WRAP" value="1" />
        <option name="RESOURCE_LIST_WRAP" value="1" />
        <option name="BINARY_OPERATION_WRAP" value="1" />
        <option name="TERNARY_OPERATION_WRAP" value="1" />
        <option name="KEEP_SIMPLE_METHODS_IN_ONE_LINE" value="true" />
        <option name="KEEP_SIMPLE_CLASSES_IN_ONE_LINE" value="true" />
        <option name="FOR_STATEMENT_WRAP" value="1" />
        <option name="ARRAY_INITIALIZER_WRAP" value="1" />
        <option name="ASSIGNMENT_WRAP" value="1" />
        <option name="WRAP_COMMENTS" value="true" />
        <option name="ASSERT_STATEMENT_WRAP" value="1" />
        <option name="IF_BRACE_FORCE" value="3" />
        <option name="DOWHILE_BRACE_FORCE" value="3" />
        <option name="WHILE_BRACE_FORCE" value="3" />
        <option name="FOR_BRACE_FORCE" value="3" />
        <option name="ENUM_CONSTANTS_WRAP" value="2" />
        <XML>
          <option name="XML_LEGACY_SETTINGS_IMPORTED" value="true" />
        </XML>
        <ADDITIONAL_INDENT_OPTIONS fileType="gsp">
          <option name="CONTINUATION_INDENT_SIZE" value="4" />
        </ADDITIONAL_INDENT_OPTIONS>
        <codeStyleSettings language="CSS">
          <indentOptions>
            <option name="CONTINUATION_INDENT_SIZE" value="4" />
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="CoffeeScript">
          <option name="ALIGN_MULTILINE_PARAMETERS" value="false" />
          <option name="ALIGN_MULTILINE_ARRAY_INITIALIZER_EXPRESSION" value="false" />
          <option name="ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE" value="true" />
          <option name="ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE" value="true" />
          <option name="PARENT_SETTINGS_INSTALLED" value="true" />
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="Groovy">
          <option name="KEEP_CONTROL_STATEMENT_IN_ONE_LINE" value="false" />
          <option name="BLANK_LINES_BEFORE_PACKAGE" value="1" />
          <option name="BLANK_LINES_AFTER_IMPORTS" value="3" />
          <option name="BLANK_LINES_AROUND_CLASS" value="3" />
          <option name="BLANK_LINES_AROUND_METHOD_IN_INTERFACE" value="0" />
          <option name="ELSE_ON_NEW_LINE" value="true" />
          <option name="CATCH_ON_NEW_LINE" value="true" />
          <option name="FINALLY_ON_NEW_LINE" value="true" />
          <option name="ALIGN_MULTILINE_PARAMETERS" value="false" />
          <option name="ALIGN_GROUP_FIELD_DECLARATIONS" value="true" />
          <option name="CALL_PARAMETERS_WRAP" value="1" />
          <option name="BINARY_OPERATION_WRAP" value="1" />
          <option name="TERNARY_OPERATION_WRAP" value="1" />
          <option name="FOR_STATEMENT_WRAP" value="1" />
          <option name="ASSIGNMENT_WRAP" value="1" />
          <option name="ASSERT_STATEMENT_WRAP" value="1" />
          <option name="IF_BRACE_FORCE" value="3" />
          <option name="WHILE_BRACE_FORCE" value="3" />
          <option name="FOR_BRACE_FORCE" value="3" />
          <option name="ENUM_CONSTANTS_WRAP" value="2" />
          <option name="PARENT_SETTINGS_INSTALLED" value="true" />
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="HOCON">
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="HTML">
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="JAVA">
          <option name="KEEP_CONTROL_STATEMENT_IN_ONE_LINE" value="false" />
          <option name="KEEP_BLANK_LINES_BEFORE_RBRACE" value="0" />
          <option name="BLANK_LINES_BEFORE_PACKAGE" value="1" />
          <option name="BLANK_LINES_AROUND_METHOD_IN_INTERFACE" value="0" />
          <option name="ELSE_ON_NEW_LINE" value="true" />
          <option name="WHILE_ON_NEW_LINE" value="true" />
          <option name="CATCH_ON_NEW_LINE" value="true" />
          <option name="FINALLY_ON_NEW_LINE" value="true" />
          <option name="ALIGN_MULTILINE_PARAMETERS" value="false" />
          <option name="ALIGN_GROUP_FIELD_DECLARATIONS" value="true" />
          <option name="CALL_PARAMETERS_WRAP" value="1" />
          <option name="RESOURCE_LIST_WRAP" value="1" />
          <option name="BINARY_OPERATION_WRAP" value="1" />
          <option name="TERNARY_OPERATION_WRAP" value="1" />
          <option name="FOR_STATEMENT_WRAP" value="1" />
          <option name="ARRAY_INITIALIZER_WRAP" value="1" />
          <option name="ASSIGNMENT_WRAP" value="1" />
          <option name="ASSERT_STATEMENT_WRAP" value="1" />
          <option name="IF_BRACE_FORCE" value="3" />
          <option name="DOWHILE_BRACE_FORCE" value="3" />
          <option name="WHILE_BRACE_FORCE" value="3" />
          <option name="FOR_BRACE_FORCE" value="3" />
          <option name="FIELD_ANNOTATION_WRAP" value="0" />
          <option name="PARENT_SETTINGS_INSTALLED" value="true" />
          <indentOptions>
            <option name="CONTINUATION_INDENT_SIZE" value="4" />
            <option name="TAB_SIZE" value="8" />
            <option name="LABEL_INDENT_ABSOLUTE" value="true" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="JSP">
          <indentOptions>
            <option name="CONTINUATION_INDENT_SIZE" value="4" />
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="JSPX">
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="JavaScript">
          <option name="ELSE_ON_NEW_LINE" value="true" />
          <option name="WHILE_ON_NEW_LINE" value="true" />
          <option name="CATCH_ON_NEW_LINE" value="true" />
          <option name="FINALLY_ON_NEW_LINE" value="true" />
          <option name="CALL_PARAMETERS_WRAP" value="1" />
          <option name="BINARY_OPERATION_WRAP" value="1" />
          <option name="TERNARY_OPERATION_WRAP" value="1" />
          <option name="KEEP_SIMPLE_METHODS_IN_ONE_LINE" value="true" />
          <option name="FOR_STATEMENT_WRAP" value="1" />
          <option name="ARRAY_INITIALIZER_WRAP" value="1" />
          <option name="ASSIGNMENT_WRAP" value="1" />
          <option name="IF_BRACE_FORCE" value="3" />
          <option name="DOWHILE_BRACE_FORCE" value="3" />
          <option name="WHILE_BRACE_FORCE" value="3" />
          <option name="FOR_BRACE_FORCE" value="3" />
          <option name="PARENT_SETTINGS_INSTALLED" value="true" />
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="Python">
          <option name="BLANK_LINES_AFTER_IMPORTS" value="3" />
          <option name="BLANK_LINES_AROUND_CLASS" value="3" />
          <option name="ALIGN_MULTILINE_PARAMETERS" value="false" />
          <option name="PARENT_SETTINGS_INSTALLED" value="true" />
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="Scala">
          <option name="KEEP_BLANK_LINES_IN_DECLARATIONS" value="1" />
          <option name="KEEP_BLANK_LINES_IN_CODE" value="1" />
          <option name="KEEP_BLANK_LINES_BEFORE_RBRACE" value="0" />
          <option name="BLANK_LINES_BEFORE_PACKAGE" value="1" />
          <option name="BLANK_LINES_AROUND_METHOD_IN_INTERFACE" value="0" />
          <option name="ELSE_ON_NEW_LINE" value="true" />
          <option name="WHILE_ON_NEW_LINE" value="true" />
          <option name="CATCH_ON_NEW_LINE" value="true" />
          <option name="FINALLY_ON_NEW_LINE" value="true" />
          <option name="ALIGN_MULTILINE_PARAMETERS" value="false" />
          <option name="ALIGN_GROUP_FIELD_DECLARATIONS" value="true" />
          <option name="CALL_PARAMETERS_WRAP" value="1" />
          <option name="BINARY_OPERATION_WRAP" value="1" />
          <option name="FOR_STATEMENT_WRAP" value="1" />
          <option name="DOWHILE_BRACE_FORCE" value="3" />
          <option name="WHILE_BRACE_FORCE" value="3" />
          <option name="FOR_BRACE_FORCE" value="3" />
          <option name="PARENT_SETTINGS_INSTALLED" value="true" />
          <indentOptions>
            <option name="INDENT_SIZE" value="4" />
            <option name="CONTINUATION_INDENT_SIZE" value="4" />
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="TypeScript">
          <option name="ELSE_ON_NEW_LINE" value="true" />
          <option name="WHILE_ON_NEW_LINE" value="true" />
          <option name="CATCH_ON_NEW_LINE" value="true" />
          <option name="FINALLY_ON_NEW_LINE" value="true" />
          <option name="ALIGN_MULTILINE_PARAMETERS" value="false" />
          <option name="CALL_PARAMETERS_WRAP" value="1" />
          <option name="BINARY_OPERATION_WRAP" value="1" />
          <option name="TERNARY_OPERATION_WRAP" value="1" />
          <option name="KEEP_SIMPLE_METHODS_IN_ONE_LINE" value="true" />
          <option name="FOR_STATEMENT_WRAP" value="1" />
          <option name="ARRAY_INITIALIZER_WRAP" value="1" />
          <option name="ASSIGNMENT_WRAP" value="1" />
          <option name="IF_BRACE_FORCE" value="3" />
          <option name="DOWHILE_BRACE_FORCE" value="3" />
          <option name="WHILE_BRACE_FORCE" value="3" />
          <option name="FOR_BRACE_FORCE" value="3" />
          <option name="PARENT_SETTINGS_INSTALLED" value="true" />
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="XML">
          <indentOptions>
            <option name="CONTINUATION_INDENT_SIZE" value="4" />
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
        <codeStyleSettings language="ruby">
          <option name="ALIGN_MULTILINE_PARAMETERS" value="false" />
          <option name="ALIGN_GROUP_FIELD_DECLARATIONS" value="true" />
          <option name="PARENT_SETTINGS_INSTALLED" value="true" />
          <indentOptions>
            <option name="TAB_SIZE" value="8" />
          </indentOptions>
        </codeStyleSettings>
      </value>
    </option>
    <option name="USE_PER_PROJECT_SETTINGS" value="true" />
    <option name="PREFERRED_PROJECT_CODE_STYLE" value="Clear Code Style" />
  </component>
</project>
```
