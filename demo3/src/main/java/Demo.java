import java.io.File;

public class Demo {

    public static void main(String[] args) {
        //File dir = new File("D:\\00imagecompress");
       // ImageCompressor.compressImagesInDir(dir.getAbsolutePath(),85);

       /* File dir = new File("D:\\迅雷下载\\【泄密资源】高校毕业生母狗雯雯怀孕被绿帽渣男流出 附高清无水印照488P\\P");
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

        String path = "/Users/hss/java/thumbnailator/testpic/1601370950825-IMG_20190923_230641.jpg";
        File in = new File(path);
        File out3 = new File(in.getParentFile(),"compress3-"+in.getName());
        ImageCompressor.compressToQuality(path,out3.getAbsolutePath(),40);


    }


}
