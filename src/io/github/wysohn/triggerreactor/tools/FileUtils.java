package io.github.wysohn.triggerreactor.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {
    public static void writeToFile(File file, String str) throws IOException{
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File temp = File.createTempFile(file.getName(), ".tmp", file.getParentFile());

        try(FileWriter fw = new FileWriter(temp)){
            fw.write(str);
        }catch(IOException e){
            throw e;
        }

        try (FileInputStream istream = new FileInputStream(temp);
                FileOutputStream ostream = new FileOutputStream(file)) {
            FileChannel src = istream.getChannel();
            FileChannel dest = ostream.getChannel();
            dest.transferFrom(src, 0, src.size());
        } catch (IOException e) {
            throw e;
        }

        temp.delete();
    }
}
