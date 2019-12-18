package com.masaar.limp_rxjava;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;

public class LimpFile {

    private static final String TAG = "LimpFile";

    public String name;
    public long size;
    public String type;
    public String lastModified;
    public int[] content;

    public File mFile;
    private BufferedInputStream stream;
    private int mBytesRead;


    public LimpFile(String fullFilePath) {

        //for prepare:
        mFile = new File(fullFilePath);
        this.name = mFile.getName();
        this.size = mFile.length();

        //for fileType:
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            this.type = "";
        }
        this.type = name.substring(lastIndexOf);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.lastModified = sdf.format(mFile.lastModified());

        //for file content:
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try {
            stream = new BufferedInputStream(new FileInputStream(mFile));
            mBytesRead =  stream.read(buffer);
            byteBuffer.write(buffer, 0, mBytesRead);
        }catch (IOException e){
            System.out.println(e);
        }

        IntBuffer intBuf =
                ByteBuffer.wrap(byteBuffer.toByteArray())
                        .order(ByteOrder.BIG_ENDIAN)
                        .asIntBuffer();
        this.content = new int[intBuf.remaining()];
        intBuf.get(content);

    }

    JSONObject getDocObject() {

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("name", name);
            jsonObject.put("size", size);
            jsonObject.put("type", type);
            jsonObject.put("lastModified", lastModified);
            jsonObject.put("content", content);

        } catch (JSONException e) {
            System.out.println(e);
        }
        return jsonObject;
    }
}

