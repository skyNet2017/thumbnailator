import java.io.File;

public class Demo {

    public static void main(String[] args){




        File file = new File("/Users/hss/Downloads/IMG_20200628_111551.jpg");
        File out = new File("/Users/hss/Downloads/IMG_20200628_111551-compressed.jpg");
        File out2 = new File("/Users/hss/Downloads/thumbnail.IMG_20200628_111551.jpg");

        /*File dir = new File("/Users/hss/Downloads");
        File[] files = dir.listFiles();
        for (File file1 : files) {
            if(file1.getName().endsWith(".jpg") || file1.getName().endsWith(".webp") || file1.getName().endsWith(".png")
                    || file1.getName().endsWith(".gif") || file1.getName().endsWith(".jpeg")){
                ImageCompressor.compressToQuality(file1.getAbsolutePath(),80);
            }

        }*/

        ImageCompressor.compressToQuality(file.getAbsolutePath(),70);



    }


}
