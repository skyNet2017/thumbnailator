import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageCompressor {

     static Map<Integer,String> tagDes = getDes();


    private static Map<Integer, String> getDes() {
        tagDes = new HashMap<Integer,String>();
        Class clazz =  ExifInterface.class;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                boolean isTarget =  Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers());
                if(!isTarget){
                    continue;
                }
                String name = field.getName();
                if(name.startsWith("TAG_")){
                    name = name.substring("TAG_".length()).toLowerCase();
                    tagDes.put((Integer) field.get(clazz),name);
                }
            }catch (Throwable throwable){
                throwable.printStackTrace();
            }

        }
        return tagDes;

    }

    public static boolean compressToQuality(String path,int quality){
        if(tagDes == null || tagDes.isEmpty()){
            tagDes = getDes();
        }
        try {
            File file = new File(path);
            String name = file.getName();

         /* String type =   MimeTable.loadTable().getContentTypeFor(name);
          if(type == null){
              System.out.println( "未知mime类型 : "+name);
              return true;
          }
          if(!type.contains("image")){
              System.out.println( "不是图片类型 : "+type+" , name: "+name);
              return true;
          }*/

            if(file.length() < 50*1024){
                System.out.println( "文件小于50k,不压 :"+path);
                return true;
            }
            File out = null;
            boolean deleteOriginal = false;

            if(name.endsWith(".webp")){
                System.out.println( "webp 不压缩");
                return true;
            }
            if(name.endsWith(".gif")){
                System.out.println( "gif 不压缩");
                return true;
            }
            if(name.endsWith(".png") || name.endsWith(".PNG")){
                path = path.substring(0,path.lastIndexOf("."))+".jpg";
                out = new File(path);
                deleteOriginal = true;
                System.out.println( "png 压缩后更换后缀,并删除原文件:"+path);
            }
            if(out == null){
                out = file;
            }
            ExifInterface exif  =  printExif(file);
            if(exif != null){
                int jpeg_quality =  exif.getQualityGuess();
                if(jpeg_quality < quality && jpeg_quality > 0){
                    System.out.println( "图片质量>0且小于"+quality+",无需压缩:"+file.getAbsolutePath());
                    return true;
                }
            }


            Thumbnails.of(file)
                    .outputFormat("jpg")
                    .outputQuality(quality*1.0f/100f)
                    .scale(1.0)
                    .toFile(out);
            if(exif != null){
                exif.writeExif(out.getAbsolutePath());
                printExif(out);
            }

            if(deleteOriginal){
               boolean success =  file.delete();
                System.out.println( "png 压缩后删除原文件 是否成功:"+success+" , path: "+path);
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }

    }



    private static ExifInterface printExif(File file) {
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif( file.getAbsolutePath(), ExifInterface.Options.OPTION_ALL );
// list of all tags found
            List<ExifTag> all_tags = exif.getAllTags();
            if(all_tags != null){
                //String tags = Arrays.toString(all_tags.toArray()).replace(",","\n");
                System.out.println(file.getAbsolutePath()+"\n");


                for (ExifTag tag : all_tags) {
                    if(tag != null){
                        int tagId = ExifInterface.defineTag(tag.getIfd(),tag.getTagId());
                        String str = tag.forceGetValueAsString();
                        if(str != null){
                            str = str.trim();
                        }
                        System.out.println( tagDes.get(tagId)+" : "+ str);
                        //System.out.println( tag.toString());

                    }else {
                        System.out.println( "tag is null");
                    }
                }
            }else {
                System.out.println(file.getAbsolutePath()+"  no exif info");
            }
// jpeg quality
            int jpeg_quality =  exif.getQualityGuess();

            System.out.println(file.getAbsolutePath()+"  quality:"+jpeg_quality);
            if(all_tags == null || all_tags.isEmpty()){
                return null;
            }
            return exif;


        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        return exif;
    }

    public static Map<String,String> getExifInfo(File file){
        ExifInterface exif = new ExifInterface();
        Map<String,String> map = new HashMap<String, String>();
        try {
            exif.readExif( file.getAbsolutePath(), ExifInterface.Options.OPTION_ALL );
// list of all tags found
            List<ExifTag> all_tags = exif.getAllTags();
            if(all_tags != null){
                for (ExifTag tag : all_tags) {
                    if(tag != null){
                        int tagId = ExifInterface.defineTag(tag.getIfd(),tag.getTagId());
                        String str = tag.forceGetValueAsString();
                        if(str != null){
                            str = str.trim();
                            map.put(tagDes.get(tagId),str);
                        }
                        //System.out.println( tagDes.get(tagId)+" : "+ str);
                        //System.out.println( tag.toString());

                    }else {
                        System.out.println( "tag is null");
                    }
                }
            }else {
                System.out.println(file.getAbsolutePath()+"  no exif info");
            }
// jpeg quality
            int jpeg_quality =  exif.getQualityGuess();
            map.put("jpeg_quality",jpeg_quality+"");

            System.out.println(file.getAbsolutePath()+"  quality:"+jpeg_quality);
            return map;


        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        return map;
    }
}
