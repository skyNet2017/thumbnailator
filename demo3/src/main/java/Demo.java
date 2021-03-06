import java.io.File;

public class Demo {

    public static void main(String[] args) {


        File file = new File("/Users/hss/Downloads/IMG_20200628_111551.jpg");
        File out = new File("/Users/hss/Downloads/IMG_20200628_111551-compressed.jpg");
        File out2 = new File("/Users/hss/Downloads/thumbnail.IMG_20200628_111551.jpg");

        File dir = new File("D:\\00压缩测试");
        File dir2 = new File("D:\\00压缩测试\\thumbnailator");
        dir2.mkdirs();
        File[] files = dir.listFiles();
        long start = System.currentTimeMillis();
        long sizeBefore = 0;
        long sizeAfter = 0;
        for (File file1 : files) {
            if (file1.getName().endsWith(".jpg") || file1.getName().endsWith(".JPG") || file1.getName().endsWith(".webp") || file1.getName().endsWith(".png")
                    || file1.getName().endsWith(".gif") || file1.getName().endsWith(".jpeg")) {
                sizeBefore += file1.length();
                File destFile = new File(dir2, file1.getName());
                ImageCompressor.compressToQuality(file1.getAbsolutePath(), destFile.getAbsolutePath(), 80);
                sizeAfter += destFile.length();
                if (destFile.length() == 0) {
                    System.out.println("压缩/拷贝失败:" + destFile);
                }
            }
        }
        System.out.println("cost:" + (System.currentTimeMillis() - start) / 1000 + "s,文件总大小:"
                + ImageCompressor.formatFileSize(sizeBefore) + " -> " + ImageCompressor.formatFileSize(sizeAfter));

       /* String path = "D:\\Nikon\\DSC_0229.JPG";
        File in = new File(path);
        File out3 = new File(in.getParentFile(),"compress3-"+in.getName());
        ImageCompressor.compressToQuality(path,out3.getAbsolutePath(),80);*/


    }


}
