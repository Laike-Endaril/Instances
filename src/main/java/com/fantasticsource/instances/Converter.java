package com.fantasticsource.instances;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.PlayerData;
import com.fantasticsource.tools.Tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class Converter
{
    public static void convert() throws IOException
    {
        File personalFolder = new File(MCTools.getConfigDir() + ".." + File.separator + "world" + File.separator + "personal");
        if (!personalFolder.isDirectory()) return;

        File[] folders = personalFolder.listFiles();
        if (folders == null) return;

        File skyroomFolder = new File(MCTools.getConfigDir() + ".." + File.separator + "world" + File.separator + "instances" + File.separator + "Skyroom");
        skyroomFolder.mkdirs();
        for (File folder : folders)
        {
            File regionFolder = new File(folder.getAbsolutePath() + File.separator + "region");
            if (regionFolder.exists())
            {
                String name = folder.getName().replaceAll("'s_[sS]kyroom_[-]?[0-9]+$", "");
                UUID id = PlayerData.getID(name);

                File newFolder = new File(skyroomFolder.getAbsolutePath() + File.separator + (id != null ? id : folder.getName()));
                boolean original = true;
                while (newFolder.exists())
                {
                    original = false;
                    newFolder = new File(newFolder.getAbsolutePath() + "_");
                }
                folder.renameTo(newFolder);


                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(newFolder.getAbsolutePath() + File.separator + "instanceData.txt")));
                writer.write("0\r\n");
                if (original && id != null) writer.write(id + "\r\n");
                writer.close();
            }
            else Tools.deleteFilesRecursively(folder);
        }
    }
}
