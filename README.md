# exif读取:

https://github.com/sephiroth74/Android-Exif-Extended  移植到java

```java
ExifInterface exif = new ExifInterface();
exif.readExif( filename, ExifInterface.Options.OPTION_ALL );

// list of all tags found
List<ExifTag> all_tags = exif.getAllTags();

// jpeg quality
int jpeg_quality =  exif.getQualityGuess()

// image size
int[] imagesize = exif.getImageSize();

// process used to create the jpeg file
short process = exif.getJpegProcess();

// gps lat-lon
double[] latlon = exif.getLatLongAsDoubles();


....
// save
exif.writeExif( src_file, dst_file );
```







# 原图尺寸不变,压缩到某个质量,并把exif写入:

```java
ImageCompressor.compressToQuality(file.getAbsolutePath(),70);
```











_*October 17, 2020: Thumbnailator 0.4.13 has been released!
See [Changes](https://github.com/coobird/thumbnailator/wiki/Changes) for details.*_

_*Thumbnailator is now available through
[Maven](https://github.com/coobird/thumbnailator/wiki/Maven)!*_

# What is Thumbnailator?

![](https://raw.githubusercontent.com/wiki/coobird/thumbnailator/img/home/home-image.png)

_Thumbnailator_ is a thumbnail generation library for Java.

# Why Thumbnailator?
Making high-quality thumbnails in Java can be a fairly difficult task.

Learning how to use the Image I/O API, Java 2D API, image processing,
image scaling techniques, ... but fear not! _Thumbnailator_ will take care
of all those things for you!

Thumbnailator is a single JAR file with no dependencies to external libraries,
making development and deployment simple and easy. It is also available on
the Maven Central Repository for easy inclusion in Maven projects.

# How simple is Thumbnailator?

_Thumbnailator_'s fluent interface can be used to perform fairly complicated
thumbnail processing task in one simple step.

For example, creating JPEG thumbnails of image files in a directory, all
resized to a maximum dimension of 640 pixels by 480 pixels while preserving
the aspect ratio of the original image can be performed by the following:

```
Thumbnails.of(new File("path/to/directory").listFiles())
    .size(640, 480)
    .outputFormat("jpg")
    .toFiles(Rename.PREFIX_DOT_THUMBNAIL);
```

The fluent interface provided by the _Thumbnailator_ simplifies the task of
making thumbnails into a single method call!

No need to access the Image I/O API and manually manipulate `BufferedImage`s
through `Graphics2D` objects. _Thumbnailator_ does all of that for you.

# What can Thumbnailator do?

The following pages have more information on what _Thumbnailator_ can do:

* [Features](https://github.com/coobird/thumbnailator/wiki/Features)
* [Examples](https://github.com/coobird/thumbnailator/wiki/Examples)
* [Thumbnailator API Documentation](https://coobird.github.io/thumbnailator/javadoc/0.4.13/)
* [Frequently Asked Questions](https://github.com/coobird/thumbnailator/wiki/FAQ)

# Disclaimer
*Thumbnailator is still early in its development, and the APIs are subject to
change at any time.*

