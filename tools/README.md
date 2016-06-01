Commandline Tools
=================

These sets of command line tools let you work with streams in a Stroom installation from your command line.
They are all written in Python and have been tested under 2.7.10 on OS X.
You can run each tool without any arguments to see what arguments they take, but common for them all is --host and --port to specify the location of your Stroom instance. If you do not specify these, they will default to 127.0.0.1:8080 so they will work out of the box with a standard Stroom installation on your local machine.

sd_list
-------

Lists all streams on a Stroom instance.

sd_read
-------

Reads a streams on a Stroom instance and writes the contents to standard out with one object pr line.

sd_write
--------

Takes input from standard in and posts it to a stream on a Stroom instance.

sd_load
-------

Takes a local file containing JSON data and loads it into a stream on a Stroom instance using batches.
The file must contain either an object on each line or an array of objects on each line.

If each line contains an array of objects, sd_load will parse the json so that the objects can be submitted in appropriately sized batches to Stroom (the batch size defaults to 500 but can be customized). This parsing is very slow and will give you much worse performance than if you were loading a file with individual objects on each line. However, you can get a 2x speedup by installing UltraJSON for Python.

https://github.com/esnme/ultrajson