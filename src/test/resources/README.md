# Creating Unit Test Reference Files

## Creating the Mapcode Utility

To create the test files, you will first need to build the 'mapcode' utility
from the "Mapcode C++" library on Github: https://github.com/mapcode-foundation/mapcode-cpp

You find the tool in the `utility` directory and compile it with:

    cd utility
    gcc mapcode.cpp -o -mapcode

## Creating Mapcode Test Sets

These test files have been created with the commands below.

Note: The syntax of the 'mapcode' command is:

    mapcode --random nrOfPoints [extraDigits] [seed]
    mapcode --grid   nrOfPoints [extraDigits]
    mapcode --boundaries        [extraDigits]

The parameters extraDigits is 8 means the highest precision codes are generated.
The unit tests will use cut-off version of these codes to check lower precisions
as well.

    mapcode --random 1000    8 1234 > random_e8_1k.txt
    mapcode --random 10000   8 1234 > random_e8_10k.txt
    mapcode --random 100000  8 1234 > random_e8_100k.txt
    mapcode --grid   1000    8      > grid_e8_1k.txt
    mapcode --grid   10000   8      > grid_e8_10k.txt
    mapcode --grid   100000  8      > grid_e8_100k.txt
    mapcode --boundaries     8      > boundaries_e8.txt
    
    for f in *.txt; do split -l 200000 -a 1 $f $f.; rm $f; done

**IMPORTANT:** Please note that an explicit seed `1234` is used for the generation of
random data sets, so they can be compared across different version of the software.

After generating the files, they must be split into smaller pieces (in this case
5Mb chunks) to allow uploading to Github or other source code control systems,
hence the `split` command.

## Reference File Format

The reference files are the following format:

    <number-of-mapcode-aliases> <lat-deg> <lon-deg> <x> <y> <z>
    <territory-code> <mapcode>      (repeated for every alias)
                                    (empty line)
    ...                             (repeated for every record)

Example:

    1 0.043244999999998867679 0.057660000000000266596
    HHHH1.CXRC-MMLH2S9M
    
    6 41.851944000000003143 12.433113999999999777
    ITA 0Z.0Z-00000000
    ITA G5.20M-X0230000
    ITA 2MC.29K-JVDRH4CV
    ITA 65C.X5QK-CPNRRNPN
    ITA J0QN.7X4-B1R2S4P3
    TJKM1.D2Z6-L1N8FHY0

You can use the tool Processing (http://processing.org) with the application
in `show3d` to visualize the generated data.
