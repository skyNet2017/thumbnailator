import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FileScan {

    static ExecutorService service;
    static AtomicInteger count;

    public static void main(String[] ar) {
        service = Executors.newFixedThreadPool(8);
        long start = System.currentTimeMillis();
        count = new AtomicInteger(0);
        File[] files = File.listRoots();
        for (File file : files) {
            System.out.println("disk:" + file.getAbsolutePath());
            scanDir(file);
        }
        System.out.println("list dir cost:(min)" + (System.currentTimeMillis() - start) / 1000 / 60);
    }

    private static void scanDir(final File dir) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                int count1 = count.incrementAndGet();
                System.out.println("dir start:" + dir.getAbsolutePath() + ", count:" + count1);
                File[] files = dir.listFiles();
                if (files == null) {
                    int count2 = count.decrementAndGet();
                    System.out.println("dir is empty:" + dir.getAbsolutePath() + ", count:" + count2);
                    if (count2 == 0) {
                        onFinish(dir);
                    }
                    return;
                }
                for (File file1 : files) {
                    if (file1.isDirectory()) {
                        scanDir(file1);
                    } else {
                        // 这里处理具体文件:

                        System.out.println("file :" + file1.getAbsolutePath());
                    }
                }
                int count2 = count.decrementAndGet();
                System.out.println("dir end:" + dir.getAbsolutePath() + ", count:" + count2);
                if (count2 == 0) {
                    onFinish(dir);
                }
            }
        });


    }

    private static void onFinish(File dir) {
        System.out.println("磁盘遍历完成:" + dir.getAbsolutePath());
    }
}
