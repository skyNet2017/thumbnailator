import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.quality.Magick;
import net.sourceforge.jheader.App1Header;
import net.sourceforge.jheader.JpegHeaders;
import net.sourceforge.jheader.TagValue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.SortedMap;

public class Demo {

    public static void main(String[] args) {
        //File dir = new File("D:\\00imagecompress");
       // ImageCompressor.compressImagesInDir(dir.getAbsolutePath(),85);

       /*
        File dir2 = new File(dir,"thumbnailator");
        dir2.mkdirs();
        File[] files = dir.listFiles();
        long start = System.currentTimeMillis();
        long sizeBefore = 0;
        long sizeAfter = 0;
        for (File file1 : files) {
            if (file1.getName().endsWith(".jpg") || file1.getName().endsWith(".JPG")
                    || file1.getName().endsWith(".png")
                    || file1.getName().endsWith(".PNG")
                    || file1.getName().endsWith(".jpeg") || file1.getName().endsWith(".JPEG")) {
                sizeBefore += file1.length();
                File destFile = new File(dir2, file1.getName());
                ImageCompressor.compressToQuality(file1.getAbsolutePath(), 80);
                sizeAfter += destFile.length();
                if (destFile.length() == 0) {
                    System.out.println("压缩/拷贝失败:" + destFile);
                }
            }
        }
        System.out.println("cost:" + (System.currentTimeMillis() - start) / 1000 + "s,文件总大小:"
                + ImageCompressor.formatFileSize(sizeBefore) + " -> " + ImageCompressor.formatFileSize(sizeAfter));*/

        //String path = "/Users/hss/Library/Containers/com.tencent.WeWorkMac/Data/Library/Application Support/WXWork/Data/1688853798357608/Cache/File/2021-03/2301_11373007.jpg";
        //String path = "/Users/hss/github/Luban/example/src/main/assets/imgs/webp_大图小字-思维导图-增长黑客_2944x5434.webp";
//        String path = "/Users/hss/1download/xx/comp/22/婺源2.jpeg";
//        File in = new File(path);
//        int[] qulitys = new int[]{0,10,20,30,50,70,80,90,96,100};
//        for (int qulity : qulitys) {
//            File out3 = new File(in.getParentFile(),qulity+"-"+in.getName());
//            ImageCompressor.compressToQuality(path,out3.getAbsolutePath(),qulity);











        //}
        String[] files = new String[]{
                "t_gyRZ8jGTlsJb57HHY7tLOBTXWQjPmagQdF6IkI_ts.jpeg",
                "scW_Sigamncb367TZwP743e5JKX0jZV8ONZ5aoY413I.jpeg"
        };

        String path = "/Users/hss/Downloads";
        File dir = new File(path);


        /*File file = new File(dir,"DSC_0217.JPG");
        for (int i = 0; i < 100; i+=10) {
            int quality = i;
            File out = new File(dir,"DSC_0217-"+quality+".JPG");
            ImageCompressor.compressToQuality(file.getAbsolutePath(),out.getAbsolutePath(),quality);
        }*/

        /*DSC_0217.JPG*/

        File[] files1 = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                //return s.endsWith(".jpg") || s.endsWith(".JPG") || s.endsWith(".jpeg");
                return s.contains("DSC_0217");
            }
        });
        for (int i = 0; i < files1.length; i++) {
            File in = files1[i];
            //ImageCompressor.printExif(in,null);
            ExifInterface exif = new ExifInterface();
            int quality1 = 0;
            try {
                exif.readExif(in.getAbsolutePath(), ExifInterface.Options.OPTION_ALL);
                quality1 = exif.getQualityGuess();
                System.out.println(in.getName()+" quality1:"+quality1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int quality2 =   new Magick().getJPEGImageQuality(in);
          System.out.println(in.getName()+" quality2:"+quality2);
        }

        /*try {
            jheaderdemo(path);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //ImageCompressor.printExif(in,null);


    }

    private static void jheaderdemo(String path) throws Exception{
        //jpg_5946x2258_喀纳斯广角

        JpegHeaders jpegHeaders = new JpegHeaders(path);

        // EXIF
        App1Header exifHeader = jpegHeaders.getApp1Header();

        // 遍历显示EXIF
        System.out.println(" ");
        System.out.println("-----jheader----->");
        SortedMap<App1Header.Tag, TagValue> tags = exifHeader.getTags();
        for (Map.Entry<App1Header.Tag, TagValue> entry : tags.entrySet()) {
            System.out.println(entry.getKey().name + ": " + entry.getValue());
        }

        // 修改EXIF的拍照日期
       // exifHeader.setValue(App1Header.Tag.DATETIMEORIGINAL, "2007:11:04 07:42:56");
        // 保存
       // jpegHeaders.save(true);
    }


}
