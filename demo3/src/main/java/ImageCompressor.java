import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;
import net.coobird.thumbnailator.Thumbnails;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageCompressor {

    static Map<Integer, String> tagDes = getDes();
    volatile static ExecutorService service;


    private static Map<Integer, String> getDes() {
        tagDes = new HashMap<Integer, String>();
        Class clazz = ExifInterface.class;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                boolean isTarget = Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers());
                if (!isTarget) {
                    continue;
                }
                String name = field.getName();
                if (name.startsWith("TAG_")) {
                    name = name.substring("TAG_".length()).toLowerCase();
                    tagDes.put((Integer) field.get(clazz), name);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
        return tagDes;

    }

    public static void compressImagesInDir(final String dirPath, final int quality) {
        if (service == null) {
            service = Executors.newFixedThreadPool(4);
        }

        System.out.println(Thread.currentThread().getName()+"->开始遍历文件夹: " + dirPath);
        service.execute(new Runnable() {
            @Override
            public void run() {
                File dir = new File(dirPath);
                File[] files = dir.listFiles();
                long start = System.currentTimeMillis();
                long sizeBefore = 0;
                long sizeAfter = 0;

                for (File file1 : files) {
                    if (file1.isDirectory()) {
                        compressImagesInDir(file1.getAbsolutePath(), quality);
                    } else {
                        if (file1.getName().endsWith(".jpg") || file1.getName().endsWith(".JPG")
                                || file1.getName().endsWith(".png")
                                || file1.getName().endsWith(".PNG")
                                || file1.getName().endsWith(".jpeg") || file1.getName().endsWith(".JPEG")) {
                            sizeBefore += file1.length();

                            ImageCompressor.compressToQuality(file1.getAbsolutePath(), quality);
                            sizeAfter += file1.length();
                            if (file1.length() == 0) {
                                System.out.println("压缩/拷贝失败:" + file1.getAbsolutePath());
                            }
                        }
                    }
                }
                System.out.println("文件夹压缩: cost:" + (System.currentTimeMillis() - start) / 1000 + "s,文件总大小:"
                        + ImageCompressor.formatFileSize(sizeBefore) + " -> "
                        + ImageCompressor.formatFileSize(sizeAfter)+"  文件夹路径:"+dirPath);
            }
        });

    }

    public static boolean compressToQuality(String path, int quality) {
        return compressToQuality(path, "", quality);
    }

    public static boolean compressToQuality(String path, String outPath, int quality) {
        if (tagDes == null || tagDes.isEmpty()) {
            tagDes = getDes();
        }

        try {
            System.out.println(Thread.currentThread().getName()+"->开始准备压缩: " + path);
            File file = new File(path);
            long originalSize = file.length();
            String name = file.getName();
            boolean isCompressToOtherPath = true;
            if (outPath == null || "".equals(outPath)) {
                outPath = path;
                isCompressToOtherPath = false;
            }

         /* String type =   MimeTable.loadTable().getContentTypeFor(name);
          if(type == null){
              System.out.println( "未知mime类型 : "+name);
              return true;
          }
          if(!type.contains("image")){
              System.out.println( "不是图片类型 : "+type+" , name: "+name);
              return true;
          }*/
            long start = System.currentTimeMillis();
            if (file.length() == 0) {
                System.out.println("文件0k,不存在 :" + path);
                return true;
            }
            if (file.length() < 50 * 1024) {
                System.out.println("文件小于50k,不压 :" + path);
                if (isCompressToOtherPath) {
                    ExifInterface.copyFile2(file, new File(outPath));
                }
                return true;
            }
            File out = null;
            boolean deleteOriginal = false;

            if (name.endsWith(".webp")) {
                System.out.println("webp 不压缩");
                if (isCompressToOtherPath) {
                    ExifInterface.copyFile2(file, new File(outPath));
                }
                return true;
            }
            if (name.endsWith(".gif")) {
                System.out.println("gif 不压缩");
                if (isCompressToOtherPath) {
                    ExifInterface.copyFile2(file, new File(outPath));
                }
                return true;
            }
            if (name.endsWith(".png") || name.endsWith(".PNG")) {
                path = path.substring(0, path.lastIndexOf(".")) + ".jpg";
                out = new File(path);
                if(!isCompressToOtherPath){
                    deleteOriginal = true;
                }

                System.out.println("png 压缩后更换后缀,并删除原文件:" + path);
            }
            if (out == null) {
                out = new File(outPath);
            }
            boolean[] hasExif = new boolean[]{false};
            ExifInterface exif = printExif(file,hasExif);

            //Metadata metadata = ImageMetadataReader.readMetadata(file);

                int jpeg_quality = exif.getQualityGuess();
                if (jpeg_quality <= quality && jpeg_quality > 0) {
                    System.out.println("图片质量>0且小于等于" + quality + ",无需压缩:" + file.getAbsolutePath());
                    if (isCompressToOtherPath) {
                        ExifInterface.copyFile2(file, new File(outPath));
                    }
                    return true;
                }



                String suffix = out.getName().substring(0,out.getName().lastIndexOf("."))
                        +"-comp"+out.getName().substring(out.getName().lastIndexOf("."));
            File tmp = new File(out.getParentFile(), suffix);
            tmp.createNewFile();
            Thumbnails.of(file)
                    .outputFormat("jpg")
                    .outputQuality(quality * 1.0f / 100f)
                    .scale(1.0)
                    .toFile(tmp);
            if (file.length() < tmp.length()) {
                System.out.println("压缩后反而变大,文件不要了,不压了: " + file.getAbsolutePath());
                tmp.delete();
                if (isCompressToOtherPath) {
                    ExifInterface.copyFile2(file, new File(outPath));
                }
                return true;
            }
            //new FileInputStream(tmp).readAllBytes();
            if (hasExif[0]) {
                ExifInterface exif2 = new ExifInterface();
                try {
                    exif.setTagValue(ExifInterface.TAG_ORIENTATION,0);
                    //exif2.readExif(path, ExifInterface.Options.OPTION_ALL);
                    exif2.setExif(exif.getAllTags());
                    System.out.println("写exif: getAllTags:" + exif.getAllTags().size());
                    exif2.writeExif(tmp.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //printExif(out);
            }

            System.out.println("文件拷贝:" + tmp.getAbsolutePath() + "\n->" + out.getAbsolutePath());
            //tmp拷贝到out,同时删除tmp
            boolean success1 = copyFile(tmp, out.getAbsolutePath());
            if (!success1) {
                System.out.println("文件拷贝失败,删除临时文件: " + tmp.getAbsolutePath());
                tmp.delete();
                if (isCompressToOtherPath) {
                    ExifInterface.copyFile2(file, new File(outPath));
                }
                return false;
            }
            tmp.delete();
            System.out.println("cost time: " + (System.currentTimeMillis() - start)
                    + " msec,file size:" + formatFileSize(originalSize) + "->" +
                    formatFileSize(out.length()) + ",reduce " + ((originalSize - out.length()) * 100 / originalSize) + "%," + path);
            if (deleteOriginal) {
                boolean success = file.delete();
                System.out.println("png 压缩后删除原文件 是否成功:" + success + " , path: " + path);
            }

            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }

    }

    public static String formatFileSize(long size) {
        try {
            DecimalFormat dff = new DecimalFormat(".0");

            if (size >= 1024 * 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024 * 1024);
                dff = new DecimalFormat(".00");
                String value = dff.format(doubleValue);
                return value + "G";
            } else if (size >= 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024);
                String value = dff.format(doubleValue);
                return value + "M";
            } else if (size > 1024) {
                double doubleValue = ((double) size) / 1024;
                String value = dff.format(doubleValue);
                return value + "K";
            } else {
                return size + "B";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(size);
    }


    private static ExifInterface printExif(File file,boolean[] hasExif) {
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(file.getAbsolutePath(), ExifInterface.Options.OPTION_ALL);
// list of all tags found
            List<ExifTag> all_tags = exif.getAllTags();
            if (all_tags != null) {
                //String tags = Arrays.toString(all_tags.toArray()).replace(",","\n");
                // System.out.println(file.getAbsolutePath() + "\n");


                for (ExifTag tag : all_tags) {
                    if (tag != null) {
                        int tagId = ExifInterface.defineTag(tag.getIfd(), tag.getTagId());
                        String str = tag.forceGetValueAsString();
                        if (str != null) {
                            str = str.trim();
                        }
                        System.out.println(tagDes.get(tagId) + " : " + str);
                        //System.out.println( tag.toString());

                    } else {
                        System.out.println("tag is null");
                    }
                }
            } else {
                System.out.println(file.getAbsolutePath() + "  no exif info");
            }
// jpeg quality
            int jpeg_quality = exif.getQualityGuess();

            System.out.println(file.getAbsolutePath() + "  quality:" + jpeg_quality);
            if (all_tags == null || all_tags.isEmpty()) {
                if(hasExif != null){
                    hasExif[0] = false;
                }


                return exif;
            }
            if(hasExif != null){
                hasExif[0] = true;
            }
            return exif;


        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if(hasExif != null){
                hasExif[0] = false;
            }
        }
        return exif;
    }

    public static Map<String, String> getExifInfo(File file) {
        ExifInterface exif = new ExifInterface();
        Map<String, String> map = new HashMap<String, String>();
        try {
            exif.readExif(file.getAbsolutePath(), ExifInterface.Options.OPTION_ALL);
// list of all tags found
            List<ExifTag> all_tags = exif.getAllTags();
            if (all_tags != null) {
                for (ExifTag tag : all_tags) {
                    if (tag != null) {
                        int tagId = ExifInterface.defineTag(tag.getIfd(), tag.getTagId());
                        String str = tag.forceGetValueAsString();
                        if (str != null) {
                            str = str.trim();
                            map.put(tagDes.get(tagId), str);
                        }
                        //System.out.println( tagDes.get(tagId)+" : "+ str);
                        //System.out.println( tag.toString());

                    } else {
                        System.out.println("tag is null");
                    }
                }
            } else {
                System.out.println(file.getAbsolutePath() + "  no exif info");
            }
// jpeg quality
            int jpeg_quality = exif.getQualityGuess();
            map.put("jpeg_quality", jpeg_quality + "");

            System.out.println(file.getAbsolutePath() + "  quality:" + jpeg_quality);
            return map;


        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return map;
    }


    /**
     * 根据文件路径拷贝文件
     *
     * @param src      源文件
     * @param destPath 目标文件路径
     * @return boolean 成功true、失败false
     */
    static boolean copyFile(File src, String destPath) {
        boolean result = false;
        if ((src == null) || (destPath == null)) {
            return result;
        }
        File dest = new File(destPath);
        if (dest != null && dest.exists()) {
            dest.delete(); // delete file
        }
        try {
            dest.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;

        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dest).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
